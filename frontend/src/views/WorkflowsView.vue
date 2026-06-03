<script setup lang="ts">
import { $i18n } from "@/utils/i18n.ts";
import { WorkflowScript } from "@/interfaces/workflow.ts";
import { onMounted, ref } from "vue";
import { workflowService } from "@/services/workflowService.ts";
import {LoadOverlayComponent} from 'luna';

const busy = ref(false);
const scripts = ref<Array<WorkflowScript>>([]);

const loadScripts = () => {
  busy.value = true;
  workflowService.getScripts().then((data) => {
    scripts.value = data.data;
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