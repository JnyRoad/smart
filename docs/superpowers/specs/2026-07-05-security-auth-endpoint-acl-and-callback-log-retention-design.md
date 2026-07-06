# 保密门禁端点访问控制 + OA 回调日志留存治理 设计

- 日期：2026-07-05
- 状态：已定稿（用户批准，2026-07-05）
- 来源：PR #118 / #119 评审遗留的两个安全后续项（均为既有问题，非新引入）
- 基线：main @ PR #120 合并后（`6a9564f2`）
- 关联文档：`docs/superpowers/specs/2026-07-04-oa-callback-compensation-design.md`、`docs/superpowers/runbooks/oa-callback-runbook.md`

## 1. 背景与问题

### 1.1 问题 ①：SmtSecurityAuthApplyController 两个端点缺访问控制

`smart-module/smart-platform/smart-platform-biz/.../controller/securityzone/SmtSecurityAuthApplyController.java`：

| 端点 | 用途 | 现状 | 实际调用方 |
|---|---|---|---|
| `GET /security/auth/apply/msg` | 下发提示信息推送 | 无任何注解，仅靠网关登录态 | 仅 smart-schedule `PlatformTimerTask` 经 Feign 调用（已传 `FROM_IN` 头） |
| `GET /security/auth/apply/down/{id}` | 保密区权限手动下发 | 无任何注解，仅靠网关登录态 | smart-ui 管理页 `xc_guard_apply/_service.js` 按钮直调 |

风险：任何登录用户（含普通员工 H5 账号）可直调 `/down/{id}` 手动下发保密区门禁权限（PR #119 已加 OA 状态校验，但"OA 已通过的单子由谁触发下发"仍不受控），或直调 `/msg` 触发消息推送。

### 1.2 问题 ②：smt_oa_callback_log.payload 明文 PII 无留存策略

PR #118 新增 `smt_oa_callback_log` 表，`payload` CLOB 列明文存储 OA 回调全报文（含姓名、工号等 PII）。原设计文档留存策略为「暂不自动清理（日均几十条），列入巡检观察」，需落地为明确的留存/清理策略与访问控制说明。

评审中额外发现：`OaCallbackDispatcher` 第 55 行 `log.info("收到OA审批消息：{}", payload)` 将含 PII 的完整报文打进 INFO 日志——日志系统可见面远大于数据库，必须一并脱敏。

## 2. 决策记录

| 决策点 | 结论 | 理由 |
|---|---|---|
| 分支基线 | 最新 main（#118/#119/#120 已合并） | 用户确认 |
| `/msg` 访问控制 | `@Inner` | 唯一调用方是内部定时任务，Feign 已传 `FROM_IN`，与同文件 `/oa/status/task` 对齐；@Inner 切面当前 AUDIT 灰度（PR #112 三态方案），只记审计不拦截，纳入体系等 ENFORCE 统一生效 |
| `/down/{id}` 访问控制 | `@PreAuthorize("@pms.hasPermission('platform_security_auth_down')")` | 被管理端 UI 直调不能用 @Inner；`@EnableSmartResourceServer` 已开 `prePostEnabled=true`，`@pms`（`PermissionService`）为简单 authorities 匹配，authorities 经 check-token 跨服务还原（upms `@PreAuthorize` 生产在用，跨服务机制已验证） |
| 权限码配置方式 | 管理后台菜单管理 UI 手工配置（按钮型菜单 + 角色绑定），不写 sys_menu INSERT 脚本 | sys_menu 表结构不在仓库内，盲写 SQL 有风险；配置步骤写入 runbook |
| payload 留存策略 | 90 天整行删除 | 与保密门禁对账回溯窗口（90 天）对齐；策略最简单、留存承诺最干净；被删行中若含未解决 partial 记 WARN（90 天无人重放视为放弃） |
| 清理任务实现 | smart-schedule 定时任务 → Feign（`FROM_IN`）→ platform `@Inner` 端点 | 项目既有定时任务模式（`TaskJob` Nacos 开关 + `TimerTaskEnum` 分布式锁），运维可见可控 |

## 3. 改动设计

### 3.1 端点访问控制（commit 1）

1. `SmtSecurityAuthApplyController.sendMessage()`（`/msg`）加 `@Inner`。
2. `SmtSecurityAuthApplyController.downDevice()`（`/down/{id}`）加 `@PreAuthorize("@pms.hasPermission('platform_security_auth_down')")`。
3. smart-ui `src/views/platform/security_area/xc_guard_apply/index.vue` 手动下发按钮增加 `permissions['platform_security_auth_down']` 显隐/禁用控制（沿用 `admin/role/index.vue` 的 `this.permissions['code']` 惯例）。
4. runbook 增加上线顺序硬约束：**先在菜单管理配置按钮权限码 `platform_security_auth_down` 并绑定相关角色 → 再发 platform 版本**；顺序反了管理员点按钮 403。

### 3.2 回调日志留存治理（commit 2）

1. `OaCallbackLogService` 新增 `cleanExpiredLogs()`：
   - 删除 `receive_time < sysdate - RETENTION_DAYS` 的所有行（`RETENTION_DAYS = 90` 具名常量）；
   - 删除前统计其中未解决 partial（`status=2 and resolved=0`）数量，>0 记 WARN 日志（含数量 + 最多前 10 个 request_id）；
   - 返回删除行数，任务日志记录。
2. `OAWorkflowController` 新增 `@Inner` 端点 `GET /oa/workflow/callback/log/clean`（GET 跟随项目定时任务端点惯例）。
3. platform-api 新建 Feign 客户端 `RemoteOaCallbackLogService`（现无 OA workflow 域 Feign 客户端），方法带 `@RequestHeader(SecurityConstants.FROM)`，调用方传 `FROM_IN`。
4. smart-schedule `PlatformTimerTask` 新增每日凌晨调度方法：Nacos `TaskJob` 新开关（默认关）→ `TimerTaskEnum` 新锁键防多实例并发 → Feign 调用。
5. `OaCallbackDispatcher` 第 55 行日志脱敏：只打 requestId + 报文长度，不打全量 payload（全文已落库，排查走 DB）。
6. runbook 新增「数据留存与访问控制」节：
   - 90 天整行删除策略及理由（对齐对账回溯窗口）；
   - payload 访问途径清单：仅 DB 直查（DBA 权限）+ `@Inner` 重放接口，无 UI 暴露；
   - 日志不打全量报文的约定；
   - 清理任务开关名、锁键、首次开启注意事项。

## 4. 测试

- `cleanExpiredLogs` 单测：正常删除、含未解决 partial 的 WARN 分支、无过期数据的空结果分支。
- 控制器注解断言：沿用 `OAWorkflowControllerTest` 反射校验注解的既有模式，断言 `/msg` 有 `@Inner`、`/down/{id}` 有 `@PreAuthorize` 且权限码正确、clean 端点有 `@Inner`。
- `OaCallbackDispatcher` 日志改动跑既有单测回归。
- 编译验证：`mvn -pl <受影响模块> -am package -DskipTests` + 受影响模块测试。

## 5. 风险与边界

- **`@PreAuthorize` 是 smart-module 业务模块第一例**：机制已被 upms 跨服务验证，主要风险在发版顺序（§3.1.4 硬约束）。若权限码未配置，所有人 403——快速失败可接受，不做静默放行兜底。
- **`@Inner` AUDIT 模式不实际拦截**：真正生效等 PR #112 检查单流程切 ENFORCE；本次把 `/msg` 与 clean 端点纳入体系即达标。
- **清理任务默认关**：Nacos 打开开关后生效；`smt_oa_callback_log` 是新表无存量，首次开启无一次性大删风险。
- **未解决 partial 被删**：90 天未处理的 partial 连同 payload 一起删除，重放能力随之丧失——这是留存承诺的有意取舍，WARN 日志 + 巡检 SQL（runbook 已有）保证 90 天内可见。
