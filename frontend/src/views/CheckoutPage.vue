<template>
  <div class="card">
    <h3>提交订单</h3>
    <p>当前用户ID：{{ userId ?? "-" }}</p>
    <div class="actions">
      <button class="btn" @click="createOrderAction">创建订单</button>
      <button class="btn secondary" :disabled="!lastOrderNo" @click="payOrderAction">模拟支付最新订单</button>
    </div>
    <div class="result" v-if="resultText">{{ resultText }}</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { createOrder, payOrder } from "../api/order";
import { getUserId } from "../utils/user-context";

const userId = ref(getUserId());
const lastOrderNo = ref("");
const resultText = ref("");

onMounted(() => {
  userId.value = getUserId();
});

async function createOrderAction() {
  const res = await createOrder({});
  lastOrderNo.value = res.data.orderNo;
  resultText.value = `订单创建成功：${res.data.orderNo}，状态：${res.data.orderStatus}`;
}

async function payOrderAction() {
  if (!lastOrderNo.value) return;
  const res = await payOrder({ orderNo: lastOrderNo.value });
  resultText.value = `支付成功：${res.data.orderNo}，状态：${res.data.orderStatus}`;
}
</script>

<style scoped>
.actions {
  display: flex;
  gap: 10px;
}
.result {
  margin-top: 12px;
  background: #f0f7ff;
  border: 1px solid #d0e4ff;
  padding: 10px;
}
</style>
