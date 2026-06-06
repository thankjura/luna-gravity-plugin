package ru.slie.luna.plugins.gravity.metrics;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.slie.luna.db.SearchResultList;
import ru.slie.luna.db.filter.Filters;
import ru.slie.luna.db.query.FindOptions;
import ru.slie.luna.db.query.Query;
import ru.slie.luna.plugins.gravity.db.ScriptRunResultEntity;
import ru.slie.luna.plugins.gravity.script.ScriptRunEvent;
import ru.slie.luna.regolith.ActiveDocManager;
import ru.slie.luna.search.SearchParams;
import ru.slie.luna.search.SearchResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class ScriptMetricsManager {
    private final ActiveDocManager activeDocManager;

    public ScriptMetricsManager(ActiveDocManager activeDocManager) {
        this.activeDocManager = activeDocManager;
    }

    private ScriptRunResult wrap(ScriptRunResultEntity scriptRunResultEntity) {
        return new ScriptRunResult(scriptRunResultEntity);
    }

    public SearchResult<ScriptRunResult> getLastResults(String scriptId, SearchParams searchParams) {
        Query<ScriptRunResultEntity> query = activeDocManager.query(ScriptRunResultEntity.class);
        query.filter(Filters.eq("scriptId", scriptId));
        FindOptions options = new FindOptions();
        options.sort("startAt", false).limit(searchParams.getLimit()).skip(searchParams.getSkip());
        List<ScriptRunResultEntity> results = query.list(options);
        results.sort(Comparator.comparing(ScriptRunResultEntity::getStartAt));
        long total = query.count();
        return new SearchResultList<>(total, results.stream().map(this::wrap).toList(), searchParams);
    }

    @Transactional
    public void create(Collection<ScriptRunEvent> scriptRunEvents) {
        List<ScriptRunResultEntity> scriptRunResultEntities = new ArrayList<>();
        for (ScriptRunEvent event : scriptRunEvents) {
            ScriptRunResultEntity scriptRunResultEntity = new ScriptRunResultEntity(
                    event.getScriptId(),
                    event.getStartTime(),
                    event.getExecutionTimeMs(),
                    event.getCpuTimeMs(),
                    event.getException(),
                    event.getPayload(),
                    event.getLogs()
            );
            scriptRunResultEntities.add(scriptRunResultEntity);
        }

        activeDocManager.saveAll(scriptRunResultEntities);
    }
}
