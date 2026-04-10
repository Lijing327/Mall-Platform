<template>
  <div class="card" v-if="detail">
    <h3>商品详情</h3>
    <p><b>商品ID：</b>{{ detail.id }}</p>
    <p><b>商品名：</b>{{ detail.productName }}</p>
    <p><b>副标题：</b>{{ detail.productSubtitle || "-" }}</p>
    <p><b>价格：</b>{{ detail.price }}</p>
    <p><b>库存：</b>{{ detail.stock }}</p>
    <p><b>店铺：</b>{{ detail.shop?.shopName || "-" }}</p>
    <p><b>详情：</b>{{ detail.detail || "-" }}</p>

    <div class="actions">
      <input v-model.number="quantity" type="number" min="1" />
      <button class="btn" @click="addToCartAction">加入购物车</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { fetchProductDetail } from "../api/product";
import { addCart } from "../api/cart";
import { getUserId } from "../utils/user-context";

const route = useRoute();
const detail = ref(null);
const quantity = ref(1);

async function loadData() {
  const res = await fetchProductDetail(route.params.id);
  detail.value = res.data;
}

async function addToCartAction() {
  await addCart({
    userId: getUserId(),
    productId: Number(route.params.id),
    quantity: quantity.value
  });
  window.alert("已加入购物车");
}

onMounted(loadData);
</script>

<style scoped>
.actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}
input {
  width: 80px;
  padding: 6px;
}
</style>
