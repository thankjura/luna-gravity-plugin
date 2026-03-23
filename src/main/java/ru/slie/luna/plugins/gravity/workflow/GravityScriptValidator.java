package ru.slie.luna.plugins.gravity.workflow;

import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.Issue;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.validator.WorkflowValidator;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GravityScriptValidator extends AbstractGravityFunction implements WorkflowValidator {
    public GravityScriptValidator(I18nResolver i18n, ScriptRunnerService scriptRunnerService) {
        super(i18n, scriptRunnerService);
    }

    @Override
    public void execute(User user, Issue issue, WorkflowAction action, Map<String, String> funcParams) throws ValidateException {
        String script = funcParams.get(SCRIPT_KEY);
        Map<String, Object> scriptEnv = new HashMap<>();
        scriptEnv.put("user", user);
        scriptEnv.put("issue", issue);
        scriptEnv.put("action", action);
        scriptRunnerService.execute(script, scriptEnv, this::logConsumer);
    }

    @Override
    public List<String> getRequiredFields(User user, Issue issue, Map<String, String> funcParams) {
        return List.of();
    }

    @Override
    public String getName() {
        return i18n.getText("gravity.workflow.validator.name");
    }

    @Override
    public String getDescription() {
        return i18n.getText("gravity.workflow.validator.description");
    }
}
