package ru.slie.luna.plugins.gravity.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.slie.luna.exception.DeleteException;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.status.Status;
import ru.slie.luna.issue.status.StatusManager;
import ru.slie.luna.issue.workflow.Workflow;
import ru.slie.luna.issue.workflow.WorkflowManager;
import ru.slie.luna.issue.workflow.WorkflowSchema;
import ru.slie.luna.issue.workflow.WorkflowSchemaEntry;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.action.WorkflowActionConditionGroup;
import ru.slie.luna.issue.workflow.action.WorkflowActionFunction;
import ru.slie.luna.issue.workflow.action.WorkflowActionValidator;
import ru.slie.luna.permission.GlobalPermissionManager;
import ru.slie.luna.plugins.gravity.model.WorkflowFunctionType;
import ru.slie.luna.plugins.gravity.model.WorkflowScript;
import ru.slie.luna.plugins.gravity.rest.request.WorkflowScriptStateRequest;
import ru.slie.luna.plugins.gravity.rest.response.ProjectInfoImpl;
import ru.slie.luna.plugins.gravity.rest.response.WorkflowScriptsResponse;
import ru.slie.luna.plugins.gravity.script.ScriptManager;
import ru.slie.luna.plugins.gravity.workflow.AbstractGravityFunction;
import ru.slie.luna.project.Project;
import ru.slie.luna.project.ProjectInfo;
import ru.slie.luna.project.ProjectManager;
import ru.slie.luna.security.AuthenticationContext;
import ru.slie.luna.user.User;

import java.util.*;

@RestController
@RequestMapping("/gravity/workflow")
public class WorkflowRest {
    private final ScriptManager scriptManager;
    private final ProjectManager projectManager;
    private final WorkflowManager workflowManager;
    private final StatusManager statusManager;
    private final AuthenticationContext authenticationContext;
    private final GlobalPermissionManager globalPermissionManager;

    public WorkflowRest(ScriptManager scriptManager,
                        ProjectManager projectManager,
                        WorkflowManager workflowManager,
                        StatusManager statusManager,
                        AuthenticationContext authenticationContext,
                        GlobalPermissionManager globalPermissionManager) {
        this.scriptManager = scriptManager;
        this.projectManager = projectManager;
        this.workflowManager = workflowManager;
        this.statusManager = statusManager;
        this.authenticationContext = authenticationContext;
        this.globalPermissionManager = globalPermissionManager;
    }

    @GetMapping("/scripts")
    public WorkflowScriptsResponse getWorkflowScripts() {
        List<WorkflowScript> scripts = scriptManager.getWorkflowScripts();
        Map<String, Project> projectsMap = new HashMap<>();
        Map<Long, Set<String>> workflowProjectKeysMap = new HashMap<>();
        Map<Long, Set<String>> schemaProjectMap = new HashMap<>();
        for (Project project : projectManager.getAll()) {
            if (project.getWorkflowSchemaId() == null) {
                continue;
            }
            projectsMap.put(project.getKey(), project);
            schemaProjectMap.computeIfAbsent(project.getWorkflowSchemaId(), id -> new HashSet<>()).add(project.getKey());
        }

        for (Map.Entry<Long, Set<String>> entry : schemaProjectMap.entrySet()) {
            Optional<WorkflowSchema> schema = workflowManager.getSchemaById(entry.getKey());
            if (schema.isEmpty()) {
                continue;
            }

            workflowProjectKeysMap.computeIfAbsent(schema.get().getDefaultWorkflowId(), id -> new HashSet<>()).addAll(entry.getValue());

            for (WorkflowSchemaEntry schemaEntry: schema.get().getEntries()) {
                workflowProjectKeysMap.computeIfAbsent(schemaEntry.getWorkflow().getId(), id -> new HashSet<>()).addAll(entry.getValue());
            }
        }

        Map<String, ProjectInfo> affectedProject = new HashMap<>();
        Set<Long> statuses = new HashSet<>();

        for (WorkflowScript workflowScript: scripts) {
            statuses.addAll(workflowScript.getTransition().getSourceStatuses());
            statuses.add(workflowScript.getTransition().getTargetStatus());
            if (workflowProjectKeysMap.containsKey(workflowScript.getWorkflowId())) {
                for (String projectKey : workflowProjectKeysMap.get(workflowScript.getWorkflowId())) {
                    if (!affectedProject.containsKey(projectKey)) {
                        affectedProject.put(projectKey, new ProjectInfoImpl(projectsMap.get(projectKey)));
                    }
                }
                workflowScript.addProjectKeys(workflowProjectKeysMap.get(workflowScript.getWorkflowId()));
            }
        }

        Map<Long, Status> statusesMap = new HashMap<>();
        for (Status status: statusManager.getByIds(statuses)) {
            statusesMap.put(status.getId(), status);
        }


        return new WorkflowScriptsResponse(scripts, affectedProject, statusesMap);
    }

    @PostMapping("/scripts")
    public WorkflowScript updateScriptState(@RequestBody WorkflowScriptStateRequest request) throws ValidateException, DeleteException {
        User user = authenticationContext.getCurrentUser();
        if (!globalPermissionManager.hasAdminPerms(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        WorkflowFunctionType functionType = WorkflowFunctionType.fromString(request.getFunctionType());


        if (functionType == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Optional<Workflow> workflow = workflowManager.getById(request.getWorkflowId());
        if (workflow.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        WorkflowAction action = workflow.get().getActionById(request.getActionId());
        if (action == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        WorkflowActionFunction actionFunction = getActionFunction(action, functionType, request.getFunctionId());
        if (actionFunction == null || !actionFunction.getClassName().startsWith("ru.slie.luna.plugins.gravity.workflow.")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (workflow.get().getOriginalId() != null) {
            updateWorkflowFunctionState(workflow.get(), functionType, action, actionFunction, request.getDisabled());
        } else {
            if (workflowManager.getDraft(workflow.get()).isEmpty()) {
                Workflow draft = workflowManager.getOrCreateDraft(user, workflow.get());
                updateWorkflowFunctionState(draft, functionType, action, actionFunction, request.getDisabled());
                try {
                    workflowManager.applyDraft(user, workflow.get(), Map.of());
                } catch (Exception ignored) {
                    workflowManager.delete(draft);
                }
            }
        }

        return new WorkflowScript(workflow.get(), action, actionFunction, functionType);
    }

    private WorkflowActionFunction getConditionParams(WorkflowActionConditionGroup group, String functionId) {
        if (group == null) {
            return null;
        }

        if (functionId.equals(group.getId())) {
            if (group.getCondition() != null) {
                return group.getCondition();
            }
            return null;
        }

        for (WorkflowActionConditionGroup child: group.getItems()) {
            WorkflowActionFunction out = getConditionParams(child, functionId);
            if (out != null) {
                return out;
            }
        }

        return null;
    }

    private WorkflowActionFunction getActionFunction(WorkflowAction action, WorkflowFunctionType functionType, String functionId) {
        return switch (functionType) {
            case CONDITION -> getConditionParams(action.getConditions(), functionId);
            case VALIDATOR -> {
                for (WorkflowActionValidator validator: action.getValidators()) {
                    if (validator.getId().equals(functionId)) {
                        yield validator;
                    }
                }
                yield null;
            }
            case POSTFUNCTION -> {
                for (WorkflowActionFunction function: action.getPostfunctions()) {
                    if (function.getId().equals(functionId)) {
                        yield function;
                    }
                }

                yield null;
            }
        };
    }

    private void updateWorkflowFunctionState(Workflow workflow, WorkflowFunctionType functionType, WorkflowAction action, WorkflowActionFunction function, boolean disabled) throws ValidateException {
        Map<String, String> params = function.getParams();
        if (params == null) {
            params = new HashMap<>();
        }
        AbstractGravityFunction.setDisabled(params, disabled);

        switch (functionType) {
            case CONDITION -> workflowManager.updateCondition(workflow, action.getId(), function.getId(), params);
            case VALIDATOR -> workflowManager.updateValidator(workflow, action.getId(), function.getId(), params);
            case POSTFUNCTION -> workflowManager.updatePostfunction(workflow, action.getId(), function.getId(), params);
        }
    }
}
