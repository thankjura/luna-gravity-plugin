package ru.slie.luna.plugins.gravity.script.groovy.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/** Monaco CompletionItem (range is computed on the client). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Suggestion {
    private final String label;
    private final String labelDetail;
    private final String labelDescription;
    private final SuggestionKind kind;
    private final InsertTextRule insertTextRules;
    private final String detail;
    private final String insertText;
    private final String doc;
    private final String sortText;
    private final String filterText;
    private final Boolean triggerSignature;
    private final List<TextEdit> additionalTextEdits;

    private Suggestion(Builder builder) {
        this.label = builder.label;
        this.labelDetail = builder.labelDetail;
        this.labelDescription = builder.labelDescription;
        this.kind = builder.kind;
        this.detail = builder.detail;
        this.insertText = builder.insertText;
        this.doc = builder.doc;
        this.sortText = builder.sortText;
        this.filterText = builder.filterText;
        this.triggerSignature = builder.triggerSignature ? Boolean.TRUE : null;
        this.additionalTextEdits = builder.additionalTextEdits;
        this.insertTextRules = builder.insertTextRules;
    }

    public static Builder builder(String insertText, SuggestionKind kind) {
        return new Builder(insertText, kind);
    }

    public String getLabel() {
        return label;
    }

    public String getLabelDetail() {
        return labelDetail;
    }

    public String getLabelDescription() {
        return labelDescription;
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

    public String getSortText() {
        return sortText;
    }

    public String getFilterText() {
        return filterText;
    }

    public Boolean getTriggerSignature() {
        return triggerSignature;
    }

    public List<TextEdit> getAdditionalTextEdits() {
        return additionalTextEdits;
    }

    public InsertTextRule getInsertTextRules() {
        return insertTextRules;
    }

    public static class Builder {
        private String label;
        private String labelDetail;
        private String labelDescription;
        private SuggestionKind kind;
        private InsertTextRule insertTextRules;
        private String detail;
        private String insertText;
        private String doc;
        private String sortText;
        private String filterText;
        private boolean triggerSignature;
        private List<TextEdit> additionalTextEdits;

        private Builder(String insertText, SuggestionKind kind) {
            this.label = insertText;
            this.insertText = insertText;
            this.kind = kind;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        /** Shown right after the label, e.g. method parameters. */
        public Builder labelDetail(String labelDetail) {
            this.labelDetail = labelDetail;
            return this;
        }

        /** Shown on the right side, e.g. return type or package. */
        public Builder labelDescription(String labelDescription) {
            this.labelDescription = labelDescription;
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

        public Builder snippet(String snippet) {
            this.insertText = snippet;
            this.insertTextRules = InsertTextRule.InsertAsSnippet;
            return this;
        }

        public Builder doc(String doc) {
            this.doc = doc;
            return this;
        }

        public Builder sortText(String sortText) {
            this.sortText = sortText;
            return this;
        }

        public Builder filterText(String filterText) {
            this.filterText = filterText;
            return this;
        }

        public Builder triggerSignature(boolean triggerSignature) {
            this.triggerSignature = triggerSignature;
            return this;
        }

        public Builder insertTextRule(InsertTextRule insertTextRules) {
            this.insertTextRules = insertTextRules;
            return this;
        }

        public Builder addEdit(TextEdit edit) {
            if (edit == null) {
                return this;
            }
            if (additionalTextEdits == null) {
                additionalTextEdits = new ArrayList<>();
            }
            additionalTextEdits.add(edit);
            return this;
        }

        public Suggestion build() {
            return new Suggestion(this);
        }
    }
}
