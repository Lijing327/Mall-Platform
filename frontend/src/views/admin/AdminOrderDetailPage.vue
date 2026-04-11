<template>
  <div class="admin-page">
    <div class="admin-page-toolbar">
      <RouterLink class="btn btn-secondary" to="/admin/orders">返回订单列表</RouterLink>
    </div>

    <div v-if="loading" class="admin-loading">正在加载订单详情…</div>
    <p v-else-if="error" class="admin-error">{{ error }}</p>
    <EmptyState
      v-else-if="!order"
      icon="订"
      title="没有找到这笔订单"
      description="请返回订单列表确认订单是否存在。"
    />
    <template v-else>
      <div class="detail-grid">
        <InfoBlock
          title="订单基本信息"
          :items="[
            { label: '订单号', value: order.orderNo },
            { label: '订单状态', value: orderStatusZh(order.orderStatus) },
            { label: '下单时间', value: formatDateTime(order.createTime) },
            { label: '支付时间', value: formatDateTime(order.payTime) }
          ]"
        >
          <template #extra>
            <OrderStatusTag :status="order.orderStatus" />
          </template>
        </InfoBlock>

        <InfoBlock
          title="收货信息"
          :items="[
            { label: '收货人', value: '当前管理端接口未返回' },
            { label: '手机号', value: '当前管理端接口未返回' },
            { label: '收货地址', value: '当前管理端接口未返回' },
            { label: '用户 ID', value: `用户 ${order.userId}` }
          ]"
        />
      </div>

      <div class="detail-grid">
        <InfoBlock
          title="金额信息"
          :items="[
            { label: '商品总额', value: formatCurrency(order.totalAmount) },
            { label: '运费', value: '暂未拆分' },
            { label: '实付金额', value: formatCurrency(order.payAmount || order.totalAmount) },
            { label: '支付方式', value: payTypeZh(order.payType) }
          ]"
        />

        <InfoBlock title="发货信息" :items="shippingItems" />
      </div>

      <InfoBlock title="商品信息">
        <EmptyState
          icon="商"
          title="当前未返回商品明细"
          description="现有后台接口只提供订单汇总字段。本页已预留正式详情结构，后续接入明细接口后可直接补齐商品表格与收货信息。"
        />
      </InfoBlock>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import EmptyState from "../../components/admin/EmptyState.vue";
import InfoBlock from "../../components/admin/InfoBlock.vue";
import OrderStatusTag from "../../components/admin/OrderStatusTag.vue";
import { fetchAdminOrders } from "../../api/admin";
import { formatCurrency, formatDateTime } from "../../utils/admin-ui";
import { orderStatusZh, payTypeZh } from "../../utils/display-labels";

const route = useRoute();

const loading = ref(false);
const error = ref("");
const order = ref(null);

const shippingItems = computed(() => [
  { label: "物流单号", value: order.value?.shippingNo || "未发货" },
  { label: "发货时间", value: formatDateTime(order.value?.shipTime) },
  {
    label: "处理状态",
    value: order.value?.orderStatus === "PAID" ? "待发货" : orderStatusZh(order.value?.orderStatus)
  },
  { label: "发货备注", value: "当前管理端接口未返回" }
]);

async function loadData() {
  loading.value = true;
  error.value = "";
  try {
    const orderId = Number(route.params.id);
    const res = await fetchAdminOrders({ pageNum: 1, pageSize: 100 });
    const matched = (res.data.list || []).find((item) => Number(item.orderId) === orderId);
    order.value = matched || null;
  } catch (e) {
    error.value = e?.message || "订单详情加载失败";
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>
