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
    <CodeView class="code-viewer" :script="func?.viewParams?.script" max-height="100px"></CodeView>
  </span>
</template>

<style>
  .gravity-view-function {
    width: 100%;
  }
</style>