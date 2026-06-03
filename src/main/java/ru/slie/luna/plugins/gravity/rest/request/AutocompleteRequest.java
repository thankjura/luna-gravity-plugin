package ru.slie.luna.plugins.gravity.rest.request;

import java.util.Map;

public class AutocompleteRequest {
    private String code;
    private Integer line;
    private Integer column;
    private Integer limit;
    private Map<String, String> context;

    public String getCode() {
        return code;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getLine() {
        return line;
    }

    public Integer getColumn() {
        return column;
    }

    public Map<String, String> getContext() {
        return context;
    }
}
