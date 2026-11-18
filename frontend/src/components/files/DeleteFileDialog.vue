<script setup lang="ts">
import { BaseDialog, ButtonBusy, $notify } from "luna";
import { ref, useTemplateRef } from "vue";
import { $i18n } from "@/utils/i18n";
import type { ComponentExposed } from "vue-component-type-helpers";
import type { ScriptFileNode } from "@/interfaces/files";
import type { WorkflowScript } from "@/interfaces/workflow";
import { fileService } from "@/services/fileService";
import FileUsagesList from "@/components/files/FileUsagesList.vue";

const emits = defineEmits({
  deleted: (_node: ScriptFileNode) => true,
});

const dialog = useTemplateRef<ComponentExposed<typeof BaseDialog>>('dialog');
const node = ref<ScriptFileNode>(null);
const usages = ref<Array<WorkflowScript>>([]);
const loading = ref(false);
const busy = ref(false);

const show = (value: ScriptFileNode) => {
  node.value = value;
  usages.value = [];
  loading.value = true;
  dialog.value.show();
  fileService.getUsages(value.path).then(({data}) => {
    usages.value = data;
  }).finally(() => {
    loading.value = false;
  });
};

const submit = () => {
  busy.value = true;
  fileService.delete(node.value.path).then(() => {
    emits('deleted', node.value);
    dialog.value.hide();
  }).catch((resp) => {
    $notify.error($i18n.t("Failed to delete"), resp?.data?.errors?.path ?? resp?.data?.reason);
  }).finally(() => {
    busy.value = false;
  });
};

defineExpose({show});
</script>

<template>
  <BaseDialog ref="dialog" :busy="busy || loading">
    <template v-slot:header>{{ node?.directory ? $i18n.t("Delete folder") : $i18n.t("Delete script") }}</template>
    <template v-slot:default>
      <div class="pad" v-if="node">
        <div v-if="usages.length" class="message error">
          <div class="title">{{ $i18n.t("Can not delete: used in workflows") }}</div>
          <FileUsagesList :usages="usages"></FileUsagesList>
        </div>
        <div v-else-if="!loading" class="message warning">
          <div class="title">
            {{ node.directory ? $i18n.t("Delete folder {0} with all its content?", node.path) : $i18n.t("Delete script {0}?", node.path) }}
          </div>
          <div>{{ $i18n.t("This can not be undone.") }}</div>
        </div>
      </div>
    </template>
    <template v-slot:footer>
      <div>
        <button type="button" @click="dialog.hide()" :disabled="busy">{{ $i18n.t("Cancel") }}</button>
        <ButtonBusy class="dangerous" :busy="busy" :disabled="busy || loading || usages.length > 0" @click="submit">{{ $i18n.t("Delete") }}</ButtonBusy>
      </div>
    </template>
  </BaseDialog>
</template>
