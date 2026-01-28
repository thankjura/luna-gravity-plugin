package ru.slie.luna.plugins.gravity.script.groovy;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GroovyAutocompleteService implements InitializingBean {
    private final Map<String, String> classCache = new ConcurrentHashMap<>();

    private final GenericApplicationContext context;
    public GroovyAutocompleteService(GenericApplicationContext context) {
        this.context = context;
    }

    public void afterPropertiesSet() {
        try (ScanResult scanResult = new ClassGraph()
                                             .enableClassInfo()
                                             .acceptPackages("ru.slie.*")
                                             .scan()) {

            scanResult.getAllClasses().forEach(cls -> {
                classCache.put(cls.getSimpleName(), cls.getName());
            });
        }
    }

    private Class<?> findClassGlobally(String className) {
        String fullName = classCache.get(className);
        if (fullName != null) {
            try {
                return Class.forName(fullName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    public AutocompleteResult getSuggestions(String fullText, int position, int limit) {
        String textBefore = fullText.substring(0, position);
        String lastWord = getLastWord(textBefore);

        int from = position - (lastWord.contains(".")
                                  ? lastWord.length() - lastWord.lastIndexOf(".") - 1
                                  : lastWord.length());

        List<Suggestion> suggestions = new ArrayList<>();

        String currentLine = getCurrentLine(textBefore);
        if (currentLine.trim().startsWith("import ")) {
            // import ...
            suggestions = getPackageSuggestions(lastWord);
        } else if (lastWord.contains(".")) {
            // issueManager.
            String varPart = lastWord.substring(0, lastWord.lastIndexOf("."));
            suggestions = getMethodSuggestions(varPart, fullText);
        } else {
            suggestions = getGlobalSuggestions(lastWord);
        }

        return new AutocompleteResult(from, suggestions);
    }

    private List<Suggestion> getPackageSuggestions(String lastWord) {
        List<Suggestion> suggestions = new ArrayList<>();

        String searchPath = lastWord.contains(".")
                                    ? lastWord.substring(0, lastWord.lastIndexOf(".")).replace('.', '/')
                                    : "";

        String filter = lastWord.contains(".")
                                ? lastWord.substring(lastWord.lastIndexOf(".") + 1).toLowerCase()
                                : lastWord.toLowerCase();

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory(resolver);

            String locationPattern = "classpath*:" + searchPath + "/*.class";
            Resource[] resources = resolver.getResources(locationPattern);

            for (Resource resource : resources) {
                try {
                    MetadataReader reader = factory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    String simpleName = className.substring(className.lastIndexOf('.') + 1);

                    if (simpleName.toLowerCase().startsWith(filter)) {
                        suggestions.add(new Suggestion(simpleName, "class", "class", className, null));
                    }
                } catch (Exception ignored) {

                }
            }
        } catch (IOException ignoring) {
        }

        List.of("java.util", "java.time", "ru.slie").forEach(pkg -> {
            if (pkg.startsWith(lastWord) && pkg.length() > lastWord.length()) {
                String remaining = pkg.substring(lastWord.length());
                String nextPart = remaining.contains(".") ? remaining.substring(0, remaining.indexOf(".")) : remaining;

                if (!nextPart.isEmpty()) {
                    suggestions.add(new Suggestion(nextPart, "package", "package", pkg, nextPart + "."));
                }
            }
        });

        return suggestions.stream().distinct().toList();
    }

    private List<Suggestion> getSubpackages(ScanResult scanResult, String searchPath, String filter) {
        List<Suggestion> suggestions = new ArrayList<>();

        scanResult.getPackageInfo().forEach(pkgInfo -> {
            String pkgName = pkgInfo.getName();

            if (pkgName.startsWith(searchPath + ".") || (searchPath.isEmpty() && !pkgName.contains("."))) {
                String remaining = searchPath.isEmpty() ? pkgName : pkgName.substring(searchPath.length() + 1);
                String simpleName = remaining.contains(".") ? remaining.substring(0, remaining.indexOf(".")) : remaining;
                if (simpleName.toLowerCase().startsWith(filter)) {
                    suggestions.add(new Suggestion(
                            simpleName,
                            "package",
                            "package",
                            pkgName,
                            simpleName + "."
                    ));
                }
            }
        });
        return suggestions;
    }

    private List<Suggestion> getClassesInPackage(ScanResult scanResult, String searchPath, String filter) {
        List<Suggestion> suggestions = new ArrayList<>();

        scanResult.getAllClasses().forEach(classInfo -> {
            if (classInfo.getPackageName().equals(searchPath)) {
                String simpleName = classInfo.getSimpleName();
                if (simpleName.toLowerCase().startsWith(filter)) {
                    suggestions.add(new Suggestion(
                            simpleName,
                            "class",
                            "class",
                            classInfo.getName(),
                            null
                    ));
                }
            }
        });
        return suggestions;
    }

    private List<Suggestion> getMethodSuggestions(String varPart, String fullText) {
        List<Suggestion> suggestions = new ArrayList<>();
        Class<?> clazz = resolveClass(varPart, fullText);

        if (clazz != null) {
            for (Method m : clazz.getMethods()) {
                suggestions.add(new Suggestion(
                        m.getName(),
                        "method",
                        m.getReturnType().getSimpleName(),
                        Arrays.stream(m.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(", ", "(", ")")),
                        null
                ));
            }
        }
        return suggestions.stream().distinct().sorted(Comparator.comparing(Suggestion::name)).toList();
    }

    private List<Suggestion> getGlobalSuggestions(String lastWord) {
        List<Suggestion> suggestions = new ArrayList<>();
        String filter = lastWord.toLowerCase();

        for (String beanName : context.getBeanDefinitionNames()) {
            if (beanName.toLowerCase().startsWith(filter)) {
                Class<?> type = context.getType(beanName);
                suggestions.add(new Suggestion(beanName, "variable",
                        type != null ? type.getSimpleName() : "Bean", "Spring Managed Bean", null));
            }
        }

        Stream.of("def", "if", "else", "return", "import", "println", "log", "new", "try", "catch", "finally").filter(k -> k.startsWith(filter))
                .forEach(k -> suggestions.add(new Suggestion(k, "keyword", "keyword", null, null)));

        return suggestions;
    }

    private Class<?> resolveClass(String className, String fullText) {
        Pattern importPattern = Pattern.compile("import\\s+([\\w.]+?\\." + className + ");");
        Matcher importMatcher = importPattern.matcher(fullText);
        if (importMatcher.find()) {
            try { return Class.forName(importMatcher.group(1)); } catch (Exception ignored) {}
        }

        Pattern starPattern = Pattern.compile("import\\s+([\\w.]+)\\.\\*;");
        Matcher starMatcher = starPattern.matcher(fullText);
        while (starMatcher.find()) {
            try {
                return Class.forName(starMatcher.group(1) + "." + className);
            } catch (Exception ignored) {}
        }

        try { return Class.forName("java.lang." + className); } catch (Exception ignored) {}
        try { return Class.forName("java.util." + className); } catch (Exception ignored) {}

        return findClassGlobally(className);
    }

    private List<Suggestion> getGlobalClassSuggestions(String filter, String fullText) {
        List<Suggestion> suggestions = new ArrayList<>();
        if (filter.length() < 3) return suggestions;

        try (ScanResult scanResult = new ClassGraph().enableClassInfo().scan()) {
            scanResult.getAllClasses().forEach(cls -> {
                if (cls.getSimpleName().toLowerCase().startsWith(filter)) {
                    boolean alreadyImported = fullText.contains("import " + cls.getName());

                    suggestions.add(new Suggestion(
                            cls.getSimpleName(),
                            "class",
                            cls.getPackageName(),
                            alreadyImported ? "Already imported" : "Press Enter to auto-import",
                            alreadyImported ? cls.getSimpleName() : "AUTO_IMPORT:" + cls.getName() + ":" + cls.getSimpleName()
                    ));
                }
            });
        }
        return suggestions;
    }

    private String getCurrentLine(String textBefore) {
        int lastNewline = textBefore.lastIndexOf('\n');
        return lastNewline < 0 ? textBefore : textBefore.substring(lastNewline + 1);
    }

    private String getLastWord(String text) {
        Pattern p = Pattern.compile("[\\w.]+$");
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : "";
    }
}
