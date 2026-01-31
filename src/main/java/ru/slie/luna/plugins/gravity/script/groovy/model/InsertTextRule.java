package ru.slie.luna.plugins.gravity.script.groovy.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum InsertTextRule {
    None(0),
    KeepWhitespace(1),
    InsertAsSnippet(4);

    private final int value;

    InsertTextRule(int value) {
        this.value = value;
    }

    public static InsertTextRule ofValue(Integer value) {
        if (value != null) {
            for (InsertTextRule rule: InsertTextRule.values()) {
                if (value.equals(rule.value)) {
                    return rule;
                }
            }
        }

        return null;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}