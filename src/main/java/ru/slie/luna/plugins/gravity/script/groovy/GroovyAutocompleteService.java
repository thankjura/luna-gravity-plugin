package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroovyAutocompleteService {
    private final static Logger log = LoggerFactory.getLogger(GroovyAutocompleteService.class);

    private final GenericApplicationContext context;
    public GroovyAutocompleteService(GenericApplicationContext context) {
        this.context = context;
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

            System.out.println(nodeFinder.getFoundNode());
        } catch (Exception e) {
            // TODO: display row and col in UI
            log.error("parse error", e);
        }

        return AutocompleteResult.empty();
    }
}
