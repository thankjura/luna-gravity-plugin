package ru.slie.luna.plugins.gravity.script.groovy;

import java.util.List;

public record AutocompleteResult(int from, List<Suggestion> suggestions) {}
