# 生命周期、设备命令与回执内部契约

状态：v1 设计基线；供 `smart-platform`、`smart-lock`、`smart-bridge-lock` 和验收任务引用。

本文件冻结控制面与协议桥之间的业务不变量，不表示代码、Oracle、网关或真机已经实现。协议字段是本地 API/消息版本；升级本地契约不要求修改门锁固件，但每个硬件 profile 仍必须通过独立的真机能力门禁。

## 1. 责任边界与通道

最终接管后只保留一个真实命令执行方：`smart-bridge-lock`；切换前生产仍由旧系统独占执行，新桥默认禁发。不新增消息中间件，逻辑消息可以由内部 HTTP/Feign 或同等服务调用承载，但必须携带同一版本化信封、服务身份和幂等键。浏览器、H5 和门户不得连接 TCP，也不得调用桥接内部接口。

| 合同 ID | 生产者 → 消费者 | 逻辑内部操作/通道 | 语义 |
|---|---|---|---|
| LC-EVT-001 | `smart-platform` → `smart-lock` | `POST /internal/lock/v1/lifecycle-events`（逻辑 `lock.lifecycle.v1`） | 住宿事实提交后的生命周期事件；由 Outbox 至少一次投递。 |
| LC-CMD-001 | `smart-lock` → `smart-bridge-lock` | `POST /internal/lock/v1/commands`（逻辑 `lock.command.v1`） | 仅接收已持久化、不可变的设备命令尝试。 |
| LC-RCP-001 | `smart-bridge-lock` → `smart-lock` | `POST /internal/lock/v1/results`（逻辑 `lock.result.v1`） | 传输结果、设备确认、协议拒绝和未知结果。 |
| LC-EVT-002 | `smart-bridge-lock` → `smart-lock` | `POST /internal/lock/v1/device-events`（逻辑 `lock.device-event.v1`） | 网关健康、设备事件、协议异常；不能直接改变授权终态。 |
| LC-EVT-003 | `smart-platform` → `smart-lock` | `POST /internal/lock/v1/fact-events`（逻辑 `lock.identity-mapping.v1`） | 平台 Outbox 产生的人员身份映射事实；不由入住五事件隐含推断。 |
| LC-EVT-004 | `smart-lock` `RoomBindingService` → `smart-lock` 授权/重算任务 | 同一事务内的版本化绑定事实 + 持久化重算任务（逻辑 `lock.room-device-binding.v1`，不新增 MQ、不要求平台跨表写） | 锁服务自己拥有房间-设备绑定版本；重算时通过平台 API 核对当前住房，不把平台事件当绑定事实源。 |

`/internal/lock/v1/*` 是服务间前缀，不是外部网关前缀；外部 Web/H5 路径见 [web-h5.md](web-h5.md)。实现可把这些逻辑操作适配到当前 Smart 服务发现或 Feign，但不得把内部路径暴露给用户 token、浏览器或设备。

### 1.1 服务身份和单活围栏

1. `smart-platform` 只产生住宿事实，不产生设备命令；`smart-lock` 是授权、命令、回执和审计的业务事实源；`smart-bridge-lock` 只负责已批准命令的网关/协议投递与证据回传。
2. `smart-lock` 校验内部服务身份、租户/园区 scope、信封版本、完整性摘要和有效期；桥接不接受前端字段来决定 `staffId`、`roomId`、`parkId` 或授权。
3. 同一锁存在多个 gateway 候选时，控制面先取得按 `deviceId`/锁地址围栏的唯一执行租约；未持有租约的候选只能上报健康或待处理结果，不能并发发送同一业务命令。
4. 切换前过渡期由旧执行方唯一发送，影子/冻结阶段新模块不得向生产桥接发送；全通信域停止旧执行、完成最终迁移和改址回读后，才允许新执行方 `NEW_ONLY`。禁止“一个锁两个 gateway 同时试发”作为灰度策略。
5. 执行租约丢失、会话身份冲突或无法判断命令是否已出线时，状态进入 `RECONCILIATION_REQUIRED`，不以重新发送消除不确定性。

## 2. 通用信封、标识与版本

所有异步事件、命令投递和回执都使用带时区的时间和整数 `schemaVersion`。字段删除、语义改变、ID 规则改变必须升大版本；同一大版本只允许新增可选字段或新增事件/结果类型。`schemaVersion` 与设备 `protocolProfileId`、固件版本相互独立，不能以升级本地 API 为理由要求刷固件。

### 2.1 生命周期事件信封

```json
{
  "eventId": "evt_01J...",
  "eventType": "ACCOMMODATION_MEMBERSHIP_OPENED_V1",
  "schemaVersion": 1,
  "occurredAt": "2026-09-05T10:00:00+08:00",
  "producer": "smart-platform",
  "traceId": "trc_01J...",
  "aggregateId": "ACCOMMODATION:staff_123",
  "aggregateVersion": 7,
  "staffId": "staff_123",
  "roomId": "room_456",
  "parkId": "park_001",
  "membershipId": "membership_20260905_01",
  "payload": {},
  "payloadHash": "sha256:..."
}
```

字段约束：

- `eventId` 全局唯一、不可变，是事件去重主键；重放继续使用原值。
- 对住宿生命周期事件，`aggregateId` 是稳定的人员住宿生命周期聚合标识，建议形式为 `ACCOMMODATION:{staffId}`。同一人员的调宿、退宿、冻结和再次入住仍按同一人员聚合递增 `aggregateVersion`；不能改用房间 ID 或设备 ID 替代。LC-EVT-004 的房间-设备绑定事实是显式例外，使用独立绑定聚合和 `bindingVersion`，不能冒充住宿生命周期事件。
- `aggregateVersion` 是该人员住宿事实流的单调正整数。平台在住宿事实与 Outbox 同一事务内写入新版本；事务回滚时不得留下可投递的生命周期事件。
- `staffId`、`roomId`、`parkId` 是字符串 API 标识，不把数字化或旧 MySQL 自增键写死进协议。每次有效住宿使用新的 `membershipId`，以隔离再次入住与旧撤权。
- `occurredAt` 是业务事实发生时间；`receivedAt` 只记录接收时间，不能覆盖事实时间排序。所有时间均为带偏移/时区的 ISO-8601 值。
- `payloadHash` 只作完整性摘要；普通日志和前端不得打印完整敏感 payload。人员姓名、手机号、密码、卡号、指纹模板不属于必需信封字段。

### 2.2 事件类型与最小载荷

| `eventType` | 触发事实 | 必需 payload | 控制面动作 |
|---|---|---|---|
| `ACCOMMODATION_MEMBERSHIP_OPENED_V1` | 入住事务提交 | `membershipId`、有效期、`roomId`、目标设备/房间绑定快照 | 为新 membership 创建或更新授权意图，状态从无效进入 `PENDING_PROVISION`。 |
| `ACCOMMODATION_MEMBERSHIP_MOVED_V1` | 同房换床、跨房调宿 | 新旧 `roomId`、新旧绑定快照、同一 `membershipId` | 同房换床不重复发放；跨房必须先取得旧目标撤权的明确设备确认，再创建新目标授权；旧目标离线时进入人工/应急路径，不静默放宽。 |
| `ACCOMMODATION_MEMBERSHIP_CLOSED_V1` | 退宿、离职、有效期结束 | `membershipId`、关闭原因、需撤权的目标快照 | 状态进入 `PENDING_REVOKE`；撤权可引用已关闭 membership 的历史 grant/credential/slot；后续旧新增结果不得重新激活。 |
| `ACCOMMODATION_MEMBERSHIP_FROZEN_V1` | 冻结或风险处置 | `membershipId`、冻结原因、时间边界 | 暂停新增和敏感自助；已有权限按策略撤权或进入人工核验。 |
| `ACCOMMODATION_MEMBERSHIP_UNFROZEN_V1` | 解除冻结 | `membershipId`、恢复原因 | 只有当前版本仍有效时才可恢复发放，不能重放旧事件。 |
| `STAFF_IDENTITY_MAPPING_CHANGED_V1` | 平台人员身份映射/身份状态改变（平台 Outbox） | `staffId`、`identityMappingVersion`、生效时间、变更原因、受影响 `membershipId` | 更新人员引用和身份版本；不得以旧 badge/外部键自动换人或直接发设备命令，必要时由当前 membership 生成显式补偿任务。 |
| `ROOM_DEVICE_BINDING_CHANGED_V1` | `smart-lock` `RoomBindingService` 在自身事务内变更房间、门锁、网关绑定或有效能力 | `bindingId`、`bindingVersion`、旧/新 `roomId`、旧/新 `deviceId`/`gatewayId`、受影响 membership 列表、生效时间 | 锁服务先持久化绑定事实并创建重算任务，再通过平台 API 核对当前住房；围栏旧目标，每个受影响 membership 走明确撤权/新授权顺序，不从入住事件隐含推断设备替换。 |

事件消费规则：

1. 先验证服务身份、`schemaVersion`、`payloadHash` 和 `eventId`，再写 Inbox/处理记录。
2. 只有相同 `eventId` 且不可变载荷摘要一致的精确重投才返回该事件原处理结果，不再创建授权或命令；同 ID 不同载荷须隔离冲突，相同业务事实不能靠生成新 `eventId` 绕过去。
3. 对采用 aggregateVersion 的事件，新 `eventId` 携带小于等于已处理版本时，为本事件持久记录 `STALE_EVENT`/迟到结果，不返回另一事件的成功；事实冲突或无法核实一致性则进入 `RECONCILIATION_REQUIRED`。不得推进或回退当前授权；高于当前版本但存在缺口时先入持久待处理/对账，不跳过缺失版本直接推进物理权限。
4. `membershipId` 是授权关系和撤权围栏，但“当前有效”只适用于新增/修改凭据和新授权。撤权/删钥命令可以针对已关闭 membership，只要能够匹配历史 `grantId + grantRevision`、`credentialId`、物理/逻辑 `credentialSlot` 和目标设备，并重新核验当前仍有效的其他 grant 引用；旧 membership 的迟到新增成功只能记录证据并生成补偿撤权/人工核验，不能使新 membership 或人员状态恢复为 `ACTIVE`。
5. 重放必须带 `replayBatchId` 与环境标志；影子/演练只更新模拟投影和审计，不发送真实设备命令。
6. `STAFF_IDENTITY_MAPPING_CHANGED_V1` 使用人员住宿聚合的 `aggregateId`/`aggregateVersion`，由 platform Outbox 产生；`ROOM_DEVICE_BINDING_CHANGED_V1` 使用独立的 `ROOM_DEVICE_BINDING:{roomId}` 事实聚合和 `bindingVersion`，由 lock 的 `RoomBindingService` 在自身事务中持久化并排队重算。二者是拆分的显式事实记录，不能被五种住宿事件替代；绑定事实不要求额外 MQ 或 platform 跨表写，重算时通过住房 API 核对。
7. 跨房调宿的安全顺序是“旧授权关系撤销、所需物理删钥均获得明确 `DEVICE_ACK` 且旧授权进入 `REVOKED` → 再排队新目标 provision”。仍被其他有效 grant 引用的共享凭据按 3.2 记录 `RETAINED_BY_OTHER_ACTIVE_GRANT`，属于经核验无需删钥，不伪造 ACK。需要实际撤权的旧目标离线、无回执或结果未知时不自动发新房授权，进入 `RECONCILIATION_REQUIRED` 和人工应急通行流程。
8. 绑定事实的 `bindingId` 稳定对应房间绑定聚合，`bindingVersion` 在该聚合单调递增，一版可包含多个设备目标。锁域同事务保存绑定事实、重算任务和本地已知的受影响 membership；提交后才分页核对平台住房，以 `scopeWatermark` 和 `scopeStatus=SEALED` 封存完整范围。不得把跨服务分页当作同事务原子快照；范围缺口、住房版本变化或绑定版本过期时停止新目标 provision，保留可恢复的分项进度和错误。持久字段及 claim/retry 规则见 [数据模型](../data-model.md) 的绑定重算两表。
9. 范围收集与授权执行分阶段领取：`claimPhase=SCOPE_COLLECTION` 可在范围未封存时领取、分页和重试，但不能发送命令；只有范围已封存且绑定版本仍匹配时才可 `claimPhase=EXECUTION`。平台住房读取须提供可复用的稳定快照，或提供所有相关住房变更都递增的水位，并在遍历前、每页和结束时确认一致；变更/快照失效须重新收集，不能混合不同水位的页面。现有 API 若无法提供上述证据，任务保持 `GAP/RECONCILIATION_REQUIRED`，由 T038 补齐只读契约，不能假定普通分页天然一致。
10. 同 `eventId` 异载荷不覆盖 Inbox 的原记录：以原 Inbox 行为审计对象，向 `DL_AUDIT_EVENT` 追加冲突摘要、来源/接收时间及独立不可变的受控 `evidenceRef`，保留原载荷与冲突载荷的对应关系；冲突记录同事务落账且不产生授权或命令。普通审计只存脱敏字段，完整载荷仅保存在批准的证据存储中。

## 3. 授权与设备状态

### 3.1 业务授权状态

| 状态 | 进入条件 | 允许动作 | 终态含义 |
|---|---|---|---|
| `PENDING_PROVISION` | 当前 membership 有效且授权意图已持久化，尚无设备成功证据 | 生成/排队凭据新增或配置命令；查询、取消、对账 | 不是已生效，不向 H5/Web 显示为可用。 |
| `ACTIVE` | 该 `grantId + grantRevision` 所需的**全部**设备凭据均收到明确 `DEVICE_ACK` 成功，且 membership/aggregateVersion 仍有效 | 查询、受控更新、退宿撤权 | 仅表示该授权记录所需凭据全部被设备确认；部分凭据成功仍不得标为 `ACTIVE`。 |
| `PENDING_REVOKE` | 退宿、冻结、调宿、到期或人工撤权已受理 | 只允许撤权/查询/对账，不允许新授权覆盖 | 不是物理撤销完成。 |
| `REVOKED` | 本 grant 关系已撤销，且需删除的凭据均已设备确认/人工核验；若仍有其他有效 grant 共用凭据，则以 `RETAINED_BY_OTHER_ACTIVE_GRANT` 记录无需删钥的可解释结论 | 旧 grant 仅保留审计；当前有效 membership 可创建新的 `grantRevision`/授权记录并重新进入 `PENDING_PROVISION` | 旧 grant 不得复活；membership 已结束时必须使用新 membership。 |
| `RECONCILIATION_REQUIRED` | 无回执、连接断开、重复/矛盾回执、租约丢失、能力未知或迁移差异 | 人工核验、状态读取、受控补偿；禁止伪装成功 | 物理事实未知或需要解释，不能作为通行成功依据。 |

主要转换（不是完整枚举）：`PENDING_PROVISION → PENDING_REVOKE`（未完成即退宿/取消）；核验状态经证据进入对应当前状态；`PENDING_PROVISION → ACTIVE`（该 grant 所需全部凭据设备确认成功）、`PENDING_PROVISION → RECONCILIATION_REQUIRED`（未知/冲突）、`ACTIVE → PENDING_REVOKE`、`PENDING_REVOKE → REVOKED`（所需撤权设备确认成功，或共享凭据无需删除且已记录关系解除）或 `RECONCILIATION_REQUIRED`。旧 `REVOKED` grant 不能复活；若同一 `membershipId` 仍有效，可创建新的 `grantRevision`/授权记录并重新走 `PENDING_PROVISION`，若 membership 已结束则必须使用新的 membership。旧迟到回执不得沿用旧 grant 转回 `ACTIVE`。

### 3.2 授权记录、凭据共享与撤权

1. 授权记录的业务身份至少为 `grantId + grantRevision + membershipId`；由 `DL_ACCESS_GRANT` 保存父记录、`DL_GRANT_TARGET` 保存每个真实设备目标、`DL_GRANT_CREDENTIAL` 保存各目标凭据关系，不能为每个设备创建互不关联的父授权。设备凭据引用为 `credentialId`，不等于密码/卡号等秘密本身，并保留可审计的物理/逻辑 `credentialSlot` 映射。一次授权可能要求多个设备凭据，只有全部必需目标及其必需凭据都确认成功才是 `ACTIVE`。
2. 多个有效 grant 可以引用同一设备上的同一 `credentialId`。撤销其中一个 grant 时，只撤销该 grant 的关系；只有该 `credentialId` 已没有其他当前有效 grant 引用，才可生成物理删钥命令。仍有其他有效 grant 时，物理钥匙必须保留，并在审计中记录 `RETAINED_BY_OTHER_ACTIVE_GRANT`。
3. 重新授权不能复活旧 `grantId + grantRevision`；管理员在同一有效 membership 下重新授权必须生成新 `grantRevision`/新记录，同一 revision 下的多个设备目标共享父授权身份，旧回执按旧 revision 围栏。membership 结束后的新入住必须使用新的 `membershipId` 和授权身份。
4. 撤权不是“当前 membership 有效”检查：已关闭 membership 的撤权必须保留历史 grant、credential、slot 和设备目标，按当前其他有效 grant 引用重新计算是否需要物理删钥；历史关系不存在、slot 已被复用或引用计数冲突时进入 `RECONCILIATION_REQUIRED`，不能盲删。

### 3.3 命令状态

| 状态 | 语义 | 下一步 |
|---|---|---|
| `QUEUED` | 命令已持久化，等待唯一执行租约/网关能力 | 可调度、取消或因过期终止。 |
| `DISPATCHED` | 已交给桥接并登记本次尝试 | 等待桥接传输证据。 |
| `WAITING_ACK` | 已有传输层接收或写出证据，但没有设备结果 | 等待设备回执/超时；不算成功。 |
| `SUCCEEDED` | 设备明确确认本次操作成功，且业务围栏仍匹配 | 推进相应授权/配置状态。 |
| `RETRY_PENDING` | 已证明未出线，或已获得明确设备失败且 profile 证明无未知副作用；operation/profile 批准重试，当前授权、版本和有效期均仍满足 | 产生下一 `attemptNo`；不能无限重试，不适用于未知物理结果。 |
| `FAILED` | 明确的设备失败、协议拒绝或业务不可重试错误 | 保留原因；必要时进入补偿/人工处理。 |
| `EXPIRED` | 超过命令有效期或目标版本已失效 | 不发送、不重放；需要新业务意图。 |
| `CANCELLED` | 在安全发送前由业务或系统取消 | 不得由迟到成功回执恢复业务授权。 |
| `RECONCILIATION_REQUIRED` | 物理结果未知、身份/租约冲突或证据矛盾 | 读状态/人工核验/受控补偿。 |

状态不变量：`WAITING_ACK`、`RETRY_PENDING`、`FAILED`、`EXPIRED`、`CANCELLED`、`RECONCILIATION_REQUIRED` 都不能在 UI 被渲染成物理完成；“无回执”永远不能作为成功。远程开门等非幂等或短时操作在结果未知时不得由恢复任务数小时后自动重放。

## 4. 命令信封、尝试和协议映射

```json
{
  "messageId": "msg_01J...",
  "schemaVersion": 1,
  "commandId": "cmd_01J...",
  "attemptNo": 1,
  "wireTaskId": "wire_64bit_safe_value",
  "targetKind": "CREDENTIAL",
  "commandType": "PROVISION_CREDENTIAL",
  "operation": "ADD_PASSWORD",
  "grantId": "grant_01J...",
  "grantRevision": 2,
  "credentialId": "credential_01J...",
  "credentialSlot": "slot_07",
  "staffId": "staff_123",
  "roomId": "room_456",
  "parkId": "park_001",
  "membershipId": "membership_20260905_01",
  "gatewayId": "gateway_001",
  "deviceId": "device_001",
  "protocolProfileId": "profile:model-fw-protocol",
  "targetAggregateVersion": 7,
  "expectedVersion": 12,
  "operatorAuthorization": null,
  "issuedAt": "2026-09-05T10:00:02+08:00",
  "expiresAt": "2026-09-05T10:05:02+08:00",
  "payloadReference": "secret-ref:...",
  "payloadHash": "sha256:...",
  "traceId": "trc_01J..."
}
```

- `targetKind` 是命令信封的目标分类，不得根据是否带有某个可选字段猜测。允许值为 `CREDENTIAL`（人员凭据）、`DEVICE_ASSET`（门锁资产配置/启停/解绑）、`GATEWAY_ASSET`（网关资产设置/关联）和 `DEVICE_ACTION`（管理员远程开门等危险设备动作）。
- `CREDENTIAL` 命令必须同时有 `membershipId`、`grantId`、`grantRevision`、`credentialId`、`credentialSlot`（或等价的已登记逻辑槽位）以及人员/房间/园区和设备目标。`PROVISION_CREDENTIAL`、修改凭据等新增/修改操作要求 membership 在命令签发时仍有效；`REVOKE_CREDENTIAL`/物理删钥是有意例外，可以使用已关闭 membership 的历史快照，但必须按历史 grant/credential/slot 与当前其他有效引用重新核验。创建型 `PROVISION_CREDENTIAL` 可以不带资产 `expectedVersion`，撤权、替换、修改或其他非创建凭据操作仍必须带当前凭据/slot 关系的 `expectedVersion`。缺少 membership/grant revision 时拒绝，不能用一个虚构的入住实例补齐。
- `DEVICE_ASSET`、`GATEWAY_ASSET` 和 `DEVICE_ACTION` 不得伪造 `staffId`、`roomId`、`membershipId`、`grantId` 或 `grantRevision`。它们必须以实际设备/网关资产为目标，带当前 `expectedVersion` 和服务端签发的 `operatorAuthorization`；后者至少包含管理员 subject、已裁定的园区/资源 scope、允许的权限和不可由客户端延长的 `authorizationExpiresAt`。管理员远程开门使用 `DEVICE_ACTION`，不能伪装成凭据命令。
- `operatorAuthorization` 只在管理员资产/危险动作需要时出现；自动住宿生命周期命令不把管理员 token 或前端 scope 复制进设备命令，而由 `smart-platform` 入口裁定、`smart-lock` 内部再次校验并形成审计关联。桥接只执行已签发的命令，不自行扩大 scope。
- 除纯创建外，`UPDATE`、`REVOKE`、`UNBIND`、`CONFIGURE`、`REMOTE_OPEN`、`DELETE` 等修改、解绑、配置和危险动作均必须携带目标当前 `expectedVersion`；缺失或过期返回 `EXPECTED_VERSION_REQUIRED`/`VERSION_CONFLICT`，不得静默覆盖并发修改。`targetAggregateVersion` 仍只用于人员住宿事实围栏，不能代替设备资产 `expectedVersion`。
- 资产/危险动作的 `authorizationExpiresAt` 和命令 `expiresAt` 都必须是短时有效期，不能由请求方延长；远程开门采用更短的动作有效期。过期、scope 不匹配或 `expectedVersion` 已变化时不出线，进入拒绝/对账，而不是创建伪造 membership。
- `commandId` 是控制面持久化的业务命令标识；同一业务命令的重试仍使用同一 `commandId`，以 `attemptNo` 区分尝试。
- `attemptNo` 从 1 开始递增，不可复用；桥接以 `commandId + attemptNo` 去重并记录每次传输证据。
- `wireTaskId` 是控制面命令到厂商线协议任务关联的独立映射，不等同于 `commandId`，更不能把任意 UUID 硬塞进老协议字段。若 profile 需要 64-bit/8-byte/16 位十六进制槽位，由桥接分配或持久化映射一个协议可编码值，并保存 `commandId + attemptNo ↔ wireTaskId`；无法证明位宽、字符集或回执回传规则时，该 profile 为 `UNVERIFIED`，禁止真实发送。
- 每个 `commandId + attemptNo` 只对应一个 `wireTaskId`。同一次尝试可以产生传输 ACK、多个分包 ACK、最终设备 ACK；Bridge 回传 outbox 以事件 ID 去重，不能对 commandId+attemptNo 建唯一约束从而丢掉后续回执。协议无法安全复用关联号时，下一尝试必须生成新的映射并明确旧尝试为未知/终止；不能借旧线任务号制造“看似同一”的确认。
- `payloadReference` 指向受控密文/短时秘密存储；普通日志只保存类型、长度和摘要。服务间不传明文密码、完整卡号、指纹模板或可重放原始报文。
- 命令快照必须按 `targetKind` 包含创建时的目标事实：`CREDENTIAL` 必须有 `grantId`、`grantRevision`、`credentialId`、`credentialSlot`、`membershipId`、目标设备、园区和 `targetAggregateVersion`；新增/修改快照对应当前有效 membership，撤权/删钥快照可对应已关闭 membership 但必须保留历史关系和当前引用校验结果；资产/危险动作必须有目标设备或网关、园区、`expectedVersion` 和未过期的 `operatorAuthorization`。桥接只能执行快照，不可自行从平台查询另一人员或房间，也不能把缺失的人员字段补成默认值。
- 上述快照由 `DL_COMMAND` 不可变字段及受控、脱敏的授权声明引用持久保存，不能仅依赖重启后查询当前表重建，也不保存可重放 bearer token。每次发送/重试仍需重新核验当前权限、目标版本和有效期，核验记录归 `DL_COMMAND_ATTEMPT`；快照用于追溯原意图，不取代当前授权门禁。

### 4.1 命令接收响应

桥接在完成服务身份和字段校验后返回 `RECEIVED`、`QUEUED`、`REJECTED` 或 `DUPLICATE`。这只证明桥接处理了命令消息，不等于 TCP 写入、网关接收或设备执行成功。控制面将同步响应与异步回执分别记录，不能用 HTTP 2xx 代替设备结果。

## 5. 回执/事件信封与确认分层

```json
{
  "eventId": "evt_01J-result",
  "eventType": "LOCK_COMMAND_RESULT_V1",
  "schemaVersion": 1,
  "occurredAt": "2026-09-05T10:00:04+08:00",
  "receivedAt": "2026-09-05T10:00:04+08:00",
  "producer": "smart-bridge-lock",
  "traceId": "trc_01J...",
  "commandId": "cmd_01J...",
  "attemptNo": 1,
  "wireTaskId": "wire_64bit_safe_value",
  "resultType": "DEVICE_ACK",
  "transportAccepted": true,
  "deviceConfirmed": true,
  "deviceOutcome": "SUCCESS",
  "resultCode": "DEVICE_OK",
  "gatewayId": "gateway_001",
  "deviceId": "device_001",
  "protocolCorrelationRef": "protocol-ref-redacted",
  "payloadHash": "sha256:..."
}
```

字段/结果规则：

| 结果类型 | `transportAccepted` | `deviceConfirmed`/`deviceOutcome` | 控制面结论 |
|---|---:|---|---|
| `RECEIVED`/`QUEUED` | 未知或 false | 未知 | 命令继续排队/调度。 |
| `TRANSPORT_ACK` | true | false/unknown | `WAITING_ACK`；绝不推进 `ACTIVE`/`REVOKED`。 |
| `DEVICE_ACK` 成功 | true（通常） | true/`SUCCESS` | 仅在 membership、聚合版本和执行租约仍匹配时进入成功状态。 |
| `DEVICE_ACK` 失败 | true（通常） | true/`FAILURE` | `FAILED` 或按错误策略进入 `RETRY_PENDING`；保留设备错误码。 |
| `PROTOCOL_REJECT` | false/unknown | false/unknown | `FAILED` 或能力 `UNVERIFIED`；不得伪造成功。 |
| `UNKNOWN_AFTER_DISCONNECT` | unknown | unknown | `RECONCILIATION_REQUIRED`；禁止对未知远程开门自动重放。 |
| `GATEWAY_EVENT`/`DEVICE_EVENT` | 不适用 | 不适用 | 仅更新通信/审计投影，除非能通过命令关联提供明确设备结果。 |

`transportAccepted` 表示 TCP/网关层已经接收或可证明写出；`deviceConfirmed` 表示设备协议有明确、可关联且未被业务围栏淘汰的结果。二者必须是不同字段和不同审计证据。没有设备回执、只有连接成功、只有 HTTP 200、只有网关“已发送”都不能标为 `SUCCEEDED` 或 `ACTIVE`。

### 5.1 至少一次、去重和对账

1. Outbox、内部调用和桥接回传均按至少一次投递；消费者先按 `eventId` 去重，再按 `commandId + attemptNo` 和 `wireTaskId` 检查重复/冲突。
2. 同一回执重复到达时返回已记录结果；相同关联号但 payload/hash/设备身份冲突时进入 `RECONCILIATION_REQUIRED` 和安全告警。
3. 进程重启不得丢失 `QUEUED`、`DISPATCHED`、`WAITING_ACK`、`RETRY_PENDING` 或待投递回执；恢复扫描必须尊重 `expiresAt`、membership 和单活租约。
4. 设备不承诺物理 exactly-once。系统承诺的是业务消息至少一次、数据库幂等、协议关联可追溯、未知结果显式对账。断线、崩溃或回执丢失场景只有持久证据证明未出线，且当前授权/版本/有效期和 `operation`/profile 安全重试门禁均满足，才允许 `RETRY_PENDING`；已经写出、是否写出未知或关联号不确定则进入 `RECONCILIATION_REQUIRED`，不生成新物理发送。明确的设备失败是另一条已知结果路径，只有 profile 证据排除未知副作用并批准该错误码重试才可受控重试；不能借此放行无回执场景。
5. 迟到 `DEVICE_ACK` 先按命令/尝试去重，再检查当前 membership 与聚合版本；过期、取消或撤权后的新增成功不得恢复授权，只能触发补偿撤权或人工核验。

### 5.2 网关健康与设备事件

`LC-EVT-002` 的 `eventType` 只能取已注册的 `GATEWAY_HEALTH_V1`、`DEVICE_EVENT_V1`、`PROTOCOL_ERROR_V1` 或新增版本类型，最小载荷如下：

```json
{
  "eventId": "evt_01J-device",
  "eventType": "DEVICE_EVENT_V1",
  "schemaVersion": 1,
  "occurredAt": "2026-09-05T10:01:00+08:00",
  "receivedAt": "2026-09-05T10:01:01+08:00",
  "gatewayId": "gateway_001",
  "deviceId": "device_001",
  "protocolProfileId": "profile:model-fw-protocol",
  "deduplicationKey": "device_001:protocol-ref:...",
  "eventCode": "DOOR_OPENED",
  "commandId": "cmd_01J...",
  "protocolCorrelationRef": "protocol-ref-redacted",
  "safeSummary": {"length": 24, "hash": "sha256:..."},
  "traceId": "trc_01J..."
}
```

健康/设备事件只更新通信投影、告警和审计；只有同时具备合法 `commandId + attemptNo`、未冲突的 `wireTaskId` 和明确设备结果时，才可作为命令回执参与授权状态机。非法帧、未知 profile、身份冲突、重复键冲突和频率超限进入隔离/告警，不直接改变授权终态；`safeSummary` 不能替代原始敏感报文，也不能让前端重放。

`DL_DEVICE_EVENT` 持久保存 `deduplicationKey`，其范围为 `gatewayId + deviceId + protocolProfileId`；同键跨 `eventId` 重投也要去重。相同内容只保留一个 canonical 事件，并为新 `eventId` 保存可追溯 alias；不同内容保留冲突行和受控证据，不覆盖原事件。canonical 的并发唯一性必须在 Oracle 本地事务中保证，不能只查询后插入；后续迁移事件仍使用来源系统/表/主键定位，不以历史事件重放设备操作。

## 6. 安全、审计和错误语义

- Web 园区权限仍由 `smart-platform` 的现有 token/资源/数据范围裁定并适配入口；`smart-lock` 只接受受信服务上下文并再次验证内部身份与 scope，不能把前端 `parkId` 当授权依据。
- H5 只使用服务端 token 的 `sub`/员工身份；旧前端 `badge`、query、路由、localStorage 和请求 body 不是权威身份。详见 [web-h5.md](web-h5.md)。
- 密码、动态码、卡号、指纹/人脸材料不进入 URL、普通日志、浏览器持久化存储或普通审计文本。审计保存操作者/服务、对象、原因、命令/事件关联、状态变化和脱敏摘要。
- 门户和前端只能访问外部业务门面；协议桥的 TCP、网关密钥、协议原文和内部 service endpoint 不得暴露。
- 每个写入失败至少可区分：`AUTH_REQUIRED`、`SCOPE_DENIED`、`STALE_MEMBERSHIP`、`VERSION_CONFLICT`、`CAPABILITY_UNVERIFIED`、`EXECUTOR_FENCED`、`DUPLICATE_EVENT`、`OUT_OF_ORDER_EVENT`、`PAYLOAD_HASH_MISMATCH`、`DEVICE_UNKNOWN_RESULT`、`COMMAND_EXPIRED`。错误响应不得包含敏感报文或他人资源是否存在的旁路信息。

## 7. 能力与切换门禁

能力矩阵按 `gatewayId + deviceId + model + firmware + protocolProfileId` 建立。以下任一证据缺失，能力状态为 `UNVERIFIED`：稳定网关身份、认证/网络围栏、线协议分帧与关联位、传输 ACK、设备 ACK、重连/并发语义、撤权和状态读取、地址改址回读。`UNVERIFIED` 能力在 Web/H5 显示清晰原因并拒绝真实下发，不因旧 UI 按钮存在、源码有分支或旧数据状态而放开。

正式 `NEW_ONLY` 的最小证据是：旧执行方已停止；全通信域所有网关成员已清点并改址回读；新桥接身份/密钥和单活租约已验证；授权新增、撤权、迟到回执、重启恢复和未知结果对账通过；管理员/H5 不再能切回旧 URL 或旧执行开关。未满足时只能 `LEGACY_ONLY`、`SHADOW` 或 `CUTOVER_FREEZE`，不得静默丢弃住宿变更。
