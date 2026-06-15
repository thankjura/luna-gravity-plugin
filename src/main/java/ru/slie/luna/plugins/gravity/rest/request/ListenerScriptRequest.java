package ru.slie.luna.plugins.gravity.rest.request;

import java.util.Set;

public class ListenerScriptRequest {
    private String name;
    private String description;
    private Set<Long> projectIds;
    private Set<Long> eventTypeIds;
    private String script;
    private Boolean async;
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

    public Set<Long> getEventTypeIds() {
        return eventTypeIds;
    }

    public String getScript() {
        return script;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getAsync() {
        return async;
    }
}
