package ru.slie.luna.plugins.gravity.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.MutableIssue;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.postfunction.WorkflowPostfunction;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.user.User;

import java.util.HashMap;
import java.util.Map;

public class GravityScriptPostfunction extends AbstractGravityFunction implements WorkflowPostfunction {
    private static final Logger log = LoggerFactory.getLogger(GravityScriptPostfunction.class);

    public GravityScriptPostfunction(I18nResolver i18n, ScriptRunnerService scriptRunnerService) {
        super(i18n, scriptRunnerService);
    }

    @Override
    public void execute(User user, MutableIssue issue, WorkflowAction action, Map<String, String> funcParams) {
        String script = funcParams.get(SCRIPT_KEY);
        Map<String, Object> scriptEnv = new HashMap<>();
        scriptEnv.put("user", user);
        scriptEnv.put("issue", issue);
        scriptEnv.put("action", action);
        try {
            scriptRunnerService.execute(script, scriptEnv, this::logConsumer);
        } catch (ValidateException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return i18n.getText("gravity.workflow.postfunction.name");
    }

    @Override
    public String getDescription() {
        return i18n.getText("gravity.workflow.postfunction.description");
    }
}
