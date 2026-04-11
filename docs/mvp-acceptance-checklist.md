# Mall Platform MVP 业务链路验收清单

本文档基于当前仓库接口与实现整理，用于端到端验收：**用户端**、**商家端**、**平台端**三条主链路及异常覆盖说明。

---

## 验收环境与前置数据（建议）

- 准备三类账号：`userId=1` 管理员（与 `mall.auth.admin-user-ids` 一致）、`1001` 普通用户、`2001` 已审核商家（与 `db/init/mall_platform_sqlserver_init.sql` 种子一致时可减少造数成本）。
- 使用 `application.yml` / `application-test.yml` 中配置的登录口令调用 `POST /api/auth/login` 获取 `token`，后续请求头：`Authorization: Bearer <token>`。
- 鉴权范围：`WebMvcConfig` 对 `/api/auth/login`、`/api/health/**`、`/api/products`、`/api/products/**` 不拦截；其余 `/api/**` 需 Bearer；`/api/admin/**` 另需管理员角色（`AuthInterceptor`）。
- 库表名以初始化脚本为准：`dbo.merchant`、`dbo.shop`、`dbo.product`、`dbo.orders`、`dbo.order_item`、`dbo.cart`（Java 实体中商品删除字段映射为 `is_deleted`）。

---

## 1. 用户链路：浏览商品 → 订单完成

| 步骤 | 操作说明 | 调用接口 | 预期结果 | 数据库变化（关注点） |
|------|----------|----------|----------|----------------------|
| 1.1 | 打开商品列表（无需登录） | `GET /api/products?pageNum=1&pageSize=10`（可选 `keyword`、`shopId`） | `code=0`，仅返回已上架且未删除商品 | 无写库 |
| 1.2 | 查看商品详情 | `GET /api/products/{id}` | 已上架且未删除则返回详情；否则业务错误 | 无写库 |
| 1.3 | 用户登录 | `POST /api/auth/login`，Body：`userId`、`password`（配置口令） | 返回 `token`、`role=USER`（非管理员账号） | 无写库 |
| 1.4 | 加入购物车 | `POST /api/cart/add`，Body：`userId`、`productId`、`quantity` | 成功提示 | `cart`：新增一行，或同一 `(user_id, product_id)` 累加 `quantity` |
| 1.5 | 查看购物车 | `GET /api/cart/list` | 返回购物车行及商品/店铺展示信息 | 无写库 |
| 1.6 | 勾选与数量 | 当前后端**无**单独「改勾选 / 改数量」接口；MVP 依赖加购累加；下单时读取 `checked=true` 的购物车行 | 按实际前端行为 | 仅 `POST /api/cart/add` 会写 `cart` |
| 1.7 | 创建订单 | `POST /api/orders/create`，Body：`userId`（与 token 一致） | 返回 `orderId`、`orderNo`、`orderStatus=PENDING_PAYMENT` 等 | `orders` 新增；`order_item` 按勾选行写入快照；`product.stock` 扣减；对应 `cart` 行删除 |
| 1.8 | 模拟支付 | `POST /api/orders/pay`，Body：`userId`、`orderNo` | `order_status=PAID`，`pay_type=MOCK`，`pay_time` 有值 | `orders` 更新 |
| 1.9 | 等待商家发货 | 由商家链路完成发货后，用户侧可见状态变化 | — | `orders`：`SHIPPED`，物流字段写入 |
| 1.10 | 我的订单 | `GET /api/orders/my` | 含订单项、金额、状态、物流等 | 无写库 |
| 1.11 | 确认收货 | `POST /api/orders/{id}/confirm-receive` | `COMPLETED`，`complete_time` 有值 | `orders` 更新 |

### 1.1 用户链路：异常与系统处理情况

| 场景 | 是否已处理 | 说明 |
|------|------------|------|
| 未带 token 访问需登录接口 | 是 | 拦截器校验失败返回 401 JSON |
| Body 中 `userId` 与 token 主体不一致 | 是 | `AuthBinding.assertSameUser` → FORBIDDEN |
| 商品已下架 / 已删除仍加购 | 是 | `CartService` 业务异常提示 |
| 加购数量超过库存 | 否 | 加购不校验库存；**创建订单**时校验 |
| 购物车无勾选商品就下单 | 是 | 「购物车为空，无法下单」 |
| 下单时库存不足 | 是 | 明确文案；条件更新失败时「库存扣减失败，请重试」 |
| 下单时含下架 / 删除商品 | 是 | 「存在无效商品，无法下单」 |
| 并发超卖 | 部分 | 依赖 `stock` 条件更新，可缓解；非完整秒杀方案 |
| 支付时订单不存在或不属于本人 | 是 | 查询含 `user_id` |
| 重复支付（已非待支付） | 是 | 「当前订单状态不允许支付」 |
| 确认收货：非本人订单 | 是 | NOT_FOUND |
| 确认收货：状态非已发货 | 是 | 「当前订单状态不可确认收货」 |
| 购物车列表仍展示已下架商品 | 部分 | 列表接口未二次过滤商品有效性 |

---

## 2. 商家链路：入驻 → 上架 → 发货

| 步骤 | 操作说明 | 调用接口 | 预期结果 | 数据库变化 |
|------|----------|----------|----------|--------------|
| 2.1 | 新用户登录（未入驻账号） | `POST /api/auth/login` | `USER` token | 无写库 |
| 2.2 | 提交入驻申请 | `POST /api/merchant/apply`，Body 见 `MerchantApplyDTO` | `applyStatus=PENDING` 等 | `merchant` 新增 |
| 2.3 | 管理员审核通过 | `POST /api/admin/merchants/audit`（管理员 token），`auditAction=APPROVE` 等 | `APPROVED`，可能返回 `shopId` | `merchant` 更新；`shop` 可能新增 `MERCHANT` 店铺 |
| 2.4 | 商家商品列表 | `GET /api/merchant/products?pageNum=1&pageSize=10`（可选 `merchantId`、`keyword`） | 分页 | 无写库 |
| 2.5 | 新建商品 | `POST /api/merchant/products`，Body 见 `MerchantProductCreateDTO` | 默认下架 | `product` 新增，`sale_status=OFF_SHELF` |
| 2.6 | 上架 | `POST /api/merchant/products/{id}/on-shelf`（可选 `merchantId`） | 成功 | `product.sale_status=ON_SHELF` |
| 2.7 | 用户下单并支付至已支付 | 见用户链路 1.7～1.8 | 订单 `PAID` | 同用户链路 |
| 2.8 | 商家订单列表 | `GET /api/merchant/orders` | 与本店 `shop_id` 相关的订单 | 无写库 |
| 2.9 | 商家订单详情 | `GET /api/merchant/orders/{id}` | 仅本店订单项 | 无写库 |
| 2.10 | 发货 | `POST /api/merchant/orders/{id}/ship`，Body：`shippingNo`（必填）等 | 返回物流信息 | `orders`：`SHIPPED`，物流字段写入 |

### 2.1 商家链路：异常与系统处理情况

| 场景 | 是否已处理 | 说明 |
|------|------------|------|
| 同一 `user_id` 重复申请入驻 | 是 | 「该用户已提交过商家申请」 |
| 未审核通过操作商品 | 是 | 「商家审核未通过，不能操作商品」 |
| 操作非本店商品 | 是 | NOT_FOUND 或无权提示 |
| 发货：订单非已支付 | 是 | 「仅已支付订单可发货」 |
| 发货：运单号为空 | 是 | `@NotBlank` |
| 发货：订单不含本店商品 | 是 | 业务错误 |
| 已发货后再次发货 | 是 | 状态非 `PAID`，不满足条件 |
| 驳回未填原因 | 是 | 「驳回时请填写驳回原因」 |
| 审核非待审核记录 | 是 | 「当前申请不是待审核状态」 |
| `auditAction` 非法 | 是 | 明确错误提示 |

---

## 3. 管理员链路：审核 → 管理

| 步骤 | 操作说明 | 调用接口 | 预期结果 | 数据库变化 |
|------|----------|----------|----------|------------|
| 3.1 | 管理员登录 | `POST /api/auth/login`（`userId` 在 `admin-user-ids`） | `role=ADMIN` | 无写库 |
| 3.2 | 商家申请列表 | `GET /api/admin/merchants`（可选 `applyStatus`、`keyword`） | 分页 | 无写库 |
| 3.3 | 审核 | `POST /api/admin/merchants/audit` | 同商家 2.3 | 同 2.3 |
| 3.4 | 全平台订单列表 | `GET /api/admin/orders`（可选 `orderStatus`、`orderNo`） | 分页 | 无写库 |
| 3.5 | 全平台商品列表 | `GET /api/admin/products`（可选 `keyword`、`shopId`、`saleStatus`） | 分页 | 无写库 |
| 3.6 | 强制下架商品 | `POST /api/admin/products/{id}/off-shelf` | 成功 | `product.sale_status=OFF_SHELF` |

### 3.1 管理员链路：异常与系统处理情况

| 场景 | 是否已处理 | 说明 |
|------|------------|------|
| 非管理员访问 `/api/admin/**` | 是 | 403 |
| 无 token / token 无效 | 是 | 401 |
| 下架不存在或已逻辑删除商品 | 是 | NOT_FOUND |
| 查询参数非法状态字符串 | 部分 | `trim().toUpperCase()` 后查询，无枚举强校验，通常无匹配结果 |

---

## 4. 稳定性与 MVP 边界（验收结论建议用语）

- 三条主链路在接口层可串成闭环：用户浏览（公开）→ 登录 → 购物车 → 下单扣库存并清理购物车 → 支付 → 商家发货 → 用户确认收货；商家入驻依赖管理员审核；管理员可列表与下架。
- 已知薄弱点（可记为风险项）：加购不校验库存；购物车列表不剔除已失效商品；管理端筛选状态未做强枚举校验；登录为 MVP 固定口令 + `userId`，非真实账号体系。

---

## 5. 文档维护

- 接口路径或鉴权策略变更时，请同步更新本文与 `backend/README.md` 中的管理端约定。
