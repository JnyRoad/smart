# `lk_*` 旧表到 Oracle 候选模型的字段级映射

## 1. 使用说明

本表是源码级迁移映射，不是旧库 DDL 的证明。列名按实体字段和 Mapper XML
可见内容记录；没有 XML/注解明确列名时，snake_case 仅是 ORM 推断。目标列、
精度、长度和约束见 [data-model.md](data-model.md)，全部仍须 Oracle metadata
复核。迁移只在隔离环境、经批准的脱敏数据集和 dry-run 通过后实施；本轮不执行
DDL、DML、真实迁移或生产切换。

处理标记：

- **主映射**：进入新的在线门锁域，但不复制平台主数据。
- **归档不沿用**：保存可解释的来源关系，不能作为新登录、园区权限或授权真相。
- **历史日志**：迁入历史/审计/告警，只用于查询和证据，不能驱动命令。
- **设置候选**：只有非敏感且确认归属后才进入在线设置；否则归档。
- **待 metadata**：源码看得见，但旧库物理约束、行值或运行语义未核实。

来源库与目标库职责分开：旧 MySQL/source DB 负责提供脱敏 snapshot、真实列/约束、
来源主键、删除标记和最终冻结边界；目标 Oracle 负责核对平台人员/园区/房间引用、
拟用对象的列/约束/索引/分区/LOB 以及写入权限。不能用目标 Oracle 的 metadata
替代旧库字段证据，也不能把旧 MySQL 的 ORM 推断当作 Oracle DDL。

## 2. 主业务表字段映射

### 2.1 `lk_park`：旧园区副本 → 平台园区引用

来源：`smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkPark.java:16-45`。

| 旧列（Java 字段） | 目标候选 | 规则 |
|---|---|---|
| `id` (`Long`) | `DL_MIGRATION_ROW.source_pk`；必要时归档 | 旧门锁内部 ID 不成为平台园区 ID。 |
| `target_park_id` (`Long`) | `platform_park_id` | 以平台园区元数据核对；空值、重复或跨园区进入异常，不自动择一。 |
| `park_name` | 平台名称快照（仅审计/归档） | 不覆盖平台主数据。 |
| `remark` | `DL_AUDIT_EVENT.detail_redacted` 或归档 | 需脱敏审查。 |
| `create_user/create_time/update_user/update_time` | 迁移审计 | 保留来源值和时区解释，不当作 Oracle 当前审计人。 |

### 2.2 `lk_person`：旧人员副本 → 平台人员 + 凭据处置

来源：`.../entity/LkPerson.java:17-64`，显式 XML resultMap 在
`.../mapper/LkPersonMapper.xml:7-22`；人员与园区联表在 `:54-83`。

| 旧列（Java 字段/推断列） | 目标候选 | 规则 |
|---|---|---|
| `id` | `DL_MIGRATION_ROW.source_pk`；`DL_LEGACY_ROW_ARCHIVE` | 保存来源键，不作为平台人员 ID。 |
| `park_id` | `platform_park_id` | 必须经 `lk_park.target_park_id` 和平台园区核对。 |
| `person_num` | 平台人员稳定工号引用/快照 | 工号空、重复或跨园区冲突进入 `EXCEPTION`，禁止自动取第一条。 |
| `person_name/person_phone` | 平台人员查询；必要时迁移快照 | 不覆盖平台事实；手机号属于个人信息，限制归档访问。 |
| `finger_code_list` | `DL_CREDENTIAL.material_ref` 或 `DL_LEGACY_ROW_ARCHIVE` | 不写明文普通表；B8/设备能力和轮换策略未验证。 |
| `card_no` | `DL_CREDENTIAL` 的卡类元数据 | 原始卡号是否敏感由安全评审确定，保留摘要/引用。 |
| `password` | 仅受控加密材料引用/摘要 | 旧 AES 风险需轮换；禁止把旧密文直接宣称可在线使用。 |
| `remark` | 迁移备注/归档 | 脱敏。 |
| `sync_flag/status` | `DL_MIGRATION_ROW.classification` 或人员快照 | 不映射成设备已生效状态。 |
| `create_* / update_*` | 迁移审计 | 时区和空值语义待核对。 |

### 2.3 `lk_device`：设备资产 → `DL_DEVICE` + 房间/网关关系

来源：`.../entity/LkDevice.java:12-45`；XML resultMap 在
`.../mapper/LkDeviceMapper.xml:5-29`。

| 旧列 | 目标候选 | 规则 |
|---|---|---|
| `id` | `DL_DEVICE.id` 或 `source_legacy_id` | 是否保留原 ID 由 ID 碰撞检查决定。 |
| `park_id` | `DL_DEVICE.platform_park_id` | 平台园区引用，不能新建 `DL_PARK` 主表。 |
| `device_num/device_name/device_type` | `device_num/device_name/model_code` | 设备编号唯一性按园区/型号核对；类型不等于能力认证。 |
| `room_id` | `DL_DEVICE_ROOM_BINDING.platform_room_id` | 绑定历史化；换锁/解绑不能覆盖旧待撤权。 |
| `device_area` | `DL_DEVICE` 位置快照/归档 | 不替代平台房间。 |
| `device_power/sound_volume/auto_lock_time/sound_mode` | 设备设置候选 | 单位、范围和写入能力待现场核验。 |
| `hardware_version/firmware_version` | 同名设备版本字段 | 作为能力证据输入，不能自动激活某种凭据。 |
| `enable_two_verify/open_status/connect_status` | 分拆为业务设置、物理状态、在线状态 | 旧数字枚举不直接沿用；分别定义状态字典。 |
| `gateway_id` | `DL_DEVICE_GATEWAY.gateway_id` | 只生成关系候选；不保留排它 owner。 |
| `is_available/del_flag/report_state_time` | `enabled_flag`、归档/状态时间 | XML resultMap 对部分字段不完整，必须查旧列和样本。 |
| `remark/create_*/update_*` | 同名审计/归档 | 审计人类型需核实。 |

### 2.4 `lk_gateway`：网关资产 → `DL_GATEWAY`

来源：`.../entity/LkGateway.java:11-32`；XML resultMap 在
`.../mapper/LkGatewayMapper.xml:7-22`。

`id -> DL_GATEWAY.id/source_legacy_id`；`park_id -> platform_park_id`；
`gateway_num -> gateway_num`；`gateway_ip -> endpoint_host`；
`gateway_port -> endpoint_port`；`gateway_name -> gateway_name`；
`gateway_type -> gateway_type`；`gateway_area -> 位置快照`；
`connect_status -> connection_status`；`remark/create_*/update_* -> 审计/归档`。
地址、端口和连接状态必须以现场配置/回读为准，不能仅依赖旧行值。

### 2.5 `lk_device_model`：型号能力 → `DL_DEVICE_MODEL_CAPABILITY`

来源：`.../entity/LkDeviceModel.java:12-29`。

`id -> source_legacy_id`；`device_type -> model_code`；`type_name -> model_name
(候选)`；`apply_pwd/apply_card/apply_finger -> supports_*（仅待验证证据）`；
`remark -> evidence_note`；`del_flag -> archive_status`；`create_user/create_time/
update_user/update_time -> 审计`。旧配置存在不等于设备型号和固件已验证支持。

### 2.6 `lk_device_permissions`：人员-设备授权 → `DL_ACCESS_GRANT`

来源：`.../entity/LkDevicePermissions.java:17-62`，XML 明确字段见
`.../mapper/LkDevicePermissionsMapper.xml:7-20`，联表投影见 `:22-44,46-156`。

| 旧列 | 目标候选 | 规则 |
|---|---|---|
| `id` | `DL_ACCESS_GRANT.source_legacy_id` | 新授权 ID 不与旧 ID 直接假定相同。 |
| `park_id/person_id/device_id` | 平台园区/人员引用、`DL_DEVICE.id` | `person_id` 先经 `lk_person.person_num` 和平台人员匹配。 |
| `valid_time_start/valid_time_end` | `effective_from/effective_to` | 时区、精度、空值和结束边界需验证。 |
| `status` | `authorization_status` 的迁移分类 | 不把旧 ACTIVE 直接写成“设备已确认”；以当前 membership、回执和对账证据重建。 |
| `del_flag` | 迁移 `disposition`/`classification` 和撤权待处理线索 | 逻辑删除不等于设备撤权完成；只有明确设备 ACK 或人工核验设备已无凭据时才可进入 `REVOKED`，否则保留 `PENDING_REVOKE` 或 `RECONCILIATION_REQUIRED`。 |
| `remark/create_*/update_*` | 审计/迁移备注 | 不能存敏感原文。 |

`person_num/person_name/device_name/connect_status` 等在 XML 的
`personDeviceMap` 中是联表投影，不是 `lk_device_permissions` 自身列；不得
在目标表重复存成可变主数据。

### 2.7 `lk_key`：设备凭据 → `DL_CREDENTIAL`

来源：`.../entity/LkKey.java:14-46`；XML 在
`.../mapper/LkKeyMapper.xml:5-41`。

`id -> source_legacy_id/credential_id`；`key_id -> device_key_id`（源类型为
`Integer`，目标用字符串候选以容纳协议表示）；`key_type -> credential_type`；
`key_value -> material_ref/value_digest`，不得直接复制明文/旧密文；
`person_id -> platform_person_id`；`device_id -> DL_DEVICE.id`；`del_flag ->
credential_status`（只表示来源行处置/候选状态，不能据此断言设备已撤权）；
`create_*/update_* -> 审计`。与授权的生效关系必须经
`DL_GRANT_CREDENTIAL` 建立，不能把一把钥匙复制成每个 grant 各自独立的凭据。

XML 的 `LkKeyExtMap.status` 来自 `lk_device_task` 的最近任务子查询
(`:16-26,28-41`)，不是 `lk_key` 物理列。`key_id` 与任务报文中的业务 task
ID/协议 task ID 必须分开存储。

### 2.8 `lk_device_task`：旧任务 → 分类导入，不重放

来源：`.../entity/LkDeviceTask.java:14-53`；明确映射见
`.../mapper/LkDeviceTaskMapper.xml:5-20,46-60,62-99`。

| 旧列 | 目标候选 | 规则 |
|---|---|---|
| `id` | `DL_MIGRATION_ROW.source_pk`；必要时 `DL_COMMAND.source_legacy_id` | 保存来源证据，不作为新协议 task ID。 |
| `device_id` | `DL_COMMAND.device_id` | 设备存在性和房间绑定需校验。 |
| `command_type/command_code/command` | `operation/command_type/command_code/payload_ref` | 原始 payload 只进受控引用/摘要。 |
| `command_id` | 通过任务类型解析到 `DL_CREDENTIAL` 或旧钥匙来源 | 源代码把它 join 到 `lk_key.id`，不能当协议 taskId。 |
| `start_time/end_time` | `not_before/expires_at` | 源类型为 epoch `Long`；单位、时区和 0 值必须先抽样确认。 |
| `status` | `DL_COMMAND.command_status` 分类 | 旧数字状态按来源版本转换；不得把旧“下发成功”当设备 ACK。 |
| `times` | `DL_COMMAND_ATTEMPT.attempt_no` 的迁移线索 | 不是可靠的逐次尝试账本；必须从行值和日志分类。 |
| `remark/create_*/update_*` | 迁移备注/审计 | 原始错误信息脱敏。 |

目标命令状态采用 `QUEUED/DISPATCHED/WAITING_ACK/SUCCEEDED/RETRY_PENDING/
FAILED/EXPIRED/CANCELLED/RECONCILIATION_REQUIRED`。旧未完成任务必须保存原状态
和分类结论，不由迁移程序批量重发。

### 2.9 来源稳定身份、授权-凭据关系和事件围栏

- 每个可迁移来源行先按 `source_system + source_schema + source_table + source_pk`
  查 `DL_LEGACY_ID_MAP`。该表的 `target_role` 唯一键保持稳定目标 ID；不能因
  增量 snapshot 的 `source_row_hash` 变化而新建第二个设备、凭据或归档对象。
- `DL_MIGRATION_ROW` 的 `(batch_id,source_system,source_schema,source_table,source_pk,target_role,source_row_hash)` 只防同一
  snapshot/version 重复，不承担跨版本稳定身份。每个 batch 都应保留 row hash、
  处置和证据，并通过稳定 map 得到原 target ID；断点续跑按同一 snapshot 身份恢复。
- 旧 `lk_device_permissions` 与 `lk_key` 只提供候选人员/设备/钥匙来源；
  `membership_id`、生命周期 `event_id` 和有效期由平台事实/`smart-lock` Inbox
  消费时确定。`DL_GRANT_CREDENTIAL` 允许多个有效 grant 共享一条设备凭据。
  撤销单个 grant 只解除关系并发撤权命令；无其它有效关系、无在途命令且设备侧
  撤销/槽位收敛有证据时，才收敛凭据状态。
- 生命周期事件的 `event_id`、字符串 `aggregate_id`、`aggregate_version` 和
  `membership_id` 必须随 Inbox、授权、命令和回执保留；旧行没有这些字段时不得
  编造，使用迁移批次/来源引用并标记待补证。

## 3. 设置、账号、历史和告警表映射

### 3.1 独立账号/菜单（归档，不沿用）

| 旧表 | 源码字段（相对路径:行） | 处置 |
|---|---|---|
| `lk_system_user` | `.../entity/LkSystemUser.java:16-48`：`id,user_name,password,phone,nick_name,del_flag,create_user,create_time,update_user,update_time`；XML 还出现 mapper-only `park_id`：`.../mapper/LkSystemUserMapper.xml:7-19` | 写入 `DL_LEGACY_ROW_ARCHIVE`/`DL_MIGRATION_ROW`；不沿用独立登录。`park_id` mismatch 必须 metadata 核对。 |
| `lk_system_role` | `.../entity/LkSystemRole.java:16-53`：`id,park_id,role_name,role_code,role_desc,ds_type,ds_scope,del_flag,create_user,create_time,update_user,update_time` | 归档；角色/数据权限改由既有 UPMS。 |
| `lk_system_menu` | `.../entity/LkSystemMenu.java:16-62`：`id,park_id,menu_name,permission,path,parent_id,icon,component,sort,keep_alive,type,del_flag,create_user,create_time,update_user,update_time` | 归档；不复制为新后台菜单权威。 |
| `lk_system_role_menu` | `.../entity/LkSystemRoleMenu.java:7-13`：`role_id,menu_id` | 归档关系，不重建独立授权。 |
| `lk_system_user_role` | `.../entity/LkSystemUserRole.java:7-13`：`user_id,role_id` | 归档关系，不重建独立登录。 |
| `lk_system_user_park` | `.../entity/LkSystemUserPark.java:7-13`：`user_id,park_id` | 归档关系；园区范围从现有平台权限取得。 |

密码字段无论是 `lk_system_user.password` 还是 `lk_person.password` 都不应进入
普通迁移表；只有经批准的加密归档引用可恢复，且需轮换和审计方案。

### 3.2 系统设置与运行消息

| 旧表 | 源码字段 | 处置 |
|---|---|---|
| `lk_system_config` | `.../entity/LkSystemConfig.java:17-46`：`id,park_id,system_name,logo_img,create_user,create_time,update_user,update_time` | `system_name` 可作为非敏感设置候选；`logo_img` 作为受控 BLOB/对象引用候选；不覆盖平台品牌配置。 |
| `lk_warn_config` | `.../entity/LkWarnConfig.java:11-32`：`id,park_id,electric_threshold,enable_ele_warn,offline_threshold,enable_offline_warn,email_list,enable_tip,tip_intervals,remark,create_user,create_time,update_user,update_time` | 映射 `DL_OPERATIONS_ALERT`/设置候选；邮箱列表需个人信息和发送授权审查；不改变命令状态。 |
| `lk_message_config` | `.../entity/LkMessageConfig.java:11-21`：`id,device_id,type,next_send_time,create_time` | 归档或转通知策略；时间/类型枚举待验证。 |
| `lk_message_record` | `.../entity/LkMessageRecord.java:11-25`：`id,park_id,device_id,message,read_status,type,create_time,update_user,update_time` | 映射 `DL_OPERATIONS_ALERT`；正文脱敏，不能作为授权事实。 |

### 3.3 历史日志和开门记录

| 旧表 | 源码字段 | 目标候选 |
|---|---|---|
| `lk_device_log` | `.../entity/LkDeviceLog.java:11-21`：`id,park_id,device_id,action_type,param,remark,create_time` | `DL_DEVICE_EVENT`；`param` 只保存摘要/受控引用，并写 `source_kind/source_schema/source_pk`。XML 联表投影见 `.../mapper/LkDeviceLogMapper.xml:15-53`。 |
| `lk_event_record` | `.../entity/LkEventRecord.java:16-48`：`id,gateway_id,device_id,event_type,event_desc,create_user,create_time,update_user,update_time` | `DL_DEVICE_EVENT`；保留 source kind、source schema/pk 和原始时间解释。 |
| `lk_open_record` | `.../entity/LkOpenRecord.java:14-47`：`id,park_id,device_id,device_name,device_area,open_type,person_id,person_num,person_name,open_time` | `DL_DEVICE_EVENT`；人员名称/工号是历史快照，不覆盖平台人员；XML resultMap 未列 `person_id`，需旧库核验；以 source kind/schema/pk 防多源合并碰撞。 |
| `lk_modify_pwd_log` | `.../entity/LkModifyPwdLog.java:13-30`：`id,password,park_id,device_id,device_name,device_area,status,type,remark,create_user,create_time` | `DL_AUDIT_EVENT`；`password` 不迁明文，保留摘要/处置结果。 |

## 4. 重复运行、异常与敏感值策略

1. 每条 snapshot 源记录先由稳定 `DL_LEGACY_ID_MAP` 解析目标身份，再以
   `source_table + source_pk + source_row_hash` 写入 `DL_MIGRATION_ROW`；后者只
   防同版本重复。增量 row hash 变化仍复用稳定 target ID，不创建重复有效授权或
   触发发送；resume 必须绑定同一 snapshot 身份/水位。
2. 人员工号、园区映射、设备编号、房间引用、凭据类型/槽位或时间无法唯一解释
   时写 `EXCEPTION`，不自动择第一条。
3. 旧任务按 `SUCCEEDED/FAILED/EXPIRED/CANCELLED/RECONCILIATION_REQUIRED` 等
   迁移分类保留证据；旧的未完成任务不整表重放。
4. 旧 `del_flag`、旧授权 `status` 和旧凭据删除状态只作为来源分类；不能直接把
   逻辑删除写成 `REVOKED`，必须经过设备 ACK/人工核验和 membership 围栏。
5. 旧密码/指纹/卡号只有在能力、密钥轮换、合规授权和现场抽样均通过后才能进入
   受控迁移流程。数据库没有该值不代表锁内值已失效；撤权以设备确认和对账为准。
6. 旧 SQL 的 `LIMIT`、`str_to_date`、`concat`、MySQL 时间/空字符串语义必须在
   Oracle 实施时重写并用目标版本计划验证；不能把源码查询直接当 Oracle SQL。

## 5. 迁移前必补的 metadata 与样本

- 旧 MySQL `information_schema` 的全部 `LK_%` 对象和 22 个实体之外的未知表/视图、
  触发器/序列清单；目标 Oracle 则另行列出现有平台字段和拟用对象，不把两边
  对象清单混为一谈。
- 旧来源表的真实列、PK/FK、默认值、NULL/空串值、索引、分区、LOB 和字符
  长度语义；尤其是 `lk_system_user.park_id` mismatch、`LkDevice` 缺失字段和
  `LkKey` 外键/逻辑删除字段。
- 旧 MySQL 产品/版本、时区、字符集、ID/epoch 范围、来源删除状态、B8 真实帧
  样本（脱敏）、命令
  与 ACK 关联样本；不能用源码或历史 JAR 推断现场设备能力。
- 目标 Oracle 现有平台字段/拟用对象的 `ALL_*` metadata、权限、索引/分区/LOB
  与字符语义；不得拿这些结果反推旧 MySQL 的真实列。
- 历史日志多源合并时按 `source_kind + source_schema + source_pk` 复合身份，
  保留删除行、无 `updatedAt` 行和最终冻结全量的核验结果；不得用单一旧自增
  `id` 直接去重。
- 迁移前后按 source key、target key、有效授权、设备凭据、命令分类和对账案例
  做双向抽样；真实行数必须来自受控只读查询，历史 `TABLE_ROWS` 估计值不得作为
  验收计数。
