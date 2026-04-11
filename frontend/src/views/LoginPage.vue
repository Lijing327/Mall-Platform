<template>
  <div class="card login-card">
    <h3>登录</h3>
    <p class="hint">MVP：使用配置中的用户 ID + 登录口令换取令牌；管理员 ID 见后端 <code>mall.auth.admin-user-ids</code>。</p>
    <div class="form-row">
      <label>用户 ID</label>
      <input v-model.number="userId" type="number" min="1" />
    </div>
    <div class="form-row">
      <label>口令</label>
      <input v-model="password" type="password" autocomplete="current-password" />
    </div>
    <button class="btn" :disabled="loading" @click="onSubmit">{{ loading ? "登录中…" : "登录" }}</button>
    <p v-if="error" class="err">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { login } from "../api/auth";
import { applyLoginSession } from "../utils/user-context";

const route = useRoute();
const router = useRouter();

const userId = ref(1001);
const password = ref("mvp-demo");
const loading = ref(false);
const error = ref("");

async function onSubmit() {
  error.value = "";
  loading.value = true;
  try {
    const res = await login({ userId: userId.value, password: password.value });
    applyLoginSession(res.data);
    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/products";
    await router.replace(redirect || "/products");
  } catch (e) {
    error.value = e?.message || "登录失败";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-card {
  max-width: 480px;
  margin: 24px auto;
}
.hint {
  font-size: 13px;
  color: #667085;
  margin-bottom: 16px;
  line-height: 1.5;
}
.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  gap: 10px;
}
label {
  width: 72px;
}
input {
  flex: 1;
  padding: 8px 10px;
}
.err {
  color: #c00;
  margin-top: 12px;
}
</style>
