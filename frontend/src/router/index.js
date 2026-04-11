import { createRouter, createWebHistory } from "vue-router";
import MallLayout from "../layouts/MallLayout.vue";
import EmptyLayout from "../layouts/EmptyLayout.vue";
import AdminLayout from "../layouts/AdminLayout.vue";

import ProductListPage from "../views/mall/ProductListPage.vue";
import ProductDetailPage from "../views/mall/ProductDetailPage.vue";
import CartPage from "../views/mall/CartPage.vue";
import CheckoutPage from "../views/mall/CheckoutPage.vue";
import MyOrdersPage from "../views/mall/MyOrdersPage.vue";
import MyAddressPage from "../views/mall/MyAddressPage.vue";
import LoginPage from "../views/mall/LoginPage.vue";

import MerchantApplyPage from "../views/merchant/MerchantApplyPage.vue";
import MerchantProductManagePage from "../views/merchant/MerchantProductManagePage.vue";
import MerchantProductEditPage from "../views/merchant/MerchantProductEditPage.vue";
import MerchantOrderPage from "../views/merchant/MerchantOrderPage.vue";

import AdminMerchantAuditPage from "../views/admin/AdminMerchantAuditPage.vue";
import AdminOrderListPage from "../views/admin/AdminOrderListPage.vue";
import AdminProductListPage from "../views/admin/AdminProductListPage.vue";
import AdminDashboardPage from "../views/admin/AdminDashboardPage.vue";
import AdminLoginPage from "../views/admin/AdminLoginPage.vue";
import AdminOrderDetailPage from "../views/admin/AdminOrderDetailPage.vue";
import AdminUsersPage from "../views/admin/AdminUsersPage.vue";

import { getToken, isAdmin } from "../utils/user-context";

const routes = [
  {
    path: "/",
    component: MallLayout,
    children: [
      { path: "", redirect: "/products" },
      { path: "products", component: ProductListPage, meta: { public: true } },
      { path: "products/:id", component: ProductDetailPage, meta: { public: true } },
      { path: "cart", component: CartPage, meta: { requiresAuth: true } },
      { path: "checkout", component: CheckoutPage, meta: { requiresAuth: true } },
      { path: "orders", component: MyOrdersPage, meta: { requiresAuth: true } },
      { path: "addresses", component: MyAddressPage, meta: { requiresAuth: true } },
      { path: "merchant/apply", component: MerchantApplyPage, meta: { requiresAuth: true } },
      { path: "merchant/products", component: MerchantProductManagePage, meta: { requiresAuth: true } },
      { path: "merchant/products/new", component: MerchantProductEditPage, meta: { requiresAuth: true } },
      { path: "merchant/products/:id/edit", component: MerchantProductEditPage, meta: { requiresAuth: true } },
      { path: "merchant/orders", component: MerchantOrderPage, meta: { requiresAuth: true } }
    ]
  },
  {
    path: "/login",
    component: EmptyLayout,
    children: [{ path: "", component: LoginPage, meta: { public: true } }]
  },
  {
    path: "/admin/login",
    component: EmptyLayout,
    children: [{ path: "", component: AdminLoginPage, meta: { public: true } }]
  },
  {
    path: "/admin",
    component: AdminLayout,
    redirect: "/admin/dashboard",
    children: [
      { path: "dashboard", component: AdminDashboardPage, meta: { requiresAuth: true, requiresAdmin: true } },
      { path: "products", component: AdminProductListPage, meta: { requiresAuth: true, requiresAdmin: true } },
      { path: "orders", component: AdminOrderListPage, meta: { requiresAuth: true, requiresAdmin: true } },
      { path: "orders/:id", component: AdminOrderDetailPage, meta: { requiresAuth: true, requiresAdmin: true } },
      { path: "users", component: AdminUsersPage, meta: { requiresAuth: true, requiresAdmin: true } },
      { path: "merchants", component: AdminMerchantAuditPage, meta: { requiresAuth: true, requiresAdmin: true } }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (to.meta.public) {
    return true;
  }
  if (to.meta.requiresAuth && !getToken()) {
    const needAdminLogin = to.path.startsWith("/admin") && to.path !== "/admin/login";
    if (needAdminLogin) {
      return { path: "/admin/login", query: { redirect: to.fullPath } };
    }
    return { path: "/login", query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresAdmin && !isAdmin()) {
    return { path: "/products", query: { needAdmin: "1" } };
  }
  return true;
});

export default router;
