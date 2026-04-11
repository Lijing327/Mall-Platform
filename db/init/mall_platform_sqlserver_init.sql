/*
================================================================================
 Mall Platform - SQL Server 初始化脚本（表结构 + 种子数据）
 默认数据库: mall_platform_test（与 backend/src/main/resources/application-test.yml 中 databaseName 一致）
 要求: SQL Server 2016+（使用 DROP IF EXISTS 等语法）
 说明: 本脚本与当前 MyBatis-Plus 实体字段（下划线命名）对齐；用户身份为逻辑 userId，与鉴权配置 mall.auth.admin-user-ids 配合使用。
================================================================================
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

/* 如需在实例上自动建库，取消下面两段注释（需要 master 权限） */
/*
IF DB_ID(N'mall_platform_test') IS NULL
BEGIN
    CREATE DATABASE mall_platform_test COLLATE Chinese_PRC_CI_AS;
END
GO
*/

USE mall_platform_test;
GO

/* ---------- 清理（按外键依赖逆序） ---------- */
IF OBJECT_ID(N'dbo.order_item', N'U') IS NOT NULL DROP TABLE dbo.order_item;
IF OBJECT_ID(N'dbo.cart', N'U') IS NOT NULL DROP TABLE dbo.cart;
IF OBJECT_ID(N'dbo.shop_order', N'U') IS NOT NULL DROP TABLE dbo.shop_order;
IF OBJECT_ID(N'dbo.orders', N'U') IS NOT NULL DROP TABLE dbo.orders;
IF OBJECT_ID(N'dbo.product', N'U') IS NOT NULL DROP TABLE dbo.product;
IF OBJECT_ID(N'dbo.shop', N'U') IS NOT NULL DROP TABLE dbo.shop;
IF OBJECT_ID(N'dbo.merchant', N'U') IS NOT NULL DROP TABLE dbo.merchant;
IF OBJECT_ID(N'dbo.user_address', N'U') IS NOT NULL DROP TABLE dbo.user_address;
GO

/* ---------- merchant ---------- */
CREATE TABLE dbo.merchant (
    id              BIGINT          NOT NULL IDENTITY(1,1) CONSTRAINT PK_merchant PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    merchant_code   NVARCHAR(64)    NOT NULL,
    merchant_name   NVARCHAR(128)   NOT NULL,
    contact_name    NVARCHAR(64)    NOT NULL,
    contact_mobile  NVARCHAR(32)    NOT NULL,
    apply_status    NVARCHAR(32)    NOT NULL,
    audit_remark    NVARCHAR(512)   NULL,
    apply_time      DATETIME2(0)    NOT NULL,
    audit_time      DATETIME2(0)    NULL,
    status          NVARCHAR(32)    NOT NULL,
    create_time     DATETIME2(0)    NOT NULL CONSTRAINT DF_merchant_create DEFAULT (SYSUTCDATETIME()),
    update_time     DATETIME2(0)    NOT NULL CONSTRAINT DF_merchant_update DEFAULT (SYSUTCDATETIME())
);
CREATE INDEX IX_merchant_user_id ON dbo.merchant (user_id);
CREATE INDEX IX_merchant_apply_status ON dbo.merchant (apply_status);
GO

/* ---------- shop ---------- */
CREATE TABLE dbo.shop (
    id              BIGINT          NOT NULL IDENTITY(1,1) CONSTRAINT PK_shop PRIMARY KEY,
    shop_code       NVARCHAR(64)    NOT NULL,
    shop_name       NVARCHAR(128)   NOT NULL,
    shop_type       NVARCHAR(32)    NOT NULL,
    merchant_id     BIGINT          NULL,
    owner_user_id   BIGINT          NOT NULL,
    status          NVARCHAR(32)    NOT NULL,
    create_time     DATETIME2(0)    NOT NULL CONSTRAINT DF_shop_create DEFAULT (SYSUTCDATETIME()),
    update_time     DATETIME2(0)    NOT NULL CONSTRAINT DF_shop_update DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_shop_merchant FOREIGN KEY (merchant_id) REFERENCES dbo.merchant (id)
);
CREATE INDEX IX_shop_merchant_id ON dbo.shop (merchant_id);
CREATE INDEX IX_shop_type ON dbo.shop (shop_type);
GO

/* ---------- product ---------- */
CREATE TABLE dbo.product (
    id               BIGINT           NOT NULL IDENTITY(1,1) CONSTRAINT PK_product PRIMARY KEY,
    shop_id          BIGINT           NOT NULL,
    product_sn       NVARCHAR(64)     NOT NULL,
    product_name     NVARCHAR(256)    NOT NULL,
    product_subtitle NVARCHAR(512)    NULL,
    main_image       NVARCHAR(512)    NULL,
    detail           NVARCHAR(MAX)    NULL,
    price            DECIMAL(18,2)    NOT NULL,
    stock            INT              NOT NULL,
    sale_status      NVARCHAR(32)     NOT NULL,
    audit_status     NVARCHAR(32)     NOT NULL,
    is_deleted       BIT              NOT NULL CONSTRAINT DF_product_deleted DEFAULT (0),
    create_time      DATETIME2(0)     NOT NULL CONSTRAINT DF_product_create DEFAULT (SYSUTCDATETIME()),
    update_time      DATETIME2(0)     NOT NULL CONSTRAINT DF_product_update DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_product_shop FOREIGN KEY (shop_id) REFERENCES dbo.shop (id)
);
CREATE INDEX IX_product_shop_id ON dbo.product (shop_id);
CREATE INDEX IX_product_sale_status ON dbo.product (sale_status);
GO

/* ---------- orders ---------- */
CREATE TABLE dbo.orders (
    id               BIGINT           NOT NULL IDENTITY(1,1) CONSTRAINT PK_orders PRIMARY KEY,
    order_no         NVARCHAR(64)     NOT NULL,
    user_id          BIGINT           NOT NULL,
    order_status     NVARCHAR(32)     NOT NULL,
    total_amount     DECIMAL(18,2)    NOT NULL,
    pay_amount       DECIMAL(18,2)    NOT NULL,
    pay_type         NVARCHAR(32)     NULL,
    pay_time         DATETIME2(0)     NULL,
    receiver_name    NVARCHAR(64)     NULL,
    receiver_mobile  NVARCHAR(32)     NULL,
    receiver_address NVARCHAR(512)    NULL,
    remark           NVARCHAR(512)    NULL,
    complete_time    DATETIME2(0)     NULL,
    shipping_no      NVARCHAR(128)    NULL,
    shipping_remark  NVARCHAR(512)    NULL,
    ship_time        DATETIME2(0)     NULL,
    create_time      DATETIME2(0)     NOT NULL CONSTRAINT DF_orders_create DEFAULT (SYSUTCDATETIME()),
    update_time      DATETIME2(0)     NOT NULL CONSTRAINT DF_orders_update DEFAULT (SYSUTCDATETIME())
);
CREATE UNIQUE INDEX UX_orders_order_no ON dbo.orders (order_no);
CREATE INDEX IX_orders_user_id ON dbo.orders (user_id);
GO

/* ---------- shop_order（子订单：按店铺拆分，多商户预留；当前主流程可不写入） ---------- */
CREATE TABLE dbo.shop_order (
    id               BIGINT           NOT NULL IDENTITY(1,1) CONSTRAINT PK_shop_order PRIMARY KEY,
    order_id         BIGINT           NOT NULL,
    shop_id          BIGINT           NOT NULL,
    shop_name        NVARCHAR(128)    NOT NULL,
    amount           DECIMAL(18,2)    NOT NULL,
    status           NVARCHAR(32)     NOT NULL,
    shipping_no      NVARCHAR(128)    NULL,
    shipping_remark  NVARCHAR(512)    NULL,
    ship_time        DATETIME2(0)     NULL,
    complete_time    DATETIME2(0)     NULL,
    create_time      DATETIME2(0)     NOT NULL CONSTRAINT DF_shop_order_create DEFAULT (SYSUTCDATETIME()),
    update_time      DATETIME2(0)     NOT NULL CONSTRAINT DF_shop_order_update DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_shop_order_orders FOREIGN KEY (order_id) REFERENCES dbo.orders (id),
    CONSTRAINT FK_shop_order_shop FOREIGN KEY (shop_id) REFERENCES dbo.shop (id)
);
CREATE INDEX IX_shop_order_order_id ON dbo.shop_order (order_id);
CREATE INDEX IX_shop_order_shop_id ON dbo.shop_order (shop_id);
CREATE UNIQUE INDEX UX_shop_order_order_shop ON dbo.shop_order (order_id, shop_id);
GO

/* ---------- order_item ---------- */
CREATE TABLE dbo.order_item (
    id            BIGINT           NOT NULL IDENTITY(1,1) CONSTRAINT PK_order_item PRIMARY KEY,
    order_id      BIGINT           NOT NULL,
    shop_id       BIGINT           NOT NULL,
    product_id    BIGINT           NOT NULL,
    product_name  NVARCHAR(256)    NOT NULL,
    product_image NVARCHAR(512)    NULL,
    product_price DECIMAL(18,2)    NOT NULL,
    quantity      INT              NOT NULL,
    item_amount   DECIMAL(18,2)    NOT NULL,
    item_status   NVARCHAR(32)     NOT NULL,
    create_time   DATETIME2(0)     NOT NULL CONSTRAINT DF_order_item_create DEFAULT (SYSUTCDATETIME()),
    update_time   DATETIME2(0)     NOT NULL CONSTRAINT DF_order_item_update DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_order_item_order FOREIGN KEY (order_id) REFERENCES dbo.orders (id),
    CONSTRAINT FK_order_item_product FOREIGN KEY (product_id) REFERENCES dbo.product (id)
);
CREATE INDEX IX_order_item_order_id ON dbo.order_item (order_id);
CREATE INDEX IX_order_item_shop_id ON dbo.order_item (shop_id);
GO

/* ---------- cart ---------- */
CREATE TABLE dbo.cart (
    id          BIGINT        NOT NULL IDENTITY(1,1) CONSTRAINT PK_cart PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    shop_id     BIGINT        NOT NULL,
    product_id  BIGINT        NOT NULL,
    quantity    INT           NOT NULL,
    checked     BIT           NOT NULL CONSTRAINT DF_cart_checked DEFAULT (1),
    create_time DATETIME2(0)  NOT NULL CONSTRAINT DF_cart_create DEFAULT (SYSUTCDATETIME()),
    update_time DATETIME2(0)  NOT NULL CONSTRAINT DF_cart_update DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT FK_cart_shop FOREIGN KEY (shop_id) REFERENCES dbo.shop (id),
    CONSTRAINT FK_cart_product FOREIGN KEY (product_id) REFERENCES dbo.product (id)
);
CREATE INDEX IX_cart_user_id ON dbo.cart (user_id);
GO

/* ---------- user_address（一期地址簿：CRUD + 默认地址；下单时快照到订单） ---------- */
CREATE TABLE dbo.user_address (
    id              BIGINT        NOT NULL IDENTITY(1,1) CONSTRAINT PK_user_address PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    receiver_name   NVARCHAR(64)  NOT NULL,
    receiver_mobile NVARCHAR(32)  NOT NULL,
    province        NVARCHAR(64)  NOT NULL,
    city            NVARCHAR(64)  NOT NULL,
    district        NVARCHAR(64)  NOT NULL,
    detail_address  NVARCHAR(256) NOT NULL,
    is_default      BIT           NOT NULL CONSTRAINT DF_user_address_default DEFAULT (0),
    is_deleted      BIT           NOT NULL CONSTRAINT DF_user_address_deleted DEFAULT (0),
    create_time     DATETIME2(0)  NOT NULL CONSTRAINT DF_user_address_create DEFAULT (SYSUTCDATETIME()),
    update_time     DATETIME2(0)  NOT NULL CONSTRAINT DF_user_address_update DEFAULT (SYSUTCDATETIME())
);
CREATE INDEX IX_user_address_user_id ON dbo.user_address (user_id);
GO

/* ================================================================================
   种子数据（逻辑用户说明见同目录 README.md）
   userId=1   管理员（须与 mall.auth.admin-user-ids 配置一致）
   userId=1001 普通消费者
   userId=2001 商家主账号（已审核商家 merchant_id=1）
   ================================================================================ */

DECLARE @t DATETIME2(0) = CAST(N'2026-01-15T08:00:00' AS DATETIME2(0));

SET IDENTITY_INSERT dbo.merchant ON;
INSERT INTO dbo.merchant (id, user_id, merchant_code, merchant_name, contact_name, contact_mobile, apply_status, audit_remark, apply_time, audit_time, status, create_time, update_time)
VALUES
(1, 2001, N'M2026011500001', N'演示商家有限公司', N'张商', N'13800002001', N'APPROVED', N'初始化脚本审核通过', @t, @t, N'ENABLED', @t, @t);
SET IDENTITY_INSERT dbo.merchant OFF;

SET IDENTITY_INSERT dbo.shop ON;
INSERT INTO dbo.shop (id, shop_code, shop_name, shop_type, merchant_id, owner_user_id, status, create_time, update_time)
VALUES
(1, N'SELF_PLATFORM_001', N'平台自营旗舰店', N'SELF', NULL, 1, N'ENABLED', @t, @t),
(2, N'S_MERCH_2001_001', N'演示商家有限公司店铺', N'MERCHANT', 1, 2001, N'ENABLED', @t, @t);
SET IDENTITY_INSERT dbo.shop OFF;

/* 说明：一期定位为"自营商城 MVP 1.0"，种子示例统一挂在自营店（shop_id=1）下。
   merchant/shop 的商家店结构继续保留，作为后续平台化预留。 */
SET IDENTITY_INSERT dbo.product ON;
INSERT INTO dbo.product (id, shop_id, product_sn, product_name, product_subtitle, main_image, detail, price, stock, sale_status, audit_status, is_deleted, create_time, update_time)
VALUES
(101, 1, N'P1SELF20260115001', N'自营｜无线鼠标 M1', N'自营旗舰店示例商品', N'https://example.com/img/mouse.jpg', N'<p>自营商品详情</p>', 59.90, 1000, N'ON_SHELF', N'PASS', 0, @t, @t),
(102, 1, N'P1SELF20260115002', N'自营｜机械键盘 K8', N'自营旗舰店示例商品', N'https://example.com/img/kb.jpg', N'<p>自营商品详情</p>', 299.00, 200, N'ON_SHELF', N'PASS', 0, @t, @t),
(103, 1, N'P1SELF20260115003', N'自营｜USB-C 数据线（已下架样例）', N'用于后台管理/筛选演示', NULL, NULL, 19.90, 50, N'OFF_SHELF', N'PASS', 0, @t, @t);
SET IDENTITY_INSERT dbo.product OFF;

SET IDENTITY_INSERT dbo.orders ON;
INSERT INTO dbo.orders (id, order_no, user_id, order_status, total_amount, pay_amount, pay_type, pay_time, receiver_name, receiver_mobile, receiver_address, remark, complete_time, shipping_no, shipping_remark, ship_time, create_time, update_time)
VALUES
(500, N'DEMO2026011510001001500001', 1001, N'SHIPPED', 299.00, 299.00, N'MOCK', @t, N'李买家', N'13900001001', N'上海市浦东新区演示路 1 号', N'用户下单备注示例', NULL, N'SF1234567890', N'请当面签收', @t, @t, @t);
SET IDENTITY_INSERT dbo.orders OFF;

SET IDENTITY_INSERT dbo.order_item ON;
INSERT INTO dbo.order_item (id, order_id, shop_id, product_id, product_name, product_image, product_price, quantity, item_amount, item_status, create_time, update_time)
VALUES
(501, 500, 1, 102, N'自营｜机械键盘 K8', N'https://example.com/img/kb.jpg', 299.00, 1, 299.00, N'NORMAL', @t, @t);
SET IDENTITY_INSERT dbo.order_item OFF;

SET IDENTITY_INSERT dbo.cart ON;
INSERT INTO dbo.cart (id, user_id, shop_id, product_id, quantity, checked, create_time, update_time)
VALUES
(1, 1001, 1, 101, 2, 1, @t, @t);
SET IDENTITY_INSERT dbo.cart OFF;
GO

PRINT N'Mall Platform 初始化完成：merchant/shop/product/orders/order_item/cart 已就绪。';
GO
