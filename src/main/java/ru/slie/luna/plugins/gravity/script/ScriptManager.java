package ru.slie.luna.plugins.gravity.script;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.slie.luna.db.query.DeleteResult;
import ru.slie.luna.db.query.Query;
import ru.slie.luna.issue.workflow.Workflow;
import ru.slie.luna.issue.workflow.WorkflowManager;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.action.WorkflowActionConditionGroup;
import ru.slie.luna.issue.workflow.action.WorkflowActionFunction;
import ru.slie.luna.plugins.gravity.db.ScriptListenerEntity;
import ru.slie.luna.plugins.gravity.model.ListenerScript;
import ru.slie.luna.plugins.gravity.model.WorkflowFunctionType;
import ru.slie.luna.plugins.gravity.model.WorkflowScript;
import ru.slie.luna.plugins.gravity.utils.SetUtils;
import ru.slie.luna.regolith.ActiveDocManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Transactional(readOnly = true)
public class ScriptManager implements InitializingBean {
    private final ActiveDocManager activeDocManager;
    private final WorkflowManager workflowManager;
    private final Map<Long, ListenerScript> listenerScriptMap = new ConcurrentHashMap<>();
    private final PlatformTransactionManager transactionManager;

    public ScriptManager(ActiveDocManager activeDocManager,
                         WorkflowManager workflowManager,
                         PlatformTransactionManager transactionManager) {
        this.activeDocManager = activeDocManager;
        this.workflowManager = workflowManager;
        this.transactionManager = transactionManager;
    }

    private void extractFunction(Workflow workflow, WorkflowAction action, WorkflowActionFunction workflowFunction, WorkflowFunctionType functionType, List<WorkflowScript> scripts) {
        if (!functionType.getClassName().equals(workflowFunction.getClassName())) {
            return;
        }

        scripts.add(new WorkflowScript(workflow, action, workflowFunction, functionType));
    }

    private void extractScriptFromCondition(Workflow workflow, WorkflowAction action, WorkflowActionConditionGroup conditionGroup, List<WorkflowScript> scripts) {
        if (conditionGroup == null) {
            return;
        }

        if (conditionGroup.getCondition() != null) {
            extractFunction(workflow, action, conditionGroup.getCondition(), WorkflowFunctionType.CONDITION, scripts);
        }

        for (WorkflowActionConditionGroup group : conditionGroup.getItems()) {
            extractScriptFromCondition(workflow, action, group, scripts);
        }
    }

    // TODO: cache manager
    public List<WorkflowScript> getWorkflowScripts() {
        List<WorkflowScript> workflowScripts = new ArrayList<>();

        for (Workflow workflow: workflowManager.getAll()) {
            for (WorkflowAction action : workflow.getActions()) {
                if (action.getConditions() != null) {
                    extractScriptFromCondition(workflow, action, action.getConditions(), workflowScripts);
                }

                for (WorkflowActionFunction validator: action.getValidators()) {
                    extractFunction(workflow, action, validator, WorkflowFunctionType.VALIDATOR, workflowScripts);
                }

                for (WorkflowActionFunction postfunction: action.getPostfunctions()) {
                    extractFunction(workflow, action, postfunction, WorkflowFunctionType.POSTFUNCTION, workflowScripts);
                }
            }
        }

        return workflowScripts;
    }

    public List<ListenerScript> getListenerScripts() {
        Query<ScriptListenerEntity> query = activeDocManager.query(ScriptListenerEntity.class);
        return query.list().stream().map(ListenerScript::new).toList();
    }

    public Collection<ListenerScript> getCachedListenerScripts() {
        return this.listenerScriptMap.values();
    }

    public Optional<ListenerScript> getListenerScript(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        Optional<ScriptListenerEntity> entity = activeDocManager.getById(ScriptListenerEntity.class, id);
        return entity.map(ListenerScript::new);
    }

    @Transactional
    public void save(ListenerScript script) {
        ScriptListenerEntity entity = null;
        if (script.getId() != null) {
            entity = activeDocManager.getById(ScriptListenerEntity.class, script.getId()).orElse(null);
        }

        if (entity == null) {
            entity = new ScriptListenerEntity(
                    script.getName(),
                    script.getDescription(),
                    SetUtils.setToString(script.getProjectIds()),
                    SetUtils.setToString(script.getEventTypeIds()),
                    script.getScript()
            );
        }

        activeDocManager.save(entity);
        script.update(entity);
        listenerScriptMap.put(script.getId(), script);
    }

    @Transactional
    public DeleteResult deleteListenerScript(Long scriptId) {
        listenerScriptMap.remove(scriptId);
        return activeDocManager.delete(ScriptListenerEntity.class, scriptId);
    }

    @Override
    public void afterPropertiesSet() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            for (ScriptListenerEntity entity: activeDocManager.query(ScriptListenerEntity.class).list()) {
                this.listenerScriptMap.put(entity.getId(), new ListenerScript(entity));
            }
        });
    }
}
