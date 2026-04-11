<template>
  <div class="mall-layout">
    <header class="mall-header">
      <div class="header-inner">
        <RouterLink class="logo" to="/products">自营商城 MVP 1.0</RouterLink>

        <div class="search-wrap">
          <input
            v-model="searchKeyword"
            type="search"
            placeholder="搜索商品名称"
            autocomplete="off"
            @keyup.enter="runSearch"
          />
          <button type="button" class="btn-search" @click="runSearch">搜索</button>
        </div>

        <nav class="nav-links">
          <RouterLink to="/products">商品首页</RouterLink>
          <RouterLink to="/cart">购物车</RouterLink>
          <RouterLink to="/orders">我的订单</RouterLink>
          <RouterLink to="/addresses">我的地址</RouterLink>
        </nav>

        <div class="user-area">
          <template v-if="tokenPresent">
            <span class="identity-chip" :class="identityChipClass" :title="identityTitle">{{ identityLabel }}</span>
            <span class="user-name">用户 {{ displayUserId }}</span>
            <RouterLink v-if="isAdmin()" class="btn-admin-entry" to="/admin/dashboard">管理后台</RouterLink>
            <button type="button" class="btn-text" @click="doLogout">退出</button>
          </template>
          <template v-else>
            <RouterLink class="btn-login" to="/login">登录</RouterLink>
          </template>
        </div>
      </div>

      <div class="merchant-bar">
        <span class="merchant-label">商家端</span>
        <template v-if="!tokenPresent">
          <span class="merchant-hint">登录后可提交入驻申请；审核通过后可管理商品与订单。</span>
          <RouterLink class="merchant-action" to="/login">去登录</RouterLink>
        </template>
        <template v-else-if="merchantLoading">
          <span class="merchant-hint">加载商家信息…</span>
        </template>
        <template v-else-if="merchantLoadError">
          <span class="merchant-hint warn">商家信息加载失败，无法区分是否已入驻（请确认后端已启动）。</span>
          <button type="button" class="btn-retry" @click="syncMerchantProfile">重试</button>
        </template>
        <template v-else-if="!merchantMe">
          <RouterLink class="merchant-action" to="/merchant/apply">入驻申请</RouterLink>
        </template>
        <template v-else-if="merchantMe.applyStatus === 'PENDING'">
          <span class="merchant-hint">入驻审核中，通过后即可管理店铺</span>
        </template>
        <template v-else-if="merchantMe.applyStatus === 'REJECTED'">
          <span class="merchant-hint warn">入驻未通过，暂不可管理商品</span>
        </template>
        <template v-else-if="merchantMe.applyStatus === 'APPROVED'">
          <RouterLink class="merchant-action" to="/merchant/products">商品管理</RouterLink>
          <span class="sep">|</span>
          <RouterLink class="merchant-action" to="/merchant/orders">店铺订单</RouterLink>
        </template>
        <template v-else>
          <RouterLink class="merchant-action" to="/merchant/apply">入驻申请</RouterLink>
        </template>
      </div>
    </header>

    <main class="mall-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { fetchMerchantMe } from "../api/merchant";
import { getToken, getUserId, isAdmin, logout, setMerchantId } from "../utils/user-context";

const route = useRoute();
const router = useRouter();

const searchKeyword = ref("");
const tokenPresent = ref(!!getToken());
const displayUserId = ref(getUserId());
const merchantLoading = ref(false);
const merchantLoadError = ref(false);
/** 当前用户是否已有商家记录（含待审/未通过/已通过） */
const merchantMe = ref(null);

const identityLabel = computed(() => {
  if (!tokenPresent.value) {
    return "";
  }
  if (isAdmin()) {
    return "平台管理员";
  }
  if (merchantLoadError.value) {
    return "身份未同步";
  }
  if (!merchantMe.value) {
    return "买家";
  }
  const s = merchantMe.value.applyStatus;
  if (s === "APPROVED") {
    return "已入驻商家";
  }
  if (s === "PENDING") {
    return "商家·审核中";
  }
  if (s === "REJECTED") {
    return "商家·未通过";
  }
  return "买家";
});

const identityTitle = computed(() => {
  if (merchantLoadError.value) {
    return "商家信息接口异常，请点击第二行「重试」或检查后端是否运行";
  }
  return "";
});

const identityChipClass = computed(() => {
  if (isAdmin()) {
    return "chip-admin";
  }
  if (merchantLoadError.value) {
    return "chip-warn";
  }
  if (merchantMe.value?.applyStatus === "APPROVED") {
    return "chip-merchant";
  }
  if (merchantMe.value && merchantMe.value.applyStatus !== "APPROVED") {
    return "chip-pending";
  }
  return "chip-buyer";
});

function refreshSession() {
  tokenPresent.value = !!getToken();
  displayUserId.value = getUserId();
}

async function syncMerchantProfile() {
  merchantLoading.value = true;
  merchantLoadError.value = false;
  try {
    if (!getToken()) {
      merchantMe.value = null;
      setMerchantId(null);
      return;
    }
    const res = await fetchMerchantMe();
    const data = res?.data;
    if (data && data.merchantId != null) {
      merchantMe.value = {
        merchantId: data.merchantId,
        merchantName: data.merchantName,
        applyStatus: String(data.applyStatus ?? "")
          .trim()
          .toUpperCase()
      };
      setMerchantId(data.merchantId);
    } else {
      merchantMe.value = null;
      setMerchantId(null);
    }
  } catch {
    merchantMe.value = null;
    setMerchantId(null);
    merchantLoadError.value = true;
  } finally {
    merchantLoading.value = false;
  }
}

function onSessionChanged() {
  refreshSession();
  syncMerchantProfile();
}

function doLogout() {
  logout();
  refreshSession();
  merchantMe.value = null;
  merchantLoadError.value = false;
  router.push("/products");
}

function runSearch() {
  const q = searchKeyword.value.trim();
  router.push({ path: "/products", query: q ? { keyword: q } : {} });
}

watch(
  () => route.query.keyword,
  (k) => {
    searchKeyword.value = typeof k === "string" ? k : "";
  },
  { immediate: true }
);

onMounted(() => {
  refreshSession();
  syncMerchantProfile();
  window.addEventListener("mall-auth-changed", onSessionChanged);
  window.addEventListener("mall-merchant-profile-changed", syncMerchantProfile);
});

watch(
  () => route.fullPath,
  () => {
    if (getToken()) {
      syncMerchantProfile();
    }
  }
);

onUnmounted(() => {
  window.removeEventListener("mall-auth-changed", onSessionChanged);
  window.removeEventListener("mall-merchant-profile-changed", syncMerchantProfile);
});
</script>

<style scoped>
.mall-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
}

.mall-header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 16px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 16px;
}

.logo {
  font-size: 17px;
  font-weight: 700;
  color: #1677ff;
  text-decoration: none;
  white-space: nowrap;
}

.logo:hover {
  opacity: 0.85;
}

.search-wrap {
  flex: 1;
  min-width: 200px;
  max-width: 420px;
  display: flex;
  gap: 0;
  border: 1px solid #d9d9d9;
  border-radius: 20px;
  overflow: hidden;
  background: #fafafa;
}

.search-wrap input {
  flex: 1;
  border: none;
  padding: 8px 14px;
  font-size: 14px;
  background: transparent;
  outline: none;
}

.btn-search {
  padding: 8px 18px;
  border: none;
  background: #1677ff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.btn-search:hover {
  background: #4096ff;
}

.nav-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
  align-items: center;
}

.nav-links a {
  color: #333;
  text-decoration: none;
  font-size: 14px;
}

.nav-links a:hover {
  color: #1677ff;
}

.nav-links a.router-link-active {
  color: #1677ff;
  font-weight: 500;
}

.user-area {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #666;
  flex-wrap: wrap;
}

.identity-chip {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  white-space: nowrap;
}

.chip-admin {
  background: #fff7e6;
  color: #d46b08;
  border: 1px solid #ffd591;
}

.chip-merchant {
  background: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

.chip-buyer {
  background: #f0f5ff;
  color: #1d39c4;
  border: 1px solid #adc6ff;
}

.chip-pending {
  background: #fffbe6;
  color: #ad6800;
  border: 1px solid #ffe58f;
}

.chip-warn {
  background: #fff2f0;
  color: #cf1322;
  border: 1px solid #ffccc7;
}

.btn-admin-entry {
  font-size: 13px;
  color: #d46b08;
  text-decoration: none;
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid #ffd591;
  background: #fff7e6;
}

.btn-admin-entry:hover {
  background: #ffe7ba;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-text {
  background: none;
  border: none;
  color: #1677ff;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}

.btn-login {
  background: #1677ff;
  color: #fff !important;
  padding: 6px 16px;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
}

.merchant-bar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 6px 16px 10px;
  font-size: 12px;
  color: #888;
  border-top: 1px solid #f0f0f0;
}

.merchant-label {
  margin-right: 8px;
  color: #bbb;
}

.merchant-bar a {
  color: #666;
  text-decoration: none;
}

.merchant-bar a:hover {
  color: #1677ff;
}

.merchant-hint {
  color: #888;
  margin-right: 10px;
}

.merchant-hint.warn {
  color: #d4380d;
}

.merchant-action {
  color: #666;
  text-decoration: none;
}

.merchant-action:hover {
  color: #1677ff;
}

.sep {
  margin: 0 6px;
  color: #ddd;
}

.btn-retry {
  margin-left: 6px;
  padding: 2px 10px;
  font-size: 12px;
  border: 1px solid #1677ff;
  border-radius: 4px;
  background: #fff;
  color: #1677ff;
  cursor: pointer;
}

.btn-retry:hover {
  background: #e6f4ff;
}

.mall-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 16px;
  box-sizing: border-box;
}
</style>
