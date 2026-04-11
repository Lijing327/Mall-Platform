<template>
  <span class="status-tag" :class="toneClass">{{ label }}</span>
</template>

<script setup>
import { computed } from "vue";
import { getOrderStatusTone } from "../../utils/admin-ui";
import { orderStatusZh } from "../../utils/display-labels";

const props = defineProps({
  status: {
    type: String,
    default: ""
  }
});

const label = computed(() => orderStatusZh(props.status));
const toneClass = computed(() => `status-tag--${getOrderStatusTone(props.status)}`);
</script>
