package ru.slie.luna.plugins.gravity.workflow;

import org.springframework.context.ApplicationEventPublisher;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.workflow.WorkflowFunction;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptParseErrorException;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.plugins.gravity.script.file.ScriptFileManager;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGravityFunction implements WorkflowFunction  {
    protected final I18nResolver i18n;
    protected final ScriptRunnerService scriptRunnerService;
    protected final ScriptFileManager scriptFileManager;
    private final ApplicationEventPublisher eventPublisher;

    public static String SCRIPT_KEY = "script";
    public static String SCRIPT_NOTE = "note";
    public static String SCRIPT_DISABLED = "disabled";
    /** Where the script comes from: {@link #SOURCE_INLINE} (default) or {@link #SOURCE_FILE}. */
    public static String SCRIPT_SOURCE = "source";
    /** Path of the script file relative to {@code <luna.home>/scripts}. */
    public static String SCRIPT_FILE = "file";
    public static String SOURCE_INLINE = "inline";
    public static String SOURCE_FILE = "file";

    protected AbstractGravityFunction(I18nResolver i18n,
                                      ScriptRunnerService scriptRunnerService,
                                      ScriptFileManager scriptFileManager,
                                      ApplicationEventPublisher eventPublisher) {
        this.i18n = i18n;
        this.scriptRunnerService = scriptRunnerService;
        this.scriptFileManager = scriptFileManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void validateParams(Map<String, String> params) throws ValidateException {
        if (isFileSource(params)) {
            String file = params.get(SCRIPT_FILE);
            if (file == null || file.isBlank()) {
                throw new ValidateException(SCRIPT_FILE, i18n.getText("gravity.script.file.required"));
            }
            if (!scriptFileManager.isScript(file)) {
                throw new ValidateException(SCRIPT_FILE, i18n.getText("gravity.files.not_found", file));
            }
            ScriptFileManager.CompiledScript compiled = scriptFileManager.getCompiled(file);
            if (compiled.error() != null) {
                throw new ValidateException(SCRIPT_FILE, compiled.error());
            }
            return;
        }

        if (!params.containsKey(SCRIPT_KEY)) {
            throw new ValidateException(SCRIPT_KEY, i18n.getText("gravity.script.required"));
        }

        try {
            scriptRunnerService.validate(params.get(SCRIPT_KEY));
        } catch (ScriptParseErrorException e) {
            throw new ValidateException(SCRIPT_KEY, e.getMessage());
        }
    }

    /** Runs the inline script or the cached script file. */
    protected Object runScript(String scriptId, Map<String, String> params, Map<String, Object> env, boolean scriptValidators) throws ValidateException {
        if (isFileSource(params)) {
            String file = params.get(SCRIPT_FILE);
            ScriptFileManager.CompiledScript compiled = scriptFileManager.getCompiled(file);
            if (compiled.scriptClass() == null) {
                throw new ValidateException("message", i18n.getText("gravity.script.file.failed", file, compiled.error()));
            }
            return scriptRunnerService.executeWithMetrics(scriptId, compiled.scriptClass(), env, scriptValidators);
        }
        return scriptRunnerService.executeWithMetrics(scriptId, params.get(SCRIPT_KEY), env, scriptValidators);
    }

    @Override
    public Map<String, Object> getParamsForView(Map<String, String> params) {
        Map<String, Object> out = new HashMap<>(params);
        if (isFileSource(params)) {
            ScriptFileManager.CompiledScript compiled = scriptFileManager.getCompiled(params.get(SCRIPT_FILE));
            out.put(SCRIPT_KEY, compiled.content() != null ? compiled.content() : "");
            if (compiled.error() != null) {
                out.put("fileError", compiled.error());
            }
        }
        return out;
    }

    @Override
    public boolean isAllowMultiple() {
        return true;
    }

    public static boolean isFileSource(Map<String, String> params) {
        return params != null && SOURCE_FILE.equals(params.get(SCRIPT_SOURCE));
    }

    public static String getScriptFile(Map<String, String> params) {
        return isFileSource(params) ? params.get(SCRIPT_FILE) : null;
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
