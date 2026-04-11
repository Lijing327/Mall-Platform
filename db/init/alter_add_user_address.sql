/*
  已有库增量：新增 user_address 表（一期地址簿）。
  在目标库执行一次即可；与主初始化脚本 dbo.user_address 定义保持一致。
  若 JDBC 连接串的 databaseName 不是 mall_platform，请将下一行 USE 改成你的库名。
*/
USE mall_platform;
GO

IF OBJECT_ID(N'dbo.user_address', N'U') IS NULL
BEGIN
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
END
GO
