package ru.slie.luna.plugins.gravity.script.groovy.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal, error tolerant Groovy tokenizer. It never fails: it is used to understand the code around
 * the cursor even when the script does not compile.
 */
public final class GroovyLexer {
    public enum Kind {
        IDENT, NUMBER, STRING,
        DOT, SAFE_DOT, SPREAD_DOT, METHOD_REF,
        LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE,
        COMMA, SEMI, NEWLINE, AT, OP
    }

    public record Token(Kind kind, String text, int start, int end) {
        public boolean is(Kind k) {
            return kind == k;
        }

        public boolean isIdent(String value) {
            return kind == Kind.IDENT && text.equals(value);
        }

        public boolean isDotLike() {
            return kind == Kind.DOT || kind == Kind.SAFE_DOT || kind == Kind.SPREAD_DOT;
        }
    }

    public record Result(List<Token> tokens, boolean endsInComment, boolean endsInString) {}

    private GroovyLexer() {}

    public static Result lex(String s) {
        List<Token> tokens = new ArrayList<>();
        int n = s.length();
        int i = 0;
        boolean inComment = false;
        boolean inString = false;

        while (i < n) {
            char c = s.charAt(i);
            if (c == '\n') {
                tokens.add(new Token(Kind.NEWLINE, "\n", i, i + 1));
                i++;
            } else if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '/' && i + 1 < n && s.charAt(i + 1) == '/') {
                int end = s.indexOf('\n', i);
                if (end < 0) {
                    inComment = true;
                    end = n;
                }
                i = end;
            } else if (c == '/' && i + 1 < n && s.charAt(i + 1) == '*') {
                int end = s.indexOf("*/", i + 2);
                if (end < 0) {
                    inComment = true;
                    i = n;
                } else {
                    i = end + 2;
                }
            } else if (c == '"' || c == '\'') {
                int start = i;
                String triple = String.valueOf(c).repeat(3);
                boolean closed = false;
                if (s.startsWith(triple, i)) {
                    int end = i + 3;
                    while (end < n) {
                        if (s.charAt(end) == '\\') {
                            end += 2;
                            continue;
                        }
                        if (s.startsWith(triple, end)) {
                            closed = true;
                            end += 3;
                            break;
                        }
                        end++;
                    }
                    i = Math.min(end, n);
                } else {
                    int end = i + 1;
                    boolean newline = false;
                    while (end < n) {
                        char ch = s.charAt(end);
                        if (ch == '\\') {
                            end += 2;
                            continue;
                        }
                        if (ch == c) {
                            closed = true;
                            end++;
                            break;
                        }
                        if (ch == '\n') {
                            newline = true;
                            break;
                        }
                        end++;
                    }
                    if (newline) {
                        closed = true; // broken literal, stop at the end of the line
                    }
                    i = Math.min(end, n);
                }
                if (!closed) {
                    inString = true;
                }
                tokens.add(new Token(Kind.STRING, s.substring(start, i), start, i));
            } else if (Character.isJavaIdentifierStart(c)) {
                int start = i;
                while (i < n && Character.isJavaIdentifierPart(s.charAt(i))) {
                    i++;
                }
                tokens.add(new Token(Kind.IDENT, s.substring(start, i), start, i));
            } else if (Character.isDigit(c)) {
                int start = i;
                while (i < n && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_'
                        || (s.charAt(i) == '.' && i + 1 < n && Character.isDigit(s.charAt(i + 1))))) {
                    i++;
                }
                tokens.add(new Token(Kind.NUMBER, s.substring(start, i), start, i));
            } else {
                int start = i;
                Kind kind;
                int len = 1;
                switch (c) {
                    case '(' -> kind = Kind.LPAREN;
                    case ')' -> kind = Kind.RPAREN;
                    case '[' -> kind = Kind.LBRACKET;
                    case ']' -> kind = Kind.RBRACKET;
                    case '{' -> kind = Kind.LBRACE;
                    case '}' -> kind = Kind.RBRACE;
                    case ',' -> kind = Kind.COMMA;
                    case ';' -> kind = Kind.SEMI;
                    case '@' -> kind = Kind.AT;
                    case '.' -> {
                        if (s.startsWith(".&", i)) {
                            kind = Kind.METHOD_REF;
                            len = 2;
                        } else if (s.startsWith("..", i)) {
                            kind = Kind.OP;
                            len = s.startsWith("...", i) ? 3 : (s.startsWith("..<", i) ? 3 : 2);
                        } else if (s.startsWith(".@", i)) {
                            kind = Kind.DOT;
                            len = 2;
                        } else {
                            kind = Kind.DOT;
                        }
                    }
                    case '?' -> {
                        if (s.startsWith("?.", i)) {
                            kind = Kind.SAFE_DOT;
                            len = 2;
                        } else {
                            kind = Kind.OP;
                        }
                    }
                    case '*' -> {
                        if (s.startsWith("*.", i)) {
                            kind = Kind.SPREAD_DOT;
                            len = 2;
                        } else {
                            kind = Kind.OP;
                        }
                    }
                    case ':' -> {
                        if (s.startsWith("::", i)) {
                            kind = Kind.METHOD_REF;
                            len = 2;
                        } else {
                            kind = Kind.OP;
                        }
                    }
                    default -> kind = Kind.OP;
                }
                i += len;
                tokens.add(new Token(kind, s.substring(start, i), start, i));
            }
        }

        return new Result(tokens, inComment, inString);
    }

    /** Closing brackets required to balance the given text, in the order they should be appended. */
    public static String missingClosers(List<Token> tokens) {
        StringBuilder stack = new StringBuilder();
        for (Token t : tokens) {
            switch (t.kind()) {
                case LPAREN -> stack.append(')');
                case LBRACKET -> stack.append(']');
                case LBRACE -> stack.append('}');
                case RPAREN, RBRACKET, RBRACE -> {
                    int idx = stack.lastIndexOf(t.text());
                    if (idx >= 0) {
                        stack.setLength(idx);
                    }
                }
                default -> {}
            }
        }
        return stack.reverse().toString();
    }
}
