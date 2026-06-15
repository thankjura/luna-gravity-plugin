package ru.slie.luna.plugins.gravity.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.slie.luna.db.query.DeleteResult;
import ru.slie.luna.event.type.IssueEventTypeManager;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.plugins.gravity.model.ListenerScript;
import ru.slie.luna.plugins.gravity.rest.request.ListenerScriptRequest;
import ru.slie.luna.plugins.gravity.rest.response.ListenerScriptWithProjects;
import ru.slie.luna.plugins.gravity.rest.response.ListenerScriptsResponse;
import ru.slie.luna.plugins.gravity.script.ScriptManager;
import ru.slie.luna.project.ProjectManager;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

@RestController
@RequestMapping("/gravity/listener")
public class ListenerScriptRest {
    private final ScriptManager scriptManager;
    private final IssueEventTypeManager eventTypeManager;
    private final ProjectManager projectManager;

    public ListenerScriptRest(ScriptManager scriptManager,
                              IssueEventTypeManager eventTypeManager,
                              ProjectManager projectManager) {
        this.scriptManager = scriptManager;
        this.eventTypeManager = eventTypeManager;
        this.projectManager = projectManager;
    }

    @GetMapping("/scripts")
    public ListenerScriptsResponse getScripts() {
        List<ListenerScript> scriptList = scriptManager.getListenerScripts();
        Set<Long> projectIds = new HashSet<>();
        for (ListenerScript script: scriptList) {
            projectIds.addAll(script.getProjectIds());
        }
        return new ListenerScriptsResponse(scriptList, eventTypeManager.getAll(), projectManager.getByIds(projectIds));
    }

    @GetMapping("/scripts/{id}")
    public ListenerScriptWithProjects getScript(@PathVariable Long id) {
        Optional<ListenerScript> script = scriptManager.getListenerScript(id);
        if (script.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new ListenerScriptWithProjects(script.get(), projectManager.getByIds(script.get().getProjectIds()));
    }

    @PostMapping("/scripts")
    public ListenerScriptWithProjects create(@RequestBody ListenerScriptRequest request) throws ValidateException {
        ListenerScript script = new ListenerScript(request);
        scriptManager.save(script);
        return new ListenerScriptWithProjects(script, projectManager.getByIds(script.getProjectIds()));
    }

    @PatchMapping("/scripts/{id}")
    public ListenerScriptWithProjects patchScript(@PathVariable Long id, @RequestBody Map<String, Object> request) throws ValidateException {
        ListenerScript script = scriptManager.getListenerScript(id).orElse(null);
        if (script == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        ObjectMapper mapper = new ObjectMapper();
        for (Map.Entry<String, Object> entry: request.entrySet()) {
            switch (entry.getKey()) {
                case "name" -> script.setName(mapper.convertValue(entry.getValue(), String.class));
                case "description" -> script.setDescription(mapper.convertValue(entry.getValue(), String.class));
                case "projectIds" -> script.setProjectIds(mapper.convertValue(entry.getValue(), new TypeReference<>() {}));
                case "eventTypeIds" -> script.setEventTypeIds(mapper.convertValue(entry.getValue(), new TypeReference<>() {}));
                case "script" -> script.setScript(mapper.convertValue(entry.getValue(), String.class));
                case "enabled" -> script.setEnabled(mapper.convertValue(entry.getValue(), Boolean.class));
                case "async" -> script.setAsync(mapper.convertValue(entry.getValue(), Boolean.class));
            }
        }
        scriptManager.save(script);
        return new ListenerScriptWithProjects(script, projectManager.getByIds(script.getProjectIds()));
    }

    @DeleteMapping("/scripts/{id}")
    public DeleteResult deleteScript(@PathVariable Long id) {
        return scriptManager.deleteListenerScript(id);
    }
}
