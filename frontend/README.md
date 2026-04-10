# 商城用户端（第九阶段A）

## 1. 说明

该模块为 Vue3 用户端基础页面，已接入当前后端真实接口，目标是先打通 MVP 主流程：

- 商品列表
- 商品详情
- 购物车
- 提交订单
- 模拟支付
- 我的订单
- 商家入驻申请
- 商家商品管理
- 商家店铺订单发货
- 管理员商家审核
- 管理员订单列表
- 管理员商品列表

## 2. 启动方式

1. 先启动后端（默认 `http://localhost:8080`）
2. 在 `frontend` 目录执行：

```bash
npm install
npm run dev
```

3. 浏览器打开：

`http://localhost:5173`

## 3. 当前页面

- `/products` 商品列表页（分页 + 关键字搜索）
- `/products/:id` 商品详情页（可加入购物车）
- `/cart` 购物车页
- `/checkout` 提交订单页（创建订单 + 模拟支付）
- `/orders` 我的订单页
- `/merchant/apply` 商家入驻申请页
- `/merchant/products` 商家商品管理页
- `/merchant/products/new` 商家商品新增页
- `/merchant/products/:id/edit` 商家商品编辑页
- `/merchant/orders` 商家店铺订单页
- `/admin/merchants` 管理员商家审核页
- `/admin/orders` 管理员订单列表页
- `/admin/products` 管理员商品列表页

## 4. 注意事项

- 当前用户使用本地 `localStorage` 的 `mall_mvp_user_id`（默认 `1001`）
- 当前商家使用本地 `localStorage` 的 `mall_mvp_merchant_id`（默认 `1`）
- Vite 已配置 `/api` 代理到 `http://localhost:8080`
- 当前后端暂无 `/api/orders/my`，我的订单页暂时使用 `/api/admin/orders` 后按 `userId` 过滤
