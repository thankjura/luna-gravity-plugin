package ru.slie.luna.plugins.gravity.script.groovy;

import java.util.ArrayList;
import java.util.List;

public class Suggestion {
    private final String label;
    private final SuggestionKind kind;
    private final String detail;
    private final String insertText;
    private final String doc;
    private final List<AdditionalTextEdit> additionalTextEdits;

    private Suggestion(Builder builder) {
        this.label = builder.label;
        this.kind = builder.kind;
        this.detail = builder.detail;
        this.insertText = builder.insertText;
        this.doc = builder.doc;
        this.additionalTextEdits = builder.additionalTextEdits;
    }

    public String getLabel() {
        return label;
    }

    public SuggestionKind getKind() {
        return kind;
    }

    public String getDetail() {
        return detail;
    }

    public String getInsertText() {
        return insertText;
    }

    public String getDoc() {
        return doc;
    }

    public static Builder builder(String insertText, SuggestionKind kind) {
        return new Builder(insertText, kind);
    }

    public List<AdditionalTextEdit> getAdditionalTextEdits() {
        return additionalTextEdits;
    }

    public record AdditionalTextEdit(AutocompleteRange range, String text) {}

    public static class Builder {
        private String label;
        private SuggestionKind kind;
        private String detail;
        private String insertText;
        private String doc;
        private List<AdditionalTextEdit> additionalTextEdits;

        private Builder(String insertText, SuggestionKind kind) {
            this.label = insertText;
            this.insertText = insertText;
            this.kind = kind;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder kind(SuggestionKind kind) {
            this.kind = kind;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder insertText(String insertText) {
            this.insertText = insertText;
            return this;
        }

        public Builder doc(String doc) {
            this.doc = doc;
            return this;
        }

        public Builder addAutoImport(String importClass) {
            if (additionalTextEdits == null) {
                additionalTextEdits = new ArrayList<>();
            }

            additionalTextEdits.add(new AdditionalTextEdit(AutocompleteRange.start(), String.format("import %s;\n", importClass)));
            return this;
        }

        public Suggestion build() {
            return new Suggestion(this);
        }
    }
}
