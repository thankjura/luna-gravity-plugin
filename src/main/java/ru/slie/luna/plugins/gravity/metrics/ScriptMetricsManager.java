package ru.slie.luna.plugins.gravity.metrics;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.slie.luna.db.SearchResultImpl;
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

    public SearchResult<ScriptRunResult> getResults(String functionId, SearchParams searchParams) {
        Query<ScriptRunResultEntity> query = activeDocManager.query(ScriptRunResultEntity.class);
        query.filter(Filters.eq("functionId", functionId));
        FindOptions options = new FindOptions();
        options.sort("startAt");
        return new SearchResultImpl<>(query, searchParams, options, this::wrap);
    }

    @Transactional
    public void create(Collection<ScriptRunEvent> scriptRunEvents) {
        List<ScriptRunResultEntity> scriptRunResultEntities = new ArrayList<>();
        for (ScriptRunEvent event : scriptRunEvents) {
            ScriptRunResultEntity scriptRunResultEntity = new ScriptRunResultEntity(
                    event.getFunctionId(),
                    event.getStartTime(),
                    event.getExecutionTimeMs(),
                    event.getCpuTimeMs(),
                    event.getException(),
                    event.getPayload()
            );
            scriptRunResultEntities.add(scriptRunResultEntity);
        }

        activeDocManager.saveAll(scriptRunResultEntities);
    }
}
