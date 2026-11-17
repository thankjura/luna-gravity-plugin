package ru.slie.luna.plugins.gravity.script.groovy.analysis;

import java.lang.reflect.*;
import java.util.*;

/**
 * Lightweight generic-aware type: a raw class plus resolved type arguments (e.g. {@code List<Issue>}).
 */
public final class JType {
    public static final JType OBJECT = new JType(Object.class, List.of());
    public static final JType STRING = new JType(String.class, List.of());
    public static final JType BOOLEAN = new JType(Boolean.class, List.of());
    public static final JType INTEGER = new JType(Integer.class, List.of());
    public static final JType CLOSURE = new JType(groovy.lang.Closure.class, List.of());

    private final Class<?> raw;
    private final List<JType> args;

    private JType(Class<?> raw, List<JType> args) {
        this.raw = raw;
        this.args = args;
    }

    public static JType of(Class<?> raw) {
        return new JType(raw, List.of());
    }

    public static JType of(Class<?> raw, List<JType> args) {
        return new JType(raw, args == null ? List.of() : List.copyOf(args));
    }

    public static JType of(Class<?> raw, JType... args) {
        return of(raw, Arrays.asList(args));
    }

    public Class<?> raw() {
        return raw;
    }

    /** Class used for member lookup: primitives are boxed (Groovy autoboxes them). */
    public Class<?> memberClass() {
        return box(raw);
    }

    public List<JType> args() {
        return args;
    }

    public JType arg(int i) {
        return i < args.size() ? args.get(i) : JType.OBJECT;
    }

    public Map<TypeVariable<?>, JType> bindings() {
        TypeVariable<?>[] vars = raw.getTypeParameters();
        if (vars.length == 0 || args.isEmpty()) {
            return Map.of();
        }
        Map<TypeVariable<?>, JType> out = new HashMap<>();
        for (int i = 0; i < vars.length && i < args.size(); i++) {
            out.put(vars[i], args.get(i));
        }
        return out;
    }

    /** Finds parameterization of {@code target} among supertypes, e.g. {@code ArrayList<Issue>.asSuper(Iterable) -> Iterable<Issue>}. */
    public JType asSuper(Class<?> target) {
        return asSuper(target, 0);
    }

    private JType asSuper(Class<?> target, int depth) {
        if (raw == target) {
            return this;
        }
        if (depth > 20 || !target.isAssignableFrom(memberClass())) {
            return null;
        }
        Class<?> cls = memberClass();
        Map<TypeVariable<?>, JType> bindings = cls == raw ? bindings() : Map.of();
        List<Type> supers = new ArrayList<>();
        if (cls.getGenericSuperclass() != null) {
            supers.add(cls.getGenericSuperclass());
        }
        supers.addAll(Arrays.asList(cls.getGenericInterfaces()));
        for (Type sup : supers) {
            JType st = from(sup, bindings);
            if (st != null && target.isAssignableFrom(st.raw)) {
                JType found = st.asSuper(target, depth + 1);
                if (found != null) {
                    return found;
                }
            }
        }
        return of(target);
    }

    /** Resolves a type declared in {@code declaringClass} (a member's return/param type) in the context of this type. */
    public JType resolveMember(Type type, Class<?> declaringClass) {
        return resolveMember(type, declaringClass, Map.of());
    }

    public JType resolveMember(Type type, Class<?> declaringClass, Map<TypeVariable<?>, JType> extra) {
        Map<TypeVariable<?>, JType> bindings = new HashMap<>(extra);
        JType owner = asSuper(declaringClass);
        if (owner != null) {
            bindings.putAll(owner.bindings());
        }
        return from(type, bindings);
    }

    /** Element type when iterating with Groovy (each/collect/for-in...). */
    public JType elementType() {
        if (raw.isArray()) {
            return of(raw.getComponentType());
        }
        if (Map.class.isAssignableFrom(raw)) {
            JType map = asSuper(Map.class);
            return of(Map.Entry.class, map.arg(0), map.arg(1));
        }
        JType it = asSuper(Iterable.class);
        if (it != null) {
            return it.arg(0);
        }
        JType iterator = asSuper(Iterator.class);
        if (iterator != null) {
            return iterator.arg(0);
        }
        if (CharSequence.class.isAssignableFrom(raw)) {
            return STRING;
        }
        return OBJECT;
    }

    public static JType from(Type type, Map<TypeVariable<?>, JType> bindings) {
        return from(type, bindings, 0);
    }

    private static JType from(Type type, Map<TypeVariable<?>, JType> bindings, int depth) {
        if (type == null || depth > 10) {
            return OBJECT;
        }
        if (type instanceof Class<?> c) {
            return of(c);
        }
        if (type instanceof ParameterizedType pt) {
            List<JType> args = new ArrayList<>();
            for (Type a : pt.getActualTypeArguments()) {
                args.add(from(a, bindings, depth + 1));
            }
            return of((Class<?>) pt.getRawType(), args);
        }
        if (type instanceof TypeVariable<?> tv) {
            JType bound = bindings.get(tv);
            if (bound != null) {
                return bound;
            }
            Type[] bounds = tv.getBounds();
            return of(erasure(bounds.length > 0 ? bounds[0] : Object.class));
        }
        if (type instanceof WildcardType wt) {
            if (wt.getLowerBounds().length > 0) {
                return from(wt.getLowerBounds()[0], bindings, depth + 1);
            }
            return from(wt.getUpperBounds().length > 0 ? wt.getUpperBounds()[0] : Object.class, bindings, depth + 1);
        }
        if (type instanceof GenericArrayType gat) {
            JType component = from(gat.getGenericComponentType(), bindings, depth + 1);
            return of(Array.newInstance(component.raw, 0).getClass());
        }
        return OBJECT;
    }

    public static Class<?> erasure(Type type) {
        if (type instanceof Class<?> c) {
            return c;
        }
        if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        }
        if (type instanceof TypeVariable<?> tv) {
            return tv.getBounds().length > 0 ? erasure(tv.getBounds()[0]) : Object.class;
        }
        if (type instanceof WildcardType wt) {
            return wt.getUpperBounds().length > 0 ? erasure(wt.getUpperBounds()[0]) : Object.class;
        }
        if (type instanceof GenericArrayType gat) {
            return Array.newInstance(erasure(gat.getGenericComponentType()), 0).getClass();
        }
        return Object.class;
    }

    /**
     * Binds method type variables by matching a formal (generic) type against an actual type.
     * E.g. formal {@code Class<T>} and actual {@code Class<IssueManager>} binds {@code T -> IssueManager}.
     */
    public static void unify(Type formal, JType actual, Map<TypeVariable<?>, JType> out) {
        unify(formal, actual, out, 0);
    }

    private static void unify(Type formal, JType actual, Map<TypeVariable<?>, JType> out, int depth) {
        if (actual == null || formal == null || depth > 10) {
            return;
        }
        if (formal instanceof TypeVariable<?> tv) {
            out.putIfAbsent(tv, actual.raw.isPrimitive() ? of(box(actual.raw)) : actual);
        } else if (formal instanceof ParameterizedType pt) {
            JType sup = actual.asSuper((Class<?>) pt.getRawType());
            if (sup == null || sup.args.isEmpty()) {
                return;
            }
            Type[] fa = pt.getActualTypeArguments();
            for (int i = 0; i < fa.length && i < sup.args.size(); i++) {
                unify(fa[i], sup.args.get(i), out, depth + 1);
            }
        } else if (formal instanceof WildcardType wt) {
            Type bound = wt.getLowerBounds().length > 0 ? wt.getLowerBounds()[0] : wt.getUpperBounds()[0];
            unify(bound, actual, out, depth + 1);
        } else if (formal instanceof GenericArrayType gat && actual.raw.isArray()) {
            unify(gat.getGenericComponentType(), of(actual.raw.getComponentType()), out, depth + 1);
        }
    }

    public static Class<?> box(Class<?> c) {
        if (!c.isPrimitive()) {
            return c;
        }
        if (c == int.class) return Integer.class;
        if (c == long.class) return Long.class;
        if (c == boolean.class) return Boolean.class;
        if (c == double.class) return Double.class;
        if (c == float.class) return Float.class;
        if (c == short.class) return Short.class;
        if (c == byte.class) return Byte.class;
        if (c == char.class) return Character.class;
        return Void.class;
    }

    /** Short human-readable form: {@code List<Issue>}. */
    public String display() {
        StringBuilder sb = new StringBuilder(simpleName(raw));
        if (!args.isEmpty()) {
            StringJoiner joiner = new StringJoiner(", ", "<", ">");
            args.forEach(a -> joiner.add(a.display()));
            sb.append(joiner);
        }
        return sb.toString();
    }

    public static String simpleName(Class<?> c) {
        if (c.isArray()) {
            return simpleName(c.getComponentType()) + "[]";
        }
        Class<?> enclosing = c.getEnclosingClass();
        if (enclosing != null && !c.isAnonymousClass()) {
            return simpleName(enclosing) + "." + c.getSimpleName();
        }
        return c.getSimpleName();
    }

    /** Display form of a generic type as declared (keeps type variable names, e.g. {@code List<T>}). */
    public static String displayGeneric(Type type, Map<TypeVariable<?>, JType> bindings) {
        if (type instanceof Class<?> c) {
            return simpleName(c);
        }
        if (type instanceof TypeVariable<?> tv) {
            JType bound = bindings.get(tv);
            return bound != null ? bound.display() : tv.getName();
        }
        if (type instanceof ParameterizedType pt) {
            StringJoiner joiner = new StringJoiner(", ", "<", ">");
            for (Type a : pt.getActualTypeArguments()) {
                joiner.add(displayGeneric(a, bindings));
            }
            return simpleName((Class<?>) pt.getRawType()) + joiner;
        }
        if (type instanceof WildcardType wt) {
            if (wt.getLowerBounds().length > 0) {
                return "? super " + displayGeneric(wt.getLowerBounds()[0], bindings);
            }
            Type upper = wt.getUpperBounds().length > 0 ? wt.getUpperBounds()[0] : Object.class;
            return upper == Object.class ? "?" : "? extends " + displayGeneric(upper, bindings);
        }
        if (type instanceof GenericArrayType gat) {
            return displayGeneric(gat.getGenericComponentType(), bindings) + "[]";
        }
        return type == null ? "" : type.getTypeName();
    }

    @Override
    public String toString() {
        return display();
    }
}
