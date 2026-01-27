<script setup lang="ts">
import { onBeforeUnmount, onMounted, useTemplateRef, watch } from "vue";
import { EditorView, basicSetup } from "codemirror";
import { java } from '@codemirror/lang-java';
import { vscodeDark } from '@uiw/codemirror-theme-vscode';

const value = defineModel<string>();
const container = useTemplateRef<HTMLDivElement>('container');
let editor: EditorView;

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
      })
    ],
    parent: container.value
  });
});

watch(value, (newVal) => {
  if (editor && newVal !== editor.state.doc.toString()) {
    editor.dispatch({
      changes: { from: 0, to: editor.state.doc.length, insert: newVal }
    });
  }
});

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy();
  }
});

</script>

<template>
  <div ref="container"></div>
</template>

<style>

</style>