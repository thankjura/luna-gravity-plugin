<script setup lang="ts">
import { onBeforeUnmount, onMounted, type PropType, useTemplateRef, watch } from "vue";
import type * as MonacoEditorType from 'monaco-editor';
import {
  attachModel,
  detachModel,
  registerGroovyLanguageForMonaco,
  registerGroovyProviders,
  watchDiagnostics
} from "@/components/groovy";
import { loadMonacoInstance } from "@/utils/monaco";

const props = defineProps({
  disabled: Boolean,
  context: Object as PropType<Record<string, string>>
});

const value = defineModel<string>();
const container = useTemplateRef<HTMLDivElement>('container');
let editor: MonacoEditorType.editor.IStandaloneCodeEditor;
let diagnostics: MonacoEditorType.IDisposable;
let unmounted = false;

watch(value, (newValue) => {
  if (editor && newValue !== editor.getValue()) {
    editor.setValue(newValue || "");
  }
});

watch(() => props.disabled, (disabled) => {
  editor?.updateOptions({readOnly: disabled});
});

onMounted(async () => {
  const instance = await loadMonacoInstance() as typeof MonacoEditorType;
  if (unmounted) {
    return;
  }

  registerGroovyLanguageForMonaco(instance);
  registerGroovyProviders(instance);

  editor = instance.editor.create(container.value, {
    value: value.value,
    language: "groovy",
    automaticLayout: true,
    suggestOnTriggerCharacters: true,
    quickSuggestions: {other: true, comments: false, strings: false},
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
    lightbulb: {enabled: instance.editor.ShowLightbulbIconMode.OnCode},
    suggestSelection: "first",
    suggest: {
      showWords: false,
      showStatusBar: true,
      preview: true,
      snippetsPreventQuickSuggestions: false,
    },
  });

  attachModel(editor.getModel(), () => props.context);
  if (!props.disabled) {
    diagnostics = watchDiagnostics(instance, editor);
  }

  editor.onDidChangeModelContent(() => {
    value.value = editor.getValue();
  });
  editor.layout();
});

onBeforeUnmount(() => {
  unmounted = true;
  diagnostics?.dispose();
  if (editor) {
    const model = editor.getModel();
    if (model) {
      detachModel(model);
    }
    editor.dispose();
    model?.dispose();
  }
});

</script>

<template>
  <div ref="container" class="gravity-code-editor" @click.stop @mousedown.stop></div>
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