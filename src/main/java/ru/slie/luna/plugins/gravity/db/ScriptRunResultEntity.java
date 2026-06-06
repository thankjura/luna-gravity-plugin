package ru.slie.luna.plugins.gravity.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import ru.slie.luna.regolith.ActiveDocEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "script_run_results", indexes = {
        @Index(name = "idx_script_run_results_script_id_start", columnList = "script_id, start_at desc")
})
public class ScriptRunResultEntity extends ActiveDocEntity {
    private String scriptId;
    private LocalDateTime startAt;
    private Long executionTimeMs;
    private Long cpuTimeMs;

    @Column(columnDefinition = "TEXT")
    private String exception;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String logs;

    public ScriptRunResultEntity() {}

    public ScriptRunResultEntity(String scriptId, LocalDateTime startAt, long executionTimeMs, Long cpuTimeMs, String exception, String payload, String logs) {
        this.scriptId = scriptId;
        this.startAt = startAt;
        this.executionTimeMs = executionTimeMs;
        this.cpuTimeMs = cpuTimeMs;
        this.exception = exception;
        this.payload = payload;
        this.logs = logs;
    }

    public String getScriptId() {
        return scriptId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Long getCpuTimeMs() {
        return cpuTimeMs;
    }

    public String getException() {
        return exception;
    }

    public String getPayload() {
        return payload;
    }

    public String getLogs() {
        return logs;
    }
}
