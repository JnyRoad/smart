# manual

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
