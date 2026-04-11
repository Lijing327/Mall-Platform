<template>
  <section class="info-block">
    <header class="info-block-head">
      <h3>{{ title }}</h3>
      <slot name="extra" />
    </header>
    <div v-if="items.length" class="info-grid">
      <div v-for="item in items" :key="item.label" class="info-item">
        <span class="info-label">{{ item.label }}</span>
        <strong class="info-value">{{ item.value || "—" }}</strong>
      </div>
    </div>
    <div v-else class="info-block-content">
      <slot />
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  items: {
    type: Array,
    default: () => []
  }
});
</script>
