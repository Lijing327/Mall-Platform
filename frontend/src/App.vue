<template>
  <div>
    <header class="header">
      <div class="container nav">
        <h2>平台商城 MVP</h2>
        <div class="links">
          <RouterLink to="/products">商品列表</RouterLink>
          <RouterLink to="/cart">购物车</RouterLink>
          <RouterLink to="/checkout">提交订单</RouterLink>
          <RouterLink to="/orders">我的订单</RouterLink>
          <RouterLink to="/merchant/apply">商家入驻</RouterLink>
          <RouterLink to="/merchant/products">商品管理</RouterLink>
          <RouterLink to="/merchant/orders">店铺订单</RouterLink>
          <RouterLink to="/admin/merchants">商家审核</RouterLink>
          <RouterLink to="/admin/orders">订单列表</RouterLink>
          <RouterLink to="/admin/products">商品列表</RouterLink>
        </div>
      </div>
    </header>
    <main class="container main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from "vue-router";
</script>

<style scoped>
.header {
  background: #fff;
  border-bottom: 1px solid #eee;
}

.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.links {
  display: flex;
  gap: 16px;
}

.main {
  padding: 20px 0;
}
</style>
