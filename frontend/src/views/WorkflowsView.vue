<script setup lang="ts">
import { $i18n } from "@/utils/i18n.ts";
import { WorkflowScript } from "@/interfaces/workflow.ts";
import { computed, onMounted, ref } from "vue";
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
} from 'luna';
import { ScriptRunResult } from "@/interfaces/metrics.ts";
import { vLazyLoad } from "@/directives/LazyLoad.ts";
import { metricService } from "@/services/metricService.ts";
import HistoryButtonComponent from "@/components/metrics/HistoryButtonComponent.vue";

const busy = ref(false);
const scripts = ref<Array<WorkflowScript>>([]);
const projects = ref<Record<string, ProjectInfo>>({});
const statuses = ref<Record<number, Status>>({});
const term = ref<string>(null);
const selectedProjects = ref<Array<string>>([]);
const results = ref<Record<string, SearchResult<ScriptRunResult>>>({});
const resultsBusy = ref<Record<string, boolean>>({});

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

const getResultSummary = (result: SearchResult<ScriptRunResult>) => {
  const fails = result.results.filter(sr => sr.exception).length;
  const total = result.results.length;
  return {
    icon: fails > 0? 'icon-cancel-circle' : 'icon-ok-circle',
    summary: fails > 0 ? $i18n.t("Has {0} failures in the last {0} executions", fails, total) : $i18n.t("No failures in the last {0} executions", total),
  }
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
          <MultiSelect v-model="selectedProjects" type="text" id="gravity-workflow-project" :options="projectOptions"></MultiSelect>
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
            <th>{{ $i18n.t("Performance") }}</th>
            <th>{{ $i18n.t("History") }}</th>
            <th>{{ $i18n.t("Actions") }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in filteredScripts" :key="s.id">
            <td>{{ s.scriptNote }}</td>
            <td>
              <div class="project-list">
                <span class="badget" v-for="p in s.projectKeys.slice(0, 3)">{{ p }}</span>
                <button type="button" v-if="s.projectKeys.length > 3" class="button-icon icon-dots" :title="$i18n.t('More')"></button>
              </div>
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
                <div class="transition-name">-> {{ s.transition.transitionName }} -></div>
                <div class="transition-target">
                  <StatusComponent v-if="statuses[s.transition.targetStatus]" :name="statuses[s.transition.targetStatus].name" :category="statuses[s.transition.targetStatus].categoryKey"></StatusComponent>
                </div>
              </div>
            </td>
            <td>
              {{ functionTypeNames[s.functionType] }}
            </td>
            <td>
              <button type="button" class="button-icon button-transparent icon-stats-bars" :title="$i18n.t('Show performance')"></button>
            </td>
            <td v-lazy-load="() => loadScriptResults(s.id)">
              <template v-if="!s.workflowOriginalId">
                <template v-if="results[s.id]">
                  <HistoryButtonComponent :results="results[s.id].results"></HistoryButtonComponent>
                </template>
                <BusyIconComponent v-else></BusyIconComponent>
              </template>
              <HistoryButtonComponent v-else :results="[]"></HistoryButtonComponent>
            </td>
            <td>
              <button type="button" class="button-icon button-transparent icon-dots"></button>
            </td>
          </tr>
        </tbody>
      </table>
      <LoadOverlayComponent class="pad" v-if="busy" :absolute="true" :dim="true"></LoadOverlayComponent>
    </div>

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

    .project-list {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }

    .transition {
      display: flex;
      align-items: stretch;
      gap: 10px;

      & > * {
        display: flex;
        flex-direction: column;
        gap: 5px;
      }
    }
  }
</style>