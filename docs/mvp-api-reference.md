# Mall Platform MVP 接口清单

本文档按**用户端**、**商家端**、**管理端**分类，描述当前后端暴露的 HTTP 接口。格式力求简单，非 Swagger。

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

**特性开关**（`application.yml` → `mall.feature`，默认均为 `false`，不改变现有主流程）：

| 配置项 | 说明 |
|--------|------|
| `multi-shop` | 多店铺运营相关（预留） |
| `split-order` | 以子订单表为主拆单展示/流转（预留；为 `true` 且无 `shop_order` 数据时，「我的订单」中 `shopOrders` 可按店铺汇总行展示） |
| `settlement` | 分账/结算（预留） |

代码中请通过 `FeatureFlags` Bean 读取，勿散落硬编码。

---

## 一、用户端

面向消费者：浏览商品、登录、购物车、订单。

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
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "total": 2, "pageNum": 1, "pageSize": 10, "list": [ { "id": 102, "productName": "商家｜机械键盘 K8", "productSubtitle": "…", "mainImage": "https://…", "price": 299.00, "stock": 200, "shop": { "shopId": 2, "shopName": "演示商家有限公司店铺", "shopType": "MERCHANT" } } ] } }` |

---

### 1.4 商品详情

| 项目 | 内容 |
|------|------|
| **URL** | `/api/products/{id}` |
| **方法** | `GET` |
| **请求参数** | Path：`id` 商品主键 |
| **示例请求** | `GET /api/products/102` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "id": 102, "productSn": "P2MCH20260115001", "productName": "商家｜机械键盘 K8", "productSubtitle": "…", "mainImage": "https://…", "detail": "<p>…</p>", "price": 299.00, "stock": 200, "shop": { "shopId": 2, "shopName": "演示商家有限公司店铺", "shopType": "MERCHANT" } } }` |

---

### 1.5 加入购物车

| 项目 | 内容 |
|------|------|
| **URL** | `/api/cart/add` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`productId`（必填）、`quantity`（必填，≥1）；`userId`（可选，须与 token 一致） |
| **示例请求** | `POST /api/cart/add` Header: `Authorization: Bearer <token>` Body: `{ "productId": 102, "quantity": 1 }` |
| **示例返回** | `{ "code": 0, "message": "加入购物车成功", "data": "OK" }` |

---

### 1.6 购物车列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/cart/list` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`（用户身份由 token 解析） |
| **示例请求** | `GET /api/cart/list` Header: `Authorization: Bearer <token>` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": [ { "cartId": 1, "productId": 102, "productName": "商家｜机械键盘 K8", "productImage": "https://…", "productPrice": 299.00, "quantity": 1, "shopId": 2, "shopName": "演示商家有限公司店铺", "shopType": "MERCHANT" } ] }` |

---

### 1.7 创建订单

| 项目 | 内容 |
|------|------|
| **URL** | `/api/orders/create` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`userId`（可选，须与 token 一致）；仅基于当前用户购物车中 **`checked=true`** 的行生成订单并扣库存、删除对应购物车行 |
| **示例请求** | `POST /api/orders/create` Header: `Authorization: Bearer <token>` Body: `{ "userId": 1001 }` 或 `{}` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "orderId": 501, "orderNo": "202604111200001001234567", "orderStatus": "PENDING_PAYMENT", "totalAmount": 299.00 } }` |

---

### 1.8 模拟支付

| 项目 | 内容 |
|------|------|
| **URL** | `/api/orders/pay` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`orderNo`（必填）、`userId`（可选，须与 token 一致） |
| **示例请求** | `POST /api/orders/pay` Header: `Authorization: Bearer <token>` Body: `{ "orderNo": "202604111200001001234567" }` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "orderNo": "202604111200001001234567", "orderStatus": "PAID", "payType": "MOCK", "payTime": "2026-04-11T12:05:00" } }` |

---

### 1.9 我的订单列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/orders/my` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization` |
| **示例请求** | `GET /api/orders/my` Header: `Authorization: Bearer <token>` |
| **示例返回** | 在原有订单字段与 `items` 基础上，每条订单增加 **`shopOrders`**（子订单/按店维度；默认 `split-order=false` 时为一条与主单对齐的派生数据，`id` 可为 `null`）：`{ "code": 0, "message": "成功", "data": [ { "orderId": 501, "orderNo": "…", "orderStatus": "PAID", "items": [ … ], "shopOrders": [ { "id": null, "orderId": 501, "shopId": 2, "shopName": "演示商家有限公司店铺", "amount": 299.00, "status": "PAID", "shippingNo": null, "shipTime": null, "completeTime": null } ] } ] }`（字段以实际 JSON 为准） |

---

### 1.10 确认收货

| 项目 | 内容 |
|------|------|
| **URL** | `/api/orders/{id}/confirm-receive` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Path：`id` 为订单主键 |
| **示例请求** | `POST /api/orders/501/confirm-receive` Header: `Authorization: Bearer <token>` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "orderId": 501, "orderNo": "202604111200001001234567", "orderStatus": "COMPLETED", "completeTime": "2026-04-11T14:00:00" } }` |

---

## 二、商家端

入驻申请、商品管理、订单发货。均需 **`Authorization`**；操作商品/订单时，token 用户须为**已审核通过**的商家对应账号。

### 2.1 提交商家入驻申请

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/apply` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`merchantName`、`contactName`、`contactMobile`（均必填）；`qualificationText`（可选）；`userId`（可选，须与 token 一致） |
| **示例请求** | `POST /api/merchant/apply` Header: `Authorization: Bearer <token>` Body: `{ "merchantName": "测试公司", "contactName": "张三", "contactMobile": "13800000000", "qualificationText": "说明文字" }` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "merchantId": 10, "merchantCode": "M2026041112001234", "applyStatus": "PENDING", "applyTime": "2026-04-11T12:00:00" } }` |

---

### 2.2 商家商品分页列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/products` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Query：`pageNum`（默认 1）、`pageSize`（默认 10）、`merchantId`（可选）、`keyword`（可选） |
| **示例请求** | `GET /api/merchant/products?pageNum=1&pageSize=10` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "total": 1, "pageNum": 1, "pageSize": 10, "list": [ { "id": 200, "shopId": 2, "productSn": "P2…", "productName": "新商品", "productSubtitle": null, "mainImage": null, "price": 99.00, "stock": 10, "saleStatus": "OFF_SHELF", "auditStatus": "PASS", "createTime": "2026-04-11T12:00:00" } ] } }` |

---

### 2.3 商家商品详情

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/products/{id}` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Path：`id`。Query：`merchantId`（可选） |
| **示例请求** | `GET /api/merchant/products/200?merchantId=1` |
| **示例返回** | 同列表中单条结构，且含 `detail` 字段（若后端在详情中返回）。 |

---

### 2.4 新增商品

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/products` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`productName`（必填）；`price`（必填，≥0）；`stock`（必填，≥0）；`productSubtitle`、`mainImage`、`detail`（可选）；`merchantId`（可选）；`userId`（可选，须与 token 一致） |
| **示例请求** | `POST /api/merchant/products` Body: `{ "productName": "新商品", "price": 99.00, "stock": 10, "mainImage": "https://example.com/a.jpg" }` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "id": 200, "shopId": 2, "productSn": "P2…", "productName": "新商品", "saleStatus": "OFF_SHELF", "auditStatus": "PASS", "createTime": "2026-04-11T12:00:00", "detail": null, ... } }` |

---

### 2.5 修改商品

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/products/{id}` |
| **方法** | `PUT` |
| **请求参数** | Header：`Authorization`。Path：`id`。Body 同创建（`productName`、`price`、`stock` 等必填规则与创建一致）；`merchantId`、`userId` 可选 |
| **示例请求** | `PUT /api/merchant/products/200` Body: `{ "productName": "新商品改名", "price": 89.00, "stock": 8 }` |
| **示例返回** | 同商品 VO 结构。 |

---

### 2.6 商品上架

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/products/{id}/on-shelf` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Path：`id`。Query：`merchantId`（可选） |
| **示例请求** | `POST /api/merchant/products/200/on-shelf` |
| **示例返回** | `{ "code": 0, "message": "上架成功", "data": "OK" }` |

---

### 2.7 商品下架

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/products/{id}/off-shelf` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Path：`id`。Query：`merchantId`（可选） |
| **示例请求** | `POST /api/merchant/products/200/off-shelf` |
| **示例返回** | `{ "code": 0, "message": "下架成功", "data": "OK" }` |

---

### 2.8 删除商品（逻辑删除）

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/products/{id}` |
| **方法** | `DELETE` |
| **请求参数** | Header：`Authorization`。Path：`id`。Query：`merchantId`（可选） |
| **示例请求** | `DELETE /api/merchant/products/200` |
| **示例返回** | `{ "code": 0, "message": "删除成功", "data": "OK" }` |

---

### 2.9 商家订单分页列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/orders` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Query：`pageNum`、`pageSize`、`merchantId`（可选） |
| **示例请求** | `GET /api/merchant/orders?pageNum=1&pageSize=10` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "total": 1, "pageNum": 1, "pageSize": 10, "list": [ { "orderId": 501, "orderNo": "202604111200001001234567", "orderStatus": "PAID", "payAmount": 299.00, "payType": "MOCK", "payTime": "2026-04-11T12:05:00", "createTime": "2026-04-11T12:00:00", "shippingNo": null, "shipTime": null } ] } }` |

---

### 2.10 商家订单详情

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/orders/{id}` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Path：`id` 订单主键。Query：`merchantId`（可选） |
| **示例请求** | `GET /api/merchant/orders/501` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "orderId": 501, "orderNo": "…", "orderStatus": "PAID", "totalAmount": 299.00, "payAmount": 299.00, "receiverName": "…", "items": [ { "orderItemId": 600, "productId": 102, "productName": "…", "quantity": 1, "itemAmount": 299.00 } ] } }`（字段以实际响应为准） |

---

### 2.11 商家发货

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/orders/{id}/ship` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Path：`id`。Body JSON：`shippingNo`（必填）、`shippingRemark`（可选）、`merchantId`（可选）、`userId`（可选，须与 token 一致） |
| **示例请求** | `POST /api/merchant/orders/501/ship` Body: `{ "shippingNo": "SF1234567890", "shippingRemark": "轻放" }` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "orderId": 501, "orderNo": "…", "orderStatus": "SHIPPED", "shippingNo": "SF1234567890", "shippingRemark": "轻放", "shipTime": "2026-04-11T13:00:00" } }` |

---

### 2.12 子订单分页列表（预留数据表；已接库查询）

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/shop-orders` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Query：`pageNum`、`pageSize`、`merchantId`（可选） |
| **说明** | 查询 `shop_order` 表中 **本店 shop_id** 的记录；主流程未写入时 **`list` 通常为空**。 |
| **示例请求** | `GET /api/merchant/shop-orders?pageNum=1&pageSize=10` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "total": 0, "pageNum": 1, "pageSize": 10, "list": [] } }` |

---

### 2.13 子订单发货（占位；未接入）

| 项目 | 内容 |
|------|------|
| **URL** | `/api/merchant/shop-orders/{id}/ship` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Path：`id` 为 **shop_order 主键**。Body：`shippingNo`（必填）、`shippingRemark`、`merchantId`、`userId`（可选，与 token 一致） |
| **说明** | 当前调用返回 **业务错误**（`code=400`），提示使用 **2.11 主订单发货** 接口；**不修改数据库**。 |
| **示例请求** | `POST /api/merchant/shop-orders/1/ship` Body: `{ "shippingNo": "SF000" }` |

---

## 三、管理端

均需 **`Authorization`**，且用户须在 **`mall.auth.admin-user-ids`** 中（登录返回 `role` 为 `ADMIN`）。

### 3.1 商家申请分页列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/admin/merchants` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Query：`pageNum`（默认 1）、`pageSize`（默认 10）、`applyStatus`（可选，如 `PENDING`）、`keyword`（可选，商家名称模糊） |
| **示例请求** | `GET /api/admin/merchants?pageNum=1&pageSize=10&applyStatus=PENDING` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "total": 1, "pageNum": 1, "pageSize": 10, "list": [ { "merchantId": 10, "userId": 3001, "merchantCode": "M…", "merchantName": "测试公司", "contactName": "张三", "contactMobile": "13800000000", "applyStatus": "PENDING", "auditRemark": null, "applyTime": "2026-04-11T12:00:00", "auditTime": null } ] } }` |

---

### 3.2 审核商家申请

| 项目 | 内容 |
|------|------|
| **URL** | `/api/admin/merchants/audit` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Body JSON：`merchantId`（必填）、`auditAction`（必填，`APPROVE` 或 `REJECT`）、`auditRemark`（可选；驳回时建议填写，服务端会校验） |
| **示例请求** | `POST /api/admin/merchants/audit` Body: `{ "merchantId": 10, "auditAction": "APPROVE", "auditRemark": "资料齐全" }` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "merchantId": 10, "applyStatus": "APPROVED", "auditRemark": "资料齐全", "auditTime": "2026-04-11T12:30:00", "shopId": 20 } }` |

---

### 3.3 订单分页列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/admin/orders` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Query：`pageNum`、`pageSize`、`orderStatus`（可选）、`orderNo`（可选，模糊） |
| **示例请求** | `GET /api/admin/orders?pageNum=1&pageSize=10&orderStatus=PAID` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "total": 1, "pageNum": 1, "pageSize": 10, "list": [ { "orderId": 501, "orderNo": "…", "userId": 1001, "orderStatus": "PAID", "totalAmount": 299.00, "payAmount": 299.00, "payType": "MOCK", "payTime": "2026-04-11T12:05:00", "createTime": "2026-04-11T12:00:00", "shippingNo": null, "shipTime": null } ] } }` |

---

### 3.4 商品分页列表

| 项目 | 内容 |
|------|------|
| **URL** | `/api/admin/products` |
| **方法** | `GET` |
| **请求参数** | Header：`Authorization`。Query：`pageNum`、`pageSize`、`keyword`（可选）、`shopId`（可选）、`saleStatus`（可选，如 `ON_SHELF`） |
| **示例请求** | `GET /api/admin/products?pageNum=1&pageSize=10&saleStatus=ON_SHELF` |
| **示例返回** | `{ "code": 0, "message": "成功", "data": { "total": 2, "pageNum": 1, "pageSize": 10, "list": [ { "productId": 102, "shopId": 2, "productSn": "…", "productName": "商家｜机械键盘 K8", "price": 299.00, "stock": 200, "saleStatus": "ON_SHELF", "auditStatus": "PASS", "deleted": false, "createTime": "2026-01-15T08:00:00" } ] } }` |

---

### 3.5 管理员下架商品

| 项目 | 内容 |
|------|------|
| **URL** | `/api/admin/products/{id}/off-shelf` |
| **方法** | `POST` |
| **请求参数** | Header：`Authorization`。Path：`id` 商品主键 |
| **示例请求** | `POST /api/admin/products/102/off-shelf` |
| **示例返回** | `{ "code": 0, "message": "下架成功", "data": "OK" }` |

---

## 附录：与代码同步

- 接口以 `backend/src/main/java/com/mall/platform/controller` 包下类为准；若代码变更，请同步更新本文档。
- 订单状态、商品上下架、商家申请状态等枚举含义见业务代码 `com.mall.platform.enums` 与 `docs/mvp-acceptance-checklist.md`。
