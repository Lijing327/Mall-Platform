<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <div class="admin-brand-mark">MP</div>
        <div>
          <strong>商城后台</strong>
          <p>运营管理中心</p>
        </div>
      </div>

      <nav class="admin-nav">
        <RouterLink
          v-for="item in adminNavigation"
          :key="item.key"
          class="admin-nav-item"
          :to="item.to"
        >
          <span class="admin-nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <section class="admin-main">
      <header class="admin-topbar">
        <div>
          <p class="admin-topbar-kicker">管理端</p>
          <h2>{{ pageMeta.title }}</h2>
          <p class="admin-topbar-subtitle">{{ pageMeta.subtitle }}</p>
        </div>
        <div class="admin-topbar-actions">
          <div class="admin-admin-chip">
            <span class="admin-admin-chip__label">管理员</span>
            <strong>{{ adminName }}</strong>
            <small v-if="tokenPresent">ID {{ displayUserId || "—" }} · {{ userRoleZh(displayRole) }}</small>
          </div>
          <button type="button" class="btn admin-ghost-btn" @click="doLogout">退出登录</button>
        </div>
      </header>

      <main class="admin-content">
        <RouterView />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { getAdminPageMeta, adminNavigation } from "../utils/admin-ui";
import { userRoleZh } from "../utils/display-labels";
import { getRole, getToken, getUserId, logout } from "../utils/user-context";

const route = useRoute();
const router = useRouter();

const tokenPresent = ref(!!getToken());
const displayUserId = ref(getUserId());
const displayRole = ref(getRole());

const pageMeta = computed(() => getAdminPageMeta(route.path));
const adminName = computed(() => (tokenPresent.value ? "admin" : "未登录"));

function refreshHeader() {
  tokenPresent.value = !!getToken();
  displayUserId.value = getUserId();
  displayRole.value = getRole();
}

function doLogout() {
  logout();
  refreshHeader();
  router.push("/admin/login");
}

onMounted(() => {
  refreshHeader();
  window.addEventListener("mall-auth-changed", refreshHeader);
});

onUnmounted(() => {
  window.removeEventListener("mall-auth-changed", refreshHeader);
});
</script>
