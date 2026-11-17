package ru.slie.luna.plugins.gravity.script.groovy.analysis;

import ru.slie.luna.plugins.gravity.script.groovy.analysis.GroovyLexer.Kind;
import ru.slie.luna.plugins.gravity.script.groovy.analysis.GroovyLexer.Token;

import java.util.List;
import java.util.Set;

/**
 * What the user is completing at the cursor, found lexically (works for code that does not compile).
 */
public final class CursorContext {
    public enum Type {
        /** Inside a comment/string or right after a declared name: nothing to suggest. */
        NONE,
        /** Plain identifier: variables, classes, keywords. */
        IDENTIFIER,
        /** After {@code receiver.} */
        MEMBER,
        /** Inside an import statement. */
        IMPORT,
        /** After {@code new}. */
        NEW,
        /** Type position: after {@code @}, {@code instanceof}, {@code as}, in generics. */
        TYPE
    }

    /** Keywords after which an expression (and so an identifier completion) may follow. */
    private static final Set<String> EXPRESSION_KEYWORDS = Set.of(
            "return", "in", "else", "case", "throw", "assert", "yield", "do", "try", "finally", "default"
    );
    private static final Set<String> TYPE_KEYWORDS = Set.of("instanceof", "as", "extends", "implements", "throws");

    private final Type type;
    private final String prefix;
    private final int prefixStart;
    private final String receiverText;
    /** IMPORT: {@code import static}; MEMBER: spread access {@code list*.} */
    private final boolean flag;

    private CursorContext(Type type, String prefix, int prefixStart, String receiverText, boolean flag) {
        this.type = type;
        this.prefix = prefix;
        this.prefixStart = prefixStart;
        this.receiverText = receiverText;
        this.flag = flag;
    }

    public Type type() {
        return type;
    }

    /** Identifier part already typed before the cursor. */
    public String prefix() {
        return prefix;
    }

    public int prefixStart() {
        return prefixStart;
    }

    /** For MEMBER: source of the receiver expression; for IMPORT: qualified name typed so far (without trailing dot). */
    public String receiverText() {
        return receiverText;
    }

    public boolean importStatic() {
        return type == Type.IMPORT && flag;
    }

    public boolean spread() {
        return type == Type.MEMBER && flag;
    }

    public static CursorContext analyze(String text, int offset) {
        String before = text.substring(0, offset);
        GroovyLexer.Result lex = GroovyLexer.lex(before);
        List<Token> tokens = lex.tokens();
        if (lex.endsInComment() || lex.endsInString()) {
            return none(offset);
        }

        int idx = tokens.size();
        String prefix = "";
        int prefixStart = offset;
        if (!tokens.isEmpty()) {
            Token last = tokens.getLast();
            if (last.is(Kind.IDENT) && last.end() == offset) {
                prefix = last.text();
                prefixStart = last.start();
                idx = tokens.size() - 1;
            } else if (last.is(Kind.NUMBER) && last.end() == offset) {
                return none(offset);
            }
        }

        // import statement
        int stmtStart = idx;
        while (stmtStart > 0 && !tokens.get(stmtStart - 1).is(Kind.NEWLINE) && !tokens.get(stmtStart - 1).is(Kind.SEMI)) {
            stmtStart--;
        }
        if (stmtStart < idx && tokens.get(stmtStart).isIdent("import")) {
            int i = stmtStart + 1;
            boolean isStatic = false;
            if (i < idx && tokens.get(i).isIdent("static")) {
                isStatic = true;
                i++;
            }
            StringBuilder qualifier = new StringBuilder();
            for (; i < idx; i++) {
                qualifier.append(tokens.get(i).text());
            }
            String q = qualifier.toString();
            if (q.endsWith(".")) {
                q = q.substring(0, q.length() - 1);
            } else if (!q.isEmpty()) {
                return none(offset); // e.g. "import foo.Bar as X|"
            }
            return new CursorContext(Type.IMPORT, prefix, prefixStart, q, isStatic);
        }

        int prevIdx = idx - 1;
        while (prevIdx >= 0 && tokens.get(prevIdx).is(Kind.NEWLINE)) {
            // "foo\n  .bar" is a valid continuation in Groovy, but an identifier on a new line starts a new statement
            if (prevIdx - 1 >= 0 && tokens.get(prevIdx - 1).isDotLike()) {
                prevIdx--;
                continue;
            }
            break;
        }
        Token prev = prevIdx >= 0 ? tokens.get(prevIdx) : null;

        if (prev != null && (prev.isDotLike() || prev.is(Kind.METHOD_REF))) {
            int receiverStart = findReceiverStart(tokens, prevIdx - 1);
            if (receiverStart < 0) {
                return none(offset);
            }
            String receiver = before.substring(tokens.get(receiverStart).start(), prev.start()).trim();
            if (receiver.isEmpty()) {
                return none(offset);
            }
            return new CursorContext(Type.MEMBER, prefix, prefixStart, receiver, prev.is(Kind.SPREAD_DOT));
        }

        if (prev != null && prev.is(Kind.IDENT)) {
            if (prev.text().equals("new")) {
                return new CursorContext(Type.NEW, prefix, prefixStart, null, false);
            }
            if (TYPE_KEYWORDS.contains(prev.text())) {
                return new CursorContext(Type.TYPE, prefix, prefixStart, null, false);
            }
            if (!EXPRESSION_KEYWORDS.contains(prev.text())) {
                // "Issue iss|" or "def x|": the user is naming a variable
                return none(offset);
            }
        }
        if (prev != null && (prev.is(Kind.NUMBER) || prev.is(Kind.STRING) || prev.is(Kind.RBRACKET))) {
            return none(offset);
        }
        if (prev != null && prev.is(Kind.AT)) {
            return new CursorContext(Type.TYPE, prefix, prefixStart, null, false);
        }

        return new CursorContext(Type.IDENTIFIER, prefix, prefixStart, null, false);
    }

    /**
     * Walks backwards over a member access chain like {@code foo.bar(1, 2)[0].baz { }} and returns
     * the index of its first token, or -1.
     */
    static int findReceiverStart(List<Token> tokens, int end) {
        int i = end;
        while (i >= 0 && tokens.get(i).is(Kind.NEWLINE)) {
            i--;
        }
        if (i < 0) {
            return -1;
        }
        int start = -1;
        while (i >= 0) {
            Token t = tokens.get(i);
            boolean consumed = false;
            // call/index/closure suffixes
            while (i >= 0 && (tokens.get(i).is(Kind.RPAREN) || tokens.get(i).is(Kind.RBRACKET) || tokens.get(i).is(Kind.RBRACE))) {
                int open = matchOpen(tokens, i);
                if (open < 0) {
                    return -1;
                }
                start = open;
                i = open - 1;
                consumed = true;
            }
            if (i >= 0) {
                t = tokens.get(i);
            }
            if (i >= 0 && (t.is(Kind.IDENT) || t.is(Kind.STRING) || t.is(Kind.NUMBER))) {
                if (t.is(Kind.IDENT) && isStatementKeyword(t.text())) {
                    break;
                }
                start = i;
                i--;
                consumed = true;
            } else if (!consumed) {
                return start;
            }
            // continue the chain only if there is a dot before
            int j = i;
            while (j >= 0 && tokens.get(j).is(Kind.NEWLINE)) {
                j--;
            }
            if (j >= 0 && tokens.get(j).isDotLike()) {
                i = j - 1;
                continue;
            }
            if (j >= 0 && tokens.get(j).isIdent("new") && start >= 0) {
                start = j;
            }
            break;
        }
        return start;
    }

    private static boolean isStatementKeyword(String word) {
        return switch (word) {
            case "return", "if", "else", "for", "while", "def", "var", "final", "in", "case", "throw", "assert" -> true;
            default -> false;
        };
    }

    private static int matchOpen(List<Token> tokens, int closeIdx) {
        int depth = 0;
        for (int i = closeIdx; i >= 0; i--) {
            Kind k = tokens.get(i).kind();
            if (k == Kind.RPAREN || k == Kind.RBRACKET || k == Kind.RBRACE) {
                depth++;
            } else if (k == Kind.LPAREN || k == Kind.LBRACKET || k == Kind.LBRACE) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static CursorContext none(int offset) {
        return new CursorContext(Type.NONE, "", offset, null, false);
    }

    /** Call site around the cursor for signature help. */
    public record CallSite(int nameStart, int nameEnd, String name, boolean constructor, int activeParameter) {}

    public static CallSite findCallSite(String text, int offset) {
        GroovyLexer.Result lex = GroovyLexer.lex(text.substring(0, offset));
        if (lex.endsInComment() || lex.endsInString()) {
            return null;
        }
        List<Token> tokens = lex.tokens();
        int depth = 0;
        int commas = 0;
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token t = tokens.get(i);
            switch (t.kind()) {
                case RPAREN, RBRACKET, RBRACE -> depth++;
                case LBRACKET, LBRACE -> {
                    if (depth == 0) {
                        return null;
                    }
                    depth--;
                }
                case COMMA -> {
                    if (depth == 0) {
                        commas++;
                    }
                }
                case LPAREN -> {
                    if (depth > 0) {
                        depth--;
                        continue;
                    }
                    if (i == 0 || !tokens.get(i - 1).is(Kind.IDENT)) {
                        return null;
                    }
                    Token name = tokens.get(i - 1);
                    boolean ctor = false;
                    int k = i - 2;
                    // new java.util.ArrayList(
                    while (k >= 1 && tokens.get(k).is(Kind.DOT) && tokens.get(k - 1).is(Kind.IDENT)) {
                        k -= 2;
                    }
                    if (k >= 0 && tokens.get(k).isIdent("new")) {
                        ctor = true;
                    }
                    return new CallSite(name.start(), name.end(), name.text(), ctor, commas);
                }
                default -> {}
            }
        }
        return null;
    }
}
