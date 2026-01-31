package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
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

    private final LunaBeanProvider beanProvider;
    private final ClassPropertyProvider propertyProvider;

    public AutocompleteGroovyService(LunaBeanProvider beanProvider,
                                     ClassPropertyProvider propertyProvider) {
        this.beanProvider = beanProvider;
        this.propertyProvider = propertyProvider;
    }

    public AutocompleteResult getSuggestions(String scriptText, int line, int column, int limit) {
        List<Suggestion> suggestions = new ArrayList<>();

        CompilationUnit cu = new CompilationUnit();
        SourceUnit su = cu.addSource("script.groovy", scriptText);

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
                        suggestions.addAll(beanProvider.getSuggestions(e.getText().toLowerCase(), limit));
                    }
                }
                case PropertyExpression e -> {
                    Expression exp = e.getObjectExpression();
                    if (exp != null) {
                        Class<?> clazz = exp.getType().getTypeClass();
                        if (clazz != null) {
                            range = AutocompleteRange.forNode(e.getProperty());
                            suggestions.addAll(propertyProvider.getSuggestions(e.getPropertyAsString().toLowerCase(), clazz, limit));
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
