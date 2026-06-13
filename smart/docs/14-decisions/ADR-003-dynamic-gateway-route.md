# ADR-003：动态网关路由（DB + Redis + Nacos 事件）

- 状态：Accepted（生产运行中）
- 日期：约 2020（推断）

## 背景

智慧园区生态中业务子系统（门禁/停车/访客/能耗…）持续接入，每个子系统都需要新增网关路由。如果路由配置写死在 `application.yml`：

- 每次接入 / 调整都要改配置、重新打包、重启网关；
- 网关重启会中断所有入口请求；
- 多环境配置漂移风险高。

## 决策

采用 **DB + Redis + Nacos 事件** 三件套：

1. `sys_route_conf` 表持久化路由配置；
2. smart-upms-biz 的 `/route` API 提供 CRUD，写 DB 同时写 Redis；
3. 写完后通过 Nacos 发布配置变更事件；
4. smart-gateway 通过 `RedisRouteDefinitionWriter` 监听事件，热加载路由。

## 理由

- **零停机**：路由变更秒级生效，不重启网关；
- **可审计**：DB 留痕；
- **快速读取**：网关从 Redis 读取，避开数据库压力；
- **配置一致**：DB/Redis/网关内存三处同步，运维操作面单一（管理 UI）。

## 后果

正面：
- 接入新业务子系统只需在管理 UI 加路由 + 配置 OAuth Client，无须重新部署网关；
- 调整 `predicates` / `filters` 即时生效。

负面：
- 三处存储一致性依赖发布/订阅链路稳定（DB/Redis/Nacos 任一故障均影响刷新）；
- 路由变更风险大（写错路径可能瞬间影响所有用户），需评审 + 灰度策略；
- 网关启动顺序敏感：需先连上 Redis 与 Nacos。

## 相关

- [04-architecture/system-context.md](../04-architecture/system-context.md)（数据流-动态路由刷新）
- [07-api/api-index.md](../07-api/api-index.md)（RouteConfController）
- [06-data/data-model.md](../06-data/data-model.md)（sys_route_conf）
