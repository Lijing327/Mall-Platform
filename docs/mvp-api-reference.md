# 自营商城 MVP 1.0 接口清单

本文档按**用户端**、**后台管理端**分类，描述一期（自营商城 MVP 1.0）正式对外的 HTTP 接口。
商家端与平台治理相关接口作为结构预留，集中放在文末"平台化预留"章节，默认不在一期演示路径中使用。

---

## 通用约定

**Base URL**：以实际部署为准，下文路径均指服务端上下文根之后部分（如 `/api/...`）。

**统一响应结构**（`application/json`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | number | `0` 表示成功；非 `0` 为失败（如 `400`、`401`、`403`、`404`） |
| `message` | string | 提示信息 |
| `data` | object / array / null | 业务数据，失败时多为 `null` |

**鉴权**：

- 请求头：`Authorization: Bearer <token>`（登录接口返回的 `token`）。
- **无需令牌**：`POST /api/auth/login`、`GET /api/health`、`GET /api/health/**`、`GET /api/products`、`GET /api/products/**`。
- **需要令牌**：其余 `/api/**`。
- **管理端**：路径以 `/api/admin` 开头时，令牌对应用户须在配置 `mall.auth.admin-user-ids` 中，否则 `403`。

**说明**：部分接口 Body 可带 `userId`；若携带，须与令牌中的用户一致，否则 `403`。

**特性开关**（`application.yml` → `mall.feature`，默认均为 `false`，不改变一期主流程）：

| 配置项 | 说明 |
|--------|------|
| `multi-shop` | 多店铺运营相关（平台化预留） |
| `split-order` | 以子订单表为主拆单展示/流转（平台化预留） |
| `settlement` | 分账/结算（平台化预留） |

代码中请通过 `FeatureFlags` Bean 读取，勿散落硬编码。

---

## 一、用户端

面向消费者：浏览商品、登录、地址管理、购物车、订单。

### 1.1 健康检查

| 项目 | 内容 |
|------|------|
| **URL** | `/api/health` |
| **方法** | `GET` |
| **请求参数** | 无 |
| **示例请求** | `GET /api/health` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "serviceName": "mall-platform-backend", "status": "UP", "currentTime": "2026-04-11T12:00:00.123" } }` |

---

### 1.2 登录

| 项目 | 内容 |
|------|------|
| **URL** | `/api/auth/login` |
| **方法** | `POST` |
| **请求参数** | Body JSON：`userId`（必填，long）、`password`（必填，与配置 `mall.auth.login-password` 一致） |
| **示例请求** | `POST /api/auth/login` Body: `{ "userId": 1001, "password": "your-config-password" }` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "token": "eyJ...", "userId": 1001, "role": "USER" } }`（管理员账号为 `"role": "ADMIN"`） |

---

### 1.3 商品分页列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/products` |
| **方法** | `GET` |
| **请求参数** | Query：`pageNum`（默认 1）、`pageSize`（默认 10）、`keyword`（可选）、`shopId`（可选） |
| **示例请求** | `GET /api/products?pageNum=1&pageSize=10&keyword=键盘` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "total": 2, "pageNum": 1, "pageSize": 10, "list": [ { "id": 102, "productName": "自营｜机械键盘 K8", "productSubtitle": "…", "mainImage": "https://…", "price": 299.00, "stock": 200, "shop": { "shopId": 1, "shopName": "平台自营旗舰店", "shopType": "SELF" } } ] } }` |

---

### 1.4 商品详情

| 项目 | 内容 |
|------|------|
| **URL** | `/api/products/{id}` |
| **方法** | `GET` |
| **请求参数** | Path：`id` 商品主键 |
| **示例请求** | `GET /api/products/102` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "id": 102, "productName": "自营｜机械键盘 K8", "detail": "<p>…</p>", "price": 299.00, "stock": 200, "shop": { "shopId": 1, "shopName": "平台自营旗舰店", "shopType": "SELF" } } }` |

---

### 1.5 收货地址列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/user/addresses` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": [ { "id": 10, "userId": 1001, "receiverName": "李买家", "receiverMobile": "13900001001", "province": "上海市", "city": "上海市", "district": "浦东新区", "detailAddress": "演示路 1 号", "isDefault": true, "createTime": "...", "updateTime": "..." } ] }` |

---

### 1.6 默认收货地址

| 项目 | 内容 |
|------|------|
| **URL** | `/api/user/addresses/default` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization` |
| **示例返回** | 同单条地址；无默认时 `data: null` |

---

### 1.7 新增收货地址

| 项目 | 内容 |
|------|------|
| **URL** | `/api/user/addresses` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`receiverName`、`receiverMobile`、`province`、`city`、`district`、`detailAddress`（均必填）、`isDefault`（可选 boolean） |
| **示例请求** | `POST /api/user/addresses` Body: `{ "receiverName": "李买家", "receiverMobile": "13900001001", "province": "上海市", "city": "上海市", "district": "浦东新区", "detailAddress": "演示路 1 号", "isDefault": true }` |
| **示例返回** | 新增地址详情；若为首条地址会自动标记为默认 |

---

### 1.8 修改收货地址

| 项目 | 内容 |
|------|------|
| **URL** | `/api/user/addresses/{id}` |
| **方法** | `PUT` |
| **请求参数** | Header：`Authorization`。Path：`id`。Body 同新增；`isDefault=true` 时会切换默认位 |
| **示例返回** | 更新后地址详情 |

---

### 1.9 删除收货地址

| 项目 | 内容 |
|------|------|
| **URL** | `/api/user/addresses/{id}` |
| **方法** | `DELETE` |
| **说明** | 软删；若删除的是默认地址，剩余最近一条地址自动成为新默认 |
| **示例返回** | `{ "code": 0, "message": "删除成功", "data": "OK" }` |

---

### 1.10 设为默认收货地址

| 项目 | 内容 |
|------|------|
| **URL** | `/api/user/addresses/{id}/default` |
| **方法** | `POST` |
| **示例返回** | 新的默认地址详情 |

---

### 1.11 加入购物车

| 项目 | 内容 |
|------|------|
| **URL** | `/api/cart/add` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`productId`（必填）、`quantity`（必填，≥1） |
| **示例请求** | `POST /api/cart/add` Body: `{ "productId": 102, "quantity": 1 }` |
| **示例返回** | `{ "code": 0, "message": "加入购物车成功", "data": "OK" }` |

---

### 1.12 修改购物车数量

| 项目 | 内容 |
|------|------|
| **URL** | `/api/cart/update` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`cartId`（必填）、`quantity`（必填，≥1） |
| **说明** | 服务端校验归属、商品有效性和库存 |
| **示例返回** | `{ "code": 0, "message": "修改成功", "data": "OK" }` |

---

### 1.13 删除购物车项

| 项目 | 内容 |
|------|------|
| **URL** | `/api/cart/delete` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`cartId`（必填） |
| **示例返回** | `{ "code": 0, "message": "删除成功", "data": "OK" }` |

---

### 1.14 购物车列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/cart/list` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": [ { "cartId": 1, "productId": 102, "productName": "自营｜机械键盘 K8", "productImage": "https://…", "productPrice": 299.00, "quantity": 1, "shopId": 1, "shopName": "平台自营旗舰店", "shopType": "SELF", "invalid": false } ] }` |
| **说明** | `invalid=true` 表示商品已下架或被删除，前端应置灰，结算链路会自动跳过 |

---

### 1.15 创建订单

| 项目 | 内容 |
|------|------|
| **URL** | `/api/orders/create` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：**`addressId`（必填）**、`remark`（可选） |
| **说明** | 服务端基于当前用户购物车中 **`checked=true`** 的行生成订单，扣库存并清理购物车；同时将地址簿中所选地址的收货人 / 手机 / 地址快照到订单 |
| **示例请求** | `POST /api/orders/create` Body: `{ "addressId": 10, "remark": "不用电话联系" }` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "orderId": 501, "orderNo": "202604111200001001234567", "orderStatus": "PENDING_PAYMENT", "totalAmount": 299.00 } }` |

---

### 1.16 模拟支付

| 项目 | 内容 |
|------|------|
| **URL** | `/api/orders/pay` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`orderNo`（必填） |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "orderNo": "…", "orderStatus": "PAID", "payType": "MOCK", "payTime": "..." } }` |

---

### 1.17 我的订单列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/orders/my` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization` |
| **示例返回** | 每条订单包含 `receiverName / receiverMobile / receiverAddress` 快照、`items`、`shopOrders`（单店默认派生一条） |

---

### 1.18 确认收货

| 项目 | 内容 |
|------|------|
| **URL** | `/api/orders/{id}/confirm-receive` |
| **方法** | `POST` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "orderId": 501, "orderStatus": "COMPLETED", "completeTime": "..." } }` |

---

## 二、后台管理端

均需 **`Authorization`**，且用户须在 **`mall.auth.admin-user-ids`** 中（登录返回 `role=ADMIN`）。
一期主要聚焦订单管理与商品管理。发货动作一期复用 `/api/merchant/orders/{id}/ship` 主订单发货接口。

### 2.1 订单分页列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/admin/orders` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Query：`pageNum`、`pageSize`、`orderStatus`（可选）、`orderNo`（可选，模糊） |
| **示例请求** | `GET /api/admin/orders?pageNum=1&pageSize=10&orderStatus=PAID` |

---

### 2.2 商品分页列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/admin/products` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Query：`pageNum`、`pageSize`、`keyword`（可选）、`shopId`（可选）、`saleStatus`（可选） |

---

### 2.3 管理员下架商品

| 项目 | 内容 |
|------|------|
| **URL** | `/api/admin/products/{id}/off-shelf` |
| **方法** | `POST` |

---

### 2.4 订单发货（复用主订单发货接口）

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/orders/{id}/ship` |
| **方法** | `POST` |
| **请求参数** | Body JSON：`shippingNo`（必填）、`shippingRemark`（可选） |
| **说明** | 一期仅占位物流字段；一期管理员账号可直接调用以完成订单流转 |

---

## 三、平台化预留（一期不在演示路径中）

以下接口与数据表均作为未来升级到平台型商城的结构预留，一期**默认不使用**，前端菜单已隐藏但路由保留。

### 3.1 商家端接口（预留）

- `POST /api/merchant/apply` — 商家入驻申请
- `GET / POST / PUT / DELETE /api/merchant/products` 等 — 商家商品管理
- `POST /api/merchant/products/{id}/on-shelf` / `off-shelf`
- `GET /api/merchant/orders` / `GET /api/merchant/orders/{id}`
- `GET /api/merchant/shop-orders`、`POST /api/merchant/shop-orders/{id}/ship`（未接入）

### 3.2 平台治理接口（预留）

- `GET /api/admin/merchants`、`POST /api/admin/merchants/audit` — 商家入驻审核

### 3.3 预留数据表

- `dbo.merchant`、`dbo.shop`（`shop_type=MERCHANT`）、`dbo.shop_order`
- 对应特性开关：`mall.feature.multi-shop / split-order / settlement`

---

## 附录：与代码同步

- 接口以 `backend/src/main/java/com/mall/platform/controller` 包下类为准；若代码变更，请同步更新本文档。
- 订单状态、商品上下架等枚举含义见业务代码 `com.mall.platform.enums` 与 `docs/mvp-acceptance-checklist.md`。
