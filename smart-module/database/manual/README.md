# 手工数据库脚本说明

当前平台没有 Flyway、Liquibase 等数据库自动迁移机制，本目录脚本需要人工按需执行。

## 执行方式

- Oracle 低版本不支持 `CREATE TABLE IF NOT EXISTS` 这类普通 SQL 写法；为了兼容现有生产库，脚本使用 PL/SQL 查询 `USER_TABLES`、`USER_INDEXES` 后再动态执行 DDL。
- 每个 `.sql` 文件都是一个完整的 PL/SQL 匿名块，请使用“执行脚本 / Run Script”整段执行单个文件，不要按分号逐句执行。
- 脚本末尾没有 SQL*Plus 的 `/` 分隔符，避免部分 JDBC 工具把 `/` 当成一条 SQL 后报 `ORA-00900`。
- 如果数据库工具只支持普通 SQL、不支持 PL/SQL 匿名块，就无法在一个 SQL 文件内可靠实现“存在才建表 / 存在才建索引”，需要改用 SQL Developer、DBeaver、DataGrip 这类支持 PL/SQL 匿名块整段执行的模式，或由运维工具先查对象再执行 DDL。
- 不要使用 SQL*Plus / SQLcl 的脚本执行方式直接跑当前文件；这类工具通常需要 `/` 结束匿名块，而当前脚本为了兼容 JDBC 工具已去掉 `/`。

## 正向脚本

| 脚本 | 是否执行 | 说明 |
| --- | --- | --- |
| `20260602_add_smt_isc_park_config.sql` | 需要 | 创建海康 ISC 园区绑定配置表 `SMT_ISC_PARK_CONFIG`。脚本可重复执行，表已存在时只补 `ACTIVE_KEY` 字段、中文备注和索引。 |
| `20260602_add_smt_isc_staff_card.sql` | 需要 | 创建人员海康 ISC 实体卡主表 `SMT_ISC_STAFF_CARD`。脚本可重复执行，表已存在时会补同步状态字段、卡号规则约束、中文备注和索引。 |
| `20260602_add_smt_isc_card_task.sql` | 需要 | 创建海康 ISC 卡片同步任务表 `SMT_ISC_CARD_TASK`。脚本可重复执行，表已存在时只补中文备注和索引。 |
| `20260610_cleanup_invalid_isc_staff_cards.sql` | 按需 | 软删除历史遗留的不符合海康 ISC 卡号规则的本地有效卡。若 `20260602_add_smt_isc_staff_card.sql` 提示存在非法有效卡，执行本脚本清理后再重跑主脚本。 |
| `20260701_add_smt_device_authority_relation_device_id_index.sql` | 需要 | 给 `SMT_DEVICE_AUTHORITY_RELATION.DEVICE_ID` 加索引，支撑"按设备反查权限组"功能的查询性能。脚本内置索引存在性判断，可重复执行。 |
| `2026-07-01-oauth-client-secret-prefix.sql` | 需要 | 给 `sys_oauth_client_details.client_secret` 存量明文行补 `{noop}` 编码前缀，配合 `SecurityConstants.CLIENT_FIELDS` 改为直接读取 `client_secret`（不再由 SQL 强制拼前缀），为后续新增 `{bcrypt}` 编码的 client 做铺垫。已带前缀的行不受影响，可重复执行。 |
| `2026-07-01-register-file-receiver-app.sql` | 需要 | 注册许昌 FileReceiver 开放应用（App ID: file-receiver-xc）至 `sys_oauth_client_details`。脚本内置应用存在性判断，可重复执行。部署后需立即通过管理页「重置 App Secret」生成正式凭证；占位符 `<许昌园区ID>` 需执行前从 `smt_park` 表确认填入。 |
| `2026-07-01-rename-client-menu-to-app-management.sql` | 需要 | 将管理后台菜单 `SYS_MENU.NAME` 由 "客户端管理" 改名为 "应用管理"（开放 API 鉴权规范要求）。脚本内置菜单存在性判断，可重复执行；未找到不报错。 |

建议执行顺序：

1. `20260602_add_smt_isc_park_config.sql`
2. `20260602_add_smt_isc_staff_card.sql`
3. 若第 2 步提示存在历史非法有效卡，执行 `20260610_cleanup_invalid_isc_staff_cards.sql`，然后重跑 `20260602_add_smt_isc_staff_card.sql`
4. `20260602_add_smt_isc_card_task.sql`

## 回滚脚本

| 脚本 | 是否执行 | 说明 |
| --- | --- | --- |
| `20260602_rollback_smt_isc_park_config.sql` | 仅回滚时执行 | 删除 `SMT_ISC_PARK_CONFIG` 及相关索引，会丢失数据。 |
| `20260602_rollback_smt_isc_staff_card.sql` | 仅回滚时执行 | 删除 `SMT_ISC_STAFF_CARD` 及相关索引，会丢失数据。 |
| `20260602_rollback_smt_isc_card_task.sql` | 仅回滚时执行 | 删除 `SMT_ISC_CARD_TASK` 及相关索引，会丢失数据。 |
| `2026-07-01-oauth-client-secret-prefix-rollback.sql` | 仅回滚时执行 | 剥离 `2026-07-01-oauth-client-secret-prefix.sql` 写入的 `{noop}` 前缀；迁移后新增的 `{bcrypt}` 编码行不可逆，本脚本不处理。 |

## 注意事项

- 不要执行已废弃的人员表 `isc_card_no` 字段脚本；卡片数据使用独立表 `SMT_ISC_STAFF_CARD`。
- 正向脚本已内置表、字段和索引存在性判断，不需要单独的修复脚本。
- 回滚脚本也已内置对象存在性判断，但会删除表和数据，只能在确认回滚时执行。
- 如果执行时提示重复有效园区配置或重复有效卡片，需要先清理重复业务数据，再重新执行对应正向脚本。
