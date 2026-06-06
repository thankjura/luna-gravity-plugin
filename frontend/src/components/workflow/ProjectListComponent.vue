<script setup lang="ts">

import { PropType, useTemplateRef } from "vue";
import { ProjectInfo } from "luna";
import { $i18n } from "@/utils/i18n.ts";
import { PopoverComponent } from "luna";
import { ComponentExposed } from "vue-component-type-helpers";

defineProps({
  projects: Object as PropType<Record<string, ProjectInfo>>,
  keys: Array as PropType<Array<string>>,
});

const popover = useTemplateRef<ComponentExposed<typeof PopoverComponent>>('popover');
const button = useTemplateRef<HTMLButtonElement>('button');

const showMoreProjects = (event: MouseEvent) => {
  if (button.value) {
    popover.value.show(event, button.value);
  }
}

</script>

<template>
  <ul class="project-list">
    <li v-for="key in keys.slice(0, 3)" :key="key">
      <span class="item" :title="projects[key]?.name">
        {{ key }}
      </span>
    </li>
    <li v-if="keys.length > 3">
      <button class="item icon-dots" :title="$i18n.t('More')" ref="button" @click="showMoreProjects"></button>
      <PopoverComponent group="projects" ref="popover">
        <div class="popover-body">
          <ul class="project-list">
            <li v-for="key in keys" :key="key">
              <span class="item" :title="projects[key]?.name">
                {{ key }}
              </span>
            </li>
          </ul>
        </div>
      </PopoverComponent>
    </li>
  </ul>
</template>

<style scoped>
  .popover-body {
    padding: 10px;
  }

  .project-list {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    list-style: none;
    padding: 0;
    margin: 0;

    .item {
      background: var(--panel-bg-color-alternate);
      border-radius: 2px;
      padding: 2px 5px;
      font-size: 80%;
      height: 20px;
      display: flex;
      align-items: center;
    }

    button.item {
      border: none;
      outline: none;
      height: 100%;

      &:focus-within, &:active {
        outline: none;
        border: none;
      }
    }
  }
</style>