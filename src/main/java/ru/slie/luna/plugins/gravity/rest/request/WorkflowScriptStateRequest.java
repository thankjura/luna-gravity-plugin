package ru.slie.luna.plugins.gravity.rest.request;

public class WorkflowScriptStateRequest {
    private Long workflowId;
    private Integer actionId;
    private String functionType;
    private String functionId;
    private Boolean disabled;

    public Long getWorkflowId() {
        return workflowId;
    }

    public Integer getActionId() {
        return actionId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public boolean getDisabled() {
        if (disabled != null) {
            return disabled;
        }

        return false;
    }

    public String getFunctionType() {
        return functionType;
    }
}
