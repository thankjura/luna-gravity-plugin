<script setup lang="ts">
import { BaseDialog, CopyToBufferButton } from "luna";
import { ref, useTemplateRef } from "vue";
import { ComponentExposed } from "vue-component-type-helpers";
import { $i18n } from "@/utils/i18n.ts";
import { ScriptRunResult } from "@/interfaces/metrics.ts";

const dialog = useTemplateRef<ComponentExposed<typeof BaseDialog>>('dialog');
const results = ref<Array<ScriptRunResult>>();
const selectedTab = ref<number>(0);

const show = (resultsValue: Array<ScriptRunResult>) => {
  results.value = resultsValue;
  selectedTab.value = resultsValue.length-1;
  if (resultsValue.length > 0) {
    dialog.value.show();
  }
}

defineExpose({
  show
})

</script>

<template>
  <BaseDialog ref="dialog" class="gravity-executions-dialog">
    <template v-slot:header>{{ $i18n.t("Script executions") }}</template>
    <template v-slot:default>
      <div class="gravity-executions-dialog-body">
        <div class="tabs-header">
          <ul>
            <li v-for="(res, idx) in results" :key="res.scriptId" :class="{active: idx == selectedTab}">
              <button @click="selectedTab = idx" :title="res.startAt" type="button" class="button-icon" :class="[res.exception? 'icon-cancel-circle' : 'icon-ok-circle']"></button>
            </li>
          </ul>
        </div>
        <div class="tabs-body">
          <div class="group">
            <div class="group-header">
              <span class="label">{{ $i18n.t("Time") }}</span>
            </div>
            <span class="description">{{ $i18n.t('The time this was executed (server timezone).') }}</span>
            <span class="code-block">{{ results[selectedTab].startAt }}</span>
          </div>
          <div class="group">
            <div class="group-header">
              <span class="label">{{ $i18n.t("Logs") }}</span>
              <CopyToBufferButton :value="results[selectedTab].logs" :title="$i18n.t('Copy logs to clipboard')"></CopyToBufferButton>
            </div>
            <span class="description">
              {{ $i18n.t('The following log entries were produced by this script execution.') }}
              <span v-html="$i18n.t('Use statements like {0} in your script (depending on your logging configuration) to record logging information.', '<span class=\'code-inline\'>log.warn(\'...\')</span>')"></span>
            </span>
            <span class="code-block">
              <template v-if="results[selectedTab].logs">
                {{ results[selectedTab].logs }}
              </template>
              <template v-else>
                {{ $i18n.t("No logs were found for this execution.") }}
              </template>
            </span>
          </div>
          <div class="group">
            <div class="group-header">
              <span class="label">{{ $i18n.t("Payload") }}</span>
              <CopyToBufferButton :value="results[selectedTab].payload" :title="$i18n.t('Copy payload to clipboard')"></CopyToBufferButton>
            </div>
            <span class="description">{{ $i18n.t('Payload represents the runtime binding variables provided to the executed script.') }}</span>
            <span class="code-block">
              {{ results[selectedTab].payload }}
            </span>
          </div>
          <div class="group">
            <div class="group-header">
              <span class="label">{{ $i18n.t("Timing") }}</span>
            </div>
            <span class="description">{{ $i18n.t("Timing information related to this script execution. Elapsed measures the real world or «wall clock» duration of the execution whereas CPU time measures the amount of time the CPU was busy.") }}</span>
            <span class="code-block">
              <div>
                {{ $i18n.t("Elapsed: {0} ms", results[selectedTab].executionTimeMs) }}
              </div>
              <div>
              {{ $i18n.t("CPU time: {0} ms", results[selectedTab].cpuTimeMs) }}
              </div>
            </span>
          </div>
        </div>
      </div>
    </template>
  </BaseDialog>
</template>

<style>
  .gravity-executions-dialog {
    width: 900px;

    .gravity-executions-dialog-body {
      padding: 20px;
    }

    .tabs-body {
      display: flex;
      flex-direction: column;
      gap: 20px;
      padding-top: 20px;

      .group {
        display: flex;
        flex-direction: column;
        gap: 10px;

        .group-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
        }

        .description {
          font-size: 80%;
          color: var(--grey-color);
        }

        .code-block {
          width: 100%;
          padding: 5px;
          background-color: var(--panel-bg-color-alternate);
          white-space: pre;
          border-radius: 5px;
          overflow: auto;
          max-height: 200px;
        }

        .code-inline {
          background-color: var(--panel-bg-color-alternate);
          padding: 2px 5px;
          border-radius: 2px;
        }
      }
    }
  }
</style>