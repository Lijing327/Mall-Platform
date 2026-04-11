<template>
  <div class="admin-page">
    <AdminSearchBar>
      <label class="admin-field">
        <span>商品名称</span>
        <input v-model.trim="keyword" placeholder="输入商品名称搜索" />
      </label>
      <label class="admin-field">
        <span>商品状态</span>
        <select v-model="saleStatus">
          <option value="">全部</option>
          <option value="ON_SHELF">上架</option>
          <option value="OFF_SHELF">下架</option>
        </select>
      </label>
      <template #actions>
        <button type="button" class="btn" @click="loadData">查询</button>
        <button type="button" class="btn btn-secondary" @click="resetFilters">重置</button>
      </template>
    </AdminSearchBar>

    <section class="admin-panel">
      <div class="panel-heading">
        <div>
          <h3>商品列表</h3>
          <p>共 {{ filteredList.length }} 条，已上架 {{ onShelfCount }} 条。</p>
        </div>
      </div>

      <div v-if="loading" class="admin-loading">正在加载商品数据…</div>
      <p v-else-if="error" class="admin-error">{{ error }}</p>
      <EmptyState
        v-else-if="filteredList.length === 0"
        icon="商"
        title="没有找到符合条件的商品"
        description="可以尝试调整搜索词或状态筛选。"
      />
      <table v-else class="table admin-table">
        <thead>
          <tr>
            <th style="width: 80px">ID</th>
            <th style="width: 96px">图片</th>
            <th>商品名称</th>
            <th style="width: 120px">价格</th>
            <th style="width: 100px">库存</th>
            <th style="width: 110px">状态</th>
            <th style="width: 150px">创建时间</th>
            <th style="width: 220px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredList" :key="item.productId">
            <td>{{ item.productId }}</td>
            <td>
              <div class="product-thumb">{{ getInitial(item.productName) }}</div>
            </td>
            <td>
              <div class="table-primary">{{ item.productName }}</div>
              <div class="table-secondary">SPU: {{ item.productSn || "未设置" }} · 店铺 {{ item.shopId }}</div>
            </td>
            <td>{{ formatCurrency(item.price) }}</td>
            <td>{{ item.stock ?? 0 }}</td>
            <td>
              <span class="status-tag" :class="`status-tag--${getSaleStatusTone(item.saleStatus)}`">
                {{ saleStatusZh(item.saleStatus) }}
              </span>
            </td>
            <td>{{ formatDateTime(item.createTime) }}</td>
            <td>
              <AdminTableActions :actions="buildActions(item)" />
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import AdminSearchBar from "../../components/admin/AdminSearchBar.vue";
import AdminTableActions from "../../components/admin/AdminTableActions.vue";
import EmptyState from "../../components/admin/EmptyState.vue";
import { adminOffShelfProduct, fetchAdminProducts } from "../../api/admin";
import { formatCurrency, formatDateTime, getInitial, getSaleStatusTone } from "../../utils/admin-ui";
import { saleStatusZh } from "../../utils/display-labels";

const loading = ref(false);
const error = ref("");
const list = ref([]);
const keyword = ref("");
const saleStatus = ref("");

const filteredList = computed(() =>
  list.value.filter((item) => {
    const matchKeyword =
      !keyword.value || String(item.productName || "").toLowerCase().includes(keyword.value.toLowerCase());
    const matchStatus = !saleStatus.value || item.saleStatus === saleStatus.value;
    return matchKeyword && matchStatus;
  })
);

const onShelfCount = computed(() => list.value.filter((item) => item.saleStatus === "ON_SHELF").length);

async function loadData() {
  loading.value = true;
  error.value = "";
  try {
    const res = await fetchAdminProducts({
      pageNum: 1,
      pageSize: 100,
      keyword: keyword.value || undefined,
      saleStatus: saleStatus.value || undefined
    });
    list.value = res.data.list || [];
  } catch (e) {
    error.value = e?.message || "商品数据加载失败";
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  keyword.value = "";
  saleStatus.value = "";
  loadData();
}

function showComingSoon(label) {
  window.alert(`${label} 页面暂未开放，当前版本先完成后台列表管理。`);
}

function buildActions(item) {
  return [
    {
      label: "查看",
      tone: "neutral",
      onClick: () => showComingSoon(`商品 ${item.productId} 查看`)
    },
    {
      label: "编辑",
      tone: "neutral",
      onClick: () => showComingSoon(`商品 ${item.productId} 编辑`)
    },
    {
      label: item.saleStatus === "OFF_SHELF" ? "已下架" : "下架",
      tone: "danger",
      disabled: item.saleStatus === "OFF_SHELF",
      onClick: () => offShelf(item)
    }
  ];
}

async function offShelf(item) {
  try {
    await adminOffShelfProduct(item.productId);
    await loadData();
  } catch (e) {
    window.alert(e?.message || "商品下架失败");
  }
}

onMounted(loadData);
</script>
