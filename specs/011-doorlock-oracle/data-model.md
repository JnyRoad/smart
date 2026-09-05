# 门锁 Oracle 候选数据模型

## 1. 设计边界

这是实施规格阶段的字段级候选模型，不是可直接执行的 DDL。Oracle 产品版本、
schema owner、表空间、字符集、实际字段约束和运行版本都为 **UNVERIFIED**。
所有类型、精度、长度、可空性、主键、外键、唯一键和索引均标为候选，必须在
DBA 只读核验后才能固化。

旧实体字段来自 [source-inventory.md](source-inventory.md) 和
[migration-mapping.md](migration-mapping.md)。旧实体使用 Java `Long`、
`Integer`、`String`、`Double`、`LocalDateTime`、`byte[]`，只能支持以下
候选映射：`Long -> NUMBER(19,0)`、`Integer -> NUMBER(10,0)`、
`Double -> NUMBER(18,6)`、`LocalDateTime -> TIMESTAMP(6)`、敏感/大文本
按用途候选 `VARCHAR2`/`CLOB`/`BLOB`。这些不是源库或目标库的已确认定义。

目标系统不复制平台人员、园区、房间和住宿主数据。目标门锁服务只保存稳定
引用和必要的迁移快照；`platform_person_id`、`platform_park_id`、
`platform_room_id` 的来源、范围和外键方式由 Smart 平台确认。所有字符串长度
是候选上限，需按现有 API 字符串契约和 Oracle `VARCHAR2` CHAR/BYTE 语义复核。

## 2. 关系和责任边界

```text
平台住宿事实 ───── DL_OUTBOX（platform 本地事务）
                         │ 生命周期事件至少一次投递
                         ▼
              DL_INBOX ── DL_INBOX_CONSUMER_CURSOR
                         │ smart-lock 消费本地事务
                         ▼
DL_ACCESS_GRANT ── DL_GRANT_TARGET ── DL_GRANT_CREDENTIAL ── DL_CREDENTIAL ── DL_COMMAND
        │                                      │                 │
        │                                      │                 ├─ lock: ATTEMPT ── RECEIPT
        │                                      │                 └─ bridge API → BRIDGE_OUTBOX/DELIVERY
        │                                      │
DL_DEVICE ── DL_DEVICE_ROOM_BINDING ── DL_BINDING_RECALC_TASK ── DL_BINDING_RECALC_MEMBERSHIP
                                             DL_DEVICE_EVENT
    │  ▲                                      DL_AUDIT_EVENT
    │  └──── DL_DEVICE_GATEWAY ──── DL_GATEWAY
```

`smart-platform` 的入住/调宿/退宿事务只写住宿事实和生命周期事件
`DL_OUTBOX`，不跨模块写 `DL_ACCESS_GRANT` 或设备命令；该 outbox 行与住宿事实
在 platform 自己的 Oracle 本地事务中提交。`smart-lock` 接收后在自己的锁域消费
事务中原子写 `DL_INBOX`、消费游标、授权/凭据关系和命令事实，重复 `eventId` 或
旧 `aggregateVersion` 不重复产生设备效果；事务提交后才向 Bridge 投递。Bridge
拥有自己的持久发送去重/待回传账本，只通过内部 API 回传结果，不能跨模块直接写
`DL_COMMAND_ATTEMPT`/`DL_RECEIPT`，也不重建业务决策。一个锁可以关联多个网关，
`DL_DEVICE_GATEWAY` 不允许以单一 `owner_gateway_id` 简化；实际发送网关属于每次
尝试的事实。`RoomBindingService` 在锁域本地事务中同时提交绑定事实、绑定版本、当时本地
可见的 `membership_id` 范围和重算任务；任务提交前不得调用平台或 Bridge。平台住房 API
不是跨服务原子快照，任务必须通过 `scope_watermark`/分页结果封存范围，发现缺口时不得
宣称范围完整或开始 provision。

## 3. 在线核心表候选

### 3.1 资产、房间和能力

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_DEVICE` | `id NUMBER(19,0) PK`；`platform_park_id NUMBER(19,0) FK?`；`device_num VARCHAR2(128)`；`device_name VARCHAR2(256)`；`model_code VARCHAR2(64)`；`hardware_version VARCHAR2(128)`；`firmware_version VARCHAR2(128)`；`enabled_flag NUMBER(1,0)`；`online_status VARCHAR2(32)`；`physical_state VARCHAR2(32)`；`last_report_at TIMESTAMP(6)`；`remark VARCHAR2(1000)`；`created_at/updated_at TIMESTAMP(6)`；`created_by/updated_by VARCHAR2(128)`；`source_legacy_id NUMBER(19,0)` | `IDX(platform_park_id,device_num)` 候选；房间只由 `DL_DEVICE_ROOM_BINDING` 表达；在线、物理状态和启停分开；不含排它 `gateway_id`。 |
| `DL_GATEWAY` | `id NUMBER(19,0) PK`；`platform_park_id NUMBER(19,0) FK?`；`gateway_num VARCHAR2(128)`；`endpoint_host VARCHAR2(256)`；`endpoint_port NUMBER(10,0)`；`gateway_name VARCHAR2(256)`；`gateway_type VARCHAR2(64)`；`protocol_version VARCHAR2(64)`；`enabled_flag NUMBER(1,0)`；`connection_status VARCHAR2(32)`；`last_seen_at TIMESTAMP(6)`；`remark VARCHAR2(1000)`；审计字段同上 | `IDX(platform_park_id,gateway_num)` 候选；endpoint 真实性和地址变更另留审计。 |
| `DL_DEVICE_GATEWAY` | `id NUMBER(19,0) PK`；`device_id NUMBER(19,0) FK`；`gateway_id NUMBER(19,0) FK`；`relation_status VARCHAR2(24)`；`route_priority NUMBER(10,0)`；`valid_from/valid_to TIMESTAMP(6)`；`last_observed_at TIMESTAMP(6)`；`source_ref VARCHAR2(256)`；审计字段 | 可有多个有效关系；候选 `IDX(device_id,relation_status)`、`IDX(gateway_id,relation_status)`；不能建立 device 单列唯一 owner。 |
| `DL_DEVICE_ROOM_BINDING` | `id NUMBER(19,0) PK`；`binding_id VARCHAR2(256)`；`binding_version NUMBER(19,0)`；`binding_target_key VARCHAR2(256)`；`device_id NUMBER(19,0) FK`；`platform_room_id NUMBER(19,0) FK?`；`membership_id VARCHAR2(128)`；`binding_status VARCHAR2(24)`；`valid_from/valid_to TIMESTAMP(6)`；`lodging_version NUMBER(19,0)`；`event_id VARCHAR2(256)`；审计字段 | `binding_id` 稳定对应独立绑定聚合 `ROOM_DEVICE_BINDING:{roomId}`，不能每次变更新造一个不相关 ID；`binding_version` 在该聚合内单调递增且不可复用，独立于住宿 `aggregateVersion`。一版可有多个设备目标行，候选唯一 `(binding_id,binding_version,binding_target_key)`（至少保证同版同设备目标不重复）；保留换锁/解绑历史；候选 `IDX(device_id,valid_to)`、`IDX(platform_room_id,binding_status)`；关系变化必须能追溯到绑定事实 `eventId`。同一事务还必须创建一个 `DL_BINDING_RECALC_TASK` 及其当时已知 membership 范围，不能只更新当前绑定行。 |
| `DL_BINDING_RECALC_TASK` | `id NUMBER(19,0) PK`；`task_id VARCHAR2(256) UNIQUE?`；`binding_id VARCHAR2(256)`；`binding_version NUMBER(19,0)`；`old_platform_room_id/new_platform_room_id NUMBER(19,0) FK?`；`platform_room_id NUMBER(19,0) FK?`；`old_device_id/new_device_id NUMBER(19,0) FK?`；`old_gateway_id/new_gateway_id NUMBER(19,0) FK?`；`task_status VARCHAR2(32)`；`claim_phase VARCHAR2(24)`；`scope_status VARCHAR2(24)`；`scope_collection_generation NUMBER(19,0)`；`scope_source_ref VARCHAR2(256)`；`scope_cursor VARCHAR2(256)`；`scope_snapshot_token VARCHAR2(256)`；`scope_watermark VARCHAR2(256)`；`scope_watermark_start/scope_watermark_end VARCHAR2(256)`；`scope_end_verified_at TIMESTAMP(6)`；`scope_sealed_at TIMESTAMP(6)`；`claim_token VARCHAR2(256)`；`claimed_at/claim_until TIMESTAMP(6)`；`retry_count NUMBER(10,0)`；`next_retry_at TIMESTAMP(6)`；`last_error_code VARCHAR2(128)`；`last_error_at TIMESTAMP(6)`；`trace_id VARCHAR2(128)`；审计字段 | 候选唯一 `(binding_id,binding_version)`；`task_status` 候选 `PENDING/CLAIMED/RETRY_PENDING/SUCCEEDED/FAILED/RECONCILIATION_REQUIRED`，`claim_phase` 候选 `SCOPE_COLLECTION/EXECUTION`，`scope_status` 候选 `OPEN/SEALED/GAP/RECONCILIATION_REQUIRED`。同一持久租约可在 `SCOPE_COLLECTION` 阶段领取 `OPEN/GAP` 任务，必须保存 cursor，租约超时后可从 cursor 恢复；`EXECUTION` 阶段只允许 `scope_status=SEALED`。必须有可验证分页 snapshot token，或全程相同的单调住房水位并通过结束校验；否则保持 `GAP`，不得执行。snapshot 失效或水位改变时必须递增 `scope_collection_generation`、使旧代次 scope 行失效、清空 cursor/封存水位并从新代次重新收集，不能混用不同水位页面。claim/lease、重试次数、下次重试、scope 水位和最后错误必须持久化，且不能丢失或覆盖较新绑定版本。 |
| `DL_BINDING_RECALC_MEMBERSHIP` | `id NUMBER(19,0) PK`；`task_id VARCHAR2(256) FK`；`collection_generation NUMBER(19,0)`；`membership_id VARCHAR2(128)`；`scope_status VARCHAR2(32)`；`created_at/updated_at TIMESTAMP(6)`；`last_error_code VARCHAR2(128)` | 候选唯一 `(task_id,collection_generation,membership_id)`；一行代表该绑定版本收集到的一个受影响 membership；范围可在 `SCOPE_COLLECTION` 阶段增量落账，但只有 scope 封存且 `EXECUTION` claim 后才按当前代次完整范围执行。snapshot/水位失效时旧代次行标记失效，不与新代次混合。范围内每个 membership 按明确旧目标撤权/新目标授权顺序重算，不能只重算绑定行上的可选 `membership_id`。 |
| `DL_DEVICE_MODEL_CAPABILITY` | `id NUMBER(19,0) PK`；`gateway_id NUMBER(19,0) FK`；`device_id NUMBER(19,0) FK`；`model_code VARCHAR2(64)`；`firmware_version VARCHAR2(128)`；`protocol_profile_id VARCHAR2(128)`；`supports_password/card/fingerprint NUMBER(1,0)`；`max_credential_slots NUMBER(10,0)`；`capability_evidence_status VARCHAR2(24)`；`evidence_ref VARCHAR2(256)`；`evidence_digest VARCHAR2(128)`；`verified_at TIMESTAMP(6)`；`expires_at TIMESTAMP(6)`；审计字段 | 能力证据候选唯一 `(gateway_id,device_id,model_code,firmware_version,protocol_profile_id)`；`apply_pwd/apply_card/apply_finger` 只能成为该精确五元组的待验证证据。不得按型号、型号+固件范围、设备或网关单独外推；缺少任一维度即 `UNVERIFIED`。 |

### 3.2 生命周期事件接收和消费游标

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_INBOX` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256)`；`event_type VARCHAR2(128)`；`schema_version NUMBER(10,0)`；`producer VARCHAR2(64)`；`aggregate_type VARCHAR2(64)`；`aggregate_id VARCHAR2(256)`；`aggregate_version NUMBER(19,0)`；`membership_id VARCHAR2(128)`；`occurred_at/received_at TIMESTAMP(6)`；`payload_ref VARCHAR2(256)`；`payload_digest VARCHAR2(128)`；`process_status VARCHAR2(32)`；`processed_at TIMESTAMP(6)`；`failure_code VARCHAR2(128)`；`replay_batch_id VARCHAR2(128)` | `event_id` 唯一候选仅保留 canonical Inbox 行；只有相同 `eventId + payload_digest` 的精确重投才返回该行原处理结果。相同 `eventId` 不同摘要不得覆盖原行或产生业务效果，必须在同一事务追加 `DL_AUDIT_EVENT` 冲突摘要/来源时间/独立 `evidence_ref`；新 `eventId` 携带旧 `aggregateVersion` 时保留自己的 Inbox 行并标记 `STALE_EVENT`，不能返回另一事件的成功。`aggregate_id` 是字符串 API 聚合标识，不得改成设备/房间数字键；payload 原文只保存受控引用。 |
| `DL_INBOX_CONSUMER_CURSOR` | `id NUMBER(19,0) PK`；`consumer_name VARCHAR2(64)`；`aggregate_type VARCHAR2(64)`；`aggregate_id VARCHAR2(256)`；`last_aggregate_version NUMBER(19,0)`；`last_event_id VARCHAR2(256)`；`updated_at TIMESTAMP(6)`；`lease_token VARCHAR2(128)`；`lease_until TIMESTAMP(6)` | 候选唯一 `(consumer_name,aggregate_type,aggregate_id)`；以游标拒绝旧版本回退，发现版本缺口则进入待处理/对账，不跳过缺失事件。 |

### 3.3 人员授权和设备凭据

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_ACCESS_GRANT` | `id NUMBER(19,0) PK`（内部行键）；`grant_id VARCHAR2(256)`（稳定业务标识）；`grant_revision NUMBER(19,0)`；`platform_person_id NUMBER(19,0) FK?`；`platform_park_id NUMBER(19,0) FK?`；`platform_room_id NUMBER(19,0) FK?`；`membership_id VARCHAR2(128)`；`aggregate_id VARCHAR2(256)`；`aggregate_version NUMBER(19,0)`；`lodging_ref VARCHAR2(128)`；`effective_from/effective_to TIMESTAMP(6)`；`authorization_status VARCHAR2(32)`；`reason_code VARCHAR2(64)`；`event_id VARCHAR2(256)`；`created_at/updated_at TIMESTAMP(6)`；`created_by/updated_by VARCHAR2(128)`；`source_legacy_id NUMBER(19,0)` | 授权父记录的业务身份是 `grant_id + grant_revision + membership_id`，候选唯一 `(grant_id,grant_revision,membership_id)`；`id` 不能替代对外 `grantId`。父记录不含单一 `device_id`，同一业务授权可挂多个真实 target；不得为每个设备拆成互不关联的 grant。状态候选仅为 `PENDING_PROVISION/ACTIVE/PENDING_REVOKE/REVOKED/RECONCILIATION_REQUIRED`；`ACTIVE` 必须覆盖全部必需 target 及其全部必需凭据并且资格仍有效；平台不直接写此表。 |
| `DL_GRANT_TARGET` | `id NUMBER(19,0) PK`（内部行键）；`grant_row_id NUMBER(19,0) FK`；`grant_id VARCHAR2(256)`；`grant_revision NUMBER(19,0)`；`membership_id VARCHAR2(128)`；`target_kind VARCHAR2(32)`；`target_id VARCHAR2(256)`；`device_id NUMBER(19,0) FK`；`platform_room_id NUMBER(19,0) FK?`；`target_expected_version NUMBER(19,0)`；`required_flag NUMBER(1,0)`；`target_status VARCHAR2(32)`；`created_at/updated_at TIMESTAMP(6)`；审计字段 | 每个真实设备目标一行；候选唯一 `(grant_id,grant_revision,membership_id,target_kind,target_id)`，并以 `grant_row_id` 约束到同一父 grant。`CREDENTIAL` target 必须有真实 `device_id`，不得用另一个无关 grant/id 代替；`target_expected_version` 是该目标的并发版本快照。 |
| `DL_CREDENTIAL` | `id NUMBER(19,0) PK`；`platform_person_id NUMBER(19,0) FK?`；`device_id NUMBER(19,0) FK`；`credential_type VARCHAR2(24)`；`device_key_id VARCHAR2(64)`；`legacy_key_id NUMBER(10,0)`；`material_ref VARCHAR2(256)`；`value_digest VARCHAR2(128)`；`material_version VARCHAR2(64)`；`credential_status VARCHAR2(24)`；`valid_from/valid_to TIMESTAMP(6)`；`revoked_at TIMESTAMP(6)`；审计字段；`source_legacy_id NUMBER(19,0)` | `device_key_id` 是锁内槽/钥匙标识；不得与 `protocol_task_id` 共列。原始密码/指纹不进普通表；候选 `IDX(device_id,credential_type,credential_status)`。同一房间可有多个凭据，设备侧按 `device_id + credential`（含槽位/类型）确认，人员/membership 资格由 grant 关系另行约束，不以房间唯一化。凭据生命周期不随单个 grant 删除。 |
| `DL_GRANT_CREDENTIAL` | `id NUMBER(19,0) PK`；`grant_row_id NUMBER(19,0) FK`；`grant_target_id NUMBER(19,0) FK`；`grant_id VARCHAR2(256)`；`grant_revision NUMBER(19,0)`；`credential_id NUMBER(19,0) FK`；`credential_slot VARCHAR2(128)`；`membership_id VARCHAR2(128)`；`required_flag NUMBER(1,0)`；`relation_status VARCHAR2(24)`；`credential_confirmation_status VARCHAR2(24)`；`confirmed_at TIMESTAMP(6)`；`event_id VARCHAR2(256)`；`bound_at/unbound_at TIMESTAMP(6)`；`created_at/updated_at TIMESTAMP(6)`；审计字段 | 候选唯一 `(grant_target_id,credential_id,credential_slot)`；`grant_id + grant_revision + membership_id` 必须与父 grant 和 target 一致。一个凭据可被多个有效业务 grant 复用。`ACTIVE` 只能在每个 `required_flag=1` 的 target 下所有必需凭据均为设备确认状态后进入；部分 target/凭据成功不得提升父 grant。撤权只解除当前 grant 关系；仅当无其它有效 grant、无在途命令且设备侧撤销/槽位收敛得到证据时，才允许回收或标记凭据失效。 |

### 3.4 命令、尝试、回执和对账

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_COMMAND` | `id NUMBER(19,0) PK`；`command_id VARCHAR2(256) UNIQUE?`；`request_id VARCHAR2(256)`（HTTP 意图关联，非命令唯一键）；`self_service_request_id NUMBER(19,0) FK?`；`tenant_ref VARCHAR2(128)`；`idempotency_service VARCHAR2(64)`；`idempotency_subject_ref VARCHAR2(256)`；`idempotency_operation VARCHAR2(64)`；`idempotency_target_kind VARCHAR2(32)`；`idempotency_target_ref VARCHAR2(256)`（规范化目标）；`idempotency_key VARCHAR2(256)`；`event_id VARCHAR2(256)`；`aggregate_id VARCHAR2(256)`；`aggregate_version NUMBER(19,0)`；`membership_id VARCHAR2(128)`；`membership_scope_ref VARCHAR2(256)`；`platform_person_id NUMBER(19,0) FK?`；`platform_park_id NUMBER(19,0) FK?`；`platform_room_id NUMBER(19,0) FK?`；`grant_row_id NUMBER(19,0) FK?`；`grant_id VARCHAR2(256)`；`grant_revision NUMBER(19,0)`；`credential_id NUMBER(19,0) FK?`；`credential_slot VARCHAR2(128)`；`target_kind VARCHAR2(32)`；`target_id VARCHAR2(256)`；`gateway_id NUMBER(19,0) FK?`（目标网关）；`device_id NUMBER(19,0) FK?`（目标设备，可空）；`expected_version NUMBER(19,0)`；`operator_subject_ref VARCHAR2(128)`；`operator_scope_ref VARCHAR2(256)`；`operator_allowed_actions VARCHAR2(512)`；`operator_authorization_snapshot_ref VARCHAR2(256)`；`operator_authorization_digest VARCHAR2(128)`；`operator_authorization_expires_at TIMESTAMP(6)`；`operation VARCHAR2(32)`；`command_type VARCHAR2(64)`；`command_code VARCHAR2(64)`；`protocol_task_id VARCHAR2(128)`；`protocol_profile_id VARCHAR2(128)`；`command_status VARCHAR2(32)`；`not_before/expires_at TIMESTAMP(6)`；`payload_ref VARCHAR2(256)`；`payload_digest VARCHAR2(128)`；`payload_digest_algorithm VARCHAR2(32)`；`payload_digest_key_version VARCHAR2(64)`；`authorization_snapshot_digest VARCHAR2(128)`；`snapshot_created_at TIMESTAMP(6)`；`executor_lease_id VARCHAR2(256)`；`bridge_session_id VARCHAR2(256)`；`next_attempt_at TIMESTAMP(6)`；`last_attempt_at TIMESTAMP(6)`；`reconciliation_required_flag NUMBER(1,0)`；审计字段；`source_legacy_id NUMBER(19,0)` | smart-lock 在锁域消费事务中持久化命令；`command_id` 是 API 稳定字符串标识，内部 `id` 仅为候选数字键；`request_id` 只是 HTTP 意图关联，同一请求可 fan-out 为 0..N 命令，不能作为命令唯一约束。`target_kind`、`grant_id`、`grant_revision`、`credential_slot`、`expected_version`、`membership_id`、scope 和 operator authorization 相关列组成写入即不可变的授权快照，不能用后续查询覆盖；`GATEWAY_ASSET` 必须有真实 `gateway_id` 且 `device_id` 为空，`CREDENTIAL`/`DEVICE_ASSET`/`DEVICE_ACTION` 等设备目标必须有真实 `device_id`，不允许用同一个 target/id 混淆两类资产；目标 `gateway_id` 与 attempt 的实际传输网关分开。`operator_authorization_snapshot_ref` 只指向不可重放的脱敏声明快照/受控证据，不保存 bearer token、长期 token 或明文权限凭据。`command_status` 候选枚举：`QUEUED/DISPATCHED/WAITING_ACK/SUCCEEDED/RETRY_PENDING/FAILED/EXPIRED/CANCELLED/RECONCILIATION_REQUIRED`。不放 `attempt_no`；候选唯一仅为 `(command_id)` 和 `(tenant_ref,idempotency_service,idempotency_subject_ref,idempotency_operation,idempotency_target_kind,idempotency_target_ref,idempotency_key)`，不对 `(tenant,service,subject,request_id)` 建命令唯一约束，唯一键均不包含 `payload_digest`。同一 scoped key 的 digest 不同必须返回冲突并拒绝插入第二条命令；相同 digest 才可返回既有命令。重试前必须重新核验当前 membership、scope、operator 权限和目标 `expected_version`，快照本身不能授予新权限。 |
| `DL_COMMAND_ATTEMPT` | `id NUMBER(19,0) PK`；`command_row_id NUMBER(19,0) FK`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`event_id VARCHAR2(256)`；`membership_id VARCHAR2(128)`；`device_id NUMBER(19,0) FK?`（网关目标命令可空）；`gateway_id NUMBER(19,0) FK`（本次实际发送网关）；`protocol_profile_id VARCHAR2(128)`；`protocol_task_id VARCHAR2(128)`；`executor_lease_id VARCHAR2(256)`；`bridge_session_id VARCHAR2(256)`；`authorization_revalidation_status VARCHAR2(24)`；`authorization_revalidated_at TIMESTAMP(6)`；`authorization_revalidation_ref VARCHAR2(256)`；`dispatch_status VARCHAR2(32)`；`transport_result_type VARCHAR2(32)`；`transport_accepted NUMBER(1,0)`；`device_result_type VARCHAR2(32)`；`device_confirmed NUMBER(1,0)`；`device_outcome VARCHAR2(32)`；`dispatched_at TIMESTAMP(6)`；`ack_received_at TIMESTAMP(6)`；`result_code VARCHAR2(64)`；`transport_error_code VARCHAR2(128)`；`wire_correlation VARCHAR2(256)`；`evidence_ref VARCHAR2(256)`；`created_at TIMESTAMP(6)` | 候选唯一 `(command_id,attempt_no)`，`event_id` 用于命令消息去重；此处 `gateway_id` 是本次租约选定并实际发送的网关，不是 `DL_COMMAND.gateway_id` 所表示的命令目标网关；每次尝试可选不同传输网关但必须持有唯一租约；`device_id` 是否必填继承命令 `target_kind` 条件；每次重试需留下当前权限/目标版本重新核验的状态和证据；`attempt_no` 与授权/命令状态严格分开。 |
| `DL_RECEIPT` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256) UNIQUE?`；`command_row_id NUMBER(19,0) FK?`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`membership_id VARCHAR2(128)`；`device_id NUMBER(19,0) FK?`；`gateway_id NUMBER(19,0) FK?`；`protocol_profile_id VARCHAR2(128)`；`protocol_task_id VARCHAR2(128)`；`executor_lease_id VARCHAR2(256)`；`bridge_session_id VARCHAR2(256)`；`report_kind VARCHAR2(32)`；`transport_result_type VARCHAR2(32)`；`transport_accepted NUMBER(1,0)`；`device_result_type VARCHAR2(32)`；`device_confirmed NUMBER(1,0)`；`device_outcome VARCHAR2(32)`；`result_code VARCHAR2(64)`；`packet_sequence NUMBER(10,0)`；`received_at TIMESTAMP(6)`；`match_status VARCHAR2(24)`；`payload_digest VARCHAR2(128)`；`raw_payload_ref VARCHAR2(256)` | smart-lock 只通过回传 API 写入；`gateway_id` 是实际回传/传输来源，不是命令目标 `DL_COMMAND.gateway_id`；`event_id` 是回执去重键；保留 late/unmatched/duplicate 回执；B8 分包字段是协议候选，现场需验证；原始帧通过受控引用保存；仅 `device_confirmed=true` 的合法回执推进授权。 |
| `DL_BRIDGE_OUTBOX` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256) UNIQUE?`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`membership_id VARCHAR2(128)`；`protocol_profile_id VARCHAR2(128)`；`device_id NUMBER(19,0) FK?`；`gateway_id NUMBER(19,0) FK?`；`payload_ref VARCHAR2(256)`；`payload_digest VARCHAR2(128)`；`delivery_status VARCHAR2(32)`；`available_at/claimed_at TIMESTAMP(6)`；`claim_token VARCHAR2(256)`；`created_at TIMESTAMP(6)` | Bridge 自己的待回传结果/设备事件 outbox，只以 `event_id` 唯一；同一 command/attempt 必须允许传输 ACK、多个分包 ACK 和最终设备结果各占一行，不能对 command/attempt 建唯一约束。重投同一事件复用 event_id；不由其他模块直接写。 |
| `DL_BRIDGE_DELIVERY` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256)`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`membership_id VARCHAR2(128)`；`protocol_profile_id VARCHAR2(128)`；`protocol_task_id VARCHAR2(128)`；`executor_lease_id VARCHAR2(256)`；`bridge_session_id VARCHAR2(256)`；`device_id NUMBER(19,0) FK?`；`gateway_id NUMBER(19,0) FK?`；`delivery_status VARCHAR2(32)`；`transport_result_type VARCHAR2(32)`；`transport_accepted NUMBER(1,0)`；`device_result_type VARCHAR2(32)`；`device_confirmed NUMBER(1,0)`；`device_outcome VARCHAR2(32)`；`sent_at/ack_at TIMESTAMP(6)`；`result_code VARCHAR2(64)`；`evidence_ref VARCHAR2(256)` | Bridge 以 `(command_id,attempt_no)` 唯一持久化一次发送尝试、租约/会话和结果摘要；逐条回执由 DL_BRIDGE_OUTBOX 保存；只能通过 API 回传 smart-lock，由 smart-lock 写 `DL_COMMAND_ATTEMPT`/`DL_RECEIPT`。 |
| `DL_RECONCILIATION` | `id NUMBER(19,0) PK`；`device_id NUMBER(19,0) FK?`；`credential_id NUMBER(19,0) FK?`；`command_row_id NUMBER(19,0) FK?`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`membership_id VARCHAR2(128)`；`reason_code VARCHAR2(64)`；`expected_state VARCHAR2(64)`；`observed_state VARCHAR2(64)`；`case_status VARCHAR2(24)`；`opened_at/resolved_at TIMESTAMP(6)`；`evidence_ref VARCHAR2(256)`；`owner_ref VARCHAR2(128)`；`notes VARCHAR2(2000)` | `device_id` 按关联命令 `target_kind` 条件可空，网关目标不得伪造设备；`case_status` 独立于命令和授权；迟到新增成功、槽位复用、回执丢失都进入对账，不自动重放远程开门。 |

### 3.5 Outbox、运行历史和审计

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_OUTBOX` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256) UNIQUE?`；`producer_service VARCHAR2(64)`；`aggregate_type VARCHAR2(64)`；`aggregate_id VARCHAR2(256)`；`aggregate_version NUMBER(19,0)`；`membership_id VARCHAR2(128)`；`event_type VARCHAR2(128)`；`schema_version NUMBER(10,0)`；`event_key VARCHAR2(256)`；`payload CLOB`；`payload_digest VARCHAR2(128)`；`outbox_status VARCHAR2(32)`；`available_at TIMESTAMP(6)`；`claim_token VARCHAR2(128)`；`claimed_at TIMESTAMP(6)`；`publish_attempts NUMBER(10,0)`；`published_at TIMESTAMP(6)`；`last_error_code VARCHAR2(128)`；`created_at TIMESTAMP(6)` | `aggregate_id` 是字符串 API 标识，不能用 `NUMBER`；生命周期 outbox 由 platform 与住宿事实同一 Oracle 本地事务写入，platform 不写授权；候选状态 `PENDING/CLAIMED/PUBLISHED/FAILED/RECONCILIATION_REQUIRED`；claim 并发语义须由 DBA/实现验证。 |
| `DL_DEVICE_EVENT` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256)`；`device_id NUMBER(19,0) FK?`；`gateway_id NUMBER(19,0) FK?`；`protocol_profile_id VARCHAR2(128)`；`platform_person_id NUMBER(19,0) FK?`；`membership_id VARCHAR2(128)`；`event_kind VARCHAR2(64)`；`action_code VARCHAR2(64)`；`event_time TIMESTAMP(6)`；`outcome VARCHAR2(32)`；`deduplication_key VARCHAR2(256)`；`deduplication_status VARCHAR2(24)`（候选 `CANONICAL_CLAIMED/CANONICAL/DUPLICATE/CONFLICT/QUARANTINED`）；`canonical_claim_status VARCHAR2(24)`；`canonical_claim_token VARCHAR2(256)`；`canonical_claimed_at TIMESTAMP(6)`；`canonical_claim_version NUMBER(19,0)`；`canonical_event_id VARCHAR2(256)`；`duplicate_of_event_id VARCHAR2(256)`；`conflict_reason_code VARCHAR2(64)`；`conflict_evidence_ref VARCHAR2(256)`；`source_kind VARCHAR2(64)`；`source_schema VARCHAR2(128)`；`source_pk VARCHAR2(256)`；`payload_digest VARCHAR2(128)`；`payload_ref VARCHAR2(256)`；`created_at TIMESTAMP(6)` | `deduplication_key` 和 canonical claim 必须持久化；除候选唯一 `(gateway_id,device_id,protocol_profile_id,deduplication_key,event_id,payload_digest)` 防相同 eventId/内容重复外，还必须有候选条件唯一门禁 `(gateway_id,device_id,protocol_profile_id,deduplication_key)`，仅覆盖 `deduplication_status IN ('CANONICAL','CANONICAL_CLAIMED')`，alias/conflict 行排除在外。入口用数据库 CAS/行锁或先插入 `CANONICAL_CLAIMED` 搭配 `canonical_claim_token/version` 抢占该门禁；唯一冲突后必须重读已提交的 canonical 行，以摘要相同分类为 `DUPLICATE` alias、摘要不同分类为 `CONFLICT/QUARANTINED`，不得并发生成第二个 `CANONICAL`，也不得靠查询后插入。不同 eventId、相同键和相同摘要写可追溯 alias 行且不再产生设备效果；相同键不同摘要写独立冲突行，保留 `payload_digest`、受控 `payload_ref` 和冲突证据，不能覆盖原事件或推进授权。合并旧设备日志时仍用 `source_kind/source_schema/source_pk` 复合来源键；事件不能代替命令状态。 |
| `DL_AUDIT_EVENT` | `id NUMBER(19,0) PK`；`actor_ref VARCHAR2(128)`；`platform_park_id NUMBER(19,0) FK?`；`action VARCHAR2(64)`；`object_type VARCHAR2(64)`；`object_id NUMBER(19,0)`；`outcome VARCHAR2(32)`；`correlation_id VARCHAR2(256)`；`source_event_id VARCHAR2(256)`；`source_payload_digest VARCHAR2(128)`；`source_occurred_at/source_received_at TIMESTAMP(6)`；`event_time TIMESTAMP(6)`；`detail_redacted CLOB`；`evidence_ref VARCHAR2(256)`；`append_only_flag NUMBER(1,0)` | 生命周期 event payload 冲突必须 append-only 写入：`object_type='LIFECYCLE_EVENT'`、`object_id=DL_INBOX.id`，保留冲突摘要、来源 `eventId`/事实时间/接收时间和独立不可变 `evidence_ref`（受控原载荷）；同事务不得创建/更新授权、凭据或命令效果。密码、指纹、恢复密钥、完整原始帧不可写入 `detail_redacted`；候选 `IDX(object_type,object_id,event_time)`。 |
| `DL_SYSTEM_SETTING` | `id NUMBER(19,0) PK`；`scope_type VARCHAR2(32)`；`scope_id NUMBER(19,0)`；`setting_key VARCHAR2(128)`；`value_ref VARCHAR2(256)`；`value_digest VARCHAR2(128)`；`setting_status VARCHAR2(24)`；`version_no NUMBER(19,0)`；审计字段 | 独立后台设置仅在非敏感且有归属时迁移；密钥/模板只保存受控引用，不保存明文。 |
| `DL_OPERATIONS_ALERT` | `id NUMBER(19,0) PK`；`platform_park_id NUMBER(19,0) FK?`；`severity VARCHAR2(16)`；`alert_type VARCHAR2(64)`；`related_type VARCHAR2(64)`；`related_id NUMBER(19,0)`；`message_redacted VARCHAR2(2000)`；`read_status VARCHAR2(24)`；`created_at/updated_at TIMESTAMP(6)`；`source_legacy_id NUMBER(19,0)` | 旧消息/告警用于运维可见性，不改变授权/命令事实。 |

### 3.6 本人短时请求与防重放

| 候选表 | 候选字段 | 必需逻辑约束 |
| --- | --- | --- |
| `DL_SELF_SERVICE_REQUEST` | `id NUMBER(19,0) PK`；`tenant_ref VARCHAR2(128)`；`idempotency_service VARCHAR2(64)`；`subject_ref VARCHAR2(256)`；`membership_id VARCHAR2(128)`；`request_id VARCHAR2(256)`；`idempotency_operation VARCHAR2(64)`；`idempotency_target_kind VARCHAR2(32)`；`idempotency_target_ref VARCHAR2(256)`（规范化目标）；`idempotency_key VARCHAR2(256)`；`verification_digest VARCHAR2(256)`；`purpose VARCHAR2(64)`；`status VARCHAR2(64)`；`target_set_snapshot_ref VARCHAR2(256)`；`target_set_digest VARCHAR2(128)`；`target_set_frozen_at TIMESTAMP(6)`；`expected_command_count NUMBER(10,0)`；`completed_command_count NUMBER(10,0)`；`succeeded_command_count NUMBER(10,0)`；`failed_command_count NUMBER(10,0)`；`aggregate_status VARCHAR2(32)`；`aggregated_at TIMESTAMP(6)`；`payload_digest VARCHAR2(128)`；`payload_digest_algorithm VARCHAR2(32)`；`payload_digest_key_version VARCHAR2(64)`；`expires_at/consumed_at/created_at TIMESTAMP(6)` | 本表与 `DL_COMMAND` 各自独立保存 `requestId` 和 `idempotencyKey`，不共享唯一键或把 request 行伪装成单一 command；`DL_COMMAND.self_service_request_id` 反向关联本行，允许一个父 request 关联 0..N 条命令，本表不再以 `command_id` 作为权威结果。幂等候选唯一 `(tenant_ref,idempotency_service,subject_ref,idempotency_operation,idempotency_target_kind,idempotency_target_ref,idempotency_key)`，父 HTTP 意图候选唯一 `(tenant_ref,idempotency_service,subject_ref,request_id)`；该 request 唯一性不阻止同一请求 fan-out 多个 target。需要一次性 verification 的 purpose 与 `verification_digest` 均不可为空；`verification_digest` 是受控 `verificationRef` 的不可逆消费摘要，候选数据库唯一 `(tenant_ref,subject_ref,purpose,verification_digest)`（仅对非空摘要生效），并在同一事务原子校验未过期/未消费后写入 `consumed_at`。因此 verificationRef 不能通过更换 request/idempotency key 或 purpose 重放。target set 必须在同一锁域事务中以受控不可变 `target_set_snapshot_ref + target_set_digest` 冻结，并写入 `expected_command_count`；聚合结果只能在持久命令数达到预期后按全部命令状态计算，不能只取第一条命令判定全成功。`expected_command_count=0` 对本地 reveal 等无设备命令流程合法，结果为 `NO_COMMANDS`。幂等唯一键不包含 `payload_digest`；同一 scoped key 的不同 digest 必须冲突并拒绝第二行，不能因 digest 入唯一键而悄悄创建两条；相同 digest 才返回原 request/命令集合。低熵密码等敏感 payload 的摘要只能使用批准的 HMAC/密钥版本，禁止裸 hash；reveal 秘密不保存在本表或幂等响应缓存；超时响应丢失后再次读取需新校验。 |

### 3.7 迁移账本和旧账号归档

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_MIGRATION_BATCH` | `id NUMBER(19,0) PK`；`source_system VARCHAR2(64)`；`source_schema_version VARCHAR2(128)`；`mode VARCHAR2(24)`；`batch_status VARCHAR2(32)`；`source_export_hash VARCHAR2(128)`；`source_watermark VARCHAR2(256)`；`started_at/finished_at TIMESTAMP(6)`；`initiated_by VARCHAR2(128)`；`error_summary VARCHAR2(2000)` | 支持 dry-run、断点续跑和重复运行；旧版本/导出 hash 必须有证据。 |
| `DL_MIGRATION_ROW` | `id NUMBER(19,0) PK`；`batch_id NUMBER(19,0) FK`；`source_system VARCHAR2(64)`；`source_schema VARCHAR2(128)`；`source_table VARCHAR2(128)`；`source_pk VARCHAR2(256)`；`target_role VARCHAR2(64)`；`snapshot_id VARCHAR2(128)`；`source_row_hash VARCHAR2(128)`；`target_table VARCHAR2(128)`；`target_id NUMBER(19,0)`；`disposition VARCHAR2(32)`；`classification VARCHAR2(64)`；`reason_code VARCHAR2(128)`；`processed_at TIMESTAMP(6)`；`evidence_ref VARCHAR2(256)` | 候选唯一 `(batch_id,source_system,source_schema,source_table,source_pk,target_role,source_row_hash)`，只防同一批/snapshot 重复；`DL_LEGACY_ID_MAP` 负责跨批稳定目标 ID；`EXCEPTION/EXCLUDED` 必须有理由，不能静默合并。 |
| `DL_LEGACY_ID_MAP` | `id NUMBER(19,0) PK`；`source_system VARCHAR2(64)`；`source_schema VARCHAR2(128)`；`source_table VARCHAR2(128)`；`source_pk VARCHAR2(256)`；`target_role VARCHAR2(64)`；`target_table VARCHAR2(128)`；`target_id NUMBER(19,0)`；`first_batch_id NUMBER(19,0) FK`；`last_batch_id NUMBER(19,0) FK`；`source_row_hash VARCHAR2(128)`；`active_flag NUMBER(1,0)`；`created_at/updated_at TIMESTAMP(6)` | 候选唯一 `(source_system,source_schema,source_table,source_pk,target_role)`；稳定来源身份不随增量 `row_hash` 变化而重复创建目标。`DL_MIGRATION_ROW` 仍记录每个 snapshot/version 的处理结果。 |
| `DL_LEGACY_ROW_ARCHIVE` | `id NUMBER(19,0) PK`；`batch_id NUMBER(19,0) FK`；`source_table VARCHAR2(128)`；`source_pk VARCHAR2(256)`；`archive_ref VARCHAR2(256)`；`redaction_policy VARCHAR2(128)`；`content_digest VARCHAR2(128)`；`access_class VARCHAR2(32)`；`retained_until TIMESTAMP(6)`；`created_at TIMESTAMP(6)` | 旧账号/角色/菜单和不沿用的旧副本仅受控归档；如确需可恢复敏感材料，存批准的加密归档引用，不写普通明文列。 |

### 3.8 字段映射与必需逻辑约束

物理实现方式待 T005 的目标 Oracle 证据定版，但以下逻辑不变量不可省略：

- API `staffId/roomId/parkId` 对应平台引用列的**字符串序列化**；候选 NUMBER 只适用于目标平台键经核实为数字的情况。示例 `staff_123` 是说明性标识，不得强制转数字或当生产格式。
- `grantRevision` 对应 `DL_ACCESS_GRANT.grant_revision`，`grantId` 对应稳定的 `DL_ACCESS_GRANT.grant_id`，不是内部数字行 ID；`grant_id + grant_revision + membership_id` 是父 grant 业务身份，`aggregateVersion` 是住宿事件版本，不能与 grantRevision 混用。`wireTaskId` 对应 attempt/delivery/receipt 的 `protocol_task_id`；Command 上同名字段若保留，只作旧来源快照，不是运行态关联权威。
- 一个父 grant 通过 `DL_GRANT_TARGET` 挂多个实际设备目标，再由 `DL_GRANT_CREDENTIAL` 绑定各 target 的凭据/槽位；不能为每个设备生成互不关联的 grant。只有所有 `required_flag=1` 的 target 及其全部必需凭据都有可信设备确认，父 grant 才能为 `ACTIVE`。
- 凭据命令必须带历史 `membership_id`、`grant_id`、`grant_revision`、真实 target 和槽位快照；设备配置/管理员开门不伪造会员关系，另存 `target_kind VARCHAR2(32)`、`expected_version NUMBER(19,0)`、操作者授权快照和批准范围。相关字段条件可空，以契约 targetKind 校验。
- 设备房间绑定是锁域资产关系，不属于某一个住户；绑定行的 `membership_id` 只可作可选来源线索，不可用于独占或查找当前房间成员。一个房间可有多个住户，变化必须在同一事务原子落账 `DL_BINDING_RECALC_TASK` 及当时本地已知的 `DL_BINDING_RECALC_MEMBERSHIP` 范围；事务后只能在 `claim_phase=SCOPE_COLLECTION` 收集分页并持久 cursor，平台提供可验证 snapshot token，或全程相同单调住房水位且通过结束校验，才可封存完整 scope。`OPEN/GAP` 可领取收集租约但不得执行，只有 `scope_status=SEALED` 且 `claim_phase=EXECUTION` 才能重算/下发；否则保持 `GAP`，不得宣称范围完整。
- 同一有效设备编号和网关身份不可歧义；同一设备同一时间的有效房间绑定不可冲突。同一设备钥匙类型/槽位不能同时属于两个有效凭据版本。并发约束需 Oracle 条件唯一键或受控行锁实现并测试，不只靠查询后插入。
- `DL_DEVICE_MODEL_CAPABILITY` 的能力证据键必须是 `gateway_id + device_id + model_code + firmware_version + protocol_profile_id` 五元组；缺失任一维度、只有型号/固件范围的证据均不得外推为可下发。`DL_RECEIPT` 补事实发生时间 `occurred_at TIMESTAMP(6)`；`DL_OUTBOX` 补同名事实时间，接收/创建时间不替代它。
- platform 负责稳定人员聚合版本分配，依据现有主键方案选择平台持久版本记录/行锁；同事务保证 Outbox 的 `(aggregate_type,aggregate_id,aggregate_version)` 唯一。锁域 Inbox 另保存已消费游标，不回写平台版本。
- 每个迁移 batch 绑定一个不可变 snapshotId/源导出摘要，新增快照必须新批次。稳定 ID map 不含 rowhash，版本行账本含 batch/source/targetRole；复合键过长时可用规范化来源身份摘要加原始字段碰撞校验，不直接假定任意长度联合索引可建。
- “全部历史迁入 Oracle”默认包括结构化历史和可解释归档内容，不只保存指向旧 MySQL 的链接：`DL_LEGACY_ROW_ARCHIVE` 增加候选 `archive_payload CLOB/BLOB` 保存批准的脱敏历史或加密归档，`archive_ref` 只作同库对象/受控材料定位，不得依赖旧 MySQL 在线读取。历史事件补安全人员姓名/工号、设备位置快照，保证人员后续变更不篡改旧记录。敏感原文的排除或外部保管必须有 T004 明确批准，不默认为已迁完。
- 所有事件/命令/尝试/审计需有 `trace_id VARCHAR2(128)` 或稳定关联键；`DL_COMMAND` 与 `DL_SELF_SERVICE_REQUEST` 各自持有独立 `request_id`/`idempotency_key`，幂等范围至少为 `tenant + callerService + subject + operation + targetKind + normalizedTarget`，唯一键不得包含 `payload_digest`。同一 scoped key 的不同 digest 必须冲突而不是插入第二行；低熵秘密的 payload digest 只能使用批准的 HMAC 和 key version。
- `DL_SELF_SERVICE_REQUEST` 是父 HTTP 意图，`request_id` 只在父表按租户/服务/主体唯一；`DL_COMMAND.self_service_request_id` 可反查 0..N 命令，命令唯一性仍按完整 target scope。冻结的 target-set、`expected_command_count` 与 fan-out 命令必须同一锁域事务持久化，聚合不得取第一条命令代替全量结果；`expected_command_count=0` 的本地 reveal 可合法无命令完成。
- `verificationRef` 的单次消费不依赖客户端 `requestId/idempotencyKey`：`DL_SELF_SERVICE_REQUEST` 另需候选唯一 `(tenant_ref,subject_ref,purpose,verification_digest)`，要求校验凭证的 purpose 不允许空摘要。签发及消费均核对凭证绑定的主体、用途、membership 和到期时间；同一凭证换请求 ID 或幂等键仍不得创建第二个消费记录。消费领取与父请求/子命令在同一锁域事务持久化，失败回滚不留下假消费；精确重投只查询原请求结果，不能重新返回一次性 reveal 的秘密。
- `DL_INBOX` 对同 `eventId` 不同摘要只追加 `DL_AUDIT_EVENT`（`object_type=LIFECYCLE_EVENT`、`object_id=DL_INBOX.id`）及独立受控证据，不覆盖 canonical 行或产生业务效果；新 `eventId` 的旧版本事件保留自己的行并标记 `STALE_EVENT`。
- `GATEWAY_ASSET` 命令的目标 `DL_COMMAND.gateway_id` 为真实网关、`device_id` 可空；其 `DL_COMMAND_ATTEMPT`、`DL_RECEIPT`、`DL_RECONCILIATION` 及桥接账本中的设备 FK 也按 `target_kind` 条件可空，不能为网关操作伪造设备。`DL_COMMAND_ATTEMPT.gateway_id`/回传 `DL_RECEIPT.gateway_id` 只表示实际传输或观测来源，不替代命令目标网关。
- `DL_DEVICE_EVENT` 的 `deduplication_key` 以 gateway/device/profile 为范围持久化，并用仅覆盖 canonical 状态的数据库唯一门禁/CAS 抢占首个事件；跨 `eventId` 的同键同内容重投写可追溯 alias 但不重复业务效果，同键不同摘要保留隔离的冲突行和受控证据，不覆盖首个 canonical 事件。唯一冲突必须重读 canonical 后再分类，不能以并发查询后插入替代门禁。

| 表归属 | 唯一写入服务 |
| --- | --- |
| 平台住宿事实、平台聚合版本、生命周期 DL_OUTBOX | smart-platform |
| DL_INBOX、消费游标、设备资产/绑定、绑定重算任务/范围、Grant/Target/Credential/Command/Attempt/Receipt、本人请求、迁移与历史审计 | smart-lock |
| DL_BRIDGE_DELIVERY、DL_BRIDGE_OUTBOX | smart-bridge-lock |

同一 Oracle 实例不表示可跨服务任意改表；分别定义运行账号/对象权限。Bridge 必须在本地事务中持久化接收结果和回传事件，重启后先恢复账本。内部事件原文应是最小化非敏感 payload 或受控引用，引用的生命周期必须覆盖恢复窗口。

## 4. 状态与事务规则

### 4.1 状态不可混用

- `DL_ACCESS_GRANT.authorization_status` 描述当前 membership 的业务授权和设备
  资格，候选状态仅为 `PENDING_PROVISION`、`ACTIVE`、`PENDING_REVOKE`、
  `REVOKED`、`RECONCILIATION_REQUIRED`；`ACTIVE` 必须同时覆盖 `DL_GRANT_TARGET` 中所有
  `required_flag=1` 的设备目标，并且每个目标下 `DL_GRANT_CREDENTIAL` 的全部必需凭据均有
  可信设备确认且 membership 仍有效。它与命令状态分列、分索引、分审计；到期/过期通过 membership
  有效期和撤权流程表达，不新增 `EXPIRED` 授权状态。
- `DL_COMMAND.command_status` 只允许使用
  `QUEUED`、`DISPATCHED`、`WAITING_ACK`、`SUCCEEDED`、`RETRY_PENDING`、
  `FAILED`、`EXPIRED`、`CANCELLED`、`RECONCILIATION_REQUIRED`。
- `DL_COMMAND_ATTEMPT` 记录每次尝试，`attempt_no` 单独递增；不能把旧
  `times` 直接当作新的 attemptNo。
- `SUCCEEDED` 仅表示与可信设备回执匹配；传输写成功只能到
  `DISPATCHED/WAITING_ACK`。回执缺失、迟到、冲突或设备状态未知都进入对账。

### 4.2 事务和发送

1. `smart-platform` 的入住/调宿/退宿事务只写住宿事实和生命周期 `DL_OUTBOX`；
   两者使用同一 Oracle 本地事务，平台不得在该事务跨模块写
   `DL_ACCESS_GRANT`/凭据/设备命令。Outbox publisher 提交后投递 `eventId`，
   `smart-lock` 在独立消费事务中写 `DL_INBOX`、游标、授权/凭据关系和需要的命令
   事实；重复或旧版本事件不得留下第二次设备效果。
2. smart-lock 的 Inbox consumer 在同一锁域事务中原子提交 `DL_ACCESS_GRANT`、
   `DL_GRANT_TARGET`、`DL_GRANT_CREDENTIAL` 和需要的 `DL_COMMAND`；`RoomBindingService`
   则在同一锁域事务中提交 `DL_DEVICE_ROOM_BINDING`、`DL_BINDING_RECALC_TASK` 和已知的
   `DL_BINDING_RECALC_MEMBERSHIP` 行。事务提交后，持有同一持久租约的
   `claim_phase=SCOPE_COLLECTION` 只允许分页收集并更新 cursor；收集租约超时可恢复，
   但 `OPEN/GAP` 不得执行。只有验证 snapshot token，或验证同一单调住房水位的开始/结束值并
   封存 `scope_status=SEALED` 后，才可切换 `claim_phase=EXECUTION` 重算/创建目标命令；
   snapshot 失效或水位改变时递增 collection generation、使旧 scope 行失效并清空 cursor/封存水位，
   从新代次重新收集，不能混用不同水位页面。无法验证快照、分页有缺口或绑定版本已过期时保持
   `GAP/RECONCILIATION_REQUIRED`，不创建新目标设备命令。
   Bridge 只执行已生成的命令，并在自己的 `DL_BRIDGE_OUTBOX`/
   `DL_BRIDGE_DELIVERY` 持久化发送与回传去重，不能直接写锁域的 attempt/receipt。
3. 发送采用至少一次语义：使用事件/业务版本、幂等键、尝试记录和对账；不承诺
   物理设备 exact-once。每次 retry 都要重新核验当前 membership、scope、operator 权限和
   目标 `expected_version`，不能仅凭持久命令快照重放；远程开门命令过期后不可由恢复扫描无条件重放。
4. 单活桥和“默认真实发送关闭”属于运行门禁，不是数据库状态的替代；影子/演练
   阶段仍必须落账但不能向真实网关发送。

## 5. 敏感字段和迁移原则

- `lk_person.password`、`lk_person.finger_code_list`、`lk_key.key_value`、
  `lk_modify_pwd_log.password` 不按“全部密码必迁/全部指纹必重采”处理；先按
  设备能力、原始材料可用性、密钥轮换和现场验证分类。
- 新表只保留 `material_ref`、摘要、版本和处置结果；原始敏感值只能进入经批准
  的受控加密存储。数据库未迁移不等于锁内凭据立即失效，设备侧核验和撤权必须
  单独完成。
- B8/ACK 现有源码证据可用于设计 `DL_RECEIPT`，但不能替代现场固件、分包顺序、
  设备槽位和回执语义验证。
