<script setup lang="ts">
import CodeEditor from "@/components/CodeEditor.vue";
import { PropType } from "vue";
import { Errors } from "luna";

defineProps({
  errors: Object as PropType<Errors>,
})

type Params = {
  script: string;
}

const value = defineModel<Params>();
</script>

<template>
  <span class="gravity-edit-function">
    <CodeEditor v-model="value.script"></CodeEditor>
  </span>
</template>

<style>

</style>