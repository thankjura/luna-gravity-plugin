package ru.slie.luna.plugins.gravity.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.rest.request.AutocompleteRequest;
import ru.slie.luna.plugins.gravity.rest.request.ScriptRequest;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;
import ru.slie.luna.plugins.gravity.script.groovy.AutocompleteResult;
import ru.slie.luna.plugins.gravity.script.groovy.AutocompleteGroovyService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/gravity/script")
public class ScriptRest {
    private final ScriptRunnerService scriptService;
    private final Logger log = LoggerFactory.getLogger(ScriptRest.class);
    private final I18nResolver i18n;
    private final AutocompleteGroovyService autocompleteService;


    public ScriptRest(ScriptRunnerService scriptService,
                      I18nResolver i18n,
                      AutocompleteGroovyService autocompleteService) {
        this.scriptService = scriptService;
        this.i18n = i18n;
        this.autocompleteService = autocompleteService;
    }

    @PostMapping(value = "/execute", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public SseEmitter execute(@RequestBody ScriptRequest request) {
        Long timeout = request.getTimeout();
        if (timeout == null) {
            timeout = 60_000L;
        }
        SseEmitter emitter = new SseEmitter(timeout);
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> context = new HashMap<>();
                Object result = scriptService.execute(request.getScriptContent(), context, line -> {
                    try {
                        emitter.send(SseEmitter.event().name("log").data(line));
                    } catch (Exception e) {
                        log.debug(i18n.getText("gravity.warn.client_disconnected"), e);
                    }
                });

                if (result == null) {
                    result = "";
                }

                emitter.send(SseEmitter.event().name("result").data(result));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }


    @PostMapping("/autocomplete")
    public AutocompleteResult getAutocomplete(@RequestBody AutocompleteRequest request) {
        Integer limit = request.getLimit();
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        if (request.getCode() == null || request.getLine() == null || request.getColumn() == null) {
            return new AutocompleteResult();
        }

        return autocompleteService.getSuggestions(request.getCode(), request.getLine(), request.getColumn(), limit);
    }
}
