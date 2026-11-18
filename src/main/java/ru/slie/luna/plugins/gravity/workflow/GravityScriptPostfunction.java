package ru.slie.luna.plugins.gravity.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.workflow.WorkflowTransientVars;
import ru.slie.luna.issue.workflow.postfunction.WorkflowPostfunction;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.plugins.gravity.script.file.ScriptFileManager;

import java.util.HashMap;
import java.util.Map;

public class GravityScriptPostfunction extends AbstractGravityFunction implements WorkflowPostfunction {
    private static final Logger log = LoggerFactory.getLogger(GravityScriptPostfunction.class);

    public GravityScriptPostfunction(I18nResolver i18n, ScriptRunnerService scriptRunnerService, ScriptFileManager scriptFileManager,
                                     ApplicationEventPublisher eventPublisher) {
        super(i18n, scriptRunnerService, scriptFileManager, eventPublisher);
    }

    @Override
    public void execute(WorkflowTransientVars transientVars, Map<String, String> funcParams) {
        if (isDisabled(funcParams)) {
            return;
        }
        Map<String, Object> scriptEnv = new HashMap<>();
        scriptEnv.put("user", transientVars.getUser());
        scriptEnv.put("issue", transientVars.getIssue());
        scriptEnv.put("transientVars", transientVars);
        try {
            runScript(transientVars.getActionFunction().getId(), funcParams, scriptEnv, true);
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
