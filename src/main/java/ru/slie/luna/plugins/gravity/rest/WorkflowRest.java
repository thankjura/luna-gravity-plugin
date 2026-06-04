package ru.slie.luna.plugins.gravity.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.slie.luna.issue.status.Status;
import ru.slie.luna.issue.status.StatusManager;
import ru.slie.luna.issue.workflow.WorkflowManager;
import ru.slie.luna.issue.workflow.WorkflowSchema;
import ru.slie.luna.issue.workflow.WorkflowSchemaEntry;
import ru.slie.luna.plugins.gravity.model.WorkflowScript;
import ru.slie.luna.plugins.gravity.rest.response.ProjectInfoImpl;
import ru.slie.luna.plugins.gravity.rest.response.WorkflowScriptsResponse;
import ru.slie.luna.plugins.gravity.script.ScriptManager;
import ru.slie.luna.project.Project;
import ru.slie.luna.project.ProjectInfo;
import ru.slie.luna.project.ProjectManager;

import java.util.*;

@RestController
@RequestMapping("/gravity/workflow")
public class WorkflowRest {
    private final ScriptManager scriptManager;
    private final ProjectManager projectManager;
    private final WorkflowManager workflowManager;
    private final StatusManager statusManager;

    public WorkflowRest(ScriptManager scriptManager,
                        ProjectManager projectManager,
                        WorkflowManager workflowManager,
                        StatusManager statusManager) {
        this.scriptManager = scriptManager;
        this.projectManager = projectManager;
        this.workflowManager = workflowManager;
        this.statusManager = statusManager;
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
}
