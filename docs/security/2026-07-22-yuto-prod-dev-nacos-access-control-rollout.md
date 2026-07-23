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

## Platform 与 UPMS 精确收口审批记录

下表必须分别完整填写后才能发布对应 Data ID。空白表示尚未取得生产发布证据，不能据此写入 Nacos；禁止以恢复业务通配匿名白名单作为回滚手段。

| 必填项 | `smart-platform.yml` | `smart-upms-biz.yml` |
|---|---|---|
| 兼容版本 commit / 镜像 |  |  |
| 灰度实例 / 全量实例 |  |  |
| 旧接口 QPS=0 观察窗口（起止时间） |  |  |
| Nacos 发布前 MD5 / 历史版本 |  |  |
| Nacos 发布后 MD5 / 历史版本 |  |  |
| 仓外调用方清零或迁移确认（审批人） |  |  |
| `/actuator/health` 探针与认证业务回归结果 |  |  |
| 回滚 Data ID 历史版本 |  |  |

本地基线只保留 `/actuator/health`。Platform 的二维码 `/code` 若未完成短时单用途签名令牌方案，不得加入匿名白名单；在此之前必须保持认证后访问。

## 必须执行的探针

1. 无 Token 访问员工旧路径、新路径、门锁旧路径、物品放行旧路径：401 或 403，响应不含敏感字段。
2. 带伪造 `from=Y` 的外部 Gateway 请求：401 或 403。
3. 合法 Smart UI 人员搜索：仅返回人员 ID、工号、姓名、部门。
4. 合法当前 Smart H5 入住、门锁、物品放行：完成业务流，浏览器请求不出现身份证、住址或他人工号。
5. 合法 Smart App、UPMS、Feign 和定时任务：成功且日志不含完整员工对象。
6. 设备、厂商回调：仅在有效签名、时间窗和 nonce 条件下成功。

## 全局 Nacos 精确收口硬门槛

每次任何 Data ID 发布前，必须在**拟发布 commit** 的仓库根目录执行：

```bash
node scripts/security/check-nacos-ignore-urls.mjs docker/nacos/config/dev
```

命令必须以退出码 `0` 结束。它动态扫描当前 Data ID，不以历史“剩余数量”或人工摘录替代；任意通配匿名路由、未知白名单项或脚本失败均阻断发布。完成一项模块整改并不表示可单独绕过仍未收口的其他 Data ID。

公开业务例外必须逐路径登记并具备各自的认证边界：例如简历资料提交 `/regist/save/identification` 与人脸裁剪 `/regist/face/crop` 不得用 `/regist/**` 泛化放行，后者只能使用短时、单用途 capability。例外的协议、调用方和回归证据必须进入本清单的单 Data ID 发布记录。

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

## UPMS 用户资料接口分阶段切换

为避免认证、Platform 或 App 在滚动发布期间中断，按以下不可跳过的顺序执行：

1. 先在隔离环境登记 `SMART_AUTH`、`SMART_PLATFORM`、`SMART_APP` 三个独立 client_credentials 客户端，并以对应服务令牌验证 `/internal/user/**` 的 client_id、`server` scope 与 purpose 均匹配；错误 client、用户 token、错误 purpose 必须拒绝。
2. 发布 UPMS 兼容镜像：新 `/internal/user/**` 已可用，旧 `/api/user/**` 只作为短期观测兼容路径；Nacos 不得再匿名放行旧路径。记录每个旧路径 QPS、调用方服务和响应码。
3. 依次灰度并全量发布 `smart-auth`、Platform、App 的新 Feign 契约；每批发布后验证登录、手机验证码、组织管理员、员工离职和手机号同步。任何服务令牌失败只能回滚该调用方镜像或 Nacos 历史版本，不能恢复通配匿名白名单。
4. 旧路径连续一个完整业务观察窗口 QPS 为零、无未知调用方，且新路径探针均通过后，发布最终 UPMS 镜像删除 `/api/user/**`、`/social/info/**` 与旧 Feign 契约。本分支的最终代码对应此阶段。

若第 2 步发现仓外调用方，停止第 4 步，先为该调用方实现最小 DTO、专属 client_id 和 purpose；不得以保留完整 `UserInfo` 响应作为兼容方案。

## 回滚规则

回滚优先级为：当前调用方镜像 → 当前 Data ID 历史版本 → 灰度实例下线。禁止通过恢复 `/staff/**`、`/articlesrelease/**`、`/api/**` 或 `/**` 解决故障。若必须临时恢复兼容，恢复的是上一版“认证 + 最小字段”的精确路由配置。
