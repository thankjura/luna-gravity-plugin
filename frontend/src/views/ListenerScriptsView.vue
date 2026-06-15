<script setup lang="ts">
import { useRoute } from "vue-router";
import ListenerScriptsList from "@/views/listeners/ListenerScriptsList.vue";
import ListenerScriptsEditor from "@/views/listeners/ListenerScriptsEditor.vue";

const route = useRoute();

</script>

<template>
  <ListenerScriptsEditor v-if="route.params.id"></ListenerScriptsEditor>
  <ListenerScriptsList v-else></ListenerScriptsList>
</template>

<style>

</style>