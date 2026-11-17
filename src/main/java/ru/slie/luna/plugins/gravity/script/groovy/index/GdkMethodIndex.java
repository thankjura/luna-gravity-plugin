package ru.slie.luna.plugins.gravity.script.groovy.index;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Groovy extension methods (GDK): {@code each}, {@code collect}, {@code find}... available on JDK types.
 * Each method is static and its first parameter is the receiver ("self").
 */
@Component
public class GdkMethodIndex {
    private static final List<Class<?>> SOURCES = List.of(
            DefaultGroovyMethods.class, StringGroovyMethods.class, IOGroovyMethods.class, ResourceGroovyMethods.class
    );
    /** Methods that are technically available on everything but only clutter completion. */
    private static final Set<String> HIDDEN = Set.of(
            "getAt", "putAt", "invokeMethod", "getMetaClass", "setMetaClass", "getMetaPropertyValues", "getProperties",
            "hasProperty", "respondsTo", "mixin", "use", "plus", "minus", "multiply", "div", "mod", "power", "leftShift",
            "rightShift", "rightShiftUnsigned", "xor", "or", "and", "next", "previous", "bitwiseNegate", "negative",
            "positive", "isCase", "asBoolean", "asType", "addShutdownHook", "sleep", "print", "println", "printf",
            "sprintf", "dump", "inspect", "identity", "getProperty", "setProperty", "is", "asImmutable",
            "asSynchronized", "asUnmodifiable", "asChecked", "metaClass", "notifyAll"
    );

    private final List<Method> methods;

    public GdkMethodIndex() {
        Map<String, Method> unique = new LinkedHashMap<>();
        for (Class<?> source : SOURCES) {
            for (Method m : source.getMethods()) {
                int mod = m.getModifiers();
                if (!Modifier.isStatic(mod) || !Modifier.isPublic(mod) || m.getParameterCount() == 0
                        || m.isAnnotationPresent(Deprecated.class) || m.isSynthetic() || HIDDEN.contains(m.getName())) {
                    continue;
                }
                Class<?> self = m.getParameterTypes()[0];
                if (self.isPrimitive() || (self.isArray() && self.getComponentType().isPrimitive())) {
                    continue;
                }
                String key = self.getName() + "#" + m.getName() + Arrays.toString(m.getParameterTypes());
                unique.putIfAbsent(key, m);
            }
        }
        this.methods = List.copyOf(unique.values());
    }

    /** GDK methods applicable to the receiver class. Overloads for more specific receivers come first. */
    public List<Method> forReceiver(Class<?> receiver) {
        List<Method> out = new ArrayList<>();
        for (Method m : methods) {
            if (m.getParameterTypes()[0].isAssignableFrom(receiver)) {
                out.add(m);
            }
        }
        out.sort(Comparator.comparingInt(m -> distance(receiver, m.getParameterTypes()[0])));
        return out;
    }

    public List<Method> forReceiver(Class<?> receiver, String name) {
        return forReceiver(receiver).stream().filter(m -> m.getName().equals(name)).toList();
    }

    /** How far {@code target} is from {@code cls} in its hierarchy (Object is the farthest). */
    public static int distance(Class<?> cls, Class<?> target) {
        if (target == Object.class) {
            return 100;
        }
        if (cls == target) {
            return 0;
        }
        if (target.isInterface()) {
            return 10;
        }
        int d = 0;
        for (Class<?> c = cls; c != null; c = c.getSuperclass(), d++) {
            if (c == target) {
                return d;
            }
        }
        return 50;
    }
}
