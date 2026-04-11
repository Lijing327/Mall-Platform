<template>
  <div class="admin-table-actions">
    <button
      v-for="action in actions"
      :key="action.label"
      type="button"
      class="table-action"
      :class="action.tone || 'neutral'"
      :disabled="action.disabled"
      @click="action.onClick?.()"
    >
      {{ action.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  actions: {
    type: Array,
    default: () => []
  }
});
</script>
