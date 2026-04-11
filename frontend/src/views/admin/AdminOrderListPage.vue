<template>
  <div class="card">
    <div class="page-mark order">
      <h3>全平台订单中心</h3>
      <p>查看所有用户订单，按状态和订单号筛选追踪交易流转。</p>
    </div>
    <div class="toolbar">
      <select v-model="orderStatus">
        <option value="">全部状态</option>
        <option value="PENDING_PAYMENT">待支付</option>
        <option value="PAID">已支付</option>
        <option value="SHIPPED">已发货</option>
        <option value="COMPLETED">已完成</option>
      </select>
      <input v-model="orderNo" placeholder="订单号模糊搜索" />
      <button class="btn" @click="loadData">查询</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>订单ID</th>
          <th>订单号</th>
          <th>用户ID</th>
          <th>状态</th>
          <th>实付金额</th>
          <th>支付方式</th>
          <th>支付时间</th>
          <th>快递单号</th>
          <th>发货时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.orderId">
          <td>{{ item.orderId }}</td>
          <td>{{ item.orderNo }}</td>
          <td>{{ item.userId }}</td>
          <td>{{ item.orderStatus }}</td>
          <td>{{ item.payAmount }}</td>
          <td>{{ item.payType || "-" }}</td>
          <td>{{ item.payTime || "-" }}</td>
          <td>{{ item.shippingNo || "-" }}</td>
          <td>{{ item.shipTime || "-" }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchAdminOrders } from "../../api/admin";

const list = ref([]);
const orderStatus = ref("");
const orderNo = ref("");

async function loadData() {
  const res = await fetchAdminOrders({
    pageNum: 1,
    pageSize: 100,
    orderStatus: orderStatus.value || undefined,
    orderNo: orderNo.value || undefined
  });
  list.value = res.data.list || [];
}

onMounted(loadData);
</script>

<style scoped>
.page-mark {
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
}
.page-mark h3 {
  margin: 0 0 6px;
}
.page-mark p {
  margin: 0;
  color: #5c6370;
  font-size: 13px;
}
.page-mark.order {
  background: #f7f5ff;
  border: 1px solid #e7e2ff;
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
input,
select {
  padding: 6px 8px;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  border-bottom: 1px solid #eee;
  padding: 8px 6px;
}
</style>
