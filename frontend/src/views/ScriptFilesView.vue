<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, useTemplateRef } from "vue";
import { onBeforeRouteLeave, useRoute, useRouter } from "vue-router";
import { ButtonBusy, LoadOverlayComponent, $notify } from "luna";
import type { ComponentExposed } from "vue-component-type-helpers";
import CodeEditor from "@/components/CodeEditor.vue";
import FileTreeNode, { type FileAction } from "@/components/files/FileTreeNode.vue";
import FileNameDialog from "@/components/files/FileNameDialog.vue";
import DeleteFileDialog from "@/components/files/DeleteFileDialog.vue";
import FileUsagesList from "@/components/files/FileUsagesList.vue";
import { fileService } from "@/services/fileService";
import type { ScriptFileContent, ScriptFileNode } from "@/interfaces/files";
import type { WorkflowScript } from "@/interfaces/workflow";
import { $i18n } from "@/utils/i18n";

const EDITOR_CONTEXT = {__context__: "workflowFile"};

const route = useRoute();
const router = useRouter();

const tree = ref<ScriptFileNode>(null);
/** Selected tree node (file or folder): target of toolbar actions. */
const selected = ref<ScriptFileNode>(null);
const layout = useTemplateRef<HTMLDivElement>('layout');
/** Fixed height on wide screens (panels fill the viewport, the tree scrolls), natural height on narrow ones. */
const layoutHeight = ref<number | null>(null);
const treeBusy = ref(false);
const expanded = ref(new Set<string>());

const file = ref<ScriptFileContent>(null);
const content = ref("");
const fileBusy = ref(false);
const saving = ref(false);
const saveError = ref<string>(null);
const usages = ref<Array<WorkflowScript>>([]);

const nameDialog = useTemplateRef<ComponentExposed<typeof FileNameDialog>>('nameDialog');
const deleteDialog = useTemplateRef<ComponentExposed<typeof DeleteFileDialog>>('deleteDialog');

const dirty = computed(() => file.value != null && content.value !== file.value.content);

const parentOf = (path: string) => path.includes('/') ? path.substring(0, path.lastIndexOf('/')) : '';
const join = (dir: string, name: string) => dir ? `${dir}/${name}` : name;

const confirmDiscard = () => !dirty.value || window.confirm($i18n.t("Discard unsaved changes?"));

// ------------------------------------------------------------------------------------------------- tree

const loadTree = () => {
  treeBusy.value = true;
  return fileService.getTree().then(({data}) => {
    tree.value = data;
    selected.value = selected.value ? findNode(data, selected.value.path) : null;
  }).catch((resp) => {
    $notify.error($i18n.t("Failed to load scripts"), resp?.data?.reason);
  }).finally(() => {
    treeBusy.value = false;
  });
};

const findNode = (node: ScriptFileNode, path: string): ScriptFileNode => {
  if (node.path === path) {
    return node;
  }
  for (const child of node.children ?? []) {
    if (path === child.path || path.startsWith(child.path + "/")) {
      return findNode(child, path);
    }
  }
  return null;
};

const onSelect = (node: ScriptFileNode) => {
  selected.value = node;
  if (!node.directory) {
    openFile(node.path);
  }
};

const toggle = (path: string) => {
  const set = new Set(expanded.value);
  if (set.has(path)) {
    set.delete(path);
  } else {
    set.add(path);
  }
  expanded.value = set;
};

const expandTo = (path: string) => {
  const set = new Set(expanded.value);
  let dir = parentOf(path);
  while (dir) {
    set.add(dir);
    dir = parentOf(dir);
  }
  expanded.value = set;
};

// ------------------------------------------------------------------------------------------------- file

const openFile = (path: string, force = false) => {
  if (!force && file.value?.path === path) {
    return;
  }
  if (!force && !confirmDiscard()) {
    return;
  }
  fileBusy.value = true;
  saveError.value = null;
  fileService.getContent(path).then(({data}) => {
    file.value = data;
    content.value = data.content;
    expandTo(data.path);
    if (tree.value) {
      selected.value = findNode(tree.value, data.path) ?? selected.value;
    }
    if (route.query.path !== data.path) {
      router.replace({query: {...route.query, path: data.path}});
    }
    loadUsages(data.path);
  }).catch((resp) => {
    $notify.error($i18n.t("Failed to open script"), resp?.data?.errors?.path ?? resp?.data?.reason);
  }).finally(() => {
    fileBusy.value = false;
  });
};

const closeFile = () => {
  file.value = null;
  content.value = "";
  usages.value = [];
  saveError.value = null;
  const query = {...route.query};
  delete query.path;
  router.replace({query});
};

const loadUsages = (path: string) => {
  usages.value = [];
  fileService.getUsages(path).then(({data}) => {
    if (file.value?.path === path) {
      usages.value = data;
    }
  });
};

const save = () => {
  if (!file.value || saving.value || !dirty.value) {
    return;
  }
  saving.value = true;
  saveError.value = null;
  const text = content.value;
  fileService.save(file.value.path, text, file.value.modified).then(({data}) => {
    file.value = {...data, content: text};
    $notify.ok($i18n.t("Script saved"));
  }).catch((resp) => {
    saveError.value = resp?.data?.errors?.content ?? resp?.data?.errors?.path ?? null;
    $notify.error($i18n.t("Failed to save script"), saveError.value ?? resp?.data?.reason);
  }).finally(() => {
    saving.value = false;
  });
};

const reload = () => {
  if (file.value && confirmDiscard()) {
    openFile(file.value.path, true);
  }
};

// ------------------------------------------------------------------------------------------------- actions

const createFile = (dir: string) => {
  nameDialog.value.show({
    title: $i18n.t("New script"),
    value: "",
    hint: dir ? $i18n.t("In folder {0}. The .groovy extension is added automatically.", dir) : $i18n.t("The .groovy extension is added automatically."),
    submitLabel: $i18n.t("Create"),
    submit: (name) => fileService.createFile(join(dir, name)).then(({data}) => {
      expandTo(data.path);
      return loadTree().then(() => openFile(data.path));
    }),
  });
};

const createDirectory = (dir: string) => {
  nameDialog.value.show({
    title: $i18n.t("New folder"),
    value: "",
    hint: dir ? $i18n.t("In folder {0}", dir) : undefined,
    submitLabel: $i18n.t("Create"),
    submit: (name) => fileService.createDirectory(join(dir, name)).then(({data}) => {
      expandTo(join(data.path, "_"));
      return loadTree();
    }),
  });
};

const rename = (node: ScriptFileNode) => {
  if (file.value && file.value.path.startsWith(node.path) && !confirmDiscard()) {
    return;
  }
  nameDialog.value.show({
    title: node.directory ? $i18n.t("Rename folder") : $i18n.t("Rename script"),
    value: node.name,
    hint: $i18n.t("Scripts used in workflows can not be renamed."),
    submitLabel: $i18n.t("Rename"),
    submit: (name) => fileService.rename(node.path, join(parentOf(node.path), name)).then(({data}) => {
      const opened = file.value?.path;
      return loadTree().then(() => {
        if (opened === node.path) {
          openFile(data.path, true);
        } else if (opened && opened.startsWith(node.path + "/")) {
          openFile(data.path + opened.substring(node.path.length), true);
        }
      });
    }),
  });
};

const onAction = (action: FileAction, node: ScriptFileNode) => {
  switch (action) {
    case "newFile": createFile(node.path); break;
    case "newDirectory": createDirectory(node.path); break;
    case "rename": rename(node); break;
    case "delete": deleteDialog.value.show(node); break;
  }
};

const onDeleted = (node: ScriptFileNode) => {
  if (selected.value && (selected.value.path === node.path || selected.value.path.startsWith(node.path + "/"))) {
    selected.value = null;
  }
  const opened = file.value?.path;
  if (opened && (opened === node.path || opened.startsWith(node.path + "/"))) {
    file.value = null;
    closeFile();
  }
  $notify.ok(node.directory ? $i18n.t("Folder deleted") : $i18n.t("Script deleted"));
  loadTree();
};

/** Folder for new items: the selected folder, or the folder of the selected file. */
const currentDir = computed(() => {
  if (!selected.value) {
    return "";
  }
  return selected.value.directory ? selected.value.path : parentOf(selected.value.path);
});

const onTreeKeyDown = (e: KeyboardEvent) => {
  if (!selected.value || (e.target as HTMLElement)?.closest('.gravity-file-menu')) {
    return;
  }
  if (e.key === 'Delete') {
    e.preventDefault();
    deleteDialog.value.show(selected.value);
  } else if (e.key === 'F2') {
    e.preventDefault();
    rename(selected.value);
  }
};

/** A click on the empty space of the tree selects the root folder. */
const onTreeBackgroundClick = (e: MouseEvent) => {
  if (!(e.target as HTMLElement).closest('.gravity-file-node')) {
    selected.value = null;
  }
};

const updateLayoutHeight = () => {
  if (window.innerWidth <= 800) {
    layoutHeight.value = null;
  } else if (layout.value) {
    const top = layout.value.getBoundingClientRect().top + window.scrollY;
    layoutHeight.value = Math.max(window.innerHeight - top - 20, 420);
  }
};

// ------------------------------------------------------------------------------------------------- lifecycle

const onKeyDown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 's') {
    e.preventDefault();
    save();
  }
};

const onBeforeUnload = (e: BeforeUnloadEvent) => {
  if (dirty.value) {
    e.preventDefault();
  }
};

onBeforeRouteLeave(() => confirmDiscard());

onMounted(() => {
  window.addEventListener('keydown', onKeyDown);
  window.addEventListener('beforeunload', onBeforeUnload);
  window.addEventListener('resize', updateLayoutHeight);
  updateLayoutHeight();
  loadTree().then(() => {
    const path = route.query.path;
    if (typeof path === 'string' && path) {
      openFile(path, true);
    }
  });
});

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeyDown);
  window.removeEventListener('beforeunload', onBeforeUnload);
  window.removeEventListener('resize', updateLayoutHeight);
});
</script>

<template>
  <div class="gravity-page gravity-page-scripts pad">
    <nav class="breadcrumbs header">
      <ol role="list">
        <li>{{ $i18n.t("Gravity") }}</li>
        <li>{{ $i18n.t("Script editor") }}</li>
      </ol>
    </nav>

    <div class="gravity-scripts-layout" ref="layout" :style="layoutHeight ? {height: `${layoutHeight}px`} : {}">
      <aside class="panel gravity-scripts-tree">
        <div class="gravity-scripts-toolbar">
          <button type="button" class="button-icon button-transparent icon-file" :title="$i18n.t('New script')"
                  :aria-label="$i18n.t('New script')" @click="createFile(currentDir)"></button>
          <button type="button" class="button-icon button-transparent icon-folder-plus" :title="$i18n.t('New folder')"
                  :aria-label="$i18n.t('New folder')" @click="createDirectory(currentDir)"></button>
          <button type="button" class="button-icon button-transparent icon-pencil" :title="$i18n.t('Rename') + ' (F2)'"
                  :aria-label="$i18n.t('Rename')" :disabled="!selected" @click="rename(selected)"></button>
          <button type="button" class="button-icon button-transparent icon-trash" :title="$i18n.t('Delete') + ' (Delete)'"
                  :aria-label="$i18n.t('Delete')" :disabled="!selected" @click="deleteDialog.show(selected)"></button>
          <span class="gravity-scripts-toolbar-spacer"></span>
          <button type="button" class="button-icon button-transparent icon-refresh-cw" :title="$i18n.t('Refresh')"
                  :aria-label="$i18n.t('Refresh')" @click="loadTree()"></button>
        </div>
        <div class="gravity-scripts-target muted ellipsis" :title="currentDir ? `scripts/${currentDir}` : 'scripts'">
          <span class="icon-folder"></span> scripts{{ currentDir ? `/${currentDir}` : '' }}
        </div>
        <div class="gravity-scripts-nodes-scroll" @click="onTreeBackgroundClick" @keydown="onTreeKeyDown">
          <ul class="gravity-scripts-nodes" role="tree" v-if="tree?.children?.length">
            <FileTreeNode v-for="child in tree.children" :key="child.path" :node="child" :expanded="expanded"
                          :selected-path="selected?.path" @select="onSelect" @toggle="toggle" @action="onAction"></FileTreeNode>
          </ul>
          <div v-else-if="tree && !treeBusy" class="empty-panel pad gravity-scripts-empty">
            <div>{{ $i18n.t("No scripts yet") }}</div>
            <button type="button" class="primary" @click="createFile('')">{{ $i18n.t("Create script") }}</button>
          </div>
        </div>
        <LoadOverlayComponent v-if="treeBusy" :absolute="true" :dim="true"></LoadOverlayComponent>
      </aside>

      <section class="panel gravity-scripts-editor">
        <template v-if="file">
          <div class="gravity-scripts-editor-header">
            <div class="gravity-scripts-file-title ellipsis" :title="file.path">
              <span class="icon-file"></span>
              <span class="ellipsis">{{ file.path }}</span>
              <span v-if="dirty" class="gravity-dirty-mark" :title="$i18n.t('Unsaved changes')">●</span>
            </div>
            <div class="buttons">
              <button type="button" @click="reload" :disabled="saving">{{ $i18n.t("Reload") }}</button>
              <ButtonBusy class="primary" :busy="saving" :disabled="saving || !dirty" @click="save" :title="'Ctrl+S'">{{ $i18n.t("Save") }}</ButtonBusy>
            </div>
          </div>
          <div v-if="saveError" class="message error gravity-save-error">{{ saveError }}</div>
          <CodeEditor class="gravity-scripts-code" v-model="content" :context="EDITOR_CONTEXT"></CodeEditor>
          <div class="gravity-scripts-usages">
            <template v-if="usages.length">
              <div class="muted">{{ $i18n.t("Used in workflows ({0}). The script must compile to be saved, it can not be renamed or deleted.", usages.length) }}</div>
              <FileUsagesList :usages="usages"></FileUsagesList>
            </template>
            <div v-else class="muted">{{ $i18n.t("Not used in workflows") }}</div>
          </div>
        </template>
        <div v-else class="empty-panel pad gravity-scripts-placeholder">
          <span class="icon-code"></span>
          <div>{{ $i18n.t("Select a script in the tree or create a new one") }}</div>
        </div>
        <LoadOverlayComponent v-if="fileBusy" :absolute="true" :dim="true"></LoadOverlayComponent>
      </section>
    </div>

    <FileNameDialog ref="nameDialog"></FileNameDialog>
    <DeleteFileDialog ref="deleteDialog" @deleted="onDeleted"></DeleteFileDialog>
  </div>
</template>

<style>
  .gravity-page-scripts {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .gravity-scripts-layout {
      display: grid;
      grid-template-columns: minmax(220px, 300px) minmax(0, 1fr);
      gap: 20px;
      min-height: 420px;
    }

    .gravity-scripts-tree, .gravity-scripts-editor {
      position: relative;
      margin: 0;
      min-height: 0;
      height: 100%;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
    }

    .gravity-scripts-tree {
      padding: 8px;
    }

    .gravity-scripts-toolbar {
      display: flex;
      align-items: center;
      gap: 2px;
      padding: 0 0 6px;
      border-bottom: 1px solid var(--border-color);

      button:disabled {
        opacity: 0.35;
      }
    }

    .gravity-scripts-toolbar-spacer {
      flex: 1;
    }

    .gravity-scripts-target {
      padding: 6px 4px;
      font-size: 0.9em;
    }

    .gravity-scripts-nodes-scroll {
      flex: 1;
      min-height: 0;
      overflow: auto;
    }

    .gravity-scripts-nodes {
      margin: 0;
      padding: 0;
    }

    .gravity-scripts-editor {
      padding: 12px;
      gap: 12px;
    }

    .gravity-scripts-editor-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }

    .gravity-scripts-file-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      min-width: 0;
    }

    .gravity-dirty-mark {
      color: var(--warning-color);
    }

    .gravity-scripts-code {
      flex: 1;
      min-height: 200px;
    }

    .gravity-scripts-usages {
      max-height: 25%;
      overflow: auto;
      flex-shrink: 0;
    }

    .gravity-scripts-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      text-align: center;
    }

    .gravity-scripts-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 10px;
      flex: 1;

      .icon-code {
        font-size: 2em;
        color: var(--muted-text-color);
      }
    }

    @media (max-width: 800px) {
      .gravity-scripts-layout {
        grid-template-columns: minmax(0, 1fr);
      }

      .gravity-scripts-tree {
        max-height: 40vh;
      }

      .gravity-scripts-editor {
        min-height: 600px;
      }

      .gravity-scripts-editor-header {
        flex-wrap: wrap;
      }
    }
  }
</style>
