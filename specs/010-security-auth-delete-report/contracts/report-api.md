# 报表契约

## HTTP

网关前缀 `/platform`；Controller `/security/auth/delete/log`。

- `GET /page`：`current`（默认1）、`size`（默认20，1–100）；筛选 `parkId, startTime, endTime, staffBadge, staffName, department, authName, result`。时间格式 `yyyy-MM-dd HH:mm:ss`，包含起止时间所表示的整秒；SQL以endTime加1秒作为排他上界，包含当天23:59:59的小数秒。响应沿用 `Result<IPage<SecurityAuthDeleteLogRespDTO>>`。
- `GET /export`：相同筛选，服务端最多10000条，超限明确失败；响应下载文件（CSV UTF-8 BOM），中文表头，防公式注入。权限 `platform_security_auth_delete_log_export`。
- `GET /{id}/tasks`：先验证主记录及园区范围，再返回 `Result<List<SecurityAuthDeleteLogTaskRespDTO>>`。查询权限 `platform_security_auth_delete_log_view`。
- `/page` 和 `/{id}/tasks` 使用查询权限；所有接口根据 `SecurityUtils.getUser().getParkIdList()` 再校验园区范围，不能接受客户端的 parkIdList。

## 报表字段

`id`（字符串）、`parkId`、`execTime`、`staffId`（字符串）、`staffBadge`、`staffName`、`department`、`authId`（Integer）、`authName`、`lastSnapTime`、`triggerReason`、`result`、`remark`、`taskCount`、`successCount`、`failCount`、`pendingCount`、`unknownCount`。

结果：`SKIPPED_WHITELIST` 白名单跳过、`SKIPPED_NOT_DUE` 未到删除期限、`SKIPPED_NO_DEVICE` 无关联设备、`SKIPPED_STAFF_MISSING` 人员不存在、`SKIPPED_MISSING_TIME` 缺少判定时间、`DRY_RUN` 演练命中、`PROCESSING` 任务执行中、`SUCCESS` 任务执行成功、`FAILED` 处理或任务失败、`UNKNOWN` 任务状态未知。

状态聚合：任务失败/取消/过期（2/4/5）优先FAILED；无关联、缺失、非法状态为UNKNOWN；状态0、3或6（离线）为PROCESSING；全部状态1才SUCCESS。非正式删权结果直接使用主表结果。

任务详情字段：`taskSource`（NORMAL/ISC）、`taskId`（字符串）、`deviceCode`（生成时快照）、`action`、`status`（任务缺失或状态为空时为null，界面显示“未知”；不伪造成功或引入设备状态码）、`code`、`remark`、`createTime`、`updateTime`。仅从该记录的关联表取任务，不暴露任意任务查询。

## 审计写入接口

`SmtSecurityAuthDeleteLogService.record(SmtSecurityAuthDeleteLog log, List<SecurityAuthDeleteTaskRef> tasks)`：在调用者事务中保存主记录和任务来源+ID+deviceCode+action快照；任一写入失败抛异常，不静默返回。日志实体主键Long，权限组Integer，普通任务主键Integer、ISC任务主键Long，统一以字符串传输。正式提交写 `result=PROCESSING`；任务集合为空不得标记正式成功。

`SmtStaffService.savePersonCardTasksWithResult(...)`：兼容新增入口，与既有 savePersonCardTask 参数相同，返回实际创建的全部 `List<SecurityAuthDeleteTaskRef>`；原void方法保留兼容行为。审计入口保存失败/没有返回合法数字主键必须抛错。

`SecurityAuthDeleteTaskRef` 位于 `core/dto/securityzone`，字段 `taskSource(String NORMAL/ISC), taskId(String), deviceCode(String), action(Integer)`。根据真实设备路由确定来源，普通任务查询 `smt_device_task`，ISC 查询 `smt_isc_device_task`；不能混用两个表的同号任务。

## 演练配置

现有配置 entity、ReqDTO、RespDTO 增加 `dryRun`（Integer，0/1）；新建默认0，历史null按0；旧客户端未传时保持原值，非法值拒绝。只有命中超限才进入演练分支，不生成设备任务、不删除权限。
