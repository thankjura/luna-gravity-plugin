package ru.slie.luna.plugins.gravity.metrics;

import com.blazebit.persistence.CriteriaBuilder;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.slie.luna.db.SearchResultList;
import ru.slie.luna.db.filter.Filters;
import ru.slie.luna.db.query.FindOptions;
import ru.slie.luna.db.query.Query;
import ru.slie.luna.plugins.gravity.db.ScriptRunResultEntity;
import ru.slie.luna.plugins.gravity.metrics.dto.MetricPointDto;
import ru.slie.luna.plugins.gravity.script.ScriptRunEvent;
import ru.slie.luna.regolith.ActiveDocManager;
import ru.slie.luna.search.SearchParams;
import ru.slie.luna.search.SearchResult;

import java.time.Duration;
import java.time.LocalDateTime;
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

    @NullMarked
    public List<MetricPointDto> getMetricPoints(String scriptId, LocalDateTime from, LocalDateTime to) {
        String bucketStep = resolveBucketStep(from, to);
        CriteriaBuilder<ScriptRunResultEntity> cb = activeDocManager.getCriteriaBuilder(ScriptRunResultEntity.class);
        return cb.from(ScriptRunResultEntity.class, "s")
                .selectNew(MetricPointDto.class)
                    .with("FUNCTION('date_trunc', :timeStep, s.startAt)", "bucket")
                    .with("COUNT(s.id)", "totalCount")
                    .with("AVG(s.executionTimeMs)", "avgExecutionTime")
                    .with("MAX(s.executionTimeMs)", "maxExecutionTime")
                    .with("AVG(s.cpuTimeMs)", "avgCpuTime")
                    .with("MAX(s.cpuTimeMs)", "maxCpuTime")
                .end()
                .where("s.scriptId").eq(scriptId)
                .where("s.startAt").between(from).and(to)
                .groupBy("FUNCTION('date_trunc', :timeStep, s.startAt)")
                .orderByAsc("FUNCTION('date_trunc', :timeStep, s.startAt)")
                .setParameter("timeStep", bucketStep)
                .getResultList();
    }

    @NullMarked
    private static String resolveBucketStep(LocalDateTime start, LocalDateTime end) {
        long hoursBetween = Duration.between(start, end).toHours();
        if (hoursBetween <= 2) {
            return "minute";
        } else if (hoursBetween <= 24) {
            return "hour";
        } else if (hoursBetween <= 24 * 14) {
            return "hour";
        } else if (hoursBetween <= 24 * 30) {
            return "day";
        } else {
            return "week";
        }
    }
}
