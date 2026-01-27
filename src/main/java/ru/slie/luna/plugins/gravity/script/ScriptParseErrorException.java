package ru.slie.luna.plugins.gravity.script;

import org.codehaus.groovy.control.CompilationFailedException;

public class ScriptParseErrorException extends Exception {
    private final CompilationFailedException parent;

    public ScriptParseErrorException(CompilationFailedException parent) {
        this.parent = parent;
    }

    public String getMessage() {
        return parent.getLocalizedMessage();
    }
}
