<script setup lang="ts">
import CodeEditor from "@/components/CodeEditor.vue";
import { ref } from "vue";
import { ButtonBusy } from "luna";
import { ScriptResult } from "@/interfaces/script.ts";
import { scriptService } from "@/services/scriptService.ts";

const value = ref<string>(null)
const busy = ref(false);
const result = ref<ScriptResult>(null);

const runScript = () => {
  if (!value.value) {
    return;
  }

  busy.value = true;
  scriptService.runScript(value.value).then((data) => {
    result.value = data.data
  }).catch((e) => {
    console.log(e);
  }).finally(() => {
    busy.value = false;
  })
}

</script>

<template>
  <div class="demo-page pad">
    <h3>Demo page</h3>
    <CodeEditor v-model="value"></CodeEditor>

    <ButtonBusy :busy="busy" :disabled="busy" @click="runScript">Выполнить</ButtonBusy>
    <span v-if="result">{{ result.result }}</span>
  </div>
</template>

<style>

</style>
