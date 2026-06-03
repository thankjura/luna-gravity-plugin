package ru.slie.luna.plugins.gravity.script;

import org.springframework.stereotype.Component;
import ru.slie.luna.issue.workflow.Workflow;
import ru.slie.luna.issue.workflow.WorkflowManager;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.action.WorkflowActionConditionGroup;
import ru.slie.luna.issue.workflow.action.WorkflowActionFunction;
import ru.slie.luna.plugins.gravity.model.WorkflowFunctionType;
import ru.slie.luna.plugins.gravity.model.WorkflowScript;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class ScriptManager {
    private final WorkflowManager workflowManager;

    public ScriptManager(WorkflowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    private void extractFunction(Workflow workflow, WorkflowAction action, WorkflowActionFunction workflowFunction, WorkflowFunctionType functionType, List<WorkflowScript> scripts) {
        if (!functionType.getClassName().equals(workflowFunction.getClassName())) {
            return;
        }

        scripts.add(new WorkflowScript(workflow, action, workflowFunction, functionType));
    }

    private void extractScriptFromCondition(Workflow workflow, WorkflowAction action, WorkflowActionConditionGroup conditionGroup, List<WorkflowScript> scripts) {
        if (conditionGroup == null) {
            return;
        }

        if (conditionGroup.getCondition() != null) {
            extractFunction(workflow, action, conditionGroup.getCondition(), WorkflowFunctionType.CONDITION, scripts);
        }

        for (WorkflowActionConditionGroup group : conditionGroup.getItems()) {
            extractScriptFromCondition(workflow, action, group, scripts);
        }
    }

    // TODO: cache manager
    public List<WorkflowScript> getWorkflowScripts() {
        List<WorkflowScript> workflowScripts = new ArrayList<>();

        try (Stream<Workflow> workflowStream = workflowManager.getAll()) {
            workflowStream.forEach(workflow -> {
                for (WorkflowAction action : workflow.getActions()) {
                    if (action.getConditions() != null) {
                        extractScriptFromCondition(workflow, action, action.getConditions(), workflowScripts);
                    }

                    for (WorkflowActionFunction validator: action.getValidators()) {
                        extractFunction(workflow, action, validator, WorkflowFunctionType.VALIDATOR, workflowScripts);
                    }

                    for (WorkflowActionFunction postfunction: action.getPostfunctions()) {
                        extractFunction(workflow, action, postfunction, WorkflowFunctionType.POSTFUNCTION, workflowScripts);
                    }
                }
            });
        }

        return workflowScripts;
    }
}
