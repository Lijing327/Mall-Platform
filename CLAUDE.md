# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

单店自营商城 MVP。一期范围为单店交易内核；商家/多租户能力已在结构上预留，但不在当前演示路径中。

三种用户角色与对应门户：
- **用户（商城端）** — 浏览商品、购物车、下单、地址簿
- **商家端** — 入驻申请、商品管理、订单发货
- **管理后台** — 商家审核、订单/商品/用户管理

## 常用命令

### 后端（需要 Java 17，使用 Maven）

```bash
cd backend

# 以开发/测试模式启动（默认 profile）
mvn spring-boot:run

# 以生产模式启动
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 打包 JAR
mvn clean package -DskipTests
```

### 前端（Node/npm）

```bash
cd frontend

npm install
npm run dev      # 开发服务器，地址 http://localhost:5173（/api 代理至 localhost:8080）
npm run build
npm run preview
```

## 架构

### 技术栈

| 层级 | 技术 |
|---|---|
| 前端 | Vue 3 + Vite 5 + Vue Router 4 + Axios |
| 后端 | Spring Boot 3.3.4，Java 17 |
| ORM | MyBatis-Plus 3.5.7 |
| 数据库 | SQL Server（MSSQL JDBC） |

### 后端分层结构

```
Controller → Service → Repository（MyBatis-Plus Mapper）→ Entity
```

- **`common/`** — `Result<T>` 统一响应体、`BizException`、`GlobalExceptionHandler`、`ResultCode`
- **`auth/`** — `TokenService`（HMAC-SHA256）、`AuthInterceptor`、`AuthContextHolder`（ThreadLocal）
- **`config/`** — `MallAuthProperties`、`MallFeatureProperties`（功能开关）

所有接口均返回 `Result<T>`：
```json
{ "code": 0, "message": "成功", "data": { ... } }
```

### 认证机制

无状态 HMAC-SHA256 Token，格式：`Base64(userId|role|过期时间).Base64(签名)`

- Token 存储于 `localStorage`，键名 `mall_mvp_token`
- 所有受保护请求携带 `Authorization: Bearer <token>`
- 公开路径（无需 Token）：`GET /api/auth/login`、`GET /api/health`、`GET /api/products`、`GET /api/products/{id}`
- 管理员校验：`/api/admin/**` 需要 ADMIN 角色；管理员用户 ID 在 `mall.auth.admin-user-ids` 中配置
- `AuthContextHolder.get()` 可在请求生命周期内任意位置获取当前 `AuthPrincipal`（userId、role）

### 前端认证与状态管理

- 无 Pinia/Vuex，所有认证状态通过 localStorage 管理（见 `user-context.js`）
- 路由守卫位于 `router/index.js`，使用 `meta.requiresAuth` 和 `meta.requiresAdmin`
- 自定义事件：`mall-auth-changed`、`mall-merchant-profile-changed`

### API 路径规范

- 用户/商家端：`/api/{resource}` 或 `/api/{resource}/{id}/{action}`
- 管理后台：`/api/admin/{resources}`（复数形式，如 `/api/admin/merchants`、`/api/admin/orders`）

## 配置说明

### 后端环境变量

**test profile（默认）** — 本地开发凭据硬编码于 `application-test.yml`：
- `MALL_TLS_LEGACY_SQL_SERVER=true` — 针对仅支持 TLS 1.0 的旧版 SQL Server

**prod profile** — 所有敏感配置从环境变量读取：
```
DB_PROD_URL、DB_PROD_USERNAME、DB_PROD_PASSWORD
MALL_AUTH_TOKEN_SECRET    # 32 位以上 HMAC 签名密钥
MALL_AUTH_LOGIN_PASSWORD  # 所有用户共用的登录密码
```

### JDK 版本强制要求

Maven enforcer 插件**强制要求 JDK 17**（范围 [17, 18)），版本不符则构建失败。需在三处保持一致：IDE 项目 SDK、Maven Runner JDK、`JAVA_HOME`。

### 功能开关（`application.yml`）

`mall.feature.*` 下的开关（`multi-shop`、`split-order`、`settlement`）在 MVP 阶段均为 `false`。多店铺相关表（`shop_order`）已创建但未激活。

## 数据库

Schema 与种子数据脚本位于 `db/` 目录。默认数据库：测试环境 `mall_platform_test`，生产环境 `mall_platform_prod`。

种子数据用户 ID：管理员=1，普通用户=1001，商家=2001。自营店铺=1，商家店铺=2。示例商品 ID：101、102、103。

SQL 日志：测试环境输出至 stdout，生产环境关闭（配置项：`mybatis-plus.configuration.log-impl`）。

## 关键设计决策（来自项目记忆）

- `merchant` 包作为平台化扩展预留保留，收口单店自营时不得删除
- 地址簿已完整实现：`user_address` 表、增删改查、默认地址、下单时选择地址

## 语言偏好

请始终使用中文与我沟通，包括代码注释和文档。
