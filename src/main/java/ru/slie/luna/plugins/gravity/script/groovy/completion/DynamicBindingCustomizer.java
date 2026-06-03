package ru.slie.luna.plugins.gravity.script.groovy.completion;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

import java.lang.reflect.Modifier;
import java.util.Map;

public class DynamicBindingCustomizer extends CompilationCustomizer {
    private final Map<String, Class<?>> contextVariables;

    public DynamicBindingCustomizer(Map<String, Class<?>> contextVariables) {
        super(CompilePhase.CONVERSION);
        this.contextVariables = contextVariables;
    }

    @Override
    public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
        if (classNode.isScript()) {
            contextVariables.forEach((varName, varClass) -> {
                ClassNode typeNode = ClassHelper.make(varClass);

                FieldNode fieldNode = new FieldNode(
                        varName,
                        Modifier.PUBLIC,
                        typeNode,
                        classNode,
                        null
                );
                classNode.addField(fieldNode);
            });
        }
    }
}
