<template>
  <div class="card">
    <h3>我的订单</h3>
    <p>当前用户ID：{{ userId ?? "-" }}</p>
    <div v-if="list.length === 0" class="empty">暂无订单</div>
    <div v-else class="orders">
      <div v-for="order in list" :key="order.orderId" class="order-block">
        <div class="order-head">
          <span class="muted">订单号</span> {{ order.orderNo }}
          <span class="sep">|</span>
          <span>{{ orderStatusZh(order.orderStatus) }}</span>
          <span class="sep">|</span>
          <span>实付 {{ order.payAmount }}</span>
          <span v-if="order.payType" class="sep">|</span>
          <span v-if="order.payType">{{ payTypeZh(order.payType) }}</span>
          <span class="sep">|</span>
          <span class="muted">支付</span> {{ order.payTime || "-" }}
          <span v-if="order.completeTime" class="sep">|</span>
          <span v-if="order.completeTime" class="muted">完成</span>
          <span v-if="order.completeTime">{{ order.completeTime }}</span>
          <span v-if="order.orderStatus === 'SHIPPED'" class="head-actions">
            <button type="button" class="btn-sm" :disabled="submittingId === order.orderId" @click="onConfirmReceive(order)">
              {{ submittingId === order.orderId ? "提交中…" : "确认收货" }}
            </button>
          </span>
        </div>
        <div v-if="order.receiverName || order.remark || order.shippingNo || order.shipTime" class="order-extra">
          <p v-if="order.receiverName">
            <span class="muted">收货人</span>{{ order.receiverName }}
            <span v-if="order.receiverMobile" class="muted-sep">·</span>
            <span v-if="order.receiverMobile">{{ order.receiverMobile }}</span>
          </p>
          <p v-if="order.receiverAddress">
            <span class="muted">收货地址</span>{{ order.receiverAddress }}
          </p>
          <p v-if="order.remark"><span class="muted">备注</span>{{ order.remark }}</p>
          <template v-if="order.shippingNo || order.shipTime">
            <p><span class="muted">快递单号</span>{{ order.shippingNo || "-" }}</p>
            <p v-if="order.shippingRemark"><span class="muted">发货说明</span>{{ order.shippingRemark }}</p>
            <p><span class="muted">发货时间</span>{{ order.shipTime || "-" }}</p>
          </template>
        </div>
        <table class="table sub">
          <thead>
            <tr>
              <th>商品</th>
              <th>单价</th>
              <th>数量</th>
              <th>小计</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(line, idx) in order.items" :key="idx">
              <td>{{ line.productName }}</td>
              <td>{{ line.productPrice }}</td>
              <td>{{ line.quantity }}</td>
              <td>{{ line.itemAmount }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { confirmReceiveOrder, fetchMyOrders } from "../api/order";
import { orderStatusZh, payTypeZh } from "../utils/display-labels";
import { getUserId } from "../utils/user-context";

const userId = ref(getUserId());
const list = ref([]);
const submittingId = ref(null);

async function loadData() {
  const res = await fetchMyOrders();
  list.value = res.data || [];
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

function refreshUser() {
  userId.value = getUserId();
}

onMounted(() => {
  refreshUser();
  loadData();
  window.addEventListener("mall-auth-changed", refreshUser);
});

onUnmounted(() => {
  window.removeEventListener("mall-auth-changed", refreshUser);
});
</script>

<style scoped>
.empty {
  color: #888;
  padding: 12px 0;
}
.orders {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.order-block {
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}
.order-head {
  background: #fafafa;
  padding: 10px 12px;
  font-size: 14px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 0;
}
.head-actions {
  margin-left: auto;
}
.btn-sm {
  padding: 4px 10px;
  font-size: 13px;
  border-radius: 4px;
  border: 1px solid #1677ff;
  background: #1677ff;
  color: #fff;
  cursor: pointer;
}
.btn-sm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.muted {
  color: #888;
}
.sep {
  margin: 0 8px;
  color: #ccc;
}
.order-extra {
  padding: 8px 12px;
  font-size: 13px;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.order-extra .muted {
  color: #888;
  margin-right: 6px;
}
.order-extra .muted-sep {
  color: #ccc;
  margin: 0 6px;
}
.order-extra p {
  margin: 4px 0;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table.sub th,
.table.sub td {
  border-bottom: 1px solid #eee;
  padding: 8px 12px;
  text-align: left;
}
.table.sub th {
  font-weight: 600;
  font-size: 13px;
  background: #fff;
}
</style>
