/*
  一期收口单店自营：把原种子数据中的"商家店"示例商品与示例订单 item 迁到自营店下。
  注意：merchant/shop 的商家店记录本身保留（作为结构预留），仅修正 product/order_item 的 shop_id。
  幂等：仅在 shop_id=2 且仍指向商家店的记录上执行；可重复运行无副作用。
*/
USE mall_platform_test;
GO

UPDATE dbo.product
SET    shop_id      = 1,
       product_sn   = CASE id WHEN 102 THEN N'P1SELF20260115002'
                              WHEN 103 THEN N'P1SELF20260115003'
                              ELSE product_sn END,
       product_name = CASE id WHEN 102 THEN N'自营｜机械键盘 K8'
                              WHEN 103 THEN N'自营｜USB-C 数据线（已下架样例）'
                              ELSE product_name END,
       product_subtitle = CASE id WHEN 102 THEN N'自营旗舰店示例商品'
                                  WHEN 103 THEN N'用于后台管理/筛选演示'
                                  ELSE product_subtitle END,
       update_time  = SYSUTCDATETIME()
WHERE  id IN (102, 103) AND shop_id = 2;
GO

UPDATE dbo.order_item
SET    shop_id      = 1,
       product_name = N'自营｜机械键盘 K8',
       update_time  = SYSUTCDATETIME()
WHERE  id = 501 AND shop_id = 2;
GO

PRINT N'单店自营种子迁移完成：商家店示例商品与订单 item 已挂到自营店（shop_id=1）。';
GO
