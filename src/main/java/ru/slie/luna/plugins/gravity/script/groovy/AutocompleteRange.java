package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.ast.ASTNode;

public record AutocompleteRange(int startLineNumber, int startColumn, int endLineNumber, int endColumn) {
    public static AutocompleteRange start() {
        return new AutocompleteRange(1, 1, 1, 1);
    }
    public static AutocompleteRange forNode(ASTNode node) {
        return new AutocompleteRange(node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber());
    }
}
