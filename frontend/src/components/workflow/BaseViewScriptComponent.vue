<script setup lang="ts">

import CodeView from "@/components/CodeView.vue";
import { type PropType } from "vue";
import { type WorkflowActionFunction } from "luna";
import { $i18n } from "@/utils/i18n";

defineProps({
  func: Object as PropType<WorkflowActionFunction>,
})

</script>

<template>
  <div class="gravity-view-function-container">
    <div class="note" v-if="func?.viewParams?.note">{{ func.viewParams['note'] }}</div>
    <div class="gravity-view-file iconed" v-if="func?.viewParams?.source === 'file'" :title="$i18n.t('Script file')">
      <span class="icon-file"></span> {{ func.viewParams['file'] }}
    </div>
    <div class="error" v-if="func?.viewParams?.fileError">{{ func.viewParams['fileError'] }}</div>
    <div class="gravity-view-function">
      <CodeView class="code-viewer" :script="func?.viewParams?.script" max-height="100px"></CodeView>
    </div>
  </div>
</template>

<style>
  .gravity-view-function-container {
    .gravity-view-file {
      font-family: monospace;
      color: var(--muted-text-color);
      margin-bottom: 4px;
    }

    .gravity-view-function {
      width: 100%;
    }
  }
</style>