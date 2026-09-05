# Oracle 数据基线、只读证据与开工门禁

## 1. 基线结论

- Oracle 是用户已确认的新在线唯一数据库；本轮没有真实 Oracle 连接，也没有
  执行 DDL/DML、迁移、索引创建、统计信息刷新或生产操作。
- 目标 Oracle 产品版本、schema owner、表空间、字符集、时区、兼容参数、连接
  池和部署版本均为 **UNVERIFIED**。本文件不虚构版本，也不把 19c 文档当成
  目标实例已确认版本。
- 19c 官方文档可作为版本化讨论参考：[NULL/空串语义](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Nulls.html)、
  [Oracle 数据类型](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Data-Types.html)。
  在目标版本确认前，链接只支持待核对事项，不是上线证据。
- 重构源码当前配置是 MySQL：
  `smart-module/smart-lock/smart-lock-biz/src/main/resources/application.yml:9-21`
  使用 MySQL driver，`.../config/MybatisPlusConfig2.java:10-15` 使用
  `DbType.MYSQL`。这是旧运行配置事实，不是 Oracle 目标 DDL；旧 Mapper 还含
  `LIMIT`、`str_to_date`、`concat` 等 MySQL 方言，需要单独改写和计划验证。

## 2. 设计级可确认事实与不可确认事实

### 可从安全配置/源码确认

| 项目 | 证据 | 结论 |
|---|---|---|
| 数据库目标 | 用户本轮确认 | 新在线业务、命令、审计使用 Oracle；旧 MySQL 不做新系统在线双写。 |
| 旧 ORM 表 | `source-inventory.md` 的 22 个 `@TableName` 实体 | 仅能确认源码可见实体，不等于物理表全集。 |
| 旧时间/字段类型 | 22 个实体字段声明、Mapper XML | 可生成候选映射；不能确认物理精度/默认值/约束。 |
| 任务/凭据关系 | `LkDeviceTaskMapper.xml:70-73`、`LkKeyMapper.xml:28-41` | 旧 `command_id` 指向钥匙行；协议 task ID 与设备 `key_id` 需分列。 |
| B8/ACK 形态 | `LkKeyServiceImpl.java:294-423` | 源码存在 B8 分包/终态判断；现场语义和固件支持仍待证。 |
| 配置安全边界 | `application.yml` 使用 `SMART_LOCK_*` 占位符；源码仓库 README | 不读取/打印 `.env`，不在文档写凭据。 |

### 必须由 DBA/只读环境补证

- `SYS_CONTEXT`/会话环境允许的目标 schema、Oracle `version`/兼容参数、实例
  时区、数据库时区、字符集、NLS 数字/日期格式和 JDBC 驱动兼容性。
- 目标 Oracle 的 `ALL_TABLES`、`ALL_TAB_COLUMNS`、`ALL_CONSTRAINTS`、
  `ALL_CONS_COLUMNS`、`ALL_INDEXES`、`ALL_IND_COLUMNS`、`ALL_TAB_PARTITIONS`、
  `ALL_LOBS` 和 `ALL_TAB_COMMENTS` 受控只读结果；包含 owner、现有平台字段与
  拟用对象的列精度/长度、NULL、默认值、PK/FK、唯一键、索引列顺序、分区/LOB
  storage。候选新表未建前不能宣称有对象 metadata。
- 目标表空间配额、LOB/分区策略、读写账号权限、锁/事务隔离、序列/identity
  策略、连接池和并发 claim 权限。
- 旧 MySQL/source DB 的 `information_schema` 物理表全集和真实行数；源数据库
  产品/版本、字符集、时区、空值/空字符串样本、删除状态、epoch 单位、B8/ACK
  脱敏样本。源码或目标 Oracle metadata 不是这些来源事实的替代品。

### 2.1 待证项、责任角色和任务阻塞边界

以下责任为待指定的角色，不是已取得某位人员批准。编号对应当前 [tasks.md](tasks.md)。

| 待证项 | 责任角色 | 所需证据 | 阻塞范围 |
| --- | --- | --- | --- |
| Oracle 版本、Schema、字符集/时区、配额与平台对象 | Oracle DBA、平台数据库负责人 | T002 的带时间戳脱敏元数据/权限报告 | T005 物理定版、T012/T013 真实库验证及 T068 迁移演练；不阻塞 T010/T011 等契约 |
| 旧 MySQL 物理对象、精确计数、删除状态、可靠快照/水位 | 旧库负责人、迁移负责人 | T003 的 information_schema 清单、快照标识和计数口径 | T062～T068 的真实数据处理；合成转换和测试仍可开发 |
| 平台稳定身份、membership 与版本分配 | 平台后端负责人 | T018/T021 事务/入口测试和主数据契约 | T026～T029 实际平台闭环；不阻塞编解码 |
| Outbox/Inbox 事务、表归属与最小权限 | 平台、门锁、Bridge 后端负责人 | T006/T014 的边界测试及 T013 数据库角色验证 | T026～T035 的持久联调；不得跨服务改表 |
| Bridge 发送去重、多个回执及崩溃恢复 | Bridge 后端负责人 | T022～T025 与 T033/T034 的数据库/模拟恢复证据 | T039 集成闭环及 T075 实锁验收；不阻塞离线样本解析 |
| 多 grant 共用 credential、最后资格撤销 | 门锁领域负责人 | T020/T029 的 TEST-026 状态/并发断言 | 授权可靠性验收及实际凭据接管；不阻塞 UI 布局 |
| 型号/固件/B8/ACK/网络身份与全域成员 | 设备协议、现场运维负责人 | T075 的 TEST-008/TEST-011/TEST-012 profile 证据 | T076/T077 演练和真实发送；TEST-025 是 Oracle 语义，不是设备测试 |
| 凭据恢复、轮换、重采与历史保留 | 业务、安全、数据负责人 | T004 批准的材料处置/保留/访问方案 | T017 的真实材料、T064 真实敏感迁移和 T081 本人实际 reveal；合成测试可先行 |
| 索引/LOB/分区、计划与规模阈值 | DBA、性能与业务负责人 | T088 阈值和 T089 Oracle 压测/计划报告 | T091 上线准备通过；不阻塞文档、Mock 或未优化的离线逻辑 |
| 窗口、旧执行停止、应急与回退增量 | 业务、DBA、现场运维负责人 | T076/T077 清单及实际切换的另行授权 | 全通信域正式切换与 T092 实际退役，不能由文档代替授权 |

门禁只阻塞相应事实依赖，不把数据库未验证扩大成所有代码都必须停工。

## 3. Oracle 语义复核清单

### 3.1 NULL、空字符串和字符长度

1. 对所有迁移源列抽样记录 `NULL`、空字符串、全空白和默认值的原始差异；目标
   规则不能把 `''` 的处理假定成跨数据库一致。
2. `VARCHAR2` 候选长度必须明确 `CHAR` 或 `BYTE` 语义，尤其是人员姓名、设备
   描述、告警正文、协议引用和国际化文本。
3. 业务中 `NULL` 表示“未知/未提供”，空字符串是否表示“清空”必须由字段级
   映射决定；不能用全局 trim 或全局 `NVL` 静默改变授权/凭据含义。

### 3.2 NUMBER、时间和 ID

1. `Long -> NUMBER(19,0)`、`Integer -> NUMBER(10,0)` 是候选，必须以来源 ID
   最大/最小值、API 字符串序列化和目标约束复核；不得按 Java 类型直接建表。
2. `LkDeviceTask.startTime/endTime` 是 Java `Long`，单位、0 值、时区和边界未
   证明；转换为 `TIMESTAMP(6)` 前必须用抽样数据和业务日志确认。
3. `LocalDateTime -> TIMESTAMP(6)` 仅为候选；Oracle session/database time
   zone、应用序列化时区和历史 MySQL 时区须统一写入迁移报告。
4. `id`、业务版本、outbox/命令 claim token 不能只靠应用时间排序；需要稳定
   主键/索引和并发更新条件。

### 3.3 分页、排序和查询改写

- 旧 Mapper 的 `LIMIT 1`、MySQL `str_to_date`、`concat`、`COALESCE`、隐式
  数字/字符串转换均需 Oracle 目标版本下重写；分页必须有稳定唯一排序键，不能
  只按 `create_time`。
- 以 `person_id/device_id/credential_type` 查询时必须包含园区/业务版本和有效期
  边界，防止跨园区串读或旧授权覆盖新授权。
- 前置 `%` 模糊查询不能承诺普通 B-tree 性能；索引变更需当前数据规模和执行
  计划证据，不能从 Mapper 或历史估算推断。

### 3.4 LOB、敏感数据和日志

- `DL_OUTBOX.payload`、审计/归档大文本、受控 `logo` 只能在用途、大小、LOB
  storage 和保留期得到 DBA/安全批准后定为 CLOB/BLOB 或外部引用。
- 密码、指纹模板、恢复密钥和完整协议报文不落普通列/普通日志；在线表保存
  `material_ref`、摘要、版本和处置状态。原始材料必须由批准的密钥管理/加密
  归档负责，访问需审计。
- `payload_digest` 用于重复/完整性校验，不代表可恢复敏感值；摘要算法和密钥
  版本仍需安全确认。

## 4. 事务、并发与运行语义门禁

### 4.1 Outbox 与住宿本地事务

- `smart-platform` 的入住/调宿/退宿事务只写住宿事实和生命周期 `DL_OUTBOX`；
  二者在 platform 自己的 Oracle 本地事务提交，必须明确不在该事务跨模块写入
  `DL_ACCESS_GRANT`、`DL_CREDENTIAL` 或设备命令。Outbox 事件持久化
  `event_id`、字符串 `aggregate_id`、`aggregate_version` 和 `membership_id`。
- publisher 提交后至少一次投递到 smart-lock Inbox；smart-lock 在独立消费事务中
  原子写入 `DL_INBOX`、消费版本游标、`DL_ACCESS_GRANT`、
  `DL_GRANT_CREDENTIAL` 及需要的命令事实。重复 `event_id` 或旧
  `aggregate_version` 返回既有处理结果，不跨服务改写平台事务。
- publisher claim 需要明确 `SELECT ... FOR UPDATE`/版本号/claim token、超时
  回收、并发实例和索引方案；本轮不指定具体 SQL，因为目标 Oracle 版本和权限
  未验证。
- Bridge 不创建授权/命令，不凭缓存重建业务决策；发送失败、回执丢失、迟到或
  冲突均通过 `DL_COMMAND_ATTEMPT`、`DL_RECEIPT`、`DL_RECONCILIATION` 处理。

### 4.2 至少一次与对账

- 同一业务版本/幂等键可重复投递，但必须不产生重复有效授权；设备物理动作不
  承诺 exact-once。
- `DL_ACCESS_GRANT.authorization_status` 只使用
  `PENDING_PROVISION/ACTIVE/PENDING_REVOKE/REVOKED/RECONCILIATION_REQUIRED`；
  `ACTIVE` 同时要求目标设备凭据确认和当前 membership 资格有效，不能把
  `EXPIRED` 作为授权状态。多个 grant 可通过 `DL_GRANT_CREDENTIAL` 共享凭据，
  单个撤权不应删除仍被其它有效 grant 使用的凭据。
- `DL_COMMAND.command_status` 只使用：
  `QUEUED/DISPATCHED/WAITING_ACK/SUCCEEDED/RETRY_PENDING/FAILED/EXPIRED/
  CANCELLED/RECONCILIATION_REQUIRED`。
- `DL_COMMAND`、`DL_COMMAND_ATTEMPT`、`DL_RECEIPT` 必须保留 API `commandId`（可用
  字符串映射）、`attemptNo`、`eventId` 去重键、`protocolProfileId`、
  `membershipId`、执行租约/桥接会话和 transport/device 分层结果；不把 API ID、
  厂商 task ID 或数字主键混为一列。`attempt_no` 位于 attempt，不与授权状态
  `ACTIVE/REVOKED` 共用。
- 远程开门、失效授权和过期命令不得被通用恢复扫描无条件重放；需人工/策略确认。

### 4.3 默认关闭真实发送

- 开发、影子和演练环境默认真实网关发送关闭；仍可写命令、outbox、回执模拟和
  对账数据，但必须带环境/发送门禁标识。
- 单活桥是运行部署门禁，不是“只有一条 gateway_id”数据模型；多网关关系必须
  保留，发送尝试记录实际选择的网关。

## 5. 分阶段开工 Gate

Gate 分为可并行的离线验证线和依赖数据库证据的迁移/集成线。纯离线任务不因
目标 Oracle metadata 尚未开放而停工；但任何真实 DB DDL/DML、来源数据读取、
生产连接或真实网关发送仍必须等待对应 Gate。

### Gate 0：设计与权限（仅作为 DB 集成/迁移前置，不阻塞纯离线代码）

- [ ] 规格、候选模型、映射和任务边界通过评审；明确不执行 DDL/真实迁移。
- [ ] 为 DB 集成/迁移确认目标 schema/Oracle 版本/数据权限的最小只读清单；
  凭据由受控渠道提供，不写入文档或仓库。该项未完成时，Gate 1A、脱敏夹具、
  API/状态机和协议离线解析仍可继续，但不得连接真实 DB。
- [ ] 确认平台人员、园区、房间、住宿引用和事件版本 API/字段契约。

### Gate 1A：纯离线契约与状态机（可与 Gate 0 并行）

- [ ] 用脱敏固定夹具/测试替身验证 `eventId`、字符串 `aggregateId`、
  `aggregateVersion`、`membershipId`、Inbox 去重/游标和五种授权状态；不连接
  真实 Oracle、不写生产数据。
- [ ] 验证命令/attempt/receipt 的 `commandId + attemptNo`、profile、租约/会话、
  transport/device 结果分层、至少一次和对账；默认真实发送关闭。
- [ ] 可先完成 API/契约静态检查、序列化/分页规则和模拟 Bridge 回执；数据库
  metadata 仅在需要真实 SQL/索引计划时成为后置条件。

### Gate 1B：只读 metadata baseline（DB 线前置）

- [ ] 从旧 MySQL/source DB 导出并脱敏保存 `LK_%` 全对象/列/约束/索引、来源
  schema/version、删除状态和冻结水位；解释源码 22 表与真实对象差异。
- [ ] 从目标 Oracle 导出并脱敏保存现有平台字段/拟用对象的列/约束/索引/分区/
  LOB/权限报告；不能把候选表当成已建对象。
- [ ] 证明 NULL/空字符串、字符长度、时区、ID/epoch、LOB 和分页语义。
- [ ] 以只读执行计划确认关键查询和索引需求；未有计划不得承诺性能。

### Gate 2：脱敏迁移演练（依赖 Gate 1B；可复用 Gate 1A 夹具）

- [ ] 迁移批次、source key/hash、异常、归档和重跑结果可核验。
- [ ] 人员/园区/房间冲突不自动择一；旧账号不进入在线认证。
- [ ] 凭据仅按批准策略进行引用化、轮换、重采或设备侧验证；“DB 不迁 = 锁
  内失效”不得作为结论。
- [ ] 旧未完成任务分类但不重放；回执丢失/迟到/槽位复用进入对账。

### Gate 3：离线命令与协议验证（协议夹具可依赖 Gate 1A，不依赖 Gate 1B；DB 集成
需 Gate 1B）

- [ ] 模拟命令状态机、attemptNo、幂等键、claim 并发和 outbox 回滚测试通过。
- [ ] 脱敏 B8/ACK 样本验证长度、分包序号、终态和重复/迟到处理；源码证据不能
  替代真机/固件测试。
- [ ] Bridge 单活、发送默认关闭、重启恢复和对账修复路径可证明。

### Gate 4：隔离真机和通信域演练

- [ ] 测试园区/网关/锁、人员、房间和凭据均为隔离资源；逐设备回读确认。
- [ ] 入住、调宿、退宿、换锁、撤权、迟到新增成功和旧在途命令均有证据。
- [ ] 全通信域停止旧执行、改址回读、单执行方和回退增量差异通过；未完成不
  放开新生产发送。

### Gate 5：生产切换授权

- [ ] 生产 DDL/DML、迁移、部署、重启、网关改址和密钥轮换分别取得当轮授权。
- [ ] 最终数据核对、审计、应急通行、回滚和恢复责任人已签字；本轮设计文件
  本身不代表已获授权或已上线。

## 6. 容量和验收边界

- 本轮没有真实 DB 连接、当前行数、QPS、增长率或物理容量实测；文档不写“当前
  规模”。任何历史估算必须在报告中附来源、日期、抽样口径并标为历史估算。
- MySQL `information_schema.TABLES.TABLE_ROWS` 只能作为旧 InnoDB 的估计值，
  不能作为迁移验收精确计数。验收需使用受控只读查询、source key/hash、目标
  行数和业务关系抽样。
- Oracle 类型、索引、分区、LOB 和执行计划只有在目标版本/实例 metadata 和
  只读计划证据齐全后才能从“候选”升级为“确认”。
