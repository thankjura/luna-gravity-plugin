<script setup lang="ts">
import { onBeforeUnmount, onMounted, useTemplateRef, watch } from "vue";
import type * as MonacoEditorType from 'monaco-editor';
import { registerAutoCompleteService, registerGroovyLanguageForMonaco } from "@/components/groovy.ts";
import { loadMonacoInstance } from "@/utils/monaco.ts";

defineProps({
  disabled: Boolean
})

const value = defineModel<string>();
const container = useTemplateRef<HTMLDivElement>('container');
let editor: MonacoEditorType.editor.IStandaloneCodeEditor;
let completionProvider: MonacoEditorType.IDisposable;

watch(value, (newValue) => {
  if (editor && newValue !== editor.getValue()) {
    editor.setValue(newValue || "");
  }
});

onMounted(async () => {
  const instance = await loadMonacoInstance();
  if (!completionProvider) {
    completionProvider = registerAutoCompleteService(instance as typeof MonacoEditorType);
  }
  registerGroovyLanguageForMonaco(instance as typeof MonacoEditorType);

  editor = instance.editor.create(container.value, {
    value: value.value,
    language: "groovy",
    automaticLayout: true,
    suggestOnTriggerCharacters: true,
    theme: "vs-dark",
    fixedOverflowWidgets: true,
    suggest: {
      showWords: false
    }
  });

  editor.onDidChangeModelContent(() => {
    value.value = editor.getValue();
  });
});

onBeforeUnmount(() => {
  if (editor) {
    editor.dispose();
  }

  if (completionProvider) {
    completionProvider.dispose();
    completionProvider = null;
  }
});

</script>

<template>
  <div ref="container" class="gravity-code-editor"></div>
</template>

<style>
  .gravity-code-editor {
    border-radius: 6px;
    overflow: hidden;
    height: 400px;
    border: 1px solid #ccc;
    text-align: left;

    .monaco-editor {
      padding: 10px 0;

      .suggest-widget.message {
        height: auto!important;
        width: 255px!important;
        .message {
          padding-left: 45px;
        }
      }
    }
  }
</style>