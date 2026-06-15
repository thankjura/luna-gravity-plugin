package ru.slie.luna.plugins.gravity.rest.request;

import java.util.Set;

public class ListenerScriptRequest {
    private String name;
    private String description;
    private Set<Long> projectIds;
    private Set<Long> eventTypesIds;
    private String script;
    private Boolean enabled;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Long> getProjectIds() {
        return projectIds;
    }

    public Set<Long> getEventTypesIds() {
        return eventTypesIds;
    }

    public String getScript() {
        return script;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}
