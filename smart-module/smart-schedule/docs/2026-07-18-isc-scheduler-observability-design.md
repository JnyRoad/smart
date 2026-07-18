# ISC 调度日志可观测性设计

## 背景

`smart-schedule` 的 ISC 权限下发日志缺少任务、人员和设备关联字段。出现异步下发失败时，运维人员无法从单条日志定位到对应的人员和设备任务。

## 目标

- ISC 权限任务状态变化可通过任务 ID、人员标识、设备编码、园区和 ISC 任务 ID 检索。
- ISC 请求与响应仍可用于排障，但手机号和证件号必须脱敏；人脸、照片、Base64 及超长值必须截断。
- 定时任务记录开始、完成或失败及耗时；离线设备重发任务记录汇总结果。

## 非目标

- 不修改 `SMT_ISC_DEVICE_TASK`、不新增表或接口。
- 不改变 ISC 下发、重试、任务状态机和调度开关的业务行为。
- 不为补充姓名增加数据库或远程调用；任务已有的工号、人员 ID 和设备字段足以关联业务数据。

## 日志契约

### 单任务状态

`event=isc_auth_task_state` 固定包含：`task_id`、`park_id`、`staff_id`、`staff_no`、`person_id`、`device_code`、`action`、`service_type`、`isc_task_id`、`status_from`、`status_to` 和 `retry_times`。

### ISC 调用

`event=isc_auth_dispatch_request` 与 `event=isc_auth_dispatch_response` 固定包含分发事件、园区、设备、任务数量、任务 ID 摘要和耗时。请求与响应载荷保留在 `payload` 字段。

### 定时任务

`event=isc_scheduler_run` 固定包含 `job_name`、`outcome` 和 `elapsed_ms`。未启用的任务仅输出 DEBUG 级跳过日志。

## 敏感字段处理

- 手机号、电话号、身份证号及证件号字段输出脱敏值。
- 键名包含 `face`、`photo`、`image` 或 `base64` 的超长字符串仅保留前缀、原始长度和截断标识。
- 其他 ISC 请求/响应最大保留 16 KiB，并带 `truncated=true` 标识。

## 修改范围

1. 新增 ISC 载荷格式化工具及单元测试。
2. 在 `ISCDeviceTaskServiceImpl` 为权限任务状态和分发请求/响应补充结构化字段。
3. 在 `ISCDeviceTimerTask` 统一记录 ISC 定时任务结果与耗时。
4. 在 `OfflineDeviceTaskHandler` 输出设备样本、任务上下文和处理汇总，避免 INFO 级输出完整设备列表。
