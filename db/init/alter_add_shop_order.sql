/*
  已有库增量：新增子订单表 shop_order（按店铺拆分，多商户扩展预留）。
  当前线上主流程仍只使用 orders / order_item；执行本脚本不改变业务行为。
  在目标库执行一次即可。
*/
USE mall_platform_test;
GO

IF OBJECT_ID(N'dbo.shop_order', N'U') IS NULL
BEGIN
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
END
GO
