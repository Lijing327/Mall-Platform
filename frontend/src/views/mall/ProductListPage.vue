<template>
  <div class="product-list-page">
    <header class="list-header">
      <h1>全部商品</h1>
      <p>清晰浏览、快速筛选、直接下单。</p>
    </header>

    <section class="search-panel">
      <div class="search-box">
        <input
          v-model.trim="keywordInput"
          type="search"
          placeholder="搜索你想购买的商品"
          @keyup.enter="applySearch"
        />
        <button type="button" class="search-btn" @click="applySearch">搜索</button>
      </div>
      <button type="button" class="chip-btn" :class="{ active: inStockOnly }" @click="toggleInStockOnly">
        仅看有货
      </button>
    </section>

    <section class="category-tabs">
      <button
        v-for="category in categories"
        :key="category.key"
        type="button"
        class="category-tab"
        :class="{ active: activeCategory === category.key }"
        @click="changeCategory(category.key)"
      >
        {{ category.label }}
      </button>
    </section>

    <section class="sort-bar">
      <button
        v-for="option in sortOptions"
        :key="option.key"
        type="button"
        class="sort-item"
        :class="{ active: sortBy === option.key }"
        @click="changeSort(option.key)"
      >
        {{ option.label }}
        <span v-if="option.key === 'price'">{{ sortBy === "price" ? (sortOrder === "asc" ? "↑" : "↓") : "↕" }}</span>
      </button>
    </section>

    <section v-if="loading" class="loading-grid">
      <article v-for="index in pageSize" :key="index" class="skeleton-card">
        <div class="skeleton-image"></div>
        <div class="skeleton-line w80"></div>
        <div class="skeleton-line w60"></div>
        <div class="skeleton-line w30"></div>
      </article>
    </section>

    <section v-else-if="error" class="state-card">
      <h3>商品加载失败，请稍后重试</h3>
      <p>{{ error }}</p>
      <button type="button" class="retry-btn" @click="loadData">重新加载</button>
    </section>

    <section v-else-if="filteredList.length === 0" class="state-card">
      <h3>暂无符合条件的商品</h3>
      <p>换个关键词试试，或者切换到其他分类。</p>
      <button type="button" class="retry-btn" @click="resetFilters">返回全部商品</button>
    </section>

    <section v-else class="grid-wrap">
      <div class="card-grid">
        <article v-for="item in paginatedList" :key="item.id" class="product-card">
          <RouterLink :to="`/products/${item.id}`" class="card-img-wrap">
            <img v-if="item.mainImage" :src="item.mainImage" :alt="item.productName" class="card-img" />
            <div v-else class="card-img placeholder">暂无图片</div>
            <span v-if="isLowStock(item)" class="stock-tag">库存紧张</span>
          </RouterLink>
          <div class="card-body">
            <RouterLink :to="`/products/${item.id}`" class="card-title">{{ item.productName }}</RouterLink>
            <p class="card-sub">{{ item.productSubtitle || "精选好物，支持快速下单" }}</p>
            <div class="card-price">¥{{ formatPrice(item.price) }}</div>
            <div class="card-meta">
              <span>{{ stockText(item.stock) }}</span>
              <span v-if="item.shop?.shopName">店铺：{{ item.shop.shopName }}</span>
            </div>
            <div class="card-actions">
              <RouterLink :to="`/products/${item.id}`" class="btn-outline">查看详情</RouterLink>
              <button
                type="button"
                class="btn-primary"
                :disabled="addingId === item.id || !canAddToCart(item)"
                @click="quickAdd(item)"
              >
                {{ canAddToCart(item) ? (addingId === item.id ? "加入中…" : "加入购物车") : "已售罄" }}
              </button>
            </div>
          </div>
        </article>
      </div>

      <div class="pager">
        <button type="button" class="btn-page" :disabled="pageNum <= 1" @click="goPrev">上一页</button>
        <span>第 {{ pageNum }} / {{ totalPage }} 页</span>
        <button type="button" class="btn-page" :disabled="pageNum >= totalPage" @click="goNext">下一页</button>
      </div>
    </section>
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

const loading = ref(false);
const error = ref("");
const addingId = ref(null);
const allList = ref([]);

const pageNum = ref(1);
const pageSize = 12;
const keywordInput = ref("");
const keyword = ref("");
const activeCategory = ref("all");
const sortBy = ref("comprehensive");
const sortOrder = ref("desc");
const inStockOnly = ref(false);

const categories = [
  { key: "all", label: "全部" },
  { key: "digital", label: "手机数码" },
  { key: "appliance", label: "家用电器" },
  { key: "home", label: "居家日用" },
  { key: "food", label: "食品生鲜" },
  { key: "other", label: "其他" }
];

const sortOptions = [
  { key: "comprehensive", label: "综合" },
  { key: "newest", label: "最新" },
  { key: "price", label: "价格" }
];

const filteredList = computed(() => {
  let list = [...allList.value];

  if (activeCategory.value !== "all") {
    list = list.filter((item) => getCategory(item) === activeCategory.value);
  }
  if (inStockOnly.value) {
    list = list.filter((item) => Number(item.stock || 0) > 0);
  }

  if (sortBy.value === "price") {
    list.sort((a, b) => {
      const pa = Number(a.price || 0);
      const pb = Number(b.price || 0);
      return sortOrder.value === "asc" ? pa - pb : pb - pa;
    });
  } else {
    list.sort((a, b) => Number(b.id || 0) - Number(a.id || 0));
  }

  return list;
});

const totalPage = computed(() => Math.max(1, Math.ceil(filteredList.value.length / pageSize)));

const paginatedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize;
  return filteredList.value.slice(start, start + pageSize);
});

function getCategory(item) {
  const text = `${item.productName || ""} ${item.productSubtitle || ""}`.toLowerCase();
  if (/手机|数码|耳机|电脑|键盘|鼠标|电子|digital|phone|ipad|laptop/.test(text)) {
    return "digital";
  }
  if (/空调|冰箱|洗衣机|电器|appliance/.test(text)) {
    return "appliance";
  }
  if (/食品|零食|生鲜|牛奶|水果|food/.test(text)) {
    return "food";
  }
  if (/家居|日用|收纳|清洁|home/.test(text)) {
    return "home";
  }
  return "other";
}

function stockText(stock) {
  const value = Number(stock || 0);
  if (value <= 0) {
    return "暂无库存";
  }
  if (value <= 5) {
    return `库存紧张：${value}件`;
  }
  return `库存：${value}件`;
}

function isLowStock(item) {
  const stock = Number(item.stock || 0);
  return stock > 0 && stock <= 5;
}

function canAddToCart(item) {
  return Number(item.stock || 0) > 0;
}

function formatPrice(v) {
  const num = Number(v);
  if (!Number.isFinite(num)) {
    return "0.00";
  }
  return num.toFixed(2);
}

async function loadData() {
  loading.value = true;
  error.value = "";
  try {
    const res = await fetchProductList({
      pageNum: 1,
      pageSize: 100,
      keyword: keyword.value || undefined
    });
    allList.value = res.data.list || [];
    if (pageNum.value > totalPage.value) {
      pageNum.value = totalPage.value;
    }
  } catch (e) {
    error.value = e?.message || "请求失败";
  } finally {
    loading.value = false;
  }
}

function syncRoute() {
  const query = {};
  if (keyword.value) query.keyword = keyword.value;
  if (activeCategory.value !== "all") query.category = activeCategory.value;
  if (sortBy.value !== "comprehensive") query.sortBy = sortBy.value;
  if (sortBy.value === "price") query.sortOrder = sortOrder.value;
  if (inStockOnly.value) query.inStock = "1";
  if (pageNum.value > 1) query.page = String(pageNum.value);
  router.replace({ path: "/products", query });
}

function applySearch() {
  keyword.value = keywordInput.value.trim();
  pageNum.value = 1;
  syncRoute();
  loadData();
}

function changeCategory(category) {
  if (activeCategory.value === category) return;
  activeCategory.value = category;
  pageNum.value = 1;
  syncRoute();
}

function changeSort(key) {
  if (key === "price") {
    if (sortBy.value !== "price") {
      sortBy.value = "price";
      sortOrder.value = "asc";
    } else {
      sortOrder.value = sortOrder.value === "asc" ? "desc" : "asc";
    }
  } else {
    sortBy.value = key;
  }
  pageNum.value = 1;
  syncRoute();
}

function toggleInStockOnly() {
  inStockOnly.value = !inStockOnly.value;
  pageNum.value = 1;
  syncRoute();
}

function goPrev() {
  if (pageNum.value <= 1) return;
  pageNum.value -= 1;
  syncRoute();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function goNext() {
  if (pageNum.value >= totalPage.value) return;
  pageNum.value += 1;
  syncRoute();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function resetFilters() {
  keyword.value = "";
  keywordInput.value = "";
  activeCategory.value = "all";
  sortBy.value = "comprehensive";
  sortOrder.value = "desc";
  inStockOnly.value = false;
  pageNum.value = 1;
  syncRoute();
  loadData();
}

async function quickAdd(item) {
  if (!getToken()) {
    await router.push({ path: "/login", query: { redirect: route.fullPath } });
    return;
  }
  if (!canAddToCart(item)) {
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
  () => route.query,
  (query) => {
    keyword.value = typeof query.keyword === "string" ? query.keyword : "";
    keywordInput.value = keyword.value;
    activeCategory.value = typeof query.category === "string" ? query.category : "all";
    sortBy.value = typeof query.sortBy === "string" ? query.sortBy : "comprehensive";
    sortOrder.value = query.sortOrder === "asc" ? "asc" : "desc";
    inStockOnly.value = query.inStock === "1";
    pageNum.value = Number(query.page || 1) > 0 ? Number(query.page || 1) : 1;
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
.product-list-page {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.list-header h1 {
  margin: 0;
  font-size: 30px;
  color: #1e293b;
}

.list-header p {
  margin: 8px 0 0;
  color: #64748b;
}

.search-panel {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  padding: 14px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
}

.search-box {
  flex: 1;
  min-width: 260px;
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #d5deeb;
}

.search-box input {
  flex: 1;
  padding: 10px 12px;
  border: none;
  outline: none;
}

.search-btn {
  min-width: 88px;
  border: none;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

.chip-btn {
  border: 1px solid #d5deeb;
  background: #fff;
  color: #334155;
  border-radius: 999px;
  min-height: 36px;
  padding: 0 14px;
  cursor: pointer;
}

.chip-btn.active {
  border-color: #2563eb;
  color: #2563eb;
  background: #eff6ff;
}

.category-tabs,
.sort-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-tab,
.sort-item {
  border: 1px solid #d9e1ee;
  background: #fff;
  border-radius: 999px;
  min-height: 34px;
  padding: 0 14px;
  cursor: pointer;
  color: #334155;
}

.category-tab.active,
.sort-item.active {
  border-color: #2563eb;
  color: #2563eb;
  background: #eef4ff;
}

.grid-wrap {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.card-grid,
.loading-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.product-card,
.skeleton-card {
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e5ebf5;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
}

.product-card {
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 24px rgba(15, 23, 42, 0.1);
}

.card-img-wrap {
  display: block;
  position: relative;
  aspect-ratio: 1;
  background: #f6f8fc;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-img.placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #94a3b8;
}

.stock-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 3px 8px;
  border-radius: 999px;
  background: #fff7ed;
  color: #c2410c;
  font-size: 11px;
  font-weight: 600;
}

.card-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-title {
  color: #0f172a;
  font-weight: 600;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-sub {
  margin: 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.4;
}

.card-price {
  color: #dc2626;
  font-size: 22px;
  font-weight: 700;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
}

.card-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.btn-outline,
.btn-primary {
  flex: 1;
  min-height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  font-size: 13px;
}

.btn-outline {
  border: 1px solid #2563eb;
  color: #2563eb;
  background: #fff;
}

.btn-primary {
  border: none;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

.btn-primary:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.state-card {
  text-align: center;
  padding: 56px 20px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
}

.state-card h3 {
  margin: 0 0 8px;
  font-size: 20px;
}

.state-card p {
  margin: 0 0 14px;
  color: #64748b;
}

.retry-btn {
  min-width: 120px;
  min-height: 36px;
  border: none;
  border-radius: 8px;
  background: #2563eb;
  color: #fff;
  cursor: pointer;
}

.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  color: #475569;
}

.btn-page {
  min-height: 34px;
  padding: 0 14px;
  border: 1px solid #dbe3ef;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.skeleton-card {
  padding: 12px;
}

.skeleton-image,
.skeleton-line {
  border-radius: 10px;
  background: linear-gradient(90deg, #eef2f7 25%, #f8fafc 37%, #eef2f7 63%);
  background-size: 400% 100%;
  animation: shimmer 1.2s infinite;
}

.skeleton-image {
  aspect-ratio: 1;
  margin-bottom: 12px;
}

.skeleton-line {
  height: 12px;
  margin-bottom: 8px;
}

.skeleton-line.w80 {
  width: 80%;
}

.skeleton-line.w60 {
  width: 60%;
}

.skeleton-line.w30 {
  width: 30%;
}

@keyframes shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}

@media (max-width: 1100px) {
  .card-grid,
  .loading-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .card-grid,
  .loading-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .list-header h1 {
    font-size: 24px;
  }

  .card-grid,
  .loading-grid {
    grid-template-columns: 1fr;
  }
}
</style>
