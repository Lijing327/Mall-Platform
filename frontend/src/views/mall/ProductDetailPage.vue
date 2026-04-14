<template>
  <div class="product-detail-page">
    <nav class="breadcrumb">
      <RouterLink to="/products">全部商品</RouterLink>
      <span>/</span>
      <RouterLink to="/products">商品列表</RouterLink>
      <span>/</span>
      <span class="current">{{ detail?.productName || "商品详情" }}</span>
    </nav>

    <div v-if="loading" class="detail-loading">
      <div class="skeleton hero"></div>
      <div class="detail-loading-grid">
        <div class="skeleton image"></div>
        <div class="skeleton summary"></div>
      </div>
      <div class="skeleton section"></div>
    </div>

    <div v-else-if="error" class="empty-state">
      <h2>商品不存在或已下架</h2>
      <p>{{ error }}</p>
      <RouterLink class="back-link" to="/products">返回商品列表</RouterLink>
    </div>

    <template v-else-if="detail">
      <section class="hero-card">
        <div class="gallery-panel">
          <div class="main-image-frame">
            <img
              v-if="detail.mainImage"
              :src="detail.mainImage"
              :alt="detail.productName"
              class="main-image"
            />
            <div v-else class="image-placeholder">暂无图片</div>
          </div>

          <div class="thumb-strip">
            <button type="button" class="thumb is-active">
              <img v-if="detail.mainImage" :src="detail.mainImage" :alt="detail.productName" />
              <span v-else>图</span>
            </button>
          </div>
        </div>

        <div class="summary-panel">
          <p class="summary-tag">自营商品</p>
          <h1 class="product-name">{{ detail.productName }}</h1>
          <p class="product-subtitle">{{ productSubtitle }}</p>

          <div class="price-card">
            <span class="price-label">价格</span>
            <strong class="price-value">¥{{ formatPrice(detail.price) }}</strong>
          </div>

          <div class="meta-list">
            <div class="meta-item">
              <span class="meta-label">库存</span>
              <strong :class="['meta-value', stockStateClass]">{{ stockText }}</strong>
            </div>
            <div class="meta-item">
              <span class="meta-label">商品编号</span>
              <strong class="meta-value">{{ detail.productSn || "暂无" }}</strong>
            </div>
            <div class="meta-item" v-if="detail.shop?.shopName">
              <span class="meta-label">店铺</span>
              <strong class="meta-value">{{ detail.shop.shopName }}</strong>
            </div>
          </div>

          <div class="quantity-row">
            <span class="quantity-label">数量</span>
            <div class="quantity-selector">
              <button
                type="button"
                class="quantity-btn"
                :disabled="quantity <= 1"
                @click="decreaseQuantity"
              >
                -
              </button>
              <span class="quantity-value">{{ quantity }}</span>
              <button
                type="button"
                class="quantity-btn"
                :disabled="quantity >= maxPurchasable"
                @click="increaseQuantity"
              >
                +
              </button>
            </div>
          </div>

          <div class="action-area">
            <button
              type="button"
              class="add-cart-button"
              :disabled="!canPurchase || adding"
              @click="addToCartAction"
            >
              {{ adding ? "加入中…" : "加入购物车" }}
            </button>
            <p class="action-hint" :class="{ danger: !canPurchase }">
              {{ purchaseHint }}
            </p>
            <p v-if="successMessage" class="action-success">{{ successMessage }}</p>
          </div>

          <div class="assist-card">
            <h3>购买说明</h3>
            <ul>
              <li>支持模拟支付流程，下单后可在订单页查看状态。</li>
              <li>当前版本为自营商城一期演示版。</li>
              <li>发货后可在“我的订单”中继续完成收货流程。</li>
            </ul>
          </div>
        </div>
      </section>

      <section class="content-section">
        <div class="section-head">
          <h2>商品详情</h2>
          <p>查看商品说明与图文介绍。</p>
        </div>
        <div class="detail-content" v-html="detailHtml"></div>
      </section>

      <section class="content-section">
        <div class="section-head">
          <h2>商品参数</h2>
          <p>一期先展示已有字段，后续可继续扩展。</p>
        </div>
        <div class="params-grid">
          <div class="param-item">
            <span>商品名称</span>
            <strong>{{ detail.productName }}</strong>
          </div>
          <div class="param-item">
            <span>商品编号</span>
            <strong>{{ detail.productSn || "暂无" }}</strong>
          </div>
          <div class="param-item">
            <span>价格</span>
            <strong>¥{{ formatPrice(detail.price) }}</strong>
          </div>
          <div class="param-item">
            <span>库存</span>
            <strong>{{ stockText }}</strong>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { addCart } from "../../api/cart";
import { fetchProductDetail } from "../../api/product";
import { getToken } from "../../utils/user-context";

const route = useRoute();
const router = useRouter();

const detail = ref(null);
const loading = ref(true);
const error = ref("");
const quantity = ref(1);
const adding = ref(false);
const successMessage = ref("");

const stock = computed(() => Math.max(0, Number(detail.value?.stock || 0)));
const maxPurchasable = computed(() => Math.max(1, stock.value || 1));
const canPurchase = computed(() => !!detail.value && stock.value > 0);

const productSubtitle = computed(() => {
  if (detail.value?.productSubtitle && String(detail.value.productSubtitle).trim()) {
    return detail.value.productSubtitle;
  }
  return "精选自营商品，适合当前商城一期场景使用。";
});

const stockText = computed(() => {
  if (stock.value <= 0) {
    return "暂无库存";
  }
  if (stock.value <= 3) {
    return `库存紧张，仅剩 ${stock.value} 件`;
  }
  return `库存 ${stock.value} 件`;
});

const stockStateClass = computed(() => {
  if (stock.value <= 0) {
    return "danger";
  }
  if (stock.value <= 3) {
    return "warning";
  }
  return "";
});

const purchaseHint = computed(() => {
  if (!canPurchase.value) {
    return "该商品暂不可购买";
  }
  return "加入购物车后可前往购物车统一结算。";
});

const detailHtml = computed(() => {
  const raw = detail.value?.detail;
  if (!raw || !String(raw).trim()) {
    return "<p>暂无商品详情说明。</p>";
  }

  const content = String(raw).trim();
  const seemsHtml = /<[^>]+>/.test(content);
  if (seemsHtml) {
    return content;
  }
  return content
    .split(/\n+/)
    .filter(Boolean)
    .map((line) => `<p>${escapeHtml(line)}</p>`)
    .join("");
});

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function formatPrice(v) {
  if (v == null || v === "" || Number.isNaN(Number(v))) {
    return "0.00";
  }
  return Number(v).toFixed(2);
}

function decreaseQuantity() {
  quantity.value = Math.max(1, quantity.value - 1);
}

function increaseQuantity() {
  quantity.value = Math.min(maxPurchasable.value, quantity.value + 1);
}

async function loadData() {
  loading.value = true;
  error.value = "";
  successMessage.value = "";

  try {
    const res = await fetchProductDetail(route.params.id);
    if (!res?.data) {
      throw new Error("商品不存在或已下架");
    }
    detail.value = res.data;
    quantity.value = 1;
  } catch (e) {
    detail.value = null;
    error.value = e?.message || "商品详情加载失败";
  } finally {
    loading.value = false;
  }
}

async function addToCartAction() {
  if (!detail.value || !canPurchase.value) {
    return;
  }
  if (!getToken()) {
    await router.push({ path: "/login", query: { redirect: route.fullPath } });
    return;
  }

  adding.value = true;
  successMessage.value = "";

  try {
    await addCart({
      productId: Number(route.params.id),
      quantity: quantity.value
    });
    successMessage.value = "已加入购物车";
  } catch (e) {
    window.alert(e?.message || "加入购物车失败");
  } finally {
    adding.value = false;
  }
}

onMounted(loadData);
</script>

<style scoped>
.product-detail-page {
  width: 100%;
}

.breadcrumb {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
  color: #8a94a6;
  font-size: 13px;
}

.breadcrumb a {
  color: #4f5d75;
}

.breadcrumb .current {
  color: #1f2937;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-loading-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  gap: 22px;
}

.skeleton {
  border-radius: 20px;
  background: linear-gradient(90deg, #eef2f7 25%, #f8fafc 37%, #eef2f7 63%);
  background-size: 400% 100%;
  animation: shimmer 1.3s infinite;
}

.skeleton.hero,
.skeleton.section {
  height: 120px;
}

.skeleton.image {
  min-height: 540px;
}

.skeleton.summary {
  min-height: 540px;
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  gap: 28px;
  padding: 28px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
  border: 1px solid #e8ecf3;
  border-radius: 24px;
  box-shadow: 0 20px 48px rgba(15, 23, 42, 0.06);
}

.gallery-panel,
.summary-panel {
  min-width: 0;
}

.main-image-frame {
  aspect-ratio: 1;
  border-radius: 24px;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(22, 119, 255, 0.12), transparent 220px),
    #f7f9fc;
  border: 1px solid #edf1f6;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #97a0af;
  font-size: 15px;
}

.thumb-strip {
  margin-top: 14px;
  display: flex;
  gap: 10px;
}

.thumb {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  border: 1px solid #dce3ef;
  background: #fff;
  overflow: hidden;
  padding: 0;
}

.thumb.is-active {
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.12);
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.summary-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  margin: 0 0 14px;
  border-radius: 999px;
  background: #edf4ff;
  color: #1677ff;
  font-size: 12px;
  font-weight: 600;
}

.product-name {
  margin: 0;
  font-size: 30px;
  line-height: 1.35;
  color: #151b26;
}

.product-subtitle {
  margin: 12px 0 0;
  color: #6b7280;
  font-size: 15px;
  line-height: 1.7;
}

.price-card {
  margin-top: 24px;
  padding: 18px 20px;
  border-radius: 20px;
  background: linear-gradient(135deg, #fff5f3 0%, #fff9f8 100%);
  border: 1px solid #ffe2dc;
}

.price-label {
  display: block;
  margin-bottom: 8px;
  color: #8f5e56;
  font-size: 13px;
}

.price-value {
  font-size: 36px;
  line-height: 1;
  color: #e5484d;
  font-weight: 700;
}

.meta-list {
  margin-top: 20px;
  display: grid;
  gap: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding-bottom: 14px;
  border-bottom: 1px dashed #e6eaf0;
}

.meta-label {
  width: 70px;
  color: #8a94a6;
  font-size: 14px;
}

.meta-value {
  color: #1f2937;
  font-size: 15px;
}

.meta-value.warning {
  color: #c47a00;
}

.meta-value.danger {
  color: #d14343;
}

.quantity-row {
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
}

.quantity-label {
  width: 70px;
  color: #4b5563;
  font-size: 14px;
}

.quantity-selector {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d8dfe8;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
}

.quantity-btn {
  width: 44px;
  height: 44px;
  border: none;
  background: #f8fafc;
  font-size: 20px;
  color: #2f3a4e;
  cursor: pointer;
}

.quantity-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.quantity-value {
  min-width: 56px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
}

.action-area {
  margin-top: 28px;
}

.add-cart-button {
  width: min(320px, 100%);
  min-height: 52px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #1677ff, #0d5ad8);
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 16px 28px rgba(22, 119, 255, 0.22);
}

.add-cart-button:disabled {
  background: #c4cad4;
  box-shadow: none;
  cursor: not-allowed;
}

.action-hint {
  margin: 12px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.action-hint.danger {
  color: #d14343;
}

.action-success {
  margin: 10px 0 0;
  color: #16803c;
  font-size: 13px;
  font-weight: 600;
}

.assist-card {
  margin-top: 24px;
  padding: 18px 20px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #e7edf5;
}

.assist-card h3 {
  margin: 0 0 10px;
  font-size: 15px;
}

.assist-card ul {
  margin: 0;
  padding-left: 18px;
  color: #667085;
  line-height: 1.7;
  font-size: 14px;
}

.content-section {
  margin-top: 20px;
  padding: 24px 28px;
  background: #fff;
  border: 1px solid #e8ecf3;
  border-radius: 24px;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.04);
}

.section-head {
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 0;
  font-size: 22px;
  color: #151b26;
}

.section-head p {
  margin: 8px 0 0;
  color: #8a94a6;
  font-size: 14px;
}

.detail-content {
  color: #374151;
  font-size: 15px;
  line-height: 1.9;
}

.detail-content :deep(p) {
  margin: 0 0 14px;
}

.detail-content :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;
  margin: 12px auto;
  border-radius: 16px;
}

.params-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.param-item {
  padding: 16px 18px;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #edf1f6;
}

.param-item span {
  display: block;
  margin-bottom: 8px;
  color: #8a94a6;
  font-size: 13px;
}

.param-item strong {
  color: #1f2937;
  font-size: 15px;
}

.empty-state {
  padding: 72px 24px;
  text-align: center;
  background: #fff;
  border: 1px solid #e8ecf3;
  border-radius: 24px;
}

.empty-state h2 {
  margin: 0 0 12px;
  font-size: 24px;
}

.empty-state p {
  margin: 0 0 18px;
  color: #6b7280;
}

.back-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 140px;
  min-height: 42px;
  padding: 0 18px;
  border-radius: 999px;
  background: #1677ff;
  color: #fff;
}

@keyframes shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}

@media (max-width: 960px) {
  .hero-card,
  .detail-loading-grid,
  .params-grid {
    grid-template-columns: 1fr;
  }

  .hero-card,
  .content-section {
    padding: 20px;
  }
}

@media (max-width: 640px) {
  .product-name {
    font-size: 24px;
  }

  .price-value {
    font-size: 30px;
  }

  .meta-item,
  .quantity-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .meta-label,
  .quantity-label {
    width: auto;
  }

  .add-cart-button {
    width: 100%;
  }
}
</style>
