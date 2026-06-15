package ru.slie.luna.plugins.gravity.rest.response;

import ru.slie.luna.issue.status.Status;
import ru.slie.luna.plugins.gravity.model.WorkflowScript;
import ru.slie.luna.project.ProjectInfo;

import java.util.Collection;
import java.util.List;

public record WorkflowScriptsResponse(List<WorkflowScript> scripts, Collection<ProjectInfo> projects, List<Status> statuses) {}
