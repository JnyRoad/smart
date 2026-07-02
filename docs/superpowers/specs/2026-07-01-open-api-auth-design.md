# 开放 API 鉴权框架设计

- 日期：2026-07-01
- 状态：已定稿（三轮 Codex 独立评审，2026-07-01 双方一致；待业务方最终确认）
- 关联：《入厂申请照片拉取与下发状态回写设计》（同日 spec，依赖本框架）

## 1. 背景与目标

系统后续要对外提供 API 接口（第三方应用、MCP 服务等对接）。当前所有接口只支持平台用户 token，缺少面向「应用」的机器对机器（M2M）鉴权能力。本设计在现有 Spring Security OAuth2 栈上补齐开放 API 能力，第一个接入方为许昌打印机电脑上的 FileReceiver（照片拉取，见关联 spec）。

**目标**：
1. 第三方应用凭 `client_id/client_secret` 通过标准 OAuth2 `client_credentials` 模式获取 token 调用开放接口；
2. 开放接口按 scope 做细粒度授权，与现有用户权限体系互不干扰；
3. 应用全生命周期可管理（注册、scope 分配、secret 重置、启停）；
4. 开放接口调用有结构化审计日志。

**非目标（本期不做，记为已知风险）**：
- 按 client 的网关限流（内网 + 接入方可控，正式对公网开放前必须补）；
- 开放平台门户 / 开发者自助注册；
- HTTPS 全链路改造（现状内网 HTTP，随基础设施规划另行处理）。

## 2. 现状盘点（可复用的既有设施）

| 设施 | 位置 | 现状 |
|---|---|---|
| OAuth2 授权服务器 | `smart/smart-auth`（`AuthorizationServerConfig`） | 已启用，`tokenEnhancer` 已预留 `client_credentials` 分支 |
| 客户端存储 | `sys_oauth_client_details` 表 + `SmartClientDetailsService`（JDBC + Redis 缓存） | 已有 |
| 客户端管理接口 | `smart-upms`：`OauthClientDetailsController` | 已有 CRUD |
| 客户端管理页面 | `smart-ui/src/views/admin/client/index.vue` | 已有 |
| 资源服务器 | `smart-common-security`（`SmartResourceServerConfigurerAdapter` 等） | 各业务服务已是资源服务器，校验 Bearer token |
| Token 存储 | RedisTokenStore | 已有 |

结论：**凭证签发、token 校验、客户端管理全部现成**，本框架的增量只有「开放 API 的 scope 授权层」和管理增强。

## 3. 设计

### 3.1 凭证与令牌

- **命名规范（硬性要求）**：所有用户可见层面——管理页面字段、对接文档、错误提示、审计日志字段——一律使用「**App ID / App Secret**」（对标飞书开放平台），**禁止出现「客户端 ID」等 OAuth2 内部术语**。底层存储仍是 `sys_oauth_client_details.client_id/client_secret`，token 端点参数名按 OAuth2 标准保持 `client_id/client_secret`（各语言标准库自动处理），对接文档中注明「App ID 对应请求参数 client_id」一处即可。换 token 调用的流程与飞书 `tenant_access_token` 完全同构，但走标准 OAuth2 协议——MCP 规范、各语言 OAuth2 客户端库、Postman 均原生支持，无需自研 SDK。
- 开放应用在 `sys_oauth_client_details` 注册：`authorized_grant_types=client_credentials`，`scope` 填其被授权的开放 scope 列表（逗号分隔），`access_token_validity` 按应用设置（FileReceiver 建议 12h）。
- 应用调用 `POST /oauth/token`（grant_type=client_credentials）换取 access token，调用开放接口时携带 `Authorization: Bearer <token>`。
- **secret 存储（Codex 评审确认为前置验收项）**：现状已核实——`SecurityConstants.CLIENT_FIELDS` 对查询出的 `client_secret` 强制拼接 `{noop}`，即库中明文。对外发放 AppSecret 前必须完成 BCrypt 迁移：查询 SQL 去掉 `{noop}` 拼接、新建/重置走 BCrypt（`{bcrypt}` 前缀）、存量客户端（含 smart-ui 等登录客户端）一次性迁移脚本 + 回滚脚本，放 `smart-module/database/manual/`。迁移影响现有前端登录链路，需在测试环境全量回归后再上生产。

### 3.2 权限模型（scope）

- **命名约定**：`open:<业务域>:<资源>:<动作>`，如 `open:admittance:photo:read`。所有开放 scope 必须带 `open:` 前缀，与内部用途 scope 区分。
- **应用 token 默认拒绝（Codex 评审阻断项，deny-by-default）**：现有资源服务器全局规则仅 `anyRequest().authenticated()`，大量业务接口无方法级权限——应用 token（client_credentials）若不加拦截可直接调用普通业务接口。因此在 `smart-common-security` 增加**全局过滤器**：认证为 client-only（`OAuth2Authentication.isClientOnly()`）时，仅放行标注了 `@OpenApi` 的接口，其余一律 403。业务代码零改动，默认安全。
- **`@OpenApi` 注解 + 切面**（新增于 `smart-common-security`，所有资源服务可用）：
  - 用法：`@OpenApi("open:admittance:photo:read")` 标注在开放接口方法上；
  - 校验当前认证满足：① `OAuth2Authentication.isClientOnly()`（应用身份判定不依赖 token 附加信息，授权服务器对 client token 不增强 claim）；② token scope 包含注解声明的 scope。任一不满足返回 403（含结构化审计日志）；
  - 普通用户 token（password/授权码等模式）访问 `@OpenApi` 接口一律 403——开放接口只认应用身份。
- **数据范围绑定（Codex 评审阻断项）**：应用凭证绑定数据范围（如 `allowedParkIds`，存 `sys_oauth_client_details.additional_information` JSON 字段，不加列）。开放接口的数据范围**由服务端从应用配置推导，不信任请求参数**；请求参数与绑定范围不符时 403。
- `@Inner`（服务间内部调用）机制不受影响，两者正交。
- `@OpenApi` 接口必须携带有效 token（不加入 `PermitAllUrlProperties` 白名单），授权只在 scope 层放行。

### 3.3 应用管理增强

- UPMS / `admin/client` 页面补充（页面标题与字段标签改为「应用管理 / App ID / App Secret」）：
  - **scope 分配**：编辑界面支持从「已登记开放 scope 字典」多选（字典先用 Nacos 配置或常量类维护，YAGNI，不建表）；
  - **secret 重置**：生成 32 位随机 secret，明文只在重置响应中返回一次，库中存 BCrypt；
  - **停用**：本期不加状态位列——停用即删除 `sys_oauth_client_details` 记录，并同步清除 Redis 中该 client 的缓存与已签发 token。**现有代码无按 clientId 批量吊销能力**（删除仅 `removeById` + 清 client 缓存），需新增 `revokeByClientId`（基于 `RedisTokenStore.findTokensByClientId` 逐个吊销）；验收标准：删除应用后，其旧 access token 调开放接口必须立即 401/403，不允许等自然过期。重新启用=重新注册。
- **审计日志**：`@OpenApi` 切面统一输出结构化日志（clientId、URI、结果、耗时、来源 IP），走现有日志体系，不建审计表（本期）。

### 3.4 网关

- 开放接口走现有网关路由与全局 token 校验，架构不动；
- 限流不做（见非目标），在网关侧为将来预留：审计日志字段足够支撑后续按 clientId 限流的观测需求。

## 4. 错误处理

| 场景 | 行为 |
|---|---|
| token 缺失/无效/过期 | 401（现有资源服务器行为） |
| scope 不足 / 用户 token 访问开放接口 | 403 + 审计日志（切面统一处理） |
| client 被停用 | 现有 token 立即失效（清 token store + client 缓存），后续换 token 失败 |
| /oauth/token 凭证错误 | 400/401（Spring Security OAuth 标准响应） |

快速失败：切面校验失败直接抛授权异常，禁止降级放行。

## 5. 测试

- **unit**：`@OpenApi` 切面判定矩阵（client token 有/无 scope、用户 token、匿名、多 scope）；client-only 全局过滤器判定；数据范围推导与不符拒绝；secret 重置的 BCrypt 存储与一次性明文返回；
- **integration**：
  - client_credentials 换 token → 带 token 调 `@OpenApi` 测试端点（200）→ 缺 scope（403）→ 用户 token（403）→ 停用后（401）；
  - **应用 token 越权（阻断项回归）**：client token 调用未标注的普通业务接口（如 `/admittance/apply/page`、`/repeat/auth`）必须 403；
  - **吊销**：删除应用后旧 token 立即 401/403；
  - **数据范围**：绑定园区 A 的应用请求园区 B 数据 → 403；
- **回归**：现有用户登录、菜单权限、`@Inner` 调用不受影响；BCrypt 迁移后全部既有登录客户端可正常登录（测试环境全量回归）。

## 6. 风险

- Spring Security OAuth 栈已停维（随全系统版本，不在本期升级），新增代码遵循其扩展点，避免深度魔改；
- 无按 client 限流，恶意/失控应用可打满接口——对公网开放前必须补齐；
- 内网 HTTP 明文传输 token 与照片（PII），与现状一致，随基础设施规划处理。

## 7. 决策记录

- Codex 评审建议「首期只做固定 scope + 绑定园区的极简版，完整管理面等第二个调用方再抽象」；业务方（旅途）明确要求本次一次性交付完整开放能力（后续 AI 模型 / CLI / MCP / 第三方对接都走此框架），故保留完整设计。scope 字典用常量维护（不建表）已是最简实现。

## 8. 交付边界

本 spec 只含鉴权框架（smart-common-security、smart-upms、smart-ui 客户端页、数据库脚本）。FileReceiver 接入与照片接口见关联 spec，依赖本框架先行合并。
