<template>
  <div class="card">
    <div class="page-mark product">
      <h3>全平台商品中心</h3>
      <p>查看所有店铺商品，支持按状态筛选并执行管理员下架。</p>
    </div>
    <div class="toolbar">
      <input v-model="keyword" placeholder="商品名称关键字" />
      <select v-model="saleStatus">
        <option value="">全部状态</option>
        <option value="ON_SHELF">已上架</option>
        <option value="OFF_SHELF">已下架</option>
      </select>
      <button class="btn" @click="loadData">查询</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>商品ID</th>
          <th>店铺ID</th>
          <th>商品名</th>
          <th>价格</th>
          <th>库存</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.productId">
          <td>{{ item.productId }}</td>
          <td>{{ item.shopId }}</td>
          <td>{{ item.productName }}</td>
          <td>{{ item.price }}</td>
          <td>{{ item.stock }}</td>
          <td>{{ saleStatusZh(item.saleStatus) }}</td>
          <td>
            <button class="btn secondary" :disabled="item.saleStatus === 'OFF_SHELF'" @click="offShelf(item)">
              下架
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { adminOffShelfProduct, fetchAdminProducts } from "../../api/admin";
import { saleStatusZh } from "../../utils/display-labels";

const list = ref([]);
const keyword = ref("");
const saleStatus = ref("");

async function loadData() {
  const res = await fetchAdminProducts({
    pageNum: 1,
    pageSize: 100,
    keyword: keyword.value || undefined,
    saleStatus: saleStatus.value || undefined
  });
  list.value = res.data.list || [];
}

async function offShelf(item) {
  await adminOffShelfProduct(item.productId);
  await loadData();
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
.page-mark.product {
  background: #f4fbf4;
  border: 1px solid #dff2df;
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
