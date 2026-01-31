package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;

public class CustomErrorCollector extends ErrorCollector {
    public CustomErrorCollector(CompilerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void failIfErrors() throws CompilationFailedException {

    }
}
