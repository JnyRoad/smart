# 保密区权限自动删除报表迁移

本目录提供本版本 Oracle SQLPlus/SQLcl 的结构升级和应用回滚前检查脚本。脚本只处理 `SMT_SECURITY_AUTH_DELETE.DRY_RUN`、审计主表 `SMT_SECURITY_AUTH_DELETE_LOG` 和任务关联表 `SMT_SECURITY_AUTH_DELETE_TASK` 的结构；不回填历史任务或审计记录，不更新配置、权限、任务和业务数据，不创建序列、迁移账本或候选性能索引。

## 执行前提

由目标环境 DBA 在已暂停 `task.job.supplier-auto-auth-delete` 的目标 schema 会话中执行。每个脚本都必须显式传入目标 schema，并要求 `SESSION_USER` 与 `CURRENT_SCHEMA` 同时等于该值；`SYS`、`SYSTEM` 及其他 schema 会被拒绝。示例中的 `APP_SCHEMA` 仅是占位符：

```sql
@precheck.sql APP_SCHEMA
@upgrade.sql APP_SCHEMA
@verify.sql APP_SCHEMA
```

`APP_SCHEMA` 必须是未加引号的 Oracle 简单 schema 名称。不要把密码、连接串或环境凭据写入脚本或发布记录。

## 推荐升级顺序

1. 备份并确认所有平台实例和自动删权调度均已停用，避免新旧版本混跑。
2. 在目标 schema 执行 `@precheck.sql APP_SCHEMA`。基础表缺失、既有列类型不兼容、同名约束被其他表占用或约束语义漂移会直接失败；缺失的新表、`DRY_RUN` 列和本版本约束会被列为待补齐项。已有 `DRY_RUN` 若没有默认值可兼容（应用按空值作为正式模式处理），若声明默认值则必须为 `0`。
3. 复核前置检查输出后执行 `@upgrade.sql APP_SCHEMA`。已存在且兼容的对象会跳过；缺失对象以增量 DDL 补齐。已有数据的兼容表若缺少 `NOT NULL` 列会停止，脚本不会猜测值或回填。
4. 执行 `@verify.sql APP_SCHEMA`，确认所有列、类型、可空性和必要约束已就绪，再部署对应平台 Jar 和管理端文件。
5. 按发布说明完成菜单/角色授权、测试园区验收和调度恢复。真实 Oracle 执行计划、数据规模、设备联调和 3 秒目标仍需现场验收，脚本本身不构成这些结论。

完整的应用、数据库、菜单升级顺序见 [`smart-module/docs/releases/security-auth-delete-report.md`](../../../docs/releases/security-auth-delete-report.md)。

## 应用回滚门禁

应用回滚前执行：

```sql
@rollback_check.sql APP_SCHEMA
```

该脚本只读校验最终结构并输出两张新表当前记录数，不删除对象或数据。若发现任何 `DRY_RUN=1` 配置，它会明确失败并阻止直接恢复旧版自动调度，因为旧版本忽略演练标识，可能执行真实删权。即使检查通过，也必须保持调度停用并按发布流程切换上一版本；新增表、列、约束和审计数据始终保留。

## 重跑、失败与 Oracle 事务语义

脚本使用 `WHENEVER SQLERROR EXIT FAILURE ROLLBACK` 和 `WHENEVER OSERROR EXIT FAILURE ROLLBACK`。Oracle DDL 会隐式提交，因此失败后 `ROLLBACK` 不能撤销已经成功创建的表、列或约束；先核对目标结构，再以同一版本脚本幂等重跑或由 DBA 处理明确的结构漂移。脚本不会吞掉 `ORA-00955`、删除重建对象或执行 DROP。

本目录未连接或执行任何真实 Oracle 数据库；提交前只能做源级检查，目标环境结果必须由发布流程留存。
