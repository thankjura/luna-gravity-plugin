package ru.slie.luna.plugins.gravity.model;

import ru.slie.luna.issue.workflow.action.WorkflowAction;

import java.util.List;

public class WorkflowTransition {
    private final List<Long> sourceStatuses;
    private final Long targetStatus;
    private final String transitionName;

    public WorkflowTransition(WorkflowAction action) {
        this.sourceStatuses = action.getSourceStatusIds();
        this.targetStatus = action.getTargetStatusId();
        this.transitionName = action.getName();
    }

    public List<Long> getSourceStatuses() {
        return sourceStatuses;
    }

    public Long getTargetStatus() {
        return targetStatus;
    }

    public String getTransitionName() {
        return transitionName;
    }
}
