package ru.slie.luna.plugins.gravity.model;

import ru.slie.luna.issue.workflow.Workflow;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.action.WorkflowActionFunction;
import ru.slie.luna.plugins.gravity.workflow.AbstractGravityFunction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class WorkflowScript {
    private final Workflow workflow;
    private final WorkflowAction action;
    private final WorkflowActionFunction function;
    private final WorkflowFunctionType functionType;
    private final Set<String> projectKeys;
    private final WorkflowTransition transition;

    public WorkflowScript(Workflow workflow,
                          WorkflowAction action,
                          WorkflowActionFunction function,
                          WorkflowFunctionType functionType) {
        this.workflow = workflow;
        this.action = action;
        this.function = function;
        this.functionType = functionType;
        this.projectKeys = new HashSet<>();
        this.transition = new WorkflowTransition(action);
    }

    public String getId() {
        return function.getId();
    }

    public boolean isDraft() {
        return workflow.getOriginalId() != null;
    }

    public Long getWorkflowOriginalId() {
        return workflow.getOriginalId();
    }

    public Long getWorkflowId() {
        return workflow.getId();
    }

    public String getWorkflowName() {
        return workflow.getName();
    }

    public Integer getActionId() {
        return action.getId();
    }

    public String getActionName() {
        return action.getName();
    }

    public String getScript() {
        return function.getParams().get("script");
    }

    public WorkflowFunctionType getFunctionType() {
        return functionType;
    }

    public String getScriptNote() {
        return function.getParams().get(AbstractGravityFunction.SCRIPT_NOTE);
    }

    public Set<String> getProjectKeys() {
        return projectKeys;
    }

    public void addProjectKeys(Collection<String> projectKeys) {
        this.projectKeys.addAll(projectKeys);
    }

    public WorkflowTransition getTransition() {
        return transition;
    }

    public boolean isDisabled() {
        return AbstractGravityFunction.isDisabled(function.getParams());
    }
}
