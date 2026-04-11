<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">自营商城 MVP 1.0</div>

      <div class="menu-group">
        <div class="group-title">用户端</div>
        <RouterLink class="menu-item" to="/products">商品列表</RouterLink>
        <RouterLink class="menu-item" to="/cart">购物车</RouterLink>
        <RouterLink class="menu-item" to="/checkout">提交订单</RouterLink>
        <RouterLink class="menu-item" to="/orders">我的订单</RouterLink>
        <RouterLink class="menu-item" to="/addresses">我的地址</RouterLink>
      </div>

      <div class="menu-group">
        <div class="group-title">后台管理</div>
        <RouterLink class="menu-item" to="/admin/orders">订单列表</RouterLink>
        <RouterLink class="menu-item" to="/admin/products">商品列表</RouterLink>
      </div>
    </aside>

    <section class="content">
      <header class="topbar">
        <div class="title">{{ pageTitle }}</div>
        <div class="top-actions">
          <div class="meta">
            <span v-if="tokenPresent">用户ID={{ displayUserId }} / 角色={{ userRoleZh(displayRole) }}</span>
            <span v-if="tokenPresent && displayMerchantId"> / 商家ID={{ displayMerchantId }}</span>
            <span v-else-if="!tokenPresent">未登录</span>
          </div>
          <RouterLink v-if="!tokenPresent" class="btn-link" to="/login">登录</RouterLink>
          <button v-else type="button" class="btn-link" @click="doLogout">退出</button>
        </div>
      </header>
      <main class="page-body">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { userRoleZh } from "./utils/display-labels";
import { getMerchantId, getRole, getToken, getUserId, logout } from "./utils/user-context";

const route = useRoute();
const router = useRouter();

const tokenPresent = ref(!!getToken());
const displayUserId = ref(getUserId());
const displayRole = ref(getRole());
const displayMerchantId = ref(getMerchantId());

function refreshHeader() {
  tokenPresent.value = !!getToken();
  displayUserId.value = getUserId();
  displayRole.value = getRole();
  displayMerchantId.value = getMerchantId();
}

function doLogout() {
  logout();
  refreshHeader();
  router.push("/products");
}

const titleMap = {
  "/products": "商品列表",
  "/cart": "购物车",
  "/checkout": "提交订单",
  "/orders": "我的订单",
  "/addresses": "我的地址",
  "/login": "登录",
  "/merchant/apply": "商家入驻申请",
  "/merchant/products": "商家商品管理",
  "/merchant/orders": "店铺订单",
  "/admin/merchants": "商家审核",
  "/admin/orders": "订单列表",
  "/admin/products": "商品列表"
};

const pageTitle = computed(() => {
  if (route.path.startsWith("/merchant/products/") && route.path.endsWith("/edit")) {
    return "编辑商品";
  }
  if (route.path === "/merchant/products/new") {
    return "新增商品";
  }
  if (route.path.startsWith("/products/")) {
    return "商品详情";
  }
  return titleMap[route.path] || "自营商城";
});

onMounted(() => {
  refreshHeader();
  window.addEventListener("mall-auth-changed", refreshHeader);
});

onUnmounted(() => {
  window.removeEventListener("mall-auth-changed", refreshHeader);
});
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  background: #f5f6fa;
}

.sidebar {
  width: 220px;
  background: #001529;
  color: #fff;
  padding: 16px 12px;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 16px;
}

.menu-group {
  margin-bottom: 18px;
}

.group-title {
  font-size: 12px;
  color: #96a2b8;
  margin-bottom: 8px;
}

.menu-item {
  display: block;
  color: #d9e1f2;
  padding: 8px 10px;
  border-radius: 6px;
  margin-bottom: 4px;
}

.menu-item.router-link-active {
  background: #1677ff;
  color: #fff;
}

.content {
  flex: 1;
  min-width: 0;
}

.topbar {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
}

.title {
  font-weight: 600;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta {
  color: #667085;
  font-size: 13px;
}

.btn-link {
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 13px;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
}

.page-body {
  padding: 16px;
}
</style>
