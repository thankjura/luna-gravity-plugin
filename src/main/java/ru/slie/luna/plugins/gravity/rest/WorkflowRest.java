package ru.slie.luna.plugins.gravity.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.slie.luna.plugins.gravity.model.WorkflowScript;
import ru.slie.luna.plugins.gravity.script.ScriptManager;

import java.util.List;

@RestController
@RequestMapping("/gravity/workflow")
public class WorkflowRest {
    private final ScriptManager scriptManager;

    public WorkflowRest(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    @GetMapping("/scripts")
    public List<WorkflowScript> getWorkflowScripts() {
        return scriptManager.getWorkflowScripts();
    }
}
