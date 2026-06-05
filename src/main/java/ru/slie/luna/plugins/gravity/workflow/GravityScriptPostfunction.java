package ru.slie.luna.plugins.gravity.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.workflow.WorkflowTransientVars;
import ru.slie.luna.issue.workflow.postfunction.WorkflowPostfunction;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.metrics.ScriptMetricsListener;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;

import java.util.HashMap;
import java.util.Map;

public class GravityScriptPostfunction extends AbstractGravityFunction implements WorkflowPostfunction {
    private static final Logger log = LoggerFactory.getLogger(GravityScriptPostfunction.class);

    public GravityScriptPostfunction(I18nResolver i18n, ScriptRunnerService scriptRunnerService, ScriptMetricsListener scriptMetricsListener) {
        super(i18n, scriptRunnerService, scriptMetricsListener);
    }

    @Override
    public void execute(WorkflowTransientVars transientVars, Map<String, String> funcParams) {
        String script = funcParams.get(SCRIPT_KEY);
        Map<String, Object> scriptEnv = new HashMap<>();
        scriptEnv.put("user", transientVars.getUser());
        scriptEnv.put("issue", transientVars.getIssue());
        scriptEnv.put("transientVars", transientVars);
        try {
            executeWithMetrics(transientVars.getActionFunction().getId(), script, scriptEnv);
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
