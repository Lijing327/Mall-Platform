# 数据库初始化（SQL Server）

本目录提供与当前后端实体一致的 **一键初始化脚本**，用于新环境复现表结构与联调种子数据。

初始化脚本对应当前自营单店 MVP 模式；若未开启 `mall.feature.multi-shop` / `split-order` / `settlement`，系统仍按主单流程运行，`shop_order` 仅作为未来扩展结构保留。

## 文件说明

| 文件 | 说明 |
|------|------|
| `mall_platform_sqlserver_init.sql` | 建表 + 外键 + 种子数据（默认目标库 `mall_platform_test`） |
| `alter_orders_add_complete_time.sql` | 已有库增量：为 `orders` 增加 `complete_time`（若已用旧版全量脚本可补执行） |
| `alter_orders_add_shipping_fields.sql` | 已有库增量：`shipping_no` / `shipping_remark` / `ship_time` |
| `alter_add_shop_order.sql` | 已有库增量：新增子订单表 `shop_order`（多店铺扩展预留；全量脚本已含建表时可不执行） |

> 实体 `DemoEntity` / `demo_table` 仅为分层演示，**未纳入**业务初始化脚本。

## 前置条件

1. 已安装 **SQL Server 2016+**（脚本使用 `DROP IF EXISTS` 等语法）。
2. 已创建数据库 **`mall_platform_test`**（与 `backend/src/main/resources/application-test.yml` 默认 `databaseName` 一致）。  
   - 若尚未建库，可手动执行：  
     `CREATE DATABASE mall_platform_test COLLATE Chinese_PRC_CI_AS;`  
   - 或在 `mall_platform_sqlserver_init.sql` 顶部按注释说明取消 **自动建库** 段落的注释（需 `master` 级权限）。

## 执行方式

### 方式一：SQL Server Management Studio (SSMS)

1. 连接到目标实例。
2. 新建查询，打开 `mall_platform_sqlserver_init.sql`。
3. 若库名不是 `mall_platform_test`，全文替换 `USE mall_platform_test;` 为你的库名。
4. 执行（F5）。

### 方式二：sqlcmd（命令行）

在仓库根目录执行（按环境修改服务器、账号密码）：

```bash
sqlcmd -S 127.0.0.1,1433 -U sa -P "你的密码" -d mall_platform_test -i "db/init/mall_platform_sqlserver_init.sql"
```

> 若脚本内含 `USE mall_platform_test`，也可省略 `-d`，但建议与连接串保持一致。

## 种子数据与联调身份

当前项目 **无独立用户表**；登录态为 `userId` + 令牌（见 `mall.auth.*`）。下列为脚本约定的 **逻辑用户 ID**（与种子数据一致）：

| 角色 | userId | 说明 |
|------|--------|------|
| 管理员 | **1** | 须与 `application.yml` 中 `mall.auth.admin-user-ids` 包含 `1`（默认已包含） |
| 普通用户 | **1001** | 购物车、示例订单归属用户 |
| 商家主账号 | **2001** | `merchant.id = 1`，已审核 `APPROVED` |

### 店铺与商品

| 类型 | shop.id | shop_type | 说明 |
|------|---------|-----------|------|
| 自营 | 1 | `SELF` | `merchant_id` 为空，`owner_user_id = 1` |
| 商家 | 2 | `MERCHANT` | 绑定 `merchant_id = 1`，`owner_user_id = 2001` |

| product.id | shop_id | sale_status | 说明 |
|------------|---------|-------------|------|
| 101 | 1 | `ON_SHELF` | 自营上架商品 |
| 102 | 2 | `ON_SHELF` | 商家上架商品 |
| 103 | 2 | `OFF_SHELF` | 下架样例（列表/筛选演示） |

另含：用户 **1001** 的已支付示例订单（`orders.id = 500`）、购物车一行（`cart.id = 1`）。

### 前端联调提示

- 登录口令默认与后端一致：`mvp-demo`（见 `mall.auth.login-password`）。
- 商家端请求可携带 `merchantId=1`（与 `merchant` 主键一致）；本地可在浏览器存储中设置 `mall_mvp_merchant_id = 1`。

## 一键后端启动（概要）

1. 执行本脚本初始化库表与数据。  
2. 配置 `DB_TEST_URL` / `DB_TEST_USERNAME` / `DB_TEST_PASSWORD`（或修改 `application-test.yml`）。  
3. 在 `backend` 目录：`mvn spring-boot:run`。  
4. 前端代理指向 `http://localhost:8080`，按 `backend/README.md` 推荐顺序联调。

## 重复执行

脚本会先 **DROP** 再 **CREATE** 业务表（`order_item`、`cart`、`orders`、`product`、`shop`、`merchant`），适合开发环境反复刷库。**切勿**在生产库上直接执行而未备份。
