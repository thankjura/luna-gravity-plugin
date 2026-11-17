package ru.slie.luna.plugins.gravity.script.groovy.index;

/**
 * IDE-like name matching: prefix, case insensitive prefix and camel humps ({@code IM -> IssueManager},
 * {@code getIM -> getIssueManager}).
 */
public final class NameMatcher {
    public static final int NO_MATCH = -1;

    private NameMatcher() {}

    /** @return 0 exact prefix, 1 case-insensitive prefix, 2 camel humps, -1 no match */
    public static int match(String name, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return 0;
        }
        if (name.startsWith(pattern)) {
            return 0;
        }
        if (name.regionMatches(true, 0, pattern, 0, pattern.length())) {
            return 1;
        }
        if (!eq(name.charAt(0), pattern.charAt(0))) {
            return NO_MATCH;
        }
        return humps(name, 1, pattern, 1, 0) ? 2 : NO_MATCH;
    }

    private static boolean humps(String name, int ni, String p, int pi, int depth) {
        if (pi == p.length()) {
            return true;
        }
        if (ni >= name.length() || depth > 64) {
            return false;
        }
        char pc = p.charAt(pi);
        if (eq(name.charAt(ni), pc) && humps(name, ni + 1, p, pi + 1, depth + 1)) {
            return true;
        }
        for (int k = ni + 1; k < name.length(); k++) {
            if (isHumpStart(name, k) && eq(name.charAt(k), pc) && humps(name, k + 1, p, pi + 1, depth + 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHumpStart(String name, int k) {
        char c = name.charAt(k);
        char prev = name.charAt(k - 1);
        return Character.isUpperCase(c) && !Character.isUpperCase(prev)
                || prev == '_'
                || Character.isDigit(c) && !Character.isDigit(prev)
                || Character.isUpperCase(c) && k + 1 < name.length() && Character.isLowerCase(name.charAt(k + 1));
    }

    private static boolean eq(char a, char b) {
        return Character.toLowerCase(a) == Character.toLowerCase(b);
    }
}
