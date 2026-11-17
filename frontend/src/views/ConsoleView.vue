<script setup lang="ts">
import CodeEditor from "@/components/CodeEditor.vue";
import { ref } from "vue";
import { ButtonBusy, type Errors, $notify, SwitchButtons } from "luna";
import { scriptService } from "@/services/scriptService";
import { $i18n } from "@/utils/i18n";

const value = ref<string>("System.out.println(\"Hello\");\nlog.info(\"hello world\")");
const busy = ref(false);
const out = ref<Array<string>>([]);
const activeTab = ref(0);
const result = ref<string>(null);
const LIMIT = 300;
const errors = ref<Errors>({});

const RESULT_TAB = 0;
const LOGS_TAB = 1;

const onResultFetch = (resp: {log?: string, result?: string, error?: any}) => {
  if (resp.log) {
    out.value.push(resp.log);
  }

  if (out.value.length > LIMIT) {
    out.value.shift();
  }

  if (resp.result != undefined) {
    result.value = resp.result;
    busy.value = false;
    activeTab.value = RESULT_TAB;
  }

  if (resp.error != undefined) {
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
  errors.value = {};
  activeTab.value = LOGS_TAB;
  busy.value = true;
  scriptService.runScript(value.value, {}, onResultFetch);
}

</script>

<template>
  <div class="gravity-page gravity-page-console pad">
    <nav class="breadcrumbs header">
      <ol role="list">
        <li>{{ $i18n.t("Gravity") }}</li>
        <li>{{ $i18n.t("Console") }}</li>
      </ol>
    </nav>
    <div class="pad panel editor-panel">
      <CodeEditor class="inline-code-editor" v-model="value" :diabled="busy"></CodeEditor>
      <ButtonBusy :busy="busy" :disabled="busy" @click="runScript">{{ $i18n.t("Execute") }}</ButtonBusy>
    </div>

    <div class="pad-y">
      <SwitchButtons :index="activeTab">
        <button type="button" @click="activeTab=RESULT_TAB">{{ $i18n.t("Result") }}</button>
        <button type="button" @click="activeTab=LOGS_TAB">{{ $i18n.t("Logs") }}</button>
      </SwitchButtons>
    </div>
    <div v-if="activeTab==LOGS_TAB" class="panel pad log-tab">
      <div v-for="(line, idx) in out" :key="idx">{{ line }}</div>
      <div class="error" v-if="errors.message">
        {{ errors.message }}
      </div>
    </div>

    <div v-if="activeTab==RESULT_TAB" class="panel pad result-tab">
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

      .inline-code-editor {
        min-height: 400px;
      }
    }

    .log-tab {
      .error {
        color: red;
      }
    }
  }
</style>
