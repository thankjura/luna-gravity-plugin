<script setup lang="ts">
import { $i18n } from "@/utils/i18n.ts";
import { WorkflowScript } from "@/interfaces/workflow.ts";
import { computed, inject, onMounted, ref, useTemplateRef } from "vue";
import { workflowService } from "@/services/workflowService.ts";
import {
  LoadOverlayComponent,
  ProjectInfo,
  MultiSelect,
  StatusComponent,
  Status,
  WorkflowFunctionType,
  SearchResult,
  BusyIconComponent,
  DropDownGroupOption,
  DropDownButton, DropDownOption, RouteNames, Injections,
} from 'luna';
import { ScriptRunResult } from "@/interfaces/metrics.ts";
import { vLazyLoad } from "@/directives/LazyLoad.ts";
import { metricService } from "@/services/metricService.ts";
import HistoryButtonComponent from "@/components/metrics/HistoryButtonComponent.vue";
import HistoryExecutionDialog from "@/components/metrics/HistoryExecutionDialog.vue";
import { ComponentExposed } from "vue-component-type-helpers";
import ProjectListComponent from "@/components/workflow/ProjectListComponent.vue";
import PerformanceDialog from "@/components/metrics/PerformanceDialog.vue";
import WorkflowScriptViewDialog from "@/components/metrics/WorkflowScriptViewDialog.vue";

const busy = ref(false);
const scripts = ref<Array<WorkflowScript>>([]);
const projects = ref<Record<string, ProjectInfo>>({});
const statuses = ref<Record<number, Status>>({});
const term = ref<string>(null);
const selectedProjects = ref<Array<string>>([]);
const results = ref<Record<string, SearchResult<ScriptRunResult>>>({});
const resultsBusy = ref<Record<string, boolean>>({});
const historyExecutionDialog = useTemplateRef<ComponentExposed<typeof HistoryExecutionDialog>>('historyExecutionDialog');
const performanceDialog = useTemplateRef<ComponentExposed<typeof PerformanceDialog>>('performanceDialog');
const workflowScriptViewDialog = useTemplateRef<ComponentExposed<typeof WorkflowScriptViewDialog>>('workflowScriptViewDialog');
const $notify = inject(Injections.$notify);

const orig2draftMap = computed<Record<string, string>>(() => {
  const out = {};
  if (scripts.value) {
    for (const s of scripts.value) {
      if (s.workflowOriginalId) {
        out[s.workflowOriginalId] = s.workflowId;
      }
    }
  }
  return out;
})

const projectOptions = computed<Array<ProjectInfo>>(() => {
  const out = [];
  if (projects.value) {
    for (const project of Object.values(projects.value)) {
      out.push(project);
    }
  }
  return out;
});

const filteredScripts = computed<Array<WorkflowScript>>(() => {
  if (!term.value && selectedProjects.value.length == 0) {
    return scripts.value;
  }

  const out: Array<WorkflowScript> = [];

  const defaultNote = $i18n.t("Custom script").toLowerCase();

  for (const s of scripts.value) {
    if (term.value && (
        (s.scriptNote == null && !defaultNote.includes(term.value)) ||
        (s.scriptNote && !s.scriptNote.toLowerCase().includes(term.value.toLowerCase())))
    ) {
      continue;
    }

    if (selectedProjects.value.length > 0 && !s.projectKeys.some(item => selectedProjects.value.includes(item))) {
      continue;
    }

    out.push(s);
  }

  return out;
});

const functionTypeNames = computed<Record<WorkflowFunctionType, string>>(() => {
  return {
    condition: $i18n.t("Condition"),
    validator: $i18n.t("Validator"),
    postfunction: $i18n.t("Postfunction"),
  }
});

const loadScriptResults = (scriptId: string) => {
  if (resultsBusy.value[scriptId] || results.value[scriptId]) {
    return;
  }
  resultsBusy.value[scriptId] = true;
  metricService.getResults(scriptId, {page: 1, limit: 15}).then((data) => {
    results.value[scriptId] = data.data;
  }).finally(() => {
    delete resultsBusy.value[scriptId];
  })
}


const showExecutionDialog = (scriptId: string) => {
  if (results.value[scriptId]?.results?.length > 0) {
    historyExecutionDialog.value.show(results.value[scriptId].results);
  }
}

const showPerformanceDialog = (script: WorkflowScript) => {
  performanceDialog.value.show(script.id);
}

const showScriptDialog = (script: WorkflowScript) => {
  workflowScriptViewDialog.value.show(script);
}

const loadScripts = () => {
  busy.value = true;
  workflowService.getScripts().then((data) => {
    scripts.value = data.data.scripts;
    projects.value = data.data.projects;
    statuses.value = data.data.statuses;
  }).finally(() => {
    busy.value = false;
  });
}

const toggleScriptState = (script: WorkflowScript) => {
  if (busy.value) {
    return
  }

  busy.value = true;
  workflowService.setDisabled(script.workflowId, script.actionId, script.functionType, script.id, !script.disabled).then((data) => {
    const idx = scripts.value.findIndex(item => item.id === data.data.id);
    if (idx != -1) {
      scripts.value.splice(idx, 1, data.data);
    }
    $notify.ok($i18n.t('Function params updated'));
  }).catch(() => {
    $notify.error($i18n.t('Failed update function params'));
  }).finally(() => {
    busy.value = false;
  });
}

const dropDownOptions = (script: WorkflowScript): Array<DropDownGroupOption> => {
  const options: Array<DropDownOption> = [
    {
      id: 'show',
      label: $i18n.t('Show script'),
      cb() {
        showScriptDialog(script)
      },
      iconName: 'icon-eye'
    },
    {
      id: 'perf',
      label: $i18n.t('Performance chart'),
      cb() {
        showPerformanceDialog(script)
      },
      iconName: 'icon-stats-bars'
    }
  ];

  if (script.workflowOriginalId || !orig2draftMap.value[script.workflowId]) {
    options.push(
        {
          id: 'state',
          label: script.disabled ? $i18n.t('Enable') : $i18n.t('Disable'),
          cb() {
            toggleScriptState(script);
          },
          iconName: script.disabled ? 'icon-ok-circle' : 'icon-blocked'
        }
    )
  }

  return [
    {
      id: script.id,
      options,
    }
  ]
}

onMounted(() => {
  loadScripts();
});

</script>

<template>
  <div class="gravity-page gravity-page-workflows pad">
    <nav class="breadcrumbs header">
      <ol role="list">
        <li>{{ $i18n.t("Gravity") }}</li>
        <li>{{ $i18n.t("Workflows") }}</li>
      </ol>
    </nav>

    <div class="pad panel">
      <form class="ui horizontal" @submit.prevent>
        <div class="field-group">
          <label for="gravity-workflow-term">{{ $i18n.t("Filter") }}</label>
          <input type="text" v-model="term" id="gravity-workflow-term" :placeholder="$i18n.t('note...')">
        </div>
        <div class="field-group">
          <label for="gravity-workflow-project">{{ $i18n.t("Projects") }}</label>
          <MultiSelect v-model="selectedProjects" type="text" id="gravity-workflow-project" value-key="key" :options="projectOptions"></MultiSelect>
        </div>
      </form>
    </div>

    <div class="workflow-scripts-table-container">
      <table class="table workflow-scripts-table">
        <thead>
          <tr>
            <th>{{ $i18n.t("Name") }}</th>
            <th>{{ $i18n.t("Used in") }}</th>
            <th>{{ $i18n.t("Workflow") }}</th>
            <th>{{ $i18n.t("Transition") }}</th>
            <th>{{ $i18n.t("Type") }}</th>
            <th>{{ $i18n.t("History") }}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in filteredScripts" :key="s.id" :class="{disabled: s.disabled}">
            <td>{{ s.scriptNote }}</td>
            <td>
              <ProjectListComponent :projects="projects" :keys="s.projectKeys"></ProjectListComponent>
            </td>
            <td>
              {{ s.workflowName }} {{ s.workflowOriginalId? `(${$i18n.t('Draft')})`: '' }}
            </td>
            <td>
              <div class="transition">
                <div class="transition-source">
                  <template v-for="status in s.transition.sourceStatuses" :key="status">
                    <StatusComponent v-if="statuses[status]" :name="statuses[status].name" :category="statuses[status].categoryKey"></StatusComponent>
                  </template>
                </div>
                <div class="transition-name">-> <router-link :to="{name: RouteNames.admin.workflows, params: {id: s.workflowId}, query: {hl: s.actionId}}">{{ s.transition.transitionName }}</router-link> -></div>
                <div class="transition-target">
                  <StatusComponent v-if="statuses[s.transition.targetStatus]" :name="statuses[s.transition.targetStatus].name" :category="statuses[s.transition.targetStatus].categoryKey"></StatusComponent>
                </div>
              </div>
            </td>
            <td>
              {{ functionTypeNames[s.functionType] }}
            </td>
            <td v-lazy-load="() => loadScriptResults(s.id)">
              <template v-if="!s.workflowOriginalId">
                <template v-if="results[s.id]">
                  <HistoryButtonComponent @click="showExecutionDialog(s.id)" :results="results[s.id].results"></HistoryButtonComponent>
                </template>
                <BusyIconComponent v-else></BusyIconComponent>
              </template>
              <HistoryButtonComponent v-else :results="[]"></HistoryButtonComponent>
            </td>
            <td>
              <DropDownButton :options="() => dropDownOptions(s)" :toggle-icon="false" class="button button-icon button-transparent icon-dots" :cache-options="false"></DropDownButton>
            </td>
          </tr>
        </tbody>
      </table>
      <LoadOverlayComponent class="pad" v-if="busy" :absolute="true" :dim="true"></LoadOverlayComponent>
    </div>

    <HistoryExecutionDialog ref="historyExecutionDialog"></HistoryExecutionDialog>
    <PerformanceDialog ref="performanceDialog"></PerformanceDialog>
    <WorkflowScriptViewDialog ref="workflowScriptViewDialog"></WorkflowScriptViewDialog>
  </div>
</template>

<style>
  .gravity-page-workflows {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .workflow-scripts-table-container {
    position: relative;
    min-height: 200px;

    .transition {
      display: flex;
      align-items: stretch;
      gap: 10px;

      .transition-name {
        display: flex;
        align-items: center;
        flex-direction: row;
      }

      & > * {
        display: flex;
        flex-direction: column;
        gap: 5px;
      }
    }
  }
</style>