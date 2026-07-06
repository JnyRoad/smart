# OA 回调补偿与监听器隔离改造 — 设计方案

- 日期：2026-07-04（v6 定稿）
- 状态：**已定稿**（Codex 六轮评审后 approved，2026-07-05；实施要求：TTL 上界推导与"handler 外部调用必须有超时"做成硬校验断言，不得只留注释）
- 相关事故：OA 单 28753680（sc-bm2026070016）保密门禁申请，OA 已审批完成但系统停留"待审批/待下发"

## 0. v2 修订记录（对应 Codex 评审 10 条）

| Codex # | 处理 | 摘要 |
|---|---|---|
| 1 | 采纳 | 回调与对账共用 `claimOaFinalStatus` CAS；明细级原子抢占（§3.1） |
| 2 | 部分采纳 | 显式定义主表/明细状态语义与补偿边界（§3.1.5）；**不重做**主表状态机，理由见 §6 |
| 3 | 采纳 | 分发器全量执行、汇总失败后向 OA 返回非 200，保留 OA 重试语义（§3.2） |
| 4 | 采纳 | 保密门禁独立设计扫描窗口/游标/索引，不照搬入厂（§3.1.4） |
| 5 | 采纳 | 终态解析按操作时间排序 + 实施前用真实报文核实字段语义（§3.1.2） |
| 6 | 采纳 | smt_oa_callback_log 增加结构化状态列与重放定义（§3.3） |
| 7 | 采纳 | 结构化计数日志 + 巡检 SQL 清单 + 超龄告警日志（§5.3） |
| 8 | 采纳 | 过程记录归一化 DTO，收敛 6+ 处重复（§3.2.4） |
| 9 | 采纳 | 手动下发补输入/状态校验，复用同一 claim 流程（§3.4） |
| 10 | 采纳 | 事故证据标注为运维事实并附验证命令（附录 A） |

v3 增补（Codex 复审新问题）：

| Codex # | 处理 | 摘要 |
|---|---|---|
| N1 | 采纳 | 部分成功后 OA 重试跳过已成功 handler：smt_oa_callback_log 增 `succeeded_handlers` 列 + 分发器跳过逻辑（§3.2.2） |
| N2 | 采纳 | 重放改为按 log id 的内部接口，retry_count 回写原记录，不再重新 POST 网关入口（§3.3） |

Codex 复审同时确认：#2 分歧（不重做主表 device_status 状态机）被接受，前提是守住不变量——主表 `4` 仅表示"已触发下发"，最终成败以明细表为准（§3.1.5 已如此定义）。

v4 增补（Codex 三审缺口）：

| 三审条目 | 处理 | 摘要 |
|---|---|---|
| N1 缺口：历史 partial log 关闭规则 | 采纳 | 增 `resolved` 列；处理完成后无条件关闭命中的旧 partial，跳过集合只取未解决记录（§3.2.2） |
| 排序/索引未定义 | 采纳 | 写明 `order by receive_time desc, id desc` 与组合索引（§3.2.2、§3.3） |
| N2 缺口：并发重放 | 采纳 | request_id 级互斥（v5 升级，见下）+ CAS 回写（§3.3） |
| 测试缺口 | 采纳 | 补 partial 关闭/排序选择/并发重放测试（§4） |

v5 增补（Codex 四审）：

| 四审条目 | 处理 | 摘要 |
|---|---|---|
| High-a：同 request_id 并发回调可打破"至多一条未解决 partial"不变量 | 采纳 | 分发器全程持 request_id 级 Redis 互斥锁 + Oracle 函数唯一索引兜底（§3.2.2、§3.3 表结构） |
| High-b：replay 与自然回调无共享并发控制，失败 handler 可被并发重复执行 | 采纳（实现偏差） | replay 与分发器共用同一把 request_id 锁；DB 行锁表方案不采用，理由见 §6 | 
| Medium：`Result.fail()`/全局异常处理器均返回 HTTP 200，"非 200"易实现失效 | 采纳 | 回调 controller 显式 `ResponseEntity.status(500)`，不依赖异常抛出；MockMvc 断言真实 HTTP status（§3.2.2、§4） |

v6 增补（Codex 终审）：

| 终审条目 | 处理 | 摘要 |
|---|---|---|
| High：锁 TTL 过期窗口仍可能重复执行 handler 副作用 | 采纳 | 锁 TTL 必须大于分发全程耗时上界（各外部调用显式超时推导 + 安全余量，初始 10 分钟）；唯一索引兜底表述限定为"日志不变量"，不承诺副作用兜底；补超 TTL 模拟测试（§3.2.2、§4、§6） |
| Low：`ISwitchService` 位于 smart-schedule，platform 不能直接复用 | 采纳 | 表述改为抽公共 raw-key 锁 helper（token+Lua 模式）（§3.2.2） |

## 1. 背景与问题

### 1.1 事故根因

保密门禁申请的状态同步**完全依赖 OA 归档时的一次性回调** `POST /platform/oa/workflow/over`（入口 `OAWorkflowController.listen` → Spring 同步事件 → `LeaveApplicationListener`）。2026-07-02 有两单（28753680、28760183）OA 未推送回调，单据永久卡在 `oa_status=0`（待审批）、`device_status=0`（待下发），权限无法下发。

> 证据为生产运维事实（服务器日志 + 生产库查询），仓库内不可复验，验证命令见附录 A。

### 1.2 现有代码缺陷清单

| # | 缺陷 | 位置 | 影响 |
|---|---|---|---|
| D1 | 回调丢失无补偿：无对账任务，丢一次卡死一单 | 整体设计 | 本次事故 |
| D2 | 监听器约 12 个业务分支共用一个同步事务（`saveProcessRecord` 为 `@Transactional`，Spring 事件同步执行）：任一前置分支异常 → 后续分支不执行 + 全体回滚 | `LeaveApplicationListener`（449 行单类） | 另一类状态丢失来源 |
| D3 | 回调报文不落库：无法审计"OA 推没推" | `OAWorkflowController` | 排查成本高（本次约 40 分钟翻归档日志） |
| D4 | `updateStatus` 下发异常被吞（catch 后仅 log.error 无堆栈），产生 `oa_status=1 && device_status=0` 中间态且无人发现 | `SmtSecurityAuthApplyServiceImpl:109-119` | 审批过了但没下发 |
| D5 | 手动下发 `/down/{id}` 不校验记录存在/`processId`/`oa_status`：未审批也能下发保密区权限（越权）；下发后不修 `oa_status`，列表页成功/失败数刷新条件（`oaStatus=1 && deviceStatus=4`）永不满足 | `SmtSecurityAuthApplyServiceImpl:121-127`、`:348-349` | 安全 + 状态不一致 |
| D6 | 转发大岭山 `HttpUtil.post` 无超时 + 空 catch | `LeaveApplicationListener:92-96` | 挂住拖死所有 OA 回调 |
| D7 | `log.info("收到OA审批消息：{}" + message)` 占位符误用 | `LeaveApplicationListener:88` | 轻微 |

### 1.3 可复用资产与差异声明

- **入厂申请 OA 拉取对账**（`SmtAdmittanceApplyServiceImpl.updateOaStatusTask`，HF 访客同款）：提供模式参考——OA 日志查询、`CURRENTNODETYPE` 终态判定、CAS 抢占、Redis 游标、失败重试。
- **差异声明（Codex #4）**：入厂扫描条件为 `status=待审核 + processId not null + endTime > now`，依赖入厂实体的 `endTime` 天然截止；保密门禁实体（`SmtSecurityAuthApply`）无 endTime，扫描边界须独立设计（见 §3.1.4），**模式复用、参数与边界不照搬**。
- **调度接线模式**（`EHRViewTimerTask.admittanceUpdateOaTask`）：`@Scheduled` + Nacos `TaskJob` 开关 + `TimerTaskEnum` Redis 分布式锁 + Feign `FROM_IN`。
- OA 实时查询接口 `OAWorkflowServiceImpl.query(requestId)`（带超时，详情页在用）。

## 2. 决策记录

| 决策点 | 结论 |
|---|---|
| 对账覆盖范围 | 本次只做保密门禁申请；其他业务列入后续扩展清单，框架可复用 |
| 监听器改造方式 | 拆成独立 Handler（业务逻辑原样搬迁不改写） |
| 回调报文落库 | 做（smt_oa_callback_log 表，含结构化状态列） |
| 事故单 28753680 处置 | 不手工改数据，等对账任务上线自动补齐 |
| Handler 失败对 OA 的响应 | 全量执行、汇总失败后返回非 200（保留 OA 重试语义） |
| 主表 device_status 状态机 | 本期不改语义，仅显式文档化 + 补偿覆盖两类中间态（分歧说明见 §6） |

## 3. 方案设计

### 3.1 保密门禁申请 OA 拉取对账

#### 3.1.1 统一终态抢占（Codex #1）

新增 `SmtSecurityAuthApplyService.claimOaFinalStatus(applyId, finalOaStatus)`：

```sql
update smt_security_auth_apply set oa_status = :final where id = :id and oa_status = 0
```

- **回调 handler 与对账任务都必须经它抢占**，返回 1 才允许触发下发/拒绝处理；返回 0 说明对方已处理，直接跳过。取代现回调路径"getByProcessId 后直接 updateStatus"的裸写。
- 明细级原子抢占：`down()` 前先 `update smt_security_task_details set status=3(IN_WORK) where id=:id and status=0(WAIT)`，抢到才对该人员执行设备下发——两路并发（迟到回调 × 对账、双实例）都不会重复下发同一人。

#### 3.1.2 终态判定（Codex #5）

- `oaWorkflowService.query(processId)` 成功后，将流转记录按 `OPERATEDATE + OPERATETIME` 升序排序，取**排序后最新一条**的 `CURRENTNODETYPE`：`3`（归档）→ 通过；`0` → 退回；其余/空/解析失败 → 视为审批中，跳过等下轮。
- **实施前置任务**：用真实 OA 报文样本核实 `CURRENTNODETYPE` 是流程级（所有记录同值）还是记录级，并将结论回写本文档；单测覆盖乱序、重复记录、退回后再提交、空数据、查询异常。

#### 3.1.3 补偿扫描对象与处理

| 场景 | 条件 | 处理 |
|---|---|---|
| 回调丢失（本次事故） | `oa_status=0 && process_id is not null && create_time between now()-90天 and now()-5分钟` | 查 OA 终态 → claim → 通过则下发 / 退回则置 2；仍在审批则跳过 |
| 已通过但下发未执行（D4 中间态） | `oa_status=1 && device_status=0`（同 90 天窗口） | 不查 OA，直接重触发下发（明细级抢占保证幂等） |

- 通过后同步补写 `smt_process_record`（走 §3.2.4 的归一化组件），详情页本地留痕。
- 下发语义修正（修 D4）：`downDevice` 全部明细触发成功才置主表 `device_status=4`；抛异常则主表保持现值、记带堆栈 ERROR，下轮补偿场景 2 重试。

#### 3.1.4 扫描边界、游标与索引（Codex #4）

- 回溯窗口：`create_time >= now() - 90 天`（早于窗口的历史单不自动补，避免翻出陈年数据触发过期权限下发；如需处理走手动下发）。
- 每轮批量上限 200 条，`order by id asc` + Redis 共享游标（key 独立于入厂），扫完一轮游标归零；OA 查询逐单串行，失败单记入 Redis 重查集合（复用入厂 `rememberPendingOaStatus` 模式），下轮优先。
- 索引评估：`smt_security_auth_apply(oa_status, create_time)` 复合索引；上线前用生产数据量核对执行计划（当前表量级小，预计可延后建）。

#### 3.1.5 状态语义显式化（Codex #2，部分采纳）

- 主表 `device_status` 语义 = **下发触发状态**（0=未触发，4=已触发），**不是**逐人下发结果；真实结果在明细表 `smt_security_task_details.status`（0 待下发/3 下发中/1 成功/2 失败），由既有 20 分钟任务 `syncTaskStatus` 同步。
- 本期补偿只负责把单据推进到"已触发下发"；明细 FAIL 的重试沿用既有机制（页面"更换照片"、手动下发），**不在本期范围**。不重做主表状态机的理由见 §6。

#### 3.1.6 调度接线

`PlatformTimerTask` 新增 `@Scheduled(fixedDelay = 2 分钟)` 方法：Nacos `TaskJob` 新开关（默认关）→ `ISwitchService.acquire/release`（新增 `TimerTaskEnum` 键，锁 TTL 覆盖单轮最长耗时）→ Feign `RemoteSecurityAuthService` 新方法（`FROM_IN` + `@Inner`）→ platform 端 `updateOaStatusTask()`。

### 3.2 监听器拆分（Handler 化）

#### 3.2.1 接口与搬迁

- 新增 `OaWorkflowCallbackHandler`：`String name()`；`void handle(String processId, WorkFlowAO ao)`。handler 内部自行按 processId 查表决定是否处理（与现逻辑一致）。
- 现有 12 个分支（离职、请假、加班、补卡、调休、外宿、外宿补贴撤销、保密区预约、保密区权限申请、入厂申请、HF 访客、物品放行）逐一**原样搬迁**为独立 `@Component`；PR 内每业务一个 commit 便于 diff。
- 保密区权限申请 handler 改为走 §3.1.1 的 claim 流程（这是唯一一处行为变更，其余 handler 零改动）。

#### 3.2.2 失败语义（Codex #3 + N1 + 四审 High-a/Medium）

- **request_id 级互斥（四审 High-a/b）**：分发器处理回调前，先按 `request_id` 获取 Redis 互斥锁（在 `smart-common`/`smart-tool` 新增公共 raw-key 锁 helper，沿用 `ISwitchService` 的 token + `setIfAbsent` + Lua 原子释放模式——`ISwitchService` 本身位于 smart-schedule 且 API 绑定 `TimerTaskEnum`，不直接复用；key 形如 `oa:callback:lock:{requestId}`），**查跳过集合 → 执行 handlers → 落库/回写/关闭旧 partial 全程在锁内**。拿不到锁：短暂重试（3 次 × 2 秒）后仍失败 → 落库 status=0 备查并返回 HTTP 500（交给 OA 重试）。同 request_id 的自然回调、OA 重推、重放接口全部串行化，"至多一条未解决 partial"不变量在处理层得到保证。
- **锁 TTL 约束（终审 High）**：TTL 必须**大于分发全程耗时上界**并留安全余量。上界可推导：handler 数（12）× 单 handler 最坏耗时——handler 内外部调用均有显式超时（大岭山转发 5s、OA query 超时、Feign 全局超时），实施时以实际超时配置累加核定；**初始值定 10 分钟**，并在实现中断言"任一 handler 无未设超时的外部调用"。不采用续租方案（复杂度高，且处理上界可静态推导）。
- **不变量兜底（表述限定）**：Oracle 函数唯一索引 `unique index ux_oa_cb_unresolved on smt_oa_callback_log (case when status=2 and resolved=0 then request_id end)`——它**只兜底"日志层至多一条未解决 partial"这一不变量**（第二条写入报错、记 ERROR 并落为 resolved=1 失败快照），**不能防止锁过期窗口内 handler 副作用被重复执行**；副作用重复的防线是上一条的 TTL 上界约束 + handler 自身幂等（过程记录判重、保密门禁 CAS claim）。
- 分发器循环执行**全部** handler，逐个 try/catch；单个失败不阻断其他 handler。
- 全部执行完后若存在失败：回写 smt_oa_callback_log（status=部分失败 + failed_handlers + succeeded_handlers + 异常摘要），**并向 OA 返回 HTTP 500**。
- **HTTP 状态码实现约束（四审 Medium）**：项目 `Result.fail()` 与 `GlobalExceptionHandlerResolver` 均返回 HTTP 200（仅业务码非 0），**不能**靠返回 `Result.fail` 或抛异常实现"非 200"；回调 controller 必须显式返回 `ResponseEntity.status(500)`。测试用 MockMvc 断言真实 HTTP status（见 §4）。
- 全部成功：回写成功状态（含 succeeded_handlers），返回 HTTP 200。
- **重试跳过已成功 handler（N1）**：分发器处理任一回调前，先查 `smt_oa_callback_log` 中同 `request_id` 的**未解决 partial 记录**（`status=2 and resolved=0`，`order by receive_time desc, id desc` 取第一条；`receive_time` 相同用 `id desc` 兜底）：
  - 存在 → 该记录的 `succeeded_handlers` 作为跳过集合，本次只执行未成功的 handler（防止 OA 因非 200 重推时，已成功 handler 重复产生副作用，如重复 App/微信通知）；
  - 不存在（首次回调，或历史 partial 均已解决）→ 全量执行。此时 OA 的再次推送视为流程新事件，幂等由过程记录判重与保密门禁 CAS claim 兜底——与今天 OA 重推的现状行为一致，无回归。
- **历史 partial 记录关闭规则（三审缺口 1）**：本次回调处理完成后，**无条件**将命中的旧 partial 记录置 `resolved=1`（同一处理流内回写）——无论本次全成功还是仍有失败。本次结果照常落新 log，其 `succeeded_handlers` = 本次实际成功 + 跳过集合（合并值）；若本次仍有失败，新 log 即成为唯一未解决 partial（status=2, resolved=0）。不变量：**任一 request_id 至多存在一条未解决 partial**。巡检与告警只看 `status=2 and resolved=0`。

#### 3.2.3 事务边界

`saveProcessRecord`（发布/分发环节）去除 `@Transactional`；各 handler 沿用自己业务 service 层事务。任一业务失败只回滚自身，不再全体回滚。

#### 3.2.4 过程记录归一化（Codex #8）

- 现状：`processRecord` 判重写入逻辑在 ≥6 处重复（`LeaveApplicationListener:398`、`LeaveApplicationServiceImpl:186`、`SmtAskLeaveApplicationServiceImpl:357`、`SmtOvertimeApplicationServiceImpl:239`、`SmtReplaceApplicationServiceImpl:344`、`SmtArticlesReleaseServiceImpl:805` 等），且入参分两套 DTO（回调 `WorkFlowRecordAO` / OA 查询 `WorkFlowLogDataDTO`）。
- 方案：定义归一化 `ProcessRecordItem`（workcode/lastname/nodename/logtype/operate 时间/remark），两套 DTO 各提供一个转换器；判重写入收敛为单一组件 `ProcessRecordWriter`。本期先接入：分发器公共路径 + 保密门禁对账补写；其余 5 处调用点替换列入搬迁 commit（机械替换）。
- 回退 flag 判断、`htmlHandle` 一并抽入共享组件。
- 转发大岭山（D6）：挪入分发器，5 秒超时，异常记 WARN；转发失败不影响本地处理、不影响对 OA 的响应码。修 D7 占位符。

### 3.3 回调报文落库（smt_oa_callback_log，Codex #6）

- 新表（Oracle，脚本入 `smart-module/database/manual/`）：

| 列 | 类型 | 说明 |
|---|---|---|
| id | number 主键 | |
| request_id | varchar2(64)，索引 | OA requestid |
| payload | CLOB | 完整报文 |
| receive_time | date，索引 | |
| status | number(1) | 0=已接收 1=处理成功 2=部分失败 |
| resolved | number(1) 默认 0 | 0=未解决 1=已解决（后续重试/重放成功或被新 partial 取代） |
| succeeded_handlers | varchar2(512) | 成功 handler 名逗号分隔（N1 跳过集合，含合并值） |
| failed_handlers | varchar2(512) | 失败 handler 名逗号分隔 |
| last_error | varchar2(2000) | 最后一次失败摘要 |
| retry_count | number(3) 默认 0 | 重放次数 |
| cost_ms | number | |

- 索引：`(request_id, status, resolved, receive_time, id)` 组合索引，支撑"未解决 partial 查询 + 排序"；**函数唯一索引** `ux_oa_cb_unresolved`（§3.2.2）兜底不变量；`receive_time` 单列索引撤销（被组合索引覆盖场景有限，保留 `receive_time` 用于清理/巡检可另评估）。
- 入口先落库（`REQUIRES_NEW` 独立事务，落库失败仅记日志不阻断处理）；分发完回写 status/resolved 关联/succeeded_handlers/failed_handlers/last_error/cost_ms。
- **重放定义（N2）**：新增内部重放接口 `POST /oa/workflow/replay/{logId}`（`@Inner` + `FROM_IN`，仅供运维/管理端调用）：按 logId 读取原 payload，跳过该记录 `succeeded_handlers` 中的 handler，只重跑失败项；执行后**回写原记录**——`retry_count+1`、合并 succeeded_handlers、更新 status/last_error（全部成功则 `status=1, resolved=1`），**不产生新 log 记录**，落点无歧义。
- **重放并发控制（三审缺口 3，v5 升级为共享锁）**：重放接口先按 logId 读出 `request_id`，然后**获取与分发器同一把 request_id Redis 锁**（拿不到直接返回"正在处理，请稍后"），锁内校验记录 `status=2 and resolved=0`（不满足则返回"已解决或状态不符"）→ 只重跑失败 handler → CAS 回写原记录（`where id=:logId and status=2 and resolved=0`）。replay 对 replay、replay 对自然回调的并发都被同一把锁串行化；v4 的 `for update nowait` 行锁方案废弃（原因：分发器跨多个独立事务，无法用 DB 行锁覆盖全程，见 §6）。
- 本期不做自动重试 worker——保密门禁已有对账兜底，其余业务失败量未知，先靠 `status=2 and resolved=0` 巡检发现、人工重放，避免过早建设。运维手册附 curl 示例。
- 保留策略：暂不自动清理（日均几十条），列入巡检观察。

### 3.4 手动下发修复（D5，Codex #9）

`/down/{id}` 重写为：

1. 校验：记录存在（不存在返回明确错误，杜绝现 NPE）、`process_id` 非空。
2. 按当前状态分支：
   - `oa_status=0`：实时 `query(processId)` 判终态——归档：走 §3.1.1 claim + 补过程记录 + 下发；退回：claim 置 2 并拒绝；审批中/查询失败：明确报错拒绝。
   - `oa_status=1`：直接重触发下发（明细级抢占幂等），用于补漏/重试。
   - `oa_status=2`：拒绝并提示已被 OA 退回。
3. 不提供"未审批强制下发"口子。手动入口与回调、对账共用同一套 claim/backfill/down 流程，无第三套逻辑。

## 4. 测试策略

- **unit**：终态解析（3/0/其他/空/乱序/重复/退回后重提/查询异常）、`claimOaFinalStatus` 并发抢占（仅一方成功）、明细级抢占幂等、handler 隔离（第 N 个抛异常其余照跑且各自事务独立）、失败汇总后响应非 200、手动下发全分支（不存在/无 processId/0-归档/0-退回/0-审批中/1/2）、下发失败不置 4。
- **unit（N1/N2 增补）**：部分失败后重推跳过 succeeded_handlers、上次全成功则全量执行、重放接口只跑失败项并回写原记录（retry_count/status/succeeded_handlers 合并）、logId 不存在或 `status≠2 or resolved≠0` 的重放拒绝。
- **unit（三审缺口 4 增补）**：同 request_id 多条日志时按 `receive_time desc, id desc` 选中正确 partial；partial 后自然重试全成功 → 旧记录 `resolved=1` 且后续回调全量执行（不再被旧 partial 影响）；partial 后重试仍有失败 → 旧记录关闭、新 log 成为唯一未解决 partial（succeeded_handlers 为合并值）；两个并发 replay 同一 logId 仅一个执行、另一个收到"正在重放"。
- **unit/integration（四审增补）**：两条同 request_id 回调并发到达 → 锁串行化、处理后至多一条未解决 partial（不变量保持）；replay 执行中自然回调到达 → 被锁挡住不并发重跑失败 handler；锁获取重试耗尽 → 落库 status=0 且返回 HTTP 500；MockMvc 断言部分失败时真实 HTTP status=500、全成功时 200（不被全局异常处理器"吞"成 200）。
- **unit（终审增补，超 TTL 场景）**：用短 TTL 模拟锁过期——处理耗时超过 TTL 后第二请求获锁进入，断言唯一索引拦下第二条未解决 partial（记 ERROR + resolved=1 失败快照）、且 handler 幂等机制（过程记录判重/CAS claim）挡住关键副作用；同时校验默认 TTL 大于按超时配置推导的处理上界。
- **integration**：mock OA query 跑对账全链路（两类扫描对象 + 游标翻页 + 失败重查集合）；回调分发器全 handler 冒烟；smt_oa_callback_log 落库/回写/重放幂等。
- 参考 `SmtAdmittanceApplyServiceImplTest` 既有写法。

## 5. 上线与运维

### 5.1 上线顺序

1. 建表（smt_oa_callback_log）→ 发 platform → 发 schedule（Nacos 开关默认关）。
2. 测试环境构造"无回调"场景验证对账自动补齐；用真实 OA 报文核实 §3.1.2 前置任务。
3. 生产灰度开开关，观察对账计数日志与 smt_oa_callback_log。
4. 预期 28753680 被自动补齐；28760183 先在 OA 侧确认已归档再观察。

### 5.2 回滚

- 关 Nacos 开关 → 回到纯回调模式。
- Handler 拆分为行为等价重构（除保密门禁 claim 外零行为变更），异常回滚部署即可。

### 5.3 监控与巡检（Codex #7）

- 对账任务每轮输出结构化计数日志：`扫描数/补偿成功/退回/仍在审批/OA查询失败/下发触发失败`。
- 发现 `oa_status=0` 超 24 小时的单：log.warn 单独打点（含 processId），便于日志告警系统抓取。
- 巡检 SQL 清单（附录 B）：超龄待审批数、`oa_status=1&&device_status=0` 数、smt_oa_callback_log `status=2` 数、明细 FAIL 数。
- 正式指标/告警系统接入列为后续项。

## 6. 分歧说明与风险

- **对 Codex 四审 High-b 修复方式的偏差（终审已接受方向）**：Codex 建议新建 `oa_callback_request_lock` 行锁表 + `select for update nowait` 实现 request_id 级互斥。本方案改用 **Redis 互斥锁**（公共 raw-key helper，token+Lua 模式）。理由：① 分发器"执行全部 handler"跨多个相互独立的业务事务（§3.2.3），DB 行锁要覆盖全程必须让外层持有一个长事务，与"每 handler 独立事务"的设计直接冲突，且长事务持锁放大 DB 风险；② token+Lua Redis 锁是本项目定时任务防重的既有生产模式，成本与心智负担最低。锁 TTL 过期窗口的处理见 §3.2.2 终审 High 条目：TTL 以显式超时上界推导（初始 10 分钟）+ 唯一索引仅兜底日志不变量 + handler 幂等挡副作用，不做续租。
- **对 Codex #2 的分歧**：不重做主表 `device_status` 状态机（`WAIT→IN_WORK→SUCCESS/FAIL`）。理由：该字段取值被列表页查询/前端下拉/20 分钟消息任务共同消费，改语义需要同步改前端与存量数据迁移，回归面远超本次事故域；本期以"文档化现状语义 + 补偿覆盖两类中间态 + 明细级真实状态已有同步机制"达成同等排障能力。状态机重构列入后续扩展清单。
- `CURRENTNODETYPE` 语义待真实报文核实（§3.1.2 前置任务），核实前不进入编码。
- Handler 搬迁回归面：一业务一 commit + 逐个 diff + 集成冒烟控制。
- OA 回调仅进 10.0.20.113 单点网关属基础设施拓扑问题，不在本方案内，另行提示运维。
- 其他业务对账接入、自动重试 worker、状态机重构：后续扩展清单。

## 附录 A：事故证据（生产运维事实，仓库内不可复验）

- 生产库（plmdb.szyuto.com/ev6db，tech_platform）：
  - `select oa_status, device_status from smt_security_auth_apply where process_id='28753680'` → `0 / 0`
  - `select count(*) from smt_process_record where process_id='28753680'` → `0`（正常单如 28759709 有 3 条）
- 网关日志（10.0.20.113 `/home/yuto/smart/logs/smart-gateway/2026-07/`）：`zgrep "workflow/over" info.2026-07-02.*.log.gz` → 全天 16 次回调全部 200，终审 09:57:36 后最近两条（09:59:12/09:59:20）requestid 为 28753710/28742947，均非本单。
- 平台日志（三台 113/136/119 `/home/yuto/smart/logs/smart-platform/2026-07/`）：`zgrep "28753680\|sc-bm2026070016\|6cbaf2a5dd2f4eed8694589c5c0c47ce" info.2026-07-0[234].*.log.gz` → 零命中；当天"保密区权限申请收到OA推送"共 8 条，均为其他正常单。

## 附录 B：巡检 SQL（示例）

```sql
-- 超 24 小时仍待审批（疑似回调丢失，待对账补偿或 OA 侧异常）
select count(*) from smt_security_auth_apply
 where oa_status = 0 and create_time < sysdate - 1;

-- 审批已过但未触发下发（D4 中间态）
select count(*) from smt_security_auth_apply
 where oa_status = 1 and device_status = 0;

-- 回调处理部分失败（仅未解决的）
select count(*) from smt_oa_callback_log where status = 2 and resolved = 0;

-- 明细下发失败
select count(*) from smt_security_task_details where status = 2;
```
