package ru.slie.luna.plugins.gravity.dto;

import ru.slie.luna.plugins.gravity.model.ListenerScript;
import ru.slie.luna.project.Project;

import java.util.List;

public class ListenerScriptWithProjectsDto extends ListenerScript {
    private final List<Project> projects;

    public ListenerScriptWithProjectsDto(ListenerScript script, List<Project> projects) {
        super(script);
        this.projects = projects;
    }

    public List<Project> getProjects() {
        return projects;
    }
}
