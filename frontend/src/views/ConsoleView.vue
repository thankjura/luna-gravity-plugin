<script setup lang="ts">
import CodeEditor from "@/components/CodeEditor.vue";
import { inject, ref } from "vue";
import { ButtonBusy, NotifyComponentInterface, Errors } from "luna";
import { scriptService } from "@/services/scriptService.ts";
import { $i18n } from "@/utils/i18n.ts";

const value = ref<string>("System.out.println(\"Hello\");\nlog.info(\"hello world\")");
const busy = ref(false);
const out = ref<Array<string>>([]);
const activeTab = ref<'log'|'result'>('result');
const result = ref<string>(null);
const $notify = inject<NotifyComponentInterface>('$notify');
const LIMIT = 300;
const errors = ref<Errors>({});

const onResultFetch = (resp: {log?: string, result?: string, error?: any}) => {
  //console.log(resp);
  if (resp.log) {
    out.value.push(resp.log);
  }

  if (out.value.length > LIMIT) {
    out.value.shift();
  }

  if (resp.result != undefined) {
    result.value = resp.result;
    busy.value = false;
    activeTab.value = 'result';
  }

  if (resp.error) {
    busy.value = false;
    $notify.error($i18n.t("Failed to execute script"));
    if (typeof resp.error == 'string') {
      try {
        const data = JSON.parse(resp.error);
        if (data.errors) {
          errors.value = data.errors;
        }
      } catch (e) {}
    }
  }
}

const runScript = () => {
  if (!value.value) {
    return;
  }
  result.value = null;
  out.value = [];
  activeTab.value = 'log';
  busy.value = true;
  scriptService.runScript(value.value, onResultFetch);
}

</script>

<template>
  <div class="gravity-page gravity-page-console pad">
    <nav class="breadcrumbs header">
      <ol role="list">
        <li>Gravity</li>
        <li>Console</li>
      </ol>
    </nav>
    <div class="pad panel editor-panel">
      <CodeEditor v-model="value" :diabled="busy"></CodeEditor>
      <ButtonBusy :busy="busy" :disabled="busy" @click="runScript">{{ $i18n.t("Execute") }}</ButtonBusy>
    </div>


    <div class="tabs-header no-border">
      <ul>
        <li :class="{active: activeTab == 'result'}">
          <button type="button" @click="activeTab='result'">Result</button>
        </li>
        <li :class="{active: activeTab == 'log'}">
          <button type="button" @click="activeTab='log'">Log</button>
        </li>
      </ul>
    </div>

    <div v-if="activeTab=='log'" class="panel pad log-tab">
      <div v-for="(line, idx) in out" :key="idx">{{ line }}</div>
      <div class="error" v-if="errors.message">
        {{ errors.message }}
      </div>
    </div>

    <div v-if="activeTab=='result'" class="panel pad result-tab">
      {{ result }}
    </div>

  </div>
</template>

<style scoped>
  .gravity-page-console {
    .editor-panel {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .log-tab {
      .error {
        color: red;
      }
    }
  }
</style>
