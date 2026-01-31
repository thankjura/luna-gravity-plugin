package ru.slie.luna.plugins.gravity.script.groovy;

import java.util.List;

public record AutocompleteResult(List<Suggestion> suggestions, AutocompleteRange range) {
    public static AutocompleteResult empty() {
        return new AutocompleteResult(List.of(), AutocompleteRange.start());
    }
}