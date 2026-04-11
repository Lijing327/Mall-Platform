<template>
  <div class="card">
    <h3>商家入驻申请</h3>
    <div class="form-row"><label>商家名称</label><input v-model="form.merchantName" /></div>
    <div class="form-row"><label>联系人</label><input v-model="form.contactName" /></div>
    <div class="form-row"><label>联系电话</label><input v-model="form.contactMobile" /></div>
    <div class="form-row"><label>资质文本</label><textarea v-model="form.qualificationText" rows="4" /></div>
    <button class="btn" @click="submitApply">提交申请</button>
    <p v-if="result" class="result">{{ result }}</p>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { merchantApply } from "../../api/merchant";
const form = reactive({
  merchantName: "",
  contactName: "",
  contactMobile: "",
  qualificationText: ""
});
const result = ref("");

async function submitApply() {
  const res = await merchantApply(form);
  result.value = `申请已提交，商家ID=${res.data.merchantId}，状态=${res.data.applyStatus}`;
}
</script>

<style scoped>
.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}
label {
  width: 80px;
}
input,
textarea {
  width: 360px;
  padding: 6px 8px;
}
.result {
  margin-top: 10px;
  color: #1677ff;
}
</style>
