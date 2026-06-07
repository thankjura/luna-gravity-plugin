package ru.slie.luna.plugins.gravity.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import ru.slie.luna.plugins.gravity.workflow.GravityScriptCondition;
import ru.slie.luna.plugins.gravity.workflow.GravityScriptPostfunction;
import ru.slie.luna.plugins.gravity.workflow.GravityScriptValidator;

public enum WorkflowFunctionType {
    CONDITION("condition", GravityScriptCondition.class),
    VALIDATOR("validator", GravityScriptValidator.class),
    POSTFUNCTION("postfunction", GravityScriptPostfunction.class);

    private final String key;
    private final String className;

    WorkflowFunctionType(String key, Class<?> clazz) {
        this.key = key;
        this.className = clazz.getCanonicalName();
    }

    public String getClassName() {
        return className;
    }

    @JsonValue
    public String getKey() {
        return key;
    }

    @JsonCreator
    public static WorkflowFunctionType fromString(String key) {
        for (WorkflowFunctionType functionType: WorkflowFunctionType.values()) {
            if (functionType.key.equals(key)) {
                return functionType;
            }
        }

        return null;
    }
}
