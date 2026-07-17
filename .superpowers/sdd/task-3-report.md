# Task 3：ISC 接管、worker、调度围栏与状态聚合报告

## 状态

已完成实现、回归测试和自评审；可提交。

## RED / GREEN 证据

### RED

- 核心接管测试先因缺少 `saveSecurityAuthTask` / `cancelSecurityAuthTask` 在 testCompile 失败。
- 专用人员下发测试先因缺少 `SecurityAuthDispatchContext` 在 testCompile 失败。
- ISC 聚合测试先因服务仍依赖错误的 `SMT_DEVICE_TASK`、缺少申请单与 ISC 服务依赖失败。
- worker、终态回调、30 秒调度测试分别先因缺少 `processDispatch`、Feign 聚合字段、调度枚举/开关/方法失败。
- 非 ISC 设备防串表测试先进入普通任务路径并出现空动作异常，证明来源边界未生效。

### GREEN

最终定向回归：

```bash
cd smart-module
mvn -pl smart-platform/smart-platform-biz,smart-schedule -am test \
  -Dtest=SecurityAuthDispatchAcceptanceTest,SecurityAuthIscAggregationTest,SmtDeviceTaskServiceImplTest,SmtIscDeviceTaskServiceImplTest,SmtIscDeviceTaskMapperXmlTest,ISCDeviceTaskServiceImplTest,PlatformTimerTaskSecurityDispatchTest \
  -Dsurefire.failIfNoSpecifiedTests=false
```

结果：core 50、biz 10、schedule 64，共 124 个测试全部通过，`BUILD SUCCESS`。更广的任务相关回归曾通过 core 50、biz 51、schedule 64，共 165 个测试。

附加验证：`xmllint --noout SmtIscDeviceTaskMapper.xml` 与 `git diff --check` 均通过。

## 实现结果

- 新增保密区专用上下文和人员下发入口；通用 `updatePersonCard` 语义不变，且保密区来源禁止落入 `SMT_DEVICE_TASK`。
- 旧 INIT/离线、已提交 DOING、超过两分钟保护窗口的空 ISC ID DOING 均以旧状态、旧 batch、旧 updateTime CAS 本地取消；保留 `ISC_TASK_ID`，没有外部 ISC 取消调用。
- `getCardDown`、`getDelayDown` 两个真实 ISC 权限提交查询增加申请单当前批次围栏。
- worker 每轮最多读取一百个候选，按申请单与批次分组；每组在 `TransactionTemplate + SELECT FOR UPDATE` 内完成批次复核、明细领取、旧任务接管和新 ISC 任务创建。
- 当前批次按 `SOURCE_DETAIL_ID` 聚合真实 ISC 多设备任务；全成功才成功，FAIL/CANCEL/EXPIRED 任一出现即失败并保留原因，旧批次迟到结果不参与。
- smart-schedule 新增默认关闭的 Nacos 开关、30 秒调度和显式分布式锁；ISC 终态通过 `@Inner` Feign 触发当前批次聚合。

## 自评审

- 已修正候选查询中 `ORDER BY + ROWNUM` 可能拼出非法 Oracle SQL 的问题。
- 已让单个申请事务失败后回滚并继续处理其他申请，避免坏单长期饿死后续批次。
- 未连接 Oracle/ISC 实环境；上线前仍需按计划在测试库验证 SQL 方言、锁竞争和 ISC 覆盖语义。调度开关应保持关闭，待平台服务发布和演练完成后再开启。

## Review Important 修复（2026-07-16）

独立评审提出的三项 Important 已逐项按 RED→GREEN 修复：

1. `getReTryCardDown` 也引用 `security_auth_current_batch_fence`，错误码重试链不再绕过申请单当前批次。
2. 明细聚合更新增加 `IN_WORK + batchId` CAS；主单仅允许从 WAIT/IN_WORK 推进，并在主单聚合前重读当前批次明细。进度查询的第二条主单回写路径同步增加终态保护，旧回调/旧快照不能把 SUCCESS/FAIL 回退为 IN_WORK。
3. worker 候选改走专用 Mapper SQL：先 JOIN 申请单并匹配 `CURRENT_DISPATCH_BATCH_ID`，再按明细 ID 稳定排序，最后由外层 `ROWNUM` 限额。测试用一百条旧批次作为通用查询陷阱，确认当前批次候选不会被饿死。

聚焦回归命令覆盖 core/biz/schedule，结果 core 44、biz 12、schedule 64，共 120 个测试全部通过；两个 Mapper XML 均通过 `xmllint`，`git diff --check` 通过。
