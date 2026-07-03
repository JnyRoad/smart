# 入厂申请照片拉取——上线 Checklist（Task 10）

> **背景**：本清单是《入厂申请照片拉取与下发状态回写设计》
> （`docs/superpowers/specs/2026-07-01-admittance-photo-pull-and-status-design.md`）
> §3.5「上线顺序（防照片断供窗口）」与 §6「风险」的落地展开，覆盖四个上线步骤
> 的精确命令/操作、预期结果、失败排查，以及回退手段。执行者具备生产 Nacos、
> 数据库、许昌现场机器的操作权限；本清单不产出"已执行通过"的结果，供具备权限者
> 直接照抄执行。
>
> **前置代码事实**（写清单前已核实，避免凭空编造）：
> - 推送开关：`admittance.photo-push-enabled`（`SmtAdmittanceApplyServiceImpl.java:185`
>   `@Value("${admittance.photo-push-enabled:true}")`，默认 `true`）；已在
>   `docker/nacos/config/dev/smart-platform.yml` 的 `admittance:` 节下加入
>   `photo-push-enabled: true` 子键（dev 环境用，生产 Nacos 由运维按本清单第 2 步同步）。
> - `spring.admittance.save-path`（同文件 `save-path: D:/visitor/`）自 Task 3 起已成死代码
>   （推送 `filePath` 已改传相对文件名，不再依赖该配置拼绝对路径），**本次先不删**，
>   待第 4 步推送整体退役时随配置一并清理。
> - 照片开放接口最终路由（Task 8 修正版，`AdmittancePhotoOpenController.java`
>   `@RequestMapping("/open/admittance/photo")`，经网关 `/platform` 前缀后为）：
>   - `GET /platform/open/admittance/photo/pending`
>   - `GET /platform/open/admittance/photo/download/{photoId}`
> - FileReceiver 配置键（`smart-module/FileReceiver/src/main/resources/application.properties`
>   与 `smart-module/FileReceiver/README.md`）：`file-receiver.pull.enabled`、
>   `file-receiver.pull.server-url`、`file-receiver.pull.app-id`、`file-receiver.pull.app-secret`、
>   `file-receiver.pull.interval-seconds`（默认 30 秒）、`file-receiver.photo-dir`
>   （默认 `D:/visitor`，与打印页面 b-PAC 硬约定一致，**不可改**）、
>   `file-receiver.cleanup.retention-days`（默认 7 天，`0` 关闭清理）。
> - 发布产物固定为 `smart-module/FileReceiver/build/file.jar`（`mvn clean package -DskipTests`
>   在 `smart-module/FileReceiver/` 目录下产出）。
> - 涉及 DDL：`smart-module/database/manual/2026-07-01-isc-batch-model.sql`
>   （给 `SMT_ISC_DEVICE_TASK` 加 `APPLY_ID`，给相关表加批次追踪字段与索引）及其回滚脚本
>   `2026-07-01-isc-batch-model-rollback.sql`；两者均为 PL/SQL 匿名块，**整段执行**，
>   不可按分号逐句跑（Oracle 低版本不支持 `IF NOT EXISTS`，脚本内置存在性判断保证幂等）。
> - 鉴权前置：`file-receiver-xc` 客户端注册脚本
>   `smart-module/database/manual/2026-07-01-register-file-receiver-app.sql`
>   内置 `client_secret` 为占位符明文 `CHANGE-ME-ON-DEPLOY`，**不可直接用于鉴权**，
>   必须在管理页「应用管理」（`/admin/client`）对 `file-receiver-xc` 走一次
>   「重置 App Secret」拿到正式 `{bcrypt}` 编码凭证——这一步与本清单第 1 步共用同一批
>   数据库前置动作，详细步骤见
>   `docs/superpowers/plans/2026-07-01-open-api-auth-regression-checklist.md`
>   第 1、3 类（换 token）、第 6.4 类（重置弹窗）。
> - 观察期日志关键字来源（真实代码位置，非臆测）：
>   - FileReceiver 拉取失败：`PhotoPullTask.java` `log.error("下载照片失败，photoId={}", photoId, e)`；
>     单张缺图跳过：`log.warn("照片不存在，跳过：photoId={}", photoId)`；
>     清单拉取整体失败：`PhotoCleanupTask.java` `log.warn("拉取待处理清单失败，跳过本轮照片清理", e)`。
>   - **已知观察盲点**：`PhotoPullTask` 每轮成功时**没有** INFO 级"成功"日志（只有失败/缺图才打日志），
>     因此"拉取轮成功"无法直接搜日志关键字确认，只能用「本节第 4 步验证方法」里给出的替代方案
>     （数落盘文件数 + 确认无 ERROR/WARN 密集刷屏）间接判断，见下方第 4 步。
>   - smart-schedule 聚合回写：`AdmittanceDispatchAggregator.java`
>     `log.warn("ISC任务聚合：cardNo[{}]无法解析为fellowId，跳过该任务", ...)`（数据质量问题，非阻断）；
>     `log.warn("ISC任务聚合回写device_status未生效...判定为写冲突，将重试）"`（写冲突重试，
>     持续大量出现需关注，偶发正常）。
> - 与鉴权回归清单的衔接：`docs/superpowers/plans/2026-07-01-open-api-auth-regression-checklist.md`
>   第 3 类（换 token）、第 4 类（开放端点裁决矩阵）已写好通用步骤，唯独 `<OPEN_ENDPOINT>`
>   占位符和 scope 值需要替换成本清单确认的真实路径——本清单第 5 步给出替换后的具体命令，
>   不重复誊抄该清单全文。

---

## 第 1 步：先合并鉴权框架 + 注册 `file-receiver-xc`（重置正式凭证）

- **前置条件**：《开放 API 鉴权框架设计》配套实施计划（Task 1-6）已全部合并到待发布分支，
  且其 DDL（`2026-07-01-oauth-client-secret-prefix.sql`）与代码在同一停机窗口内执行——
  这是鉴权清单已写明的硬约束，本步骤不重复展开，直接引用：
  执行 `docs/superpowers/plans/2026-07-01-open-api-auth-regression-checklist.md`
  「0. 执行前准备」「1. 数据库前置」全部小节（1.1 迁移脚本 + 1.1.1 清 Redis 缓存 +
  1.2 注册 `file-receiver-xc` + 1.3 菜单改名）。
- **本步骤新增动作（1.2 之后）**：
  1. 确认 1.2 脚本执行后 `sys_oauth_client_details` 已有 `file-receiver-xc` 行（脚本自带校验 SQL）。
  2. 管理员登录 smart-ui「应用管理」页（`/admin/client`），找到 `file-receiver-xc`，
     点击「重置 App Secret」，二次确认后**立即复制记录**弹窗展示的新明文
     （只展示一次，关闭后无法再查看）——这是本次上线唯一一次能拿到该密钥的机会。
  3. 用新 secret 走一次 client_credentials 换 token 验证（鉴权清单第 3 类原样复用）：
     ```bash
     curl -i -X POST '<GATEWAY>/auth/oauth/token' \
       -u 'file-receiver-xc:<新SECRET>' \
       -d 'grant_type=client_credentials'
     ```
- **预期结果**：HTTP 200，响应体 `scope` 为 `open:admittance:photo:read`，
  且含 `app_park_ids` 数组、值为许昌园区 ID（1.2 脚本填入的园区 ID）。
- **失败排查**：
  - `401 invalid_client`：secret 抄错或已再次被重置覆盖，回管理页重新走一次重置。
  - 响应体没有 `app_park_ids`：回 `sys_oauth_client_details.additional_information`
    核对园区 ID 是否为合法 JSON（鉴权清单 1.2 步骤已有详细排查路径）。
- **产出物妥善保管**：新 secret 需交接给第 3 步现场部署人员填入 FileReceiver 配置，
  **不要**通过明文邮件/IM 传递，走密钥管理工具或线下口头交接。

---

## 第 2 步：服务端上线（生产 Nacos 同步 + DDL 低峰执行 + 推拉并存）

### 2.1 Nacos 生产配置同步

- **前置条件**：本次改动已把 `admittance.photo-push-enabled: true` 加入
  `docker/nacos/config/dev/smart-platform.yml`（`admittance:` 节下）；生产 Nacos
  与本仓库 dev 配置文件不是自动同步关系，需运维手工比对。
- **步骤**：
  1. 登录生产 Nacos 控制台，定位 `smart-platform.yml` 配置（namespace/group 按实际生产环境）。
  2. 在 `admittance:` 节下新增子键：
     ```yaml
     admittance:
       photo-push-enabled: true
     ```
     若生产配置里 `admittance:` 节缩进/已有键顺序与 dev 不同，以生产现有格式为准，
     只需保证 `photo-push-enabled` 是 `admittance` 节的直接子键（对应
     `@Value("${admittance.photo-push-enabled:true}")` 的点分路径）。
  3. 发布配置，观察 smart-platform 服务是否收到 Nacos 配置刷新（若走
     `@RefreshScope`/`@Value` 静态注入需评估是否需要重启服务生效——
     `SmtAdmittanceApplyServiceImpl` 未见 `@RefreshScope` 注解，**保守按需要重启服务生效处理**，
     纳入本步骤发布窗口）。
- **预期结果**：`photo-push-enabled` 在生产 Nacos 配置中可查询到值为 `true`；
  服务重启/刷新后无启动异常。
- **失败排查**：若发布后服务无法拉取新配置，检查 Nacos 命名空间/分组/dataId 是否与
  smart-platform 服务实际引用的一致（对照该服务 `bootstrap.yml` 的 Nacos 配置项）。

### 2.2 DDL 低峰执行（含回滚脚本预演）

- **前置条件**：已确认业务低峰时段（建议许昌当地凌晨，访客申请量最低）；已有数据库 DBA 权限；
  已提前準备好回滚脚本随时可执行。
- **步骤**：
  1. 备份 `SMT_ISC_DEVICE_TASK`、`SMT_ADMITTANCE_APPLY` 两张表结构（`expdp`/等价工具导出 DDL，
     不要求全量数据备份，只需能在异常时快速核对变更前结构）。
  2. 整段执行 `smart-module/database/manual/2026-07-01-isc-batch-model.sql`
     （PL/SQL 匿名块，禁止用 SQL*Plus/SQLcl 按分号逐句跑）。
  3. 执行后核对新增列与索引确实存在：
     ```sql
     SELECT COLUMN_NAME FROM USER_TAB_COLUMNS
     WHERE TABLE_NAME = 'SMT_ISC_DEVICE_TASK' AND COLUMN_NAME = 'APPLY_ID';
     ```
     应返回 1 行。
  4. 重复执行同一脚本一次，验证幂等（不应报错、不应重复加列）。
- **预期结果**：脚本执行无 ORA 报错；新增字段/索引存在；重复执行安全。
- **失败排查**：
  - `ORA-01430`（列已存在）：说明脚本的存在性判断分支未生效，检查是否误用了旧版本脚本文件。
  - 执行报错中断：**立即**整段执行回滚脚本 `2026-07-01-isc-batch-model-rollback.sql`，
    确认数据库结构回到变更前状态后再排查根因，不要在半成功状态下重试正向脚本。

### 2.3 应用发布（照片接口 + 状态回写代码生效）

- **步骤**：
  1. 发布已合并 Task 1-9 全部代码的 smart-platform-biz、smart-schedule 新版本。
  2. 发布后立即验证照片接口可用（用第 1 步拿到的 client token）：
     ```bash
     curl -i -X GET '<GATEWAY>/platform/open/admittance/photo/pending' \
       -H 'Authorization: Bearer <CLIENT_TOKEN>'
     ```
- **预期结果**：HTTP 200，返回 `Result` 包装的 `photoId` 数组（可能为空数组，属正常）。
- **失败排查**：403 说明鉁权链路未生效，先按鉴权回归清单第 4 类逐条排查
  `OpenApiInterceptor` 是否正确注册；500 看 smart-platform 服务启动日志是否有
  Bean 装配失败（常见于 DDL 未先执行导致 MyBatis 字段映射报错——**若命中此情况，
  说明第 2.2 步 DDL 与本步骤应用发布的顺序被颠倒，需检查发布流程是否严格遵守
  "先 DDL 后代码"或"DDL 与代码同一窗口"的约束**）。
- **此阶段状态**：`photo-push-enabled=true`，推送与拉取（若 FileReceiver 新版本尚未部署）
  并不冲突——推送仍是唯一通路，直到第 3 步 FileReceiver 新版本上线。

---

## 第 3 步：FileReceiver 部署许昌 Windows 机

### 3.1 部署前核对（现役版本与配置）

- **步骤**：
  1. 登录许昌 Windows 机，核对当前运行中的 FileReceiver 版本
     （查看进程启动脚本/服务管理器里指向的 jar 路径与文件修改时间，或若有版本接口/日志启动
     banner 则直接读取；**没有独立版本号接口时，以 jar 文件哈希或修改时间 + 部署记录台账
     确认现役版本**，防止误判"已是新版本"而跳过部署）。
  2. 核对现役 `application.properties`（或环境变量）里 `file-receiver.upload-root`
     的当前配置值——该配置属于推送模式（已废弃分支），新版本仍保留该配置项以兼容
     `/file/upload` 接口过渡期共存，**部署新版本时不要删除或改动这个值**，避免推送模式
     过渡期功能被误伤。
  3. 核对现役 `file-receiver.photo-dir`（若配置过）是否为 `D:/visitor`，与打印页面
     b-PAC 硬约定路径一致——这是不可变更的强约束，若现役配置不是这个值，
     **停止部署，先查清楚为什么现网配置和硬约定不一致，不要凭经验假设"改成默认值就对"**。
- **预期结果**：明确记录现役 jar 版本标识（用于回退）；`upload-root`、`photo-dir`
  两项配置值已书面记录在本次部署工单里。

### 3.2 新版本部署

- **步骤**：
  1. 本地构建：`cd smart-module/FileReceiver && mvn clean package -DskipTests`，
     产物为 `build/file.jar`。
  2. 停止许昌机器上现役 FileReceiver 进程/服务。
  3. 备份现役 `build/file.jar`（改名保留，如 `file.jar.bak-2026-07-01`），
     供 3.1 记录的版本回退使用。
  4. 拷贝新 `file.jar` 到部署路径，配置以下拉取模式必需项
     （通过 `application.properties` 或对应环境变量注入）：
     ```properties
     file-receiver.pull.enabled=true
     file-receiver.pull.server-url=<生产网关地址，如 http://smart-gateway-prod:9990>
     file-receiver.pull.app-id=file-receiver-xc
     file-receiver.pull.app-secret=<第1步重置拿到的正式secret>
     file-receiver.pull.interval-seconds=30
     file-receiver.photo-dir=D:/visitor
     file-receiver.cleanup.retention-days=7
     ```
     `file-receiver.upload-root` 保持 3.1 记录的现役值不变。
  5. 启动新进程/服务。
- **预期结果**：进程正常启动，无端口冲突/配置缺失异常；日志中无
  `OpenApiTokenClient` 相关的鉴权失败 ERROR。
- **失败排查**：
  - 启动即报 `app-secret` 为空或鉴权 401：核对 3.2 步骤 4 填入的 secret 与第 1 步重置结果一致
    （常见错误：secret 复制时带了首尾空格/换行）。
  - `server-url` 连不通：核对许昌机器到生产网关的网络策略（防火墙/代理），
    对照旧推送模式是否本身就用同一条链路（若旧模式也不通，是网络基础设施问题，
    不属于本次代码变更范围）。

### 3.3 首轮拉取验证

- **步骤**：
  1. 部署后等待至少 1 个 `interval-seconds`（默认 30 秒）。
  2. 观察 `D:\visitor` 目录，确认是否有新照片文件按 `{photoId}.png` 命名落盘
     （对照第 2.3 步验证过的 `pending` 接口返回的 photoId 列表核对文件是否一一对应）。
  3. 查看应用日志，确认没有出现 `PhotoPullTask` 的 `log.error("下载照片失败..."）`
     或 `PhotoCleanupTask` 的 `log.warn("拉取待处理清单失败..."）`。
- **预期结果**：目录下出现与 `pending` 清单对应的照片文件；日志无上述 ERROR/WARN。
- **失败排查**：若目录始终无新文件但也无报错日志，检查 `pending` 接口当前返回是否本来就是
  空数组（无待处理照片是正常情况，非故障）；若有 WARN「照片不存在，跳过」，
  是数据质量问题（历史存量数据 photoId 缺图），不阻断上线，按 WARN 频率记录待观察。

---

## 第 4 步：观察 1-2 天日志后关闭推送开关

### 4.1 观察期检查清单（每日至少 1 次，建议早晚各 1 次）

- **步骤**：
  1. 现场核对：随机抽 2-3 张近期审批通过的入厂申请，确认 `D:\visitor` 下对应
     photoId 的照片文件存在且可正常打印（走一次真实 b-PAC 打印验证）。
  2. FileReceiver 侧日志巡检关键字（无独立成功日志，用以下组合判断"拉取轮工作正常"）：
     - **应当没有**大量出现：`log.error("下载照片失败`（单张失败可接受，持续同一 photoId
       反复失败需人工介入核对该照片在服务端是否确实存在）；
     - **应当没有**持续出现：`log.warn("拉取待处理清单失败`（偶发 1-2 次可能是网络抖动，
       连续多轮出现说明服务端接口或网络链路有问题）；
     - `log.warn("照片不存在，跳过`（缺图 WARN）频率是否与拔号前一致，无明显新增。
  3. smart-schedule 侧日志巡检关键字：
     - `AdmittanceDispatchAggregator` 的 `log.warn("ISC任务聚合：cardNo[...]无法解析为fellowId`
       ——偶发可接受（数据质量问题），大量新增需排查上游卡号数据源；
     - `log.warn("ISC任务聚合回写device_status未生效...判定为写冲突，将重试）"`
       ——偶发重试正常，若同一 apply_id 反复出现（结合日志里打印的 appId）说明该单可能
       永久卡在写冲突，需人工核实设备任务终态数据。
  4. 管理后台/H5 页面走查：抽查若干最近审批通过的申请单，`deviceStatus` 显示是否与
     ISC 实际执行结果吻合（不再出现"网络超时误报下发失败"的历史问题）。
- **观察时长**：至少覆盖 1-2 个完整工作日（含至少一次夜间/低峰时段），
  确保覆盖不同时段的申请量与网络状况。

### 4.2 关闭推送开关

- **前置条件**：4.1 观察期未发现阻断级问题（拉取持续失败、聚合回写持续写冲突未恢复、
  打印验证失败）。
- **步骤**：
  1. 登录生产 Nacos，把 `smart-platform.yml` 里 `admittance.photo-push-enabled` 改为 `false`。
  2. 发布配置，视 2.1 步骤同样的刷新/重启方式使其生效。
  3. 验证：审批通过一条新的入厂申请，确认 `SmtAdmittanceApplyServiceImpl` 不再触发
     推送调用（无推送相关日志/无对许昌旧推送接口 `/file/upload` 的出站请求），
     但 30 秒内 FileReceiver 仍能正常拉取到该单照片、正常打印。
- **预期结果**：推送功能停用，拉取功能独立承担全部照片分发，页面状态显示正常。
- **失败排查**：若关闭推送后发现拉取模式有未观察到的边界问题（如清单接口在推送关闭后
  行为变化——理论上不应该，`pending` 接口逻辑与推送开关无关，若确实出现，说明二者存在
  未预期的耦合，需立即按下方回退手段先恢复推送）。

### 4.3 后续清理（推送整体退役，非本次上线必须项，留待下个迭代）

- 待观察期稳定运行（建议再累计运行 1-2 周无回退）后，可考虑：
  - 从 `SmtAdmittanceApplyServiceImpl` 删除推送调用代码路径；
  - 从 Nacos 配置删除 `admittance.photo-push-enabled`、`spring.admittance.save-path`
    （即本次注释标注的死代码配置）；
  - 从 FileReceiver 删除 `/file/upload` 推送接口与 `file-receiver.upload-root` 配置。
  - 此项不在本次上线范围内，仅记录以便下个迭代跟踪，不建议在观察期未满时提前执行。

---

## 回退手段（任一步骤发现阻断级问题时）

| 场景 | 回退动作 |
|---|---|
| FileReceiver 新版本部署失败/拉取异常（第 3 步） | 保持/恢复 `admittance.photo-push-enabled=true`（若已提前关闭需改回 true 并发布），
维持旧推送行为；许昌机器还原 3.1 备份的旧版本 `file.jar.bak-*`，重启旧进程 |
| DDL 执行报错（第 2.2 步） | 立即整段执行 `2026-07-01-isc-batch-model-rollback.sql`，确认字段/索引已移除后再排查根因 |
| 观察期发现聚合回写持续故障（第 4 步观察期内） | 保持 `photo-push-enabled=true` 不关闭，问题解决前不进入 4.2；必要时联系
`AdmittanceDispatchAggregator` 相关开发排查写冲突重试为何未能自愈 |
| 关闭推送后发现拉取模式有未预期问题（4.2 之后） | 立即把 `admittance.photo-push-enabled` 改回 `true` 并发布，
恢复推送作为兜底，同时保留 FileReceiver 拉取并行运行（两者共存无害，互为幂等写同一目录） |
| 鉴权注册/换 token 环节异常（第 1 步） | 不影响存量功能（`file-receiver-xc` 是新增客户端），
可反复重试注册脚本（脚本幂等）与重置 secret，不构成对现网其他功能的风险 |

---

## 与鉴权回归清单的衔接（真实照片接口版）

> `docs/superpowers/plans/2026-07-01-open-api-auth-regression-checklist.md` 第 3、4 类写作时
> Task 8 真实接口尚未合并，用 `<OPEN_ENDPOINT>` 占位。本次 Task 8 已合并，执行该清单时
> 用以下真实值替换：

- `<OPEN_ENDPOINT>` 替换为 `/open/admittance/photo/pending`（清单接口）或
  `/open/admittance/photo/download/{photoId}`（下载接口，`{photoId}` 替换为第 2.3 步
  验证时拿到的一个真实 photoId）。
- scope 统一为 `open:admittance:photo:read`（无需替换，鉴权清单里已是此值）。
- 执行范围：该清单第 3 类（换 token）、第 4 类全部 6 条（4.1-4.6 裁决矩阵）、
  第 5 类（吊销验证，可直接用 `file-receiver-xc` 或另建一次性测试 client）均按清单原文
  执行，只替换上述两处占位符，其余步骤/预期结果/失败排查不变。
- 特别提醒：第 4 类矩阵测试会调用 `download/{photoId}`，若使用的 photoId 对应的照片
  已被业务标记为过期/图片不存在，接口会返回 404 而非 403/200，**不影响鉴权矩阵结论**
  （矩阵验证的是鉴权层裁决，404 发生在鉴权通过之后的业务层）；如需稳定复现 200，
  优先选一个刚审批通过、`pending` 清单里能查到的 photoId。
