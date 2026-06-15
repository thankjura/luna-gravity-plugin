package ru.slie.luna.plugins.gravity.workflow;

import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    public static String SCRIPT_KEY = "script";
    public static String SCRIPT_NOTE = "note";
    public static String SCRIPT_DISABLED = "disabled";

    protected AbstractGravityFunction(I18nResolver i18n,
                                      ScriptRunnerService scriptRunnerService,
                                      ApplicationEventPublisher eventPublisher) {
        this.i18n = i18n;
        this.scriptRunnerService = scriptRunnerService;
        this.eventPublisher = eventPublisher;
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



    public static boolean isDisabled(Map<String, String> params) {
        return params != null && params.containsKey("disabled") && "yes".equals(params.get("disabled"));
    }

    public static void setDisabled(Map<String, String> params, boolean disabled) {
        if (disabled) {
            params.put(SCRIPT_DISABLED, "yes");
        } else {
            params.remove(SCRIPT_DISABLED);
        }
    }
}
