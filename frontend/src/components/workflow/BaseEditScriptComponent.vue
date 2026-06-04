<script setup lang="ts">
import CodeEditor from "@/components/CodeEditor.vue";
import { computed, inject, PropType, Ref } from "vue";
import { Errors, WorkflowFunctionType } from "luna";
import { $i18n } from "@/utils/i18n.ts";

defineProps({
  errors: Object as PropType<Errors>,
})

type Params = {
  script: string,
  note: string,
}

const context = computed(() => {
  if (funcType?.value == 'condition') {
    return {
      __context__: "workflowCondition",
    }
  } else if (funcType?.value == 'validator') {
    return {
      __context__: "workflowValidator",
    }
  } else if (funcType?.value == 'postfunction') {
    return {
      __context__: "workflowPostFunction",
    }
  }
});

const value = defineModel<Params>();
const funcType = inject<Ref<WorkflowFunctionType>>('funcType')

</script>

<template>
  <div class="gravity-edit-function">
    <div class="gravity-field-group">
      <label for="note">{{ $i18n.t('Note') }}</label>
      <input type="text" v-model="value.note" :placeholder="$i18n.t('optional script description...')">
    </div>
    <CodeEditor class="function-code-editor" v-model="value.script" :context="context"></CodeEditor>
  </div>
</template>

<style>
  .gravity-edit-function {
    padding: 10px;
    display: flex;
    gap: 20px;
    flex-direction: column;

    .gravity-field-group {
      display: flex;
      gap: 20px;
      align-items: center;
    }

    .function-code-editor {
      flex-shrink: 0;
      height: 100%;
      min-height: 400px;
    }
  }
</style>