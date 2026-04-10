import { createRouter, createWebHistory } from "vue-router";
import ProductListPage from "../views/ProductListPage.vue";
import ProductDetailPage from "../views/ProductDetailPage.vue";
import CartPage from "../views/CartPage.vue";
import CheckoutPage from "../views/CheckoutPage.vue";
import MyOrdersPage from "../views/MyOrdersPage.vue";
import MerchantApplyPage from "../views/merchant/MerchantApplyPage.vue";
import MerchantProductManagePage from "../views/merchant/MerchantProductManagePage.vue";
import MerchantProductEditPage from "../views/merchant/MerchantProductEditPage.vue";
import MerchantOrderPage from "../views/merchant/MerchantOrderPage.vue";
import AdminMerchantAuditPage from "../views/admin/AdminMerchantAuditPage.vue";
import AdminOrderListPage from "../views/admin/AdminOrderListPage.vue";
import AdminProductListPage from "../views/admin/AdminProductListPage.vue";

const routes = [
  { path: "/", redirect: "/products" },
  { path: "/products", component: ProductListPage },
  { path: "/products/:id", component: ProductDetailPage },
  { path: "/cart", component: CartPage },
  { path: "/checkout", component: CheckoutPage },
  { path: "/orders", component: MyOrdersPage },
  { path: "/merchant/apply", component: MerchantApplyPage },
  { path: "/merchant/products", component: MerchantProductManagePage },
  { path: "/merchant/products/new", component: MerchantProductEditPage },
  { path: "/merchant/products/:id/edit", component: MerchantProductEditPage },
  { path: "/merchant/orders", component: MerchantOrderPage },
  { path: "/admin/merchants", component: AdminMerchantAuditPage },
  { path: "/admin/orders", component: AdminOrderListPage },
  { path: "/admin/products", component: AdminProductListPage }
];

export default createRouter({
  history: createWebHistory(),
  routes
});
