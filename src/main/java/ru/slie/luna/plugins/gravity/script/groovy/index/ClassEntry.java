package ru.slie.luna.plugins.gravity.script.groovy.index;

public record ClassEntry(String fqn, String simpleName, String packageName, ClassSource source, Kind kind, boolean bean) {
    public enum Kind { CLASS, INTERFACE, ENUM, ANNOTATION, RECORD }

    public ClassEntry withBean(boolean value) {
        return new ClassEntry(fqn, simpleName, packageName, source, kind, value);
    }
}
