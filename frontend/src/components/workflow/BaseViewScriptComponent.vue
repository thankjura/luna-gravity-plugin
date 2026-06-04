<script setup lang="ts">

import CodeView from "@/components/CodeView.vue";
import { PropType } from "vue";
import { WorkflowActionFunction } from "luna";

defineProps({
  func: Object as PropType<WorkflowActionFunction>,
})

</script>

<template>
  <div class="gravity-view-function-container">
    <div class="note" v-if="func?.viewParams?.note">{{ func.viewParams['note'] }}</div>
    <div class="gravity-view-function">
      <CodeView class="code-viewer" :script="func?.viewParams?.script" max-height="100px"></CodeView>
    </div>
  </div>
</template>

<style>
  .gravity-view-function-container {
    .gravity-view-function {
      width: 100%;
    }
  }
</style>