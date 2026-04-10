<template>
  <div class="card">
    <h3>全平台商品列表</h3>
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
          <td>{{ item.saleStatus }}</td>
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
