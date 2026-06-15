<script setup lang="ts">
import { $i18n } from "@/utils/i18n.ts";
import { inject, onMounted, ref } from "vue";
import {
  ProjectInfo,
  IssueEventType,
  Errors,
  MultiSelect,
  ButtonBusy,
  Injections,
  ToggleComponent,
} from 'luna';
import { ListenerScript } from "@/interfaces/listener.ts";
import { listenerService } from "@/services/listenerService.ts";
import { useRoute, useRouter } from "vue-router";
import { systemService } from "@/services/systemService.ts";
import CodeEditor from "@/components/CodeEditor.vue";

const busy = ref(false);
const script = ref<Partial<ListenerScript>>({});
const projects = ref<Array<ProjectInfo>>([]);
const eventTypes = ref<Array<IssueEventType>>([]);
const $notify = inject(Injections.$notify);
const route = useRoute();
const router = useRouter();
const scriptName = ref<string>();
const errors = ref<Errors>({});

const loadScript = (scriptId: string) => {
  if (scriptId == "create") {
    script.value = {};
    scriptName.value = $i18n.t("New listener script");
    return;
  }
  const id = Number(scriptId);
  if (!id) {
    router.push("/admin/gravity/listeners");
    return;
  }

  busy.value = true;
  listenerService.get(id).then((data) => {
    script.value = data.data;
    projects.value = data.data.projects;
    scriptName.value = $i18n.t("Edit listener script");
  }).finally(() => {
    busy.value = false;
  });
}

const submit = () => {
  busy.value = true;
  if (script.value.id) {
    listenerService.patch(script.value.id, script.value).then((data) => {
      script.value = data.data;
      projects.value = data.data.projects;
      scriptName.value = $i18n.t("Edit listener script");
      errors.value = {};
      $notify.ok($i18n.t("Listener updated successfully."));
    }).catch((e) => {
      if (e.data?.errors) {
        errors.value = e.data?.errors;
      }
      $notify.error($i18n.t("Failed to update listener script"));
    }).finally(() => {
      busy.value = false;
    });
  } else {
    listenerService.create(script.value).then((data) => {
      script.value = data.data;
      projects.value = data.data.projects;
      scriptName.value = $i18n.t("Edit listener script");
      errors.value = {};
      router.replace({name: "gravityListeners", params: {id: data.data.id}});
      $notify.ok($i18n.t("Listener created successfully."));
    }).catch((e) => {
      if (e.data?.errors) {
        errors.value = e.data?.errors;
      }
      $notify.error($i18n.t("Failed to create listener script"));
    }).finally(() => {
      busy.value = false;
    });
  }
}

const projectOptions = (term: string|null, excludes?: Array<number>) => {
  return systemService.projectSuggestions(term, excludes);
}

onMounted(() => {
  systemService.getEventTypes().then((data) => {
    eventTypes.value = data.data;
  })
  loadScript(route.params.id as string);
});

</script>

<template>
  <div class="gravity-page gravity-page-listener-editor pad">
    <nav class="breadcrumbs header">
      <ol role="list">
        <li>{{ $i18n.t("Gravity") }}</li>
        <li><router-link to="/admin/gravity/listeners">{{ $i18n.t("Listeners") }}</router-link></li>
        <li>{{ scriptName }}</li>
      </ol>
    </nav>

    <div class="panel pad">
      <form class="ui pad compact">
        <div class="field-group">
          <label for="script-name">{{ $i18n.t("Name") }}</label>
          <input :disabled="busy" type="text" id="script-name" v-model="script.name" />
          <div class="error" v-if="errors['name']">{{ errors['name'] }}</div>
        </div>
        <div class="field-group">
          <label for="script-description">{{ $i18n.t("Description") }}</label>
          <textarea :disabled="busy" class="textarea" id="script-description" v-model="script.description" />
        </div>
        <div class="field-group">
          <label for="script-projects">{{ $i18n.t("Projects") }}</label>
          <MultiSelect :disabled="busy" :model-data="projects" id="script-projects" v-model="script.projectIds" :options="projectOptions" :show-icons="true"></MultiSelect>
          <div class="description">{{ $i18n.t("Leave blank for all projects") }}</div>
          <div class="error" v-if="errors['projectIds']">{{ errors['projectIds'] }}</div>
        </div>
        <div class="field-group">
          <label for="script-event-types">{{ $i18n.t("EventTypes") }}</label>
          <MultiSelect :disabled="busy" id="script-event-types" v-model="script.eventTypeIds" :options="eventTypes"></MultiSelect>
          <div class="error" v-if="errors['eventTypeIds']">{{ errors['eventTypeIds'] }}</div>
        </div>
        <div class="field-group">
          <label for="script-async">{{ $i18n.t("Async") }}</label>
          <ToggleComponent :disabled="busy" id="script-async" v-model="script.async"></ToggleComponent>
        </div>
        <div class="field-group">
          <strong class="label">{{ $i18n.t("Script") }}</strong>
          <CodeEditor v-model="script.script" :context="{__context__: 'listener'}" :style="{maxWidth: '100%'}"></CodeEditor>
        </div>

        <div>
          <router-link class="button" :to="{name: 'gravityListeners'}">{{ $i18n.t("Cancel") }}</router-link>
          <ButtonBusy :disabled="busy" :busy="busy" @click="submit" class="primary">
            <template v-if="script.id">
              {{ $i18n.t("Save") }}
            </template>
            <template v-else>
              {{ $i18n.t("Create") }}
            </template>
          </ButtonBusy>
        </div>
      </form>
    </div>

  </div>
</template>

<style>
  .gravity-page-listener-editor {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
</style>