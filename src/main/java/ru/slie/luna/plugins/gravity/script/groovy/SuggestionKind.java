package ru.slie.luna.plugins.gravity.script.groovy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SuggestionKind {
    Method(0),
    Function(1),
    Constructor(2),
    Field(3),
    Variable(4),
    Class(5),
    Struct(6),
    Interface(7),
    Module(8),
    Property(9),
    Event(10),
    Operator(11),
    Unit(12),
    Value(13),
    Constant(14),
    Enum(15),
    EnumMember(16),
    Keyword(17),
    Text(18),
    Color(19),
    File(20),
    Reference(21),
    Customcolor(22),
    Folder(23),
    TypeParameter(24),
    User(25),
    Issue(26),
    Tool(27),
    Snippet(28);

    private final int kind;

    SuggestionKind(int kind) {
        this.kind = kind;
    }

    @JsonValue
    public int getKind() {
        return kind;
    }

    @JsonCreator
    public static SuggestionKind getOf(Integer kind) {
        if (kind == null) {
            return null;
        }

        for (SuggestionKind k: SuggestionKind.values()) {
            if (kind.equals(k.getKind())) {
                return k;
            }
        }

        return null;
    }
}
