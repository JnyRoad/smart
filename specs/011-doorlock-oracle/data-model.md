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
DL_ACCESS_GRANT ── DL_GRANT_CREDENTIAL ── DL_CREDENTIAL ── DL_COMMAND
        │                                      │                 │
        │                                      │                 ├─ lock: ATTEMPT ── RECEIPT
        │                                      │                 └─ bridge API → BRIDGE_OUTBOX/DELIVERY
        │                                      │
DL_DEVICE ── DL_DEVICE_ROOM_BINDING        DL_DEVICE_EVENT
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
尝试的事实。

## 3. 在线核心表候选

### 3.1 资产、房间和能力

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_DEVICE` | `id NUMBER(19,0) PK`；`platform_park_id NUMBER(19,0) FK?`；`device_num VARCHAR2(128)`；`device_name VARCHAR2(256)`；`model_code VARCHAR2(64)`；`hardware_version VARCHAR2(128)`；`firmware_version VARCHAR2(128)`；`enabled_flag NUMBER(1,0)`；`online_status VARCHAR2(32)`；`physical_state VARCHAR2(32)`；`last_report_at TIMESTAMP(6)`；`remark VARCHAR2(1000)`；`created_at/updated_at TIMESTAMP(6)`；`created_by/updated_by VARCHAR2(128)`；`source_legacy_id NUMBER(19,0)` | `IDX(platform_park_id,device_num)` 候选；房间只由 `DL_DEVICE_ROOM_BINDING` 表达；在线、物理状态和启停分开；不含排它 `gateway_id`。 |
| `DL_GATEWAY` | `id NUMBER(19,0) PK`；`platform_park_id NUMBER(19,0) FK?`；`gateway_num VARCHAR2(128)`；`endpoint_host VARCHAR2(256)`；`endpoint_port NUMBER(10,0)`；`gateway_name VARCHAR2(256)`；`gateway_type VARCHAR2(64)`；`protocol_version VARCHAR2(64)`；`enabled_flag NUMBER(1,0)`；`connection_status VARCHAR2(32)`；`last_seen_at TIMESTAMP(6)`；`remark VARCHAR2(1000)`；审计字段同上 | `IDX(platform_park_id,gateway_num)` 候选；endpoint 真实性和地址变更另留审计。 |
| `DL_DEVICE_GATEWAY` | `id NUMBER(19,0) PK`；`device_id NUMBER(19,0) FK`；`gateway_id NUMBER(19,0) FK`；`relation_status VARCHAR2(24)`；`route_priority NUMBER(10,0)`；`valid_from/valid_to TIMESTAMP(6)`；`last_observed_at TIMESTAMP(6)`；`source_ref VARCHAR2(256)`；审计字段 | 可有多个有效关系；候选 `IDX(device_id,relation_status)`、`IDX(gateway_id,relation_status)`；不能建立 device 单列唯一 owner。 |
| `DL_DEVICE_ROOM_BINDING` | `id NUMBER(19,0) PK`；`device_id NUMBER(19,0) FK`；`platform_room_id NUMBER(19,0) FK?`；`membership_id VARCHAR2(128)`；`binding_status VARCHAR2(24)`；`valid_from/valid_to TIMESTAMP(6)`；`lodging_version NUMBER(19,0)`；`event_id VARCHAR2(256)`；审计字段 | 保留换锁/解绑历史；候选 `IDX(device_id,valid_to)`、`IDX(platform_room_id,binding_status)`；关系变化必须能追溯到生命周期 `eventId`。 |
| `DL_DEVICE_MODEL_CAPABILITY` | `id NUMBER(19,0) PK`；`model_code VARCHAR2(64)`；`firmware_range VARCHAR2(128)`；`supports_password/card/fingerprint NUMBER(1,0)`；`max_credential_slots NUMBER(10,0)`；`capability_evidence_status VARCHAR2(24)`；`evidence_ref VARCHAR2(256)`；`verified_at TIMESTAMP(6)`；审计字段 | `apply_pwd/apply_card/apply_finger` 只能成为待验证证据；候选唯一 `(model_code,firmware_range)`。 |

### 3.2 生命周期事件接收和消费游标

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_INBOX` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256)`；`event_type VARCHAR2(128)`；`schema_version NUMBER(10,0)`；`producer VARCHAR2(64)`；`aggregate_type VARCHAR2(64)`；`aggregate_id VARCHAR2(256)`；`aggregate_version NUMBER(19,0)`；`membership_id VARCHAR2(128)`；`occurred_at/received_at TIMESTAMP(6)`；`payload_ref VARCHAR2(256)`；`payload_digest VARCHAR2(128)`；`process_status VARCHAR2(32)`；`processed_at TIMESTAMP(6)`；`failure_code VARCHAR2(128)`；`replay_batch_id VARCHAR2(128)` | `event_id` 唯一候选，重复投递返回既有处理结果；`aggregate_id` 是字符串 API 聚合标识，不得改成设备/房间数字键；payload 原文只保存受控引用。 |
| `DL_INBOX_CONSUMER_CURSOR` | `id NUMBER(19,0) PK`；`consumer_name VARCHAR2(64)`；`aggregate_type VARCHAR2(64)`；`aggregate_id VARCHAR2(256)`；`last_aggregate_version NUMBER(19,0)`；`last_event_id VARCHAR2(256)`；`updated_at TIMESTAMP(6)`；`lease_token VARCHAR2(128)`；`lease_until TIMESTAMP(6)` | 候选唯一 `(consumer_name,aggregate_type,aggregate_id)`；以游标拒绝旧版本回退，发现版本缺口则进入待处理/对账，不跳过缺失事件。 |

### 3.3 人员授权和设备凭据

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_ACCESS_GRANT` | `id NUMBER(19,0) PK`；`platform_person_id NUMBER(19,0) FK?`；`platform_park_id NUMBER(19,0) FK?`；`platform_room_id NUMBER(19,0) FK?`；`device_id NUMBER(19,0) FK`；`membership_id VARCHAR2(128)`；`aggregate_id VARCHAR2(256)`；`aggregate_version NUMBER(19,0)`；`lodging_ref VARCHAR2(128)`；`business_version NUMBER(19,0)`；`effective_from/effective_to TIMESTAMP(6)`；`authorization_status VARCHAR2(32)`；`reason_code VARCHAR2(64)`；`event_id VARCHAR2(256)`；`created_at/updated_at TIMESTAMP(6)`；`created_by/updated_by VARCHAR2(128)`；`source_legacy_id NUMBER(19,0)` | 授权状态候选仅为 `PENDING_PROVISION/ACTIVE/PENDING_REVOKE/REVOKED/RECONCILIATION_REQUIRED`；`ACTIVE` 必须同时表示目标设备凭据已确认且资格仍有效；候选唯一 `(platform_person_id,device_id,membership_id,business_version)`；平台不直接写此表。 |
| `DL_CREDENTIAL` | `id NUMBER(19,0) PK`；`platform_person_id NUMBER(19,0) FK?`；`device_id NUMBER(19,0) FK`；`credential_type VARCHAR2(24)`；`device_key_id VARCHAR2(64)`；`legacy_key_id NUMBER(10,0)`；`material_ref VARCHAR2(256)`；`value_digest VARCHAR2(128)`；`material_version VARCHAR2(64)`；`credential_status VARCHAR2(24)`；`valid_from/valid_to TIMESTAMP(6)`；`revoked_at TIMESTAMP(6)`；审计字段；`source_legacy_id NUMBER(19,0)` | `device_key_id` 是锁内槽/钥匙标识；不得与 `protocol_task_id` 共列。原始密码/指纹不进普通表；候选 `IDX(device_id,credential_type,credential_status)`。同一房间可有多个凭据，设备侧按 `device_id + credential`（含槽位/类型）确认，人员/membership 资格由 grant 关系另行约束，不以房间唯一化。凭据生命周期不随单个 grant 删除。 |
| `DL_GRANT_CREDENTIAL` | `id NUMBER(19,0) PK`；`grant_id NUMBER(19,0) FK`；`credential_id NUMBER(19,0) FK`；`membership_id VARCHAR2(128)`；`relation_status VARCHAR2(24)`；`event_id VARCHAR2(256)`；`bound_at/unbound_at TIMESTAMP(6)`；`created_at/updated_at TIMESTAMP(6)`；审计字段 | 候选唯一 `(grant_id,credential_id)`；一个凭据可被多个有效业务 grant 复用。撤权只解除当前 grant 关系；仅当无其它有效 grant、无在途命令且设备侧撤销/槽位收敛得到证据时，才允许回收或标记凭据失效。 |

### 3.4 命令、尝试、回执和对账

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_COMMAND` | `id NUMBER(19,0) PK`；`command_id VARCHAR2(256) UNIQUE?`；`event_id VARCHAR2(256)`；`aggregate_id VARCHAR2(256)`；`aggregate_version NUMBER(19,0)`；`membership_id VARCHAR2(128)`；`access_grant_id NUMBER(19,0) FK?`；`credential_id NUMBER(19,0) FK?`；`device_id NUMBER(19,0) FK`；`operation VARCHAR2(32)`；`command_type VARCHAR2(64)`；`command_code VARCHAR2(64)`；`protocol_task_id VARCHAR2(128)`；`protocol_profile_id VARCHAR2(128)`；`command_status VARCHAR2(32)`；`business_version NUMBER(19,0)`；`idempotency_key VARCHAR2(256)`；`not_before/expires_at TIMESTAMP(6)`；`payload_ref VARCHAR2(256)`；`payload_digest VARCHAR2(128)`；`executor_lease_id VARCHAR2(256)`；`bridge_session_id VARCHAR2(256)`；`next_attempt_at TIMESTAMP(6)`；`last_attempt_at TIMESTAMP(6)`；`reconciliation_required_flag NUMBER(1,0)`；审计字段；`source_legacy_id NUMBER(19,0)` | smart-lock 在锁域消费事务中持久化命令；`command_id` 是 API 稳定字符串标识，内部 `id` 仅为候选数字键；`command_status` 候选枚举：`QUEUED/DISPATCHED/WAITING_ACK/SUCCEEDED/RETRY_PENDING/FAILED/EXPIRED/CANCELLED/RECONCILIATION_REQUIRED`。不放 `attempt_no`；候选唯一 `(command_id)` 和 `(idempotency_key)`，按 membership/业务版本防旧事件覆盖。 |
| `DL_COMMAND_ATTEMPT` | `id NUMBER(19,0) PK`；`command_row_id NUMBER(19,0) FK`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`event_id VARCHAR2(256)`；`membership_id VARCHAR2(128)`；`device_id NUMBER(19,0) FK`；`gateway_id NUMBER(19,0) FK`；`protocol_profile_id VARCHAR2(128)`；`protocol_task_id VARCHAR2(128)`；`executor_lease_id VARCHAR2(256)`；`bridge_session_id VARCHAR2(256)`；`dispatch_status VARCHAR2(32)`；`transport_result_type VARCHAR2(32)`；`transport_accepted NUMBER(1,0)`；`device_result_type VARCHAR2(32)`；`device_confirmed NUMBER(1,0)`；`device_outcome VARCHAR2(32)`；`dispatched_at TIMESTAMP(6)`；`ack_received_at TIMESTAMP(6)`；`result_code VARCHAR2(64)`；`transport_error_code VARCHAR2(128)`；`wire_correlation VARCHAR2(256)`；`evidence_ref VARCHAR2(256)`；`created_at TIMESTAMP(6)` | 候选唯一 `(command_id,attempt_no)`，`event_id` 用于命令消息去重；每次尝试可选不同网关但必须持有唯一租约；`attempt_no` 与授权/命令状态严格分开。 |
| `DL_RECEIPT` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256) UNIQUE?`；`command_row_id NUMBER(19,0) FK?`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`membership_id VARCHAR2(128)`；`device_id NUMBER(19,0) FK`；`gateway_id NUMBER(19,0) FK?`；`protocol_profile_id VARCHAR2(128)`；`protocol_task_id VARCHAR2(128)`；`executor_lease_id VARCHAR2(256)`；`bridge_session_id VARCHAR2(256)`；`report_kind VARCHAR2(32)`；`transport_result_type VARCHAR2(32)`；`transport_accepted NUMBER(1,0)`；`device_result_type VARCHAR2(32)`；`device_confirmed NUMBER(1,0)`；`device_outcome VARCHAR2(32)`；`result_code VARCHAR2(64)`；`packet_sequence NUMBER(10,0)`；`received_at TIMESTAMP(6)`；`match_status VARCHAR2(24)`；`payload_digest VARCHAR2(128)`；`raw_payload_ref VARCHAR2(256)` | smart-lock 只通过回传 API 写入；`event_id` 是回执去重键；保留 late/unmatched/duplicate 回执；B8 分包字段是协议候选，现场需验证；原始帧通过受控引用保存；仅 `device_confirmed=true` 的合法回执推进授权。 |
| `DL_BRIDGE_OUTBOX` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256) UNIQUE?`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`membership_id VARCHAR2(128)`；`protocol_profile_id VARCHAR2(128)`；`device_id NUMBER(19,0) FK?`；`gateway_id NUMBER(19,0) FK?`；`payload_ref VARCHAR2(256)`；`payload_digest VARCHAR2(128)`；`delivery_status VARCHAR2(32)`；`available_at/claimed_at TIMESTAMP(6)`；`claim_token VARCHAR2(256)`；`created_at TIMESTAMP(6)` | Bridge 自己的待回传结果/设备事件 outbox，只以 `event_id` 唯一；同一 command/attempt 必须允许传输 ACK、多个分包 ACK 和最终设备结果各占一行，不能对 command/attempt 建唯一约束。重投同一事件复用 event_id；不由其他模块直接写。 |
| `DL_BRIDGE_DELIVERY` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256)`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`membership_id VARCHAR2(128)`；`protocol_profile_id VARCHAR2(128)`；`protocol_task_id VARCHAR2(128)`；`executor_lease_id VARCHAR2(256)`；`bridge_session_id VARCHAR2(256)`；`device_id NUMBER(19,0) FK?`；`gateway_id NUMBER(19,0) FK?`；`delivery_status VARCHAR2(32)`；`transport_result_type VARCHAR2(32)`；`transport_accepted NUMBER(1,0)`；`device_result_type VARCHAR2(32)`；`device_confirmed NUMBER(1,0)`；`device_outcome VARCHAR2(32)`；`sent_at/ack_at TIMESTAMP(6)`；`result_code VARCHAR2(64)`；`evidence_ref VARCHAR2(256)` | Bridge 以 `(command_id,attempt_no)` 唯一持久化一次发送尝试、租约/会话和结果摘要；逐条回执由 DL_BRIDGE_OUTBOX 保存；只能通过 API 回传 smart-lock，由 smart-lock 写 `DL_COMMAND_ATTEMPT`/`DL_RECEIPT`。 |
| `DL_RECONCILIATION` | `id NUMBER(19,0) PK`；`device_id NUMBER(19,0) FK`；`credential_id NUMBER(19,0) FK?`；`command_row_id NUMBER(19,0) FK?`；`command_id VARCHAR2(256)`；`attempt_no NUMBER(10,0)`；`membership_id VARCHAR2(128)`；`reason_code VARCHAR2(64)`；`expected_state VARCHAR2(64)`；`observed_state VARCHAR2(64)`；`case_status VARCHAR2(24)`；`opened_at/resolved_at TIMESTAMP(6)`；`evidence_ref VARCHAR2(256)`；`owner_ref VARCHAR2(128)`；`notes VARCHAR2(2000)` | `case_status` 独立于命令和授权；迟到新增成功、槽位复用、回执丢失都进入对账，不自动重放远程开门。 |

### 3.5 Outbox、运行历史和审计

| 候选表 | 字段（候选 Oracle 类型；`PK`/`FK`/`IDX` 为候选） | 关键约束/说明 |
|---|---|---|
| `DL_OUTBOX` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256) UNIQUE?`；`producer_service VARCHAR2(64)`；`aggregate_type VARCHAR2(64)`；`aggregate_id VARCHAR2(256)`；`aggregate_version NUMBER(19,0)`；`membership_id VARCHAR2(128)`；`event_type VARCHAR2(128)`；`schema_version NUMBER(10,0)`；`event_key VARCHAR2(256)`；`payload CLOB`；`payload_digest VARCHAR2(128)`；`outbox_status VARCHAR2(32)`；`available_at TIMESTAMP(6)`；`claim_token VARCHAR2(128)`；`claimed_at TIMESTAMP(6)`；`publish_attempts NUMBER(10,0)`；`published_at TIMESTAMP(6)`；`last_error_code VARCHAR2(128)`；`created_at TIMESTAMP(6)` | `aggregate_id` 是字符串 API 标识，不能用 `NUMBER`；生命周期 outbox 由 platform 与住宿事实同一 Oracle 本地事务写入，platform 不写授权；候选状态 `PENDING/CLAIMED/PUBLISHED/FAILED/RECONCILIATION_REQUIRED`；claim 并发语义须由 DBA/实现验证。 |
| `DL_DEVICE_EVENT` | `id NUMBER(19,0) PK`；`event_id VARCHAR2(256)`；`device_id NUMBER(19,0) FK?`；`gateway_id NUMBER(19,0) FK?`；`platform_person_id NUMBER(19,0) FK?`；`membership_id VARCHAR2(128)`；`event_kind VARCHAR2(64)`；`action_code VARCHAR2(64)`；`event_time TIMESTAMP(6)`；`outcome VARCHAR2(32)`；`source_kind VARCHAR2(64)`；`source_schema VARCHAR2(128)`；`source_pk VARCHAR2(256)`；`payload_digest VARCHAR2(128)`；`payload_ref VARCHAR2(256)`；`created_at TIMESTAMP(6)` | 合并旧设备日志、事件和开门记录时用 `source_kind/source_schema/source_pk` 复合来源键并保留 `event_id`；不能代替命令状态。 |
| `DL_AUDIT_EVENT` | `id NUMBER(19,0) PK`；`actor_ref VARCHAR2(128)`；`platform_park_id NUMBER(19,0) FK?`；`action VARCHAR2(64)`；`object_type VARCHAR2(64)`；`object_id NUMBER(19,0)`；`outcome VARCHAR2(32)`；`correlation_id VARCHAR2(256)`；`event_time TIMESTAMP(6)`；`detail_redacted CLOB`；`evidence_ref VARCHAR2(256)` | 密码、指纹、恢复密钥、完整原始帧不可写入 `detail_redacted`；候选 `IDX(object_type,object_id,event_time)`。 |
| `DL_SYSTEM_SETTING` | `id NUMBER(19,0) PK`；`scope_type VARCHAR2(32)`；`scope_id NUMBER(19,0)`；`setting_key VARCHAR2(128)`；`value_ref VARCHAR2(256)`；`value_digest VARCHAR2(128)`；`setting_status VARCHAR2(24)`；`version_no NUMBER(19,0)`；审计字段 | 独立后台设置仅在非敏感且有归属时迁移；密钥/模板只保存受控引用，不保存明文。 |
| `DL_OPERATIONS_ALERT` | `id NUMBER(19,0) PK`；`platform_park_id NUMBER(19,0) FK?`；`severity VARCHAR2(16)`；`alert_type VARCHAR2(64)`；`related_type VARCHAR2(64)`；`related_id NUMBER(19,0)`；`message_redacted VARCHAR2(2000)`；`read_status VARCHAR2(24)`；`created_at/updated_at TIMESTAMP(6)`；`source_legacy_id NUMBER(19,0)` | 旧消息/告警用于运维可见性，不改变授权/命令事实。 |

### 3.6 本人短时请求与防重放

| 候选表 | 候选字段 | 必需逻辑约束 |
| --- | --- | --- |
| `DL_SELF_SERVICE_REQUEST` | `id NUMBER(19,0)`；`subject_ref/membership_id/request_id/verification_digest VARCHAR2(256)`；`purpose/status VARCHAR2(64)`；`payload_digest VARCHAR2(128)`；`command_id VARCHAR2(256)` 可空；`expires_at/consumed_at/created_at TIMESTAMP(6)` | 门锁域保存本人请求、校验引用消费摘要及幂等结果；`(subject_ref,purpose,request_id)` 和一次性校验引用消费键唯一。同键不同 payload 拒绝；reveal 秘密不保存在本表或幂等响应缓存；超时响应丢失后再次读取需新校验。 |

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
- `grantRevision` 对应 `DL_ACCESS_GRANT.business_version`，`grantId` 为授权行 ID 的字符串表示；`aggregateVersion` 是住宿事件版本，不能与 grantRevision 混用。`wireTaskId` 对应 attempt/delivery/receipt 的 `protocol_task_id`；Command 上同名字段若保留，只作旧来源快照，不是运行态关联权威。
- 凭据命令必须带历史 `membership_id`、grant 与槽位快照；设备配置/管理员开门不伪造会员关系，另存 `target_kind VARCHAR2(32)`、`target_asset_version NUMBER(19,0)`、操作者和批准范围。相关字段条件可空，以契约 targetKind 校验。
- 设备房间绑定是锁域资产关系，不属于某一个住户；绑定行的 `membership_id` 只可作可选来源线索，不可用于独占或查找当前房间成员。一个房间可有多个住户，变化必须重算全部受影响授权。
- 同一有效设备编号和网关身份不可歧义；同一设备同一时间的有效房间绑定不可冲突。同一设备钥匙类型/槽位不能同时属于两个有效凭据版本。并发约束需 Oracle 条件唯一键或受控行锁实现并测试，不只靠查询后插入。
- `DL_DEVICE_MODEL_CAPABILITY` 补 `protocol_profile_id VARCHAR2(128)`，`DL_RECEIPT` 补事实发生时间 `occurred_at TIMESTAMP(6)`；`DL_OUTBOX` 补同名事实时间，接收/创建时间不替代它。
- platform 负责稳定人员聚合版本分配，依据现有主键方案选择平台持久版本记录/行锁；同事务保证 Outbox 的 `(aggregate_type,aggregate_id,aggregate_version)` 唯一。锁域 Inbox 另保存已消费游标，不回写平台版本。
- 每个迁移 batch 绑定一个不可变 snapshotId/源导出摘要，新增快照必须新批次。稳定 ID map 不含 rowhash，版本行账本含 batch/source/targetRole；复合键过长时可用规范化来源身份摘要加原始字段碰撞校验，不直接假定任意长度联合索引可建。
- “全部历史迁入 Oracle”默认包括结构化历史和可解释归档内容，不只保存指向旧 MySQL 的链接：`DL_LEGACY_ROW_ARCHIVE` 增加候选 `archive_payload CLOB/BLOB` 保存批准的脱敏历史或加密归档，`archive_ref` 只作同库对象/受控材料定位，不得依赖旧 MySQL 在线读取。历史事件补安全人员姓名/工号、设备位置快照，保证人员后续变更不篡改旧记录。敏感原文的排除或外部保管必须有 T004 明确批准，不默认为已迁完。
- 所有事件/命令/尝试/审计需有 `trace_id VARCHAR2(128)` 或稳定关联键；幂等键至少按服务、主体、操作范围隔离，不能让两个用户相同输入 key 发生串用。

| 表归属 | 唯一写入服务 |
| --- | --- |
| 平台住宿事实、平台聚合版本、生命周期 DL_OUTBOX | smart-platform |
| DL_INBOX、消费游标、设备资产/绑定、Grant/Credential/Command/Attempt/Receipt、本人请求、迁移与历史审计 | smart-lock |
| DL_BRIDGE_DELIVERY、DL_BRIDGE_OUTBOX | smart-bridge-lock |

同一 Oracle 实例不表示可跨服务任意改表；分别定义运行账号/对象权限。Bridge 必须在本地事务中持久化接收结果和回传事件，重启后先恢复账本。内部事件原文应是最小化非敏感 payload 或受控引用，引用的生命周期必须覆盖恢复窗口。

## 4. 状态与事务规则

### 4.1 状态不可混用

- `DL_ACCESS_GRANT.authorization_status` 描述当前 membership 的业务授权和设备
  资格，候选状态仅为 `PENDING_PROVISION`、`ACTIVE`、`PENDING_REVOKE`、
  `REVOKED`、`RECONCILIATION_REQUIRED`；`ACTIVE` 必须同时有该授权所需全部设备凭据确认和
  仍有效的资格。它与命令状态分列、分索引、分审计；到期/过期通过 membership
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
   `DL_GRANT_CREDENTIAL` 和需要的 `DL_COMMAND`；事务提交后才调用 Bridge 投递。
   Bridge 只执行已生成的命令，并在自己的 `DL_BRIDGE_OUTBOX`/
   `DL_BRIDGE_DELIVERY` 持久化发送与回传去重，不能直接写锁域的 attempt/receipt。
3. 发送采用至少一次语义：使用事件/业务版本、幂等键、尝试记录和对账；不承诺
   物理设备 exact-once。远程开门命令过期后不可由恢复扫描无条件重放。
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
