package ru.slie.luna.plugins.gravity.script.groovy;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroovyAutocompleteService {
    private final GenericApplicationContext context;
    public GroovyAutocompleteService(GenericApplicationContext context) {
        this.context = context;
    }

    public AutocompleteResult getSuggestions(String fullText, int position, int limit) {
        List<Suggestion> suggestions = new ArrayList<>();

        return new AutocompleteResult(position, suggestions);
    }
}
