package ru.slie.luna.plugins.gravity.rest.request;

public class ScriptRequest {
    private String scriptContent;
    private Long timeout;

    public String getScriptContent() {
        return scriptContent;
    }

    public Long getTimeout() {
        return timeout;
    }
}
