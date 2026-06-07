<script setup lang="ts">
import { BaseDialog } from "luna";
import { WorkflowScript } from "@/interfaces/workflow.ts";
import { ref, useTemplateRef } from "vue";
import { $i18n } from "@/utils/i18n.ts";
import { ComponentExposed } from "vue-component-type-helpers";
import CodeEditor from "@/components/CodeEditor.vue";

const workflowScript = ref<WorkflowScript>(null);
const dialog = useTemplateRef<ComponentExposed<typeof BaseDialog>>('dialog');

const show = (script: WorkflowScript) => {
  workflowScript.value = script;
  dialog.value.show();
}

defineExpose({
  show
});

</script>

<template>
  <BaseDialog ref="dialog" class="large workflow-script-view-dialog">
    <template v-slot:header>{{ workflowScript?.scriptNote? workflowScript.scriptNote: $i18n.t("Workflow script") }}</template>
    <template v-slot:default>
      <CodeEditor class="script-viewer" :disabled="true" :model-value="workflowScript.script" v-if="workflowScript"></CodeEditor>
    </template>
  </BaseDialog>
</template>

<style>
  .workflow-script-view-dialog {
    .script-viewer {
      .code-viewer {
        border-radius: 0;
        min-height: 400px;
      }
    }
  }
</style>