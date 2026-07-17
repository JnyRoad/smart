# 保密区门禁权限最新下发接管 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task-by-task. 每个任务完成后更新复选框，并按任务粒度提交。

## 1. 目标、范围与非目标

### 1.1 目标

修复保密区门禁申请的“手动下发”在人员多时超过 smart-ui 30 秒 Axios 超时，但后端继续执行的问题。管理员再次走现有的**手动下发**流程时，系统应创建一次新的、可追踪的下发批次；对尚未提交 ISC 的旧任务自动取消，使最新批次接管，管理员不需要到数据库清理任务。

### 1.2 本期范围

- 仅覆盖保密区门禁申请：`SmtSecurityAuthApply` / `SmtSecurityTaskDetails` 的手动下发路径。
- 实际设备执行任务仍以 `SMT_ISC_DEVICE_TASK` 为唯一事实来源；不新增控制表，批次信息直接写入现有申请单、下发明细和 ISC 任务表。
- 将当前同步 HTTP 操作改为“持久化命令后返回 `202 Accepted` + 定时消费”。
- 为该业务来源的 ISC 任务补齐来源、批次和明细关联，按“同一人员 + 同一权限策略 + 同一设备”的意图键接管旧的**权限下发/更新**任务。

### 1.3 明确不做

- 不把 `SmtIscDeviceTaskServiceImpl.saveTask` 的全局去重语义改成“所有来源都自动替换”。该入口被员工资料、人脸、访客、入厂等大量路径复用，直接修改会错误取消不相关的删除或更新任务。
- 不增加“重置失败人员”前端按钮；管理员仍使用现有“手动下发”入口。
- 不靠把 Axios 超时从 30 秒调大解决问题。命令受理本身必须是常数时间。

## 2. 已核实的现状与问题

| 事实 | 代码证据 | 影响 |
| --- | --- | --- |
| 前端所有 Axios 请求默认 30 秒超时，手动下发没有局部覆盖。 | `smart-ui/src/router/axios.js`、`smart-ui/src/views/platform/security_area/xc_guard_apply/_service.js` | 大批量时浏览器报超时，用户不知道服务端是否已继续。 |
| `GET /security/auth/apply/down/{id}` 在 HTTP 请求内同步遍历全部待处理明细。 | `SmtSecurityAuthApplyController`、`SmtSecurityAuthApplyServiceImpl.triggerDownDevice`、`SmtSecurityTaskDetailsServiceImpl.downDevice` | 人数、权限和设备数相乘时超过 30 秒；事务和连接占用时间也随之增加。 |
| 明细抢占仅防止同一明细重复进入 `updatePersonCard`，不能解决旧 ISC 任务阻塞新的正常下发。 | `SmtSecurityTaskDetailsServiceImpl.down` | 重复点击可能不重复创建明细，但旧任务仍可长期停在执行态。 |
| ISC 保存入口仅按相同 action、image、serviceType 等判断，`INIT` / `DOING` 任务在离线取消月数内都会阻塞。 | `SmtIscDeviceTaskServiceImpl.checkTaskExists` | 人脸变化、动作变化时可能并存；完全相同任务又会被旧任务长期阻塞。 |
| ISC 调度每分钟从 `SMT_ISC_DEVICE_TASK` 取任务，提交 ISC 后置为 `DOING` 并保存 `ISC_TASK_ID`，随后轮询进度。 | `ISCDeviceTimerTask.deviceTaskDownCard`、`ISCDeviceTaskServiceImpl.downAccess`、`processParkAuthConfig` | 本系统必须让旧任务退出本地调度和当前批次聚合。 |
| 业务确认：ISC 接收新的权限下发后会以新权限覆盖此前权限。 | 旅途确认（2026-07-16） | 不调用 ISC 任务取消接口；对已提交旧任务只做本地接管，最新批次继续下发。 |

## 3. 目标行为

### 3.1 管理员操作

1. 管理员点击现有“手动下发”。前端发 `POST /security/auth/apply/{id}/dispatch`。
2. 服务端完成 OA 与权限校验、创建批次、标记待处理明细、接管旧任务后立即返回 `202`：`batchId`、总人数、已接管数。
3. 前端提示“已受理，正在下发”，刷新列表/详情轮询该 `batchId` 的进度；不再把“HTTP 已返回”显示成“下发成功”。
4. 后台每 30 秒分批生成 ISC 任务；ISC 的原每分钟下发与进度轮询继续执行。
5. 同一申请再次下发时，新批次为最新批次。旧批次尚未提交 ISC 的任务被取消；新批次只处理失败、取消、离线或超时的人员，不重发已确认成功的人员。

### 3.2 接管规则（必须在代码和测试中固定）

| 旧 `SMT_ISC_DEVICE_TASK` 状态 | 条件 | 新批次动作 |
| --- | --- | --- |
| `INIT` / `DEVICE_OFFLINE` | 同一安全权限意图键，且仍是旧批次 | 条件更新为 `CANCEL`，写入“被批次 X 接管”，随后创建新任务。 |
| `DOING` 且 `ISC_TASK_ID` 为空 | 超过短暂提交保护窗口，且 CAS 仍命中旧批次 | 条件更新为 `CANCEL`，随后创建新任务。 |
| `DOING` 且 `ISC_TASK_ID` 非空 | ISC 已经接收旧下发 | 本地条件更新为 `CANCEL` 并停止追踪；直接创建新任务。ISC 以新下发覆盖旧权限，不调用外部取消接口。 |

安全边界：`CANCEL` 仅代表本系统不再调度、不再聚合该旧任务，并不尝试撤销 ISC 已接收的任务。根据已确认的 ISC 覆盖语义，最新批次重新下发才是最终权限状态；旧任务的后续结果必须按批次隔离，不能回写覆盖新批次。

## 4. 数据模型与接口

### 4.1 复用现有任务表的批次字段

不新增 `SMT_SECURITY_AUTH_DISPATCH_BATCH` 控制表。一次管理员点击的持久化命令由“申请单当前批次 + 保密区人员明细批次”表达；真正的设备下发、接管历史和最终状态全部落在 `SMT_ISC_DEVICE_TASK`。

新增/确认以下字段和索引：

- `SMT_SECURITY_AUTH_APPLY.CURRENT_DISPATCH_BATCH_ID`：申请单当前有效批次，用于并发序列化与 UI 摘要。
- `SMT_SECURITY_TASK_DETAILS.DISPATCH_BATCH_ID`：明细归属的最新批次，也是 202 命令的持久化队列标记；索引 `(APPLY_ID, DISPATCH_BATCH_ID, STATUS)`。
- `SMT_ISC_DEVICE_TASK.SOURCE_TYPE`、`SOURCE_ID`、`SOURCE_DETAIL_ID`、`INTENT_KEY`：避免复用现有 `APPLY_ID`。`APPLY_ID` 已被入厂申请聚合使用，保密区任务写进去会因 ID 碰撞错误回写入厂申请。
- 复用已有 `SMT_ISC_DEVICE_TASK.BATCH_ID`，但迁移先检查 `2026-07-01-isc-batch-model.sql` 是否已执行；新增索引 `(SOURCE_TYPE, SOURCE_ID, BATCH_ID, STATUS)` 和 `(SOURCE_TYPE, INTENT_KEY, STATUS)`。

`INTENT_KEY` 固定为 `SECURITY_AUTH:{staffId}:{authId}:{deviceCode}`。它只用于安全权限下发/更新任务的接管，不触碰其他来源任务。

### 4.2 新接口

| 接口 | 含义 |
| --- | --- |
| `POST /security/auth/apply/{id}/dispatch` | 管理端下发命令；成功固定返回 HTTP 202 与 `SecurityDispatchAcceptedVO`。保留原 GET 端点一个发布周期，内部改为委托 POST，并在前端切换后删除。 |
| `GET /security/auth/apply/{id}/dispatch/{batchId}` | 由当前批次的保密区人员明细和 `SMT_ISC_DEVICE_TASK` 聚合进度，供详情页轮询。 |
| `POST /security/auth/apply/dispatch/process`（`@Inner`） | 由 smart-schedule 调用，领取有限数量 `WAIT + DISPATCH_BATCH_ID` 明细并生成 ISC 任务。 |

三个接口都保留现有菜单权限边界：管理端命令使用 `platform_security_auth_down`；内部处理接口只允许 `FROM_IN`。

## 5. 实施任务

### Task 1：批次字段、迁移和领域对象

**文件：**

- 新增：`smart-module/database/manual/2026-07-16-security-auth-dispatch-batch.sql`
- 新增：对应 rollback SQL。
- 修改：`SmtSecurityAuthApply.java`、`SmtSecurityTaskDetails.java`、`SmtIscDeviceTask.java`、`DeviceTaskVO.java`
- 修改：`SmtIscDeviceTaskMapper.java`、`SmtIscDeviceTaskMapper.xml`

**实现：**

1. 迁移使用 Oracle 幂等 PL/SQL 块：补列、建索引、中文注释；先校验既有 `BATCH_ID` 列而不是假设所有环境已执行 7 月 1 日脚本。
2. 对申请单行使用 `SELECT ... FOR UPDATE`（或等价的条件更新）串行化同一 `applyId` 的两个点击：生成新 batchId，更新 `CURRENT_DISPATCH_BATCH_ID`，并将可重试明细的 `DISPATCH_BATCH_ID` 改为新批次。
3. 将首次待下发的 WAIT 明细，以及可重试的明细（FAIL、CANCEL、DEVICE_OFFLINE、已判终态失败、已过保护期的孤儿 IN_WORK）绑到新 batch 并设为 WAIT；已成功明细不重发。
4. 任务创建时传入来源/批次/明细/意图键；不使用 `APPLY_ID`，防止污染入厂申请聚合。

**边界：**不在安全申请明细新增 `CANCEL`、`DEVICE_OFFLINE` 等镜像状态，ISC 的真实状态以 `SMT_ISC_DEVICE_TASK.STATUS` 为准；历史任务不回填 `SOURCE_DETAIL_ID`。重新绑批的事务和候选判定由任务 2、3 结合现有明细状态与新来源字段完成。

**测试：**并发两个命令只能留下一个 current batch；新批次不包含已成功人员；迁移重复执行不报错。

### Task 2：把同步下发改为 202 命令 + 后台分批消费

**文件：**

- 修改：`SmtSecurityAuthApplyController.java`、`SmtSecurityAuthApplyService.java`、`SmtSecurityAuthApplyServiceImpl.java`
- 修改：`SmtSecurityTaskDetailsService.java`、`SmtSecurityTaskDetailsServiceImpl.java`
- 新增：`SecurityDispatchAcceptedVO`、`SecurityDispatchProgressVO`
- 修改：`RemoteSecurityAuthService.java`
- 修改：`smart-schedule/.../PlatformTimerTask.java`、`TaskJob.java`、`TimerTaskEnum.java`

**实现：**

1. 命令接口只做 OA 已通过校验、生成 batchId、明细重绑和旧 DB 任务接管；不得调用 `updatePersonCard`。明细的 `WAIT + DISPATCH_BATCH_ID` 即为已持久化的待执行命令。
2. 返回码为 HTTP 202，不以 `Boolean` 表示“设备已下发成功”。受理 VO 必须给出 batchId、受理人数和接管数。
3. 新建每 30 秒执行的 schedule：分布式锁 + Nacos 开关 + Feign `@Inner` 调用；每轮按申请单、batchId 和人员上限消费 `SmtSecurityTaskDetails`，避免一张大申请占满工作线程。
4. 明细领取改为 `id + status + dispatchBatchId` 条件更新；旧批次 worker 即使拿到旧对象，也无法再创建新任务。
5. 将当前 `triggerDownDevice` 中“明细为空也标主表已下发”的错误语义改掉。批次开始显示 `IN_WORK`；当前批次全部成功才写 `SUCCESS`，存在任一失败则写 `FAIL`，通过成功/失败数量区分部分失败，不新增不存在的状态枚举。

**测试：**控制器 MockMvc 断言 202；大于单轮上限时命令仍快速返回；同一批次两次 worker 调用不会重复领取；旧 batch worker 不能创建任务。

### Task 3：ISC 任务接管、调度围栏和状态聚合

**文件：**

- 修改：`SmtIscDeviceTaskService.java`、`SmtIscDeviceTaskServiceImpl.java`
- 修改：`SmtIscDeviceTaskMapper.java`、`SmtIscDeviceTaskMapper.xml`
- 修改：`SmtStaffService.java`、`SmtStaffServiceImpl.java`
- 修改：`ISCDeviceTaskServiceImpl.java`
- 新增：`SecurityAuthDispatchContext`（只供安全权限路径传递来源、批次和意图键）。

**实现：**

1. 新增 `updatePersonCardForSecurityDispatch(context)` 专用入口，内部复用当前任务生成代码；不得给通用 `updatePersonCard` 增加“默认替换”行为。
2. Mapper 用意图键查询旧安全权限任务，并执行带状态、旧 batch 和更新时间条件的 CAS 取消；备注写“被批次 {newBatchId} 接管”，保留原 `ISC_TASK_ID` 作为审计证据。对于已提交 ISC 的 `DOING` 任务不调用 ISC 取消接口，直接创建新任务，由 ISC 的新权限覆盖旧权限。
3. `getCardDown` 及其他实际提交 ISC 的查询增加围栏：`SOURCE_TYPE=SECURITY_AUTH` 的任务只有 `BATCH_ID = SMT_SECURITY_AUTH_APPLY.CURRENT_DISPATCH_BATCH_ID` 才可提交 ISC。被替换的 INIT/离线任务即使遗漏一次取消，也绝不能被调度取走。
4. 旧批次的任何 ISC 轮询结果只能更新其本身任务，不能聚合或回写当前批次；当前批次只统计 `BATCH_ID = CURRENT_DISPATCH_BATCH_ID` 的任务。
5. 用 `SOURCE_DETAIL_ID` 聚合一个人员的全部设备任务：全成功才 SUCCESS；任一 FAIL/CANCEL/EXPIRED 才 FAIL；其余保持 IN_WORK。批次聚合再回写申请单和进度接口，替换当前只查询 `SMT_DEVICE_TASK`、看不到 ISC 任务的 `syncTaskStatus` 实现。

**测试：**

- INIT/离线旧任务被条件取消并创建新任务；被其他线程更新后 CAS 不覆盖。
- 旧 batch INIT 任务被调度查询过滤。
- 旧 DOING 且无 ISC taskId 在保护期内不可取消，过期后可安全接管。
- ISC 已接受的旧任务被本地接管后不调用外部取消接口，新批次仍创建任务；旧任务的迟到结果不得影响新批次状态。
- 多设备人员只有全部成功才计为成功；部分失败必须准确显示原因。

### Task 4：前端状态与运维闭环

**文件：**

- 修改：`smart-ui/src/views/platform/security_area/xc_guard_apply/_service.js`
- 修改：`smart-ui/src/views/platform/security_area/xc_guard_apply/index.vue`
- 修改：`smart-ui/src/views/platform/security_area/xc_guard_apply/detail.vue`
- 修改：`smart-ui/src/router/axios.js`（只保留 30 秒默认值，不调大）
- 新增：`docs/superpowers/runbooks/security-auth-dispatch-runbook.md`

**实现：**

1. 前端改调 POST，点击后禁用当前行直到收到 202；提示“已受理，正在下发（批次 X）”，随后刷新和轮询进度。
2. 列表筛选补齐待下发、下发中、成功、部分失败/失败；详情显示当前批次、待处理、成功、失败、取消和失败原因。
3. 不新增“重置失败人员”按钮。管理员再次点同一“手动下发”就是正常接管动作。
4. runbook 提供批次、明细、ISC 任务三张表关联查询，包含“旧任务被新批次接管”“ISC 迟到结果隔离”“设备离线重发”的值班处置。

**测试：**前端单测覆盖 202、重复点击禁用、轮询停止条件；`pnpm lint` 与 `pnpm test` 通过。

## 6. 验证和上线顺序

1. 先运行数据库迁移的预检 SQL，确认 `BATCH_ID`、新字段、索引均存在；保留 rollback，但上线后不得在仍有批次运行时执行 rollback。
2. 先发布 `smart-platform`（模型、命令、内部消费端）与 `smart-schedule`（默认关闭的新开关）；再发布 `smart-ui`。
3. 打开调度开关前，在测试环境演练：正常 202、重复点击、设备离线、ISC 查询失败、ISC 长时间执行、部分人员照片异常。
4. 生产灰度一张小申请，核对批次记录、`SMT_ISC_DEVICE_TASK` 来源字段、ISC 新权限覆盖结果和 UI 聚合一致，再逐步放量。

## 7. 完成标准

- 大批量点击不再因为 HTTP 超时被前端判为“任务失败”；命令在短时间内返回 202。
- 管理员重复走原手动下发流程时，旧本地任务自动标记为被接管，新批次生成新任务，无需 DB 人工清理。
- 已被 ISC 接收的旧任务不调用 ISC 取消接口；新任务下发后由 ISC 的覆盖语义保证最终权限，旧任务迟到结果不会污染当前批次。
- UI 的成功/失败数字由 `SMT_ISC_DEVICE_TASK` 实际终态聚合，而非 HTTP 返回或错误的非 ISC 任务表推断。
- 全部新增路径有单元测试，受影响模块 Maven 测试、smart-ui lint/test/build 均通过。
