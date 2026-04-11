<template>
  <div class="admin-login-screen">
    <div class="admin-login-panel">
      <div class="admin-login-intro">
        <p class="admin-login-kicker">Admin Portal</p>
        <h1>后台登录</h1>
        <p>
          使用管理员账号进入商城运营后台。登录成功后默认进入仪表盘，并继续支持
          `redirect` 跳转。
        </p>
      </div>

      <div class="admin-login-card">
        <div class="form-row form-row--stack">
          <label for="admin-user-id">用户 ID</label>
          <input id="admin-user-id" v-model.number="userId" type="number" min="1" />
        </div>
        <div class="form-row form-row--stack">
          <label for="admin-password">密码</label>
          <input id="admin-password" v-model="password" type="password" autocomplete="current-password" />
        </div>
        <button class="btn admin-login-button" :disabled="loading" @click="onSubmit">
          {{ loading ? "登录中…" : "登录后台" }}
        </button>
        <p v-if="error" class="err">{{ error }}</p>
        <p class="admin-login-tip">默认演示账号可继续使用 `1 / mvp-demo`。</p>
        <p class="back"><RouterLink to="/products">返回商城前台</RouterLink></p>
      </div>
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

const userId = ref(1);
const password = ref("mvp-demo");
const loading = ref(false);
const error = ref("");

async function onSubmit() {
  error.value = "";
  loading.value = true;
  try {
    const res = await login({ userId: userId.value, password: password.value });
    applyLoginSession(res.data);
    const q = route.query.redirect;
    const redirect = typeof q === "string" && q ? q : "/admin/dashboard";
    await router.replace(redirect || "/admin/dashboard");
  } catch (e) {
    error.value = e?.message || "登录失败";
  } finally {
    loading.value = false;
  }
}
</script>
