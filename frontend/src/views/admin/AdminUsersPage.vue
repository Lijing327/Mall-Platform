<template>
  <div class="admin-page">
    <AdminSearchBar>
      <label class="admin-field">
        <span>用户 ID</span>
        <input v-model.trim="userIdKeyword" placeholder="输入用户 ID" />
      </label>
      <label class="admin-field">
        <span>手机号/用户名</span>
        <input v-model.trim="nameKeyword" placeholder="当前版本可按用户 ID 或默认名称搜索" />
      </label>
      <template #actions>
        <button type="button" class="btn" @click="loadData">查询</button>
        <button type="button" class="btn btn-secondary" @click="resetFilters">重置</button>
      </template>
    </AdminSearchBar>

    <section class="admin-panel">
      <div class="panel-heading">
        <div>
          <h3>用户列表</h3>
          <p>当前根据订单数据聚合出 {{ filteredUsers.length }} 位有交易记录的用户。</p>
        </div>
      </div>

      <div v-if="loading" class="admin-loading">正在加载用户数据…</div>
      <p v-else-if="error" class="admin-error">{{ error }}</p>
      <EmptyState
        v-else-if="filteredUsers.length === 0"
        icon="用"
        title="暂无用户交易数据"
        description="当产生订单后，这里会自动聚合出基础用户列表。"
      />
      <table v-else class="table admin-table">
        <thead>
          <tr>
            <th>用户 ID</th>
            <th>用户名/昵称</th>
            <th>手机号</th>
            <th>最近下单时间</th>
            <th>订单数</th>
            <th>累计支付</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.userId">
            <td>{{ user.userId }}</td>
            <td>
              <div class="table-primary">{{ user.displayName }}</div>
              <div class="table-secondary">由现有订单数据聚合生成</div>
            </td>
            <td>{{ user.mobile }}</td>
            <td>{{ formatDateTime(user.lastOrderTime) }}</td>
            <td>{{ user.orderCount }}</td>
            <td>{{ formatCurrency(user.totalPaid) }}</td>
            <td>
              <AdminTableActions
                :actions="[
                  {
                    label: '查看订单',
                    tone: 'primary',
                    onClick: () => router.push(`/admin/orders?userId=${user.userId}`)
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
import { useRoute, useRouter } from "vue-router";
import AdminSearchBar from "../../components/admin/AdminSearchBar.vue";
import AdminTableActions from "../../components/admin/AdminTableActions.vue";
import EmptyState from "../../components/admin/EmptyState.vue";
import { fetchAdminOrders } from "../../api/admin";
import { formatCurrency, formatDateTime } from "../../utils/admin-ui";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const error = ref("");
const users = ref([]);
const userIdKeyword = ref("");
const nameKeyword = ref("");

const filteredUsers = computed(() =>
  users.value.filter((user) => {
    const matchId = !userIdKeyword.value || String(user.userId).includes(userIdKeyword.value);
    const keyword = nameKeyword.value.toLowerCase();
    const matchName =
      !keyword ||
      user.displayName.toLowerCase().includes(keyword) ||
      String(user.userId).includes(keyword);
    return matchId && matchName;
  })
);

async function loadData() {
  loading.value = true;
  error.value = "";
  try {
    const res = await fetchAdminOrders({ pageNum: 1, pageSize: 100 });
    const grouped = new Map();

    for (const item of res.data.list || []) {
      const existing = grouped.get(item.userId) || {
        userId: item.userId,
        displayName: `用户 ${item.userId}`,
        mobile: "暂未接入",
        lastOrderTime: item.createTime,
        orderCount: 0,
        totalPaid: 0
      };

      existing.orderCount += 1;
      existing.totalPaid += Number(item.payAmount || item.totalAmount || 0);
      if (!existing.lastOrderTime || new Date(item.createTime) > new Date(existing.lastOrderTime)) {
        existing.lastOrderTime = item.createTime;
      }
      grouped.set(item.userId, existing);
    }

    users.value = [...grouped.values()].sort((a, b) => Number(b.totalPaid) - Number(a.totalPaid));
  } catch (e) {
    error.value = e?.message || "用户数据加载失败";
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  userIdKeyword.value = "";
  nameKeyword.value = "";
  router.replace({ path: "/admin/users" });
  loadData();
}

watch(
  () => route.query.userId,
  (value) => {
    userIdKeyword.value = typeof value === "string" ? value : "";
  },
  { immediate: true }
);

onMounted(loadData);
</script>
