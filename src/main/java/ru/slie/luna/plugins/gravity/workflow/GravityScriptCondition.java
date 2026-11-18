package ru.slie.luna.plugins.gravity.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.workflow.WorkflowTransientVars;
import ru.slie.luna.issue.workflow.condition.WorkflowCondition;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.plugins.gravity.script.file.ScriptFileManager;

import java.util.HashMap;
import java.util.Map;

public class GravityScriptCondition extends AbstractGravityFunction implements WorkflowCondition {
    private static final Logger log = LoggerFactory.getLogger(GravityScriptCondition.class);

    public GravityScriptCondition(I18nResolver i18n, ScriptRunnerService scriptRunnerService, ScriptFileManager scriptFileManager,
                                  ApplicationEventPublisher eventPublisher) {
        super(i18n, scriptRunnerService, scriptFileManager, eventPublisher);
    }

    @Override
    public boolean execute(WorkflowTransientVars transientVars, Map<String, String> params) {
        if (isDisabled(params)) {
            return true;
        }
        Map<String, Object> scriptEnv = new HashMap<>();
        scriptEnv.put("user", transientVars.getUser());
        scriptEnv.put("issue", transientVars.getIssue());
        scriptEnv.put("action", transientVars.getAction());
        scriptEnv.put("transientVars", transientVars);
        try {
            Object val = runScript(transientVars.getActionFunction().getId(), params, scriptEnv, true);
            return switch (val) {
                case null -> false;
                case String string -> !string.isBlank();
                case Boolean bool -> bool;
                case Number number -> number.doubleValue() != 0;
                default -> true;
            };
        } catch (ValidateException e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    @Override
    public String getName() {
        return i18n.getText("gravity.workflow.condition.name");
    }

    @Override
    public String getDescription() {
        return i18n.getText("gravity.workflow.condition.description");
    }
}
