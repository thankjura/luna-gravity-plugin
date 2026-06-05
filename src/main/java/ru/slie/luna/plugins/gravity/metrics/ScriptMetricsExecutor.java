package ru.slie.luna.plugins.gravity.metrics;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ScriptMetricsExecutor implements DisposableBean {
    private final ScheduledExecutorService executor;
    public ScriptMetricsExecutor() {
        this.executor = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread t = new Thread(r, "Gravity-Metrics-" + counter.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            }
        });
    }

    public void submit(Runnable task) {
        if (!executor.isShutdown()) {
            executor.submit(task);
        }
    }

    public void scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        if (!executor.isShutdown()) {
            executor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
        }
    }

    @Override
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(300, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
