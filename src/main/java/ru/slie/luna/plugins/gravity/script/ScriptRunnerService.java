package ru.slie.luna.plugins.gravity.script;

import groovy.lang.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.utils.ConsumerWriter;
import ru.slie.luna.plugins.gravity.utils.MetricsHelper;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

import static ru.slie.luna.plugins.gravity.utils.MetricsHelper.getPayloadString;

@Component
public class ScriptRunnerService {
    private final I18nResolver i18n;
    private final ApplicationEventPublisher eventPublisher;

    public ScriptRunnerService(I18nResolver i18n,
                               ApplicationEventPublisher eventPublisher) {
        this.i18n = i18n;
        this.eventPublisher = eventPublisher;
    }

    public void validate(String scriptContent) throws ScriptParseErrorException {
        try {
            GroovyShell shell = new GroovyShell();
            shell.parse(scriptContent);
        } catch (CompilationFailedException e) {
            throw new ScriptParseErrorException(e);
        }
    }

    public Object executeWithMetrics(String scriptId, String script, Map<String, Object> scriptEnv, boolean scripValidators) throws ValidateException {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        boolean isCpuTimeSupported = threadBean.isCurrentThreadCpuTimeSupported();
        LocalDateTime now = LocalDateTime.now();
        long startWallTimeNano = System.nanoTime();
        long startCpuTimeNano = isCpuTimeSupported ? threadBean.getCurrentThreadCpuTime() : 0;
        StringBuilder builder = new StringBuilder();

        String stackTrace = null;
        String payload = getPayloadString(scriptEnv);

        try {
            return execute(script, scriptEnv, builder::append);
        } catch (Exception e) {
            if (!scripValidators || !(e instanceof ValidateException)) {
                stackTrace = MetricsHelper.stackTraceToString(e);
            }
            throw e;
        } finally {
            long endWallTimeNano = System.nanoTime();
            long endCpuTimeNano = isCpuTimeSupported ? threadBean.getCurrentThreadCpuTime() : 0;

            long executionTimeMs = (endWallTimeNano - startWallTimeNano) / 1_000_000;
            long cpuTimeMs = isCpuTimeSupported ? (endCpuTimeNano - startCpuTimeNano) / 1_000_000 : 0;

            eventPublisher.publishEvent(new ScriptRunEvent(
                    scriptId, now, cpuTimeMs, executionTimeMs, stackTrace, payload, builder.toString()
            ));
        }
    }

    public Object execute(String scriptContent, Map<String, Object> params, Consumer<String> logConsumer) throws ValidateException {
        try (ScriptLogger logger = new ScriptLogger("GravityScript", logConsumer)) {
            Binding binding = new Binding(params);
            binding.setVariable("out", new ConsumerWriter(logConsumer));
            binding.setVariable("log", logger.getlogger());
            GroovyShell shell = new GroovyShell(binding);
            Script script = shell.parse(scriptContent);
            return script.run();
        } catch (MissingMethodException e) {
            String text = i18n.getText("gravity.script.method_not_found", e.getMethod());
            text += " " + i18n.getText("gravity.script.for_arguments", Arrays.toString(e.getArguments()));
            throw new ValidateException("message", text);
        } catch (MissingPropertyException e) {
            throw new ValidateException("message", i18n.getText("gravity.script.property_not_found", e.getProperty()));
        } catch (Throwable e) {
            throw new ValidateException("message", e.getLocalizedMessage());
        }
    }
}
