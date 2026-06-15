package ru.slie.luna.plugins.gravity.rest.response;

import ru.slie.luna.plugins.gravity.model.ListenerScript;
import ru.slie.luna.project.Project;

import java.util.List;

public class ListenerScriptWithProjects extends ListenerScript {
    private final List<Project> projects;

    public ListenerScriptWithProjects(ListenerScript script, List<Project> projects) {
        super(script);
        this.projects = projects;
    }

    public List<Project> getProjects() {
        return projects;
    }
}
