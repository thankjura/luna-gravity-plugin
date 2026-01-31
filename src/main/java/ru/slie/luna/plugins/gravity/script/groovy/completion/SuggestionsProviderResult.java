package ru.slie.luna.plugins.gravity.script.groovy.completion;

import ru.slie.luna.plugins.gravity.script.groovy.Suggestion;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsProviderResult {
    private boolean incomplete;
    private final List<Suggestion> suggestions;

    public SuggestionsProviderResult() {
        this.suggestions = new ArrayList<>();
        this.incomplete = false;
    }

    public void setIncomplete(boolean incomplete) {
        this.incomplete = incomplete;
    }

    public void addSuggestion(Suggestion suggestion) {
        suggestions.add(suggestion);
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }
}
