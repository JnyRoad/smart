# 保密门禁权限异步下发 Runbook

- 覆盖范围：保密区申请的受理批次、人员明细和 ISC 实际设备任务。
- 事实来源：当前批次的 ISC 任务终态是成功、失败和取消的唯一事实来源；HTTP 202 仅表示命令已受理，绝不表示设备已完成。
- 值班原则：只读 SQL 先取证；不得手工改任务状态、批次号或直接调用 ISC 取消接口。需要重试时由有权限的管理员再次点击原“手动下发”按钮。

## 上线前预检

先在目标 Oracle 用户下执行下列只读预检。三张表、字段和三个索引全部存在才可继续；保留 `2026-07-16-security-auth-dispatch-batch-rollback.sql`，但**只要仍有运行中的批次，禁止执行 rollback**。

```sql
SELECT table_name, column_name
  FROM user_tab_columns
 WHERE (table_name = 'SMT_SECURITY_AUTH_APPLY' AND column_name = 'CURRENT_DISPATCH_BATCH_ID')
    OR (table_name = 'SMT_SECURITY_TASK_DETAILS' AND column_name = 'DISPATCH_BATCH_ID')
    OR (table_name = 'SMT_ISC_DEVICE_TASK' AND column_name IN
        ('BATCH_ID', 'SOURCE_TYPE', 'SOURCE_ID', 'SOURCE_DETAIL_ID', 'INTENT_KEY'))
 ORDER BY table_name, column_name;

SELECT index_name, table_name
  FROM user_indexes
 WHERE index_name IN ('IDX_SEC_AUTH_DETAIL_BATCH', 'IDX_ISC_TASK_SECURITY_BATCH',
                      'IDX_ISC_TASK_SECURITY_INTENT')
 ORDER BY index_name;
```

## 三表关联取证

在 SQL 客户端把 `:apply_id` 绑定为目标申请 ID。先确认申请单的 `CURRENT_DISPATCH_BATCH_ID`，再只查看该批次；不要把旧批次的迟到结果当成当前结果。

```sql
SELECT a.id AS apply_id,
       a.process_id,
       a.oa_status,
       a.device_status,
       a.current_dispatch_batch_id,
       d.id AS detail_id,
       d.staff_badge,
       d.staff_name,
       d.status AS detail_status,
       d.remark AS detail_remark,
       t.id AS isc_task_id,
       t.batch_id,
       t.status AS isc_status,
       t.isc_task_id AS isc_remote_task_id,
       t.device_code,
       t.intent_key,
       t.remark AS isc_remark,
       t.update_time AS isc_update_time
  FROM smt_security_auth_apply a
  LEFT JOIN smt_security_task_details d
    ON d.apply_id = a.id
   AND d.dispatch_batch_id = a.current_dispatch_batch_id
  LEFT JOIN smt_isc_device_task t
    ON t.source_type = 'SECURITY_AUTH'
   AND t.source_id = a.id
   AND t.source_detail_id = d.id
   AND t.batch_id = a.current_dispatch_batch_id
 WHERE a.id = :apply_id
 ORDER BY d.id, t.id;
```

ISC 状态：`0=初始化`、`1=成功`、`2=失败`、`3=处理中`、`4=已取消`、`5=权限已过期`、`6=设备离线`。页面“待处理”来自 `0/3`，“取消”只统计 `4`；当前批次接口的脱敏失败原因只来自 `2/4/5`，`6` 的重发处置仍以 ISC 原始任务记录为准。

按人员核对当前批次聚合（多设备人员必须全部成功才算成功）：

```sql
SELECT d.id AS detail_id,
       d.staff_badge,
       d.staff_name,
       COUNT(t.id) AS isc_task_count,
       SUM(CASE WHEN t.status IN (0, 3) THEN 1 ELSE 0 END) AS pending_task_count,
       SUM(CASE WHEN t.status = 1 THEN 1 ELSE 0 END) AS success_task_count,
       SUM(CASE WHEN t.status IN (2, 5, 6) THEN 1 ELSE 0 END) AS failed_task_count,
       SUM(CASE WHEN t.status = 4 THEN 1 ELSE 0 END) AS canceled_task_count,
       LISTAGG(CASE WHEN t.status IN (2, 5, 6) THEN t.remark END, ' | ')
         WITHIN GROUP (ORDER BY t.update_time) AS failure_reasons
  FROM smt_security_auth_apply a
  JOIN smt_security_task_details d
    ON d.apply_id = a.id
   AND d.dispatch_batch_id = a.current_dispatch_batch_id
  LEFT JOIN smt_isc_device_task t
    ON t.source_type = 'SECURITY_AUTH'
   AND t.source_id = a.id
   AND t.source_detail_id = d.id
   AND t.batch_id = a.current_dispatch_batch_id
 WHERE a.id = :apply_id
 GROUP BY d.id, d.staff_badge, d.staff_name
 ORDER BY d.id;
```

## 值班处置

### 正常受理与轮询

1. 管理端只调用 `POST /platform/security/auth/apply/{id}/dispatch`；响应必须是 HTTP `202`，记录返回的 `batchId`。旧 GET 仅供后端兼容，前端和人工操作不使用它。
2. 202 后观察页面轮询和上节关联 SQL：`CURRENT_DISPATCH_BATCH_ID`、人员明细 `DISPATCH_BATCH_ID` 与 ISC 任务 `BATCH_ID` 必须一致。
3. 轮询仅在存在待处理任务时继续；所有人员均处于成功、失败或取消终态时停止。轮询错误要显示且停止，避免后台无限请求。

### 旧任务被新批次接管

管理员再次点击同一“手动下发”就是接管入口，不新增“重置失败人员”按钮，也不人工改库。新 202 返回的新批次必须写入申请单当前批次；核对下列查询中旧任务是否保留审计信息并写明被哪个批次接管。

```sql
SELECT t.id, t.batch_id, t.status, t.isc_task_id, t.remark, t.update_time
  FROM smt_isc_device_task t
 WHERE t.source_type = 'SECURITY_AUTH'
   AND t.source_id = :apply_id
 ORDER BY t.batch_id DESC, t.update_time DESC, t.id DESC;
```

- 尚未提交 ISC 的旧 `INIT` / 设备离线任务可被本地条件取消，备注应包含“被批次 <newBatchId> 接管”。
- 已被 ISC 接收的旧 `DOING` 任务不得调用 ISC 取消接口；保留 `ISC_TASK_ID` 作为审计证据，新批次任务依赖 ISC 覆盖语义收敛权限。

### ISC 迟到结果隔离

当旧 ISC 任务在新批次开始后才回调完成，先按上一节查询确认它的 `BATCH_ID` 不等于 `CURRENT_DISPATCH_BATCH_ID`。该迟到结果只能更新旧任务本身，不能改变当前批次页面的成功、失败、取消数量。若当前页面与“当前批次”SQL 不一致，停止放量并保留 `apply_id`、两次 `batch_id`、旧任务 `id/ISC_TASK_ID`、时间线交给开发排查。

### 设备离线重发

1. 先确认当前批次任务状态为 `6`，并记录 `device_code`、`remark`、`ISC_TASK_ID` 和设备恢复时间；先由设备/ISC 值班确认设备在线。
2. 不要更新 `SMT_SECURITY_TASK_DETAILS.STATUS`、不要把旧 ISC 任务改回 `INIT`。由管理员再次点同一“手动下发”创建新批次，系统自动接管可重试任务。
3. 新批次受理后，按“三表关联取证”确认新任务的 `BATCH_ID` 等于申请单当前批次，旧离线任务仍留在旧批次审计链中。

## 发布、演练与回退

发布顺序固定为：

1. 执行迁移并完成预检；发布 `smart-platform`（模型、202 命令、内部消费端）。
2. 发布 `smart-schedule`，确认 Nacos `task.job.securityAuthDispatchProcess=false`，此时 worker 默认不消费。
3. 发布 `smart-ui`；完成权限码 `platform_security_auth_down` 的菜单与角色授权后，用户重新登录。
4. 测试环境打开 worker 开关，演练正常 202、重复点击、设备离线、ISC 查询失败、ISC 长时间执行和部分人员照片异常。
5. 生产先灰度一张小申请，核对三表、ISC 覆盖结果和 UI 当前批次聚合一致，再逐步放量。

异常时先将 `task.job.securityAuthDispatchProcess` 设为 `false`，停止新 worker 消费；已受理和已提交 ISC 的任务保留现场，不执行 rollback 或手工取消。保留申请 ID、批次 ID、ISC 任务 ID、相关日志后再决定修复或版本回退。
