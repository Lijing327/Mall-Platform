<template>
  <div class="page">
    <header class="page-head">
      <h1 class="title">我的订单</h1>
    </header>

    <div class="tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value || 'all'"
        type="button"
        class="tab"
        :class="{ active: statusTab === tab.value }"
        @click="statusTab = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="filteredList.length === 0" class="empty">暂无订单</div>

    <div v-else class="orders">
      <article v-for="order in filteredList" :key="order.orderId" class="order-card">
        <div class="order-head">
          <div class="head-row">
            <span class="muted">订单号</span>
            <span class="mono">{{ order.orderNo }}</span>
          </div>
          <div class="head-row">
            <span class="muted">下单时间</span>
            <span>{{ formatTime(order.createTime) }}</span>
          </div>
          <div class="head-row">
            <span class="muted">状态</span>
            <span class="status">{{ orderStatusZh(order.orderStatus) }}</span>
          </div>
          <div class="head-row pay-row">
            <span>实付：<strong class="pay">￥{{ formatPrice(order.payAmount) }}</strong></span>
          </div>
        </div>

        <div v-if="order.receiverName || order.receiverAddress" class="addr-box">
          <span v-if="order.receiverName">{{ order.receiverName }} {{ order.receiverMobile }}</span>
          <p v-if="order.receiverAddress">{{ order.receiverAddress }}</p>
        </div>

        <ul class="lines">
          <li v-for="(line, idx) in order.items" :key="idx" class="line-item">
            <div class="line-img">
              <img v-if="line.productImage" :src="line.productImage" alt="" />
              <div v-else class="ph">无图</div>
            </div>
            <div class="line-info">
              <div class="line-name">{{ line.productName }}</div>
              <div class="line-meta">￥{{ formatPrice(line.productPrice) }} × {{ line.quantity }}</div>
            </div>
          </li>
        </ul>

        <div class="order-actions">
          <button
            v-if="order.orderStatus === 'PENDING_PAYMENT'"
            type="button"
            class="btn primary"
            :disabled="submittingId === order.orderId"
            @click="onPay(order)"
          >
            {{ submittingId === order.orderId ? "处理中…" : "去支付" }}
          </button>
          <button
            v-if="order.orderStatus === 'SHIPPED'"
            type="button"
            class="btn primary"
            :disabled="submittingId === order.orderId"
            @click="onConfirmReceive(order)"
          >
            {{ submittingId === order.orderId ? "提交中…" : "确认收货" }}
          </button>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { confirmReceiveOrder, fetchMyOrders, payOrder } from "../../api/order";
import { orderStatusZh } from "../../utils/display-labels";

const list = ref([]);
const submittingId = ref(null);
const statusTab = ref("");

const tabs = [
  { label: "全部", value: "" },
  { label: "待支付", value: "PENDING_PAYMENT" },
  { label: "待发货", value: "PAID" },
  { label: "已发货", value: "SHIPPED" },
  { label: "已完成", value: "COMPLETED" }
];

const filteredList = computed(() => {
  if (!statusTab.value) return list.value;
  return list.value.filter((o) => o.orderStatus === statusTab.value);
});

function formatPrice(v) {
  if (v == null || v === "") return "-";
  return Number(v).toFixed(2);
}

function formatTime(t) {
  if (!t) return "-";
  return String(t).replace("T", " ").slice(0, 19);
}

async function loadData() {
  const res = await fetchMyOrders();
  list.value = res.data || [];
}

async function onPay(order) {
  submittingId.value = order.orderId;
  try {
    await payOrder({ orderNo: order.orderNo });
    window.alert("支付成功");
    await loadData();
  } catch (e) {
    window.alert(e?.message || "支付失败");
  } finally {
    submittingId.value = null;
  }
}

async function onConfirmReceive(order) {
  if (!window.confirm(`确认已收到订单【${order.orderNo}】的商品？`)) return;
  submittingId.value = order.orderId;
  try {
    await confirmReceiveOrder(order.orderId);
    window.alert("确认收货成功");
    await loadData();
  } catch (e) {
    window.alert(e?.message || "操作失败");
  } finally {
    submittingId.value = null;
  }
}

onMounted(loadData);
</script>

<style scoped>
.page {
  width: 100%;
}

.page-head {
  margin-bottom: 16px;
}

.title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tab {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.tab.active {
  background: #1677ff;
  color: #fff;
  border-color: #1677ff;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
  background: #fff;
  border-radius: 8px;
}

.orders {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
}

.order-head {
  padding: 12px 16px;
  background: #fafafa;
  font-size: 14px;
}

.head-row {
  margin: 4px 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.mono {
  font-family: ui-monospace, monospace;
  font-size: 13px;
}

.muted {
  color: #888;
  margin-right: 6px;
}

.status {
  font-weight: 600;
  color: #1677ff;
}

.pay-row {
  margin-top: 8px;
}

.pay {
  color: #e5484d;
  font-size: 18px;
}

.addr-box {
  padding: 10px 16px;
  font-size: 13px;
  color: #555;
  border-bottom: 1px solid #f0f0f0;
}

.addr-box p {
  margin: 4px 0 0;
}

.lines {
  list-style: none;
  margin: 0;
  padding: 12px 16px;
}

.line-item {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.line-item:last-child {
  border-bottom: none;
}

.line-img {
  width: 56px;
  height: 56px;
  border-radius: 4px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}

.line-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #ccc;
}

.line-name {
  font-weight: 500;
  font-size: 14px;
}

.line-meta {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}

.order-actions {
  padding: 12px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
  border-top: 1px solid #f0f0f0;
}

.btn {
  padding: 8px 18px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  border: none;
}

.btn.primary {
  background: #e5484d;
  color: #fff;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
