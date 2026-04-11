<template>
  <div class="card">
    <h3>购物车</h3>
    <table class="table">
      <thead>
        <tr>
          <th>商品</th>
          <th>店铺</th>
          <th>单价</th>
          <th>数量</th>
          <th>小计</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.cartId">
          <td>{{ item.productName }}</td>
          <td>{{ item.shopName }}</td>
          <td>{{ item.productPrice }}</td>
          <td>{{ item.quantity }}</td>
          <td>{{ calcItemAmount(item) }}</td>
        </tr>
      </tbody>
    </table>
    <p class="total">合计：{{ totalAmount }}</p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { fetchCartList } from "../api/cart";

const list = ref([]);

function calcItemAmount(item) {
  const price = Number(item.productPrice || 0);
  return (price * Number(item.quantity || 0)).toFixed(2);
}

const totalAmount = computed(() => {
  return list.value
    .reduce((sum, item) => sum + Number(calcItemAmount(item)), 0)
    .toFixed(2);
});

async function loadData() {
  const res = await fetchCartList();
  list.value = res.data || [];
}

onMounted(loadData);
</script>

<style scoped>
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  border-bottom: 1px solid #eee;
  padding: 8px 6px;
}
.total {
  margin-top: 12px;
  font-weight: bold;
}
</style>
