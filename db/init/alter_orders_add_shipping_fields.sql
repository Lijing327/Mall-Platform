/*
  已有库增量：orders 发货结构化字段（不影响已有行，新列为 NULL）。
  历史若曾将发货信息写入 remark，可另行手工迁移，本脚本不修改 remark。
*/
USE mall_platform_test;
GO

IF COL_LENGTH('dbo.orders', 'shipping_no') IS NULL
BEGIN
    ALTER TABLE dbo.orders ADD shipping_no NVARCHAR(128) NULL;
END
GO

IF COL_LENGTH('dbo.orders', 'shipping_remark') IS NULL
BEGIN
    ALTER TABLE dbo.orders ADD shipping_remark NVARCHAR(512) NULL;
END
GO

IF COL_LENGTH('dbo.orders', 'ship_time') IS NULL
BEGIN
    ALTER TABLE dbo.orders ADD ship_time DATETIME2(0) NULL;
END
GO
