# OA 回调补偿与保密门禁对账 运维 Runbook

- 对应设计文档：`docs/superpowers/specs/2026-07-04-oa-callback-compensation-design.md`
- 覆盖范围：保密门禁申请（`smt_security_auth_apply`）OA 回调丢失补偿、回调失败重放、对账定时任务
- 读者：运维 / 值班开发，出现"OA 审批完但系统没反应"类问题时按本手册排查

## 日常巡检

以下四条 SQL 照抄 spec 附录 B，建议接入日常巡检脚本或工单系统，任一条数值异常升高都应人工介入。

```sql
-- 1. 超 24 小时仍待审批（疑似回调丢失，等对账补偿或需排查 OA 侧异常）
select count(*) from smt_security_auth_apply
 where oa_status = 0 and create_time < sysdate - 1;

-- 2. 审批已过但未触发下发（D4 中间态，应被对账任务场景2自动重试）
select count(*) from smt_security_auth_apply
 where oa_status = 1 and device_status = 0;

-- 3. 回调处理部分失败（仅看未解决的，已重放成功的 resolved=1 不算）
select count(*) from oa_callback_log where status = 2 and resolved = 0;

-- 4. 明细下发失败（页面"更换照片"/手动下发通道处理，不在本对账范围内）
select count(*) from smt_security_task_details where status = 2;
```

- SQL 1、2 命中且对账开关（见下）已开启，一般会在下一轮（2 分钟）自动被扫到并处理，无需人工干预，只作为超时未愈合的兜底告警。
- SQL 3 命中的记录需要人工排查失败原因（`last_error` 字段）并按下一节重放。
- SQL 4 是既有明细下发失败通道，不属于本次对账范围，走原有"更换照片"/手动下发流程。

## 回调失败重放

### 1. 定位失败的 log

```sql
select id, request_id, failed_handlers, last_error
  from oa_callback_log
 where status = 2 and resolved = 0;
```

拿到目标记录的 `id`（即下面的 `{logId}`）。

### 2. 调用重放接口

接口：`POST /platform/oa/workflow/replay/{logId}`

该接口标了 `@Inner`，是内部调用专用端点（`OAWorkflowController.replay`），不面向前端/公网。鉴权头固定为：

```bash
curl -X POST "http://<gateway>/platform/oa/workflow/replay/{logId}" \
  -H "from: Y"
```

- `from` 头名与取值来自 `SecurityConstants.FROM`（值 `"from"`）与 `SecurityConstants.FROM_IN`（值 `"Y"`），代码见
  `smart/smart-common/smart-common-core/src/main/java/com/tce/smart/common/core/constant/SecurityConstants.java`。
- **重要**：`@Inner` 切面当前处于 **AUDIT 灰度**（三态灰度见记忆 `inner-aspect-three-mode-rollout`，PR#112），AUDIT 模式下裸带 `from: Y` 即可通过，不代表生产已强制校验来源网络。**切到 ENFORCE 后**，该端点将只允许内网（`FROM_IN`）来源调用，运维必须从内网网关/跳板机发起，不能再从公网直接 curl。上线前请先确认当前灰度状态，避免误以为该接口对外开放。

### 3. 重放行为说明

- 只重跑该 log 记录 `failed_handlers` 中失败的 handler，`succeeded_handlers` 中已成功的不会重复执行。
- 重放与自然回调共用同一把 `request_id` 级 Redis 锁，并发到达会被串行化，拿不到锁直接返回"正在处理，请稍后"。
- 重放结果**回写原记录**（`retry_count+1`、合并 `succeeded_handlers`、更新 `status`/`last_error`），不产生新 log；全部成功后该记录 `status=1, resolved=1`。
- 若 logId 不存在，或记录当前不满足 `status=2 and resolved=0`（已被其他重放/对账处理过），接口会拒绝并提示"已解决或状态不符"。

## 对账任务开关与观察

### Nacos 开关

- 配置项：`task.job.securityAuthUpdateOa`（Boolean，relaxed binding 后对应 Nacos key 通常写作 `task.job.security-auth-update-oa`，两种写法等价）
- 代码绑定：`TaskJob`（`prefix = "task.job"`，字段 `securityAuthUpdateOa`），文件：
  `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/config/TaskJob.java`
- 默认值：**false**（关）
- 开 = 启用保密门禁 OA 状态对账定时任务（每 2 分钟一轮，扫描回调丢失单 + 触发下发失败单）
- 关 = 回滚到"纯回调模式"（只依赖 OA 单次回调，不做主动补偿），不影响回调本身处理和重放功能

修改方式：Nacos 控制台找到 `smart-schedule` 对应 dataId，把该配置项改为 `true`/`false`，无需重启，配置中心动态刷新生效。

### 单轮耗时观察项

定时任务分布式锁 TTL 为 5 分钟，按"OA 查询正常毫秒级、5 秒为超时上界"的假设留有裕度。若生产观察到对账日志中单轮耗时超过 5 分钟（两条"保密门禁OA对账完成"日志间隔异常拉长、或出现跨实例并行迹象），需下调批量（`RECONCILE_BATCH_SIZE`）或上调锁 TTL；即使锁过期导致并行，claim CAS 与明细级 CAS 会兜底幂等，不会重复下发。

### 定时任务锁

- 任务方法：`PlatformTimerTask.securityAuthUpdateOaTask()`（`smart-module/smart-schedule`）
- 分布式锁枚举：`TimerTaskEnum.SECURITY_AUTH_UPDATE_OA`，key 为 `timer_security_auth_update_oa`
- 锁 TTL：5 分钟（`SECURITY_AUTH_UPDATE_OA_LOCK_MINUTES`），覆盖单轮最长耗时，防止多实例并发重复扫描

### 对账日志关键字

- 每轮完成后打点（`SmtSecurityAuthApplyServiceImpl.updateOaStatusTask`）：

  ```
  保密门禁OA对账完成：扫描={}, 通过={}, 退回={}, 审批中={}, 查询失败={}, 触发失败={}
  ```

  正常情况每轮都应有一条，用于统计当轮扫描量与处理结果分布。

- 超龄告警（同一方法内，扫描到单据创建超过 24 小时仍未收到 OA 终态时打印）：

  ```
  保密门禁申请超24小时未收到OA终态：processId={}
  ```

  该日志适合接入日志告警系统按关键字抓取，命中即人工介入核实 OA 侧状态。

## 上线 SOP

按以下顺序执行，任一步异常立即停止并回滚上一步：

1. **建表**：执行 `smart-module/database/manual/oa_callback_log.sql`（若 PR1 已在目标环境上线过此表，跳过本步）。
2. **发布 smart-platform**：包含回调分发器、落库、重放接口。此时对账开关仍为默认 **关**，不改变现有回调行为。
3. **发布 smart-schedule**：包含对账定时任务代码，开关默认关，任务注册但不实际执行扫描。
4. **测试环境验证**：构造"无回调"场景（例如手工把某测试单的 `oa_status` 置回 0 并保留 `process_id`），打开测试环境开关，观察对账任务是否在下一轮（2 分钟）自动补齐该单状态；同时用真实 OA 报文核实 §3.1.2 终态判定前置任务（`CURRENTNODETYPE` 语义）。
5. **生产灰度开开关**：确认测试环境验证通过后，在生产 Nacos 把 `task.job.securityAuthUpdateOa` 改为 `true`，观察对账计数日志与 `oa_callback_log` 是否符合预期（无异常报错、计数合理）。
6. **观察事故单自动补齐**：

   ```sql
   select oa_status, device_status from smt_security_auth_apply where process_id = '28753680';
   ```

   预期从 `0 / 0` 变为 `1 / 4`（在对账任务跑到该单所在的窗口批次后，通常 1~2 轮内完成）。

   另一单 `28760183` 需要**先在 OA 侧确认已归档**，再观察对账任务是否将其补齐；如 OA 侧尚未归档（仍在审批中），对账任务本轮会判定"审批中"并跳过，属预期行为，不代表功能异常。

## 应急回滚

- **优先手段**：关闭 Nacos 开关 `task.job.securityAuthUpdateOa` → 回到纯回调模式，对账任务不再扫描/触发下发，风险面收缩到最小。此操作不需要重新发布，配置中心动态生效。
- **次选手段**：若怀疑是本次监听器 Handler 化拆分本身引入的回归（而非对账逻辑问题），可直接回滚 smart-platform 部署版本——除保密门禁 handler 改走 CAS claim 流程外，其余 11 个业务 handler 均为行为等价重构（原样搬迁），回滚风险低。
- 回滚后不需要额外清理数据：`oa_callback_log` 只是审计记录，`claimOaFinalStatus`/明细级抢占均为幂等 CAS 操作，重新开启开关可安全恢复。

## 相关 Redis key 与日志关键字

| 用途 | Key / 关键字 | 说明 |
|---|---|---|
| 回调分发 request_id 级互斥锁 | `oa:callback:lock:{requestId}` | 分发器处理回调、重放接口共用同一把锁，串行化同一 requestId 的并发处理；`OaCallbackDispatcher.LOCK_KEY_PREFIX` |
| 保密门禁对账扫描游标 | `oa:security:auth:cursor` | 场景1批量翻页游标，扫完一轮归零重扫；`SmtSecurityAuthApplyServiceImpl.OA_RECONCILE_CURSOR_KEY` |
| 对账定时任务分布式锁 | `timer_security_auth_update_oa` | 防止多实例并发跑同一轮对账；`TimerTaskEnum.SECURITY_AUTH_UPDATE_OA` |
| 对账每轮完成计数日志 | `保密门禁OA对账完成：` | 含扫描/通过/退回/审批中/查询失败/触发失败六项计数，正常每轮都应出现 |
| 超龄待审批告警日志 | `保密门禁申请超24小时未收到OA终态` | 含 `processId`，建议接入日志告警系统 |

## 相关代码位置（供进一步排查）

- 回调分发器：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/OaCallbackDispatcher.java`
- 重放服务：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/impl/OaCallbackReplayServiceImpl.java`
- 回调 / 重放 controller：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/OAWorkflowController.java`
- 对账主逻辑：`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/securityzone/impl/SmtSecurityAuthApplyServiceImpl.java`
- 对账定时任务：`smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/task/PlatformTimerTask.java`
- Nacos 开关绑定：`smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/config/TaskJob.java`
- 建表脚本：`smart-module/database/manual/oa_callback_log.sql`
