<template>
  <div class="admin-page order-detail-page">
    <AdminPageHeader
      eyebrow="Order Detail"
      title="订单详情"
      subtitle="查看订单信息并处理发货。当前版本优先复用现有管理端接口能力。"
    >
      <template #actions>
        <button type="button" class="btn btn-secondary" @click="loadData">刷新</button>
        <RouterLink class="btn btn-secondary" to="/admin/orders">返回订单列表</RouterLink>
      </template>
    </AdminPageHeader>

    <div v-if="loading" class="order-loading-card">
      <div class="order-skeleton order-skeleton--status"></div>
      <div class="order-skeleton-grid">
        <div class="order-skeleton order-skeleton--block"></div>
        <div class="order-skeleton order-skeleton--block"></div>
        <div class="order-skeleton order-skeleton--block"></div>
        <div class="order-skeleton order-skeleton--block"></div>
      </div>
      <div class="order-skeleton order-skeleton--table"></div>
    </div>

    <p v-else-if="error" class="admin-error">{{ error }}</p>

    <EmptyState
      v-else-if="!order"
      icon="订"
      title="订单不存在或已删除"
      description="请返回订单列表确认订单编号是否正确。"
    />

    <template v-else>
      <section class="order-status-panel">
        <div class="order-status-main">
          <p class="order-status-kicker">当前状态</p>
          <div class="order-status-row">
            <OrderStatusTag :status="order.orderStatus" />
            <strong class="order-status-title">{{ statusHeadline }}</strong>
          </div>
          <p class="order-status-description">{{ statusDescription }}</p>
        </div>

        <div class="order-status-meta">
          <div class="status-meta-item">
            <span>下单时间</span>
            <strong>{{ formatDateTime(order.createTime) }}</strong>
          </div>
          <div class="status-meta-item">
            <span>支付时间</span>
            <strong>{{ formatDateTime(order.payTime) }}</strong>
          </div>
          <div class="status-meta-item">
            <span>发货时间</span>
            <strong>{{ formatDateTime(order.shipTime) }}</strong>
          </div>
          <div class="status-meta-item">
            <span>完成时间</span>
            <strong>{{ completeTimeText }}</strong>
          </div>
        </div>
      </section>

      <div class="detail-grid">
        <InfoBlock title="基础信息" :items="basicInfoItems" />
        <InfoBlock title="金额信息" :items="amountItems" />
        <InfoBlock title="收货信息" :items="receiverItems" />
        <InfoBlock title="支付信息" :items="paymentItems" />
      </div>

      <section class="admin-panel">
        <div class="panel-heading">
          <div>
            <h3>商品明细</h3>
            <p>当前管理端列表接口未返回订单商品明细，先保留正式展示区结构。</p>
          </div>
        </div>

        <EmptyState
          icon="商"
          title="暂无商品明细"
          description="后续接入订单详情接口后，这里可直接展示商品图片、名称、单价、数量和小计。"
        />
      </section>

      <section class="admin-panel">
        <div class="panel-heading">
          <div>
            <h3>{{ shipmentSectionTitle }}</h3>
            <p>{{ shipmentSectionSubtitle }}</p>
          </div>
        </div>

        <template v-if="canShip">
          <div class="shipment-form">
            <label class="admin-field">
              <span>物流单号</span>
              <input
                v-model.trim="shippingForm.shippingNo"
                placeholder="当前管理端暂无发货接口，先展示正式表单结构"
                disabled
              />
            </label>
            <label class="admin-field">
              <span>发货备注</span>
              <textarea
                v-model.trim="shippingForm.shippingRemark"
                rows="4"
                placeholder="可填写仓库说明、包装备注等"
                disabled
              />
            </label>
            <p class="shipment-tip">
              当前管理端后端暂未开放发货接口，所以本页先展示正式操作区结构，避免继续是占位页。
            </p>
          </div>
        </template>

        <template v-else>
          <div class="shipment-readonly">
            <div class="shipment-readonly-item">
              <span>物流单号</span>
              <strong>{{ order.shippingNo || "暂无物流单号" }}</strong>
            </div>
            <div class="shipment-readonly-item">
              <span>发货时间</span>
              <strong>{{ formatDateTime(order.shipTime) }}</strong>
            </div>
            <div class="shipment-readonly-item">
              <span>发货备注</span>
              <strong>{{ shipmentRemarkText }}</strong>
            </div>
          </div>
        </template>
      </section>

      <section class="order-footer-actions">
        <RouterLink class="btn btn-secondary" to="/admin/orders">返回订单列表</RouterLink>
        <button
          v-if="canShip"
          type="button"
          class="btn"
          disabled
          title="当前管理端后端未提供发货接口"
        >
          确认发货
        </button>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { RouterLink, useRoute } from "vue-router";
import AdminPageHeader from "../../components/admin/AdminPageHeader.vue";
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
const shippingForm = ref({
  shippingNo: "",
  shippingRemark: ""
});

const basicInfoItems = computed(() => [
  { label: "订单号", value: order.value?.orderNo || "暂无" },
  { label: "订单 ID", value: order.value?.orderId || "暂无" },
  { label: "用户 ID", value: order.value?.userId ? `用户 ${order.value.userId}` : "暂无" },
  { label: "订单状态", value: orderStatusZh(order.value?.orderStatus) },
  { label: "支付方式", value: payTypeZh(order.value?.payType) },
  { label: "订单来源", value: "商城用户端" }
]);

const amountItems = computed(() => [
  { label: "商品总额", value: formatCurrency(order.value?.totalAmount) },
  { label: "运费", value: "¥0.00" },
  { label: "实付金额", value: formatCurrency(order.value?.payAmount || order.value?.totalAmount) },
  { label: "支付金额", value: formatCurrency(order.value?.payAmount || order.value?.totalAmount) }
]);

const receiverItems = computed(() => [
  { label: "收货人", value: "暂无收货信息" },
  { label: "联系电话", value: "暂无收货信息" },
  { label: "收货地址", value: "暂无收货信息" }
]);

const paymentItems = computed(() => [
  { label: "支付状态", value: paymentStatusText.value },
  { label: "支付方式", value: payTypeZh(order.value?.payType) },
  { label: "支付时间", value: formatDateTime(order.value?.payTime) },
  { label: "支付备注", value: "暂无支付备注" }
]);

const canShip = computed(() => order.value?.orderStatus === "PAID");
const completeTimeText = computed(() =>
  order.value?.orderStatus === "COMPLETED" ? "订单已完成，当前接口未返回完成时间" : "—"
);
const paymentStatusText = computed(() => {
  if (order.value?.orderStatus === "PENDING_PAYMENT") {
    return "待支付";
  }
  if (order.value?.payTime) {
    return "已支付";
  }
  return "暂无支付信息";
});

const shipmentRemarkText = computed(() => {
  if (canShip.value) {
    return "待发货";
  }
  return order.value?.shippingNo ? "当前管理端接口未返回发货备注" : "暂无发货信息";
});

const statusHeadline = computed(() => {
  if (canShip.value) {
    return "待发货";
  }
  return orderStatusZh(order.value?.orderStatus);
});

const statusDescription = computed(() => {
  switch (order.value?.orderStatus) {
    case "PENDING_PAYMENT":
      return "订单仍在等待用户完成支付，后台暂时无需发货操作。";
    case "PAID":
      return "订单已支付，下一步应安排发货。";
    case "SHIPPED":
      return "订单已经发货，当前以查看物流信息为主。";
    case "COMPLETED":
      return "订单流程已完成，可回看支付与发货信息。";
    default:
      return "查看当前订单状态与处理进度。";
  }
});

const shipmentSectionTitle = computed(() => (canShip.value ? "发货操作" : "发货信息"));
const shipmentSectionSubtitle = computed(() =>
  canShip.value ? "订单待发货时展示操作区，便于后续直接接入真实发货能力。" : "订单已进入发货后阶段，展示当前可查看的发货信息。"
);

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

loadData();
</script>

<style scoped>
.order-detail-page {
  gap: 20px;
}

.order-loading-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.order-skeleton {
  border-radius: 18px;
  background: linear-gradient(90deg, #eef2f7 25%, #f8fafc 37%, #eef2f7 63%);
  background-size: 400% 100%;
  animation: order-shimmer 1.2s infinite;
}

.order-skeleton--status {
  min-height: 170px;
}

.order-skeleton--block {
  min-height: 184px;
}

.order-skeleton--table {
  min-height: 240px;
}

.order-status-panel {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: 18px;
  padding: 22px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
  border: 1px solid #dfe9f6;
}

.order-status-kicker {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 700;
}

.order-status-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.order-status-title {
  font-size: 24px;
  color: #172033;
}

.order-status-description {
  margin: 0;
  color: #667085;
  line-height: 1.7;
}

.order-status-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.status-meta-item {
  padding: 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e6edf6;
}

.status-meta-item span {
  display: block;
  margin-bottom: 8px;
  color: #667085;
  font-size: 12px;
}

.status-meta-item strong {
  color: #172033;
  font-size: 14px;
}

.shipment-form {
  display: grid;
  gap: 14px;
}

.shipment-form textarea {
  width: 100%;
  border: 1px solid #d4dbe7;
  background: #fff;
  border-radius: 10px;
  padding: 10px 12px;
  resize: vertical;
}

.shipment-tip {
  margin: 0;
  color: #667085;
  font-size: 13px;
  line-height: 1.7;
}

.shipment-readonly {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.shipment-readonly-item {
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e6edf6;
}

.shipment-readonly-item span {
  display: block;
  margin-bottom: 8px;
  color: #667085;
  font-size: 12px;
}

.shipment-readonly-item strong {
  color: #172033;
  font-size: 14px;
  line-height: 1.6;
}

.order-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@keyframes order-shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}

@media (max-width: 960px) {
  .order-status-panel,
  .order-skeleton-grid,
  .shipment-readonly {
    grid-template-columns: 1fr;
  }

  .order-status-meta {
    grid-template-columns: 1fr;
  }
}
</style>
