<template>
  <article class="admin-stat-card">
    <div class="admin-stat-head">
      <span class="admin-stat-label">{{ label }}</span>
      <span class="admin-stat-icon">{{ icon }}</span>
    </div>
    <div class="admin-stat-value">{{ value }}</div>
    <p v-if="hint" class="admin-stat-hint">{{ hint }}</p>
  </article>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: String,
    required: true
  },
  hint: {
    type: String,
    default: ""
  },
  icon: {
    type: String,
    default: "数"
  }
});
</script>
