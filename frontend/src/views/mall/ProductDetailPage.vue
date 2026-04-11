<template>
  <div v-if="detail" class="page">
    <div class="main-block">
      <div class="gallery">
        <div class="main-img-wrap">
          <img v-if="detail.mainImage" :src="detail.mainImage" :alt="detail.productName" class="main-img" />
          <div v-else class="main-img placeholder">暂无图片</div>
        </div>
      </div>
      <div class="buy-panel">
        <h1 class="name">{{ detail.productName }}</h1>
        <p v-if="detail.productSubtitle" class="subtitle">{{ detail.productSubtitle }}</p>
        <div class="price-row">
          <span class="price-label">价格</span>
          <span class="price">￥{{ formatPrice(detail.price) }}</span>
        </div>
        <div class="stock-row">库存：{{ detail.stock ?? "-" }}</div>
        <div class="qty-row">
          <span class="qty-label">数量</span>
          <div class="qty">
            <button type="button" class="qty-btn" :disabled="quantity <= 1" @click="quantity = Math.max(1, quantity - 1)">
              −
            </button>
            <span class="qty-value">{{ quantity }}</span>
            <button type="button" class="qty-btn" @click="quantity += 1">+</button>
          </div>
        </div>
        <div class="actions">
          <button type="button" class="btn-cart" @click="addToCartAction">加入购物车</button>
        </div>
      </div>
    </div>

    <section class="detail-section">
      <h2 class="section-title">商品详情</h2>
      <div class="detail-html" v-html="detailHtml"></div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { addCart } from "../../api/cart";
import { fetchProductDetail } from "../../api/product";
import { getToken } from "../../utils/user-context";

const route = useRoute();
const router = useRouter();
const detail = ref(null);
const quantity = ref(1);

const detailHtml = computed(() => {
  const raw = detail.value?.detail;
  if (!raw || !String(raw).trim()) {
    return "<p>暂无图文详情。</p>";
  }
  return raw;
});

function formatPrice(v) {
  if (v == null || v === "") return "-";
  return Number(v).toFixed(2);
}

async function loadData() {
  const res = await fetchProductDetail(route.params.id);
  detail.value = res.data;
}

async function addToCartAction() {
  if (!getToken()) {
    await router.push({ path: "/login", query: { redirect: route.fullPath } });
    return;
  }
  await addCart({
    productId: Number(route.params.id),
    quantity: quantity.value
  });
  window.alert("已加入购物车");
}

onMounted(loadData);
</script>

<style scoped>
.page {
  width: 100%;
}

.main-block {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #eee;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .main-block {
    grid-template-columns: 1fr;
  }
}

.gallery {
  min-width: 0;
}

.main-img-wrap {
  aspect-ratio: 1;
  background: #f7f7f7;
  border-radius: 8px;
  overflow: hidden;
}

.main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.main-img.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
  height: 100%;
}

.buy-panel {
  min-width: 0;
}

.name {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.4;
}

.subtitle {
  margin: 0 0 16px;
  font-size: 14px;
  color: #666;
}

.price-row {
  margin-bottom: 12px;
}

.price-label {
  font-size: 14px;
  color: #888;
  margin-right: 8px;
}

.price {
  font-size: 26px;
  font-weight: 700;
  color: #e5484d;
}

.stock-row {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.qty-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.qty-label {
  font-size: 14px;
  color: #333;
}

.qty {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #fafafa;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
}

.qty-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.qty-value {
  min-width: 40px;
  text-align: center;
  font-size: 15px;
}

.actions {
  display: flex;
  gap: 12px;
}

.btn-cart {
  padding: 12px 32px;
  font-size: 16px;
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-cart:hover {
  background: #4096ff;
}

.detail-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #eee;
}

.section-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

.detail-html {
  font-size: 14px;
  color: #444;
  line-height: 1.7;
}

.detail-html :deep(img) {
  max-width: 100%;
  height: auto;
}
</style>
