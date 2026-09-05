# Web 管理端与 H5 本人端契约

状态：v1 外部门面基线；只定义客户端可见的业务 API，不把内部事件/设备通道暴露给浏览器。

## 1. 前缀和边界

| 用途 | 外部/内部前缀 | 调用方 | 允许的下一跳 |
|---|---|---|---|
| Web 管理 | `/platform/lock/v1`（逻辑管理入口 `/lock`） | Smart 管理后台、管理员 token | `smart-platform` 兼容门面 → `smart-lock` 领域服务。 |
| H5 本人 | `/platform/lock/me/v1`（逻辑本人入口 `/lock/me`） | 微信 H5、本人 token | `smart-platform` 兼容门面专例 → `smart-lock` 领域服务。 |
| 内部事件/命令 | `/internal/lock/v1/*` | `smart-platform`、`smart-lock`、`smart-bridge-lock` 服务身份 | 仅内网服务调用；不经过浏览器和用户网关路由。 |

部署若已有网关 external prefix（例如 `/api`、租户前缀或版本前缀），只能在网关层包裹上述外部资源；H5 本人路由是 platform 门面下的 `/lock/me` 专例，不能另设 `/app` 服务前缀，也不能把外部 `platform` 与 `internal` 互换。门户不得直连 TCP、设备 IP、gateway URL 或 `smart-bridge-lock`。

客户端继续使用 platform 兼容门面，页面不判断“旧服务还是新服务”；切换模式由服务端返回。`LEGACY_ONLY`、`SHADOW`、`CUTOVER_FREEZE`、`NEW_ONLY` 均需有可显示的安全结果，不能靠前端开关绕过。

## 2. 通用请求与响应

### 2.1 通用请求头/字段

| 字段 | Web | H5 | 规则 |
|---|---|---|---|
| `Authorization` | 必填 | 必填 | Smart 现有用户 token；服务端解析 subject、租户和权限。 |
| `requestId` | 写请求必填 | 写请求必填 | 本次 HTTP 意图的唯一标识，重试可复用。 |
| `idempotencyKey` | 设备状态写入必填 | 密码/动态码写入必填 | 同一身份、目标和 payload 摘要下重复提交返回原命令/请求，不创建第二次设备效果。 |
| `expectedVersion` | 修改、解绑、配置和危险动作必填；创建可省略 | 修改本人凭据时使用服务端状态返回的并发令牌，不能自行伪造人员版本 | 缺失或冲突返回 `EXPECTED_VERSION_REQUIRED`/`VERSION_CONFLICT`，不得静默覆盖。 |
| `reason` | 人工写操作必填 | 不允许自行伪造操作者原因 | 进入审计；敏感内容不得写入 reason。 |
| `parkId`、`staffId`、`deviceId` | 可作查询/目标候选 | **禁止作为身份依据** | Web 由 platform 数据权限和 lock scope 裁定；H5 忽略或拒绝客户端提供的人员/设备标识。 |

门锁页面不另存 token，也不改变或重做 Smart 现有认证态及其存储规则；密码、动态码、人脸原图、卡号、指纹模板、网关密钥不得放入 URL、日志、分析 query、门锁业务缓存、浏览器 localStorage/sessionStorage 或普通审计文本。密码输入仅在当前请求和受控服务端密文引用生命周期内存在；除下文明确的受控一次性 reveal 响应外，成功/失败响应都不回显明文。

### 2.2 通用响应 envelope

```json
{
  "code": 0,
  "message": "已受理，等待设备确认",
  "traceId": "trc_01J...",
  "requestId": "req_01J...",
  "data": {
    "status": "PENDING_PROVISION",
    "commandId": "cmd_01J...",
    "commandStatus": "WAITING_ACK",
    "transportAccepted": true,
    "deviceConfirmed": false,
    "nextAction": "POLL_STATUS"
  },
  "error": null
}
```

写请求的 `2xx` 只表示业务请求被校验并持久化/受理：通常为 `202 Accepted`；读取为 `200 OK`。只有响应中的 `deviceConfirmed=true` 且 `status` 为相应终态，才可展示设备已确认。下列错误语义对 Web/H5 均适用：

`code/message` 保留 Smart 现有响应兼容语义，新字段放在既有 envelope 或 `data` 中；必须经过当前 Web axios 拦截器、H5 request 封装的集成测试，不只测 DTO。旧路径在保留期继续返回其已冻结的 `code/data/msg` 等形态，由服务端兼容门面适配，不要求未升级客户端猜新字段；但本人身份和园区校验不得为兼容放宽。涉及旧接口无法表达的异步结果，需要明确等待/拒绝语义和版本退出条件，不能返回旧“成功”冒充设备已生效。

| HTTP/业务错误 | 含义 | 客户端行为 |
|---|---|---|
| `401 AUTH_REQUIRED` | token 缺失、过期或服务身份不完整 | 重新登录/刷新 token，不重放敏感 payload。 |
| `403 SCOPE_DENIED` | 园区、人员、设备或本人范围不允许 | 不泄露对象是否存在；Web 隐藏越权数据，H5 显示本人范围帮助。 |
| `409 VERSION_CONFLICT` | 目标或住宿版本已变化 | 重新查询，要求用户重新确认，不能覆盖他人变更。 |
| `409 DUPLICATE_REQUEST` | 幂等键已有不同 payload | 展示冲突并使用新 requestId/idempotencyKey；不生成第二条设备命令。 |
| `422 CAPABILITY_UNVERIFIED` | 型号/profile/固件能力无足够证据 | 显示“设备能力未验证”，拒绝真实下发；不得降级为成功。 |
| `423 CUTOVER_FROZEN` | 影子、冻结或单活切换门禁阻止写入 | 展示排队/暂停原因和下一步，不静默丢弃住宿变更。 |
| `429 RATE_LIMITED` | 本人密码、人脸或动态码请求限频 | 显示剩余等待或帮助，不重复提交。 |
| `503 DEVICE_UNAVAILABLE` | 设备/网关离线或没有可用执行租约 | 写意图仍须可审计；根据命令状态显示待处理/人工核验。 |
| `409 RECONCILIATION_REQUIRED` | 物理结果未知、迟到/矛盾回执或切换差异 | 禁止伪成功；展示受控人工处理入口。 |

## 3. Web 管理 API（八组旧操作的安全复刻）

### 3.1 资源与公开路径

以下是实现任务可直接引用的逻辑路径；旧 `/device`、`/permissions`、`/record` 等路径只作迁移映射，不是新接口。每个写接口都必须返回 `traceId`、资源/批次 ID、当前业务状态和（如有）`commandId`/`commandBatchId`。

| 旧操作组 | v1 公开资源 | 方法与用途 | 最小权限/结果 |
|---|---|---|---|
| WEB-L-001 门锁列表/详情 | `/platform/lock/v1/assets`、`/platform/lock/v1/assets/{deviceId}` | `GET` 列表/详情；`POST /{deviceId}/actions/open` 远程开门；`GET/PATCH /{deviceId}/config` 配置 | 园区范围 + 设备操作权限；开门/配置返回命令状态，不能把请求受理当物理完成。 |
| WEB-L-002 设备管理 | `/platform/lock/v1/devices`、`/platform/lock/v1/devices/{deviceId}` | `GET` 分页；`PATCH` 编辑备注/可管理字段；解绑、启停、管理员密码等能力未验证时返回 `CAPABILITY_UNVERIFIED` | 设备园区范围、版本冲突、影响授权/待执行命令的二次确认。 |
| WEB-L-003 网关/型号 | `/platform/lock/v1/gateways`、`/platform/lock/v1/models` | `GET` 查询；`POST/PATCH` 网关准入/关联和型号能力维护 | 网关服务身份和园区权限；删除、地址修改须另有切换门禁与回读证据。 |
| WEB-L-004 人员/凭据 | `/platform/lock/v1/persons`、`/platform/lock/v1/credentials` | `GET` 人员/凭据摘要；`POST` 受控导入/凭据操作；导入结果含失败清单 | 只返回必要脱敏字段；密码、完整卡号、指纹模板不可回显或导出。 |
| WEB-L-005 授权管理 | `/platform/lock/v1/grants`、`/platform/lock/v1/grants/{grantId}` | `GET` 查询；`POST` 创建/续期；`PATCH` 编辑；`POST /{grantId}/revoke` 撤权；`POST /{grantId}/reprovision` 重新授权 | 新增/编辑/重新授权要求当前有效 `membershipId`；撤权可针对已关闭 membership 的历史 grant，由服务端按历史 credential/slot 与当前其他有效引用校验；结果显示 `PENDING_PROVISION`/`PENDING_REVOKE` 等真实状态。 |
| WEB-L-006 下发结果 | `/platform/lock/v1/commands`、`/platform/lock/v1/commands/{commandId}` | `GET` 命令/尝试/回执分页和详情；`POST /{commandId}/actions` 受控重试/取消/人工结论 | 仅允许与管理员 scope 匹配的对象；按 `commandId + attemptNo` 展示传输与设备确认时间线。 |
| WEB-L-007 记录/日志 | `/platform/lock/v1/records/open`、`/records/password`、`/records/communications` | `GET` 分页/详情；`POST /export` 服务端生成受审计导出 | 园区范围、时间/设备/人员筛选；密码只显示掩码/摘要，通信只显示结构化脱敏摘要。 |
| WEB-L-008 告警/设置 | `/platform/lock/v1/alarms`、`/alarms/actions`、`/alarm-settings` | `GET` 告警；`POST` 批量处理；`GET/PATCH` 园区低电量/离线设置 | 批量未选、部分失败、整数校验、园区越权均显式失败；处理告警不等于设备异常消失。 |

旧 Web 八组的筛选、分页、空态、加载、详情、确认、导入/导出和禁用入口必须逐项对版。允许的差异仅包括：Smart 动态菜单/资源码、服务端命令确认语义、敏感字段脱敏和已知能力 `UNVERIFIED` 的受控不可用；不得借口“新工作台”删掉旧管理员可完成的操作。

### 3.2 Web 权限与状态

1. platform 门面从 Smart token 解析操作者、租户、角色和园区数据范围；客户端提供的 `parkId` 只能作为筛选候选，不能扩大范围。`smart-platform` 负责入口和园区裁定，`smart-lock` 对内部 subject/scope 再验证后执行领域规则。
2. 管理员可看到的人员、房间、设备、网关和记录均必须按园区 scope 过滤；猜测 ID、导出 URL 或直接调用内部接口不能越权。
3. Web 将业务状态与命令状态分开显示：授权显示 `PENDING_PROVISION`、`ACTIVE`、`PENDING_REVOKE`、`REVOKED`、`RECONCILIATION_REQUIRED`；命令显示 `QUEUED`、`DISPATCHED`、`WAITING_ACK`、`SUCCEEDED`、`RETRY_PENDING`、`FAILED`、`EXPIRED`、`CANCELLED`、`RECONCILIATION_REQUIRED`。
4. `transportAccepted=true, deviceConfirmed=false` 的命令必须显示“传输已受理/等待设备确认”，不显示“已授权/已撤权/已开门”。无回执、超时或断线进入等待、重试或对账，不进入成功。
5. 页面操作审计至少关联 `traceId`、`requestId`、操作者 subject、园区 scope、目标、原因、命令/事件 ID、前后状态和脱敏摘要；原始 TCP、密钥和明文凭据不进页面、导出或普通日志。

## 4. H5 本人 API

一期只承载员工本人宿舍门锁状态和受控密码/动态码流程，不承载设备、网关、人员、授权、撤权、告警、通信日志、迁移或切换管理。旧 `/key/myKey`、`/key/record` 的完整凭据/远程开门能力不因旧 bundle 存在而自动上线；是否纳入后续迭代另立需求和安全门禁。

### 4.1 公开路径

| 路径 | 方法 | 请求最小字段 | 返回/状态 |
|---|---|---|---|
| `/platform/lock/me/v1/status` | `GET` | 无人员、设备或 badge 参数 | 本人有效住宿/房间的最小摘要、授权状态、最近更新时间、帮助类别和服务端 `expectedVersion` 并发令牌；状态为 `ACTIVE`、`PENDING_PROVISION`、`PENDING_REVOKE`、`REVOKED` 或 `RECONCILIATION_REQUIRED`。 |
| `/platform/lock/me/v1/password-commands` | `POST` | `requestId`、`idempotencyKey`、`newPassword`、当前 status 返回的 `expectedVersion`、（如需）一次性 `verificationRef` | `202` 返回 `commandId`、`commandStatus`、`transportAccepted`、`deviceConfirmed=false`；不返回旧密码或明文新密码。 |
| `/platform/lock/me/v1/password-reveals` | `POST` | `requestId`、`idempotencyKey`、一次性 `verificationRef`、固定用途 `VIEW_CURRENT_PASSWORD` | 仅在当前 membership/凭据可受控查看且服务端验证通过时返回一次性短时结果；响应带 `Cache-Control: no-store`，只允许页面内存显示，不产生修改命令。 |
| `/platform/lock/me/v1/dynamic-code-requests` | `POST` | `requestId`、`idempotencyKey`、用途和一次性身份校验引用 | 只有能力/合规门禁已通过（非 `UNVERIFIED`）才可受理；返回短时请求 ID 和到期时间，不在 URL/日志持久化动态码。 |
| `/platform/lock/me/v1/requests/{requestId}` | `GET` | 路径 requestId 必须属于 token subject | 返回本人请求状态/失败类别/帮助入口；不泄露其他人的存在性或设备细节。 |

一期不提供 `/platform/lock/me/v1/open`。本人远程开门须另有业务授权、设备协议、限频、防重放、现场/真机和隐私评审；在门禁关闭前返回 `CAPABILITY_UNVERIFIED` 或不注册入口。管理员远程开门若获后续批准，只能走 Web 管理门面和 `DEVICE_ACTION` 命令，使用设备资产 `expectedVersion`、管理员 scope 与短时授权，不能伪造本人 membership。

`password-reveals` 的服务端规则是强制性的：`verificationRef` 必须由认证/校验服务签发，且 subject 与当前 token 一致、用途严格为 `VIEW_CURRENT_PASSWORD`、`membershipId` 与当前有效住宿一致、未超过短 TTL 且未被消费。lock 服务只保存不可逆消费摘要和审计关联，不保存明文密码；成功响应只返回当前一次性 reveal 所需的短时结果（含 `revealId`、`expiresAt` 和受控密码字段），并设置 `Cache-Control: no-store`，前端只在组件内存展示，卸载/登出/超时立即清除。重复 request/idempotency key 不再次返回秘密，而是返回 `REVEAL_ALREADY_CONSUMED`；`password-commands` 和普通 request 查询永远不回显密码。

### 4.2 本人身份、住宿与页面状态

- 服务端以 token subject（员工身份、租户和 scope）为唯一本人依据，并读取当前有效住宿/`membershipId`；前端 `badge`、query、路由、缓存、请求体中的 `staffId`/`deviceId` 均不是权威。若请求带了不一致的人员字段，应拒绝或忽略并记录安全事件，不能按它查询。`expectedVersion` 由服务端从本人状态返回并校验，客户端不得用 staff/room/device 标识拼造版本。
- `parkId`、`roomId` 和设备关系从服务端住宿/绑定事实派生；H5 不接收管理员 scope，也不展示其他住户或网关地址。
- `ACTIVE` 才能显示本人当前可用结果；`PENDING_PROVISION`/`PENDING_REVOKE` 显示“确认中”；`REVOKED`、无有效住宿、冻结和 `RECONCILIATION_REQUIRED` 显示真实限制与帮助，不伪造动态码或通行成功。
- 修改密码、动态码刷新、调宿、退宿、授权撤销、登录身份变化、登出和模式切换后，客户端必须清除短时结果并重新读取；页面保留最后成功时间时标注为历史数据。
- 当前旧页面“新密码 6 位数字、不能与原密码相同”的体验校验可以保留，但最终复杂度、频率、设备能力、有效住宿、幂等和设备确认由服务端裁定，不能把前端校验当安全边界。

### 4.3 H5 安全响应

| 场景 | API/页面结果 |
|---|---|
| 伪造其他 badge/personId/deviceId | `403 SCOPE_DENIED` 或按本人资源不存在处理；不返回他人数据。 |
| 尚未入住、已退宿、冻结 | `200` 返回受限状态或明确业务错误；不创建设备命令。 |
| 密码请求重复 | 返回同一 `commandId`/当前状态；不新增第二次下发。 |
| 仅传输层成功/无设备回执 | 显示 `WAITING_ACK`/对账中，不显示密码已生效。 |
| 人脸/一次性校验失败、超时、限频 | 分开显示校验失败、超时、限频和系统故障；不保留原图或动态码；token 继续由既有认证态管理，门锁页不得复制到业务缓存。 |
| 设备能力未验证/模式冻结 | `CAPABILITY_UNVERIFIED`/`CUTOVER_FROZEN`；不得回退到旧 URL 或旧账号。 |

## 5. 客户端不得承担的决定

客户端可以渲染服务端返回的状态和安全文案，但不得：

1. 通过隐藏按钮代替 Web 园区权限，或使用前端 `parkId`/`badge` 让服务端换查询对象；
2. 以 HTTP 200、按钮 loading 结束、网关写入或 `transportAccepted` 推断设备已确认；
3. 直连 bridge/TCP、拼装 `wireTaskId`、重放旧接口或切换旧服务 URL；
4. 在门锁页另存密码、动态码、完整卡号、指纹/人脸材料、认证 token 或原始协议报文；认证 token 仍沿用 Smart 既有认证态，不新增第二套存储/认证机制；
5. 在能力 `UNVERIFIED`、单活未建立、切换冻结或回执未知时自行重试物理命令；
6. 把旧设计/旧页面的明文 debug、免鉴权接口、硬编码人员标识或“请求成功即完成”作为兼容要求。
