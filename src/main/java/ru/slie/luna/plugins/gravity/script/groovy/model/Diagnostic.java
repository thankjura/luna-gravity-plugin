package ru.slie.luna.plugins.gravity.script.groovy.model;

import java.util.ArrayList;
import java.util.List;

/** Monaco marker with optional quick fixes. */
public class Diagnostic {
    public static final int SEVERITY_WARNING = 4;
    public static final int SEVERITY_ERROR = 8;

    private final AutocompleteRange range;
    private final String message;
    private final int severity;
    private final List<QuickFix> fixes = new ArrayList<>();

    public Diagnostic(AutocompleteRange range, String message, int severity) {
        this.range = range;
        this.message = message;
        this.severity = severity;
    }

    public AutocompleteRange getRange() {
        return range;
    }

    public String getMessage() {
        return message;
    }

    public int getSeverity() {
        return severity;
    }

    public List<QuickFix> getFixes() {
        return fixes;
    }

    public void addFix(QuickFix fix) {
        fixes.add(fix);
    }

    public record QuickFix(String title, List<TextEdit> edits, boolean preferred) {}
}
