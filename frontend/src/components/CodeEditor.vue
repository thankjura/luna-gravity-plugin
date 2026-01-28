<script setup lang="ts">
import { onBeforeUnmount, onMounted, useTemplateRef, watch } from "vue";
import { EditorView, basicSetup } from "codemirror";
import { java } from '@codemirror/lang-java';
import { vscodeDark } from '@uiw/codemirror-theme-vscode';
import { autocompletion } from "@codemirror/autocomplete";
import { scriptService } from "@/services/scriptService.ts";

defineProps({
  disabled: Boolean
})

const value = defineModel<string>();
const container = useTemplateRef<HTMLDivElement>('container');
let editor: EditorView;

watch(value, (newVal) => {
  if (editor && newVal !== editor.state.doc.toString()) {
    editor.dispatch({
      changes: { from: 0, to: editor.state.doc.length, insert: newVal }
    });
  }
});

onMounted(() => {
  editor = new EditorView({
    doc: value.value,
    extensions: [
      basicSetup,
      java(),            // Подсветка Groovy/Java
      vscodeDark,        // Тема оформления
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          value.value = update.state.doc.toString();
        }
      }),
      autocompletion({ override: [scriptService.groovyCompletionSource]}),
    ],
    parent: container.value
  });
});

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy();
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
  }
</style>