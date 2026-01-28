package ru.slie.luna.plugins.gravity.script;

import groovy.lang.*;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.stereotype.Component;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.utils.ConsumerWriter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class ScriptRunnerService {
    private final I18nResolver i18n;
    public ScriptRunnerService(I18nResolver i18n) {
        this.i18n = i18n;
    }

    public void validate(String scriptContent) throws ScriptParseErrorException {
        try {
            GroovyShell shell = new GroovyShell();
            shell.parse(scriptContent);
        } catch (CompilationFailedException e) {
            throw new ScriptParseErrorException(e);
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
