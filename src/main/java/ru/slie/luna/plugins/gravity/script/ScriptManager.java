package ru.slie.luna.plugins.gravity.script;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.slie.luna.db.query.DeleteResult;
import ru.slie.luna.db.query.Query;
import ru.slie.luna.exception.ValidateException;
import ru.slie.luna.issue.workflow.Workflow;
import ru.slie.luna.issue.workflow.WorkflowManager;
import ru.slie.luna.issue.workflow.action.WorkflowAction;
import ru.slie.luna.issue.workflow.action.WorkflowActionConditionGroup;
import ru.slie.luna.issue.workflow.action.WorkflowActionFunction;
import ru.slie.luna.locale.I18nResolver;
import ru.slie.luna.plugins.gravity.db.ScriptListenerEntity;
import ru.slie.luna.plugins.gravity.model.ListenerScript;
import ru.slie.luna.plugins.gravity.model.WorkflowFunctionType;
import ru.slie.luna.plugins.gravity.model.WorkflowScript;
import ru.slie.luna.plugins.gravity.script.file.ScriptFileManager;
import ru.slie.luna.plugins.gravity.utils.SetUtils;
import ru.slie.luna.plugins.gravity.workflow.AbstractGravityFunction;
import ru.slie.luna.regolith.ActiveDocManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Transactional(readOnly = true)
public class ScriptManager implements InitializingBean {
    private final ActiveDocManager activeDocManager;
    private final WorkflowManager workflowManager;
    private final Map<Long, ListenerScript> listenerScriptMap = new ConcurrentHashMap<>();
    private final PlatformTransactionManager transactionManager;
    private final I18nResolver i18n;
    private final ScriptFileManager scriptFileManager;

    public ScriptManager(ActiveDocManager activeDocManager,
                         WorkflowManager workflowManager,
                         PlatformTransactionManager transactionManager,
                         ScriptFileManager scriptFileManager,
                         I18nResolver i18n) {
        this.scriptFileManager = scriptFileManager;
        this.activeDocManager = activeDocManager;
        this.workflowManager = workflowManager;
        this.transactionManager = transactionManager;
        this.i18n = i18n;
    }

    private void extractFunction(Workflow workflow, WorkflowAction action, WorkflowActionFunction workflowFunction, WorkflowFunctionType functionType, List<WorkflowScript> scripts) {
        if (!functionType.getClassName().equals(workflowFunction.getClassName())) {
            return;
        }

        scripts.add(toWorkflowScript(workflow, action, workflowFunction, functionType));
    }

    public WorkflowScript toWorkflowScript(Workflow workflow, WorkflowAction action, WorkflowActionFunction function, WorkflowFunctionType functionType) {
        String file = AbstractGravityFunction.getScriptFile(function.getParams());
        if (file == null) {
            return new WorkflowScript(workflow, action, function, functionType);
        }
        ScriptFileManager.CompiledScript compiled = scriptFileManager.getCompiled(file);
        return new WorkflowScript(workflow, action, function, functionType, compiled.content(), compiled.error());
    }

    /**
     * Workflow functions using the script file {@code path}, or any file under it when it is a directory.
     * Drafts are included: deleting a file used by a draft would break the draft once published.
     */
    public List<WorkflowScript> getFileUsages(String path) {
        String key = scriptFileManager.normalizeKey(path);
        String prefix = key.isEmpty() ? "" : key + "/";
        List<WorkflowScript> out = new ArrayList<>();
        for (WorkflowScript script : getWorkflowScripts()) {
            String file = script.getFile();
            if (file == null) {
                continue;
            }
            String fileKey = scriptFileManager.normalizeKey(file);
            if (fileKey.equals(key) || fileKey.startsWith(prefix)) {
                out.add(script);
            }
        }
        return out;
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

    private void validateListenerScript(ListenerScript script) throws ValidateException {
        ValidateException exception = new ValidateException();

        if (script.getName() == null || script.getName().trim().isEmpty()) {
            exception.addError("name", i18n.getText("gravity.script.name.required"));
        }

        if (script.getEventTypeIds().isEmpty()) {
            exception.addError("eventTypeIds", i18n.getText("gravity.script.event_types.required"));
        }

        exception.raise();
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

        ScriptListenerEntity entity = activeDocManager.getById(ScriptListenerEntity.class, id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(new ListenerScript(entity));
    }

    @Transactional
    public void save(ListenerScript script) throws ValidateException {
        validateListenerScript(script);

        ScriptListenerEntity entity = null;
        if (script.getId() != null) {
            entity = activeDocManager.getById(ScriptListenerEntity.class, script.getId());
        }

        if (entity == null) {
            entity = new ScriptListenerEntity(
                    script.getName(),
                    script.getDescription(),
                    SetUtils.setToString(script.getProjectIds()),
                    SetUtils.setToString(script.getEventTypeIds()),
                    script.getScript(),
                    script.isAsync()
            );
        } else {
            entity.setName(script.getName());
            entity.setDescription(script.getDescription());
            entity.setProjectIds(SetUtils.setToString(script.getProjectIds()));
            entity.setEventTypeIds(SetUtils.setToString(script.getEventTypeIds()));
            entity.setScript(script.getScript());
            entity.setAsync(script.isAsync());
            entity.setEnabled(script.isEnabled());
            entity.setUpdated(LocalDateTime.now());
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
