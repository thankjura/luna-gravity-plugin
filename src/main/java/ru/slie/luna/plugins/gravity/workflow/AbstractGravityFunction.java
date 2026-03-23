package ru.slie.luna.plugins.gravity.workflow;

import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.workflow.WorkflowFunction;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptParseErrorException;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGravityFunction implements WorkflowFunction  {
    protected final I18nResolver i18n;
    protected final ScriptRunnerService scriptRunnerService;
    public static String SCRIPT_KEY = "script";

    protected AbstractGravityFunction(I18nResolver i18n,
                                      ScriptRunnerService scriptRunnerService) {
        this.i18n = i18n;
        this.scriptRunnerService = scriptRunnerService;
    }

    protected void logConsumer(String msg) {

    }

    @Override
    public void validateParams(Map<String, String> params) throws ValidateException {
        if (!params.containsKey(SCRIPT_KEY)) {
            throw new ValidateException(SCRIPT_KEY, i18n.getText("gravity.script.required"));
        }

        try {
            scriptRunnerService.validate(params.get(SCRIPT_KEY));
        } catch (ScriptParseErrorException e) {
            throw new ValidateException(SCRIPT_KEY, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getParamsForView(Map<String, String> params) {
        return new HashMap<>(params);
    }

    @Override
    public boolean isAllowMultiple() {
        return true;
    }
}
