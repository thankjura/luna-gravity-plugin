package ru.slie.luna.plugins.gravity.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import ru.slie.luna.regolith.ActiveDocEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "script_listeners")
public class ScriptListenerEntity extends ActiveDocEntity {
    private String name;
    private String description;
    private String projectIds;
    private String eventTypeIds;
    @Column(columnDefinition = "TEXT")
    private String script;
    private LocalDateTime created;
    private LocalDateTime updated;
    private Boolean async;
    private Boolean enabled;

    public ScriptListenerEntity() {}
    public ScriptListenerEntity(String name, String description, String projectIds, String eventTypeIds, String script, Boolean async) {
        this.name = name;
        this.description = description;
        this.projectIds = projectIds;
        this.eventTypeIds = eventTypeIds;
        this.script = script;
        this.created = LocalDateTime.now();
        this.updated = created;
        this.async = async;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(String projectIds) {
        this.projectIds = projectIds;
    }

    public String getEventTypeIds() {
        return eventTypeIds;
    }

    public void setEventTypeIds(String eventIds) {
        this.eventTypeIds = eventIds;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean disabled) {
        this.enabled = disabled;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public Boolean isAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public void setUpdated(LocalDateTime localDateTime) {
        this.updated = localDateTime;
    }
}
