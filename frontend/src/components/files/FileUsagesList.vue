<script setup lang="ts">
import { computed, type PropType } from "vue";
import { RouteNames, type WorkflowFunctionType } from "luna";
import type { WorkflowScript } from "@/interfaces/workflow";
import { $i18n } from "@/utils/i18n";

defineProps({
  usages: {type: Array as PropType<Array<WorkflowScript>>, required: true},
});

const functionTypeNames = computed<Record<WorkflowFunctionType, string>>(() => ({
  condition: $i18n.t("Condition"),
  validator: $i18n.t("Validator"),
  postfunction: $i18n.t("Postfunction"),
}));
</script>

<template>
  <ul class="gravity-file-usages">
    <li v-for="u in usages" :key="u.workflowId + ':' + u.id">
      <router-link :to="{name: RouteNames.admin.workflows, params: {id: u.workflowId}, query: {hl: u.actionId}}">
        {{ u.workflowName }}<template v-if="u.workflowOriginalId"> ({{ $i18n.t('Draft') }})</template> → {{ u.actionName }}
      </router-link>
      <span class="muted"> · {{ functionTypeNames[u.functionType] }}</span>
      <span class="muted" v-if="u.scriptNote"> · {{ u.scriptNote }}</span>
    </li>
  </ul>
</template>

<style>
  .gravity-file-usages {
    margin: 6px 0 0;
    padding-left: 18px;
    display: flex;
    flex-direction: column;
    gap: 4px;
  }
</style>
