package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.slie.luna.plugins.gravity.script.groovy.completion.ClassPropertyProvider;
import ru.slie.luna.plugins.gravity.script.groovy.completion.LunaBeanProvider;
import ru.slie.luna.plugins.gravity.script.groovy.model.AutocompleteRange;
import ru.slie.luna.plugins.gravity.script.groovy.model.AutocompleteResult;
import ru.slie.luna.plugins.gravity.script.groovy.model.SignatureHelp;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Component
public class AutocompleteGroovyService {
    private final static Logger log = LoggerFactory.getLogger(AutocompleteGroovyService.class);
    private static final String LUNA_PLACEHOLDER = "__LUNA_PLACEHOLDER__";

    private final LunaBeanProvider beanProvider;
    private final ClassPropertyProvider propertyProvider;

    public AutocompleteGroovyService(LunaBeanProvider beanProvider,
                                     ClassPropertyProvider propertyProvider) {
        this.beanProvider = beanProvider;
        this.propertyProvider = propertyProvider;
    }

    private ASTNode getNode(String scriptText, int line, int column) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setTolerance(10);
        CompilationUnit cu = new CompilationUnit();
        SourceUnit su = cu.addSource(new SourceUnit("script.groovy", scriptText, config, null, new CustomErrorCollector(config)));

        NodeFinder nodeFinder = new NodeFinder(line, column);
        try {
            cu.compile(Phases.CANONICALIZATION);
            ModuleNode module = su.getAST();
            module.getStatementBlock().visit(nodeFinder);
        } catch (Exception e) {
            log.error("parse error", e);
        }

        return nodeFinder.getFoundNode();
    }

    public int getActiveParam(ArgumentListExpression list, int column) {
        List<Expression> expressions = list.getExpressions();
        for (int i = 0; i < expressions.size(); i++) {
            Expression arg = expressions.get(i);
            if (column <= arg.getLastColumnNumber()) {
                return i;
            }
        }
        return expressions.size();
    }

    public SignatureHelp getSignatureHelp(String scriptText, int line, int column) {
        scriptText += ")";

        ASTNode node = getNode(scriptText, line, column);
        SignatureHelp signatureHelp = new SignatureHelp();

        if (node == null) {
            return signatureHelp;
        }

        if (node instanceof MethodCallExpression call) {
            Expression objectExpr = call.getObjectExpression();
            Class<?> targetClass = resolveClass(objectExpr);
            if (targetClass == null) {
                return signatureHelp;
            }
            String methodName = call.getMethodAsString();
            List<Method> methods = Arrays.stream(targetClass.getMethods())
                                           .filter(m -> m.getName().equals(methodName))
                                           .toList();

            Expression arguments = call.getArguments();
            if (arguments instanceof ArgumentListExpression list) {
                signatureHelp.setActiveParameter(getActiveParam(list, column));
                // TODO: signatureHelp.setActiveSignature(calcSignature)
            }

            for (Method method: methods) {
                List<String> paramLabels = Arrays.stream(method.getParameters())
                                                   .map(p -> p.getType().getSimpleName() + " " + p.getName())
                                                   .toList();
                String returnType = method.getReturnType().getSimpleName();
                String paramsJoined = String.join(", ", paramLabels);

                SignatureHelp.SignatureInfo info = new SignatureHelp.SignatureInfo(String.format("%s %s(%s)", returnType, methodName, paramsJoined));
                for (String paramLabel: paramLabels) {
                    info.addParameter(paramLabel);
                }

                signatureHelp.addSignature(info);
            }

        }

        return signatureHelp;
    }

    public AutocompleteResult getSuggestions(String scriptText, int line, int column, int limit) {
        if (scriptText.endsWith(".")) {
            scriptText += "__LUNA_PLACEHOLDER__";
        }

        ASTNode node = getNode(scriptText, line, column);
        AutocompleteResult result = new AutocompleteResult();

        if (node == null) {
            return result;
        }

        result.setRange(AutocompleteRange.forNode(node));
        switch (node) {
            case VariableExpression e -> {
                if (e.isDynamicTyped()) {
                    result.addResult(beanProvider.getSuggestions(e.getText().replaceAll(LUNA_PLACEHOLDER, "").toLowerCase(), limit));
                }
            }
            case PropertyExpression e -> {
                Expression exp = e.getObjectExpression();
                if (exp != null) {
                    Class<?> clazz = resolveClass(exp);
                    if (clazz != null) {
                        result.setRange(AutocompleteRange.forNode(e.getProperty()));
                        String prefix = e.getPropertyAsString().replaceAll(LUNA_PLACEHOLDER, "").toLowerCase();
                        boolean isStaticContext = (exp instanceof ClassExpression);
                        result.addResult(propertyProvider.getSuggestions(prefix, clazz, !isStaticContext, limit));
                    }
                }
            }
            default -> {}
        }
        return result;
    }

    private Class<?> resolveClass(Expression exp) {
        // UserManager
        if (exp instanceof ClassExpression ce) {
            return ce.getType().getTypeClass();
        }

        // um
        if (exp instanceof VariableExpression ve) {
            return ve.getType().getTypeClass();
        }

        // System.out
        if (exp instanceof PropertyExpression pe) {
            Class<?> parentClazz = resolveClass(pe.getObjectExpression());
            if (parentClazz != null) {
                try {
                    String propName = pe.getPropertyAsString();
                    return parentClazz.getField(propName).getType();
                } catch (Exception ex) {
                    // TODO: try getters
                }
            }
        }

        try {
            return exp.getType().getTypeClass();
        } catch (Exception ex) {
            return null;
        }
    }
}
