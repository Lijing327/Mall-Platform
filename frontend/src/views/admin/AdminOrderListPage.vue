<template>
  <div class="admin-page order-list-page">
    <AdminPageHeader
      eyebrow="Orders"
      title="订单管理"
      subtitle="查看订单全生命周期状态，快速筛选并进入订单详情处理。"
    />

    <section class="stats-grid order-stats">
      <button
        v-for="card in statCards"
        :key="card.key"
        type="button"
        class="stat-card"
        :class="{ active: orderStatus === card.status }"
        @click="quickFilterByStatus(card.status)"
      >
        <span class="stat-label">{{ card.label }}</span>
        <strong class="stat-value">{{ card.value }}</strong>
      </button>
    </section>

    <AdminSearchBar>
      <label class="admin-field">
        <span>订单号</span>
        <input v-model.trim="orderNo" placeholder="输入订单号精准查询" />
      </label>

      <label class="admin-field">
        <span>用户</span>
        <input v-model.trim="userKeyword" placeholder="输入用户 ID（当前版本）" />
      </label>

      <label class="admin-field">
        <span>订单状态</span>
        <select v-model="orderStatus">
          <option value="">全部</option>
          <option value="PENDING_PAYMENT">待支付</option>
          <option value="PAID">待发货</option>
          <option value="SHIPPED">已发货</option>
          <option value="COMPLETED">已完成</option>
        </select>
      </label>

      <template #actions>
        <button type="button" class="btn" @click="search">查询</button>
        <button type="button" class="btn btn-secondary" @click="resetFilters">重置</button>
      </template>
    </AdminSearchBar>

    <section class="admin-panel">
      <div class="panel-heading">
        <div>
          <h3>订单列表</h3>
          <p>按下单时间倒序展示，支持直接进入详情页处理订单。</p>
        </div>
        <div class="table-toolbar">
          <span>每页</span>
          <select v-model.number="pageSize" @change="changePageSize">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span>条</span>
        </div>
      </div>

      <div v-if="loading" class="table-loading">
        <div v-for="i in 6" :key="i" class="loading-line"></div>
      </div>

      <p v-else-if="error" class="admin-error">{{ error }}</p>

      <EmptyState
        v-else-if="filteredList.length === 0"
        icon="订"
        title="暂无订单数据"
        description="可以调整筛选条件后重试。"
      />

      <table v-else class="table admin-table">
        <thead>
          <tr>
            <th style="width: 210px">订单号</th>
            <th style="width: 130px">用户</th>
            <th style="width: 120px">订单金额</th>
            <th style="width: 120px">支付状态</th>
            <th style="width: 100px">订单状态</th>
            <th style="width: 150px">下单时间</th>
            <th style="width: 160px">发货信息</th>
            <th style="width: 190px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredList" :key="item.orderId">
            <td>
              <RouterLink class="text-link table-primary" :to="`/admin/orders/${item.orderId}`">
                {{ item.orderNo }}
              </RouterLink>
              <div class="table-secondary">订单ID：{{ item.orderId }}</div>
            </td>

            <td>
              <div class="table-primary">用户 {{ item.userId }}</div>
              <RouterLink class="text-link" :to="`/admin/users?userId=${item.userId}`">查看用户</RouterLink>
            </td>

            <td class="amount-cell">{{ formatCurrency(item.payAmount || item.totalAmount) }}</td>

            <td>
              <span class="status-tag" :class="paymentTagClass(item)">{{ paymentStatus(item) }}</span>
              <div class="table-secondary">{{ payTypeZh(item.payType) }}</div>
            </td>

            <td><OrderStatusTag :status="item.orderStatus" /></td>

            <td>{{ formatDateTime(item.createTime) }}</td>

            <td>
              <div class="table-primary">{{ item.shippingNo || "未发货" }}</div>
              <div class="table-secondary">{{ formatDateTime(item.shipTime) }}</div>
            </td>

            <td>
              <AdminTableActions :actions="buildActions(item)" />
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredList.length > 0" class="pager">
        <button type="button" class="btn-page" :disabled="pageNum <= 1" @click="prevPage">上一页</button>
        <span>第 {{ pageNum }} / {{ totalPage }} 页，共 {{ total }} 条</span>
        <button type="button" class="btn-page" :disabled="pageNum >= totalPage" @click="nextPage">下一页</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import AdminPageHeader from "../../components/admin/AdminPageHeader.vue";
import AdminSearchBar from "../../components/admin/AdminSearchBar.vue";
import AdminTableActions from "../../components/admin/AdminTableActions.vue";
import EmptyState from "../../components/admin/EmptyState.vue";
import OrderStatusTag from "../../components/admin/OrderStatusTag.vue";
import { fetchAdminOrders } from "../../api/admin";
import { formatCurrency, formatDateTime, formatCount } from "../../utils/admin-ui";
import { payTypeZh } from "../../utils/display-labels";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const error = ref("");
const list = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(10);

const orderStatus = ref("");
const orderNo = ref("");
const userKeyword = ref("");

const stats = ref({
  all: 0,
  pending: 0,
  paid: 0,
  shipped: 0,
  completed: 0
});

const statCards = computed(() => [
  { key: "all", label: "总订单数", status: "", value: formatCount(stats.value.all) },
  { key: "pending", label: "待支付", status: "PENDING_PAYMENT", value: formatCount(stats.value.pending) },
  { key: "paid", label: "待发货", status: "PAID", value: formatCount(stats.value.paid) },
  { key: "shipped", label: "已发货", status: "SHIPPED", value: formatCount(stats.value.shipped) },
  { key: "completed", label: "已完成", status: "COMPLETED", value: formatCount(stats.value.completed) }
]);

const filteredList = computed(() =>
  list.value.filter((item) => !userKeyword.value || String(item.userId).includes(userKeyword.value))
);

const totalPage = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));

function paymentStatus(item) {
  if (item.orderStatus === "PENDING_PAYMENT") {
    return "待支付";
  }
  return "已支付";
}

function paymentTagClass(item) {
  return item.orderStatus === "PENDING_PAYMENT" ? "status-tag--neutral" : "status-tag--success";
}

function buildActions(item) {
  const actions = [
    {
      label: "查看详情",
      tone: "primary",
      onClick: () => router.push(`/admin/orders/${item.orderId}`)
    }
  ];

  if (item.orderStatus === "PAID") {
    actions.unshift({
      label: "发货",
      tone: "neutral",
      onClick: () => window.alert("当前管理端暂未开放发货接口，请先在订单详情页查看状态。")
    });
  }
  return actions;
}

function syncRoute() {
  const query = {};
  if (orderNo.value) query.orderNo = orderNo.value;
  if (orderStatus.value) query.orderStatus = orderStatus.value;
  if (userKeyword.value) query.userId = userKeyword.value;
  if (pageNum.value > 1) query.page = String(pageNum.value);
  if (pageSize.value !== 10) query.pageSize = String(pageSize.value);
  router.replace({ path: "/admin/orders", query });
}

async function loadStats() {
  const [allRes, pendingRes, paidRes, shippedRes, completedRes] = await Promise.all([
    fetchAdminOrders({ pageNum: 1, pageSize: 1 }),
    fetchAdminOrders({ pageNum: 1, pageSize: 1, orderStatus: "PENDING_PAYMENT" }),
    fetchAdminOrders({ pageNum: 1, pageSize: 1, orderStatus: "PAID" }),
    fetchAdminOrders({ pageNum: 1, pageSize: 1, orderStatus: "SHIPPED" }),
    fetchAdminOrders({ pageNum: 1, pageSize: 1, orderStatus: "COMPLETED" })
  ]);

  stats.value = {
    all: Number(allRes.data.total || 0),
    pending: Number(pendingRes.data.total || 0),
    paid: Number(paidRes.data.total || 0),
    shipped: Number(shippedRes.data.total || 0),
    completed: Number(completedRes.data.total || 0)
  };
}

async function loadData() {
  loading.value = true;
  error.value = "";
  try {
    const res = await fetchAdminOrders({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      orderStatus: orderStatus.value || undefined,
      orderNo: orderNo.value || undefined
    });
    list.value = res.data.list || [];
    total.value = Number(res.data.total || 0);
    if (pageNum.value > totalPage.value) {
      pageNum.value = totalPage.value;
    }
  } catch (e) {
    error.value = e?.message || "订单数据加载失败";
  } finally {
    loading.value = false;
  }
}

async function refreshAll() {
  await Promise.all([loadStats(), loadData()]);
}

async function search() {
  pageNum.value = 1;
  syncRoute();
  await refreshAll();
}

async function resetFilters() {
  orderNo.value = "";
  orderStatus.value = "";
  userKeyword.value = "";
  pageNum.value = 1;
  pageSize.value = 10;
  syncRoute();
  await refreshAll();
}

async function quickFilterByStatus(status) {
  orderStatus.value = status;
  pageNum.value = 1;
  syncRoute();
  await refreshAll();
}

async function changePageSize() {
  pageNum.value = 1;
  syncRoute();
  await loadData();
}

async function prevPage() {
  if (pageNum.value <= 1) return;
  pageNum.value -= 1;
  syncRoute();
  await loadData();
}

async function nextPage() {
  if (pageNum.value >= totalPage.value) return;
  pageNum.value += 1;
  syncRoute();
  await loadData();
}

watch(
  () => route.query,
  (query) => {
    orderNo.value = typeof query.orderNo === "string" ? query.orderNo : "";
    orderStatus.value = typeof query.orderStatus === "string" ? query.orderStatus : "";
    userKeyword.value = typeof query.userId === "string" ? query.userId : "";
    pageNum.value = Number(query.page || 1) > 0 ? Number(query.page || 1) : 1;
    pageSize.value = [10, 20, 50].includes(Number(query.pageSize)) ? Number(query.pageSize) : 10;
  },
  { immediate: true }
);

onMounted(refreshAll);
</script>

<style scoped>
.order-list-page {
  gap: 18px;
}

.order-stats {
  grid-template-columns: repeat(5, minmax(0, 1fr));
}

.stat-card {
  padding: 16px;
  border-radius: 14px;
  border: 1px solid #dce4f2;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: 0.2s ease;
}

.stat-card:hover {
  border-color: #b8c8ea;
}

.stat-card.active {
  border-color: #2563eb;
  background: #eef4ff;
}

.stat-label {
  display: block;
  color: #667085;
  font-size: 13px;
}

.stat-value {
  display: block;
  margin-top: 8px;
  color: #172033;
  font-size: 28px;
  font-weight: 700;
}

.table-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667085;
}

.table-toolbar select {
  border: 1px solid #d4dbe7;
  border-radius: 8px;
  padding: 6px 8px;
}

.amount-cell {
  font-weight: 700;
  color: #111827;
}

.table-loading {
  display: grid;
  gap: 10px;
  padding: 6px 0;
}

.loading-line {
  height: 22px;
  border-radius: 8px;
  background: linear-gradient(90deg, #eef2f7 25%, #f8fafc 37%, #eef2f7 63%);
  background-size: 400% 100%;
  animation: loading-shimmer 1.2s infinite;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14px;
  color: #64748b;
}

.btn-page {
  min-height: 34px;
  padding: 0 14px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes loading-shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}

@media (max-width: 1200px) {
  .order-stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .order-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .order-stats {
    grid-template-columns: 1fr;
  }
}
</style>
