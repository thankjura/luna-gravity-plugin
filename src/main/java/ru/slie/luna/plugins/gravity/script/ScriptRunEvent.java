package ru.slie.luna.plugins.gravity.script;

import java.time.LocalDateTime;

public class ScriptRunEvent {
    private final String functionId;
    private final LocalDateTime startTime;
    private final Long cpuTimeMs;
    private final Long executionTimeMs;
    private final String exception;
    private final String payload;

    public ScriptRunEvent(String functionId, LocalDateTime startTime, Long cpuTimeMs, Long executionTimeMs, String exception, String payload) {
        this.functionId = functionId;
        this.startTime = startTime;
        this.cpuTimeMs = cpuTimeMs;
        this.executionTimeMs = executionTimeMs;
        this.exception = exception;
        this.payload = payload;
    }

    public String getFunctionId() {
        return functionId;
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
}
