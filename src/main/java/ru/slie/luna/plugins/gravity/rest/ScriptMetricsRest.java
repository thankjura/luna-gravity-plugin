package ru.slie.luna.plugins.gravity.rest;

import org.springframework.web.bind.annotation.*;
import ru.slie.luna.plugins.gravity.metrics.ScriptMetricsManager;
import ru.slie.luna.plugins.gravity.metrics.ScriptRunResult;
import ru.slie.luna.plugins.gravity.metrics.dto.MetricPointCollection;
import ru.slie.luna.plugins.gravity.utils.Constants;
import ru.slie.luna.search.SearchParams;
import ru.slie.luna.search.SearchResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @GetMapping("/{scriptId}/points")
    public MetricPointCollection getChartPoints(@PathVariable String scriptId, @RequestParam("from") String from, @RequestParam(value = "to", required = false) String to) {
        LocalDateTime fromDate = LocalDateTime.parse(from, DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT));
        LocalDateTime toDate;
        if (to != null && !to.isEmpty()) {
            toDate = LocalDateTime.parse(to, DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT));
        } else {
            toDate = LocalDateTime.now();
        }
        return scriptMetricsManager.getMetricPoints(scriptId, fromDate, toDate);
    }
}
