# 打印 schema 版本发布

本文件对应 `specs/009-print-template-designer` 的 T005/T044 数据库交付部分。固定版本为 `009-print-v1`；发布器已通过隔离 H2 Oracle-mode 测试，**真实 Oracle 验收及任何环境发布均未执行（UNVERIFIED）**。这不关闭 T005 的独立 Oracle/schema、容量与清理验收，也不关闭 T044 的完整组件/驱动许可与分发验收。

## 交付入口与版本边界

入口为 `com.tce.smart.platform.service.print.schema.PrintSchemaReleaseCli`。它是独立 Java main，不是 Spring Bean，不扫描配置、不启动服务、不随应用启动运行。普通应用启动和所有既有打印接口不触发 DDL。

`PrintSchemaManifest` 是随应用制品分发的版本声明，按固定声明生成 Oracle DDL，并对**版本号、账本结构、全部建表及索引语句**的规范化 UTF-8 内容计算 SHA-256。资源不依赖人工 SQL 文件或 `scripts/` 目录；CLI 不接受 SQL、路径、表名、DROP、ALTER、修复或续跑参数。

每个已发布版本及其校验和必须保留。未来变更另建版本并扩展明确的升级路径，不能修改已投放版本来绕过校验。当前执行器只支持 `009-print-v1` 首次安装，以及同版本、同 release-id 的完整校验/幂等重跑；不把已有未知打印表自动登记为本版本，不自动接管或升级别的版本。

发布记录 `SMT_PRINT_SCHEMA_RELEASE` 包括 VERSION、CHECKSUM、RELEASE_ID、STATUS、STARTED_AT、FINISHED_AT、APPLIED_BY、COMPLETED_STEPS、FAILED_STEP、ERROR_CODE。正式发布系统的工单/版本记录使用唯一 `release-id` 关联，同时保存应用提交、制品校验和、目标实例/schema、manifest 校验和、操作者、执行时间与验收证据。数据库账本不替代外部授权记录。

## 固定对象范围

只新增下表 14 张业务表及 1 张发布账本。列名来自当前 `PrintTemplateMapper.xml`、`PrintBindingMapper.xml`、`PrintObjectMapper.xml`、`PrintPreviewMapper.xml`、`PrintJobMapper.xml`，并对照现有隔离测试 DDL；不会创建或修改 `SMT_PARK`、员工、人事、访客、门禁、供应商、照片等既有表，不推测这些外部表的结构，也不写入菜单或权限。

| 表 | 主要契约与索引 |
| --- | --- |
| SMT_PRINT_TEMPLATE | UUID 主键；园区+模板键唯一；园区创建时间列表索引 |
| SMT_PRINT_TEMPLATE_VER | UUID 主键；模板+版本号唯一；布局、字段、资源清单、页面、校验报告为 CLOB |
| SMT_PRINT_TEMPLATE_PAIR | UUID 主键；固定两面版本引用；园区创建时间列表索引 |
| SMT_PRINT_OPERATION | UUID 主键；操作主体+幂等键唯一；响应 CLOB |
| SMT_PRINT_AUDIT | UUID 主键；对象+时间+审计 ID 索引；详情 CLOB |
| SMT_PRINT_PREVIEW | UUID 主键；预览详情 CLOB |
| SMT_PRINT_OBJECT | UUID 主键；私有内容 BLOB，独立内容 hash 与元数据 |
| SMT_PRINT_BIND_RULE | UUID 主键；园区/打印物/人员类型/分类/状态候选索引；职级集合 CLOB |
| SMT_PRINT_PRINTER_PROFILE | UUID 主键；园区+打印机列表索引；档案 CLOB |
| SMT_PRINT_JOB | UUID 主键；领取 ID 唯一且允许未领取时为空；园区/创建人/时间和状态队列索引；冻结快照、制品索引、状态 CLOB |
| SMT_PRINT_ATTEMPT | UUID 主键；commandId 唯一；jobId+attemptNo 唯一 |
| SMT_PRINT_EVENT | UUID 主键；jobId+时间+eventId 索引；响应和详情 CLOB |
| SMT_PRINT_JOB_ARTIFACT | UUID 主键；冻结 PDF BLOB 与 hash |
| SMT_PRINT_JOB_PREVIEW | UUID 主键；确认前预览详情 CLOB |
| SMT_PRINT_SCHEMA_RELEASE | 版本主键；release-id 唯一；发布状态和完成步数 |

设计沿用当前 Mapper/业务事务管理关联，不新增跨既有表外键或业务 CHECK，也不声称数据库约束替代园区授权。业务表保留当前夹具的可空性和既有唯一性，仅主键强制非空；发布账本的关键审计字段另有 NOT NULL。

所有文本明确使用 `VARCHAR2(n CHAR)`，整型使用 `NUMBER(10,0)` 或 `NUMBER(19,0)`，时间使用 `TIMESTAMP(6)`，JSON 为 CLOB，二进制为 BLOB。平台生成或服务端校验的 UUID 为 36 字符，业务 hash 为 `sha256:` + 64 hex，即 71 字符；发布 checksum 本身为 64 hex。clientInstanceId 同样按已验证 UUID 保留 36 字符。IDEMPOTENCY_KEY 为 128 字符；PRINCIPAL_ID/ACTOR_ID 为 135 字符，容纳 `device:` 加 128 字符设备身份。其他操作人列保留 128 字符。对象园区列由测试夹具 36 扩展到模板/预览一致的 64；任务族的园区和非 UUID 业务字段保持对应夹具 128 容量。以上是**新表声明**，不会调整已有表的容量。

## 运行方式

使用本次构建的 `smart-platform-biz.jar`。在发布系统独立的、已授权的暂存目录解包制品后，classpath 指向 `BOOT-INF/classes` 与 `BOOT-INF/lib/*`，避免启动 Spring Boot 主应用。例如工作目录为已解包的本版本制品：

```bash
java -cp 'BOOT-INF/classes:BOOT-INF/lib/*' \
  com.tce.smart.platform.service.print.schema.PrintSchemaReleaseCli --help
```

CLI 只依赖 JDK JDBC；Oracle 驱动来自原应用制品，不引入第二个驱动版本。暂存目录中不得夹带 H2 或其他非发布依赖；CLI 本身拒绝 H2 URL。上述包内 classpath 必须随正式制品在独立 Oracle 验收时再次核实；本轮仅已验证编译目录中的 `--help` 启动。

由发布系统的秘密管理注入以下环境变量，不放在命令参数、`.env`、仓库或日志中：

- `PRINT_SCHEMA_JDBC_URL`：只接受以 `jdbc:oracle:thin:@` 开头的 URL，不接受 URL 内嵌用户名密码。
- `PRINT_SCHEMA_JDBC_USER`：必须与 `--expected-schema` 精确一致。
- `PRINT_SCHEMA_JDBC_PASSWORD`：专用发布账户密码。

下面的 `APP_SCHEMA` 与 `release-20260905-001` 是占位示例，**不构成对任何真实环境的授权**。执行前替换为已经核实并获授权的实际 schema 和发布工单 ID；不能沿用示例猜测实例。

先只读核对并导出计划：

```bash
java -cp 'BOOT-INF/classes:BOOT-INF/lib/*' \
  com.tce.smart.platform.service.print.schema.PrintSchemaReleaseCli \
  --plan --expected-schema APP_SCHEMA
```

计划输出版本、checksum、实际 schema 状态；空库时输出固定 DDL。必须核对 JDBC 实例、服务名、schema、应用制品及 manifest 校验和。连接后再次查询 Oracle CURRENT_SCHEMA 与 SESSION_USER，要求两者都等于 expected-schema；禁止通过 ALTER SESSION/CURRENT_SCHEMA 跨 schema 发布。该执行器要求独占自动提交连接，不提交调用方未完成事务。

只有发布批准覆盖精确目标与新增对象后才执行：

```bash
java -cp 'BOOT-INF/classes:BOOT-INF/lib/*' \
  com.tce.smart.platform.service.print.schema.PrintSchemaReleaseCli \
  --apply --expected-schema APP_SCHEMA \
  --release-id release-20260905-001 \
  --confirm-version 009-print-v1 \
  --confirm-checksum 'APPROVED_CHECKSUM_FROM_REVIEWED_PLAN'
```

未指定模式默认 `--validate`；缺少 schema、任何写入确认参数、版本/checksum 不一致、重复参数、未知参数都会拒绝。`--plan`/`--validate` 只做元数据和发布账本 SELECT，不建表、不写记录、不执行计划、不要求刷新统计信息；只读模式不能附带写入授权参数。

完成后执行默认只读校验：

```bash
java -cp 'BOOT-INF/classes:BOOT-INF/lib/*' \
  com.tce.smart.platform.service.print.schema.PrintSchemaReleaseCli \
  --validate --expected-schema APP_SCHEMA
```

退出码：0 表示完整已发布且核对成功，或只读 plan/help 成功；2 表示 validate 检查到完全未安装；1 表示参数、连接、结构或发布状态被拒绝。SQL 异常只输出固定错误状态和数字错误码，不回显 JDBC URL、账号密码、异常堆栈或任意数据库错误文本。故障详情在已授权 DBA 只读核对和发布记录中收集，不通过提高日志级别回显凭据。

## 前置检查、失败与恢复

首次安装先确认所有目标表名、索引名、约束名不存在；Oracle 还检查可见公共同名 synonym。未知已有表、视图或对象名冲突一律停止，不采用 `IF NOT EXISTS` 掩盖差异。已安装版本必须同时匹配账本、checksum、全部表列/容量/可空性/默认值、主键、唯一性和要求的普通索引。Oracle 还核对 CHAR 语义、timestamp 精度、具名约束的启用/验证状态、索引有效性以及意外触发器/外键。任一核对失败都不自动修复。

首次创建账本也是并发发布的争用门禁；第二个发布进程不能自动接管正在创建或 STARTED 状态的版本。发布记录插入 STARTED 后，逐条执行固定新增 DDL 并记录完成步数；全部结构复核成功后才记为 APPLIED。失败尽力写 FAILED、失败步骤与数字错误码。**Oracle DDL 隐式提交，不能承诺事务 rollback 撤销已成功的 DDL。**

以下情况都禁止直接自动重试、删除账本、重置状态、删表、清空任务或把部分表补成“成功”：

1. 账本建成但尚无行：可能在引导阶段崩溃，视作未知/部分发布。
2. STARTED：可能仍有另一个执行器运行，或进程/连接已中断；先核实会话与发布记录。
3. FAILED：已完成表和索引仍可能存在，完成步数仅是已确认检查点。
4. DDL 已提交但检查点未更新：实际对象可能比 COMPLETED_STEPS 多；按元数据逐项核对，不按计数推断可重跑。
5. APPLIED 但结构缺失、checksum 不符或数据被改写：拒绝发布；保留现场并调查漂移。

处置顺序为：保持打印功能/执行开关关闭，保存发布记录与对象清单，授权 DBA 只读取证，确认业务证据是否已产生，再为**精确剩余对象/修复目标**建立新版本恢复方案及新授权。本版不提供自动 resume/repair；可重复执行的是经过校验的完整版本，不是对部分失败隐式续跑。即使空环境也不把删除重建当作默认恢复。

## 应用回退与证据保留

本版本只新增打印专用对象。回退恢复上一版应用与旧访客入口，停止新链路接单和设备领取，并按任务状态处理在途工作；结果不明的任务必须人工核对实卡，不能自动重放。旧链路不自动承接新厂牌功能。

回退**保留全部打印表、发布账本、模板/版本/组合、对象、任务、attempt、commandId、event、审计和冻结制品**。不执行 DROP、TRUNCATE、删除发布行或清空审计，不因应用回退抹去打印证据。未来数据保留/清理策略需另行制定、验收和授权；本执行器不包含清理操作。

## 独立 Oracle 验收清单

- [ ] 实例、schema、登录用户、受控发布凭据、版本/checksum 及工单目标匹配，打印开关关闭。
- [ ] 目标 Oracle 版本、实际随包驱动、字符集、CHAR 长度、NUMBER/TIMESTAMP/CLOB/BLOB 元数据与往返结果验证。
- [ ] 发布账户 CREATE TABLE/CREATE INDEX 权限、默认表空间、配额、LOB 容量、备份与留存策略确认；工具不自动授予权限或扩容。
- [ ] 完整安装与同 release-id 幂等重跑；业务 Mapper/事务、中文长文本、CLOB/BLOB、唯一键和多条空 claimId 实测。
- [ ] 模拟 DDL 中途失败、连接断开、发布进程中止；确认部分结果保留且自动重试拒绝，独立恢复方案经审批。
- [ ] 撤销或破坏索引/约束的隔离验收，验证只读校验拒绝，且没有隐式修复。
- [ ] 应用前进/回退与在途打印证据保留演练；菜单/权限、真实照片、人事字段、设备驱动与实卡验收分别完成。

本地自动化为 `PrintSchemaReleaseTest`、`PrintSchemaCliTest`、`PrintSchemaMapperTest`，从 `smart-module/` reactor 运行：

```bash
/Users/lvtu/.local/opt/apache-maven/bin/mvn \
  -pl smart-platform/smart-platform-biz -am '-Dtest=PrintSchema*Test' \
  -Dsurefire.failIfNoSpecifiedTests=false -DargLine=-Djava.awt.headless=true test
```

它们只创建独立内存 H2 Oracle-mode 数据库，覆盖发布状态、无副作用预检、重复安装、结构漂移、DDL 部分失败、幂等唯一性、容量/大字段、CLI 参数和当前 14 表 Mapper 插入契约；不连接网络数据库，不代替以上 Oracle 或设备验收。
