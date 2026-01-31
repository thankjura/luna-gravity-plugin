package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.slie.luna.plugins.gravity.script.groovy.completion.ClassPropertyProvider;
import ru.slie.luna.plugins.gravity.script.groovy.completion.LunaBeanProvider;

@Component
public class AutocompleteGroovyService {
    private final static Logger log = LoggerFactory.getLogger(AutocompleteGroovyService.class);
    private static final String LUNA_PLACEHOLDER = "__LUNA_PLACEHOLDER__";

    private final LunaBeanProvider beanProvider;
    private final ClassPropertyProvider propertyProvider;

    public AutocompleteGroovyService(LunaBeanProvider beanProvider,
                                     ClassPropertyProvider propertyProvider) {
        this.beanProvider = beanProvider;
        this.propertyProvider = propertyProvider;
    }

    public AutocompleteResult getSuggestions(String scriptText, int line, int column, int limit) {
        if (scriptText.endsWith(".")) {
            scriptText += "__LUNA_PLACEHOLDER__";
        }

        CompilerConfiguration config = new CompilerConfiguration();
        config.setTolerance(10);
        CompilationUnit cu = new CompilationUnit();
        SourceUnit su = cu.addSource(new SourceUnit("script.groovy", scriptText, config, null, new CustomErrorCollector(config)));

        NodeFinder nodeFinder = new NodeFinder(line, column);
        try {
            cu.compile(Phases.CANONICALIZATION);
            ModuleNode module = su.getAST();
            module.getStatementBlock().visit(nodeFinder);
//            module.getClasses().forEach(classNode -> {
//                classNode.visitContents(finder);
//            });
        } catch (Exception e) {
            log.error("parse error", e);
        }

        ASTNode node = nodeFinder.getFoundNode();
        AutocompleteResult result = new AutocompleteResult();

        if (node != null) {
            result.setRange(AutocompleteRange.forNode(node));
            switch (node) {
                case VariableExpression e -> {
                    if (e.isDynamicTyped()) {
                        result.addResult(beanProvider.getSuggestions(e.getText().replaceAll(LUNA_PLACEHOLDER, "").toLowerCase(), limit));
                    }
                }
                case PropertyExpression e -> {
                    Expression exp = e.getObjectExpression();
                    if (exp != null) {
                        Class<?> clazz = exp.getType().getTypeClass();
                        if (clazz != null) {
                            result.setRange(AutocompleteRange.forNode(e.getProperty()));
                            result.addResult(propertyProvider.getSuggestions(e.getPropertyAsString().replaceAll(LUNA_PLACEHOLDER, "").toLowerCase(), clazz, (exp instanceof VariableExpression), limit));
                        }
                    }
                }
                default -> {}
            }
        }


        return result;
    }
}
