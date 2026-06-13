# ADR-001：采用 Nacos 作为统一注册中心 + 配置中心

- 状态：Accepted（已落地，生产运行中）
- 日期：约 2020（推断；具体决策日期已不可考，本文档为回溯）
- 决策者：原项目架构组

## 背景

智慧家园 1.0/2.0 采用 Spring Cloud Netflix 体系（Eureka 注册 + Spring Cloud Config 配置）。3.0 重构时面临选择：
- 继续 Eureka + Config Server；
- 切换 Alibaba Nacos 单实例承担两职。

## 决策

采用 **Nacos**。namespace 固定 `eda914a9-b100-427b-9d37-4d7da89b841f`，按 group 区分环境（dev/test/prod）。`common.yml` 作为共享配置。

## 理由

1. **运维简化**：一套中间件、一套控制台，替代 Eureka + Config Server + Bus。
2. **配置动态刷新**：原生支持 `@RefreshScope`，比 Config Server + Bus 链路短。
3. **支持动态网关路由**：通过 Nacos Listener 监听变更，触发 `RedisRouteDefinitionWriter` 重载，无须重启。
4. **国内社区与文档**：阿里出品、中文文档完善、与团队既有阿里云体系契合。
5. **统一与集团其他系统**：集团其他 Java 项目已选 Nacos。

## 后果

正面：
- 部署体量缩小；
- 配置变更秒级生效；
- 动态路由解耦了网关与业务上线。

负面：
- 与 Spring Cloud Greenwich 的兼容性需要锁定 SCA `2.1.4.RELEASE`，升级耦合度高（详见 [TD-001](../12-risks/tech-debt.md)）；
- Nacos 单实例故障会同时影响发现与配置；建议至少集群三节点；
- docker-compose 中仍残留 `smart-eureka` / `smart-config`，已成为遗留干扰项（[TD-003](../12-risks/tech-debt.md)）。

## 相关

- [04-architecture/system-context.md](../04-architecture/system-context.md)
- [10-delivery/build-and-deploy.md](../10-delivery/build-and-deploy.md)
- [ADR-003](ADR-003-dynamic-gateway-route.md)
