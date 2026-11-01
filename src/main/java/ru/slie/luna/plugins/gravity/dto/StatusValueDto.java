package ru.slie.luna.plugins.gravity.dto;

import ru.slie.luna.issue.status.Status;

public class StatusValueDto {
    private final Long id;
    private final String name;
    private final String statusCategoryKey;

    public StatusValueDto(Status status) {
        this.id = status.getId();
        this.name = status.getName();
        this.statusCategoryKey = status.getCategoryKey();
    }

    public static StatusValueDto from(Status status) {
        if (status == null) {
            return null;
        }

        return new StatusValueDto(status);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatusCategoryKey() {
        return statusCategoryKey;
    }
}
