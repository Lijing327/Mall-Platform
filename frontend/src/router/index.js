import { createRouter, createWebHistory } from "vue-router";
import ProductListPage from "../views/ProductListPage.vue";
import ProductDetailPage from "../views/ProductDetailPage.vue";
import CartPage from "../views/CartPage.vue";
import CheckoutPage from "../views/CheckoutPage.vue";
import MyOrdersPage from "../views/MyOrdersPage.vue";
import LoginPage from "../views/LoginPage.vue";
import MerchantApplyPage from "../views/merchant/MerchantApplyPage.vue";
import MerchantProductManagePage from "../views/merchant/MerchantProductManagePage.vue";
import MerchantProductEditPage from "../views/merchant/MerchantProductEditPage.vue";
import MerchantOrderPage from "../views/merchant/MerchantOrderPage.vue";
import AdminMerchantAuditPage from "../views/admin/AdminMerchantAuditPage.vue";
import AdminOrderListPage from "../views/admin/AdminOrderListPage.vue";
import AdminProductListPage from "../views/admin/AdminProductListPage.vue";
import { getToken, isAdmin } from "../utils/user-context";

const routes = [
  { path: "/", redirect: "/products" },
  { path: "/products", component: ProductListPage, meta: { public: true } },
  { path: "/products/:id", component: ProductDetailPage, meta: { public: true } },
  { path: "/login", component: LoginPage, meta: { public: true } },
  { path: "/cart", component: CartPage, meta: { requiresAuth: true } },
  { path: "/checkout", component: CheckoutPage, meta: { requiresAuth: true } },
  { path: "/orders", component: MyOrdersPage, meta: { requiresAuth: true } },
  { path: "/merchant/apply", component: MerchantApplyPage, meta: { requiresAuth: true } },
  { path: "/merchant/products", component: MerchantProductManagePage, meta: { requiresAuth: true } },
  { path: "/merchant/products/new", component: MerchantProductEditPage, meta: { requiresAuth: true } },
  { path: "/merchant/products/:id/edit", component: MerchantProductEditPage, meta: { requiresAuth: true } },
  { path: "/merchant/orders", component: MerchantOrderPage, meta: { requiresAuth: true } },
  { path: "/admin/merchants", component: AdminMerchantAuditPage, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: "/admin/orders", component: AdminOrderListPage, meta: { requiresAuth: true, requiresAdmin: true } },
  { path: "/admin/products", component: AdminProductListPage, meta: { requiresAuth: true, requiresAdmin: true } }
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
    return { path: "/login", query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresAdmin && !isAdmin()) {
    return { path: "/products", query: { needAdmin: "1" } };
  }
  return true;
});

export default router;
