<script setup lang="ts">
import { computed, PropType } from "vue";
import { ScriptRunResult } from "@/interfaces/metrics.ts";
import { $i18n } from "@/utils/i18n.ts";

const props = defineProps({
  results: Array as PropType<Array<ScriptRunResult>>,
});

const fails = computed(() => {
  return props.results.filter(sr => sr.exception).length;
});

</script>

<template>
  <button class="history-button-component button-link" :disabled="results.length === 0">
    <template v-if="results.length == 0">
      <span class="icon-clock"></span>
      {{ $i18n.t('Has not run yet') }}
    </template>
    <template v-else>
      <span :class="[fails > 0? 'icon-cancel-circle' : 'icon-ok-circle']"></span>
      <template v-if="fails > 0">
        {{ $i18n.t("Has {0} failures in the last {0} executions", fails, results.length) }}
      </template>
      <template v-else>
        {{ $i18n.t("No failures in the last {0} executions", results.length) }}
      </template>
    </template>
  </button>
</template>

<style>
  .history-button-component {
    display: flex;
    gap: 10px;
    padding: 0;
    font-weight: normal!important;

    &:disabled {
      color: var(--grey-color);
    }

    &:not(:disabled):hover {
      text-decoration: underline;
    }

    .icon-ok-circle {
      color: green;
    }
  }
</style>