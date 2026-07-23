# yuto_prod/dev Nacos 访问控制发布清单

**用途：** 此文件是生产 Nacos 人工发布、灰度与回滚的版本化清单；不包含 Nacos 内容、账号、密码、Token、数据源或真实个人信息。

## 发布范围

| Data ID | 当前已验证风险 | 收口前置条件 | 灰度探针 | 生产状态 |
|---|---|---|---|---|
| `smart-platform.yml` | `/staff/**`、`/articlesrelease/**` | Tasks 2-5、7 完成，仓外调用方为零或已迁移；App 住宿内部列表 client-id 门槛完成 | 旧路径无 Token 返回 401/403；当前 H5、UI、App 通过 | 未发布 |
| `smart-upms-biz.yml` | `/api/**` | Open API App scope 核验完成 | 无 App 身份返回 401/403；合法 App token 成功 | 未发布 |
| `smart-data.yml` | `/**` | Controller 清单逐条完成分类 | 内部 Feign 成功；外部直连拒绝 | 未发布 |
| `smart-algorithm.yml` | `/**` | 人脸、OCR 调用方完成服务令牌迁移 | 内部算法调用成功；外部直连拒绝 | 未发布 |
| `smart-push.yml` | `/**` | 推送回调签名与调用方清单完成 | 签名回调成功；无签名拒绝 | 未发布 |
| `smart-dispatcher.yml` | `/**` | 所有调度 Feign 标识和令牌验证完成 | 定时任务和 Feign 成功 | 未发布 |
| `smart-schedule.yml` | 本地基线为 `/**`；生产需再次只读核验 | Nacos 读取核验与任务清单完成 | 定时任务无失败 | 未核验 |
| `smart-bridge-biz-*.yml` | `/**` | 每个设备、厂商回调签名和来源确认 | 合法设备回调成功；未签名拒绝 | 未发布 |
| `smart-bridge-isc*.yml` | `/**` | 每个设备、厂商回调签名和来源确认 | 合法设备回调成功；未签名拒绝 | 未发布 |

## 单 Data ID 发布记录

每次只填写一个 Data ID，先灰度一个实例；未完成本表任何项目不得扩大。

| 项目 | 填写值 |
|---|---|
| Data ID / Group / Namespace |  |
| 发布前 MD5 / 历史版本号 |  |
| 发布后 MD5 / 历史版本号 |  |
| 兼容代码 commit / 镜像 |  |
| 服务 OAuth 发布前门槛证据（不含秘密） |  |
| 灰度实例数 / 全量实例数 |  |
| 开始时间 / 结束时间 / 执行人 |  |
| 网关日志观察窗口 |  |
| 旧接口 QPS |  |
| 内部 Feign 失败数 |  |
| 设备或第三方回调失败数 |  |
| UI / 当前 H5 / App / UPMS 回归结果 |  |
| 回滚 Data ID 历史版本 |  |
| 运维审核人 / 业务审核人 |  |

## 必须执行的探针

1. 无 Token 访问员工旧路径、新路径、门锁旧路径、物品放行旧路径：401 或 403，响应不含敏感字段。
2. 带伪造 `from=Y` 的外部 Gateway 请求：401 或 403。
3. 合法 Smart UI 人员搜索：仅返回人员 ID、工号、姓名、部门。
4. 合法当前 Smart H5 入住、门锁、物品放行：完成业务流，浏览器请求不出现身份证、住址或他人工号。
5. 合法 Smart App、UPMS、Feign 和定时任务：成功且日志不含完整员工对象。
6. 设备、厂商回调：仅在有效签名、时间窗和 nonce 条件下成功。

## 兼容代码部署前的服务 OAuth 硬门槛

以下项目必须在部署任何含 `INTERNAL_SERVICE_AUTH_REQUIRED` 调用方的生产兼容镜像**之前**完成并留存不含秘密的证据：

1. 每个存在 `INTERNAL_SERVICE_AUTH_REQUIRED` Feign 契约的调用服务，均在对应 Data ID 配置独立
   `security.inner.service-token.client-id`、`client-secret` 与 `access-token-uri`；不得复用
   `security.oauth2.client` 的用户 OAuth 资源。密钥仅通过受管环境变量或密钥系统注入，不写入本清单。
2. 授权服务器已登记独立客户端，仅授予 `client_credentials` 的 `server` scope；在预发或隔离灰度用同一不可变镜像和受管密钥来源验证能获取该 token。
3. 预发或隔离灰度已验证标记的 App/Feign 调用成功、错误客户端/错误 scope/缺失配置拒绝，以及应用日志不记录 Authorization 或访问令牌。
4. 错误 scope、过期令牌、缺失配置或授权服务器不可用时，调用必须在 Feign 拦截器阶段失败，不能发送下游 HTTP 请求。任一项未完成时，不得部署标记调用方，也不得以 `AUDIT`、`ENFORCE` 或放宽匿名路由绕过。
5. 对 `GET /dormitory/staff/internal/self/roomDetail/{staffBadge}` 和
   `GET /dormitory/staff/internal/roomList/{staffBadge}`，`smart-platform.yml` 与 App 服务配置必须受管配置
   `security.inner.dormitory.app-client-id`、`security.inner.dormitory.app-room-purpose`；两项分别精确等于发起
   Feign 调用的 App 服务令牌 `client_id` 与受审用途。任一值缺失、为空、client_id 或用途不匹配，或 token
   不是纯 client_credentials 主体时均必须拒绝；灰度探针必须分别留存合法 App 调用成功和通用 `server`
   scope 客户端被拒绝的证据。此项未通过不得发布 App/Platform 兼容镜像。
6. `GET /dormitory/staff/internal/roomDetail/{staffBadge}` 仍返回完整住宿详情，只能配置
   `security.inner.dormitory.admin-room-detail-client-id` 和
   `security.inner.dormitory.admin-room-detail-purpose` 对应的专用管理员服务调用；任何未受管客户端或用途必须
   拒绝。若生产核查不存在该完整内部接口消费者，应在完整灰度窗口后删除该路由和 Feign 契约，而非向 App 回退。

## `security.inner.mode=ENFORCE` 后置收口

仅在上述服务 OAuth 硬门槛、兼容代码灰度和单 Data ID Nacos 精确收口均通过后，才允许从 `AUDIT` 切换 `ENFORCE`。`ENFORCE` 只负责对 `@Inner` 端点执行内部调用语义的硬拒绝；它不负责准备服务 OAuth 客户端、注入密钥或验证 token。

## 回滚规则

回滚优先级为：当前调用方镜像 → 当前 Data ID 历史版本 → 灰度实例下线。禁止通过恢复 `/staff/**`、`/articlesrelease/**`、`/api/**` 或 `/**` 解决故障。若必须临时恢复兼容，恢复的是上一版“认证 + 最小字段”的精确路由配置。
