package ru.slie.luna.plugins.gravity.rest.response;

import ru.slie.luna.project.Project;
import ru.slie.luna.project.ProjectInfo;

public class ProjectInfoImpl implements ProjectInfo {
    private final Project project;

    public ProjectInfoImpl(Project project) {
        this.project = project;
    }

    @Override
    public String getKey() {
        return project.getKey();
    }

    @Override
    public String getDescription() {
        return project.getDescription();
    }

    @Override
    public String getIconPath() {
        return project.getIconPath();
    }

    @Override
    public Long getId() {
        return project.getId();
    }

    @Override
    public String getName() {
        return project.getName();
    }
}
