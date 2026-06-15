<script setup lang="ts">
import { BaseDialog, ButtonBusy } from "luna";
import { ListenerScript } from "@/interfaces/listener.ts";
import { ref, useTemplateRef } from "vue";
import { listenerService } from "@/services/listenerService.ts";
import { $i18n } from "@/utils/i18n.ts";

const emits = defineEmits({
  deleted:(_id: number) => true
});

const busy = ref(false);
const script = ref<ListenerScript>(null);
const dialog = useTemplateRef('dialog');

const show = (scriptValue: ListenerScript) => {
  script.value = scriptValue;
  dialog.value.show();
}

const hide = () => {
  dialog.value.hide();
}

const submit = () => {
  busy.value = true;
  listenerService.delete(script.value.id).then(() => {
    emits('deleted', script.value.id);
    hide();
  }).finally(() => {
    busy.value = false;
  });
}

defineExpose({
  show
});

</script>

<template>
  <BaseDialog :busy="busy" ref="dialog">
    <template v-slot:header>{{ $i18n.t("Delete listener") }}</template>
    <template v-slot:default>
      <div class="message warning">
        <div class="title">{{ $i18n.t("Are you sure remove listener?") }}</div>
        <div>{{ script?.name }}</div>
        <div class="description">{{ script?.description }}</div>
      </div>
    </template>
    <template v-slot:footer>
      <div>
        <button type="button" @click="hide" :disabled="busy">{{ $i18n.t("Cancel") }}</button>
        <ButtonBusy @click="submit()" :disabled="busy" :busy="busy" class="dangerous">{{ $i18n.t("Delete") }}</ButtonBusy>
      </div>
    </template>
  </BaseDialog>
</template>

<style>

</style>