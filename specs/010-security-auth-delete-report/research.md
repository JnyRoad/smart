# 方案评估与依据

- 决策：独立日志表 + 多任务关联。原因：设备任务表无法表达白名单、未到期和演练；一次权限可能涉及多个任务。替代方案“直接用设备任务做报表”无法满足来源和判定轨迹。
- 决策：查询聚合当前任务状态，缺失明确未知。原因：可直接适配当前链路并反映重试结果；替代方案“新建统一任务状态同步系统”超出本次报表范围。
- 决策：逐条真实事务 + 回滚后独立失败审计。依据：现有 private REQUIRES_NEW 在同类调用内无效，导致整批异常无法独立留痕。
- 决策：补最小演练支持，保留正式删权策略。原因：蓝图 5.6 要求演练结果；5.8 是独立全链路改造，不能把报表成功写成设备确认。
- 历史依据：蓝图 5.6（独立表、筛选导出、园区范围、任务下钻）及 5.4（演练依赖）。
- 当前证据：SmtSecurityAuthDeleteServiceImpl.deleteAuthTask -> deleteStaffAuthWithTransaction -> SmtStaffService.savePersonCardTask；图 Tier 2 generation 2026-09-05T04:34:23Z，关键自动判定源码无记录缺口，getter 启发式连边以源码核验。
- Oracle 生产表结构、统计和计划未核验；交付 SQL 结构说明仅作发布准备，不代表执行或性能已验收。

- 任务链补充核验：SmtStaffServiceImpl.savePersonCardTask逐设备调用saveTask；后者分流普通Integer任务ID与ISC Long任务ID。关联必须带NORMAL/ISC来源。DeviceTaskStatusEnum包含5过期与6离线，分别作为失败/等待，不能误判未知。
