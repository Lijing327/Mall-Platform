/*
  已有库增量：为 orders 增加完成时间（确认收货时写入）。
  在目标库执行一次即可。
*/
USE mall_platform_test;
GO

IF COL_LENGTH('dbo.orders', 'complete_time') IS NULL
BEGIN
    ALTER TABLE dbo.orders ADD complete_time DATETIME2(0) NULL;
END
GO
