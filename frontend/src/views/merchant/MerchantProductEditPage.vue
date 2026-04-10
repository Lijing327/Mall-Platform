<template>
  <div class="card">
    <h3>{{ isEdit ? "编辑商品" : "新增商品" }}</h3>
    <div class="form-row"><label>商品名称</label><input v-model="form.productName" /></div>
    <div class="form-row"><label>副标题</label><input v-model="form.productSubtitle" /></div>
    <div class="form-row"><label>主图URL</label><input v-model="form.mainImage" /></div>
    <div class="form-row"><label>价格</label><input v-model.number="form.price" type="number" step="0.01" /></div>
    <div class="form-row"><label>库存</label><input v-model.number="form.stock" type="number" /></div>
    <div class="form-row"><label>详情</label><textarea v-model="form.detail" rows="4" /></div>
    <button class="btn" @click="submitForm">{{ isEdit ? "保存修改" : "新增商品" }}</button>
  </div>
</template>

<script setup>
import { computed, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { createMerchantProduct, fetchMerchantProducts, updateMerchantProduct } from "../../api/merchant";
import { getMerchantId, getUserId } from "../../utils/user-context";

const route = useRoute();
const router = useRouter();
const isEdit = computed(() => !!route.params.id);

const form = reactive({
  userId: getUserId(),
  merchantId: getMerchantId(),
  productName: "",
  productSubtitle: "",
  mainImage: "",
  detail: "",
  price: 0,
  stock: 0
});

async function loadDetailIfEdit() {
  if (!isEdit.value) return;
  const res = await fetchMerchantProducts({
    userId: form.userId,
    merchantId: form.merchantId,
    pageNum: 1,
    pageSize: 200
  });
  const target = (res.data.list || []).find((item) => Number(item.id) === Number(route.params.id));
  if (!target) return;
  form.productName = target.productName;
  form.productSubtitle = target.productSubtitle || "";
  form.mainImage = target.mainImage || "";
  form.price = Number(target.price || 0);
  form.stock = Number(target.stock || 0);
}

async function submitForm() {
  if (isEdit.value) {
    await updateMerchantProduct(route.params.id, form);
    window.alert("修改成功");
  } else {
    await createMerchantProduct(form);
    window.alert("新增成功");
  }
  router.push("/merchant/products");
}

loadDetailIfEdit();
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
  width: 420px;
  padding: 6px 8px;
}
</style>
