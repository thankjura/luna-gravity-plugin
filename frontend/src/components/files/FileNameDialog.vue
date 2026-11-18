<script setup lang="ts">
import { BaseDialog, ButtonBusy, type Errors, $notify } from "luna";
import { nextTick, ref, useTemplateRef } from "vue";
import { $i18n } from "@/utils/i18n";
import type { ComponentExposed } from "vue-component-type-helpers";

export interface FileNameRequest {
  title: string,
  value: string,
  hint?: string,
  submitLabel: string,
  /** Performs the action; a rejected promise keeps the dialog open with errors. */
  submit: (name: string) => Promise<unknown>,
}

const dialog = useTemplateRef<ComponentExposed<typeof BaseDialog>>('dialog');
const input = useTemplateRef<HTMLInputElement>('input');
const request = ref<FileNameRequest>(null);
const name = ref("");
const busy = ref(false);
const errors = ref<Errors>({});

const show = (value: FileNameRequest) => {
  request.value = value;
  name.value = value.value;
  errors.value = {};
  dialog.value.show();
  nextTick(() => {
    input.value?.focus();
    const dot = name.value.lastIndexOf('.');
    input.value?.setSelectionRange(0, dot > 0 ? dot : name.value.length);
  });
};

const submit = () => {
  if (busy.value) {
    return;
  }
  if (!name.value.trim()) {
    errors.value = {path: $i18n.t("Name required")};
    return;
  }
  busy.value = true;
  errors.value = {};
  request.value.submit(name.value.trim()).then(() => {
    dialog.value.hide();
  }).catch((resp) => {
    errors.value = resp?.data?.errors ?? {};
    if (!errors.value.path && !errors.value.newPath) {
      $notify.error($i18n.t("Operation failed"), resp?.data?.reason);
    }
  }).finally(() => {
    busy.value = false;
  });
};

defineExpose({show});
</script>

<template>
  <BaseDialog ref="dialog" :busy="busy">
    <template v-slot:header>{{ request?.title }}</template>
    <template v-slot:default>
      <form class="ui pad" @submit.prevent="submit">
        <div class="field-group">
          <label for="gravity-file-name">{{ $i18n.t("Name") }}:</label>
          <input id="gravity-file-name" ref="input" type="text" v-model="name" autocomplete="off">
          <div class="description muted" v-if="request?.hint">{{ request.hint }}</div>
          <div class="error" v-if="errors.path || errors.newPath">{{ errors.path || errors.newPath }}</div>
        </div>
      </form>
    </template>
    <template v-slot:footer>
      <div>
        <button type="button" @click="dialog.hide()" :disabled="busy">{{ $i18n.t("Cancel") }}</button>
        <ButtonBusy class="primary" :busy="busy" :disabled="busy" @click="submit">{{ request?.submitLabel }}</ButtonBusy>
      </div>
    </template>
  </BaseDialog>
</template>
