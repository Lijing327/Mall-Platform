<template>
  <div class="admin-page">
    <section class="stats-grid">
      <AdminStatCard label="商品总数" :value="formatCount(productStats.total)" hint="当前已录入商品数" icon="商" />
      <AdminStatCard label="上架商品" :value="formatCount(productStats.onShelf)" hint="可售商品数量" icon="上" />
      <AdminStatCard label="订单总数" :value="formatCount(orderStats.total)" hint="全部订单记录" icon="订" />
      <AdminStatCard label="待发货订单" :value="formatCount(orderStats.toShip)" hint="已支付待处理" icon="发" />
    </section>

    <section class="admin-panel admin-quick-entry">
      <div class="panel-heading">
        <div>
          <h3>快捷入口</h3>
          <p>先处理今天最重要的事情。</p>
        </div>
      </div>
      <div class="quick-links">
        <RouterLink class="quick-link-card" to="/admin/products">
          <strong>去商品管理</strong>
          <span>查看在售商品、库存与上下架状态</span>
        </RouterLink>
        <RouterLink class="quick-link-card" to="/admin/orders">
          <strong>去订单管理</strong>
          <span>筛选待发货订单并查看详情</span>
        </RouterLink>
        <RouterLink class="quick-link-card" to="/admin/users">
          <strong>去用户管理</strong>
          <span>查看用户交易概况与最近下单情况</span>
        </RouterLink>
      </div>
    </section>

    <section class="admin-panel">
      <div class="panel-heading">
        <div>
          <h3>最近订单</h3>
          <p>优先关注最近产生的订单与订单状态变化。</p>
        </div>
        <RouterLink class="text-link" to="/admin/orders">查看全部订单</RouterLink>
      </div>

      <div v-if="loading" class="admin-loading">正在加载仪表盘数据…</div>
      <p v-else-if="error" class="admin-error">{{ error }}</p>
      <EmptyState
        v-else-if="recentOrders.length === 0"
        icon="订"
        title="还没有订单数据"
        description="当商城出现订单后，这里会展示最近的交易记录。"
      />
      <table v-else class="table admin-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户</th>
            <th>实付金额</th>
            <th>状态</th>
            <th>下单时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in recentOrders" :key="item.orderId">
            <td>
              <strong>{{ item.orderNo }}</strong>
            </td>
            <td>用户 {{ item.userId }}</td>
            <td>{{ formatCurrency(item.payAmount || item.totalAmount) }}</td>
            <td><OrderStatusTag :status="item.orderStatus" /></td>
            <td>{{ formatDateTime(item.createTime) }}</td>
            <td>
              <RouterLink class="text-link" :to="`/admin/orders/${item.orderId}`">查看详情</RouterLink>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import AdminStatCard from "../../components/admin/AdminStatCard.vue";
import EmptyState from "../../components/admin/EmptyState.vue";
import OrderStatusTag from "../../components/admin/OrderStatusTag.vue";
import { fetchAdminOrders, fetchAdminProducts } from "../../api/admin";
import { formatCount, formatCurrency, formatDateTime } from "../../utils/admin-ui";

const loading = ref(false);
const error = ref("");
const orders = ref([]);
const products = ref([]);

const productStats = computed(() => ({
  total: products.value.length,
  onShelf: products.value.filter((item) => item.saleStatus === "ON_SHELF").length
}));

const orderStats = computed(() => ({
  total: orders.value.length,
  toShip: orders.value.filter((item) => item.orderStatus === "PAID").length
}));

const recentOrders = computed(() => orders.value.slice(0, 6));

async function loadData() {
  loading.value = true;
  error.value = "";
  try {
    const [productRes, orderRes] = await Promise.all([
      fetchAdminProducts({ pageNum: 1, pageSize: 100 }),
      fetchAdminOrders({ pageNum: 1, pageSize: 100 })
    ]);
    products.value = productRes.data.list || [];
    orders.value = orderRes.data.list || [];
  } catch (e) {
    error.value = e?.message || "仪表盘数据加载失败";
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>
