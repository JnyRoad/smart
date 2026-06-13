# smart 基础平台开发文档

本目录保存 `smart/` 基础平台后端的开发文档，描述网关、认证、UPMS 和公共组件的边界。

> **业务 API（智慧家园 App / 园区服务 / 设备桥接 / 算法 / 推送 / 文件等微服务）在同级 [../../smart-module/](../../smart-module/)，本目录不记录业务服务的完整设计。**

本目录覆盖范围：

- smart-gateway（API 网关）
- smart-auth（OAuth 2.0 认证中心）
- smart-upms（UPMS：用户/角色/菜单/部门/字典/园区/路由/客户端/日志）
- smart-common-*（被 `smart-module` 通过 Maven 依赖的公共组件）

## 文档地图

| 序号 | 主题 | 入口 |
|------|------|------|
| 00 | 项目总览 | [00-overview/README.md](00-overview/README.md) |
| 01 | 需求与能力 | [01-requirements/overview.md](01-requirements/overview.md) · [capabilities.md](01-requirements/capabilities.md) · [glossary.md](01-requirements/glossary.md) |
| 02 | 模块边界 | [02-modules/module-index.md](02-modules/module-index.md) |
| 04 | 架构与部署 | [04-architecture/system-context.md](04-architecture/system-context.md) · [deployment-topology.md](04-architecture/deployment-topology.md) |
| 05 | 技术栈 | [05-tech-stack/tech-stack.md](05-tech-stack/tech-stack.md) |
| 06 | 数据模型 | [06-data/data-model.md](06-data/data-model.md) |
| 07 | API | [07-api/api-index.md](07-api/api-index.md) · [error-codes.md](07-api/error-codes.md) |
| 08 | 安全与鉴权 | [08-security/authentication.md](08-security/authentication.md) · [permission-matrix.md](08-security/permission-matrix.md) |
| 10 | 构建与交付 | [10-delivery/build-and-deploy.md](10-delivery/build-and-deploy.md) · [version-control.md](10-delivery/version-control.md) |
| 11 | 运维 | [11-operations/environments.md](11-operations/environments.md) |
| 12 | 风险与技术债 | [12-risks/tech-debt.md](12-risks/tech-debt.md) |
| 14 | 关键架构决策 | [14-decisions/ADR-001-nacos-over-eureka.md](14-decisions/ADR-001-nacos-over-eureka.md) · [ADR-002-oauth2-redis-token.md](14-decisions/ADR-002-oauth2-redis-token.md) · [ADR-003-dynamic-gateway-route.md](14-decisions/ADR-003-dynamic-gateway-route.md) |

## 阅读建议

- 新成员：按 00 → 01 → 02 → 04 顺序通读，再按需查阅 API 与数据模型。
- 接入方（园区业务子系统开发）：直接看 [08-security/authentication.md](08-security/authentication.md) 与 [07-api/api-index.md](07-api/api-index.md)。
- 运维：[11-operations/environments.md](11-operations/environments.md) + [10-delivery/build-and-deploy.md](10-delivery/build-and-deploy.md)。
- 架构升级评审：[12-risks/tech-debt.md](12-risks/tech-debt.md) + 14 系列 ADR。

## 文档维护

- 涉及业务规则的部分（如菜单 / 权限的具体语义、园区数据归属规则）若与产品方/许昌园区实际使用方式有出入，以实际生产配置为准，并回写到对应文档。
- 任何不可逆技术决策（升级 Spring Boot、替换注册中心、切换数据库等）必须新增 ADR：`docs/14-decisions/ADR-NNN-title.md`。
- 新增功能或重大变更前，按 lt-dev-planning Skill 的 Change Planning Workflow 进行影响分析。
