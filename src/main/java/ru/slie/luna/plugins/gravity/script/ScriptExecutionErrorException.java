package ru.slie.luna.plugins.gravity.script;

public class ScriptExecutionErrorException extends Exception {
    private final Exception parent;

    public ScriptExecutionErrorException(Exception parent) {
        this.parent = parent;
    }

    public String getMessage() {
        return parent.getLocalizedMessage();
    }
}
