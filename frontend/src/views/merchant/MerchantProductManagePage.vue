<template>
  <div class="card">
    <div class="toolbar">
      <h3>商品管理</h3>
      <RouterLink class="btn" to="/merchant/products/new">新增商品</RouterLink>
    </div>
    <div class="toolbar">
      <input v-model="keyword" placeholder="关键字搜索商品" />
      <button class="btn" @click="search">搜索</button>
    </div>
    <table class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>商品名</th>
          <th>价格</th>
          <th>库存</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.productName }}</td>
          <td>{{ item.price }}</td>
          <td>{{ item.stock }}</td>
          <td>{{ saleStatusZh(item.saleStatus) }}</td>
          <td class="ops">
            <RouterLink :to="`/merchant/products/${item.id}/edit`">编辑</RouterLink>
            <a href="javascript:void(0)" @click="toggleShelf(item)">
              {{ item.saleStatus === "ON_SHELF" ? "下架" : "上架" }}
            </a>
            <a href="javascript:void(0)" @click="removeProduct(item)">删除</a>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import {
  deleteMerchantProduct,
  fetchMerchantProducts,
  offShelfMerchantProduct,
  onShelfMerchantProduct
} from "../../api/merchant";
import { saleStatusZh } from "../../utils/display-labels";
import { getMerchantId } from "../../utils/user-context";

const merchantId = getMerchantId();
const keyword = ref("");
const list = ref([]);

async function loadData() {
  const res = await fetchMerchantProducts({
    merchantId,
    pageNum: 1,
    pageSize: 100,
    keyword: keyword.value || undefined
  });
  list.value = res.data.list || [];
}

function search() {
  loadData();
}

async function toggleShelf(item) {
  if (item.saleStatus === "ON_SHELF") {
    await offShelfMerchantProduct(item.id, { merchantId });
  } else {
    await onShelfMerchantProduct(item.id, { merchantId });
  }
  await loadData();
}

async function removeProduct(item) {
  if (!window.confirm(`确认删除商品【${item.productName}】吗？`)) return;
  await deleteMerchantProduct(item.id, { merchantId });
  await loadData();
}

onMounted(loadData);
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}
input {
  width: 260px;
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
.ops {
  display: flex;
  gap: 10px;
}
</style>
