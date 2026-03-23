package ru.slie.luna.plugins.gravity.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.Issue;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.condition.WorkflowCondition;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.user.User;

import java.util.HashMap;
import java.util.Map;

public class GravityScriptCondition extends AbstractGravityFunction implements WorkflowCondition {
    private static final Logger log = LoggerFactory.getLogger(GravityScriptCondition.class);

    public GravityScriptCondition(I18nResolver i18n, ScriptRunnerService scriptRunnerService) {
        super(i18n, scriptRunnerService);
    }

    @Override
    public boolean execute(User user, Issue issue, WorkflowAction action, Map<String, String> params) {
        String script = params.get(SCRIPT_KEY);
        Map<String, Object> scriptEnv = new HashMap<>();
        scriptEnv.put("user", user);
        scriptEnv.put("issue", issue);
        scriptEnv.put("action", action);
        try {
            Object val = scriptRunnerService.execute(script, scriptEnv, this::logConsumer);
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
