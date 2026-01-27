package ru.slie.luna.plugins.gravity.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScriptRunnerService {
    public void validate(String scriptContent) throws ScriptParseErrorException {
        try {
            GroovyShell shell = new GroovyShell();
            shell.parse(scriptContent);
        } catch (CompilationFailedException e) {
            throw new ScriptParseErrorException(e);
        }
    }

    public Object execute(String scriptContent, Map<String, Object> params) throws ScriptExecutionErrorException {
        try {
            Binding binding = new Binding(params);
            GroovyShell shell = new GroovyShell(binding);

            Script script = shell.parse(scriptContent);
            return script.run();
        } catch (Exception e) {
            throw new ScriptExecutionErrorException(e);
        }
    }
}
