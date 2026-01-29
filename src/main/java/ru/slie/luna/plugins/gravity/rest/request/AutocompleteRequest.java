package ru.slie.luna.plugins.gravity.rest.request;

public class AutocompleteRequest {
    private String code;
    private Integer line;
    private Integer column;
    private Integer limit;

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
}
