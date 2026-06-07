<script setup lang="ts">

import { onBeforeUnmount, onMounted, PropType, useTemplateRef, watch } from "vue";
import { MetricPoint } from "@/interfaces/metrics.ts";
import { Chart, ChartData, ChartOptions } from "chart.js";
import { DateTime } from "luxon";
import { $i18n } from "@/utils/i18n.ts";

const props = defineProps({
  metrics: Array as PropType<Array<MetricPoint>>
});

const canvas = useTemplateRef<HTMLCanvasElement>('canvas');
let chartInstance: Chart | null = null;

const formatXLabel = (isoString: string): string => {
  const dt = DateTime.fromISO(isoString);
  if (dt.hour === 0 && dt.minute === 0) {
    return dt.toFormat('dd LLL');
  }
  return dt.toFormat('dd.MM HH:mm');
};

const prepareChartData = (data: Array<MetricPoint>): ChartData<'line'> => {
  return {
    labels: data.map(m => formatXLabel(m.bucket)),
    datasets: [
      {
        label: $i18n.t("Max time (ms)"),
        data: data.map(m => m.maxExecutionTimeMs),
        borderColor: '#ff5630',
        backgroundColor: '#ff5630',
        yAxisID: 'yTime',
        tension: 0.1,
        pointRadius: 3
      },
      {
        label: $i18n.t("Avg time (ms)"),
        data: data.map(m => m.avgExecutionTimeMs),
        borderColor: '#0052cc',
        backgroundColor: '#0052cc',
        yAxisID: 'yTime',
        tension: 0.1,
        pointRadius: 2
      },
      {
        label: $i18n.t("CPU time (ms)"),
        data: data.map(m => m.avgCpuTimeMs),
        borderColor: '#00b8d9',
        backgroundColor: '#00b8d9',
        yAxisID: 'yTime',
        borderDash: [5, 5],
        tension: 0.1,
        pointRadius: 0
      },
      {
        label: $i18n.t("Number of executions"),
        data: data.map(m => m.totalCount),
        borderColor: '#ffab00',
        backgroundColor: 'rgba(255, 171, 0, 0.08)',
        yAxisID: 'yCount',
        tension: 0.1,
        fill: true,
        pointRadius: 2
      }
    ]
  };
};

const chartOptions: ChartOptions<'line'> = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'top' },
    tooltip: { mode: 'index', intersect: false }
  },
  scales: {
    x: { grid: { display: false } },
    yTime: {
      type: 'linear',
      position: 'left',
      title: { display: true, text: $i18n.t("Time (ms)"), font: { weight: 'bold' } },
      min: 0
    },
    yCount: {
      type: 'linear',
      position: 'right',
      title: { display: true, text: $i18n.t("Number of executions"), font: { weight: 'bold' } },
      min: 0,
      grid: { drawOnChartArea: false }
    }
  }
};

watch(() => props.metrics, (newMetrics) => {
  if (chartInstance) {
    chartInstance.data = prepareChartData(newMetrics);
    chartInstance.update();
  }
}, { deep: true });

onMounted(() => {
  chartInstance = new Chart(canvas.value, {
    type: 'line',
    data: prepareChartData(props.metrics),
    options: chartOptions
  });
});

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.destroy();
    chartInstance = null;
  }
});

</script>

<template>
  <div class="chart-wrapper">
    <canvas ref="canvas"></canvas>
  </div>
</template>

<style scoped>
  .chart-wrapper {
    width: 100%;
    height: 400px;
    position: relative;
    background: #ffffff;
    padding: 10px;
  }
</style>