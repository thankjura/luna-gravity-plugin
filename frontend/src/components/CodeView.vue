<script setup lang="ts">
import { computed, nextTick, onMounted, ref, useTemplateRef, watch } from "vue";
import type * as MonacoEditorType from 'monaco-editor';
import { loadMonacoInstance } from "@/utils/monaco.ts";
import { registerGroovyLanguageForMonaco } from "@/components/groovy.ts";

const props = defineProps({
  script: String,
  maxHeight: {
    type: String,
    default: "unset",
  },
})

const container = useTemplateRef<HTMLDivElement>('container');
let instance: typeof MonacoEditorType;
const expanded = ref(false);
const needExpander = ref(false);
const contentHeight = ref('0px');

const currentMaxHeight = computed(() => {
  return expanded.value ? contentHeight.value : props.maxHeight;
});

const colorize = () => {
  return instance.editor.colorizeElement(container.value, {
    mimeType: "groovy",
    theme: "vs-dark",
  });
}

watch(() => props.script, (newValue) => {
  if (instance) {
    container.value.innerHTML = newValue;
    colorize().then(() => {
      checkTextHeight();
    });
  }
});

const toggleExpand = () => {
  if (expanded.value) {
    expanded.value = false;
  } else {
    if (container.value) {
      contentHeight.value = `${container.value.scrollHeight}px`;
    }
    expanded.value = true;
  }
};

const checkTextHeight = async () => {
  await nextTick();
  if (!container.value) return;

  const realHeight = container.value.scrollHeight;
  const limitHeight = parseInt(props.maxHeight, 10);

  needExpander.value = props.maxHeight !== 'unset' && realHeight > limitHeight;
};

onMounted(async () => {
  instance = await loadMonacoInstance() as typeof MonacoEditorType;
  registerGroovyLanguageForMonaco(instance);

  await colorize();
  await checkTextHeight();
});

</script>

<template>
  <div class="code-viewer-wrapper" :class="{expanded, fit: !needExpander}">
    <div class="code-viewer" ref="container">{{ script }}</div>

    <div v-if="needExpander" class="controls-panel">
      <button class="expand-button" @click="toggleExpand"></button>
    </div>
  </div>
</template>

<style scoped>
  .code-viewer-wrapper {
    max-height: v-bind(currentMaxHeight);
    position: relative;
    overflow: hidden;
    mask-image: linear-gradient(to bottom, black 75%, transparent 100%);
    transition: max-height 0.3s ease-in-out;

    &.expanded, &.fit {
      mask-image: none;
    }

    .expand-button {
      background: none;
      border: none;
      outline: none;
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;

      height: 20px;

      &:hover, &:active, &:focus {
        outline: none;
        border: none;
        background: none;
      }
    }

    .code-viewer {
      background: black;
      border-radius: 5px;
      padding: 5px 10px;

      white-space: pre-wrap;
      word-break: break-all;
      font-family: monospace;
    }
  }
</style>