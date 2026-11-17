package ru.slie.luna.plugins.gravity.script.groovy.model;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteResult {
    private final List<Suggestion> suggestions = new ArrayList<>();
    private boolean incomplete;

    public void add(Suggestion suggestion) {
        suggestions.add(suggestion);
    }

    public void addAll(List<Suggestion> list) {
        suggestions.addAll(list);
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public boolean getIncomplete() {
        return incomplete;
    }

    public void setIncomplete(boolean incomplete) {
        this.incomplete = incomplete;
    }
}
