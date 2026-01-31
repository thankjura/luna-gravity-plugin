package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.slie.luna.plugins.gravity.script.groovy.completion.ClassPropertyProvider;
import ru.slie.luna.plugins.gravity.script.groovy.completion.LunaBeanProvider;

import java.util.ArrayList;
import java.util.List;

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
        List<Suggestion> suggestions = new ArrayList<>();

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
        if (node != null) {
            AutocompleteRange range = AutocompleteRange.forNode(node);
            switch (node) {
                case VariableExpression e -> {
                    if (e.isDynamicTyped()) {
                        suggestions.addAll(beanProvider.getSuggestions(e.getText().replaceAll(LUNA_PLACEHOLDER, "").toLowerCase(), limit));
                    }
                }
                case PropertyExpression e -> {
                    Expression exp = e.getObjectExpression();
                    if (exp != null) {
                        Class<?> clazz = exp.getType().getTypeClass();
                        if (clazz != null) {
                            range = AutocompleteRange.forNode(e.getProperty());

                            suggestions.addAll(propertyProvider.getStaticSuggestions(e.getPropertyAsString().replaceAll(LUNA_PLACEHOLDER, "").toLowerCase(), clazz, (exp instanceof VariableExpression), limit));
                        }
                    }
                }
                default -> {}
            }

            return new AutocompleteResult(suggestions, range);
        }


        return AutocompleteResult.empty();
    }
}
