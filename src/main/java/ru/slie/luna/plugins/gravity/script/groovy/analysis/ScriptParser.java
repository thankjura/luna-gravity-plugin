package ru.slie.luna.plugins.gravity.script.groovy.analysis;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import ru.slie.luna.plugins.gravity.script.groovy.completion.DynamicBindingCustomizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Compiles a script up to semantic analysis without ever failing. In tolerant mode lines with syntax errors are
 * blanked out and missing brackets are appended until the rest of the script parses, so completion keeps working
 * while the script is being edited.
 */
public final class ScriptParser {
    public static final String PLACEHOLDER = "__LUNA__";
    private static final int MAX_REPAIRS = 10;

    public record Problem(int line, int column, int endLine, int endColumn, String message, boolean syntax) {}

    public record ParseResult(ModuleNode module, List<Problem> problems, String text) {
        public boolean hasSyntaxErrors() {
            return problems.stream().anyMatch(Problem::syntax);
        }
    }

    private final ClassLoader loader;

    public ScriptParser(ClassLoader loader) {
        this.loader = loader;
    }

    public ParseResult parse(String text, Map<String, Class<?>> variables) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setTolerance(Integer.MAX_VALUE);
        GroovyClassLoader gcl = new GroovyClassLoader(loader, config);
        CompilationUnit cu = new CompilationUnit(config, null, gcl);
        DynamicBindingCustomizer bindings = new DynamicBindingCustomizer(variables);
        cu.addPhaseOperation((source, context, classNode) -> bindings.call(source, context, classNode), Phases.CONVERSION);

        TolerantErrorCollector collector = new TolerantErrorCollector(config);
        SourceUnit su = new SourceUnit("Script1.groovy", text, config, gcl, collector);
        cu.addSource(su);

        Set<Message> messages = new LinkedHashSet<>();
        try {
            cu.compile(Phases.SEMANTIC_ANALYSIS);
        } catch (MultipleCompilationErrorsException e) {
            addAll(messages, e.getErrorCollector());
        } catch (CompilationFailedException e) {
            if (e.getCause() instanceof SyntaxException se) {
                messages.add(new SyntaxErrorMessage(se, su));
            } else {
                messages.add(new SimpleMessage(e.getMessage(), su));
            }
        } catch (Throwable e) {
            messages.add(new SimpleMessage(String.valueOf(e.getMessage()), su));
        }
        addAll(messages, collector);
        if (cu.getErrorCollector() != collector) {
            addAll(messages, cu.getErrorCollector());
        }

        boolean parsed = su.getAST() != null && su.getAST().getStatementBlock() != null;
        boolean hasSyntaxMessages = messages.stream().anyMatch(m -> m instanceof SyntaxErrorMessage);
        List<Problem> problems = new ArrayList<>();
        for (Message message : messages) {
            if (hasSyntaxMessages && !(message instanceof SyntaxErrorMessage)) {
                continue; // generic "parsing failed" duplicates of the real error
            }
            Problem p = toProblem(message, !parsed);
            if (p != null && p.message() != null && !"null".equals(p.message()) && !problems.contains(p)) {
                problems.add(p);
            }
        }
        if (!parsed && problems.isEmpty()) {
            problems.add(new Problem(1, 1, 1, 2, "Unable to parse script", true));
        }
        return new ParseResult(parsed ? su.getAST() : null, problems, text);
    }

    /**
     * Parses broken code: closes brackets left open on {@code cursorLine} and at the end of the script, then
     * repeatedly blanks lines reported by the parser.
     */
    public ParseResult parseTolerant(String text, Map<String, Class<?>> variables, int cursorLine) {
        String[] lines = text.split("\n", -1);
        if (cursorLine >= 1 && cursorLine <= lines.length) {
            String closers = GroovyLexer.missingClosers(GroovyLexer.lex(lines[cursorLine - 1]).tokens()).replace("}", "");
            lines[cursorLine - 1] = lines[cursorLine - 1] + closers;
        }

        ParseResult result = null;
        for (int attempt = 0; attempt <= MAX_REPAIRS; attempt++) {
            String base = String.join("\n", lines);
            String closers = GroovyLexer.missingClosers(GroovyLexer.lex(base).tokens());
            String work = closers.isEmpty() ? base : base + "\n" + closers;
            result = parse(work, variables);
            if (!result.hasSyntaxErrors()) {
                return result;
            }
            Problem error = result.problems().stream().filter(Problem::syntax).findFirst().orElseThrow();
            int line = Math.min(Math.max(error.line(), 1), lines.length);
            // the parser often reports a missing token on the next non-empty line
            while (line > 1 && lines[line - 1].isBlank()) {
                line--;
            }
            if (lines[line - 1].isBlank()) {
                break;
            }
            lines[line - 1] = " ".repeat(lines[line - 1].length());
        }
        return result;
    }

    private static void addAll(Set<Message> messages, ErrorCollector collector) {
        if (collector != null && collector.getErrors() != null) {
            messages.addAll(collector.getErrors());
        }
    }

    private static Problem toProblem(Message message, boolean syntax) {
        if (message instanceof SyntaxErrorMessage sem) {
            SyntaxException se = sem.getCause();
            int line = Math.max(se.getStartLine(), 1);
            int col = Math.max(se.getStartColumn(), 1);
            int endLine = Math.max(se.getEndLine(), line);
            int endCol = se.getEndColumn();
            if (endLine == line && endCol <= col) {
                endCol = col + 1;
            }
            return new Problem(line, col, endLine, endCol, cleanMessage(se.getOriginalMessage()), syntax);
        }
        if (message instanceof ExceptionMessage em) {
            return new Problem(1, 1, 1, 2, String.valueOf(em.getCause().getMessage()), syntax);
        }
        if (message instanceof SimpleMessage sm) {
            return new Problem(1, 1, 1, 2, sm.getMessage(), syntax);
        }
        return null;
    }

    private static String cleanMessage(String message) {
        if (message == null) {
            return "Syntax error";
        }
        String clean = message.replaceAll("\\s*@ line \\d+, column \\d+\\.?.*", "").replaceAll("\\s+", " ").trim();
        return clean.length() > 160 ? clean.substring(0, 157) + "..." : clean;
    }

    private static final class TolerantErrorCollector extends ErrorCollector {
        TolerantErrorCollector(CompilerConfiguration configuration) {
            super(configuration);
        }

        @Override
        protected void failIfErrors() {
        }
    }
}
