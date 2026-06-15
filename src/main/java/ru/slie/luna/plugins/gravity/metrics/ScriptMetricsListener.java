package ru.slie.luna.plugins.gravity.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.slie.luna.plugins.gravity.script.ScriptRunEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ScriptMetricsListener implements InitializingBean, DisposableBean {
    private final ScriptMetricsManager scriptMetricsManager;
    private final ScriptMetricsExecutor scriptMetricsExecutor;
    private final BlockingQueue<ScriptRunEvent> batchQueue = new LinkedBlockingQueue<>(50000);
    private static final int BATCH_SIZE = 50;
    private static final Logger log = LoggerFactory.getLogger(ScriptMetricsListener.class);
    private final AtomicBoolean isFlushing = new AtomicBoolean(false);

    public ScriptMetricsListener(ScriptMetricsManager scriptMetricsManager,
                                 ScriptMetricsExecutor scriptMetricsExecutor) {
        this.scriptMetricsManager = scriptMetricsManager;
        this.scriptMetricsExecutor = scriptMetricsExecutor;
    }

    public void afterPropertiesSet() {
        scriptMetricsExecutor.scheduleWithFixedDelay(this::flushBatch, 5, 5, TimeUnit.SECONDS);
    }

    @Async
    @EventListener
    public void handleScriptRunEvent(ScriptRunEvent event) {
        scriptMetricsExecutor.submit(() -> {
            boolean accepted = batchQueue.offer(event);
            if (accepted && batchQueue.size() >= BATCH_SIZE) {
                flushBatch();
            }
        });
    }

    private void flushBatch() {
        if (batchQueue.isEmpty()) {
            return;
        }

        if (!isFlushing.compareAndSet(false, true)) {
            return;
        }

        try {

            List<ScriptRunEvent> batch = new ArrayList<>();
            batchQueue.drainTo(batch, BATCH_SIZE);

            if (!batch.isEmpty()) {
                try {
                    scriptMetricsManager.create(batch);
                } catch (Exception e) {
                    log.error("Failed to save metrics batch: {}", e.getMessage(), e);

                    for (ScriptRunEvent event : batch) {
                        if (!batchQueue.offer(event)) {
                            log.error("Failed to return events to queue");
                        }
                    }
                }
            }
        } finally {
            isFlushing.set(false);
        }
    }

    @Override
    public void destroy() {
        flushBatch();
    }
}
