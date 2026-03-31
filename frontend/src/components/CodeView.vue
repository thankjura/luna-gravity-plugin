<script setup lang="ts">
import { onMounted, useTemplateRef, watch } from "vue";
import type * as MonacoEditorType from 'monaco-editor';
import { loadMonacoInstance } from "@/utils/monaco.ts";
import { registerGroovyLanguageForMonaco } from "@/components/groovy.ts";

const props = defineProps({
  script: String
})

const container = useTemplateRef<HTMLDivElement>('container');
let instance: typeof MonacoEditorType;

watch(() => props.script, (newValue) => {
  if (instance) {
    container.value.innerHTML = newValue;
    instance.editor.colorizeElement(container.value, {
      mimeType: "groovy",
    });
  }
});

onMounted(async () => {
  instance = await loadMonacoInstance() as typeof MonacoEditorType;
  registerGroovyLanguageForMonaco(instance);

  await instance.editor.colorizeElement(container.value, {
    mimeType: "groovy",
  });
});


</script>

<template>
  <div class="code-viewer" ref="container">{{ script }}</div>
</template>

<style>

</style>