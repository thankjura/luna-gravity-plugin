package ru.slie.luna.plugins.gravity.script.groovy.analysis;

import ru.slie.luna.plugins.gravity.script.groovy.model.AutocompleteRange;
import ru.slie.luna.plugins.gravity.script.groovy.model.TextEdit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Import statements of a script (parsed lexically) and the logic of adding a new one.
 */
public final class ImportsInfo {
    private static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(static\\s+)?([\\w$.]+?)(\\.\\*)?\\s*(?:as\\s+(\\w+))?\\s*(;)?\\s*$");
    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;?\\s*$");

    /** Packages Groovy imports by default. */
    public static final Set<String> DEFAULT_PACKAGES = Set.of(
            "java.lang", "java.util", "java.io", "java.net", "groovy.lang", "groovy.util"
    );
    private static final Set<String> DEFAULT_CLASSES = Set.of("java.math.BigDecimal", "java.math.BigInteger");

    private final Map<String, String> explicit = new LinkedHashMap<>(); // simple name (or alias) -> fqn
    private final Set<String> starPackages = new HashSet<>();
    private final List<String> lines;
    private String packageName = "";
    private int lastImportLine = 0; // 1-based
    private int packageLine = 0;
    private boolean semicolons = false;

    private ImportsInfo(String text) {
        this.lines = Arrays.asList(text.split("\n", -1));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher pm = PACKAGE.matcher(line);
            if (pm.matches()) {
                packageName = pm.group(1);
                packageLine = i + 1;
                continue;
            }
            Matcher m = IMPORT.matcher(line);
            if (!m.matches()) {
                continue;
            }
            lastImportLine = i + 1;
            semicolons = m.group(5) != null;
            if (m.group(1) != null) {
                continue; // static imports don't import types
            }
            String name = m.group(2);
            if (m.group(3) != null) {
                starPackages.add(name);
            } else {
                String alias = m.group(4) != null ? m.group(4) : name.substring(name.lastIndexOf('.') + 1);
                explicit.put(alias, name);
            }
        }
    }

    public static ImportsInfo parse(String text) {
        return new ImportsInfo(text);
    }

    public Map<String, String> explicitImports() {
        return explicit;
    }

    public Set<String> starPackages() {
        return starPackages;
    }

    /** Import lines only, used to give a standalone expression the same class resolution as the script. */
    public String importBlock() {
        StringBuilder sb = new StringBuilder();
        explicit.forEach((alias, fqn) -> {
            sb.append("import ").append(fqn);
            if (!fqn.endsWith("." + alias)) {
                sb.append(" as ").append(alias);
            }
            sb.append('\n');
        });
        starPackages.forEach(p -> sb.append("import ").append(p).append(".*\n"));
        return sb.toString();
    }

    public boolean isVisible(String fqn) {
        String pkg = packageOf(fqn);
        String simple = fqn.substring(fqn.lastIndexOf('.') + 1);
        return fqn.equals(explicit.get(simple))
                || DEFAULT_PACKAGES.contains(pkg)
                || DEFAULT_CLASSES.contains(fqn)
                || starPackages.contains(pkg)
                || pkg.equals(packageName);
    }

    /** Another class is already imported under this simple name, so {@code fqn} has to be written fully qualified. */
    public boolean conflicts(String fqn) {
        String simple = fqn.substring(fqn.lastIndexOf('.') + 1);
        String existing = explicit.get(simple);
        return existing != null && !existing.equals(fqn);
    }

    /** Edit that adds {@code import fqn} at the right place, or null when not needed. */
    public TextEdit importEdit(String fqn) {
        if (isVisible(fqn) || conflicts(fqn)) {
            return null;
        }
        String statement = "import " + fqn + (semicolons ? ";" : "");
        if (lastImportLine > 0) {
            return insertAt(lastImportLine + 1, statement + "\n");
        }
        if (packageLine > 0) {
            return insertAt(packageLine + 1, "\n" + statement + "\n");
        }
        boolean firstLineEmpty = lines.isEmpty() || lines.getFirst().isBlank();
        return insertAt(1, statement + (firstLineEmpty ? "\n" : "\n\n"));
    }

    private TextEdit insertAt(int line, String text) {
        if (line > lines.size()) {
            // after the last line: append to its end
            int last = lines.size();
            int col = lines.get(last - 1).length() + 1;
            return new TextEdit(new AutocompleteRange(last, col, last, col), "\n" + text.stripTrailing());
        }
        return new TextEdit(new AutocompleteRange(line, 1, line, 1), text);
    }

    public static String packageOf(String fqn) {
        int idx = fqn.lastIndexOf('.');
        return idx < 0 ? "" : fqn.substring(0, idx);
    }
}
