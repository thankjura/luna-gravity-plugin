<script setup lang="ts">
import { computed, type PropType } from "vue";
import { DropDownButton, type DropDownGroupOption } from "luna";
import type { ScriptFileNode } from "@/interfaces/files";
import { $i18n } from "@/utils/i18n";

export type FileAction = 'newFile' | 'newDirectory' | 'rename' | 'delete';

const props = defineProps({
  node: {type: Object as PropType<ScriptFileNode>, required: true},
  depth: {type: Number, default: 0},
  selectedPath: String,
  expanded: {type: Object as PropType<Set<string>>, required: true},
});

const emits = defineEmits({
  /** a file or a folder was clicked */
  select: (_node: ScriptFileNode) => true,
  toggle: (_path: string) => true,
  action: (_action: FileAction, _node: ScriptFileNode) => true,
});

const isOpen = computed(() => props.expanded.has(props.node.path));

const onClick = () => {
  if (props.node.directory && (props.node.path === props.selectedPath || !isOpen.value)) {
    // first click selects and opens a folder, a click on the selected folder toggles it
    emits('toggle', props.node.path);
  }
  emits('select', props.node);
};

const onToggle = () => {
  if (props.node.directory) {
    emits('toggle', props.node.path);
  }
};

const options = (): Array<DropDownGroupOption> => {
  const groups: Array<DropDownGroupOption> = [];
  if (props.node.directory) {
    groups.push({
      id: 'create',
      options: [
        {id: 'newFile', label: $i18n.t('New script'), iconName: 'icon-file', cb: () => { emits('action', 'newFile', props.node) }},
        {id: 'newDirectory', label: $i18n.t('New folder'), iconName: 'icon-folder-plus', cb: () => { emits('action', 'newDirectory', props.node) }},
      ]
    });
  }
  groups.push({
    id: 'edit',
    options: [
      {id: 'rename', label: $i18n.t('Rename'), iconName: 'icon-pencil', cb: () => { emits('action', 'rename', props.node) }},
      {id: 'delete', label: $i18n.t('Delete'), iconName: 'icon-trash', className: 'dangerous', cb: () => { emits('action', 'delete', props.node) }},
    ]
  });
  return groups;
};
</script>

<template>
  <li class="gravity-file-node" role="treeitem" :aria-expanded="node.directory ? isOpen : undefined" :aria-selected="node.path === selectedPath">
    <div class="gravity-file-row" :class="{selected: node.path === selectedPath}" :style="{paddingLeft: `${depth * 16 + 6}px`}">
      <div class="gravity-file-label" role="button" tabindex="0" @click="onClick" @keydown.enter.prevent="onClick" @keydown.space.prevent="onClick" :title="node.path">
        <span class="gravity-file-chevron" :class="node.directory ? (isOpen ? 'icon-chevron-down' : 'icon-chevron-right') : ''" @click.stop="onToggle"></span>
        <span class="gravity-file-icon" :class="node.directory ? (isOpen ? 'icon-folder-open' : 'icon-folder') : 'icon-file'"></span>
        <span class="ellipsis">{{ node.name }}</span>
      </div>
      <DropDownButton :options="options" :toggle-icon="false" :cache-options="false"
                      class="button button-icon button-transparent button-small icon-ellipsis-vertical gravity-file-menu"
                      :aria-label="$i18n.t('Actions')"></DropDownButton>
    </div>
    <ul v-if="node.directory && isOpen" role="group">
      <FileTreeNode v-for="child in node.children" :key="child.path" :node="child" :depth="depth + 1"
                    :selected-path="selectedPath" :expanded="expanded"
                    @select="n => emits('select', n)" @toggle="p => emits('toggle', p)" @action="(a, n) => emits('action', a, n)"></FileTreeNode>
      <li v-if="!node.children?.length" class="gravity-file-empty muted" :style="{paddingLeft: `${(depth + 1) * 16 + 28}px`}">{{ $i18n.t('Empty folder') }}</li>
    </ul>
  </li>
</template>

<style>
  .gravity-file-node {
    list-style: none;

    ul {
      padding: 0;
      margin: 0;
    }

    .gravity-file-row {
      display: flex;
      align-items: center;
      border-radius: 4px;
      padding-right: 2px;

      &:hover, &:focus-within {
        background: var(--surface-alt-background);

        .gravity-file-menu {
          opacity: 1;
        }
      }

      &.selected {
        background: var(--accent-background);
        color: var(--accent-text-color);
      }
    }

    .gravity-file-label {
      flex: 1;
      min-width: 0;
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 5px 0;
      cursor: pointer;
      outline-offset: -2px;
      user-select: none;
    }

    .gravity-file-chevron {
      width: 14px;
      text-align: center;
      flex-shrink: 0;
      font-size: 0.8em;
    }

    .gravity-file-icon {
      flex-shrink: 0;
    }

    .gravity-file-menu {
      opacity: 0.4;
      flex-shrink: 0;
    }

    .gravity-file-empty {
      padding-top: 4px;
      padding-bottom: 4px;
      font-size: 0.9em;
    }
  }
</style>
