package ru.slie.luna.plugins.gravity.script;

import java.time.LocalDateTime;

public class ScriptRunEvent {
    private final String scriptId;
    private final LocalDateTime startTime;
    private final Long cpuTimeMs;
    private final Long executionTimeMs;
    private final String exception;
    private final String payload;
    private final String log;

    public ScriptRunEvent(String scriptId, LocalDateTime startTime, Long cpuTimeMs, Long executionTimeMs, String exception, String payload, String log) {
        this.scriptId = scriptId;
        this.startTime = startTime;
        this.cpuTimeMs = cpuTimeMs;
        this.executionTimeMs = executionTimeMs;
        this.exception = exception;
        this.payload = payload;
        this.log = log;
    }

    public String getScriptId() {
        return scriptId;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Long getCpuTimeMs() {
        return cpuTimeMs;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public String getException() {
        return exception;
    }

    public String getPayload() {
        return payload;
    }

    public String getLog() {
        return log;
    }
}
