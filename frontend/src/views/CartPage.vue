<template>
  <div class="card">
    <h3>购物车</h3>
    <div v-if="list.length === 0" class="empty">购物车空空如也，去挑几件商品吧～</div>
    <table v-else class="table">
      <thead>
        <tr>
          <th>商品</th>
          <th>店铺</th>
          <th>单价</th>
          <th>数量</th>
          <th>小计</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.cartId" :class="{ invalid: item.invalid }">
          <td>
            {{ item.productName || "（商品信息缺失）" }}
            <span v-if="item.invalid" class="tag">已失效</span>
          </td>
          <td>{{ item.shopName }}</td>
          <td>{{ item.productPrice }}</td>
          <td>
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
          </td>
          <td>{{ calcItemAmount(item) }}</td>
          <td>
            <button
              type="button"
              class="btn-del"
              :disabled="submittingId === item.cartId"
              @click="onDelete(item)"
            >
              删除
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-if="list.length > 0" class="total">合计：{{ totalAmount }}</p>
    <div v-if="list.length > 0" class="actions">
      <RouterLink class="btn primary" to="/checkout">去结算</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { deleteCart, fetchCartList, updateCart } from "../api/cart";

const list = ref([]);
const submittingId = ref(null);

function calcItemAmount(item) {
  if (item.invalid) return "-";
  const price = Number(item.productPrice || 0);
  return (price * Number(item.quantity || 0)).toFixed(2);
}

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
.table tr.invalid td {
  color: #bbb;
}
.tag {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 6px;
  font-size: 12px;
  background: #f5f5f5;
  color: #888;
  border-radius: 3px;
}
.qty {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.qty-btn {
  width: 24px;
  height: 24px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
}
.qty-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.qty-value {
  min-width: 28px;
  text-align: center;
}
.btn-del {
  background: #fff;
  border: 1px solid #e5484d;
  color: #e5484d;
  border-radius: 4px;
  padding: 4px 10px;
  cursor: pointer;
}
.btn-del:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.empty {
  color: #888;
  padding: 16px 0;
}
.total {
  margin-top: 12px;
  font-weight: bold;
}
.actions {
  margin-top: 12px;
}
.btn.primary {
  display: inline-block;
  background: #1677ff;
  color: #fff;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 6px;
}
</style>
