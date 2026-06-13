# 技术债与风险登记

> 优先级：🔴 严重（影响安全/可用性） · 🟠 高 · 🟡 中 · ⚪ 低
> 状态：`open` / `mitigating` / `accepted` / `resolved`

## 登记表

| ID | 严重度 | 标题 | 状态 | 影响 |
|----|-------|------|------|------|
| TD-001 | 🔴 | Spring Boot 2.1.3 已 EOL | open | 无安全补丁；新版漏洞持续暴露 |
| TD-002 | 🔴 | XStream 1.4.14 存在反序列化 CVE | open | 若处理 XML 输入可被 RCE（CVE-2020-26259 等） |
| TD-003 | 🟠 | docker-compose 中 Eureka/Config 服务为遗留 | open | 维护混乱；新手易误启动 |
| TD-004 | 🟠 | Spring Security OAuth 2.3.4 已停止维护 | open | 长期需迁移到 Spring Authorization Server |
| TD-005 | 🟠 | 无 CI / CD 配置 | open | 发布过程依赖人工，易出错 |
| TD-006 | 🟠 | 无 DB Migration 工具（Flyway/Liquibase） | open | 表结构与代码漂移、无版本审计 |
| TD-007 | 🟡 | Hystrix 已停止开发 | open | 长期应迁移 Resilience4j |
| TD-008 | 🟡 | Java 8 公开支持已结束 | open | 升级到 17/21 是大工程 |
| TD-009 | 🟡 | Velocity 1.7、Curator 2.10.0、Hutool 4.4.5 等老依赖 | open | 维护性差，潜在 CVE |
| TD-010 | 🟡 | 根 pom 的 maven-compiler-plugin skip=true 隐藏行为 | open | 易让新成员误判构建流程 |
| TD-011 | 🟡 | bootstrap.yml 中 `NACOS_PORT` 默认 `8761`（Eureka 端口） | open | Nacos 默认 8848，默认值不一致；若环境变量缺失会失败 |
| TD-012 | 🟡 | 无统一业务错误码体系 | open | 一律 `code=1`，前端难做差异化提示 |
| TD-013 | 🟡 | 无数据级权限框架 | open | 多园区/部门隔离全靠业务子系统自觉 |
| TD-014 | 🟡 | 无 Prometheus 业务指标 | open | 可观测性局限于链路与健康 |
| TD-015 | 🟡 | 数据库类型未在仓库内显式声明 | open | 新人需查 Nacos 才能搭本地环境 |
| TD-016 | 🟡 | 个人信息字段（手机号/头像/openid）无 PIPL 合规检查记录 | open | 合规风险 |
| TD-017 | ⚪ | Swagger 2.9.2 已过时 | open | 应升级到 springdoc-openapi |
| TD-018 | ⚪ | Jasypt 主秘钥管理流程未文档化 | open | 运维交接风险 |

## 升级路线建议（高层）

短期（1–2 个月，无破坏性变更可立刻做）：

- [TD-002] 排除/升级 XStream；
- [TD-003] 清理 docker-compose 中的 Eureka/Config 服务块；
- [TD-005] 补一套 Jenkins/GitLab CI 流水线；
- [TD-006] 引入 Flyway 并把现有 DDL 反向生成 baseline；
- [TD-011] 修正默认 Nacos 端口为 8848；
- [TD-018] 把 Jasypt 主秘钥发放/轮换流程写入运维手册。

中期（3–6 个月，可控范围内升级）：

- [TD-007] Hystrix → Resilience4j；
- [TD-017] Swagger → springdoc-openapi 3；
- [TD-009] 老依赖逐个升到最近稳定版；
- [TD-012] 设计业务错误码段并改造关键 API。

长期（一年内整体升级）：

- [TD-001] [TD-004] [TD-008] 同步升级 Spring Boot 3.x + Java 17/21 + Spring Authorization Server，按 [ADR-001](../14-decisions/ADR-001-nacos-over-eureka.md) 等记录决策。

## 已接受的风险

暂无（待与业务方/运维一起评审后填入）。
