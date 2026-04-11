<template>
  <div class="card">
    <div class="page-mark merchant">
      <h3>商家审核中心</h3>
      <p>处理入驻申请、通过或驳回，并跟踪审核备注。</p>
    </div>
    <div class="toolbar">
      <select v-model="applyStatus">
        <option value="">全部状态</option>
        <option value="PENDING">待审核</option>
        <option value="APPROVED">已通过</option>
        <option value="REJECTED">已拒绝</option>
      </select>
      <input v-model="keyword" placeholder="商家名称关键字" />
      <button class="btn" @click="search">查询</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>商家ID</th>
          <th>商家名称</th>
          <th>联系人</th>
          <th>状态</th>
          <th>备注</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.merchantId">
          <td>{{ item.merchantId }}</td>
          <td>{{ item.merchantName }}</td>
          <td>{{ item.contactName }} / {{ item.contactMobile }}</td>
          <td>{{ applyStatusZh(item.applyStatus) }}</td>
          <td>{{ item.auditRemark || "-" }}</td>
          <td class="ops">
            <button class="btn" :disabled="item.applyStatus !== 'PENDING'" @click="approve(item)">通过</button>
            <button class="btn secondary" :disabled="item.applyStatus !== 'PENDING'" @click="reject(item)">驳回</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { auditMerchant, fetchAdminMerchants } from "../../api/admin";
import { applyStatusZh } from "../../utils/display-labels";

const list = ref([]);
const applyStatus = ref("");
const keyword = ref("");

async function loadData() {
  const res = await fetchAdminMerchants({
    pageNum: 1,
    pageSize: 100,
    applyStatus: applyStatus.value || undefined,
    keyword: keyword.value || undefined
  });
  list.value = res.data.list || [];
}

function search() {
  loadData();
}

async function approve(item) {
  await auditMerchant({
    merchantId: item.merchantId,
    auditAction: "APPROVE",
    auditRemark: "管理员审核通过"
  });
  await loadData();
}

async function reject(item) {
  const reason = window.prompt("请输入驳回原因");
  if (!reason) return;
  await auditMerchant({
    merchantId: item.merchantId,
    auditAction: "REJECT",
    auditRemark: reason
  });
  await loadData();
}

onMounted(loadData);
</script>

<style scoped>
.page-mark {
  border-radius: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
}
.page-mark h3 {
  margin: 0 0 6px;
}
.page-mark p {
  margin: 0;
  color: #5c6370;
  font-size: 13px;
}
.page-mark.merchant {
  background: #f3f8ff;
  border: 1px solid #dce9ff;
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
input,
select {
  padding: 6px 8px;
}
.table {
  width: 100%;
  border-collapse: collapse;
}
.table th,
.table td {
  border-bottom: 1px solid #eee;
  padding: 8px 6px;
}
.ops {
  display: flex;
  gap: 8px;
}
</style>
