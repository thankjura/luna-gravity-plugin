package ru.slie.luna.plugins.gravity.script.groovy.analysis;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;

import java.util.*;

/**
 * Walks the (repaired) script AST up to the cursor, tracking visible local variables with their inferred types.
 * When it meets the completion placeholder it captures the scope and the receiver expression.
 */
public final class ScopeCollector extends ClassCodeVisitorSupport {
    public record Capture(Map<String, JType> variables, Expression receiver, boolean spread) {}

    private record ClosureOwner(MethodCallExpression call, int index, int argCount) {}

    private final TypeResolver resolver;
    private final int line;
    private final int column;
    private final Deque<Map<String, JType>> scopes = new ArrayDeque<>();
    private final Map<String, JType> declaredBeforeCursor = new LinkedHashMap<>();
    private final Map<ClosureExpression, ClosureOwner> closureOwners = new IdentityHashMap<>();
    private Capture capture;

    private ScopeCollector(TypeResolver resolver, Map<String, JType> globals, int line, int column) {
        this.resolver = resolver;
        this.line = line;
        this.column = column;
        this.scopes.push(new LinkedHashMap<>(globals));
    }

    /**
     * @param line   1-based line of the placeholder (cursor)
     * @param column 1-based column of the placeholder
     */
    public static ScopeCollector collect(ModuleNode module, TypeResolver resolver, Map<String, JType> globals, int line, int column) {
        ScopeCollector collector = new ScopeCollector(resolver, globals, line, column);
        if (module != null) {
            collector.visitModule(module);
        }
        return collector;
    }

    /** Capture at the placeholder, or null if the placeholder line could not be parsed. */
    public Capture capture() {
        return capture;
    }

    /** Variables visible at the cursor (exact when the placeholder was found, approximate otherwise). */
    public Map<String, JType> visibleVariables() {
        if (capture != null) {
            return capture.variables();
        }
        Map<String, JType> out = new LinkedHashMap<>(scopes.getLast());
        out.putAll(declaredBeforeCursor);
        return out;
    }

    private void visitModule(ModuleNode module) {
        for (ClassNode cn : module.getClasses()) {
            for (MethodNode mn : cn.getMethods()) {
                boolean scriptBody = cn.isScript() && mn.getName().equals("run") && mn.getParameters().length == 0;
                if (cn.isScript() && !scriptBody && !mn.getName().equals("main") && mn.getLineNumber() > 0) {
                    resolver.addScriptMethod(mn);
                }
                if (mn.getCode() == null) {
                    continue;
                }
                if (scriptBody) {
                    mn.getCode().visit(this);
                } else if (contains(mn)) {
                    scopes.push(new LinkedHashMap<>());
                    for (Parameter p : mn.getParameters()) {
                        declare(p.getName(), p.isDynamicTyped() ? JType.OBJECT : resolver.fromClassNode(p.getType()));
                    }
                    mn.getCode().visit(this);
                    scopes.pop();
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------ scopes

    private JType lookup(String name) {
        for (Map<String, JType> scope : scopes) {
            JType t = scope.get(name);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    private Map<String, JType> snapshot() {
        Map<String, JType> out = new LinkedHashMap<>();
        Iterator<Map<String, JType>> it = scopes.descendingIterator();
        while (it.hasNext()) {
            out.putAll(it.next());
        }
        return out;
    }

    private void declare(String name, JType type) {
        if (name == null || name.startsWith(ScriptParser.PLACEHOLDER)) {
            return;
        }
        JType t = type == null ? JType.OBJECT : type;
        scopes.peek().put(name, t);
        declaredBeforeCursor.put(name, t);
    }

    private void doCapture(Expression receiver, boolean spread) {
        if (capture == null) {
            capture = new Capture(snapshot(), receiver, spread);
        }
    }

    private boolean isAfterCursor(ASTNode node) {
        int l = node.getLineNumber();
        return l > line || (l == line && node.getColumnNumber() > column);
    }

    private boolean contains(ASTNode node) {
        if (node.getLineNumber() < 0) {
            return true;
        }
        boolean afterStart = node.getLineNumber() < line || (node.getLineNumber() == line && node.getColumnNumber() <= column);
        boolean beforeEnd = node.getLastLineNumber() > line || (node.getLastLineNumber() == line && node.getLastColumnNumber() >= column);
        return afterStart && beforeEnd;
    }

    // ---------------------------------------------------------------------------------------------------- visitors

    @Override
    public void visitBlockStatement(BlockStatement block) {
        scopes.push(new LinkedHashMap<>());
        for (Statement statement : block.getStatements()) {
            if (statement.getLineNumber() > 0 && isAfterCursor(statement)) {
                break;
            }
            statement.visit(this);
        }
        scopes.pop();
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        expression.getRightExpression().visit(this);
        if (expression.isMultipleAssignmentDeclaration()) {
            for (Expression e : expression.getTupleExpression().getExpressions()) {
                if (e instanceof VariableExpression v) {
                    declare(v.getName(), v.isDynamicTyped() ? JType.OBJECT : resolver.fromClassNode(v.getOriginType()));
                }
            }
            return;
        }
        VariableExpression v = expression.getVariableExpression();
        JType type = null;
        if (!v.isDynamicTyped()) {
            type = resolver.fromClassNode(v.getOriginType());
        }
        if (type == null && !(expression.getRightExpression() instanceof EmptyExpression)) {
            type = resolver.resolveType(expression.getRightExpression(), this::lookup);
        }
        declare(v.getName(), type);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (!contains(expression)) {
            return;
        }
        scopes.push(new LinkedHashMap<>());
        Parameter[] params = expression.getParameters();
        int count = expression.isParameterSpecified() ? params.length : 1;
        ClosureOwner owner = closureOwners.get(expression);
        List<JType> inferred = owner == null ? List.of()
                : resolver.closureParameterTypes(owner.call(), owner.index(), owner.argCount(), count, this::lookup);
        if (!expression.isParameterSpecified()) {
            declare("it", inferred.isEmpty() ? JType.OBJECT : inferred.getFirst());
        } else {
            for (int i = 0; i < params.length; i++) {
                Parameter p = params[i];
                JType t = !p.isDynamicTyped() ? resolver.fromClassNode(p.getType()) : (i < inferred.size() ? inferred.get(i) : null);
                declare(p.getName(), t);
            }
        }
        if (expression.getCode() != null) {
            expression.getCode().visit(this);
        }
        scopes.pop();
    }

    @Override
    public void visitForLoop(ForStatement loop) {
        loop.getCollectionExpression().visit(this);
        if (!contains(loop)) {
            return;
        }
        scopes.push(new LinkedHashMap<>());
        Parameter variable = loop.getVariable();
        if (variable != null && variable != ForStatement.FOR_LOOP_DUMMY) {
            JType t = null;
            if (!variable.isDynamicTyped()) {
                t = resolver.fromClassNode(variable.getType());
            }
            if (t == null) {
                JType collection = resolver.resolveType(loop.getCollectionExpression(), this::lookup);
                t = collection == null ? null : collection.elementType();
            }
            declare(variable.getName(), t);
        }
        Parameter index = loop.getIndexVariable();
        if (index != null) {
            declare(index.getName(), JType.INTEGER);
        }
        loop.getLoopBlock().visit(this);
        scopes.pop();
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        if (!contains(statement)) {
            return;
        }
        scopes.push(new LinkedHashMap<>());
        Parameter p = statement.getVariable();
        declare(p.getName(), resolver.fromClassNode(p.getType()));
        statement.getCode().visit(this);
        scopes.pop();
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        if (expression.getName().startsWith(ScriptParser.PLACEHOLDER)) {
            doCapture(null, false);
        }
        super.visitVariableExpression(expression);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        String name = expression.getPropertyAsString();
        if (name != null && name.startsWith(ScriptParser.PLACEHOLDER)) {
            expression.getObjectExpression().visit(this);
            doCapture(expression.getObjectExpression(), expression.isSpreadSafe());
            return;
        }
        super.visitPropertyExpression(expression);
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        if (call.getArguments() instanceof TupleExpression args) {
            List<Expression> list = args.getExpressions();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof ClosureExpression closure) {
                    closureOwners.put(closure, new ClosureOwner(call, i, list.size()));
                }
            }
        }
        String name = call.getMethodAsString();
        if (name != null && name.startsWith(ScriptParser.PLACEHOLDER)) {
            if (!call.isImplicitThis()) {
                call.getObjectExpression().visit(this);
            }
            doCapture(call.isImplicitThis() ? null : call.getObjectExpression(), call.isSpreadSafe());
            call.getArguments().visit(this);
            return;
        }
        super.visitMethodCallExpression(call);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
}
