package ru.slie.luna.plugins.gravity.rest.response;

import ru.slie.luna.plugins.gravity.model.WorkflowScript;
import ru.slie.luna.project.ProjectInfo;

import java.util.List;
import java.util.Map;

public record WorkflowScriptsResponse(List<WorkflowScript> scripts, Map<String, ProjectInfo> projects) {}
