<template>
  <div class="card">
    <h3>提交订单</h3>

    <section class="block">
      <div class="block-head">
        <span class="block-title">收货地址</span>
        <RouterLink class="link" to="/addresses">管理地址</RouterLink>
      </div>
      <div v-if="addressList.length === 0" class="empty">
        暂无收货地址，请先到
        <RouterLink class="link" to="/addresses">我的地址</RouterLink>
        添加一个再下单。
      </div>
      <div v-else class="address-list">
        <label
          v-for="addr in addressList"
          :key="addr.id"
          class="addr-card"
          :class="{ selected: selectedAddressId === addr.id }"
        >
          <input
            type="radio"
            name="addr"
            :value="addr.id"
            v-model="selectedAddressId"
          />
          <div class="addr-main">
            <div class="addr-line1">
              <span class="name">{{ addr.receiverName }}</span>
              <span class="mobile">{{ addr.receiverMobile }}</span>
              <span v-if="addr.isDefault" class="default-tag">默认</span>
            </div>
            <div class="addr-line2">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}</div>
          </div>
        </label>
      </div>
    </section>

    <section class="block">
      <div class="block-head"><span class="block-title">订单备注（可选）</span></div>
      <textarea v-model="remark" class="remark" rows="2" maxlength="200" placeholder="可填写配送要求、发票备注等（不影响主流程）" />
    </section>

    <div class="actions">
      <button class="btn primary" :disabled="submitting || !selectedAddressId" @click="createOrderAction">
        {{ submitting ? "创建中…" : "创建订单" }}
      </button>
      <button class="btn" :disabled="!lastOrderNo || paying" @click="payOrderAction">
        {{ paying ? "支付中…" : "模拟支付最新订单" }}
      </button>
    </div>
    <div class="result" v-if="resultText">{{ resultText }}</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { fetchAddressList } from "../api/address";
import { createOrder, payOrder } from "../api/order";
import { orderStatusZh } from "../utils/display-labels";

const addressList = ref([]);
const selectedAddressId = ref(null);
const remark = ref("");
const lastOrderNo = ref("");
const resultText = ref("");
const submitting = ref(false);
const paying = ref(false);

async function loadAddresses() {
  const res = await fetchAddressList();
  addressList.value = res.data || [];
  const defaultAddr = addressList.value.find((a) => a.isDefault);
  selectedAddressId.value = defaultAddr ? defaultAddr.id : (addressList.value[0]?.id ?? null);
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
    resultText.value = `订单创建成功：${res.data.orderNo}，状态：${orderStatusZh(res.data.orderStatus)}`;
  } catch (e) {
    resultText.value = e?.message || "创建失败";
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

onMounted(loadAddresses);
</script>

<style scoped>
.block {
  margin-bottom: 16px;
}
.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.block-title {
  font-weight: 600;
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
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;
}
.addr-card.selected {
  border-color: #1677ff;
  background: #f6faff;
}
.addr-card input[type="radio"] {
  margin-top: 4px;
}
.addr-main {
  flex: 1;
  min-width: 0;
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
}
.default-tag {
  padding: 1px 6px;
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  border-radius: 3px;
}
.addr-line2 {
  color: #555;
  font-size: 14px;
}
.remark {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 6px 8px;
  font-family: inherit;
  font-size: 14px;
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}
.btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}
.btn.primary {
  background: #1677ff;
  color: #fff;
  border-color: #1677ff;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.result {
  margin-top: 12px;
  background: #f0f7ff;
  border: 1px solid #d0e4ff;
  padding: 10px;
  border-radius: 4px;
}
</style>
