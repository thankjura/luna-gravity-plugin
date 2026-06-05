package ru.slie.luna.plugins.gravity.workflow;

import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.workflow.WorkflowFunction;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.metrics.ScriptMetricsListener;
import ru.slie.luna.plugins.gravity.script.ScriptParseErrorException;
import ru.slie.luna.plugins.gravity.script.ScriptRunEvent;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.plugins.gravity.utils.ExceptionHelper;
import tools.jackson.databind.ObjectMapper;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractGravityFunction implements WorkflowFunction  {
    protected final I18nResolver i18n;
    protected final ScriptRunnerService scriptRunnerService;
    private final ScriptMetricsListener eventListener;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String SCRIPT_KEY = "script";
    public static String SCRIPT_NOTE = "note";

    protected AbstractGravityFunction(I18nResolver i18n,
                                      ScriptRunnerService scriptRunnerService,
                                      ScriptMetricsListener eventListener) {
        this.i18n = i18n;
        this.scriptRunnerService = scriptRunnerService;
        this.eventListener = eventListener;
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

    private String getPayloadString(Map<String, Object> scriptEnv) {
        if (scriptEnv == null || scriptEnv.isEmpty()) {
            return "{}";
        }

        try {
            Map<String, String> simplifiedMap = scriptEnv.entrySet().stream()
                                                        .collect(Collectors.toMap(
                                                                Map.Entry::getKey,
                                                                entry -> {
                                                                    Object val = entry.getValue();
                                                                    if (val == null) {
                                                                        return "null [null]";
                                                                    }
                                                                    return val + " [" + val.getClass().getSimpleName() + "]";
                                                                },
                                                                (existing, replacement) -> existing
                                                        ));

            return OBJECT_MAPPER.writeValueAsString(simplifiedMap);
        } catch (Exception e) {
            return "{\"error\": \"Failed to serialize env: " + e.getMessage() + "\"}";
        }
    }

    protected Object executeWithMetrics(String functionId, String script, Map<String, Object> scriptEnv) throws ValidateException {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        boolean isCpuTimeSupported = threadBean.isCurrentThreadCpuTimeSupported();
        LocalDateTime now = LocalDateTime.now();
        long startWallTimeNano = System.nanoTime();
        long startCpuTimeNano = isCpuTimeSupported ? threadBean.getCurrentThreadCpuTime() : 0;

        String stackTrace = null;
        String payload = getPayloadString(scriptEnv);

        try {
            return scriptRunnerService.execute(script, scriptEnv, this::logConsumer);
        } catch (Exception e) {
            stackTrace = ExceptionHelper.stackTraceToString(e);
            throw e;
        } finally {
            long endWallTimeNano = System.nanoTime();
            long endCpuTimeNano = isCpuTimeSupported ? threadBean.getCurrentThreadCpuTime() : 0;

            long executionTimeMs = (endWallTimeNano - startWallTimeNano) / 1_000_000;
            long cpuTimeMs = isCpuTimeSupported ? (endCpuTimeNano - startCpuTimeNano) / 1_000_000 : 0;

            eventListener.enqueue(new ScriptRunEvent(
                    functionId, now, cpuTimeMs, executionTimeMs, stackTrace, payload
            ));
        }
    }
}
