package ru.slie.luna.plugins.gravity.dto;

import ru.slie.luna.project.Project;

public class ProjectValueDto {
    private final Long id;
    private final String name;
    private final String description;
    private final String iconUrl;

    public ProjectValueDto(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.iconUrl = project.getIconUrl();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
