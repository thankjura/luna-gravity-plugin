<script setup lang="ts">
import CodeEditor from "@/components/CodeEditor.vue";
import { computed, inject, onMounted, type PropType, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { type Errors, Injections, SingleSelect, SwitchButtons } from "luna";
import { $i18n } from "@/utils/i18n";
import { fileService } from "@/services/fileService";

defineProps({
  errors: Object as PropType<Errors>,
})

type Params = {
  script: string,
  note: string,
  disabled: 'yes' | null,
  /** 'inline' (default) or 'file' */
  source?: 'inline' | 'file',
  /** script file, relative to <luna.home>/scripts */
  file?: string,
}

const value = defineModel<Params>();
const funcType = inject(Injections.workflowFuncType);
const router = useRouter();

const context = computed(() => {
  if (funcType?.value == 'condition') {
    return {
      __context__: "workflowCondition",
    }
  } else if (funcType?.value == 'validator') {
    return {
      __context__: "workflowValidator",
    }
  } else if (funcType?.value == 'postfunction') {
    return {
      __context__: "workflowPostFunction",
    }
  }
});

const isFile = computed(() => value.value?.source === 'file');
const sourceIndex = computed(() => isFile.value ? 1 : 0);
const scripts = ref<Array<string>>([]);
const scriptsLoading = ref(false);
const fileContent = ref<string>(null);

const fileOptions = computed<Array<{id: string, name: string}>>(() => {
  const out = scripts.value.map(path => ({id: path, name: path}));
  const current = value.value?.file;
  if (current && !scriptsLoading.value && !scripts.value.includes(current)) {
    out.unshift({id: current, name: `${current} (${$i18n.t('not found')})`});
  }
  return out;
});
const fileError = ref<string>(null);

const setSource = (source: 'inline' | 'file') => {
  value.value.source = source;
  if (source === 'file' && scripts.value.length === 0) {
    loadScripts();
  }
};

const loadScripts = () => {
  scriptsLoading.value = true;
  fileService.getScripts().then(({data}) => {
    scripts.value = data;
  }).finally(() => {
    scriptsLoading.value = false;
  });
};

const loadContent = (path: string) => {
  fileContent.value = null;
  fileError.value = null;
  if (!path) {
    return;
  }
  fileService.getContent(path).then(({data}) => {
    if (value.value?.file === path) {
      fileContent.value = data.content;
    }
  }).catch((resp) => {
    fileError.value = resp?.data?.errors?.path ?? $i18n.t("Failed to open script");
  });
};

const editorHref = computed(() => {
  const query = value.value?.file ? {path: value.value.file} : {};
  return router.resolve({name: 'gravityScripts', query}).href;
});

watch(() => isFile.value ? value.value.file : null, (path) => {
  if (isFile.value) {
    loadContent(path);
  }
});

onMounted(() => {
  if (isFile.value) {
    loadScripts();
    loadContent(value.value.file);
  }
});

</script>

<template>
  <div class="gravity-edit-function">
    <div class="gravity-edit-form">
      <div class="gravity-field-group">
        <label for="note">{{ $i18n.t('Note') }}</label>
        <input type="text" v-model="value.note" :placeholder="$i18n.t('optional script description...')">
      </div>
      <div class="gravity-field-group">
        <label>
          <input type="checkbox" v-model="value.disabled" true-value="yes" false-value="" >
          {{ $i18n.t('Disabled') }}
        </label>
      </div>
      <div class="gravity-field-group">
        <SwitchButtons :index="sourceIndex">
          <button type="button" @click="setSource('inline')">{{ $i18n.t('Inline script') }}</button>
          <button type="button" @click="setSource('file')">{{ $i18n.t('Script file') }}</button>
        </SwitchButtons>
      </div>
    </div>

    <template v-if="isFile">
      <div class="gravity-edit-file">
        <div class="gravity-field-group">
          <label for="gravity-script-file">{{ $i18n.t('File') }}</label>
          <SingleSelect id="gravity-script-file" class="gravity-script-file-select" v-model="value.file" :options="fileOptions"
                        :disabled="scriptsLoading" :placeholder="scriptsLoading ? $i18n.t('Loading...') : $i18n.t('Select script file')"></SingleSelect>
          <div class="gravity-edit-file-actions">
            <a :href="editorHref" target="_blank" class="iconed"><span class="icon-external-link"></span> {{ value.file ? $i18n.t('Open in script editor') : $i18n.t('Script editor') }}</a>
            <button type="button" class="button-link iconed" @click="loadScripts"><span class="icon-refresh-cw"></span> {{ $i18n.t('Refresh list') }}</button>
          </div>
        </div>
        <div class="error" v-if="errors?.file">{{ errors.file }}</div>
        <div class="error" v-else-if="fileError">{{ fileError }}</div>
        <div class="muted" v-if="!scriptsLoading && scripts.length === 0">{{ $i18n.t('No script files yet. Create one in the script editor.') }}</div>
      </div>
      <CodeEditor v-if="fileContent != null" class="function-code-editor" :model-value="fileContent" :disabled="true" :context="context"></CodeEditor>
    </template>
    <template v-else>
      <div class="error" v-if="errors?.script">{{ errors.script }}</div>
      <CodeEditor class="function-code-editor" v-model="value.script" :context="context"></CodeEditor>
    </template>
  </div>
</template>

<style>
  .gravity-edit-function {
    padding: 10px;
    display: flex;
    gap: 20px;
    flex-direction: column;

    .gravity-edit-form {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 20px;

      .gravity-field-group {
        display: flex;
        gap: 20px;
        align-items: center;
      }
    }

    .gravity-edit-file {
      display: flex;
      flex-direction: column;
      gap: 6px;

      .gravity-edit-file-actions {
        display: flex;
        align-items: center;
        gap: 20px;
      }

      .gravity-script-file-select {
        min-width: 320px;
      }
    }

    .function-code-editor {
      flex-shrink: 0;
      height: 100%;
      min-height: 400px;
    }
  }
</style>