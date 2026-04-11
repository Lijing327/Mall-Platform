<template>
  <div class="card">
    <h3>店铺订单</h3>
    <table class="table">
      <thead>
        <tr>
          <th>订单ID</th>
          <th>订单号</th>
          <th>状态</th>
          <th>金额</th>
          <th>快递单号</th>
          <th>发货时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.orderId">
          <td>{{ item.orderId }}</td>
          <td>{{ item.orderNo }}</td>
          <td>{{ orderStatusZh(item.orderStatus) }}</td>
          <td>{{ item.payAmount }}</td>
          <td>{{ item.shippingNo || "-" }}</td>
          <td>{{ item.shipTime || "-" }}</td>
          <td class="ops">
            <a href="javascript:void(0)" @click="viewDetail(item.orderId)">详情</a>
            <a
              href="javascript:void(0)"
              v-if="item.orderStatus === 'PAID'"
              @click="shipOrder(item.orderId)"
            >
              发货
            </a>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="detail" class="detail card">
      <h4>订单详情：{{ detail.orderNo }}</h4>
      <p>状态：{{ orderStatusZh(detail.orderStatus) }}</p>
      <p>收货信息：{{ detail.receiverName || "-" }} {{ detail.receiverMobile || "-" }}</p>
      <p>地址：{{ detail.receiverAddress || "-" }}</p>
      <p v-if="detail.remark"><span class="muted">用户备注</span> {{ detail.remark }}</p>
      <template v-if="detail.shippingNo || detail.shipTime">
        <p><span class="muted">快递单号</span> {{ detail.shippingNo || "-" }}</p>
        <p v-if="detail.shippingRemark"><span class="muted">发货备注</span> {{ detail.shippingRemark }}</p>
        <p><span class="muted">发货时间</span> {{ detail.shipTime || "-" }}</p>
      </template>
      <table class="table">
        <thead>
          <tr>
            <th>商品</th>
            <th>单价</th>
            <th>数量</th>
            <th>小计</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in detail.items || []" :key="row.orderItemId">
            <td>{{ row.productName }}</td>
            <td>{{ row.productPrice }}</td>
            <td>{{ row.quantity }}</td>
            <td>{{ row.itemAmount }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchMerchantOrderDetail, fetchMerchantOrders, shipMerchantOrder } from "../../api/merchant";
import { orderStatusZh } from "../../utils/display-labels";
import { getMerchantId } from "../../utils/user-context";

const merchantId = getMerchantId();
const list = ref([]);
const detail = ref(null);

async function loadData() {
  const res = await fetchMerchantOrders({ merchantId, pageNum: 1, pageSize: 100 });
  list.value = res.data.list || [];
}

async function viewDetail(orderId) {
  const res = await fetchMerchantOrderDetail(orderId, { merchantId });
  detail.value = res.data;
}

async function shipOrder(orderId) {
  const shippingNo = window.prompt("请输入快递单号");
  if (!shippingNo) return;
  const shippingRemark = window.prompt("请输入发货备注（可为空）") || "";
  await shipMerchantOrder(orderId, { merchantId, shippingNo, shippingRemark });
  window.alert("发货成功");
  await loadData();
  await viewDetail(orderId);
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
.ops {
  display: flex;
  gap: 10px;
}
.detail {
  margin-top: 14px;
}
.muted {
  color: #888;
  margin-right: 6px;
}
</style>
