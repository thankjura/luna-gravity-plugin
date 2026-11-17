package ru.slie.luna.plugins.gravity.script.groovy.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.ClassReader;
import org.springframework.asm.Opcodes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import ru.slie.luna.issue.Issue;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Index of classes available to scripts: luna-api, JDK, Groovy, bundled libraries and luna-core.
 * Built once in background from class file headers (classes are not loaded).
 */
@Component
public class ClassIndex {
    private static final Logger log = LoggerFactory.getLogger(ClassIndex.class);

    private static final String LUNA_ROOT = "ru/slie/luna/";
    private static final String API_MARKER = "ru/slie/luna/issue/Issue.class";
    private static final List<String> JDK_MODULES = List.of("java.base", "java.sql", "java.net.http", "java.logging");
    private static final Set<String> JDK_COMMON = Set.of(
            "java.lang", "java.util", "java.util.function", "java.util.stream", "java.util.regex",
            "java.util.concurrent", "java.time", "java.time.format", "java.time.temporal", "java.math",
            "java.text", "java.io", "java.nio.file", "java.net"
    );
    /** Library packages (on the Luna classpath) that are offered in completion. */
    private static final List<String> LIBRARY_ROOTS = List.of("org/apache/commons/lang3/");
    private static final List<String> GROOVY_EXCLUDED = List.of("groovy/grape/", "groovy/ui/", "groovy/cli/", "groovy/inspect/", "groovy/beans/");
    /** luna-core classes that make no sense in scripts. */
    private static final List<String> CORE_EXCLUDED_SUFFIXES = List.of("Impl", "Controller", "Rest", "Configuration", "Config", "Test");

    private final ApplicationContext context;
    private final ClassLoader lunaLoader;
    private final ClassLoader pluginLoader;
    private final CompletableFuture<Data> data;

    private record Data(List<ClassEntry> all, Map<String, List<ClassEntry>> bySimpleName,
                        Map<String, List<ClassEntry>> byPackage, NavigableSet<String> packages) {}

    public ClassIndex(ApplicationContext context) {
        this.context = context.getParent() != null ? context.getParent() : context;
        this.lunaLoader = Issue.class.getClassLoader();
        this.pluginLoader = ClassIndex.class.getClassLoader();
        this.data = CompletableFuture.supplyAsync(this::build, runnable -> {
            Thread thread = new Thread(runnable, "gravity-class-index");
            thread.setDaemon(true);
            thread.start();
        });
    }

    public ClassLoader getClassLoader() {
        return pluginLoader;
    }

    private Data data() {
        try {
            return data.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Class index is not ready", e);
            return new Data(List.of(), Map.of(), Map.of(), new TreeSet<>());
        }
    }

    public boolean isReady() {
        return data.isDone();
    }

    /** Classes whose simple name matches the pattern, best first. */
    public List<ClassEntry> search(String pattern, int limit, Predicate<ClassEntry> filter) {
        record Hit(ClassEntry entry, int quality) {}
        List<Hit> hits = new ArrayList<>();
        for (ClassEntry entry : data().all()) {
            int quality = NameMatcher.match(entry.simpleName(), pattern);
            if (quality >= 0 && filter.test(entry)) {
                hits.add(new Hit(entry, quality));
            }
        }
        hits.sort(Comparator.<Hit>comparingInt(h -> h.quality() > 0 ? 1 : 0)
                .thenComparingInt(h -> h.entry().source().rank())
                .thenComparing(h -> !h.entry().bean())
                .thenComparingInt(h -> h.entry().simpleName().length())
                .thenComparing(h -> h.entry().simpleName())
                .thenComparing(h -> h.entry().fqn()));
        return hits.stream().limit(limit).map(Hit::entry).toList();
    }

    /** All classes with exactly this simple name, best (API) first. */
    public List<ClassEntry> bySimpleName(String simpleName) {
        return data().bySimpleName().getOrDefault(simpleName, List.of());
    }

    public List<ClassEntry> inPackage(String packageName) {
        return data().byPackage().getOrDefault(packageName, List.of());
    }

    public boolean isPackage(String name) {
        return data().packages().contains(name);
    }

    /** Direct sub packages: {@code java -> [java.io, java.lang, ...]}. */
    public List<String> subPackages(String parent) {
        String prefix = parent.isEmpty() ? "" : parent + ".";
        List<String> out = new ArrayList<>();
        for (String p : data().packages().tailSet(prefix, true)) {
            if (!p.startsWith(prefix)) {
                break;
            }
            String rest = p.substring(prefix.length());
            if (!rest.isEmpty() && !rest.contains(".")) {
                out.add(p);
            }
        }
        return out;
    }

    public Class<?> load(String fqn) {
        try {
            return Class.forName(fqn, false, pluginLoader);
        } catch (Throwable e) {
            return null;
        }
    }

    // ---------------------------------------------------------------------------------------------------------- build

    private Data build() {
        long start = System.currentTimeMillis();
        Map<String, ClassEntry> entries = new LinkedHashMap<>();
        Set<String> beans = collectBeanTypes();

        try {
            scanLuna(entries, beans);
        } catch (Exception e) {
            log.warn("Unable to index luna classes", e);
        }
        try {
            scanJdk(entries);
        } catch (Exception e) {
            log.warn("Unable to index JDK classes", e);
        }
        try {
            scanResources(pluginLoader, "groovy/", ClassSource.GROOVY, entries,
                    rel -> GROOVY_EXCLUDED.stream().noneMatch(rel::startsWith));
        } catch (Exception e) {
            log.warn("Unable to index Groovy classes", e);
        }
        for (String root : LIBRARY_ROOTS) {
            try {
                scanResources(lunaLoader, root, ClassSource.LIBRARY, entries, rel -> true);
            } catch (Exception e) {
                log.debug("Unable to index library {}", root, e);
            }
        }

        List<ClassEntry> all = new ArrayList<>(entries.values());
        Map<String, List<ClassEntry>> bySimpleName = new HashMap<>();
        Map<String, List<ClassEntry>> byPackage = new HashMap<>();
        NavigableSet<String> packages = new TreeSet<>();
        Comparator<ClassEntry> order = Comparator.comparingInt((ClassEntry e) -> e.source().rank()).thenComparing(ClassEntry::fqn);
        for (ClassEntry e : all) {
            bySimpleName.computeIfAbsent(e.simpleName(), k -> new ArrayList<>()).add(e);
            byPackage.computeIfAbsent(e.packageName(), k -> new ArrayList<>()).add(e);
            String p = e.packageName();
            while (!p.isEmpty() && packages.add(p)) {
                int idx = p.lastIndexOf('.');
                p = idx < 0 ? "" : p.substring(0, idx);
            }
        }
        bySimpleName.values().forEach(l -> l.sort(order));
        byPackage.values().forEach(l -> l.sort(Comparator.comparing(ClassEntry::simpleName)));

        log.info("Gravity class index: {} classes in {} ms", all.size(), System.currentTimeMillis() - start);
        return new Data(all, bySimpleName, byPackage, packages);
    }

    private Set<String> collectBeanTypes() {
        Set<String> out = new HashSet<>();
        try {
            for (String name : context.getBeanDefinitionNames()) {
                try {
                    Class<?> type = context.getType(name, false);
                    if (type != null) {
                        for (Class<?> face : ClassUtils.getAllInterfacesForClassAsSet(type)) {
                            out.add(face.getName());
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable e) {
            log.debug("Unable to collect bean types", e);
        }
        return out;
    }

    private void scanLuna(Map<String, ClassEntry> entries, Set<String> beans) throws Exception {
        URL apiUrl = lunaLoader.getResource(API_MARKER);
        String apiLocation = apiUrl != null ? location(apiUrl.toString(), API_MARKER) : null;
        Set<String> apiNames = new HashSet<>();
        List<ClassEntry> core = new ArrayList<>();

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(lunaLoader);
        for (Resource resource : resolver.getResources("classpath*:" + LUNA_ROOT + "**/*.class")) {
            String url = resource.getURL().toString();
            String rel = relative(url, LUNA_ROOT);
            if (rel == null || rel.startsWith("ru/slie/luna/plugins/")) {
                continue;
            }
            String loc = location(url, rel);
            ClassSource source;
            if (loc.equals(apiLocation)) {
                source = ClassSource.API;
            } else if (loc.contains("luna-core")) {
                source = ClassSource.CORE;
            } else {
                continue; // luna-web and others: controllers, dto
            }
            ClassEntry entry;
            try (InputStream in = resource.getInputStream()) {
                entry = readEntry(rel, in, source);
            } catch (Exception e) {
                continue;
            }
            if (entry == null) {
                continue;
            }
            if (source == ClassSource.API) {
                entry = entry.withBean(beans.contains(entry.fqn()));
                apiNames.add(entry.simpleName());
                entries.put(entry.fqn(), entry);
            } else {
                core.add(entry);
            }
        }
        for (ClassEntry entry : core) {
            String name = entry.simpleName();
            if (CORE_EXCLUDED_SUFFIXES.stream().anyMatch(name::endsWith) || apiNames.contains(name)) {
                continue;
            }
            entries.putIfAbsent(entry.fqn(), entry);
        }
    }

    private void scanJdk(Map<String, ClassEntry> entries) throws Exception {
        FileSystem jrt = FileSystems.getFileSystem(URI.create("jrt:/"));
        for (String module : JDK_MODULES) {
            Path root = jrt.getPath("/modules", module);
            if (!Files.exists(root)) {
                continue;
            }
            Path javaRoot = root.resolve("java");
            if (!Files.exists(javaRoot)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(javaRoot)) {
                for (Path file : (Iterable<Path>) files::iterator) {
                    String rel = root.relativize(file).toString();
                    if (!rel.endsWith(".class") || rel.contains("/spi/") || rel.contains("/internal/")) {
                        continue;
                    }
                    String pkg = rel.substring(0, rel.lastIndexOf('/')).replace('/', '.');
                    ClassSource source = JDK_COMMON.contains(pkg) ? ClassSource.JDK_COMMON : ClassSource.JDK;
                    try (InputStream in = Files.newInputStream(file)) {
                        ClassEntry entry = readEntry(rel, in, source);
                        if (entry != null) {
                            entries.putIfAbsent(entry.fqn(), entry);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    private void scanResources(ClassLoader loader, String root, ClassSource source, Map<String, ClassEntry> entries,
                               Predicate<String> filter) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(loader);
        for (Resource resource : resolver.getResources("classpath*:" + root + "**/*.class")) {
            String rel = relative(resource.getURL().toString(), root);
            if (rel == null || !filter.test(rel)) {
                continue;
            }
            try (InputStream in = resource.getInputStream()) {
                ClassEntry entry = readEntry(rel, in, source);
                if (entry != null) {
                    entries.putIfAbsent(entry.fqn(), entry);
                }
            } catch (Exception ignored) {}
        }
    }

    private static ClassEntry readEntry(String rel, InputStream in, ClassSource source) throws Exception {
        String name = rel.substring(0, rel.length() - ".class".length());
        if (name.contains("$") || name.endsWith("package-info") || name.endsWith("module-info")) {
            return null;
        }
        ClassReader reader = new ClassReader(in);
        int access = reader.getAccess();
        if ((access & Opcodes.ACC_PUBLIC) == 0 || (access & Opcodes.ACC_SYNTHETIC) != 0) {
            return null;
        }
        ClassEntry.Kind kind;
        if ((access & Opcodes.ACC_ANNOTATION) != 0) {
            kind = ClassEntry.Kind.ANNOTATION;
        } else if ((access & Opcodes.ACC_INTERFACE) != 0) {
            kind = ClassEntry.Kind.INTERFACE;
        } else if ((access & Opcodes.ACC_ENUM) != 0) {
            kind = ClassEntry.Kind.ENUM;
        } else if ("java/lang/Record".equals(reader.getSuperName())) {
            kind = ClassEntry.Kind.RECORD;
        } else {
            kind = ClassEntry.Kind.CLASS;
        }
        String fqn = name.replace('/', '.');
        int idx = fqn.lastIndexOf('.');
        return new ClassEntry(fqn, fqn.substring(idx + 1), idx < 0 ? "" : fqn.substring(0, idx), source, kind, false);
    }

    /** Path of a class file relative to its classpath root ({@code ru/slie/luna/issue/Issue.class}). */
    private static String relative(String url, String root) {
        int bang = url.lastIndexOf("!/");
        if (bang >= 0) {
            String rel = url.substring(bang + 2);
            return rel.startsWith(root) ? rel : null;
        }
        int idx = url.lastIndexOf("/" + root);
        return idx < 0 ? null : url.substring(idx + 1);
    }

    /** Classpath root (jar or directory) of a resource. */
    private static String location(String url, String rel) {
        return url.endsWith(rel) ? url.substring(0, url.length() - rel.length()) : url;
    }
}
