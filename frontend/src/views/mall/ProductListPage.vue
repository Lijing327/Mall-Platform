<template>
  <div class="page">
    <header class="page-head">
      <h1 class="title">全部商品</h1>
      <p class="subtitle">精选在售商品</p>
    </header>

    <section class="filter-bar">
      <input v-model="keyword" class="filter-input" placeholder="搜索商品名称" @keyup.enter="search" />
      <button type="button" class="btn-search" @click="search">搜索</button>
    </section>

    <div v-if="list.length === 0" class="empty">暂无商品，换个关键词试试～</div>

    <div v-else class="card-grid">
      <article v-for="item in list" :key="item.id" class="product-card">
        <RouterLink :to="`/products/${item.id}`" class="card-img-wrap">
          <img v-if="item.mainImage" :src="item.mainImage" :alt="item.productName" class="card-img" />
          <div v-else class="card-img placeholder">暂无图片</div>
        </RouterLink>
        <div class="card-body">
          <RouterLink :to="`/products/${item.id}`" class="card-title">{{ item.productName }}</RouterLink>
          <p v-if="item.productSubtitle" class="card-sub">{{ item.productSubtitle }}</p>
          <div class="card-price">￥{{ formatPrice(item.price) }}</div>
          <div class="card-stock">库存：{{ item.stock ?? "-" }}</div>
          <div class="card-actions">
            <RouterLink :to="`/products/${item.id}`" class="btn-outline">查看详情</RouterLink>
            <button type="button" class="btn-primary" :disabled="addingId === item.id" @click="quickAdd(item)">
              {{ addingId === item.id ? "加入中…" : "加入购物车" }}
            </button>
          </div>
        </div>
      </article>
    </div>

    <div v-if="list.length > 0" class="pager">
      <button type="button" class="btn-page" :disabled="pageNum <= 1" @click="prevPage">上一页</button>
      <span>第 {{ pageNum }} 页 / 共 {{ totalPage }} 页</span>
      <button type="button" class="btn-page" :disabled="pageNum >= totalPage" @click="nextPage">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { addCart } from "../../api/cart";
import { fetchProductList } from "../../api/product";
import { getToken } from "../../utils/user-context";

const route = useRoute();
const router = useRouter();
const keyword = ref("");
const pageNum = ref(1);
const pageSize = ref(12);
const total = ref(0);
const list = ref([]);
const addingId = ref(null);

const totalPage = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));

function formatPrice(v) {
  if (v == null || v === "") return "-";
  return Number(v).toFixed(2);
}

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
  router.replace({ path: "/products", query: keyword.value.trim() ? { keyword: keyword.value.trim() } : {} });
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

async function quickAdd(item) {
  if (!getToken()) {
    await router.push({ path: "/login", query: { redirect: route.fullPath } });
    return;
  }
  addingId.value = item.id;
  try {
    await addCart({ productId: item.id, quantity: 1 });
    window.alert("已加入购物车");
  } catch (e) {
    window.alert(e?.message || "加入失败");
  } finally {
    addingId.value = null;
  }
}

watch(
  () => route.query.keyword,
  () => {
    keyword.value = typeof route.query.keyword === "string" ? route.query.keyword : "";
    pageNum.value = 1;
    loadData();
  },
  { immediate: true }
);

onMounted(() => {
  if (route.query.needAdmin === "1") {
    window.alert("需要管理员账号登录（见后端 mall.auth.admin-user-ids）。");
  }
});
</script>

<style scoped>
.page {
  width: 100%;
}

.page-head {
  margin-bottom: 16px;
}

.title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #888;
}

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.filter-input {
  flex: 1;
  max-width: 360px;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
}

.btn-search {
  padding: 8px 20px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.empty {
  text-align: center;
  padding: 48px 16px;
  color: #999;
  background: #fff;
  border-radius: 8px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.card-img-wrap {
  display: block;
  aspect-ratio: 1;
  background: #f7f7f7;
  overflow: hidden;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-img.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
  font-size: 13px;
  height: 100%;
}

.card-body {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #222;
  text-decoration: none;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-title:hover {
  color: #1677ff;
}

.card-sub {
  margin: 0;
  font-size: 12px;
  color: #888;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-price {
  font-size: 18px;
  font-weight: 700;
  color: #e5484d;
  margin-top: 4px;
}

.card-stock {
  font-size: 12px;
  color: #666;
}

.card-actions {
  margin-top: auto;
  padding-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.btn-outline {
  padding: 6px 12px;
  border: 1px solid #1677ff;
  color: #1677ff;
  border-radius: 4px;
  text-decoration: none;
  font-size: 13px;
  text-align: center;
}

.btn-primary {
  flex: 1;
  min-width: 100px;
  padding: 6px 12px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pager {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #666;
}

.btn-page {
  padding: 6px 14px;
  border: 1px solid #d9d9d9;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
