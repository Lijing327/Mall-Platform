<template>
  <section class="admin-page-header">
    <div>
      <p v-if="eyebrow" class="admin-page-eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <p v-if="subtitle" class="admin-page-subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="admin-page-actions">
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: ""
  },
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ""
  }
});
</script>
