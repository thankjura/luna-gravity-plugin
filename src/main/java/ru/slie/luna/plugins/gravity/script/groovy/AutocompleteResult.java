package ru.slie.luna.plugins.gravity.script.groovy;

import java.util.List;

public record AutocompleteResult(List<Suggestion> suggestions, Range range) {
    public static AutocompleteResult empty() {
        return new AutocompleteResult(List.of(), Range.empty());
    }

    public record Range(int startLineNumber, int startColumn, int endLineNumber, int endColumn) {
        public static Range empty() {
            return new Range(0, 0, 0, 0);
        }

    }
}