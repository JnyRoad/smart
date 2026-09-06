# 新平台技术基线

**状态**：已确认。
**生效范围**：本文件约束 Smart 此后新建或重构的业务服务、Web、桌面和客户端工程，并已适用于当前开发中的 `smart-app` App；不要求重写、迁移或停止现有 Java、Oracle、H5、管理端和已发布接口。
**决策日期**：2026-09-05。
**决策原则**：后续新业务优先复用本基线。确需偏离时，先记录架构决策、风险、回滚方案和验证结果。

## 1. 总体技术栈

| 层级 | 统一选择 | 责任与边界 |
| --- | --- | --- |
| 移动端与 PDA | uni-app x、Vue 3、UTS | Android、iOS、鸿蒙及小程序按平台能力适配；PDA 是直接运行 App 的终端。 |
| Web | Next.js、React、TypeScript | 提供管理、门户和业务页面；页面调用统一 API，不承载核心领域写入。 |
| 桌面端 | Next.js 静态导出 + Tauri | 复用 Web 页面、组件、设计令牌和 API 客户端；桌面容器负责受控的本地打印等系统能力。 |
| API 网关 | 自托管 Envoy Gateway | 部署在内部 Kubernetes，统一处理外部路由、TLS、限流、认证策略和可观测性接入；它是基础设施，不是另一套业务后端。 |
| 新平台后端 | Rust | 新业务 API、领域服务、异步任务、事件消费者和设备适配器均使用 Rust；不新增 Go 或 Next.js/NestJS 业务后端。 |
| 同步通信 | REST/JSON 对外，gRPC 对内 | 对外资源接口以 OpenAPI 为唯一契约；服务内部仅在明确收益时采用 gRPC。 |
| 事务数据 | PostgreSQL + SQLx | PostgreSQL 是新平台的业务事实源；SQL 迁移、约束、索引和事务随服务版本管理。 |
| 缓存与短时状态 | Valkey 或 Redis | 仅用于缓存、限流、会话辅助和短时协调，不取代 PostgreSQL 事实记录。 |
| 事件 | PostgreSQL Outbox + Kafka | 业务事务先写 PostgreSQL 与 Outbox，再可靠投递 Kafka；消费者按事件标识幂等。 |
| 可观测性 | OpenTelemetry + tracing | 每个入口传播 trace、记录结构化日志、指标和审计事件；敏感字段不得进入日志。 |

Rust 服务以 `axum + Tokio` 提供 HTTP，以 `tonic` 提供必要的内部 gRPC，以 `SQLx` 访问 PostgreSQL，以 `rust-rdkafka` 接入 Kafka。`rust-rdkafka` 依赖 `librdkafka`，发布时必须列入软件物料清单和许可证审计。项目不得为了“统一”而把浏览器页面、API 网关或 Kafka 重写成 Rust。

## 2. 目标拓扑

```text
uni-app x / PDA ─┐
Next.js Web ─────┼── HTTPS ── Envoy Gateway ── Rust API ── PostgreSQL
Tauri Desktop ───┘                              │             │
                                                 │             └── Outbox
                                                 ├── Valkey / Redis
                                                 └── Kafka ── Rust workers

设备专用适配器（仅必要时）── mTLS ── Rust 设备接入服务
```

所有公共调用使用一个网关域名和版本根路径。网关到内部服务的路由、服务发现和访问策略不暴露给 App、Web 或桌面端。Kafka 负责异步事实传播，不能替代同步授权判定或事务性查询。

## 3. 客户端与 PDA 决策

### 3.1 移动端和 PDA

PDA 不部署 Rust 边缘代理。其扫码头通常按键盘楔入方式把条码写入当前聚焦的 App 输入框并发送回车；App 直接处理扫码文本、相机权限和业务 API 请求。扫码方式由 App 设置持久保存，且现场页面只读显示当前岗位。

手机和 PDA 摄像头由 uni-app x 的平台适配能力调用。每个目标平台仍须进行真机验收，包括扫码输入、相机拒绝授权、取消、重复扫描和网络中断恢复。

### 3.2 Web 与桌面端

Web 和桌面端统一采用 Next.js、React、TypeScript。桌面端以 Tauri 承载 Next.js 静态导出产物；Tauri 环境没有 Next.js 服务端运行时，因此 Server Actions、动态 Route Handler 和依赖 SSR 的业务功能不得作为桌面功能前提。桌面端通过统一 API 办理业务，本地打印等权限只交由经过后端授权的 Tauri 插件或专用打印适配器处理。

App 与 Web/桌面端共享 OpenAPI 契约、权限码、错误码、设计令牌和测试夹具；不强行共享 Vue 与 React 的页面组件源码。

## 4. Rust 后端边界

新平台的核心业务服务全部使用 Rust，不再新建 Go 服务。Rust 的无垃圾回收运行时、编译期内存与并发安全检查，适合高并发 API、事件消费者和设备接入；实际吞吐、延迟和资源消耗仍以目标负载测试为准，不能仅按语言名称承诺。

设备适配器不是默认部署单元。仅在下列任一条件成立时，才在设备所在网络部署独立的 Rust 适配器：

- 设备只开放局域网私有协议、长连接或轮询接口；
- 设备需要本地证书、串口、USB、驱动或操作系统权限；
- 设备必须在中心网络暂时不可用时执行受控缓冲、回执或状态采集；
- 厂商 SDK 无法由 uni-app x 或 Tauri 安全、稳定地调用。

这类适配器以 mTLS 身份接入中心 Rust 服务，持久化命令、回执和幂等标识，不能直接让终端或浏览器访问设备 IP。若厂商只提供 Java、.NET 或 C/C++ SDK，先验证是否存在稳定协议或 FFI 路径；确实无法避免时，允许一个最小、独立且可替换的厂商适配进程作为例外，但核心业务 API、数据模型和事件处理仍保持 Rust，例外须单独记录决策与退役计划。

## 5. API、身份和权限标准

### 5.1 公共 API

新平台业务 API 统一以 `/api/v1` 为根路径，使用简短、稳定、无歧义的复数资源名。首期资源约定如下：

```text
GET, POST  /api/v1/item-passes
GET        /api/v1/item-passes/{id}
POST       /api/v1/item-passes/{id}/actions
GET        /api/v1/item-passes/posts

POST       /api/v1/visitor-checks
GET, POST  /api/v1/visitor-passes

GET        /api/v1/me
GET        /api/v1/me/apps
```

`item-passes` 表示保密物品放行单，避免使用会与软件版本发布混淆的 `release`。`visitor-checks` 表示厂牌扫描后的资格核验，`visitor-passes` 表示明确进入或离开的通行事件。动作使用单一 `actions` 子资源，服务端依照动作类型、身份、岗位、园区和单据状态裁定。

每个服务在 `contracts/<service>/v1/openapi.yaml` 维护 OpenAPI 契约；错误码、分页、幂等键、审计字段和弃用规则进入同一版本的配套文档。客户端从契约生成或校验类型，不自行猜测请求字段。当前开发的 `smart-app` 已直接使用本节 `/api/v1/**` 标准；现有 Java 服务以新增标准路径和网关精确路由承载它，不修改旧 OA、H5 和后台路径。

### 5.2 身份与授权

新平台认证采用 OIDC/OAuth 2.0 授权码流程与 PKCE。OIDC 发现、授权和令牌端点遵循身份提供方标准路径，不强行置于业务 `/api/v1` 下；`/api/v1/me` 只返回已认证主体的业务身份与授权快照。当前 Java 过渡服务的 `POST /api/v1/sessions` 仅创建 App 会话，后续切换 OIDC 时保持 `GET /api/v1/me` 和业务资源不变。

权限校验始终在 Rust 服务端完成。客户端模块显隐仅改善体验，不能授予权限。每次写入和现场操作都重新核验主体、权限、园区、岗位、有效期、对象状态与幂等键。DHR 正式员工与已核实的外包/派遣工作人员统一进入身份目录；供应商人员不登录 App，厂牌二维码只用于安检人员的访客资格核验。

## 6. 数据、事件与安全规则

- PostgreSQL 写入业务事实、审计记录和 Outbox；禁止在同一业务动作中通过“先写数据库、再直接发 Kafka”实现不可恢复的双写。
- Kafka 事件带稳定事件标识、发生时间、生产者、版本和关联标识；消费者必须幂等，并保留可追溯失败状态。
- API 写操作使用 `Idempotency-Key` 或等价业务幂等键。权限拒绝、并发冲突、资格过期和未知结果均需有稳定错误码。
- 敏感照片、证件、密码、令牌、二维码原文和设备密钥不得写入日志、前端持久存储或公开事件载荷。
- 生产密钥使用 Kubernetes Secret 管理并接入轮换方案；服务间、设备适配器和网关内部调用使用 mTLS 或等价工作负载身份。

## 7. 部署与迁移边界

新平台服务运行于 Kubernetes，并以基础设施即代码管理命名空间、网关、网络策略、配置和扩缩容。每个服务拥有独立部署单元、健康检查、资源限制、迁移策略、回滚版本和仪表盘。

当前 Java/Oracle 系统继续维护和运行。新服务不直接共写旧系统数据库；迁移按业务边界建立受控 API、同步事件或一次性校验迁移，完成一个边界的验收后再切换流量。任何旧接口改名、数据回填、生产切流或设备下发，都需要单独的实施规格和授权。

## 8. 开源与工程治理

优先选择 MIT 许可证依赖；经软件物料清单与法务/安全核查后可采用 Apache-2.0、BSD-2-Clause、BSD-3-Clause 或 PostgreSQL License。GPL、AGPL、SSPL、BSL 等强限制许可证不得引入，除非另有明确批准。即使使用 MIT，也必须保留版权和许可证文本。

每个新 Rust 服务至少提供：OpenAPI 契约测试、权限与幂等测试、PostgreSQL 迁移验证、Outbox/Kafka 集成验证、结构化日志与追踪验证，以及针对实际设备或模拟协议的故障恢复测试。

## 9. 固化前置验证

本基线已作为技术选型生效；下列验证是首次生产项目的上线门槛，而不是未决选型：

1. Rust 服务完成目标并发、连接数、延迟和资源预算的压测；具体 SLO 由业务量、设备数量和可用性目标确定后写入服务规格。
2. 至少一款实际 PDA 和一台手机完成 uni-app x 扫码头、相机、网络恢复和岗位操作验收。
3. 若接入打印机、门禁、定位锁等硬件，完成厂商 SDK/协议、断网、重复回执、证书轮换和现场回滚验证。
4. PostgreSQL 事务、Outbox 和 Kafka 消费幂等在故障注入条件下通过端到端验证。
5. OIDC/PKCE、网关策略、权限裁定、审计与可观测性在 Kubernetes 测试环境完成验收。

## 10. 参考资料

- [Rust 并发模型](https://doc.rust-lang.org/book/ch16-00-concurrency.html)
- [axum](https://github.com/tokio-rs/axum) 与 [Tokio](https://github.com/tokio-rs/tokio)
- [SQLx](https://github.com/launchbadge/sqlx)
- [rust-rdkafka](https://github.com/fede1024/rust-rdkafka)
- [Envoy Gateway](https://gateway.envoyproxy.io/docs/)
- [OpenAPI 3.1.1](https://spec.openapis.org/oas/v3.1.1.html)
- [OAuth 2.0 Security Best Current Practice](https://www.rfc-editor.org/rfc/rfc9700.html)
- [Tauri 的 Next.js 静态导出要求](https://tauri.app/start/frontend/nextjs/)
