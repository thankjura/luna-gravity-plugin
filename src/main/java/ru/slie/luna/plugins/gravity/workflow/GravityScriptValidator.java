package ru.slie.luna.plugins.gravity.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.workflow.WorkflowTransientVars;
import ru.slie.luna.issue.workflow.validator.WorkflowValidator;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GravityScriptValidator extends AbstractGravityFunction implements WorkflowValidator {
    private final static Logger log = LoggerFactory.getLogger(GravityScriptValidator.class);

    public GravityScriptValidator(I18nResolver i18n, ScriptRunnerService scriptRunnerService, ApplicationEventPublisher eventPublisher) {
        super(i18n, scriptRunnerService, eventPublisher);
    }

    @Override
    public void execute(WorkflowTransientVars transientVars, Map<String, String> funcParams) throws ValidateException {
        if (isDisabled(funcParams)) {
            return;
        }
        String script = funcParams.get(SCRIPT_KEY);
        Map<String, Object> scriptEnv = new HashMap<>();
        scriptEnv.put("user", transientVars.getUser());
        scriptEnv.put("issue", transientVars.getIssue());
        scriptEnv.put("transientVars", transientVars);
        try {
            scriptRunnerService.executeWithMetrics(transientVars.getActionFunction().getName(), script, scriptEnv, false);
        } catch (Exception e) {
            if (e instanceof ValidateException) {
                throw (ValidateException) e;
            }
            log.error(e.getMessage(), e);
            throw new ValidateException("message", e.getMessage());
        }
    }

    @Override
    public List<String> getRequiredFields(WorkflowTransientVars transientVars, Map<String, String> funcParams) {
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
