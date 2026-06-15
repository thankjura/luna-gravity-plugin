package ru.slie.luna.plugins.gravity.rest.response;

import ru.slie.luna.event.type.IssueEventType;
import ru.slie.luna.plugins.gravity.model.ListenerScript;
import ru.slie.luna.project.Project;
import ru.slie.luna.project.ProjectInfo;

import java.util.List;
import java.util.stream.Collectors;

public class ListenerScriptsResponse {
    private final List<ListenerScript> scripts;
    private final List<IssueEventType> eventTypes;
    private final List<ProjectInfo> projects;

    public ListenerScriptsResponse(List<ListenerScript> scripts, List<IssueEventType> eventTypes, List<Project> projects) {
        this.scripts = scripts;
        this.eventTypes = eventTypes;
        this.projects = projects.stream().map(ProjectInfoImpl::new).collect(Collectors.toList());
    }

    public List<ListenerScript> getScripts() {
        return scripts;
    }

    public List<IssueEventType> getEventTypes() {
        return eventTypes;
    }

    public List<ProjectInfo> getProjects() {
        return projects;
    }
}
