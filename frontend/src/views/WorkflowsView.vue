<script setup lang="ts">
import { $i18n } from "@/utils/i18n.ts";
import { WorkflowScript } from "@/interfaces/workflow.ts";
import { onMounted, ref } from "vue";
import { workflowService } from "@/services/workflowService.ts";
import { LoadOverlayComponent, ProjectInfo } from 'luna';

const busy = ref(false);
const scripts = ref<Array<WorkflowScript>>([]);
const projects = ref<Record<string, ProjectInfo>>({});

const loadScripts = () => {
  busy.value = true;
  workflowService.getScripts().then((data) => {
    scripts.value = data.data.scripts;
    projects.value = data.data.projects;
  }).finally(() => {
    busy.value = false;
  });
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

    <div class="pad panel workflow-scripts-list">
      <div class="workflow-script" v-for="s in scripts">
        {{ s.workflowName }}
      </div>

      <LoadOverlayComponent v-if="busy"></LoadOverlayComponent>
    </div>

  </div>
</template>

<style>
  .workflow-scripts-list {
    position: relative;
    min-height: 100px;
    width: 100%;
  }
</style>