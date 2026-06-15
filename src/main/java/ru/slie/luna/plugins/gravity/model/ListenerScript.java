package ru.slie.luna.plugins.gravity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.slie.luna.plugins.gravity.db.ScriptListenerEntity;
import ru.slie.luna.plugins.gravity.rest.request.ListenerScriptRequest;
import ru.slie.luna.plugins.gravity.utils.Constants;
import ru.slie.luna.plugins.gravity.utils.SetUtils;
import ru.slie.luna.utils.WithId;
import ru.slie.luna.utils.WithName;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ListenerScript implements WithId, WithName {
    private Long id;
    private String name;
    private String description;
    private Set<Long> projectIds;
    private Set<Long> eventTypeIds;
    private String script;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Boolean enabled;

    public ListenerScript(ScriptListenerEntity entity) {
        update(entity);
    }

    public ListenerScript(ListenerScriptRequest request) {
        this.name = request.getName();
        this.description = request.getDescription();
        setProjectIds(request.getProjectIds());
        setEventTypeIds(request.getEventTypesIds());
        this.script = request.getScript();
        this.enabled = request.getEnabled();
    }

    public ListenerScript(ListenerScript script) {
        this.id = script.getId();
        this.name = script.getName();
        this.description = script.getDescription();
        this.projectIds = script.getProjectIds();
        this.eventTypeIds = script.getEventTypeIds();
        this.script = script.getScript();
        this.created = script.getCreated();
        this.updated = script.getUpdated();
        this.enabled = script.isEnabled();
    }

    public void update(ScriptListenerEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.description = entity.getDescription();
        this.projectIds = SetUtils.parseSet(entity.getProjectIds());
        this.eventTypeIds = SetUtils.parseSet(entity.getEventTypeIds());
        this.script = entity.getScript();
        this.created = entity.getCreated();
        this.updated = entity.getUpdated();
        this.enabled = entity.isEnabled();
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

    public Set<Long> getProjectIds() {
        return projectIds;
    }

    public Set<Long> getEventTypeIds() {
        return eventTypeIds;
    }

    public String getScript() {
        return script;
    }

    @JsonFormat(pattern = Constants.DATE_TIME_SEC_FORMAT)
    public LocalDateTime getCreated() {
        return created;
    }

    @JsonFormat(pattern = Constants.DATE_TIME_SEC_FORMAT)
    public LocalDateTime getUpdated() {
        return updated;
    }

    public boolean isEnabled() {
        if (enabled != null) {
            return enabled;
        }

        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProjectIds(Set<Long> projectIds) {
        this.projectIds = Objects.requireNonNullElseGet(projectIds, HashSet::new);
    }

    public void setEventTypeIds(Set<Long> eventTypeIds) {
        this.eventTypeIds = Objects.requireNonNullElseGet(eventTypeIds, HashSet::new);
    }

    public void setScript(String script) {
        this.script = script;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
