package ru.slie.luna.plugins.gravity.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.permission.GlobalPermissionManager;
import ru.slie.luna.plugins.gravity.model.WorkflowScript;
import ru.slie.luna.plugins.gravity.rest.request.ScriptFileRequest;
import ru.slie.luna.plugins.gravity.script.ScriptManager;
import ru.slie.luna.plugins.gravity.script.ScriptParseErrorException;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.plugins.gravity.script.file.ScriptFileManager;
import ru.slie.luna.plugins.gravity.script.file.ScriptFileNode;
import ru.slie.luna.security.AuthenticationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Script editor: files and directories in {@code <luna.home>/scripts}.
 */
@RestController
@RequestMapping("/gravity/files")
public class ScriptFileRest {
    private final ScriptFileManager fileManager;
    private final ScriptManager scriptManager;
    private final ScriptRunnerService scriptRunnerService;
    private final AuthenticationContext authenticationContext;
    private final GlobalPermissionManager globalPermissionManager;
    private final I18nResolver i18n;

    public ScriptFileRest(ScriptFileManager fileManager,
                          ScriptManager scriptManager,
                          ScriptRunnerService scriptRunnerService,
                          AuthenticationContext authenticationContext,
                          GlobalPermissionManager globalPermissionManager,
                          I18nResolver i18n) {
        this.fileManager = fileManager;
        this.scriptManager = scriptManager;
        this.scriptRunnerService = scriptRunnerService;
        this.authenticationContext = authenticationContext;
        this.globalPermissionManager = globalPermissionManager;
        this.i18n = i18n;
    }

    private void checkAdmin() {
        if (!globalPermissionManager.hasAdminPerms(authenticationContext.getCurrentUser())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/tree")
    public ScriptFileNode getTree() throws IOException {
        checkAdmin();
        return fileManager.getTree();
    }

    /** All script files, for selecting a file in workflow functions. */
    @GetMapping("/list")
    public List<String> getScripts() throws IOException {
        checkAdmin();
        return fileManager.listScripts();
    }

    @GetMapping("/content")
    public ScriptFileManager.FileContent getContent(@RequestParam String path) throws ValidateException, IOException {
        checkAdmin();
        return fileManager.read(path);
    }

    /** Saves a file. A file used by workflows must compile, otherwise transitions would break. */
    @PutMapping("/content")
    public ScriptFileManager.FileContent save(@RequestBody ScriptFileRequest request) throws ValidateException, IOException {
        checkAdmin();
        if (!scriptManager.getFileUsages(request.getPath()).isEmpty()) {
            try {
                scriptRunnerService.validate(request.getContent() == null ? "" : request.getContent());
            } catch (ScriptParseErrorException e) {
                throw new ValidateException("content", i18n.getText("gravity.files.used_invalid", e.getMessage()));
            }
        }
        return fileManager.save(request.getPath(), request.getContent(), request.getModified());
    }

    @PostMapping("/file")
    public Map<String, String> createFile(@RequestBody ScriptFileRequest request) throws ValidateException, IOException {
        checkAdmin();
        return Map.of("path", fileManager.createFile(request.getPath()));
    }

    @PostMapping("/directory")
    public Map<String, String> createDirectory(@RequestBody ScriptFileRequest request) throws ValidateException, IOException {
        checkAdmin();
        return Map.of("path", fileManager.createDirectory(request.getPath()));
    }

    @PostMapping("/rename")
    public Map<String, String> rename(@RequestBody ScriptFileRequest request) throws ValidateException, IOException {
        checkAdmin();
        checkNotUsed(request.getPath());
        return Map.of("path", fileManager.rename(request.getPath(), request.getNewPath()));
    }

    @DeleteMapping
    public void delete(@RequestParam String path) throws ValidateException, IOException {
        checkAdmin();
        checkNotUsed(path);
        fileManager.delete(path);
    }

    /** Workflow functions using the file (or files of the directory). */
    @GetMapping("/usages")
    public List<WorkflowScript> getUsages(@RequestParam String path) {
        checkAdmin();
        return scriptManager.getFileUsages(path);
    }

    private void checkNotUsed(String path) throws ValidateException {
        List<WorkflowScript> usages = scriptManager.getFileUsages(path);
        if (usages.isEmpty()) {
            return;
        }
        String where = usages.stream()
                             .limit(5)
                             .map(u -> u.getWorkflowName() + " → " + u.getActionName())
                             .distinct()
                             .reduce((a, b) -> a + ", " + b)
                             .orElse("");
        if (usages.size() > 5) {
            where += ", …";
        }
        throw new ValidateException("path", i18n.getText("gravity.files.in_use", String.valueOf(usages.size()), where));
    }
}
