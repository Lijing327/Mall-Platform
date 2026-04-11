<template>
  <div class="login-wrap">
    <div class="card login-card">
      <h1 class="title">用户登录</h1>
      <p class="hint">请输入用户 ID 与统一口令（与后端配置一致）。</p>
      <div class="form-row">
        <label>账号</label>
        <input v-model.number="userId" type="number" min="1" placeholder="用户 ID" />
      </div>
      <div class="form-row">
        <label>口令</label>
        <input v-model="password" type="password" autocomplete="current-password" placeholder="登录口令" />
      </div>
      <button class="btn-submit" type="button" :disabled="loading" @click="onSubmit">
        {{ loading ? "登录中…" : "登录" }}
      </button>
      <p v-if="error" class="err">{{ error }}</p>
      <p class="back">
        <RouterLink to="/products">返回商城首页</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { login } from "../../api/auth";
import { applyLoginSession } from "../../utils/user-context";

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
.login-wrap {
  min-height: calc(100vh - 32px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 8px;
  padding: 28px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #eee;
}

.title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  text-align: center;
}

.hint {
  font-size: 13px;
  color: #667085;
  margin-bottom: 20px;
  line-height: 1.5;
  text-align: center;
}

.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
  gap: 12px;
}

.form-row label {
  width: 48px;
  flex-shrink: 0;
  font-size: 14px;
  color: #333;
}

.form-row input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 14px;
}

.btn-submit {
  width: 100%;
  margin-top: 8px;
  padding: 12px;
  font-size: 16px;
  font-weight: 600;
  background: #e5484d;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-submit:hover:not(:disabled) {
  opacity: 0.92;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.err {
  color: #c00;
  margin-top: 12px;
  font-size: 14px;
  text-align: center;
}

.back {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
}

.back a {
  color: #1677ff;
  text-decoration: none;
}
</style>
