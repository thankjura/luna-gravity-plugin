package ru.slie.luna.plugins.gravity.metrics;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.slie.luna.plugins.gravity.db.ScriptRunResultEntity;
import ru.slie.luna.plugins.gravity.utils.Constants;

import java.time.LocalDateTime;

public class ScriptRunResult {
    private final String scriptId;
    private final LocalDateTime startAt;
    private final Long executionTimeMs;
    private final Long cpuTimeMs;
    private final String exception;
    private final String payload;
    private final String logs;

    public ScriptRunResult(ScriptRunResultEntity entity) {
        this.scriptId = entity.getScriptId();
        this.startAt = entity.getStartAt();
        this.executionTimeMs = entity.getExecutionTimeMs();
        this.cpuTimeMs = entity.getCpuTimeMs();
        this.exception = entity.getException();
        this.payload = entity.getPayload();
        this.logs = entity.getLogs();
    }

    public String getScriptId() {
        return scriptId;
    }

    @JsonFormat(pattern = Constants.DATE_TIME_FORMAT)
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
