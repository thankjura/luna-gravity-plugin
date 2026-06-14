<script setup lang="ts">
import { BaseDialog, Option, SingleSelect, DateTimePicker, ButtonBusy } from "luna";
import { $i18n } from "@/utils/i18n.ts";
import { computed, ref, useTemplateRef } from "vue";
import { MetricPointCollection } from "@/interfaces/metrics.ts";
import { DateTime, DurationLike } from "luxon";
import { metricService } from "@/services/metricService.ts";
import { ComponentExposed } from "vue-component-type-helpers";
import PerformanceChartComponent from "@/components/metrics/PerformanceChartComponent.vue";

const DATE_FORMAT = 'yyyy-LL-dd HH:mm';
type Period = '1h' | '2h' | '4h' | '8h' | '1d' | '2d' | '7d' | '14d' | 'custom';

const busy = ref<boolean>(false);
const metrics = ref<MetricPointCollection>(null);
const fromDate = ref<string>(null);
const toDate = ref<string>(null);
const selectedPeriod = ref<Period>('1h');
const scriptId = ref<string>(null);
const dialog = useTemplateRef<ComponentExposed<typeof BaseDialog>>('dialog');

const periodOptions = computed<Array<Option<Period>>>(() => {
  return [
    {
      id: '1h',
      name: $i18n.t("Last hour"),
    },
    {
      id: '2h',
      name: $i18n.t("Last 2 hours"),
    },
    {
      id: '4h',
      name: $i18n.t("Last 4 hours"),
    },
    {
      id: '8h',
      name: $i18n.t("Last 8 hours"),
    },
    {
      id: '1d',
      name: $i18n.t("Last day"),
    },
    {
      id: '2d',
      name: $i18n.t("Last 2 days"),
    },
    {
      id: '7d',
      name: $i18n.t("Last week"),
    },
    {
      id: '14d',
      name: $i18n.t("Last 2 weeks"),
    },
    {
      id: 'custom',
      name: $i18n.t("Custom"),
    },
  ]
})

const loadPointsForPeriod = (from: string, to: string) => {
  busy.value = true;
  metricService.getCharPoints(scriptId.value, from, to).then((data) => {
    metrics.value = data.data;
  }).finally(() => {
    busy.value = false;
  });
}

const loadPoints = () => {
  if (selectedPeriod.value == 'custom') {
    if (fromDate.value) {
      loadPointsForPeriod(fromDate.value, toDate.value);
    }
  } else {
    const match = selectedPeriod.value.match(/^(\d+)([hd])$/);
    const value = parseInt(match[1], 10);
    const unit = match[2];

    const to = DateTime.now();
    const duration: DurationLike = unit === 'h' ? { hours: value } : { days: value };
    const from = to.minus(duration);
    loadPointsForPeriod(from.toFormat(DATE_FORMAT), to.toFormat(DATE_FORMAT));
  }
}

const onSelectPeriod = () => {
  if (selectedPeriod.value == 'custom') {
    const from = DateTime.now().minus({days: 1});
    fromDate.value = from.toFormat(DATE_FORMAT);
  }

  loadPoints();
}

const show = (scriptIdValue: string) => {
  scriptId.value = scriptIdValue;
  selectedPeriod.value = "2h";
  loadPoints();
  dialog.value.show();
}

const hide = () => {
  dialog.value.hide();
}

defineExpose({
  show
});

</script>

<template>
  <BaseDialog ref="dialog" class="gravity-performance-dialog large">
    <template v-slot:header>{{ $i18n.t("Performance chart") }}</template>
    <template v-slot:default>
      <div class="performance-chart-wrapper">
        <PerformanceChartComponent :metrics="metrics" v-if="metrics"></PerformanceChartComponent>
        <div class="no-metrics-overlay" v-if="!busy && metrics?.points.length == 0">{{ $i18n.t("No data for selected period") }}</div>
      </div>
    </template>
    <template v-slot:footer>
      <div class="performance-chart-footer">
        <SingleSelect class="chart-input" :options="periodOptions" v-model="selectedPeriod" @select="onSelectPeriod" :enable-clear="false" :disabled="busy"></SingleSelect>
        <template v-if="selectedPeriod == 'custom'">
          <DateTimePicker class="chart-input" v-model="fromDate" :disabled="busy" :with-time="true"></DateTimePicker>
          <DateTimePicker class="chart-input" v-model="toDate" :placeholder="$i18n.t('empty for now')" :disabled="busy" :with-time="true"></DateTimePicker>
        </template>
      </div>
      <div>
        <ButtonBusy :disabled="busy" class="primary" @click="loadPoints">{{ $i18n.t("Refresh") }}</ButtonBusy>
        <button type="button" @click="hide" :disabled="busy">{{ $i18n.t("Close") }}</button>
      </div>
    </template>
  </BaseDialog>
</template>

<style>
  .gravity-performance-dialog {
    width: auto;
    max-width: 100%;
    min-width: 1200px;

    .performance-chart-wrapper {
      position: relative;

      .no-metrics-overlay {
        position: absolute;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        display: flex;
        justify-content: center;
        align-items: center;
        font-weight: bold;
        color: #666666;
      }
    }

    .performance-chart-footer {
      display: flex;
      align-content: center;
      gap: 20px;

      input {
        min-width: 160px!important;
      }

      .chart-input, input {
        max-width: 200px;
      }
    }
  }
</style>