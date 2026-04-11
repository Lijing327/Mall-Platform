# 商城前端开发手册（用户端 + 商家端 + 管理端）

## 1. 说明

该模块为 Vue3 Web 前端，已接入后端真实接口，当前采用统一后台布局（左侧菜单 + 顶栏）承载三端页面。
目标是打通平台商城 MVP 主流程：

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

## 2. 技术栈

- Vue 3
- Vite
- Vue Router
- Axios

## 3. 启动方式

1. 先启动后端（默认 `http://localhost:8080`）
2. 在 `frontend` 目录执行：

```bash
npm install
npm run dev
```

3. 浏览器打开：

`http://localhost:5173`

## 4. 页面路由清单

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

## 5. 前后端接口映射（核心）

### 5.1 用户端

- 商品列表：`GET /api/products`
- 商品详情：`GET /api/products/{id}`
- 加购：`POST /api/cart/add`
- 购物车：`GET /api/cart/list`
- 创建订单：`POST /api/orders/create`
- 模拟支付：`POST /api/orders/pay`

### 5.2 商家端

- 入驻申请：`POST /api/merchant/apply`
- 商品管理：`/api/merchant/products`（增删改查、上下架）
- 店铺订单：`GET /api/merchant/orders`、`GET /api/merchant/orders/{id}`
- 发货：`POST /api/merchant/orders/{id}/ship`

### 5.3 管理端

- 商家申请列表（分页）：`GET /api/admin/merchants`
- 商家审核：`POST /api/admin/merchants/audit`
- 订单列表：`GET /api/admin/orders`
- 商品列表：`GET /api/admin/products`
- 管理员下架：`POST /api/admin/products/{id}/off-shelf`

**路径约定**：管理端统一前缀 `/api/admin`，资源名用复数（`merchants`、`orders`、`products`）；子动作为 `POST /api/admin/{资源}/{动作}`。旧路径 `GET /api/admin/merchant/list`、`POST /api/admin/merchant/audit` 已废弃，请勿再使用。

## 6. 默认测试身份

- 当前用户使用本地 `localStorage` 的 `mall_mvp_user_id`（默认 `1001`）
- 当前商家使用本地 `localStorage` 的 `mall_mvp_merchant_id`（默认 `1`）

## 7. 联调验证流程（推荐）

1. 用户端：商品浏览 -> 加购 -> 下单 -> 支付 -> 我的订单
2. 商家端：入驻申请 -> 管理员审核通过 -> 商品新增上架 -> 查看店铺订单 -> 发货
3. 管理端：查看商家/订单/商品全量列表并执行审核/下架

## 8. 常见问题排查

### 8.1 页面白屏 / Vite 报 `Element is missing end tag`

**原因**：Vue 模板标签未闭合。

**处理**：

1. 打开报错文件（通常会定位到 `App.vue` 或某个页面）
2. 检查 `template` 区域是否有多余或缺失的闭合标签
3. 保存后观察 Vite 是否自动恢复编译

### 8.2 接口 404 / 无数据

1. 确认后端已启动且端口为 `8080`
2. 确认 `vite.config.js` 代理配置为 `/api -> http://localhost:8080`
3. 在浏览器 Network 面板检查实际请求路径

### 8.3 “我的订单”数据与预期不一致

当前后端暂无用户专用订单列表接口，前端暂时通过管理员订单接口按 `userId` 过滤展示，属于 MVP 过渡方案。

## 9. 开发约定

- 页面风格务实，优先流程可用，不做复杂动画
- 页面数据返回统一走后端 `Result` 结构
- 模块化组织：`api/`、`views/`、`router/`、`utils/`
