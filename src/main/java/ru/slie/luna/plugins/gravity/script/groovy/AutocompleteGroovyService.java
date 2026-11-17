package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.slie.luna.plugins.gravity.script.groovy.analysis.*;
import ru.slie.luna.plugins.gravity.script.groovy.analysis.TypeResolver.ResolvedType;
import ru.slie.luna.plugins.gravity.script.groovy.completion.MemberSuggestions;
import ru.slie.luna.plugins.gravity.script.groovy.index.*;
import ru.slie.luna.plugins.gravity.script.groovy.model.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IDE-like assistance for Groovy scripts: completion with auto imports, signature help and diagnostics.
 * Works on broken code: the analysis repairs the script around errors.
 */
@Component
public class AutocompleteGroovyService {
    private static final Logger log = LoggerFactory.getLogger(AutocompleteGroovyService.class);
    private static final Pattern UNRESOLVED_CLASS = Pattern.compile("unable to resolve class ([\\w.$]+)");
    private static final Pattern PACKAGE_LIKE = Pattern.compile("[a-z_]\\w*(\\.[a-z_]\\w*)*");
    private static final int MEMBER_LIMIT = 500;
    /** luna-core classes are offered only after this many typed characters. */
    private static final int CORE_MIN_PREFIX = 2;

    private static final List<String> KEYWORDS = List.of(
            "def", "var", "if", "else", "for", "while", "do", "return", "new", "import", "true", "false", "null",
            "try", "catch", "finally", "throw", "switch", "case", "default", "break", "continue", "in", "instanceof",
            "as", "final", "static", "class", "interface", "enum", "void", "assert", "this", "super", "boolean",
            "int", "long", "double", "float", "char", "byte", "short"
    );

    private final ClassIndex index;
    private final GdkMethodIndex gdk;
    private final ScriptParser parser;
    private final MemberSuggestions members;

    public AutocompleteGroovyService(ClassIndex index, GdkMethodIndex gdk) {
        this.index = index;
        this.gdk = gdk;
        this.parser = new ScriptParser(index.getClassLoader());
        this.members = new MemberSuggestions(gdk);
    }

    // ------------------------------------------------------------------------------------------------- completion

    public AutocompleteResult getSuggestions(String code, int line, int column, int limit, Map<String, Class<?>> contextVariables) {
        String text = normalize(code);
        int offset = offset(text, line, column);
        AutocompleteResult result = new AutocompleteResult();
        try {
            CursorContext ctx = CursorContext.analyze(text, offset);
            ImportsInfo imports = ImportsInfo.parse(text);
            switch (ctx.type()) {
                case NONE -> {}
                case MEMBER -> suggestMembers(result, text, offset, ctx, imports, contextVariables);
                case IMPORT -> suggestImport(result, ctx, limit);
                case NEW, TYPE -> {
                    result.addAll(classes(ctx.prefix(), imports, limit, ctx.type() == CursorContext.Type.NEW));
                    result.setIncomplete(true);
                }
                case IDENTIFIER -> suggestIdentifiers(result, text, offset, ctx, imports, contextVariables, limit);
            }
        } catch (Exception e) {
            log.debug("Autocomplete failed", e);
        }
        return result;
    }

    private void suggestMembers(AutocompleteResult result, String text, int offset, CursorContext ctx,
                                ImportsInfo imports, Map<String, Class<?>> vars) {
        Analysis analysis = analyze(text, offset, ctx, imports, vars);
        ResolvedType receiver = analysis.receiver();
        if (receiver == null) {
            // java.util.| -> package content
            String receiverText = ctx.receiverText();
            if (PACKAGE_LIKE.matcher(receiverText).matches() && index.isPackage(receiverText)
                    && !analysis.variables().containsKey(receiverText.split("\\.")[0])) {
                result.addAll(packageContent(receiverText, ctx.prefix(), false));
            }
            return;
        }
        TextEdit importEdit = receiver.importFqn() != null ? imports.importEdit(receiver.importFqn()) : null;
        result.addAll(members.suggest(receiver, ctx.prefix(), importEdit, MEMBER_LIMIT));
    }

    private void suggestIdentifiers(AutocompleteResult result, String text, int offset, CursorContext ctx,
                                    ImportsInfo imports, Map<String, Class<?>> vars, int limit) {
        String prefix = ctx.prefix();
        Analysis analysis = analyze(text, offset, ctx, imports, vars);

        analysis.variables().forEach((name, type) -> {
            if (NameMatcher.match(name, prefix) >= 0) {
                result.add(Suggestion.builder(name, SuggestionKind.Variable)
                                   .labelDescription(type.display())
                                   .detail(type.display() + " " + name)
                                   .sortText("0_" + name)
                                   .build());
            }
        });
        for (MethodNode method : analysis.resolver().scriptMethods().values()) {
            if (NameMatcher.match(method.getName(), prefix) < 0) {
                continue;
            }
            StringJoiner params = new StringJoiner(", ");
            StringJoiner snippet = new StringJoiner(", ");
            int n = 1;
            for (org.codehaus.groovy.ast.Parameter p : method.getParameters()) {
                params.add(p.getType().getNameWithoutPackage() + " " + p.getName());
                snippet.add("${" + n++ + ":" + p.getName() + "}");
            }
            result.add(Suggestion.builder(method.getName(), SuggestionKind.Function)
                               .labelDetail("(" + params + ")")
                               .labelDescription(method.getReturnType().getNameWithoutPackage())
                               .snippet(method.getName() + "(" + snippet + ")")
                               .triggerSignature(method.getParameters().length > 0)
                               .sortText("0_" + method.getName())
                               .build());
        }
        if (!prefix.isEmpty()) {
            for (String keyword : KEYWORDS) {
                if (keyword.startsWith(prefix) && !keyword.equals(prefix)) {
                    result.add(Suggestion.builder(keyword, SuggestionKind.Keyword).sortText("1_" + keyword).build());
                }
            }
            if ("println".startsWith(prefix)) {
                result.add(Suggestion.builder("println", SuggestionKind.Function)
                                   .labelDetail("(Object value)")
                                   .snippet("println(${1})")
                                   .sortText("1_println")
                                   .build());
            }
        }
        // on an empty word (ctrl+space) only already imported classes: the whole index would be noise
        result.addAll(classes(prefix, imports, limit, false));
        result.setIncomplete(true);
    }

    private List<Suggestion> classes(String prefix, ImportsInfo imports, int limit, boolean constructor) {
        List<Suggestion> out = new ArrayList<>();
        Set<String> added = new HashSet<>();
        // explicitly imported classes (and aliases) first
        imports.explicitImports().forEach((alias, fqn) -> {
            if (NameMatcher.match(alias, prefix) >= 0 && added.add(fqn)) {
                ClassEntry entry = index.bySimpleName(fqn.substring(fqn.lastIndexOf('.') + 1)).stream()
                                        .filter(e -> e.fqn().equals(fqn)).findFirst()
                                        .orElse(new ClassEntry(fqn, alias, ImportsInfo.packageOf(fqn), ClassSource.API, ClassEntry.Kind.CLASS, false));
                out.add(classSuggestion(entry, alias, imports, constructor, "2_"));
            }
        });
        if (prefix.isEmpty()) {
            return out;
        }
        boolean withCore = prefix.length() >= CORE_MIN_PREFIX;
        int position = 0;
        // index.search() already orders by match quality, source priority (api first), beans, name length
        for (ClassEntry entry : index.search(prefix, limit, e -> withCore || e.source() != ClassSource.CORE)) {
            if (added.add(entry.fqn())) {
                out.add(classSuggestion(entry, null, imports, constructor, String.format("3_%04d_", position++)));
            }
        }
        return out;
    }

    private Suggestion classSuggestion(ClassEntry entry, String alias, ImportsInfo imports, boolean constructor, String sortPrefix) {
        boolean qualified = alias == null && imports.conflicts(entry.fqn());
        String name = alias != null ? alias : (qualified ? entry.fqn() : entry.simpleName());
        Suggestion.Builder b = Suggestion.builder(name, kindOf(entry))
                .label(alias != null ? alias : entry.simpleName())
                .labelDescription(entry.packageName())
                .detail(entry.fqn() + (entry.source() == ClassSource.CORE ? "  [luna-core]" : ""))
                .filterText(alias != null ? alias : entry.simpleName())
                .sortText(sortPrefix + entry.simpleName());
        if (constructor) {
            b.snippet(name + "($0)").triggerSignature(true);
        }
        if (!qualified && alias == null) {
            b.addEdit(imports.importEdit(entry.fqn()));
        }
        return b.build();
    }

    private void suggestImport(AutocompleteResult result, CursorContext ctx, int limit) {
        String qualifier = ctx.receiverText();
        String prefix = ctx.prefix();
        result.setIncomplete(true);
        if (qualifier.isEmpty()) {
            for (String pkg : index.subPackages("")) {
                if (pkg.startsWith(prefix)) {
                    result.add(Suggestion.builder(pkg, SuggestionKind.Module).sortText("0_" + pkg).build());
                }
            }
            if (!prefix.isEmpty()) {
                boolean withCore = prefix.length() >= CORE_MIN_PREFIX;
                for (ClassEntry entry : index.search(prefix, limit, e -> withCore || e.source() != ClassSource.CORE)) {
                    result.add(Suggestion.builder(entry.fqn(), kindOf(entry))
                                       .label(entry.simpleName())
                                       .labelDescription(entry.packageName())
                                       .filterText(entry.simpleName())
                                       .sortText((1 + entry.source().rank()) + "_" + entry.simpleName())
                                       .build());
                }
            }
            return;
        }
        if (ctx.importStatic()) {
            Class<?> cls = index.load(qualifier);
            if (cls != null) {
                Set<String> seen = new HashSet<>();
                for (Method m : cls.getMethods()) {
                    if (Modifier.isStatic(m.getModifiers()) && NameMatcher.match(m.getName(), prefix) >= 0 && seen.add(m.getName())) {
                        result.add(Suggestion.builder(m.getName(), SuggestionKind.Method).sortText("0_" + m.getName()).build());
                    }
                }
                for (Field f : cls.getFields()) {
                    if (Modifier.isStatic(f.getModifiers()) && NameMatcher.match(f.getName(), prefix) >= 0) {
                        result.add(Suggestion.builder(f.getName(), SuggestionKind.Constant).sortText("0_" + f.getName()).build());
                    }
                }
                result.add(Suggestion.builder("*", SuggestionKind.Keyword).sortText("1_*").build());
                return;
            }
        }
        result.addAll(packageContent(qualifier, prefix, true));
        if (index.isPackage(qualifier)) {
            result.add(Suggestion.builder("*", SuggestionKind.Keyword).sortText("9_*").build());
        }
    }

    private List<Suggestion> packageContent(String pkg, String prefix, boolean includeCore) {
        List<Suggestion> out = new ArrayList<>();
        for (String sub : index.subPackages(pkg)) {
            String name = sub.substring(pkg.length() + 1);
            if (NameMatcher.match(name, prefix) >= 0) {
                out.add(Suggestion.builder(name, SuggestionKind.Module).detail(sub).sortText("1_" + name).build());
            }
        }
        for (ClassEntry entry : index.inPackage(pkg)) {
            if ((includeCore || entry.source() != ClassSource.CORE) && NameMatcher.match(entry.simpleName(), prefix) >= 0) {
                out.add(Suggestion.builder(entry.simpleName(), kindOf(entry))
                                  .detail(entry.fqn())
                                  .sortText("0_" + entry.simpleName())
                                  .build());
            }
        }
        return out;
    }

    private static SuggestionKind kindOf(ClassEntry entry) {
        return switch (entry.kind()) {
            case INTERFACE, ANNOTATION -> SuggestionKind.Interface;
            case ENUM -> SuggestionKind.Enum;
            case RECORD -> SuggestionKind.Struct;
            case CLASS -> SuggestionKind.Class;
        };
    }

    // --------------------------------------------------------------------------------------------------- analysis

    private record Analysis(Map<String, JType> variables, ResolvedType receiver, TypeResolver resolver) {}

    /**
     * Replaces the word at the cursor with a placeholder, parses the (repaired) script and finds the variables visible
     * at the cursor and, for member access, the type of the receiver.
     */
    private Analysis analyze(String text, int offset, CursorContext ctx, ImportsInfo imports, Map<String, Class<?>> vars) {
        String work = text.substring(0, ctx.prefixStart()) + ScriptParser.PLACEHOLDER + text.substring(offset);
        int[] pos = lineColumn(text, ctx.prefixStart());

        TypeResolver resolver = new TypeResolver(index, gdk, imports);
        Map<String, JType> globals = new LinkedHashMap<>();
        vars.forEach((name, cls) -> globals.put(name, JType.of(cls)));

        ScriptParser.ParseResult parsed = parser.parseTolerant(work, vars, pos[0]);
        ScopeCollector collector = ScopeCollector.collect(parsed.module(), resolver, globals, pos[0], pos[1]);
        Map<String, JType> variables = collector.visibleVariables();
        TypeResolver.Scope scope = variables::get;

        ResolvedType receiver = null;
        if (ctx.type() == CursorContext.Type.MEMBER) {
            ScopeCollector.Capture capture = collector.capture();
            boolean spread = ctx.spread();
            if (capture != null && capture.receiver() != null) {
                receiver = resolver.resolve(capture.receiver(), scope);
                spread = capture.spread();
            }
            if (receiver == null) {
                receiver = resolveStandalone(ctx.receiverText(), imports, vars, resolver, scope);
            }
            if (receiver != null && spread) {
                receiver = new ResolvedType(receiver.type().elementType(), false, null);
            }
        }
        return new Analysis(variables, receiver, resolver);
    }

    /** Fallback when the cursor line is too broken: parse the receiver expression alone (with the script imports). */
    private ResolvedType resolveStandalone(String receiverText, ImportsInfo imports, Map<String, Class<?>> vars,
                                           TypeResolver resolver, TypeResolver.Scope scope) {
        ScriptParser.ParseResult parsed = parser.parse(imports.importBlock() + receiverText, vars);
        if (parsed.module() == null) {
            return null;
        }
        BlockStatement block = parsed.module().getStatementBlock();
        if (block == null || block.getStatements().isEmpty()) {
            return null;
        }
        Statement last = block.getStatements().getLast();
        if (last instanceof ExpressionStatement es) {
            Expression expression = es.getExpression();
            return resolver.resolve(expression, scope);
        }
        return null;
    }

    // --------------------------------------------------------------------------------------------- signature help

    public SignatureHelp getSignatureHelp(String code, int line, int column, Map<String, Class<?>> contextVariables) {
        SignatureHelp help = new SignatureHelp();
        String text = normalize(code);
        int offset = offset(text, line, column);
        try {
            CursorContext.CallSite site = CursorContext.findCallSite(text, offset);
            if (site == null) {
                return help;
            }
            help.setActiveParameter(site.activeParameter());
            ImportsInfo imports = ImportsInfo.parse(text);

            if (site.constructor()) {
                Class<?> cls = classForName(site.name(), imports);
                if (cls != null) {
                    for (Constructor<?> c : cls.getConstructors()) {
                        addSignature(help, JType.simpleName(cls), null, c.getGenericParameterTypes(),
                                     MemberSuggestions.parameterNames(c), 0, JType.of(cls));
                    }
                }
            } else {
                CursorContext ctx = CursorContext.analyze(text, site.nameEnd());
                if (ctx.type() == CursorContext.Type.MEMBER) {
                    ResolvedType receiver = analyze(text, site.nameEnd(), ctx, imports, contextVariables).receiver();
                    if (receiver != null) {
                        JType type = receiver.type();
                        for (Method m : TypeResolver.methods(type.memberClass(), site.name(), receiver.staticRef())) {
                            JType owner = type.asSuper(m.getDeclaringClass());
                            String ret = JType.displayGeneric(m.getGenericReturnType(), owner == null ? Map.of() : owner.bindings());
                            addSignature(help, site.name(), ret, m.getGenericParameterTypes(), MemberSuggestions.parameterNames(m), 0, owner);
                        }
                        if (!receiver.staticRef() && help.getSignatures().isEmpty()) {
                            for (Method m : gdk.forReceiver(type.memberClass(), site.name())) {
                                addSignature(help, site.name(), JType.simpleName(m.getReturnType()),
                                             m.getGenericParameterTypes(), MemberSuggestions.parameterNames(m), 1, null);
                            }
                        }
                    }
                } else if (ctx.type() == CursorContext.Type.IDENTIFIER) {
                    MethodNode method = analyze(text, site.nameEnd(), ctx, imports, contextVariables).resolver().scriptMethods().get(site.name());
                    if (method != null) {
                        List<String> labels = new ArrayList<>();
                        for (org.codehaus.groovy.ast.Parameter p : method.getParameters()) {
                            labels.add(p.getType().getNameWithoutPackage() + " " + p.getName());
                        }
                        SignatureHelp.SignatureInfo info = new SignatureHelp.SignatureInfo(
                                method.getReturnType().getNameWithoutPackage() + " " + site.name() + "(" + String.join(", ", labels) + ")");
                        labels.forEach(info::addParameter);
                        help.addSignature(info);
                    }
                }
            }
            help.setActiveSignature(bestSignature(help, site.activeParameter()));
        } catch (Exception e) {
            log.debug("Signature help failed", e);
        }
        return help;
    }

    private static void addSignature(SignatureHelp help, String name, String returnType, Type[] params,
                                     List<String> names, int skip, JType owner) {
        Map<java.lang.reflect.TypeVariable<?>, JType> bindings = owner == null ? Map.of() : owner.bindings();
        List<String> labels = new ArrayList<>();
        for (int i = skip; i < params.length; i++) {
            labels.add(JType.displayGeneric(params[i], bindings) + " " + names.get(i));
        }
        String label = (returnType != null ? returnType + " " : "") + name + "(" + String.join(", ", labels) + ")";
        for (SignatureHelp.SignatureInfo existing : help.getSignatures()) {
            if (existing.getLabel().equals(label)) {
                return;
            }
        }
        SignatureHelp.SignatureInfo info = new SignatureHelp.SignatureInfo(label);
        labels.forEach(info::addParameter);
        help.addSignature(info);
    }

    private static int bestSignature(SignatureHelp help, int activeParameter) {
        List<SignatureHelp.SignatureInfo> signatures = help.getSignatures();
        int best = 0;
        int bestCount = Integer.MAX_VALUE;
        for (int i = 0; i < signatures.size(); i++) {
            int count = signatures.get(i).getParameters() == null ? 0 : signatures.get(i).getParameters().size();
            if (count > activeParameter && count < bestCount) {
                best = i;
                bestCount = count;
            }
        }
        return best;
    }

    private Class<?> classForName(String name, ImportsInfo imports) {
        if (name.contains(".")) {
            return index.load(name);
        }
        String explicit = imports.explicitImports().get(name);
        if (explicit != null) {
            return index.load(explicit);
        }
        List<String> packages = new ArrayList<>(ImportsInfo.DEFAULT_PACKAGES);
        packages.addAll(imports.starPackages());
        for (String pkg : packages) {
            Class<?> c = index.load(pkg + "." + name);
            if (c != null) {
                return c;
            }
        }
        List<ClassEntry> entries = index.bySimpleName(name);
        return entries.isEmpty() ? null : index.load(entries.getFirst().fqn());
    }

    // ------------------------------------------------------------------------------------------------ diagnostics

    public List<Diagnostic> getDiagnostics(String code, Map<String, Class<?>> contextVariables) {
        String text = normalize(code);
        List<Diagnostic> out = new ArrayList<>();
        if (text.isBlank()) {
            return out;
        }
        ImportsInfo imports = ImportsInfo.parse(text);
        String[] lines = text.split("\n", -1);
        ScriptParser.ParseResult parsed = parser.parse(text, contextVariables);
        for (ScriptParser.Problem problem : parsed.problems()) {
            int line = Math.min(problem.line(), lines.length);
            int endLine = Math.min(problem.endLine(), lines.length);
            int endColumn = problem.endColumn();
            if (endLine == line && endColumn <= problem.column() + 1) {
                endColumn = wordEnd(lines[line - 1], problem.column());
            }
            Diagnostic diagnostic = new Diagnostic(new AutocompleteRange(line, problem.column(), endLine, endColumn),
                                                   problem.message(), Diagnostic.SEVERITY_ERROR);
            Matcher m = UNRESOLVED_CLASS.matcher(problem.message());
            if (m.find()) {
                String name = m.group(1);
                String simple = name.substring(name.lastIndexOf('.') + 1);
                AutocompleteRange nameRange = findWord(lines, line, endLine, name);
                if (nameRange != null) {
                    diagnostic = new Diagnostic(nameRange, problem.message(), Diagnostic.SEVERITY_ERROR);
                }
                boolean first = true;
                for (ClassEntry entry : index.bySimpleName(simple).stream().limit(6).toList()) {
                    TextEdit edit = imports.importEdit(entry.fqn());
                    if (edit != null) {
                        diagnostic.addFix(new Diagnostic.QuickFix("Import " + entry.fqn(), List.of(edit), first));
                        first = false;
                    }
                }
            }
            out.add(diagnostic);
        }
        return out;
    }

    /** Range of a whole-word occurrence of {@code word} between the given lines. */
    private static AutocompleteRange findWord(String[] lines, int fromLine, int toLine, String word) {
        Pattern p = Pattern.compile("(?<![\\w.$])" + Pattern.quote(word) + "(?![\\w$])");
        for (int l = fromLine; l <= toLine && l <= lines.length; l++) {
            Matcher m = p.matcher(lines[l - 1]);
            if (m.find()) {
                return new AutocompleteRange(l, m.start() + 1, l, m.end() + 1);
            }
        }
        return null;
    }

    private static int wordEnd(String line, int column) {
        int i = Math.max(column - 1, 0);
        while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i))) {
            i++;
        }
        return Math.max(i + 1, column + 1);
    }

    // ------------------------------------------------------------------------------------------------------- utils

    private static String normalize(String code) {
        return code == null ? "" : code.replace("\r\n", "\n").replace('\r', '\n');
    }

    /** Offset of a 1-based Monaco position. */
    static int offset(String text, int line, int column) {
        int offset = 0;
        for (int l = 1; l < line; l++) {
            int nl = text.indexOf('\n', offset);
            if (nl < 0) {
                return text.length();
            }
            offset = nl + 1;
        }
        int lineEnd = text.indexOf('\n', offset);
        if (lineEnd < 0) {
            lineEnd = text.length();
        }
        return Math.min(offset + Math.max(column - 1, 0), lineEnd);
    }

    /** 1-based line and column of an offset. */
    static int[] lineColumn(String text, int offset) {
        int line = 1;
        int lineStart = 0;
        for (int i = 0; i < offset; i++) {
            if (text.charAt(i) == '\n') {
                line++;
                lineStart = i + 1;
            }
        }
        return new int[]{line, offset - lineStart + 1};
    }
}
