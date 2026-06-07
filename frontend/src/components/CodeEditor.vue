<script setup lang="ts">
import { onBeforeUnmount, onMounted, PropType, useTemplateRef, watch } from "vue";
import type * as MonacoEditorType from 'monaco-editor';
import {
  registerAutoCompleteService,
  registerGroovyLanguageForMonaco,
  registerSignatureHelpProvider
} from "@/components/groovy.ts";
import { loadMonacoInstance } from "@/utils/monaco.ts";

const props = defineProps({
  disabled: Boolean,
  context: Object as PropType<Record<string, string>>,
});

const value = defineModel<string>();
const container = useTemplateRef<HTMLDivElement>('container');
let editor: MonacoEditorType.editor.IStandaloneCodeEditor;
let completionProvider: MonacoEditorType.IDisposable;
let signatureProvider: MonacoEditorType.IDisposable;

watch(value, (newValue) => {
  if (editor && newValue !== editor.getValue()) {
    editor.setValue(newValue || "");
  }
});

onMounted(async () => {
  const instance = await loadMonacoInstance();
  if (!completionProvider) {
    completionProvider = registerAutoCompleteService(instance as typeof MonacoEditorType, () => props.context);
  }
  if (!signatureProvider) {
    signatureProvider = registerSignatureHelpProvider(instance as typeof MonacoEditorType, () => props.context);
  }

  registerGroovyLanguageForMonaco(instance as typeof MonacoEditorType);

  editor = instance.editor.create(container.value, {
    value: value.value,
    language: "groovy",
    automaticLayout: true,
    suggestOnTriggerCharacters: true,
    scrollBeyondLastLine: false,
    readOnly: props.disabled,
    scrollbar: {
      vertical: 'auto',
      handleMouseWheel: true
    },
    theme: "vs-dark",
    padding: {
      top: 10,
      bottom: 10
    },
    //fixedOverflowWidgets: true,
    suggest: {
      showWords: false
    },
  });

  editor.onDidChangeModelContent(() => {
    value.value = editor.getValue();
  });
  editor.layout();
});

onBeforeUnmount(() => {
  if (editor) {
    editor.dispose();
  }

  if (completionProvider) {
    completionProvider.dispose();
    completionProvider = null;
  }

  if (signatureProvider) {
    signatureProvider.dispose();
    signatureProvider = null;
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
    min-height: 400px;
    border: 1px solid #ccc;
    text-align: left;
    position: relative;

    .monaco-editor {
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