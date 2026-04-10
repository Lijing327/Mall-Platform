<template>
  <div class="card">
    <h3>我的订单</h3>
    <p>当前用户ID：{{ userId }}</p>
    <table class="table">
      <thead>
        <tr>
          <th>订单号</th>
          <th>状态</th>
          <th>实付金额</th>
          <th>支付方式</th>
          <th>支付时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.orderId">
          <td>{{ item.orderNo }}</td>
          <td>{{ item.orderStatus }}</td>
          <td>{{ item.payAmount }}</td>
          <td>{{ item.payType || "-" }}</td>
          <td>{{ item.payTime || "-" }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchMyOrders } from "../api/order";
import { getUserId } from "../utils/user-context";

const userId = getUserId();
const list = ref([]);

async function loadData() {
  const res = await fetchMyOrders({ pageNum: 1, pageSize: 100 });
  const all = res.data.list || [];
  // 当前后端暂无用户订单列表接口，这里先按 userId 过滤管理员列表结果用于 MVP 联调
  list.value = all.filter((item) => Number(item.userId) === Number(userId));
}

onMounted(loadData);
</script>

<style scoped>
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
