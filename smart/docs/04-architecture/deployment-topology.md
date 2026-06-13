# 部署拓扑

> 本文档描述生产部署的实际形态。具体主机清单与 IP 见许昌裕同 IT 运维手册，不在本仓库内。

## 服务清单

| 服务 | 镜像 | 端口 | 副本（当前） | 启动命令 |
|------|------|------|-------------|---------|
| smart-gateway | smart-gateway:yuto-3.0 | 9990 | 1 | `java -Dspring.profiles.active=$APP_ENV -jar smart-gateway.jar` |
| smart-auth | smart-auth:yuto-3.0 | 3000 | 1 | 同上 |
| smart-upms | smart-upms:yuto-3.0 | 4000 | 1 | 同上 |

容器基础镜像统一为 `anapsix/alpine-java:8_server-jre_unlimited`，时区固定 `Asia/Shanghai`，日志统一挂载到宿主机 `/home/tce/smart/logs`。

## 中间件依赖

| 中间件 | 用途 | 备注 |
|--------|------|------|
| **Nacos** | 服务注册 + 配置中心 | namespace=`eda914a9-b100-427b-9d37-4d7da89b841f`，group=`dev`/`test`/`prod` 按环境切换，shared-config=`common.yml` |
| **Redis** | OAuth Token（前缀 `smart_oauth:`）、动态路由、业务缓存 | 单实例（具体集群形态见运维） |
| **关系数据库** | 业务数据（sys_* 表） | 类型在仓库未声明，由 Nacos `common.yml` 注入；历史 compose 注释出现过 `smart-oracle`，需对照实际确认 |
| **Zipkin** | 链路追踪 | 由 spring-cloud-sleuth-zipkin 上报 |
| **Spring Boot Admin** | 服务健康监控 | 通过 spring-boot-admin-starter-client 注册 |

## docker-compose 现状

仓库根 `docker-compose.yml` 描述了一套早期编排，包含 `smart-eureka` 与 `smart-config`，**但当前生产已切换 Nacos**，这两个服务实际不再启动，属于遗留配置。详见 [ADR-001](../14-decisions/ADR-001-nacos-over-eureka.md) 与 [tech-debt.md](../12-risks/tech-debt.md#td-002-docker-compose-leftover)。

## 启动顺序

```
Nacos (前置)
  → Redis、数据库（前置）
    → smart-upms-biz
    → smart-auth         （并行）
    → smart-gateway      （最后，依赖 Nacos 中已注册的服务）
```

## 环境矩阵

| 环境 | APP_ENV | Nacos Group | 用途 |
|------|---------|-------------|------|
| dev | dev | dev | 本地/开发联调 |
| test | test | test | 测试 |
| prod | prod | prod | 生产（许昌园区） |

环境切换通过容器环境变量 `APP_ENV` 完成，bootstrap.yml 中所有连接参数（Nacos URL/PORT/GROUP）均从环境变量读取。

## 待补充

- 反向代理 / 负载均衡（Nginx? SLB?）层未在仓库内描述。
- 多副本与高可用方案缺失。
- 备份与灾备策略需运维补充。
