<template>
  <div class="page">
    <header class="page-head">
      <h1 class="title">提交订单</h1>
      <p class="subtitle">确认收货信息与金额后提交</p>
    </header>

    <section class="block card-block">
      <h2 class="block-title">收货地址</h2>
      <div class="block-tools">
        <RouterLink class="link" to="/addresses">切换地址 / 新增地址</RouterLink>
      </div>
      <div v-if="addressList.length === 0" class="empty">
        暂无收货地址，请先到
        <RouterLink class="link" to="/addresses">我的地址</RouterLink>
        添加。
      </div>
      <div v-else class="address-list">
        <label
          v-for="addr in addressList"
          :key="addr.id"
          class="addr-card"
          :class="{ selected: selectedAddressId === addr.id }"
        >
          <input v-model="selectedAddressId" type="radio" name="addr" :value="addr.id" />
          <div class="addr-main">
            <div class="addr-line1">
              <span class="name">{{ addr.receiverName }}</span>
              <span class="mobile">{{ addr.receiverMobile }}</span>
              <span v-if="addr.isDefault" class="default-tag">默认</span>
            </div>
            <div class="addr-line2">{{ fullAddress(addr) }}</div>
          </div>
        </label>
      </div>
    </section>

    <section v-if="cartLines.length > 0" class="block card-block">
      <h2 class="block-title">商品清单</h2>
      <ul class="goods-list">
        <li v-for="line in cartLines" :key="line.cartId" class="goods-row">
          <span class="g-name">{{ line.productName }}</span>
          <span class="g-meta">￥{{ formatPrice(line.productPrice) }} × {{ line.quantity }}</span>
          <span class="g-sub">小计 ￥{{ lineSub(line) }}</span>
        </li>
      </ul>
    </section>

    <section class="block card-block">
      <h2 class="block-title">订单备注（可选）</h2>
      <textarea v-model="remark" class="remark" rows="2" maxlength="200" placeholder="配送要求、发票说明等" />
    </section>

    <section class="block card-block sum-block">
      <div class="sum-row"><span>商品总额</span><span>￥{{ goodsTotal }}</span></div>
      <div class="sum-row"><span>运费</span><span>￥0.00（免运费）</span></div>
      <div class="sum-row total"><span>实付金额</span><span class="pay">￥{{ goodsTotal }}</span></div>
    </section>

    <div class="actions">
      <button class="btn primary" type="button" :disabled="submitting || !selectedAddressId" @click="createOrderAction">
        {{ submitting ? "提交中…" : "提交订单" }}
      </button>
      <button class="btn secondary" type="button" :disabled="!lastOrderNo || paying" @click="payOrderAction">
        {{ paying ? "支付中…" : "模拟支付（最新订单）" }}
      </button>
    </div>
    <div v-if="resultText" class="result">{{ resultText }}</div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { fetchCartList } from "../../api/cart";
import { fetchAddressList } from "../../api/address";
import { createOrder, payOrder } from "../../api/order";
import { orderStatusZh } from "../../utils/display-labels";

const addressList = ref([]);
const selectedAddressId = ref(null);
const remark = ref("");
const lastOrderNo = ref("");
const resultText = ref("");
const submitting = ref(false);
const paying = ref(false);
const cartLines = ref([]);

function fullAddress(addr) {
  return `${addr.province || ""}${addr.city || ""}${addr.district || ""}${addr.detailAddress || ""}`;
}

function formatPrice(v) {
  if (v == null || v === "") return "-";
  return Number(v).toFixed(2);
}

function lineSub(line) {
  const p = Number(line.productPrice || 0);
  return (p * Number(line.quantity || 0)).toFixed(2);
}

const goodsTotal = computed(() => {
  return cartLines.value
    .filter((l) => !l.invalid)
    .reduce((s, line) => s + Number(lineSub(line)), 0)
    .toFixed(2);
});

async function loadAddresses() {
  const res = await fetchAddressList();
  addressList.value = res.data || [];
  const defaultAddr = addressList.value.find((a) => a.isDefault);
  selectedAddressId.value = defaultAddr ? defaultAddr.id : (addressList.value[0]?.id ?? null);
}

async function loadCart() {
  const res = await fetchCartList();
  cartLines.value = (res.data || []).filter((l) => !l.invalid);
}

async function createOrderAction() {
  if (!selectedAddressId.value) {
    window.alert("请先选择收货地址");
    return;
  }
  submitting.value = true;
  try {
    const res = await createOrder({
      addressId: selectedAddressId.value,
      remark: remark.value || undefined
    });
    lastOrderNo.value = res.data.orderNo;
    resultText.value = `订单已提交：${res.data.orderNo}，状态：${orderStatusZh(res.data.orderStatus)}`;
    await loadCart();
  } catch (e) {
    resultText.value = e?.message || "提交失败";
  } finally {
    submitting.value = false;
  }
}

async function payOrderAction() {
  if (!lastOrderNo.value) return;
  paying.value = true;
  try {
    const res = await payOrder({ orderNo: lastOrderNo.value });
    resultText.value = `支付成功：${res.data.orderNo}，状态：${orderStatusZh(res.data.orderStatus)}`;
  } catch (e) {
    resultText.value = e?.message || "支付失败";
  } finally {
    paying.value = false;
  }
}

onMounted(() => {
  loadAddresses();
  loadCart();
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
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #888;
}

.block {
  margin-bottom: 16px;
}

.card-block {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #eee;
}

.block-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
}

.block-tools {
  margin-bottom: 10px;
}

.link {
  color: #1677ff;
  text-decoration: none;
  font-size: 13px;
}

.empty {
  padding: 12px;
  color: #888;
  border: 1px dashed #ddd;
  border-radius: 6px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.addr-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
}

.addr-card.selected {
  border-color: #1677ff;
  background: #f6faff;
}

.addr-line1 {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 4px;
}

.name {
  font-weight: 600;
}

.mobile {
  color: #666;
  font-size: 14px;
}

.default-tag {
  padding: 2px 6px;
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  border-radius: 3px;
}

.addr-line2 {
  color: #555;
  font-size: 14px;
  line-height: 1.5;
}

.goods-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.goods-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.goods-row:last-child {
  border-bottom: none;
}

.g-name {
  flex: 1;
  min-width: 120px;
}

.g-meta {
  color: #666;
}

.g-sub {
  font-weight: 500;
}

.remark {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px 10px;
  font-family: inherit;
  font-size: 14px;
}

.sum-block .sum-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
}

.sum-block .sum-row.total {
  margin-top: 8px;
  padding-top: 12px;
  border-top: 1px solid #eee;
  font-size: 16px;
  font-weight: 600;
}

.pay {
  color: #e5484d;
  font-size: 20px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}

.btn {
  padding: 10px 24px;
  border-radius: 6px;
  font-size: 15px;
  cursor: pointer;
  border: 1px solid #ddd;
  background: #fff;
}

.btn.primary {
  background: #e5484d;
  color: #fff;
  border-color: #e5484d;
  font-weight: 600;
}

.btn.secondary {
  border-color: #1677ff;
  color: #1677ff;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.result {
  margin-top: 12px;
  padding: 12px;
  background: #f0f7ff;
  border: 1px solid #d0e4ff;
  border-radius: 6px;
  font-size: 14px;
}
</style>
