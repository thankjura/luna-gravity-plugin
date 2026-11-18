package ru.slie.luna.plugins.gravity.script;

import groovy.lang.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
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
import java.util.function.Function;

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
        return executeWithMetrics(scriptId, binding -> new GroovyShell(binding).parse(script), scriptEnv, scripValidators);
    }

    /** Runs an already compiled script class (scripts from files are compiled once and cached). */
    public Object executeWithMetrics(String scriptId, Class<? extends Script> scriptClass, Map<String, Object> scriptEnv, boolean scripValidators) throws ValidateException {
        return executeWithMetrics(scriptId, binding -> InvokerHelper.createScript(scriptClass, binding), scriptEnv, scripValidators);
    }

    private Object executeWithMetrics(String scriptId, Function<Binding, Script> factory, Map<String, Object> scriptEnv, boolean scripValidators) throws ValidateException {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        boolean isCpuTimeSupported = threadBean.isCurrentThreadCpuTimeSupported();
        LocalDateTime now = LocalDateTime.now();
        long startWallTimeNano = System.nanoTime();
        long startCpuTimeNano = isCpuTimeSupported ? threadBean.getCurrentThreadCpuTime() : 0;
        StringBuilder builder = new StringBuilder();

        String stackTrace = null;
        String payload = getPayloadString(scriptEnv);

        try {
            return execute(factory, scriptEnv, builder::append);
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

    /**
     * Compiles a script into a class. Each script gets its own class loader, so a replaced script can be unloaded.
     *
     * @param name file name used in stack traces
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Script> compile(String scriptContent, String name) throws ScriptParseErrorException {
        try {
            GroovyShell shell = new GroovyShell();
            GroovyCodeSource source = new GroovyCodeSource(scriptContent, scriptClassName(name), GroovyShell.DEFAULT_CODE_BASE);
            source.setCachable(false);
            return (Class<? extends Script>) shell.getClassLoader().parseClass(source, false);
        } catch (CompilationFailedException e) {
            throw new ScriptParseErrorException(e);
        }
    }

    private static String scriptClassName(String name) {
        String base = name.replaceAll("\\.groovy$", "").replaceAll("[^A-Za-z0-9_]", "_");
        if (base.isEmpty() || !Character.isJavaIdentifierStart(base.charAt(0))) {
            base = "_" + base;
        }
        return base + ".groovy";
    }

    public Object execute(String scriptContent, Map<String, Object> params, Consumer<String> logConsumer) throws ValidateException {
        return execute(binding -> new GroovyShell(binding).parse(scriptContent), params, logConsumer);
    }

    private Object execute(Function<Binding, Script> factory, Map<String, Object> params, Consumer<String> logConsumer) throws ValidateException {
        try (ScriptLogger logger = new ScriptLogger("GravityScript", logConsumer)) {
            Binding binding = new Binding(params);
            binding.setVariable("out", new ConsumerWriter(logConsumer));
            binding.setVariable("log", logger.getlogger());
            Script script = factory.apply(binding);
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
