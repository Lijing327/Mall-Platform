<template>
  <div class="card">
    <div class="head">
      <h3>我的地址</h3>
      <button type="button" class="btn primary" @click="openCreate">+ 新增地址</button>
    </div>

    <div v-if="list.length === 0" class="empty">还没有收货地址，先添加一个吧～</div>

    <div class="address-list">
      <div v-for="addr in list" :key="addr.id" class="addr-card" :class="{ 'is-default': addr.isDefault }">
        <div class="addr-main">
          <div class="addr-line1">
            <span class="name">{{ addr.receiverName }}</span>
            <span class="mobile">{{ addr.receiverMobile }}</span>
            <span v-if="addr.isDefault" class="default-tag">默认</span>
          </div>
          <div class="addr-line2">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}</div>
        </div>
        <div class="addr-actions">
          <button v-if="!addr.isDefault" type="button" class="link" @click="onSetDefault(addr)">设为默认</button>
          <button type="button" class="link" @click="openEdit(addr)">编辑</button>
          <button type="button" class="link danger" @click="onDelete(addr)">删除</button>
        </div>
      </div>
    </div>

    <div v-if="showForm" class="modal-mask" @click.self="closeForm">
      <div class="modal">
        <div class="modal-head">{{ editingId ? "编辑地址" : "新增地址" }}</div>
        <form class="form" @submit.prevent="submitForm">
          <div class="row">
            <label>收货人</label>
            <input v-model="form.receiverName" maxlength="64" required />
          </div>
          <div class="row">
            <label>手机号</label>
            <input v-model="form.receiverMobile" maxlength="32" required />
          </div>
          <div class="row">
            <label>省</label>
            <input v-model="form.province" maxlength="64" required />
          </div>
          <div class="row">
            <label>市</label>
            <input v-model="form.city" maxlength="64" required />
          </div>
          <div class="row">
            <label>区/县</label>
            <input v-model="form.district" maxlength="64" required />
          </div>
          <div class="row">
            <label>详细地址</label>
            <textarea v-model="form.detailAddress" maxlength="256" required rows="2"></textarea>
          </div>
          <div class="row check-row">
            <label>
              <input v-model="form.isDefault" type="checkbox" />
              设为默认地址
            </label>
          </div>
          <div class="modal-actions">
            <button type="button" class="btn" @click="closeForm">取消</button>
            <button type="submit" class="btn primary" :disabled="submitting">
              {{ submitting ? "提交中…" : "保存" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import {
  createAddress,
  deleteAddress,
  fetchAddressList,
  setDefaultAddress,
  updateAddress
} from "../api/address";

const list = ref([]);
const showForm = ref(false);
const editingId = ref(null);
const submitting = ref(false);

const blankForm = () => ({
  receiverName: "",
  receiverMobile: "",
  province: "",
  city: "",
  district: "",
  detailAddress: "",
  isDefault: false
});
const form = reactive(blankForm());

async function loadData() {
  const res = await fetchAddressList();
  list.value = res.data || [];
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, blankForm());
  form.isDefault = list.value.length === 0;
  showForm.value = true;
}

function openEdit(addr) {
  editingId.value = addr.id;
  form.receiverName = addr.receiverName || "";
  form.receiverMobile = addr.receiverMobile || "";
  form.province = addr.province || "";
  form.city = addr.city || "";
  form.district = addr.district || "";
  form.detailAddress = addr.detailAddress || "";
  form.isDefault = !!addr.isDefault;
  showForm.value = true;
}

function closeForm() {
  showForm.value = false;
}

async function submitForm() {
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateAddress(editingId.value, { ...form });
    } else {
      await createAddress({ ...form });
    }
    closeForm();
    await loadData();
  } catch (e) {
    window.alert(e?.message || "保存失败");
  } finally {
    submitting.value = false;
  }
}

async function onSetDefault(addr) {
  try {
    await setDefaultAddress(addr.id);
    await loadData();
  } catch (e) {
    window.alert(e?.message || "设置失败");
  }
}

async function onDelete(addr) {
  if (!window.confirm(`确认删除收货地址【${addr.receiverName}】?`)) return;
  try {
    await deleteAddress(addr.id);
    await loadData();
  } catch (e) {
    window.alert(e?.message || "删除失败");
  }
}

onMounted(loadData);
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.empty {
  color: #888;
  padding: 12px 0;
}
.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.addr-card {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.addr-card.is-default {
  border-color: #1677ff;
  background: #f6faff;
}
.addr-main {
  flex: 1;
  min-width: 0;
}
.addr-line1 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}
.name {
  font-weight: 600;
}
.mobile {
  color: #666;
}
.default-tag {
  display: inline-block;
  padding: 1px 6px;
  background: #1677ff;
  color: #fff;
  font-size: 12px;
  border-radius: 3px;
}
.addr-line2 {
  color: #555;
  font-size: 14px;
}
.addr-actions {
  display: flex;
  gap: 10px;
}
.link {
  background: none;
  border: none;
  color: #1677ff;
  cursor: pointer;
  padding: 4px 2px;
  font-size: 13px;
}
.link.danger {
  color: #e5484d;
}
.btn {
  padding: 6px 14px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
}
.btn.primary {
  background: #1677ff;
  color: #fff;
  border-color: #1677ff;
}
.btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: 8px;
  width: 420px;
  max-width: calc(100% - 32px);
  padding: 16px 20px;
}
.modal-head {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}
.form .row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}
.form label {
  font-size: 13px;
  color: #555;
}
.form input,
.form textarea {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 6px 8px;
  font-size: 14px;
  font-family: inherit;
}
.check-row label {
  display: flex;
  align-items: center;
  gap: 6px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}
</style>
