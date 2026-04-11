<template>
  <div class="card">
    <h3>商品列表</h3>
    <div class="toolbar">
      <input v-model="keyword" placeholder="请输入商品名称关键字" />
      <button class="btn" @click="search">搜索</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>商品名</th>
          <th>价格</th>
          <th>库存</th>
          <th>店铺</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.productName }}</td>
          <td>{{ item.price }}</td>
          <td>{{ item.stock }}</td>
          <td>{{ item.shop?.shopName || "-" }}</td>
          <td>
            <RouterLink :to="`/products/${item.id}`">查看详情</RouterLink>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="pager">
      <button class="btn secondary" :disabled="pageNum <= 1" @click="prevPage">上一页</button>
      <span>第 {{ pageNum }} 页 / 共 {{ totalPage }} 页</span>
      <button class="btn secondary" :disabled="pageNum >= totalPage" @click="nextPage">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import { fetchProductList } from "../api/product";

const route = useRoute();
const keyword = ref("");
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const list = ref([]);

const totalPage = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));

async function loadData() {
  const res = await fetchProductList({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value || undefined
  });
  list.value = res.data.list || [];
  total.value = Number(res.data.total || 0);
}

function search() {
  pageNum.value = 1;
  loadData();
}

function prevPage() {
  if (pageNum.value > 1) {
    pageNum.value -= 1;
    loadData();
  }
}

function nextPage() {
  if (pageNum.value < totalPage.value) {
    pageNum.value += 1;
    loadData();
  }
}

onMounted(() => {
  if (route.query.needAdmin === "1") {
    window.alert("需要管理员账号登录（见后端 mall.auth.admin-user-ids）。");
  }
  loadData();
});
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
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
  text-align: left;
}
.pager {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
