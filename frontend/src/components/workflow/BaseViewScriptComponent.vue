<script setup lang="ts">

import CodeView from "@/components/CodeView.vue";
import { PropType } from "vue";
import { WorkflowActionFunction } from "luna";

defineProps({
  func: Object as PropType<WorkflowActionFunction>,
})

</script>

<template>
  <span class="gravity-view-function">
    <CodeView :script="func?.viewParams?.script"></CodeView>
  </span>
</template>

<style>
  .gravity-view-function {
    width: 100%;

    .code-viewer {
      min-height: 200px;
    }
  }
</style>