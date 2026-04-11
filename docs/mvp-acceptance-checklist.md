# 自营商城 MVP 1.0 验收清单

本文档对应一期定位：**自营商城 MVP 1.0 —— 单店自营交易内核**。
主链路收口为**用户端**与**后台管理端**两条；商家端与多店平台治理作为结构预留（详见末尾"平台化预留"章节）。

---

## 验收环境与前置数据（建议）

- 准备两类账号：`userId=1` 管理员（与 `mall.auth.admin-user-ids` 一致）、`1001` 普通用户。
- 使用 `application.yml` / `application-test.yml` 中配置的登录口令调用 `POST /api/auth/login` 获取 `token`，后续请求头：`Authorization: Bearer <token>`。
- 鉴权范围：`WebMvcConfig` 对 `/api/auth/login`、`/api/health/**`、`/api/products`、`/api/products/**` 不拦截；其余 `/api/**` 需 Bearer；`/api/admin/**` 另需管理员角色（`AuthInterceptor`）。
- 库表名以初始化脚本为准：`dbo.product`、`dbo.orders`、`dbo.order_item`、`dbo.cart`、`dbo.user_address`、`dbo.shop`。一期种子数据仅在"自营旗舰店"（`shop_id=1`）下挂商品，商家店记录保留但不在演示链路中使用。
- 已有库升级：执行 `db/init/alter_add_user_address.sql` 建新表；执行 `db/init/alter_unify_self_shop_seed.sql` 将原商家店种子迁到自营店。

---

## 1. 用户链路：浏览 → 地址 → 加购 → 下单 → 完成

| 步骤 | 操作说明 | 调用接口 | 预期结果 | 数据库变化 |
|------|----------|----------|----------|------------|
| 1.1 | 打开商品列表（无需登录） | `GET /api/products?pageNum=1&pageSize=10` | `code=0`，仅返回已上架且未删除商品 | 无写库 |
| 1.2 | 查看商品详情 | `GET /api/products/{id}` | 已上架且未删除返回详情 | 无写库 |
| 1.3 | 用户登录 | `POST /api/auth/login` | 返回 `token`、`role=USER` | 无写库 |
| 1.4 | 维护收货地址 | `GET /api/user/addresses`、`POST /api/user/addresses`、`PUT /api/user/addresses/{id}`、`POST /api/user/addresses/{id}/default`、`DELETE /api/user/addresses/{id}` | CRUD 成功；首次创建自动置为默认；切换默认时旧默认清除 | `user_address` 新增/更新；软删 `is_deleted=1` |
| 1.5 | 加入购物车 | `POST /api/cart/add` | 成功提示 | `cart`：新增或同一 `(user_id, product_id)` 累加 |
| 1.6 | 查看购物车（含失效标记） | `GET /api/cart/list` | 购物车行及商品信息；已下架/删除商品 `invalid=true` | 无写库 |
| 1.7 | 修改数量 | `POST /api/cart/update` Body `{cartId, quantity}` | 成功；校验商品有效性与库存 | `cart` 更新 `quantity` |
| 1.8 | 删除购物车项 | `POST /api/cart/delete` Body `{cartId}` | 成功 | `cart` 行删除 |
| 1.9 | 创建订单 | `POST /api/orders/create` Body `{addressId, remark?}` | 返回 `orderId`、`orderNo`、`orderStatus=PENDING_PAYMENT` | `orders` 新增（含收货人快照）；`order_item` 按勾选行写入快照；`product.stock` 扣减；对应 `cart` 行删除 |
| 1.10 | 模拟支付 | `POST /api/orders/pay` | `order_status=PAID`，`pay_type=MOCK`，`pay_time` 有值 | `orders` 更新 |
| 1.11 | 等待后台发货 | 由后台管理端完成发货后，用户侧可见状态变化 | — | `orders`：`SHIPPED`，物流字段写入 |
| 1.12 | 我的订单 | `GET /api/orders/my` | 含订单项、金额、状态、收货人/手机/地址快照、物流等 | 无写库 |
| 1.13 | 确认收货 | `POST /api/orders/{id}/confirm-receive` | `COMPLETED`，`complete_time` 有值 | `orders` 更新 |

### 1.1 用户链路：异常与系统处理情况

| 场景 | 是否已处理 | 说明 |
|------|------------|------|
| 未带 token 访问需登录接口 | 是 | 拦截器校验失败返回 401 JSON |
| Body 中 `userId` 与 token 主体不一致 | 是 | `AuthBinding.assertSameUser` → FORBIDDEN |
| 创建订单未选地址 | 是 | `@NotNull addressId` 返回 400 |
| 创建订单使用他人地址 | 是 | `UserAddressService.findOwnEntity` 校验归属 → 400 |
| 修改数量时商品已下架 / 已删除 | 是 | `CartService.updateQuantity` → 400 |
| 修改数量超过库存 | 是 | `CartService.updateQuantity` → 400 |
| 购物车列表展示已失效商品 | 是 | 标记 `invalid=true`，前端置灰，结算链路自动跳过（下单时校验） |
| 加购数量累加超库存 | 否 | 加购仍只累加；下单/改数量时校验 |
| 购物车无勾选商品就下单 | 是 | 「购物车为空，无法下单」 |
| 下单时库存不足 | 是 | 明确文案；条件更新失败时「库存扣减失败，请重试」 |
| 下单时含下架 / 删除商品 | 是 | 「存在无效商品，无法下单」 |
| 并发超卖 | 部分 | 依赖 `stock` 条件更新，可缓解 |
| 支付时订单不存在或不属于本人 | 是 | 查询含 `user_id` |
| 重复支付（已非待支付） | 是 | 「当前订单状态不允许支付」 |
| 确认收货：非本人 / 状态非已发货 | 是 | NOT_FOUND / 业务错误 |

---

## 2. 后台管理端：订单 → 发货 / 商品管理

> 一期后台管理端聚焦订单处理与商品维护。管理员账号由 `mall.auth.admin-user-ids` 决定，登录后 `role=ADMIN`。
> 发货动作一期沿用 `/api/merchant/orders/{id}/ship` 主订单接口（管理员账号同样可调用），物流字段仅做发货单号 + 备注 + 时间占位。

| 步骤 | 操作说明 | 调用接口 | 预期结果 | 数据库变化 |
|------|----------|----------|----------|------------|
| 2.1 | 管理员登录 | `POST /api/auth/login` | `role=ADMIN` | 无写库 |
| 2.2 | 全平台订单列表 | `GET /api/admin/orders`（可选 `orderStatus`、`orderNo`） | 分页返回 | 无写库 |
| 2.3 | 发货 | `POST /api/merchant/orders/{id}/ship` Body `{shippingNo, shippingRemark?}` | 返回物流信息 | `orders`：`SHIPPED`，物流字段写入 |
| 2.4 | 全平台商品列表 | `GET /api/admin/products` | 分页返回 | 无写库 |
| 2.5 | 下架商品 | `POST /api/admin/products/{id}/off-shelf` | 成功 | `product.sale_status=OFF_SHELF` |

### 2.1 后台管理端：异常与系统处理情况

| 场景 | 是否已处理 | 说明 |
|------|------------|------|
| 非管理员访问 `/api/admin/**` | 是 | 403 |
| 无 token / token 无效 | 是 | 401 |
| 发货：订单非已支付 / 已发货 | 是 | 业务错误 |
| 发货：运单号为空 | 是 | `@NotBlank` |
| 下架不存在或已逻辑删除商品 | 是 | NOT_FOUND |

---

## 3. 稳定性与 MVP 边界（验收结论建议用语）

- 用户链路在一期形成完整闭环：浏览（公开）→ 登录 → 维护地址 → 加购（含改数量/删除/失效标记）→ 选地址下单扣库存并清理购物车 → 模拟支付 → 后台发货 → 用户确认收货。
- 已知薄弱点（记为风险项）：加购不校验库存；登录为 MVP 固定口令 + `userId`，非真实账号体系；无真实支付网关与物流轨迹。

---

## 4. 平台化预留（一期不在主链路中演示）

以下能力在代码与数据表层面已经预留，但**默认关闭、不出现在前台菜单与一期演示链路中**，用于后续升级为平台型商城：

- `dbo.merchant`、`dbo.shop`（`shop_type=MERCHANT`）、`dbo.shop_order` 等表结构保留。
- 后端 `com.mall.platform.controller.Merchant*Controller`、`AdminManagementController` 中的商家审核接口均保留，可按需启用。
- 特性开关 `mall.feature.multi-shop / split-order / settlement`（默认 `false`）。
- 前端 `frontend/src/views/merchant/**`、`AdminMerchantAuditPage.vue` 的路由仍在，只是未出现在侧栏菜单中。

后续进入"阶段二：多商户 / 平台化"时可基于上述预留逐步打开，无需重新设计表结构。

---

## 5. 文档维护

- 接口路径或鉴权策略变更时，请同步更新本文与 `docs/mvp-api-reference.md`。
