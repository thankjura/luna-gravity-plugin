package ru.slie.luna.plugins.gravity.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.slie.luna.event.IssueEvent;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.Issue;
import ru.slie.luna.plugins.gravity.model.ListenerScript;
import ru.slie.luna.plugins.gravity.script.ScriptManager;
import ru.slie.luna.plugins.gravity.script.ScriptRunnerService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class GravityGlobalListener implements DisposableBean {
    private final ScriptManager scriptManager;
    private final ScriptRunnerService runnerService;
    private static final Logger log = LoggerFactory.getLogger(GravityGlobalListener.class);
    private final ExecutorService asyncExecutor;

    public GravityGlobalListener(ScriptManager scriptManager,
                                 ScriptRunnerService runnerService) {
        this.scriptManager = scriptManager;
        this.runnerService = runnerService;
        this.asyncExecutor = new ThreadPoolExecutor(
                4,
                10,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new ThreadFactory() {
                    private int counter = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "GravityAsyncScript-" + counter++);
                    }
                },
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) throws ValidateException {
        Collection<ListenerScript> scripts = scriptManager.getCachedListenerScripts();
        if (scripts.isEmpty()) {
            return;
        }

        Issue issue = issueEvent.getIssue();
        if (issue == null) {
            return;
        }

        long projectId = issue.getProjectId();
        Map<String, Object> bindings = new HashMap<>();
        bindings.put("issue", issue);
        bindings.put("user", issueEvent.getUser());
        bindings.put("event", issueEvent);

        for (ListenerScript script: scripts) {
            if (!script.getEventTypeIds().contains(issueEvent.getEventTypeId())) {
                continue;
            }

            if (!script.getProjectIds().isEmpty() && !script.getProjectIds().contains(projectId)) {
                continue;
            }

            processScript(script, bindings);
        }
    }

    private void processScript(ListenerScript script, Map<String, Object> bindings) throws ValidateException {
        if (script.isAsync()) {
            Map<String, Object> asyncBindings = new HashMap<>(bindings);

            asyncExecutor.submit(() -> {
                try {
                    runnerService.executeWithMetrics("listener-" + script.getId(), script.getScript(), asyncBindings, true);
                } catch (Throwable e) {
                    log.error("Failed to execute async script with id: {}", script.getId(), e);
                }
            });
        } else {
            try {
                runnerService.executeWithMetrics("listener-" + script.getId(), script.getScript(), bindings, false);
            } catch (Throwable e) {
                log.error("Failed to execute script with id: {}", script.getScript(), e);
            }
        }
    }

    @Override
    public void destroy() {
        log.info("Shutting down Gravity async executor...");
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(300, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
