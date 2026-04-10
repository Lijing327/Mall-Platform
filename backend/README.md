# 平台商城 MVP 后端开发手册

## 1. 项目定位

本项目当前阶段为 **MVP 后端基础骨架**，目标是先保证：

- Spring Boot 项目可稳定启动
- SQL Server 可按环境连接
- 基础分层结构清晰可扩展
- 统一返回与全局异常处理可复用

当前不包含具体商城业务逻辑（如商品、订单、购物车等）。

## 2. 技术栈与版本

- JDK: 17
- Spring Boot: 3.3.4
- Maven: 3.9.x（建议）
- 数据库: SQL Server
- ORM: MyBatis-Plus（快速落地，便于后续扩展）

## 3. 目录结构说明

`src/main/java/com/mall/platform`：

- `controller`：接口入口层
- `service`：业务编排层
- `repository`：数据访问层（MyBatis-Plus Mapper）
- `entity`：实体层
- `dto`：请求参数对象
- `vo`：返回展示对象
- `common`：通用能力（`Result`、异常处理、错误码）

`src/main/resources`：

- `application.yml`：公共配置 + 默认激活环境
- `application-test.yml`：测试环境数据库配置
- `application-prod.yml`：正式环境数据库配置

## 4. 环境配置说明

### 4.1 Profile 约定

- 默认环境：`test`
- 正式环境：`prod`

### 4.2 JDK 版本统一（强制）

- 项目强制要求：`JDK 17`
- `pom.xml` 已启用 `maven-enforcer-plugin`，非 JDK 17 会直接构建失败
- 团队开发机请统一以下三处：
  1. IDEA Project SDK = 17
  2. IDEA Maven Runner JDK = 17
  3. 系统 `JAVA_HOME` = JDK 17

### 4.3 环境变量

测试环境：

- `DB_TEST_URL`
- `DB_TEST_USERNAME`
- `DB_TEST_PASSWORD`

正式环境：

- `DB_PROD_URL`
- `DB_PROD_USERNAME`
- `DB_PROD_PASSWORD`

> 建议：不要把真实账号密码写入仓库文件，统一使用环境变量注入。

## 5. 启动与验证

## 5.1 启动命令

默认测试环境启动：

```bash
mvn spring-boot:run
```

指定正式环境启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 5.2 验证接口

健康检查：

- `GET /api/health`

预期返回：

- `code = 0`
- `message = 成功`
- `data.status = UP`

## 6. 开发规范（当前阶段）

- 代码注释统一使用中文
- 新增功能遵循分层：`controller -> service -> repository`
- 接口返回统一使用 `Result<T>`
- 业务异常优先抛 `BizException`，统一由 `GlobalExceptionHandler` 处理
- 不在当前阶段引入复杂鉴权、消息队列、搜索引擎等非 MVP 必需能力

## 7. 常见问题与排查

### 7.1 构建报错：`ExceptionInInitializerError` / `TypeTag::UNKNOWN`

**现象**：

- 编译阶段报 `java.lang.ExceptionInInitializerError`
- 伴随 `com.sun.tools.javac.code.TypeTag :: UNKNOWN`

**常见原因**：

- JDK 与注解处理器版本不兼容（历史上常见于 Lombok）
- IDE 使用的 JDK 与 Maven 使用的 JDK 不一致

**处理步骤**：

1. 确认 IDE Project SDK 为 JDK 17
2. 确认 Maven Runner JDK 为 JDK 17
3. 重新加载 Maven 项目（Reimport）
4. 清理 IDE 缓存并重启（Invalidate Caches / Restart）
5. 执行 `mvn -v` 检查 Java 版本是否为 17

---

### 7.4 构建报错：`Detected JDK version ... is not in the allowed range [17,18)`

**现象**：

- Maven 构建直接失败，提示 JDK 版本不在允许范围

**原因**：

- 当前项目已强制 JDK 17，运行时仍使用了 JDK 21/25 等其他版本

**处理步骤**：

1. 执行 `mvn -v` 检查当前 Maven 所用 Java 版本
2. 将 `JAVA_HOME` 切换到 JDK 17
3. 在 IDEA 中将 Project SDK 与 Maven Runner JDK 都改为 17
4. 重新加载 Maven 项目后再构建

---

### 7.2 启动失败：数据库连接异常

**现象**：

- 启动时连接 SQL Server 失败
- 报账号密码错误、超时或握手异常

**排查清单**：

1. 检查环境变量是否已配置并生效
2. 检查数据库服务器是否放通对应端口（通常 1433）
3. 检查 `DB_*_URL` 中 `databaseName` 是否正确
4. 若为自签名证书环境，确保连接串包含：
   - `encrypt=true`
   - `trustServerCertificate=true`

---

### 7.3 Mapper 扫描不到

**现象**：

- 启动时提示找不到 Mapper Bean

**排查清单**：

1. 确认启动类包含 `@MapperScan("com.mall.platform.repository")`
2. 确认 Mapper 接口在 `repository` 包下
3. 确认 Mapper 接口继承 `BaseMapper<实体>`

## 8. 日常问题处理建议

- 先看启动日志的第一条根因（Root Cause），不要只看最外层报错
- 先排环境（JDK、Profile、数据库连通）再排代码
- 每次改动后先验证健康检查接口，确保基础链路不被破坏
- 发现通用问题及时补充本手册，沉淀为团队知识库

## 9. 后续迭代建议

下一阶段建议新增：

- 数据库初始化脚本目录规范（`db/migration`）
- 统一 API 文档（接口清单 + 示例请求响应）
- 日志规范（请求日志、异常日志、链路追踪字段）
