package ru.slie.luna.plugins.gravity.model;

import ru.slie.luna.issue.workflow.Workflow;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.action.WorkflowActionFunction;

public class WorkflowScript {
    private final Workflow workflow;
    private final WorkflowAction action;
    private final WorkflowActionFunction function;
    private final WorkflowFunctionType functionType;

    public WorkflowScript(Workflow workflow,
                          WorkflowAction action,
                          WorkflowActionFunction function,
                          WorkflowFunctionType functionType) {
        this.workflow = workflow;
        this.action = action;
        this.function = function;
        this.functionType = functionType;
    }

    public String getWorkflowId() {
        return workflow.getId();
    }

    public String getWorkflowName() {
        return workflow.getName();
    }

    public Integer getActionId() {
        return action.getId();
    }

    public String getScript() {
        return function.getParams().get("script");
    }

    public WorkflowFunctionType getFunctionType() {
        return functionType;
    }
}
