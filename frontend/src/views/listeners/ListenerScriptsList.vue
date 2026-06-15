<script setup lang="ts">
import { $i18n } from "@/utils/i18n.ts";
import { computed, inject, onMounted, ref, useTemplateRef } from "vue";
import {
  LoadOverlayComponent,
  ProjectInfo,
  MultiSelect,
  SearchResult,
  BusyIconComponent,
  DropDownGroupOption,
  DropDownButton, DropDownOption, Injections, IssueEventType,
} from 'luna';
import { ScriptRunResult } from "@/interfaces/metrics.ts";
import { vLazyLoad } from "@/directives/LazyLoad.ts";
import { metricService } from "@/services/metricService.ts";
import HistoryButtonComponent from "@/components/metrics/HistoryButtonComponent.vue";
import HistoryExecutionDialog from "@/components/metrics/HistoryExecutionDialog.vue";
import { ComponentExposed } from "vue-component-type-helpers";
import ProjectListComponent from "@/components/workflow/ProjectListComponent.vue";
import PerformanceDialog from "@/components/metrics/PerformanceDialog.vue";
import { ListenerScript } from "@/interfaces/listener.ts";
import { listenerService } from "@/services/listenerService.ts";
import DeleteListenerScriptDialog from "@/components/workflow/DeleteListenerScriptDialog.vue";

const busy = ref(false);
const scripts = ref<Array<ListenerScript>>([]);
const projects = ref<Array<ProjectInfo>>([]);
const eventTypes = ref<Array<IssueEventType>>([]);
const term = ref<string>(null);
const selectedProjects = ref<Array<number>>([]);
const selectedEventTypes = ref<Array<number>>([]);
const results = ref<Record<number, SearchResult<ScriptRunResult>>>({});
const resultsBusy = ref<Record<string, boolean>>({});
const historyExecutionDialog = useTemplateRef<ComponentExposed<typeof HistoryExecutionDialog>>('historyExecutionDialog');
const performanceDialog = useTemplateRef<ComponentExposed<typeof PerformanceDialog>>('performanceDialog');
const deleteDialog = useTemplateRef<ComponentExposed<typeof DeleteListenerScriptDialog>>('deleteDialog');
const $notify = inject(Injections.$notify);

const eventTypesMap = computed<Record<number, IssueEventType>>(() => {
  const out = {};
  if (eventTypes.value) {
    for (const eventType of eventTypes.value) {
      out[eventType.id] = eventType;
    }
  }
  return out;
});

const filteredScripts = computed<Array<ListenerScript>>(() => {
  if (!term.value && selectedProjects.value.length == 0) {
    return scripts.value;
  }

  const out: Array<ListenerScript> = [];

  for (const s of scripts.value) {
    if (term.value && (
        (s.name != null && !s.name.toLowerCase().includes(term.value.toLowerCase())) &&
        (s.description != null && !s.description.toLowerCase().includes(term.value.toLowerCase()))
    )) {
      continue;
    }

    if (selectedProjects.value.length > 0 && !s.projectIds.some(item => selectedProjects.value.includes(item))) {
      continue;
    }

    if (selectedEventTypes.value.length > 0 && !s.eventTypeIds.some(item => selectedEventTypes.value.includes(item))) {
      continue;
    }

    out.push(s);
  }

  return out;
});

const loadScriptResults = (scriptId: number) => {
  if (resultsBusy.value[scriptId] || results.value[scriptId]) {
    return;
  }
  resultsBusy.value[scriptId] = true;
  metricService.getResults("listener-" + scriptId, {page: 1, limit: 15}).then((data) => {
    results.value[scriptId] = data.data;
  }).finally(() => {
    delete resultsBusy.value[scriptId];
  })
}

const showExecutionDialog = (scriptId: number) => {
  if (results.value[scriptId]?.results?.length > 0) {
    historyExecutionDialog.value.show(results.value[scriptId].results);
  }
}

const showPerformanceDialog = (script: ListenerScript) => {
  performanceDialog.value.show("listener-" + script.id);
}

const loadScripts = () => {
  busy.value = true;
  listenerService.getAll().then((data) => {
    scripts.value = data.data.scripts;
    projects.value = data.data.projects;
    eventTypes.value = data.data.eventTypes;
  }).finally(() => {
    busy.value = false;
  });
}

const showScriptDialog = (script: ListenerScript) => {
  console.log(script);
}

const showDeleteDialog = (script: ListenerScript) => {
  deleteDialog.value.show(script);
}

const toggleScriptState = (script: ListenerScript) => {
  if (busy.value) {
    return
  }

  busy.value = true;
  listenerService.patch(script.id, {enabled: !script.enabled}).then((data) => {
    const idx = scripts.value.findIndex(item => item.id === data.data.id);
    if (idx != -1) {
      scripts.value.splice(idx, 1, data.data);
    }
    if (script.enabled) {
      $notify.ok($i18n.t('Listener script enabled successfully'));
    } else {
      $notify.ok($i18n.t('Listener script disabled successfully'));
    }
  }).catch(() => {
    $notify.error($i18n.t('Failed update script params'));
  }).finally(() => {
    busy.value = false;
  });
}

const onDeleteScript = (scriptId: number) => {
  const idx = scripts.value.findIndex(item => item.id === scriptId);
  if (idx != -1) {
    scripts.value.splice(idx, 1);
  }
}

const dropDownOptions = (script: ListenerScript): Array<DropDownGroupOption> => {
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
    },
    {
      id: 'state',
      label: script.enabled ? $i18n.t('Disable') : $i18n.t('Enable'),
      cb() {
        toggleScriptState(script);
      },
      iconName: script.enabled ? 'icon-blocked' : 'icon-ok-circle'
    },
    {
      id: 'delete',
      label: $i18n.t('Delete'),
      cb() {
        showDeleteDialog(script);
      },
      iconName: 'icon-bin'
    }
  ];

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
  <div class="gravity-page gravity-page-listeners pad">
    <nav class="breadcrumbs header">
      <ol role="list">
        <li>{{ $i18n.t("Gravity") }}</li>
        <li>{{ $i18n.t("Listeners") }}</li>
      </ol>
      <div class="actions">
        <router-link :to="{name: 'gravityListeners', params: {id: 'create'}}" class="button">{{ $i18n.t("Create listener") }}</router-link>
      </div>
    </nav>

    <div class="pad panel">
      <form class="ui horizontal" @submit.prevent>
        <div class="field-group">
          <label for="gravity-workflow-term">{{ $i18n.t("Filter") }}</label>
          <input type="text" v-model="term" id="gravity-workflow-term" :placeholder="$i18n.t('note...')">
        </div>
        <div class="field-group">
          <label for="gravity-listener-project">{{ $i18n.t("Projects") }}</label>
          <MultiSelect v-model="selectedProjects" type="text" id="gravity-listener-project" :options="projects"></MultiSelect>
        </div>
        <div class="field-group">
          <label for="gravity-listener-event-type">{{ $i18n.t("Event types") }}</label>
          <MultiSelect v-model="selectedEventTypes" type="text" id="gravity-listener-event-type" :options="eventTypes"></MultiSelect>
        </div>
      </form>
    </div>

    <div class="listener-scripts-table-container">
      <table class="table workflow-scripts-table">
        <thead>
          <tr>
            <th>{{ $i18n.t("Name") }}</th>
            <th>{{ $i18n.t("Used in") }}</th>
            <th>{{ $i18n.t("Event type") }}</th>
            <th>{{ $i18n.t("History") }}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in filteredScripts" :key="s.id" :class="{disabled: !s.enabled}">
            <td>
              <div :title="s.description">{{ s.name }}</div>
            </td>
            <td>
              <ProjectListComponent :projects="projects" :keys="s.projectIds"></ProjectListComponent>
            </td>
            <td>
              <template v-for="eventTypeId in s.eventTypeIds">
                <div v-if="eventTypesMap[eventTypeId]" class="event-type">
                  {{ eventTypesMap[eventTypeId].name }}
                </div>
              </template>
            </td>
            <td v-lazy-load="() => loadScriptResults(s.id)">
              <template v-if="results[s.id]">
                <HistoryButtonComponent @click="showExecutionDialog(s.id)" :results="results[s.id].results"></HistoryButtonComponent>
              </template>
              <BusyIconComponent v-else></BusyIconComponent>
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
    <DeleteListenerScriptDialog ref="deleteDialog" @deleted="onDeleteScript"></DeleteListenerScriptDialog>
  </div>
</template>

<style>
  .gravity-page-listeners {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .listener-scripts-table-container {
    position: relative;
    min-height: 200px;
  }
</style>