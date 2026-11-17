package ru.slie.luna.plugins.gravity.script.groovy.completion;

import groovy.lang.Closure;
import ru.slie.luna.plugins.gravity.script.groovy.analysis.JType;
import ru.slie.luna.plugins.gravity.script.groovy.analysis.TypeResolver;
import ru.slie.luna.plugins.gravity.script.groovy.analysis.TypeResolver.ResolvedType;
import ru.slie.luna.plugins.gravity.script.groovy.index.GdkMethodIndex;
import ru.slie.luna.plugins.gravity.script.groovy.index.NameMatcher;
import ru.slie.luna.plugins.gravity.script.groovy.model.Suggestion;
import ru.slie.luna.plugins.gravity.script.groovy.model.SuggestionKind;
import ru.slie.luna.plugins.gravity.script.groovy.model.TextEdit;

import java.lang.reflect.*;
import java.util.*;

/**
 * Members (methods, properties, fields, GDK methods) of a resolved type.
 */
public final class MemberSuggestions {
    private static final Set<String> HIDDEN_OBJECT_METHODS = Set.of("wait", "notify", "notifyAll");
    private static final Set<String> HIDDEN_PROPERTIES = Set.of("class", "metaClass");

    private static final int RANK_OWN = 0;
    private static final int RANK_INHERITED = 1;
    private static final int RANK_GDK = 2;
    private static final int RANK_GDK_OBJECT = 3;
    private static final int RANK_OBJECT = 4;

    private record Item(Suggestion.Builder builder, String name, int rank, int quality) {}

    private final GdkMethodIndex gdk;

    public MemberSuggestions(GdkMethodIndex gdk) {
        this.gdk = gdk;
    }

    public List<Suggestion> suggest(ResolvedType receiver, String prefix, TextEdit importEdit, int limit) {
        JType type = receiver.type();
        Class<?> cls = type.memberClass();
        boolean isStatic = receiver.staticRef();
        List<Item> items = new ArrayList<>();
        Set<String> javaSignatures = new HashSet<>();

        for (Method m : TypeResolver.allMethods(cls)) {
            boolean mStatic = Modifier.isStatic(m.getModifiers());
            if (isStatic != mStatic || HIDDEN_OBJECT_METHODS.contains(m.getName())) {
                continue;
            }
            int quality = NameMatcher.match(m.getName(), prefix);
            if (quality < 0) {
                continue;
            }
            javaSignatures.add(m.getName() + "/" + m.getParameterCount());
            int rank = m.getDeclaringClass() == cls ? RANK_OWN : (m.getDeclaringClass() == Object.class ? RANK_OBJECT : RANK_INHERITED);
            items.add(new Item(method(type, m, 0, null), m.getName(), rank, quality));
        }

        for (Field f : cls.getFields()) {
            boolean fStatic = Modifier.isStatic(f.getModifiers());
            int quality = NameMatcher.match(f.getName(), prefix);
            if (isStatic != fStatic || quality < 0) {
                continue;
            }
            SuggestionKind kind = f.isEnumConstant() ? SuggestionKind.EnumMember
                    : (fStatic && Modifier.isFinal(f.getModifiers()) ? SuggestionKind.Constant : SuggestionKind.Field);
            JType fieldType = type.resolveMember(f.getGenericType(), f.getDeclaringClass());
            items.add(new Item(Suggestion.builder(f.getName(), kind)
                                       .labelDescription(fieldType.display())
                                       .detail(fieldType.display() + " " + JType.simpleName(f.getDeclaringClass()) + "." + f.getName()),
                               f.getName(), f.getDeclaringClass() == cls ? RANK_OWN : RANK_INHERITED, quality));
        }

        if (isStatic) {
            for (Class<?> nested : cls.getClasses()) {
                int quality = NameMatcher.match(nested.getSimpleName(), prefix);
                if (quality >= 0 && nested.getDeclaringClass() == cls && Modifier.isStatic(nested.getModifiers())) {
                    SuggestionKind kind = nested.isEnum() ? SuggestionKind.Enum : (nested.isInterface() ? SuggestionKind.Interface : SuggestionKind.Class);
                    items.add(new Item(Suggestion.builder(nested.getSimpleName(), kind).detail(nested.getName().replace('$', '.')),
                                       nested.getSimpleName(), RANK_INHERITED, quality));
                }
            }
        } else {
            items.addAll(properties(type, cls, prefix));
            Set<String> gdkSeen = new HashSet<>();
            for (Method m : gdk.forReceiver(cls)) {
                String key = m.getName() + Arrays.toString(Arrays.copyOfRange(m.getParameterTypes(), 1, m.getParameterCount()));
                if (javaSignatures.contains(m.getName() + "/" + (m.getParameterCount() - 1)) || !gdkSeen.add(key)) {
                    continue;
                }
                int quality = NameMatcher.match(m.getName(), prefix);
                if (quality < 0) {
                    continue;
                }
                int rank = m.getParameterTypes()[0] == Object.class ? RANK_GDK_OBJECT : RANK_GDK;
                items.add(new Item(method(type, m, 1, m.getDeclaringClass()), m.getName(), rank, quality));
            }
        }

        items.sort(Comparator.comparingInt((Item i) -> i.quality() > 0 ? 1 : 0)
                .thenComparingInt(Item::rank)
                .thenComparing(Item::name));

        List<Suggestion> out = new ArrayList<>();
        int n = 0;
        for (Item item : items) {
            if (n++ >= limit) {
                break;
            }
            out.add(item.builder()
                        .sortText(String.format("%d_%04d_%s", item.rank(), n, item.name()))
                        .addEdit(importEdit)
                        .build());
        }
        return out;
    }

    private List<Item> properties(JType type, Class<?> cls, String prefix) {
        List<Item> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Method m : TypeResolver.allMethods(cls)) {
            if (Modifier.isStatic(m.getModifiers()) || m.getParameterCount() != 0 || m.getReturnType() == void.class) {
                continue;
            }
            String name = m.getName();
            String property;
            if (name.startsWith("get") && name.length() > 3) {
                property = decapitalize(name.substring(3));
            } else if (name.startsWith("is") && name.length() > 2 && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                property = decapitalize(name.substring(2));
            } else {
                continue;
            }
            int quality = NameMatcher.match(property, prefix);
            if (HIDDEN_PROPERTIES.contains(property) || quality < 0 || !seen.add(property)) {
                continue;
            }
            JType propertyType = type.resolveMember(m.getGenericReturnType(), m.getDeclaringClass());
            int rank = m.getDeclaringClass() == cls ? RANK_OWN : (m.getDeclaringClass() == Object.class ? RANK_OBJECT : RANK_INHERITED);
            out.add(new Item(Suggestion.builder(property, SuggestionKind.Property)
                                     .labelDescription(propertyType.display())
                                     .detail(propertyType.display() + " " + property + " → " + name + "()"),
                             property, rank, quality));
        }
        return out;
    }

    /**
     * @param skip       leading parameters to hide (1 for GDK "self")
     * @param gdkSource  class providing a GDK method, null for regular methods
     */
    static Suggestion.Builder method(JType owner, Method m, int skip, Class<?> gdkSource) {
        Map<TypeVariable<?>, JType> bindings = new HashMap<>();
        Type[] generic = m.getGenericParameterTypes();
        JType returnType;
        if (skip > 0) {
            JType.unify(generic[0], owner, bindings);
            returnType = JType.from(m.getGenericReturnType(), bindings);
        } else {
            JType declaring = owner.asSuper(m.getDeclaringClass());
            if (declaring != null) {
                bindings.putAll(declaring.bindings());
            }
            returnType = owner.resolveMember(m.getGenericReturnType(), m.getDeclaringClass());
        }

        List<String> names = parameterNames(m);
        List<String> labels = new ArrayList<>();
        for (int i = skip; i < generic.length; i++) {
            labels.add(JType.displayGeneric(generic[i], bindings) + " " + names.get(i));
        }
        String params = String.join(", ", labels);
        String returns = m.getReturnType() == void.class ? "void" : returnType.display();

        Suggestion.Builder b = Suggestion.builder(m.getName(), SuggestionKind.Method)
                .labelDetail("(" + params + ")")
                .labelDescription(returns)
                .detail(returns + " " + JType.simpleName(gdkSource != null ? m.getParameterTypes()[0] : m.getDeclaringClass())
                                + "." + m.getName() + "(" + params + ")" + (gdkSource != null ? "  [GDK]" : ""));
        b.snippet(callSnippet(m.getName(), m.getParameterTypes(), names, skip));
        b.triggerSignature(m.getParameterCount() > skip && !(m.getParameterCount() - skip == 1 && isClosure(m.getParameterTypes()[skip])));
        return b;
    }

    static String callSnippet(String name, Class<?>[] types, List<String> names, int skip) {
        int count = types.length - skip;
        if (count == 0) {
            return name + "()";
        }
        boolean trailingClosure = isClosure(types[types.length - 1]);
        StringJoiner args = new StringJoiner(", ");
        int last = trailingClosure ? types.length - 1 : types.length;
        int n = 1;
        for (int i = skip; i < last; i++) {
            args.add("${" + n++ + ":" + names.get(i) + "}");
        }
        if (trailingClosure) {
            String head = n == 1 ? name : name + "(" + args + ")";
            return head + " { $0 }";
        }
        return name + "(" + args + ")";
    }

    private static boolean isClosure(Class<?> type) {
        return Closure.class.isAssignableFrom(type);
    }

    public static List<String> parameterNames(Executable m) {
        List<String> out = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (Parameter p : m.getParameters()) {
            String name = p.isNamePresent() ? p.getName() : nameFor(p.getType());
            String unique = name;
            for (int i = 2; !used.add(unique); i++) {
                unique = name + i;
            }
            out.add(unique);
        }
        return out;
    }

    /** JavaBeans rules: {@code Summary -> summary}, {@code URL -> URL}. */
    static String decapitalize(String name) {
        if (name.isEmpty() || (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0)))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static String nameFor(Class<?> type) {
        if (type.isPrimitive()) {
            return switch (type.getName()) {
                case "int" -> "i";
                case "long" -> "l";
                case "boolean" -> "b";
                case "char" -> "c";
                default -> type.getName().substring(0, 1);
            };
        }
        if (Closure.class.isAssignableFrom(type)) {
            return "closure";
        }
        String simple = type.isArray() ? type.getComponentType().getSimpleName() + "s" : type.getSimpleName();
        String name = decapitalize(simple);
        return switch (name) {
            case "object" -> "obj";
            case "class" -> "cls";
            case "string" -> "str";
            default -> name;
        };
    }
}
