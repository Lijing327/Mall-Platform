<template>
  <div class="page">
    <header class="page-head">
      <h1 class="title">我的购物车</h1>
      <p class="subtitle">确认商品后进入结算</p>
    </header>

    <div v-if="list.length === 0" class="empty">购物车空空如也，去挑几件商品吧～</div>

    <template v-else>
      <div class="cart-list">
        <div v-for="item in list" :key="item.cartId" class="row-card" :class="{ invalid: item.invalid }">
          <div class="row-img">
            <img v-if="item.productImage" :src="item.productImage" :alt="item.productName" />
            <div v-else class="ph">无图</div>
          </div>
          <div class="row-main">
            <div class="row-title">
              {{ item.productName || "（商品信息缺失）" }}
              <span v-if="item.invalid" class="tag">已失效</span>
            </div>
            <div class="row-meta">店铺：{{ item.shopName || "-" }}</div>
            <div class="row-line">
              <span>单价：￥{{ formatPrice(item.productPrice) }}</span>
            </div>
            <div class="row-line qty-line">
              <span class="muted">数量</span>
              <div class="qty">
                <button
                  type="button"
                  class="qty-btn"
                  :disabled="item.invalid || item.quantity <= 1 || submittingId === item.cartId"
                  @click="onUpdateQuantity(item, item.quantity - 1)"
                >
                  −
                </button>
                <span class="qty-value">{{ item.quantity }}</span>
                <button
                  type="button"
                  class="qty-btn"
                  :disabled="item.invalid || submittingId === item.cartId"
                  @click="onUpdateQuantity(item, item.quantity + 1)"
                >
                  +
                </button>
              </div>
            </div>
            <div class="row-line">
              <span>小计：￥{{ calcItemAmount(item) }}</span>
              <button
                type="button"
                class="btn-del"
                :disabled="submittingId === item.cartId"
                @click="onDelete(item)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="checkout-bar">
        <div class="sum-lines">
          <p>已选 <strong>{{ validCount }}</strong> 件商品</p>
          <p>商品总额：￥{{ totalAmount }}</p>
          <p>运费：￥0.00（免运费）</p>
          <p class="pay-line">应付总额：<span class="pay-total">￥{{ totalAmount }}</span></p>
        </div>
        <RouterLink class="btn-checkout" to="/checkout">去结算</RouterLink>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { deleteCart, fetchCartList, updateCart } from "../../api/cart";

const list = ref([]);
const submittingId = ref(null);

function formatPrice(v) {
  if (v == null || v === "") return "-";
  return Number(v).toFixed(2);
}

function calcItemAmount(item) {
  if (item.invalid) return "-";
  const price = Number(item.productPrice || 0);
  return (price * Number(item.quantity || 0)).toFixed(2);
}

const validCount = computed(() => list.value.filter((item) => !item.invalid).length);

const totalAmount = computed(() => {
  return list.value
    .filter((item) => !item.invalid)
    .reduce((sum, item) => sum + Number(calcItemAmount(item)), 0)
    .toFixed(2);
});

async function loadData() {
  const res = await fetchCartList();
  list.value = res.data || [];
}

async function onUpdateQuantity(item, nextQuantity) {
  if (nextQuantity < 1) return;
  submittingId.value = item.cartId;
  try {
    await updateCart({ cartId: item.cartId, quantity: nextQuantity });
    await loadData();
  } catch (e) {
    window.alert(e?.message || "操作失败");
  } finally {
    submittingId.value = null;
  }
}

async function onDelete(item) {
  if (!window.confirm(`确认删除【${item.productName || "该商品"}】?`)) return;
  submittingId.value = item.cartId;
  try {
    await deleteCart({ cartId: item.cartId });
    await loadData();
  } catch (e) {
    window.alert(e?.message || "操作失败");
  } finally {
    submittingId.value = null;
  }
}

onMounted(loadData);
</script>

<style scoped>
.page {
  width: 100%;
  padding-bottom: 120px;
}

.page-head {
  margin-bottom: 16px;
}

.title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #888;
}

.empty {
  text-align: center;
  padding: 48px;
  color: #999;
  background: #fff;
  border-radius: 8px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.row-card {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #eee;
}

.row-card.invalid {
  opacity: 0.65;
}

.row-img {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
}

.row-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.row-img .ph {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #bbb;
}

.row-main {
  flex: 1;
  min-width: 0;
}

.row-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 6px;
}

.tag {
  margin-left: 6px;
  font-size: 12px;
  padding: 2px 6px;
  background: #f5f5f5;
  color: #888;
  border-radius: 3px;
  font-weight: normal;
}

.row-meta {
  font-size: 12px;
  color: #888;
  margin-bottom: 8px;
}

.row-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 14px;
  margin-top: 6px;
}

.qty-line {
  justify-content: flex-start;
  gap: 12px;
}

.muted {
  color: #888;
  font-size: 13px;
}

.qty {
  display: inline-flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #fafafa;
  cursor: pointer;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.qty-value {
  min-width: 32px;
  text-align: center;
  font-size: 14px;
}

.btn-del {
  background: #fff;
  border: 1px solid #e5484d;
  color: #e5484d;
  border-radius: 4px;
  padding: 4px 12px;
  cursor: pointer;
  font-size: 13px;
}

.btn-del:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.checkout-bar {
  position: sticky;
  bottom: 0;
  margin-top: 20px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.sum-lines p {
  margin: 4px 0;
  font-size: 14px;
  color: #444;
}

.pay-line {
  font-size: 16px;
  font-weight: 600;
}

.pay-total {
  color: #e5484d;
  font-size: 20px;
}

.btn-checkout {
  display: inline-block;
  padding: 12px 36px;
  background: #e5484d;
  color: #fff !important;
  text-decoration: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
}

.btn-checkout:hover {
  opacity: 0.92;
}
</style>
