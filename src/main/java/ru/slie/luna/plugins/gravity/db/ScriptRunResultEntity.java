package ru.slie.luna.plugins.gravity.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import ru.slie.luna.regolith.ActiveDocEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "script_run_results", indexes = {
        @Index(name = "idx_script_run_results_script_id_start", columnList = "function_id, start_at")
})
public class ScriptRunResultEntity extends ActiveDocEntity {
    private String functionId;
    private LocalDateTime startAt;
    private Long executionTimeMs;
    private Long cpuTimeMs;

    @Column(columnDefinition = "TEXT")
    private String exception;

    @Column(columnDefinition = "TEXT")
    private String payload;

    public ScriptRunResultEntity() {}

    public ScriptRunResultEntity(String functionId, LocalDateTime startAt, long executionTimeMs, Long cpuTimeMs, String exception, String payload) {
        this.functionId = functionId;
        this.startAt = startAt;
        this.executionTimeMs = executionTimeMs;
        this.cpuTimeMs = cpuTimeMs;
        this.exception = exception;
        this.payload = payload;
    }

    public String getFunctionId() {
        return functionId;
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
}
