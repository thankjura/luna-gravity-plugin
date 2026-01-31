package ru.slie.luna.plugins.gravity.script.groovy;

import ru.slie.luna.plugins.gravity.script.groovy.completion.SuggestionsProviderResult;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteResult {
    private final List<Suggestion> suggestions;
    private AutocompleteRange range;
    private boolean incomplete;

    public AutocompleteResult() {
        this.suggestions = new ArrayList<>();
        this.range = AutocompleteRange.start();
        this.incomplete = false;
    }

    public void addResult(SuggestionsProviderResult result) {
        this.suggestions.addAll(result.getSuggestions());
        this.incomplete = this.incomplete || result.isIncomplete();
    }

    public void setRange(AutocompleteRange range) {
        this.range = range;
    }

    public AutocompleteRange getRange() {
        return range;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public boolean getIncomplete() {
        return incomplete;
    }
}