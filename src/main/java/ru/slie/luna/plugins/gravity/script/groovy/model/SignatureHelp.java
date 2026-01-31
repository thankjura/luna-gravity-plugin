package ru.slie.luna.plugins.gravity.script.groovy.model;

import java.util.ArrayList;
import java.util.List;

public class SignatureHelp {
    private final List<SignatureInfo> signatures;
    private int activeSignature;
    private int activeParameter;

    public SignatureHelp() {
        this.signatures = new ArrayList<>();
        this.activeSignature = 0;
        this.activeParameter = 1;
    }

    public int getActiveSignature() {
        return activeSignature;
    }

    public void setActiveSignature(int activeSignature) {
        this.activeSignature = activeSignature;
    }

    public int getActiveParameter() {
        return activeParameter;
    }

    public void setActiveParameter(int activeParameter) {
        this.activeParameter = activeParameter;
    }

    public List<SignatureInfo> getSignatures() {
        return signatures;
    }

    public void addSignature(SignatureInfo signatureInfo) {
        this.signatures.add(signatureInfo);
    }

    public static class SignatureInfo {
        private String label;
        private String documentation;
        private Integer activeParameter;
        private List<ParameterInfo> parameters;

        public SignatureInfo(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDocumentation() {
            return documentation;
        }

        public void setDocumentation(String documentation) {
            this.documentation = documentation;
        }

        public Integer getActiveParameter() {
            return activeParameter;
        }

        public void setActiveParameter(Integer activeParameter) {
            this.activeParameter = activeParameter;
        }

        public List<ParameterInfo> getParameters() {
            return parameters;
        }

        public void addParameter(String parameter) {
            if (this.parameters == null) {
                this.parameters = new ArrayList<>();
            }

            this.parameters.add(ParameterInfo.label(parameter));
        }
    }


    public record ParameterInfo(String label, String documentation) {
        public static ParameterInfo label(String label) {
            return new ParameterInfo(label, null);
        }
    }
}

