# 项目总览

## 项目身份

| 字段 | 值 |
|------|---|
| 中文名 | 云视-裕同智慧家园 3.0 |
| 目录 | `smart/` |
| Maven 坐标 | `com.tce:smart:yuto-3.0-RELEASE` |
| 业务归属 | 许昌裕同 IT 信息化 |
| 当前生产园区 | 许昌园区 |
| 系统定位 | 智慧园区平台的 **统一身份与权限中台** + **API 网关** |
| 版本通道 | yuto-3.0-RELEASE（生产线） |

## 一句话定义

`smart/` **不是** 一套完整的智慧园区应用，而是为“智慧家园 3.0”平台中的业务模块**统一提供登录、Token 签发、权限校验、动态网关路由与组织/用户/角色/菜单管理** 的基础服务层。

## 相关目录

`smart/` 只负责基础平台，相关业务和前端目录如下：

| 目录 | 角色 | 与 `smart/` 的关系 |
|------|------|-----------|
| **smart/**（本目录） | 基础服务：网关 + 认证 + UPMS + 公共组件 | — |
| **smart-module/** | 业务微服务（smart-app、smart-platform、smart-data、smart-bridge*、smart-algorithm、smart-push、smart-dispatcher、smart-schedule 等） | 通过 Maven 依赖 `smart-common-*` 和 `smart-upms-api`；通过网关路由接入 |
| **smart-ui/** | 管理端前端（Vue 2 + Element UI） | 经 smart-gateway 调用基础平台和业务 API |
| **smart-h5/** | 微信 H5（Next.js + React） | 经 smart-gateway 调用基础平台和业务 API |

项目目录总览见 [../../../README.md](../../../README.md)。

## 目录组成

```
smart/
├── smart-gateway/         API 网关（Spring Cloud Gateway，端口 9990）
├── smart-auth/            认证中心（OAuth 2.0 AuthorizationServer，端口 3000）
├── smart-upms/
│   ├── smart-upms-biz     UPMS 业务服务（端口 4000）
│   └── smart-upms-api     UPMS 对外暴露的 Entity / Feign Client
└── smart-common/          公共组件
    ├── smart-common-bom        依赖版本统一
    ├── smart-common-core       工具/异常/统一返回
    ├── smart-common-data       MyBatis-Plus 增强
    ├── smart-common-security   OAuth2 资源服务器 + 权限切面
    ├── smart-common-gateway    动态路由（基于 Redis + Nacos）
    ├── smart-common-log        操作日志 AOP
    └── smart-common-swagger    Swagger 2.9 自动配置
```

详细模块职责见 [02-modules/module-index.md](../02-modules/module-index.md)。

## 关键非功能特性（生产实测）

- **多园区**：通过 `sys_user_park` 表支持单账号归属多个园区（M:N），登录时 Token 内嵌 `parkList`。
- **多登录方式**：账号密码、短信验证码、人脸识别（OCR）、微信公众号、友互通（YHT）、社交登录。
- **动态路由**：网关路由配置存储在 `sys_route_conf` 表 + Redis，通过 Nacos 事件实时刷新，无需重启网关。
- **统一审计**：所有写操作通过 `@SysLog` 注解异步落库到 `sys_log` 表。
- **链路追踪**：Sleuth + Zipkin。
- **配置加密**：Jasypt 对配置文件中的敏感字段（数据库密码等）加密。

## 当前阶段

- 生产已部署许昌园区，运行中，目前仅服务于许昌园区。
- 当前主要技术债：Spring Boot 2.1.3 / Spring Cloud Greenwich 已 EOL，存在升级压力。详见 [12-risks/tech-debt.md](../12-risks/tech-debt.md)。

## 利益相关方

| 角色 | 关注点 |
|------|--------|
| 园区运营/管理员 | 用户开关、角色授权、园区切换、登录方式管理 |
| 接入子系统开发 | OAuth Client 申请、Token 解析、权限码定义 |
| 许昌裕同 IT 信息化 | 平台演进、合规审计、安全与稳定 |
| 运维 | 部署拓扑、Nacos / Redis / DB / Zipkin 健康、日志归集 |
