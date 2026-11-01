package ru.slie.luna.plugins.gravity.dto;

import ru.slie.luna.event.type.IssueEventType;
import ru.slie.luna.plugins.gravity.model.ListenerScript;
import ru.slie.luna.project.Project;

import java.util.List;
import java.util.stream.Collectors;

public class ListenerScriptsDto {
    private final List<ListenerScript> scripts;
    private final List<IssueEventType> eventTypes;
    private final List<ProjectValueDto> projects;

    public ListenerScriptsDto(List<ListenerScript> scripts, List<IssueEventType> eventTypes, List<Project> projects) {
        this.scripts = scripts;
        this.eventTypes = eventTypes;
        this.projects = projects.stream().map(ProjectValueDto::new).collect(Collectors.toList());
    }

    public List<ListenerScript> getScripts() {
        return scripts;
    }

    public List<IssueEventType> getEventTypes() {
        return eventTypes;
    }

    public List<ProjectValueDto> getProjects() {
        return projects;
    }
}
