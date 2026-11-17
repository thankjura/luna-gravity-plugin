package ru.slie.luna.plugins.gravity.script.groovy.analysis;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.syntax.Types;
import ru.slie.luna.plugins.gravity.script.groovy.index.ClassEntry;
import ru.slie.luna.plugins.gravity.script.groovy.index.ClassIndex;
import ru.slie.luna.plugins.gravity.script.groovy.index.GdkMethodIndex;

import java.lang.reflect.*;
import java.util.*;

/**
 * Infers types of Groovy AST expressions using reflection: variables, method chains with generics,
 * properties (getters), GDK methods, closure parameters.
 */
public final class TypeResolver {
    /** GDK iteration methods: the closure receives the element (or entry / key, value for maps). */
    private static final Set<String> ITERATION_METHODS = Set.of(
            "each", "eachWithIndex", "collect", "collectMany", "collectEntries", "find", "findAll", "findResult",
            "findResults", "findIndexOf", "findIndexValues", "findLastIndexOf", "any", "every", "count", "countBy",
            "groupBy", "sum", "min", "max", "sort", "toSorted", "unique", "toUnique", "takeWhile", "dropWhile",
            "split", "removeAll", "retainAll", "removeIf", "forEach", "withIndex", "indexed", "reverseEach",
            "collectWithIndex", "partitionPoint", "inject", "toSet", "asType"
    );

    public interface Scope {
        JType lookup(String name);
    }

    public record ResolvedType(JType type, boolean staticRef, String importFqn) {
        static ResolvedType instance(JType type) {
            return type == null ? null : new ResolvedType(type, false, null);
        }
    }

    private final ClassIndex index;
    private final GdkMethodIndex gdk;
    private final ImportsInfo imports;
    private final Map<String, MethodNode> scriptMethods = new HashMap<>();

    public TypeResolver(ClassIndex index, GdkMethodIndex gdk, ImportsInfo imports) {
        this.index = index;
        this.gdk = gdk;
        this.imports = imports;
    }

    public void addScriptMethod(MethodNode method) {
        scriptMethods.putIfAbsent(method.getName(), method);
    }

    public Map<String, MethodNode> scriptMethods() {
        return scriptMethods;
    }

    public ResolvedType resolve(Expression e, Scope scope) {
        try {
            return doResolve(e, scope, 0);
        } catch (Throwable t) {
            return null;
        }
    }

    public JType resolveType(Expression e, Scope scope) {
        ResolvedType r = resolve(e, scope);
        return r == null || r.staticRef() ? (r == null ? null : JType.of(Class.class, r.type())) : r.type();
    }

    private ResolvedType doResolve(Expression e, Scope scope, int depth) {
        if (e == null || depth > 30) {
            return null;
        }
        switch (e) {
            case ClassExpression ce -> {
                JType t = fromClassNode(ce.getType());
                return t == null ? null : new ResolvedType(t, true, null);
            }
            case VariableExpression ve -> {
                String name = ve.getName();
                if ("this".equals(name) || "super".equals(name)) {
                    return null;
                }
                JType t = scope.lookup(name);
                if (t != null) {
                    return ResolvedType.instance(t);
                }
                if (!ve.isDynamicTyped()) {
                    return ResolvedType.instance(fromClassNode(ve.getOriginType()));
                }
                return classByName(name);
            }
            case ConstantExpression c -> {
                Object value = c.getValue();
                return value == null ? null : ResolvedType.instance(JType.of(value.getClass()));
            }
            case GStringExpression ignored -> {
                return ResolvedType.instance(JType.STRING);
            }
            case ListExpression le -> {
                JType element = le.getExpressions().isEmpty() ? JType.OBJECT : orObject(type(le.getExpression(0), scope, depth));
                return ResolvedType.instance(JType.of(ArrayList.class, element));
            }
            case MapExpression me -> {
                if (me.getMapEntryExpressions().isEmpty()) {
                    return ResolvedType.instance(JType.of(LinkedHashMap.class, JType.OBJECT, JType.OBJECT));
                }
                MapEntryExpression first = me.getMapEntryExpressions().getFirst();
                JType key = first.getKeyExpression() instanceof ConstantExpression ? JType.STRING : orObject(type(first.getKeyExpression(), scope, depth));
                return ResolvedType.instance(JType.of(LinkedHashMap.class, key, orObject(type(first.getValueExpression(), scope, depth))));
            }
            case RangeExpression ignored -> {
                return ResolvedType.instance(JType.of(List.class, JType.INTEGER));
            }
            case ClosureExpression ignored -> {
                return ResolvedType.instance(JType.CLOSURE);
            }
            case MethodPointerExpression ignored -> {
                return ResolvedType.instance(JType.CLOSURE);
            }
            case BooleanExpression ignored -> {
                return ResolvedType.instance(JType.BOOLEAN);
            }
            case CastExpression ce -> {
                return ResolvedType.instance(fromClassNode(ce.getType()));
            }
            case ConstructorCallExpression cc -> {
                return ResolvedType.instance(fromClassNode(cc.getType()));
            }
            case ArrayExpression ae -> {
                Class<?> component = classOf(ae.getElementType());
                return component == null ? null : ResolvedType.instance(JType.of(Array.newInstance(component, 0).getClass()));
            }
            case TernaryExpression te -> {
                ResolvedType r = doResolve(te.getTrueExpression(), scope, depth + 1);
                return r != null ? r : doResolve(te.getFalseExpression(), scope, depth + 1);
            }
            case DeclarationExpression de -> {
                return doResolve(de.getRightExpression(), scope, depth + 1);
            }
            case BinaryExpression be -> {
                return resolveBinary(be, scope, depth);
            }
            case StaticMethodCallExpression sm -> {
                JType owner = fromClassNode(sm.getOwnerType());
                if (owner == null) {
                    return null;
                }
                return resolveCall(new ResolvedType(owner, true, null), sm.getMethod(), arguments(sm.getArguments()), scope, false, depth);
            }
            case MethodCallExpression mc -> {
                String name = mc.getMethodAsString();
                if (name == null) {
                    return null;
                }
                if (mc.isImplicitThis()) {
                    MethodNode method = scriptMethods.get(name);
                    return method == null ? null : ResolvedType.instance(fromClassNode(method.getReturnType()));
                }
                ResolvedType receiver = doResolve(mc.getObjectExpression(), scope, depth + 1);
                if (receiver == null) {
                    return null;
                }
                return resolveCall(receiver, name, arguments(mc.getArguments()), scope, mc.isSpreadSafe(), depth);
            }
            case PropertyExpression pe -> {
                String name = pe.getPropertyAsString();
                if (name == null) {
                    return null;
                }
                ResolvedType receiver = doResolve(pe.getObjectExpression(), scope, depth + 1);
                if (receiver == null) {
                    // fully qualified class name: java.time.LocalDate
                    Class<?> c = index.load(pe.getText());
                    return c == null ? null : new ResolvedType(JType.of(c), true, null);
                }
                return resolveProperty(receiver, name, pe.isSpreadSafe());
            }
            default -> {
                return null;
            }
        }
    }

    private JType type(Expression e, Scope scope, int depth) {
        ResolvedType r = doResolve(e, scope, depth + 1);
        if (r == null) {
            return null;
        }
        return r.staticRef() ? JType.of(Class.class, r.type()) : r.type();
    }

    private static JType orObject(JType t) {
        return t == null ? JType.OBJECT : t;
    }

    private ResolvedType resolveBinary(BinaryExpression be, Scope scope, int depth) {
        int op = be.getOperation().getType();
        if (op == Types.LEFT_SQUARE_BRACKET) {
            JType left = type(be.getLeftExpression(), scope, depth);
            if (left == null) {
                return null;
            }
            if (left.raw().isArray()) {
                return ResolvedType.instance(JType.of(left.raw().getComponentType()));
            }
            JType list = left.asSuper(List.class);
            if (list != null) {
                return ResolvedType.instance(list.arg(0));
            }
            JType map = left.asSuper(Map.class);
            if (map != null) {
                return ResolvedType.instance(map.arg(1));
            }
            if (CharSequence.class.isAssignableFrom(left.raw())) {
                return ResolvedType.instance(JType.STRING);
            }
            return null;
        }
        if (Types.ofType(op, Types.COMPARISON_OPERATOR) || op == Types.LOGICAL_AND || op == Types.LOGICAL_OR
                || op == Types.KEYWORD_INSTANCEOF || op == Types.MATCH_REGEX) {
            return ResolvedType.instance(JType.BOOLEAN);
        }
        if (op == Types.ASSIGN) {
            return doResolve(be.getRightExpression(), scope, depth + 1);
        }
        if (op == Types.PLUS) {
            JType left = type(be.getLeftExpression(), scope, depth);
            if (left != null && (CharSequence.class.isAssignableFrom(left.raw()) || Collection.class.isAssignableFrom(left.raw()))) {
                return ResolvedType.instance(left);
            }
        }
        return null;
    }

    private static List<Expression> arguments(Expression args) {
        if (args instanceof TupleExpression t) {
            return t.getExpressions();
        }
        return args == null ? List.of() : List.of(args);
    }

    private ResolvedType resolveCall(ResolvedType receiver, String name, List<Expression> args, Scope scope, boolean spread, int depth) {
        JType target = spread ? receiver.type().elementType() : receiver.type();
        List<JType> argTypes = new ArrayList<>();
        for (Expression arg : args) {
            argTypes.add(type(arg, scope, depth));
        }
        JType result = callReturnType(target, receiver.staticRef() && !spread, name, argTypes);
        if (result == null) {
            return null;
        }
        return ResolvedType.instance(spread ? JType.of(List.class, result) : result);
    }

    /** Return type of {@code target.name(args)}, binding generics from the receiver and the arguments. */
    public JType callReturnType(JType target, boolean staticRef, String name, List<JType> argTypes) {
        Class<?> cls = target.memberClass();
        Method method = pickMethod(methods(cls, name, staticRef), argTypes, 0);
        if (method != null) {
            Map<TypeVariable<?>, JType> bindings = new HashMap<>();
            Type[] params = method.getGenericParameterTypes();
            for (int i = 0; i < params.length && i < argTypes.size(); i++) {
                JType.unify(params[i], argTypes.get(i), bindings);
            }
            return target.resolveMember(method.getGenericReturnType(), method.getDeclaringClass(), bindings);
        }
        if (staticRef) {
            return null;
        }
        Method ext = pickMethod(gdk.forReceiver(cls, name), argTypes, 1);
        if (ext != null) {
            Map<TypeVariable<?>, JType> bindings = new HashMap<>();
            Type[] params = ext.getGenericParameterTypes();
            JType.unify(params[0], target, bindings);
            for (int i = 1; i < params.length && i - 1 < argTypes.size(); i++) {
                JType.unify(params[i], argTypes.get(i - 1), bindings);
            }
            return JType.from(ext.getGenericReturnType(), bindings);
        }
        return null;
    }

    public static List<Method> methods(Class<?> cls, String name, boolean staticOnly) {
        List<Method> out = new ArrayList<>();
        for (Method m : allMethods(cls)) {
            if (m.getName().equals(name) && (!staticOnly || Modifier.isStatic(m.getModifiers()))) {
                out.add(m);
            }
        }
        return out;
    }

    /** Public methods without bridges; interfaces also get Object methods (Groovy calls them). */
    public static List<Method> allMethods(Class<?> cls) {
        List<Method> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<Method> source = new ArrayList<>(Arrays.asList(cls.getMethods()));
        if (cls.isInterface()) {
            source.addAll(Arrays.asList(Object.class.getMethods()));
        }
        // most specific declarations first, so covariant overrides win
        source.sort(Comparator.comparing((Method m) -> m.isBridge()));
        for (Method m : source) {
            if (m.isSynthetic() || m.isBridge()) {
                continue;
            }
            String key = m.getName() + Arrays.toString(m.getParameterTypes());
            if (seen.add(key)) {
                out.add(m);
            }
        }
        return out;
    }

    /** Picks the overload matching argument count (and types when known). {@code skip} leading params (GDK self). */
    private static Method pickMethod(List<Method> candidates, List<JType> argTypes, int skip) {
        Method byCount = null;
        for (Method m : candidates) {
            int count = m.getParameterCount() - skip;
            boolean countOk = count == argTypes.size() || (m.isVarArgs() && argTypes.size() >= count - 1);
            if (!countOk) {
                continue;
            }
            if (byCount == null) {
                byCount = m;
            }
            Class<?>[] params = m.getParameterTypes();
            boolean typesOk = true;
            for (int i = 0; i < argTypes.size() && i + skip < params.length; i++) {
                JType arg = argTypes.get(i);
                if (arg != null && !JType.box(params[i + skip]).isAssignableFrom(arg.memberClass())
                        && !(params[i + skip] == Class.class && arg.raw() == Class.class)) {
                    typesOk = false;
                    break;
                }
            }
            if (typesOk) {
                return m;
            }
        }
        return byCount;
    }

    public ResolvedType resolveProperty(ResolvedType receiver, String name, boolean spread) {
        if (spread) {
            JType element = receiver.type().elementType();
            ResolvedType r = resolveProperty(new ResolvedType(element, false, null), name, false);
            return r == null ? null : ResolvedType.instance(JType.of(List.class, r.type()));
        }
        JType type = receiver.type();
        Class<?> cls = type.memberClass();
        if (receiver.staticRef()) {
            if ("class".equals(name)) {
                return ResolvedType.instance(JType.of(Class.class, type));
            }
            try {
                Field f = cls.getField(name);
                if (Modifier.isStatic(f.getModifiers())) {
                    return ResolvedType.instance(JType.from(f.getGenericType(), Map.of()));
                }
            } catch (NoSuchFieldException ignored) {}
            for (Class<?> nested : cls.getClasses()) {
                if (nested.getSimpleName().equals(name)) {
                    return new ResolvedType(JType.of(nested), true, receiver.importFqn());
                }
            }
            return null;
        }
        if (cls.isArray() && "length".equals(name)) {
            return ResolvedType.instance(JType.INTEGER);
        }
        JType map = type.asSuper(Map.class);
        if (map != null && Map.class.isAssignableFrom(cls)) {
            return ResolvedType.instance(map.arg(1));
        }
        String cap = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (String getter : List.of("get" + cap, "is" + cap)) {
            for (Method m : methods(cls, getter, false)) {
                if (m.getParameterCount() == 0 && m.getReturnType() != void.class) {
                    return ResolvedType.instance(type.resolveMember(m.getGenericReturnType(), m.getDeclaringClass()));
                }
            }
        }
        try {
            Field f = cls.getField(name);
            return ResolvedType.instance(type.resolveMember(f.getGenericType(), f.getDeclaringClass()));
        } catch (NoSuchFieldException ignored) {}
        for (Method m : gdk.forReceiver(cls, "get" + cap)) {
            if (m.getParameterCount() == 1) {
                Map<TypeVariable<?>, JType> bindings = new HashMap<>();
                JType.unify(m.getGenericParameterTypes()[0], type, bindings);
                return ResolvedType.instance(JType.from(m.getGenericReturnType(), bindings));
            }
        }
        return null;
    }

    /**
     * Types of closure parameters when the closure is passed to {@code call} at {@code argIndex}:
     * {@code issues.each { it }} gives the element type, {@code stream.filter { it }} the functional interface parameter.
     */
    public List<JType> closureParameterTypes(MethodCallExpression call, int argIndex, int argCount, int paramCount, Scope scope) {
        String name = call.getMethodAsString();
        if (name == null || call.isImplicitThis()) {
            return List.of();
        }
        ResolvedType receiver = resolve(call.getObjectExpression(), scope);
        if (receiver == null) {
            return List.of();
        }
        JType type = receiver.staticRef() ? JType.of(Class.class, receiver.type()) : receiver.type();
        if (call.isSpreadSafe()) {
            type = type.elementType();
        }

        if (name.equals("with") || name.equals("tap")) {
            return List.of(type);
        }

        // Java functional interfaces: stream.filter { }, list.removeIf { }, map.computeIfAbsent(key) { }
        if (!receiver.staticRef()) {
            for (Method m : methods(type.memberClass(), name, false)) {
                if (m.getParameterCount() != argCount || argIndex >= argCount) {
                    continue;
                }
                JType param = type.resolveMember(m.getGenericParameterTypes()[argIndex], m.getDeclaringClass());
                List<JType> sam = samParameters(param);
                if (sam != null) {
                    return sam;
                }
            }
        }

        if (ITERATION_METHODS.contains(name)) {
            JType element = type.elementType();
            JType map = type.asSuper(Map.class);
            if (map != null && Map.class.isAssignableFrom(type.memberClass())) {
                if (name.equals("eachWithIndex") || name.equals("collectWithIndex")) {
                    return List.of(element, JType.INTEGER);
                }
                if (name.equals("inject")) {
                    return List.of(JType.OBJECT, element);
                }
                return paramCount >= 2 ? List.of(map.arg(0), map.arg(1)) : List.of(element);
            }
            if (name.equals("eachWithIndex") || name.equals("collectWithIndex")) {
                return List.of(element, JType.INTEGER);
            }
            if (name.equals("inject")) {
                return List.of(JType.OBJECT, element);
            }
            if ((name.equals("sort") || name.equals("toSorted") || name.equals("max") || name.equals("min") || name.equals("unique")) && paramCount == 2) {
                return List.of(element, element);
            }
            return List.of(element);
        }
        return List.of();
    }

    /** Parameter types of the single abstract method of a functional interface type, or null. */
    private static List<JType> samParameters(JType type) {
        Class<?> cls = type.raw();
        if (!cls.isInterface()) {
            return null;
        }
        Method sam = null;
        for (Method m : cls.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers()) && !isObjectMethod(m)) {
                if (sam != null) {
                    return null;
                }
                sam = m;
            }
        }
        if (sam == null) {
            return null;
        }
        List<JType> out = new ArrayList<>();
        for (Type p : sam.getGenericParameterTypes()) {
            out.add(type.resolveMember(p, sam.getDeclaringClass()));
        }
        return out;
    }

    private static boolean isObjectMethod(Method m) {
        try {
            Object.class.getMethod(m.getName(), m.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    // ----------------------------------------------------------------------------------------------- class nodes

    /** Unknown capitalized identifier: maybe a class the user has not imported yet. */
    private ResolvedType classByName(String name) {
        if (name.isEmpty() || !Character.isUpperCase(name.charAt(0))) {
            return null;
        }
        String explicit = imports.explicitImports().get(name);
        if (explicit != null) {
            Class<?> c = index.load(explicit);
            return c == null ? null : new ResolvedType(JType.of(c), true, null);
        }
        for (ClassEntry entry : index.bySimpleName(name)) {
            Class<?> c = index.load(entry.fqn());
            if (c != null) {
                return new ResolvedType(JType.of(c), true, imports.isVisible(entry.fqn()) ? null : entry.fqn());
            }
        }
        return null;
    }

    public JType fromClassNode(ClassNode node) {
        if (node == null) {
            return null;
        }
        Class<?> cls = classOf(node);
        if (cls == null) {
            return null;
        }
        GenericsType[] generics = node.getGenericsTypes();
        if (generics == null || generics.length == 0 || cls.getTypeParameters().length != generics.length) {
            return JType.of(cls);
        }
        List<JType> args = new ArrayList<>();
        for (GenericsType g : generics) {
            JType arg = null;
            if (g.isWildcard()) {
                if (g.getLowerBound() != null) {
                    arg = fromClassNode(g.getLowerBound());
                } else if (g.getUpperBounds() != null && g.getUpperBounds().length > 0) {
                    arg = fromClassNode(g.getUpperBounds()[0]);
                }
            } else if (!g.isPlaceholder()) {
                arg = fromClassNode(g.getType());
            }
            args.add(arg == null ? JType.OBJECT : arg);
        }
        return JType.of(cls, args);
    }

    public Class<?> classOf(ClassNode node) {
        if (node == null) {
            return null;
        }
        if (node.isArray()) {
            Class<?> component = classOf(node.getComponentType());
            return component == null ? null : Array.newInstance(component, 0).getClass();
        }
        try {
            Class<?> c = node.getTypeClass();
            if (c != null) {
                return c;
            }
        } catch (Throwable ignored) {}
        String name = node.getName();
        Class<?> c = index.load(name);
        if (c != null) {
            return c;
        }
        if (!name.contains(".")) {
            String explicit = imports.explicitImports().get(name);
            if (explicit != null) {
                return index.load(explicit);
            }
            for (ClassEntry entry : index.bySimpleName(name)) {
                c = index.load(entry.fqn());
                if (c != null) {
                    return c;
                }
            }
        }
        return null;
    }
}
