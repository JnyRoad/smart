# 运行环境

## 环境矩阵

| 环境 | APP_ENV | Nacos Group | 数据库 | Redis | 用途 |
|------|---------|-------------|--------|-------|------|
| 本地 | dev | dev | 开发库 | 开发 Redis | 本地联调 |
| 测试 | test | test | 测试库 | 测试 Redis | QA 回归 |
| 生产-许昌 | prod | prod | 许昌生产库 | 许昌生产 Redis | 在线服务 |

## 中间件清单（生产）

> 具体主机/IP 由运维管理，本节列出**类别**与**用途**。

| 中间件 | 用途 | 高可用 |
|--------|------|--------|
| Nacos | 服务注册 + 配置中心 | 建议集群部署（当前形态待运维确认） |
| Redis | OAuth Token、动态路由、业务缓存 | 单实例或主从 |
| RDBMS（Oracle/MySQL，待确认） | 业务数据 | 主备 |
| Zipkin | 链路追踪 | 单实例 |
| Spring Boot Admin | 健康监控 | 单实例 |

## 日志

- 容器内日志统一输出到 `/<service>/logs/`，由 Docker 卷映射到宿主机 `/home/tce/smart/logs/<service>/`；
- 建议接入 Filebeat → ELK / Loki 集中归档（仓库内未配置）。

## 监控与告警

| 维度 | 当前 | 缺口 |
|------|------|------|
| 服务存活 | Spring Boot Admin + Actuator | 缺统一告警通道（短信/邮件/钉钉） |
| 链路 | Sleuth + Zipkin | 缺采样率与保留期策略文档 |
| 业务指标 | sys_log 表 | 无业务侧 Prometheus 指标 |
| 主机/容器 | （由集团 IT 体系负责） | - |

## 备份与灾备

- 数据库备份策略由 DBA 维护，建议每日全量 + 增量；
- Redis 不存关键持久化数据（Token 丢失只是需重新登录），无须特殊备份；
- Nacos 配置应**版本化导出**到 Git（运维已操作待确认）。

## 应急

| 故障 | 影响 | 处置 |
|------|------|------|
| Redis 不可用 | 所有用户登录与 Token 验证失败 | 切换备用 Redis、紧急重启 |
| Nacos 不可用 | 配置无法刷新，但已加载实例可继续运行；动态路由不可更新 | 切换 Nacos 备库 |
| smart-auth 故障 | 新登录失败；已存活 Token 仍可用 | 重启或扩副本 |
| smart-gateway 故障 | 全部入口中断 | 立即重启或切流量到备节点 |
| 数据库故障 | UPMS 写操作失败 | 切主备、DBA 介入 |

## 健康检查端点

- `:9990/actuator/health`
- `:3000/actuator/health`
- `:4000/actuator/health`

经 Spring Boot Admin 聚合展示。
