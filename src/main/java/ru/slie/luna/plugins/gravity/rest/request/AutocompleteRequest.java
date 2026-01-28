package ru.slie.luna.plugins.gravity.rest.request;

public class AutocompleteRequest {
    private String script;
    private Integer position;
    private Integer limit;

    public String getScript() {
        return script;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getPosition() {
        return position;
    }
}
