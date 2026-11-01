package ru.slie.luna.plugins.gravity.dto;

import ru.slie.luna.plugins.gravity.model.WorkflowScript;

import java.util.Collection;
import java.util.List;

public record WorkflowScriptsDto(List<WorkflowScript> scripts, Collection<ProjectValueDto> projects, List<StatusValueDto> statuses) {}
