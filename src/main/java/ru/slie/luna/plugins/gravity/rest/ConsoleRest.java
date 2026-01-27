package ru.slie.luna.plugins.gravity.rest;

import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.slie.luna.plugins.gravity.rest.request.ScriptRequest;
import ru.slie.luna.plugins.gravity.script.ScriptExecutionErrorException;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/gravity/scripts")
public class ConsoleRest {
    private final ScriptRunnerService scriptService;

    public ConsoleRest(ScriptRunnerService scriptService) {
        this.scriptService = scriptService;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody ScriptRequest request) {
        try {
            scriptService.validate(request.getScriptContent());
            return ResponseEntity.ok().body(Map.of("valid", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/execute")
    public ResponseEntity<?> execute(@RequestBody ScriptRequest request) throws ScriptExecutionErrorException {
        Map<String, Object> context = Map.of(
                "log", LoggerFactory.getLogger("GroovyScript"),
                "now", LocalDateTime.now()
        );

        Object result = scriptService.execute(request.getScriptContent(), context);
        return ResponseEntity.ok(Map.of("result", result.toString()));
    }
}
