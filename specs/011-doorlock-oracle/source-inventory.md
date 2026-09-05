# 门锁重构源码与 `lk_*` 实体清单

## 1. 范围、来源和证据等级

本文只盘点可读的重构源码，不把实体类、Mapper XML 或本地配置当作真实
Oracle DDL。权威源码来源固定为仓库
[JnyRoad/smart-lock](https://github.com/JnyRoad/smart-lock.git)，提交
`e236793bbbd894b43fc06958595c2e9577d8da4a`；该提交已核实为 `origin` 的完整
commit。下文所有外部源码路径均为该提交根目录下的相对路径，不提交本机
checkout 路径、临时 worktree、JAR 内部绝对路径或环境文件。

机器可读清单见
[evidence/source-inventory.json](evidence/source-inventory.json)，包含 22 个
`@TableName("lk_*")` 实体和 17 个 Mapper XML 的相对路径、表/namespace、声明
行及 Git blob OID。用固定提交重建时，确定性查询为：

```sh
COMMIT=e236793bbbd894b43fc06958595c2e9577d8da4a
git grep -n -E '@TableName\("lk_[^\"]+"\)' "$COMMIT" -- \
  'smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/*.java' | LC_ALL=C sort
git ls-tree -r --name-only "$COMMIT" -- \
  smart-module/smart-lock/smart-lock-biz/src/main/resources/mapper | LC_ALL=C sort
```

两条查询的计数分别为 22、17；查询输出 SHA-256 分别为
`a05e1bbe682dca5684bb8c8fa756c8ab7de8d68c6aff916e6209b8738f4f9c97` 和
`54c2ec281a2d45701d5897f4fee3e6f2b84534185c31c6686a3507333293f240`。清单的
记录投影（`relativepath<TAB>table_or_namespace<TAB>declaration_line<TAB>blob_oid`，
先实体后 XML）SHA-256 为
`f88bfaedb53772634d5b500246dd4ea1d04d0eb52027e94253ff74012fbd2ca3`。

代码图谱仅作结构交叉核对：项目
`Users-lvtu-source-YUTO-smart-lock` 的 generation 为
`2026-09-05T06:29:29Z`，Tier 2/full，coverage metadata 完整；对本清单 39
条路径执行的 `check_index_coverage` 均返回 `metadata_match /
no_recorded_issue`。这是 best-effort 覆盖信号，不证明图谱或数据库完整；
Smart 主仓扫描不能得到外部仓库的 22/17 清单，完整性以固定 commit 的 Git
查询、计数和 OID 清单为准。

证据等级：

- **S（源码明确）**：`@TableName`、字段声明、`@TableId`、`@TableLogic`、
  XML `resultMap` 或 SQL 中直接出现的列/表。
- **I（ORM 推断）**：按 MyBatis-Plus 默认 camelCase 到 snake_case 的命名
  推断出来的列名；没有可见 DDL，不能称为物理列已确认。
- **U（未验证）**：目标 Oracle 版本、schema、实际约束/索引/分区/数据行数、
  运行中的旧库版本或现场设备语义。

## 2. 可读到的全部 `lk_*` 实体表

下表覆盖源码实体目录中全部 22 个 `@TableName("lk_...")` 声明。字段级旧表
映射见 [migration-mapping.md](migration-mapping.md)；此处重点记录来源、
类别和是否可作为在线主数据。

| 旧表 | 实体来源（相对路径:行） | 源码可见类别 | 本轮处置 | 在线主数据结论 |
|---|---|---|---|---|
| `lk_device` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkDevice.java:12-45` | 设备资产 | 映射 `DL_DEVICE`，房间与网关关系拆表 | 采用；不保留排它 owner |
| `lk_device_log` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkDeviceLog.java:11-21` | 设备操作/历史日志 | 映射 `DL_DEVICE_EVENT` 或归档 | 不作为命令真相 |
| `lk_device_model` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkDeviceModel.java:12-29` | 型号能力配置 | 映射 `DL_DEVICE_MODEL_CAPABILITY`，需能力证据 | 仅候选能力 |
| `lk_device_permissions` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkDevicePermissions.java:17-62` | 人员-设备授权 | 映射 `DL_ACCESS_GRANT` 与 `DL_GRANT_TARGET` | 采用；状态不等于物理确认，父授权归组需核实 |
| `lk_device_task` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkDeviceTask.java:14-53` | 旧设备任务 | 分类映射 `DL_COMMAND`/`DL_COMMAND_ATTEMPT` | 不整表重放 |
| `lk_event_record` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkEventRecord.java:16-48` | 网关/设备事件历史 | 映射 `DL_DEVICE_EVENT` | 仅历史证据 |
| `lk_gateway` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkGateway.java:11-32` | 网关资产 | 映射 `DL_GATEWAY` | 采用；与设备多对多 |
| `lk_key` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkKey.java:14-46` | 设备凭据 | 映射 `DL_CREDENTIAL` | 采用；敏感值不直接导入普通表 |
| `lk_message_config` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkMessageConfig.java:11-21` | 消息/通知设置 | 映射设置候选或保留归档 | 不作为授权状态 |
| `lk_message_record` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkMessageRecord.java:11-25` | 消息历史 | 映射 `DL_OPERATIONS_ALERT` 或归档 | 不作为命令真相 |
| `lk_modify_pwd_log` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkModifyPwdLog.java:13-30` | 密码变更历史 | 映射 `DL_AUDIT_EVENT`，敏感值脱敏 | 不复用密码列 |
| `lk_open_record` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkOpenRecord.java:14-47` | 开门历史 | 映射 `DL_DEVICE_EVENT` | 仅历史记录 |
| `lk_park` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkPark.java:16-45` | 旧园区副本 | 以 `target_park_id` 对接平台园区 | 不建立第二套在线园区主表 |
| `lk_person` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkPerson.java:17-64` | 旧人员/凭据混合副本 | 人员映射平台；凭据字段分流归档/迁移 | 不建立第二套在线人员主表 |
| `lk_system_config` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkSystemConfig.java:17-46` | 独立后台设置 | 非敏感设置候选映射；原图标受控归档 | 不沿用独立后台配置权威 |
| `lk_system_menu` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkSystemMenu.java:16-62` | 独立后台菜单 | 账号/菜单归档 | 不沿用 |
| `lk_system_role` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkSystemRole.java:16-53` | 独立后台角色 | 账号/角色归档 | 不沿用 |
| `lk_system_role_menu` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkSystemRoleMenu.java:7-13` | 角色-菜单关联 | 账号/菜单归档 | 不沿用 |
| `lk_system_user` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkSystemUser.java:16-48` | 独立后台账号 | 受控账号归档 | 不沿用登录体系 |
| `lk_system_user_park` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkSystemUserPark.java:7-13` | 账号-园区关联 | 账号/园区授权归档 | 不沿用园区权限 |
| `lk_system_user_role` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkSystemUserRole.java:7-13` | 账号-角色关联 | 账号/角色归档 | 不沿用 |
| `lk_warn_config` | `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkWarnConfig.java:11-32` | 告警设置 | 映射通知策略候选或归档 | 不作为授权状态 |

`Lk*Ext` 是查询投影/扩展对象，没有新的 `@TableName` 实体表；不能把其
投影字段误列为物理列。

## 3. 重要的 ORM/SQL 观察

1. `smart-module/smart-lock/smart-lock-biz/src/main/resources/application.yml:9-21`
   使用 MySQL 驱动和 `SMART_LOCK_DATASOURCE_*` 占位符；
   `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/config/MybatisPlusConfig2.java:10-15` 显式使用
   `PaginationInnerInterceptor(DbType.MYSQL)`。这只说明重构源码的旧运行配置
   倾向 MySQL，不能推导旧生产实例版本，也不能推导 Oracle DDL。
2. `smart-module/smart-lock/smart-lock-biz/src/main/resources/mapper/LkDeviceTaskMapper.xml:46-59` 使用 `COALESCE`、旧状态数字、
   `start_time/end_time` 和单一 `gateway_id` 查询；`:70-73` 把
   `command_id` 联到 `lk_key.id`。目标模型必须把命令、尝试、回执及网关关系
   拆开，不能把 `command_id` 当协议 taskId。
3. `smart-module/smart-lock/smart-lock-biz/src/main/resources/mapper/LkKeyMapper.xml:28-41` 以 `lk_key.id` 关联最近任务状态，并使用
   `LIMIT 1`；这是旧 MySQL 查询，不是 Oracle 分页/排序证据。
4. `smart-module/smart-lock/smart-lock-biz/src/main/resources/mapper/LkDevicePermissionsMapper.xml:22-44,46-156` 的
   `personDeviceMap` 含人员和设备联表投影；`person_num`、`device_name` 等
   并不都是 `lk_device_permissions` 的物理列。
5. `smart-module/smart-lock/smart-lock-biz/src/main/resources/mapper/LkSystemUserMapper.xml:7-19` 映射了实体中不存在的 `parkId` 属性
   (`park_id`)；这是源码/Mapper 不一致，不能未经旧库 metadata 核实就迁移。
6. `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/entity/LkDevice.java:30-45` 的 `isAvailable`、`reportStateTime` 和
   `smart-module/smart-lock/smart-lock-biz/src/main/resources/mapper/LkDeviceMapper.xml:5-29` 的 resultMap 并不完全一致；
   `LkPerson.status`、`LkKey.personId/deviceId/delFlag`、`LkOpenRecord.personId`
   也存在实体/投影不完整情形。迁移必须以旧库列元数据和抽样数据复核。

## 4. 协议和敏感数据证据

- `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/biz/service/impl/LkKeyServiceImpl.java:209-292`
  通过报文中的任务信息 ID 查询 `LkDeviceTask`，然后按回执结果更新任务/钥匙；
  这证明业务 task ID 与设备钥匙编号是不同语义。
- 同文件 `:294-423` 解析 B8 指纹回执，既有固定长度判断，也有可选分包序号，
  并在终态回执后写入 `keyId`。现有源码可作为协议候选证据，但现场帧语义、
  设备固件能力和 ACK 版本仍为 U；不能据此写死“全部指纹必须重采”。
- `smart-module/smart-lock-device-server/device-server-biz/src/main/java/com/tce/smart/lock/device/server/device/DeviceReceiveService.java:19-160`
  通过设备服务回调把自定义数据送回业务服务，并记录协议摘要；原始敏感报文
  不得进入普通 Oracle 表或日志。必要的恢复密钥/模板只能使用已批准的受控
  加密存储引用。

## 5. 不可从源码得出的内容（待 metadata 清单）

本轮未取得真实实例的 DDL、Oracle 版本、目标 schema、真实行数、主外键、
索引/分区、字符集、LOB 存储、表空间、触发器、序列、权限或正在运行的旧库
版本证据。源码和历史脚本不能替代当前实例元数据。旧 MySQL/source DB 与目标 Oracle 的核验职责必须分开：

- **旧 MySQL/source DB 只读证据**：从其 `information_schema` 核对全部 `LK_%`
  表、视图及相关触发器、事件和存储过程，另核对历史归档与应用临时对象，真实列/约束/索引、字符集、时区、
  删除状态、行数与分布；重点核对是否存在未被 22 个实体覆盖的表，未知对象不能
  自动丢弃。`information_schema.TABLES.TABLE_ROWS` 若被历史材料引用，只能标记
  为旧 InnoDB 估算证据，不得当作迁移验收精确计数。
- **目标 Oracle 只读证据**：核对现有平台字段与拟用对象的
  `ALL_TAB_COLUMNS`/`ALL_CONSTRAINTS`/`ALL_INDEXES`/`ALL_IND_COLUMNS`，以及
  `ALL_TAB_PARTITIONS`/`ALL_LOBS`/表空间/权限结果，包括实际精度、字符长度语义、
  NULL/默认值、逻辑删除列、复合键、LOB 存储和历史分区；不能反推旧 MySQL 真实列。
- 来源侧确认实际 ID 类型、范围和时间语义，目标侧确认对应 `NUMBER` 精度、
  `VARCHAR2` CHAR/BYTE 语义、时间列时区和 LOB 存储；双方核对迁移冻结水位，
  本轮不宣称任何当前容量或目标表已存在。

本轮未连接真实数据库、未执行 DDL/DML、未导出数据、未读取任何 `.env` 或
凭据；以上 U 项不会被其它文档标为已完成。
