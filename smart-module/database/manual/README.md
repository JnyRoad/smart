# manual

## 2026-09-05 保密区权限自动删除记录报表

- [版本化迁移目录](20260905_security_auth_delete_report/README.md)：包含升级前检查、正向迁移、升级后验证和只读回退检查；必须传入已核对的目标 schema。
- 新增审计主表、全部任务关联表和 `DRY_RUN` 配置字段；不回填历史来源、不修改业务权限或任务、不重置演练配置。
- 已存在的本版本对象通过兼容性校验后跳过；结构漂移明确失败。Oracle DDL 已完成部分不能由事务回滚撤销。
- 回退保留审计数据与新增结构；存在演练配置时阻止直接恢复旧版自动调度。
- 平台 Jar、管理端、数据库、菜单的发布顺序见 [升级说明](../../docs/releases/security-auth-delete-report.md)。脚本尚未在目标 Oracle 执行。

## 2026-09-03 员工通关权限有效期

- `20260903_add_staff_device_auth_window.sql`：为 `SMT_STAFF_DEVICE_AUTH` 增加 `START_TIME`、`END_TIME`，将历史记录回填为创建日零点至 `2030-12-31 23:59:59`，并拒绝结束时间早于开始时间的异常数据；脚本由 DBA 在目标 Oracle schema 执行，仓库未执行任何真实数据库变更。
- `20260903_rollback_staff_device_auth_window.sql`：只做应用回滚前置检查。日期字段和已配置的业务数据刻意保留，避免破坏性 DDL 造成有效期丢失；旧应用不读取这两个字段。

## 2026-09-02 保密区供应商软删除

- `20260902_add_supplier_soft_delete.sql`：为 `SMT_SECURITYAREA_SUPPLIER` 和 `SMT_SUPPLIER_PERSON` 增加 `DEL_FLAG`，将历史空值回填为 `0`，并校验字段只含 `0` 或 `1`。字段默认 `0`、不可为空，并以检查约束限制为 `0/1`；脚本由 DBA 在目标 Oracle schema 执行，仓库未执行任何真实数据库变更。
- `20260902_rollback_supplier_soft_delete.sql`：只做应用回退前置检查。只有两张表均无 `DEL_FLAG=1` 的软删除数据时才允许回退应用；字段刻意保留，避免破坏性 DDL。已有软删除数据时，脚本会阻止回退，防止旧版本重新展示已删除记录。

## 2026-08-05 能耗日汇总投影

- `20260805_add_energy_projection.sql`：创建能耗事件账本、可恢复投影队列（租约与有限重试）、范围规则、单表计日事实、园区日明细、园区日汇总，以及单表计日和园区日锁锚点表；脚本会对本发行早期已创建但缺列的表幂等补列。
- `20260805_rollback_energy_projection.sql`：按依赖反序删除上述八张业务表；不存在的表会被忽略。

回滚按发行 marker 删除整张投影队列表，因此无需单独回滚 `LEASE_TOKEN` 列；对于非本发行创建的表，回滚仍不会改动其结构。

园区日完整性按当前主表的有效表计数校验；若表计历史园区归属不可得，历史日期首次回填会保守显示 `PARTIAL`，不会错误显示 `READY`。

### 总分表排除配置

总表、分表去重规则存放在 `SMT_ENERGY_METER_SCOPE_RULE`，而不是静态 Nacos/YAML 列表。这样可以按园区、表计类型和生效日期保存审计记录，也能在规则调整后重算历史业务日。

- 未配置规则的有效表计默认纳入。
- 同一计量组的总表配置为 `INCLUDE_FLAG=1`、`PARENT_METER_ID=NULL`；分表配置相同的 `METER_GROUP_ID` 并以 `PARENT_METER_ID` 指向总表，系统会保守排除分表，避免重复累计。
- 需要直接排除的表计配置为 `INCLUDE_FLAG=0`，并填写 `REASON`。生效区间使用 `EFFECTIVE_START_DATE`、`EFFECTIVE_END_DATE`；每次变更递增 `RULE_VERSION`，不得覆盖历史规则。
- 规则落库后，对受影响的业务日通过内部 `POST /inner/energy/projection/reconcile/{businessDate}` 回算；当月累计会在投影刷新后自动反映新规则。该内部接口只允许定时服务调用。

应用启用前请由业务方核对每个总分表组只有一个纳入根表；发现循环、多个根表或归属无法判定时，投影会标记为 `PARTIAL`，不会将不确定数据当作完整园区总量返回。

### API 授权

网关公开查询地址为 `GET /platform/sd/statistics/month/{parkId}`，服务内路径为 `GET /sd/statistics/month/{parkId}`。调用身份除需拥有目标园区外，还必须由权限中心授予 `platform_energy_usage_view`；本仓库不保存 UPMS 角色/菜单的生产数据，发布单需包含该权限授予操作。
