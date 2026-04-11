<template>
  <div class="empty-layout">
    <RouterView />
  </div>
</template>

<script setup>
import { RouterView } from "vue-router";
</script>

<style scoped>
.empty-layout {
  min-height: 100vh;
  background: #f5f6fa;
}
</style>
