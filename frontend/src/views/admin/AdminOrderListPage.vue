<template>
  <div class="admin-page">
    <AdminSearchBar>
      <label class="admin-field">
        <span>订单号</span>
        <input v-model.trim="orderNo" placeholder="输入订单号搜索" />
      </label>
      <label class="admin-field">
        <span>订单状态</span>
        <select v-model="orderStatus">
          <option value="">全部</option>
          <option value="PENDING_PAYMENT">待支付</option>
          <option value="PAID">已支付</option>
          <option value="SHIPPED">已发货</option>
          <option value="COMPLETED">已完成</option>
        </select>
      </label>
      <label class="admin-field">
        <span>用户</span>
        <input v-model.trim="userKeyword" placeholder="输入用户 ID" />
      </label>
      <template #actions>
        <button type="button" class="btn" @click="loadData">查询</button>
        <button type="button" class="btn btn-secondary" @click="resetFilters">重置</button>
      </template>
    </AdminSearchBar>

    <section class="admin-panel">
      <div class="panel-heading">
        <div>
          <h3>订单列表</h3>
          <p>当前共 {{ filteredList.length }} 条，待发货 {{ paidCount }} 条，已完成 {{ completedCount }} 条。</p>
        </div>
      </div>

      <div v-if="loading" class="admin-loading">正在加载订单数据…</div>
      <p v-else-if="error" class="admin-error">{{ error }}</p>
      <EmptyState
        v-else-if="filteredList.length === 0"
        icon="订"
        title="没有找到符合条件的订单"
        description="可以调整筛选条件后重新查询。"
      />
      <table v-else class="table admin-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户</th>
            <th>订单金额</th>
            <th>实付金额</th>
            <th>状态</th>
            <th>支付方式</th>
            <th>下单时间</th>
            <th>发货信息</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredList" :key="item.orderId">
            <td>
              <div class="table-primary">{{ item.orderNo }}</div>
              <div class="table-secondary">订单 ID: {{ item.orderId }}</div>
            </td>
            <td>
              <div class="table-primary">用户 {{ item.userId }}</div>
              <RouterLink class="text-link" :to="`/admin/users?userId=${item.userId}`">查看用户</RouterLink>
            </td>
            <td>{{ formatCurrency(item.totalAmount) }}</td>
            <td>{{ formatCurrency(item.payAmount || item.totalAmount) }}</td>
            <td><OrderStatusTag :status="item.orderStatus" /></td>
            <td>{{ payTypeZh(item.payType) }}</td>
            <td>{{ formatDateTime(item.createTime) }}</td>
            <td>
              <div class="table-primary">{{ item.shippingNo || "未发货" }}</div>
              <div class="table-secondary">{{ formatDateTime(item.shipTime) }}</div>
            </td>
            <td>
              <AdminTableActions
                :actions="[
                  {
                    label: '查看详情',
                    tone: 'primary',
                    onClick: () => openDetail(item.orderId)
                  }
                ]"
              />
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import AdminSearchBar from "../../components/admin/AdminSearchBar.vue";
import AdminTableActions from "../../components/admin/AdminTableActions.vue";
import EmptyState from "../../components/admin/EmptyState.vue";
import OrderStatusTag from "../../components/admin/OrderStatusTag.vue";
import { fetchAdminOrders } from "../../api/admin";
import { formatCurrency, formatDateTime } from "../../utils/admin-ui";
import { payTypeZh } from "../../utils/display-labels";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const error = ref("");
const list = ref([]);
const orderStatus = ref("");
const orderNo = ref("");
const userKeyword = ref("");

const filteredList = computed(() =>
  list.value.filter((item) => {
    const matchOrderNo = !orderNo.value || String(item.orderNo || "").includes(orderNo.value);
    const matchStatus = !orderStatus.value || item.orderStatus === orderStatus.value;
    const matchUser = !userKeyword.value || String(item.userId || "").includes(userKeyword.value);
    return matchOrderNo && matchStatus && matchUser;
  })
);

const paidCount = computed(() => filteredList.value.filter((item) => item.orderStatus === "PAID").length);
const completedCount = computed(() => filteredList.value.filter((item) => item.orderStatus === "COMPLETED").length);

async function loadData() {
  loading.value = true;
  error.value = "";
  try {
    const res = await fetchAdminOrders({
      pageNum: 1,
      pageSize: 100,
      orderStatus: orderStatus.value || undefined,
      orderNo: orderNo.value || undefined
    });
    list.value = res.data.list || [];
  } catch (e) {
    error.value = e?.message || "订单数据加载失败";
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  orderStatus.value = "";
  orderNo.value = "";
  userKeyword.value = "";
  router.replace({ path: "/admin/orders" });
  loadData();
}

function openDetail(orderId) {
  router.push(`/admin/orders/${orderId}`);
}

watch(
  () => route.query.userId,
  (value) => {
    userKeyword.value = typeof value === "string" ? value : "";
  },
  { immediate: true }
);

onMounted(loadData);
</script>
