package ru.slie.luna.plugins.gravity.model;

import ru.slie.luna.plugins.gravity.workflow.GravityScriptCondition;
import ru.slie.luna.plugins.gravity.workflow.GravityScriptPostfunction;
import ru.slie.luna.plugins.gravity.workflow.GravityScriptValidator;

public enum WorkflowFunctionType {
    CONDITION(GravityScriptCondition.class),
    VALIDATOR(GravityScriptValidator.class),
    POSTFUNCTION(GravityScriptPostfunction.class);

    private final  String className;

    WorkflowFunctionType(Class<?> clazz) {
        this.className = clazz.getCanonicalName();
    }

    public String getClassName() {
        return className;
    }
}
