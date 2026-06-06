package ru.slie.luna.plugins.gravity.rest;

import org.springframework.web.bind.annotation.*;
import ru.slie.luna.plugins.gravity.metrics.ScriptMetricsManager;
import ru.slie.luna.plugins.gravity.metrics.ScriptRunResult;
import ru.slie.luna.search.SearchParams;
import ru.slie.luna.search.SearchResult;

@RestController
@RequestMapping("/gravity/scipt/metrics")
public class ScriptMetricsRest {
    private final ScriptMetricsManager scriptMetricsManager;

    public ScriptMetricsRest(ScriptMetricsManager scriptMetricsManager) {
        this.scriptMetricsManager = scriptMetricsManager;
    }

    @GetMapping("/{scriptId}")
    public SearchResult<ScriptRunResult> getResults(@PathVariable String scriptId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "15") Integer limit) {
        return scriptMetricsManager.getLastResults(scriptId, SearchParams.forPage(page, limit));
    }
}
