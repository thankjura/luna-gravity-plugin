package ru.slie.luna.plugins.gravity.script.groovy.model;

/** Monaco IRange, 1-based. */
public record AutocompleteRange(int startLineNumber, int startColumn, int endLineNumber, int endColumn) {}
