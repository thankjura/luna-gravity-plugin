package ru.slie.luna.plugins.gravity.script.file;

import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.script.ScriptParseErrorException;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.system.HomeManager;
import ru.slie.luna.system.cluster.ClusterMessageConsumer;
import ru.slie.luna.system.cluster.ClusterMessagingService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Groovy scripts stored as files in {@code <luna.home>/scripts}. Paths used by the API are relative to that root,
 * separated by {@code /}.
 * <p>
 * Workflow functions read scripts through {@link #getCompiled(String)}: a script is read and compiled once and kept
 * in memory until it is changed through this manager (on any cluster node).
 */
@Component
public class ScriptFileManager implements ClusterMessageConsumer, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(ScriptFileManager.class);
    public static final String EXTENSION = ".groovy";
    private static final String CHANNEL = "gravity.scripts";
    private static final String INVALIDATE_ALL = "*";
    private static final long MAX_SIZE = 2 * 1024 * 1024;

    private final Path root;
    private final ScriptRunnerService scriptRunner;
    private final ClusterMessagingService clusterMessaging;
    private final TransactionTemplate transactionTemplate;
    private final I18nResolver i18n;
    private final Map<String, CompiledScript> cache = new ConcurrentHashMap<>();

    /** Cached script: compiled class, or the compilation error. */
    public record CompiledScript(String path, String content, Class<? extends Script> scriptClass, String error) {}

    public record FileContent(String path, String content, long modified) {}

    public ScriptFileManager(HomeManager homeManager,
                             ScriptRunnerService scriptRunner,
                             ClusterMessagingService clusterMessaging,
                             PlatformTransactionManager transactionManager,
                             I18nResolver i18n) {
        this.root = homeManager.getHomeDirectory().resolve("scripts").toAbsolutePath().normalize();
        this.scriptRunner = scriptRunner;
        this.clusterMessaging = clusterMessaging;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.i18n = i18n;
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        Files.createDirectories(root);
        clusterMessaging.registerListener(this, CHANNEL);
    }

    @Override
    public void destroy() {
        clusterMessaging.unregisterListener(this);
        cache.clear();
    }

    // ------------------------------------------------------------------------------------------------------ reading

    public ScriptFileNode getTree() throws IOException {
        return node(root);
    }

    private ScriptFileNode node(Path path) throws IOException {
        boolean directory = Files.isDirectory(path);
        List<ScriptFileNode> children = null;
        if (directory) {
            children = new ArrayList<>();
            try (Stream<Path> stream = Files.list(path)) {
                for (Path child : stream.toList()) {
                    if (!isHidden(child)) {
                        children.add(node(child));
                    }
                }
            }
            children.sort(Comparator.comparing((ScriptFileNode n) -> !n.directory())
                                    .thenComparing(n -> n.name().toLowerCase()));
        }
        return new ScriptFileNode(
                path.equals(root) ? "" : path.getFileName().toString(),
                relative(path),
                directory,
                directory ? 0 : Files.size(path),
                Files.getLastModifiedTime(path).toMillis(),
                children
        );
    }

    /** All script files (relative paths), for selection in workflow functions. */
    public List<String> listScripts() throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(EXTENSION))
                         .filter(p -> !isHiddenPath(p))
                         .map(this::relative)
                         .sorted()
                         .toList();
        }
    }

    public boolean isScript(String path) {
        try {
            Path p = resolve(path);
            return Files.isRegularFile(p) && p.getFileName().toString().endsWith(EXTENSION);
        } catch (ValidateException e) {
            return false;
        }
    }

    public FileContent read(String path) throws ValidateException, IOException {
        Path p = resolveExisting(path);
        if (!Files.isRegularFile(p)) {
            throw new ValidateException("path", i18n.getText("gravity.files.not_a_file", path));
        }
        if (Files.size(p) > MAX_SIZE) {
            throw new ValidateException("path", i18n.getText("gravity.files.too_large", path));
        }
        return new FileContent(relative(p), Files.readString(p, StandardCharsets.UTF_8), Files.getLastModifiedTime(p).toMillis());
    }

    /**
     * Script compiled from a file, cached in memory. Never null: a missing or broken file gives a
     * {@link CompiledScript} with an error.
     */
    public CompiledScript getCompiled(String path) {
        String key = normalizeKey(path);
        return cache.computeIfAbsent(key, this::load);
    }

    private CompiledScript load(String key) {
        String content;
        try {
            content = read(key).content();
        } catch (ValidateException e) {
            return new CompiledScript(key, null, null, i18n.getText("gravity.files.not_found", key));
        } catch (IOException e) {
            log.error("Unable to read script {}", key, e);
            return new CompiledScript(key, null, null, e.getMessage());
        }
        try {
            String name = key.substring(key.lastIndexOf('/') + 1);
            return new CompiledScript(key, content, scriptRunner.compile(content, name), null);
        } catch (ScriptParseErrorException e) {
            return new CompiledScript(key, content, null, e.getMessage());
        }
    }

    // ------------------------------------------------------------------------------------------------------ writing

    /**
     * @param expectedModified last modification time the client has seen, null to skip the conflict check
     */
    public FileContent save(String path, String content, Long expectedModified) throws ValidateException, IOException {
        Path p = resolveExisting(path);
        if (!Files.isRegularFile(p)) {
            throw new ValidateException("path", i18n.getText("gravity.files.not_a_file", path));
        }
        if (expectedModified != null && Files.getLastModifiedTime(p).toMillis() != expectedModified) {
            throw new ValidateException("content", i18n.getText("gravity.files.conflict"));
        }
        String text = content == null ? "" : content;
        if (text.getBytes(StandardCharsets.UTF_8).length > MAX_SIZE) {
            throw new ValidateException("content", i18n.getText("gravity.files.too_large", path));
        }
        Path tmp = Files.createTempFile(p.getParent(), ".save-", ".tmp");
        try {
            Files.writeString(tmp, text, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
        invalidate(relative(p));
        return read(relative(p));
    }

    public String createFile(String path) throws ValidateException, IOException {
        String withExtension = path != null && !path.endsWith(EXTENSION) ? path + EXTENSION : path;
        Path p = resolveNew(withExtension);
        Files.createDirectories(p.getParent());
        Files.createFile(p);
        invalidate(relative(p));
        return relative(p);
    }

    public String createDirectory(String path) throws ValidateException, IOException {
        Path p = resolveNew(path);
        Files.createDirectories(p);
        return relative(p);
    }

    public String rename(String path, String newPath) throws ValidateException, IOException {
        Path source = resolveExisting(path);
        if (source.equals(root)) {
            throw new ValidateException("path", i18n.getText("gravity.files.root"));
        }
        boolean file = Files.isRegularFile(source);
        String target = file && newPath != null && !newPath.endsWith(EXTENSION) ? newPath + EXTENSION : newPath;
        Path destination = resolveNew(target);
        if (destination.startsWith(source)) {
            throw new ValidateException("newPath", i18n.getText("gravity.files.invalid_path", newPath));
        }
        Files.createDirectories(destination.getParent());
        Files.move(source, destination);
        invalidate(relative(source));
        invalidate(relative(destination));
        return relative(destination);
    }

    public void delete(String path) throws ValidateException, IOException {
        Path p = resolveExisting(path);
        if (p.equals(root)) {
            throw new ValidateException("path", i18n.getText("gravity.files.root"));
        }
        if (Files.isDirectory(p)) {
            try (Stream<Path> stream = Files.walk(p)) {
                for (Path item : stream.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(item);
                }
            }
        } else {
            Files.delete(p);
        }
        invalidate(relative(p));
    }

    // -------------------------------------------------------------------------------------------------------- cache

    /** Drops cached scripts at {@code path} (a file or everything under a directory) here and on other nodes. */
    private void invalidate(String path) {
        invalidateLocal(path);
        try {
            transactionTemplate.executeWithoutResult(status -> clusterMessaging.sendRemote(CHANNEL, path));
        } catch (Exception e) {
            log.warn("Unable to notify cluster about script change {}", path, e);
        }
    }

    private void invalidateLocal(String path) {
        if (path == null || path.isEmpty() || INVALIDATE_ALL.equals(path)) {
            cache.clear();
            return;
        }
        String prefix = path + "/";
        cache.keySet().removeIf(key -> key.equals(path) || key.startsWith(prefix));
    }

    @Override
    public void receive(String channel, String message, String senderId) {
        if (CHANNEL.equals(channel)) {
            invalidateLocal(message);
        }
    }

    // -------------------------------------------------------------------------------------------------------- paths

    public String normalizeKey(String path) {
        try {
            return relative(resolve(path));
        } catch (ValidateException e) {
            return path;
        }
    }

    /** Resolves a relative path inside the scripts root, rejecting anything that escapes it. */
    private Path resolve(String path) throws ValidateException {
        String rel = path == null ? "" : path.replace('\\', '/').trim();
        while (rel.startsWith("/")) {
            rel = rel.substring(1);
        }
        for (String segment : rel.split("/")) {
            if (segment.equals("..") || segment.startsWith(".") && !segment.isEmpty() || segment.chars().anyMatch(c -> c < 32 || c == ':')) {
                throw new ValidateException("path", i18n.getText("gravity.files.invalid_path", path));
            }
        }
        Path p = root.resolve(rel).normalize();
        if (!p.startsWith(root)) {
            throw new ValidateException("path", i18n.getText("gravity.files.invalid_path", path));
        }
        return p;
    }

    private Path resolveExisting(String path) throws ValidateException, IOException {
        Path p = resolve(path);
        if (!Files.exists(p)) {
            throw new ValidateException("path", i18n.getText("gravity.files.not_found", path));
        }
        if (!p.toRealPath().startsWith(root.toRealPath())) {
            throw new ValidateException("path", i18n.getText("gravity.files.invalid_path", path));
        }
        return p;
    }

    private Path resolveNew(String path) throws ValidateException, IOException {
        if (path == null || path.isBlank() || path.endsWith("/")) {
            throw new ValidateException("path", i18n.getText("gravity.files.name_required"));
        }
        Path p = resolve(path);
        if (p.equals(root)) {
            throw new ValidateException("path", i18n.getText("gravity.files.name_required"));
        }
        if (Files.exists(p)) {
            throw new ValidateException("path", i18n.getText("gravity.files.exists", relative(p)));
        }
        Path existingParent = p.getParent();
        while (existingParent != null && !Files.exists(existingParent)) {
            existingParent = existingParent.getParent();
        }
        if (existingParent == null || !existingParent.toRealPath().startsWith(root.toRealPath())) {
            throw new ValidateException("path", i18n.getText("gravity.files.invalid_path", path));
        }
        if (Files.isRegularFile(existingParent)) {
            throw new ValidateException("path", i18n.getText("gravity.files.not_a_directory", relative(existingParent)));
        }
        return p;
    }

    private String relative(Path p) {
        return root.relativize(p).toString().replace('\\', '/');
    }

    private static boolean isHidden(Path p) {
        return p.getFileName().toString().startsWith(".");
    }

    private boolean isHiddenPath(Path p) {
        for (Path part : root.relativize(p)) {
            if (part.toString().startsWith(".")) {
                return true;
            }
        }
        return false;
    }
}
