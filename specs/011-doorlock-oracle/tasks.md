# Tasks: 裕慧家园门锁统一接入与 Oracle

**Input**: [spec.md](spec.md)、[plan.md](plan.md)、数据模型、迁移映射与 contracts。
**Status**: 后续实施任务，全部未执行；本轮交付文档，不将既有独立门锁代码的测试结果冒充本模块完成。

## 执行约定与路径

- 任务编号在本文件内唯一；`[P]` 仅代表在其显式前置满足、且分配文件无重叠后可并行，不可跳过前置。
- 所有路径相对于当前 Smart 检出根。列出的 Java/前端文件是未来创建或修改目标，不表示已存在；目录任务只限描述的领域文件，不可扩大到整包复制。
- 每项行为实现必须先执行对应测试并得到可解释失败，再作最小实现和回归。写在同一任务中的“先测试、再适配”也必须保留红绿证据。
- 本地/Mock/Oracle/真机/生产证据分开。新代码有中文职责、流程和失败边界注释。任务完成立即记录验证再勾选；不自动提交、推送、PR或部署。
- T002～T005 只阻塞真实 Schema/材料/发布工作；T002 与 T004 修改同一文件，必须串行或交由同一责任人。T006～T011、纯领域测试、合成迁移、UI Mock 可先做；共享文件不得由多个 Agent 无协调写入。
- 数据发布、真实迁移、设备动作和正式切换分别要求目标明确且授权充分；本文件不是生产执行许可。

## Phase 1：输入核验与模块准备

只读输入和骨架准备；尚不向真实环境写入。所有任务为后续实施待办，本轮没有勾选业务开发。

- [ ] T001 开发者重新核对 Smart/重建源码 HEAD、已有变更和最新图谱覆盖，在 `specs/011-doorlock-oracle/research.md` 记录漂移及具体复用清单；遇到变化先更新规格。（FR-022）
- [ ] T002 [P] 在 `specs/011-doorlock-oracle/oracle-baseline.md` 填写目标 Oracle 版本、Schema、字符集/时区、配额、当前对象/索引/约束及最小权限的脱敏只读证据；不执行 DDL。（FR-001、FR-022）
- [ ] T003 [P] 在 `specs/011-doorlock-oracle/source-inventory.md` 与 `specs/011-doorlock-oracle/migration-mapping.md` 补真实 MySQL 全对象、精确计数、字段与水位证据；解释源码 22 表之外的对象及无法匹配记录。（FR-015、FR-016）
- [ ] T004 在 `specs/011-doorlock-oracle/oracle-baseline.md` 增补历史保留、凭据加密/引用/轮换/重采决策的业务与安全批准、责任人及恢复边界；未批准材料不进入真实迁移。（FR-014、FR-015）
- [ ] T005 在 `smart-module/smart-lock/docs/superpowers/doorlock/oracle-release-manifest.md` 定版已批准的 DDL 发布载体、版本校验、备份与回退验收，不新增人工 SQL 脚本目录；同时将 `specs/011-doorlock-oracle/data-model.md` 候选字段转为经证据确认的物理设计。（FR-001、FR-022；依赖 T002、T003、T004）
- [ ] T006 在 `smart-module/smart-lock/docs/superpowers/doorlock/module-boundaries.md` 先定义 api/biz 依赖、无独立认证/无 MySQL 在线依赖的静态约束和后续断言；骨架前只做文档检查，不把 Maven 缺模块当业务红测。（FR-001、FR-002、FR-003）
- [ ] T007 建立 `smart-module/smart-lock/pom.xml`、`smart-module/smart-bridge-lock/pom.xml` 及 plan 指定的四个 api/biz POM，注册 `smart-module/pom.xml`，更新根 README 和模块说明；按 T006 静态约束检查依赖树并编译空骨架，不引入无调用方通用模块。（FR-001、FR-022；依赖 T006）
- [ ] T008 在 `smart-module/smart-lock/docs/superpowers/doorlock/local-test-resources.md` 明确本地 Oracle、端口、测试 Schema、设备模拟器的独占范围和启用方式；不自动启动共用数据库或网关。（FR-017、FR-022）
- [ ] T009 [P] 在 `smart-ui/docs/superpowers/doorlock/parity-acceptance.md` 冻结八组菜单、角色、筛选、字段、按钮和弹窗证据及安全差异；旧运行态缺失的项标未验证，不认作对版完成。（FR-012）

## Phase 2：共有契约、存储与访问基础

共用契约先完成；依赖数据库的项目有单独前置，纯 DTO、领域和页面 Mock 不被 T002～T005 阻塞。

- [ ] T010 [P] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/contract/LockEnvelopeContractTest.java`，覆盖事件 ID、字符串业务 ID、版本缺口、targetKind 条件字段/资产 expectedVersion、membership、幂等键冲突和响应状态分层，记录失败。（骨架前置 T007；FR-006、FR-007；TEST-003、TEST-004、TEST-008、TEST-029、TEST-030）
- [ ] T011 在 `smart-module/smart-lock/smart-lock-api/src/main/java/com/tce/smart/lock/api/contract/` 和 `smart-module/smart-bridge-lock/smart-bridge-lock-api/src/main/java/com/tce/smart/bridge/lock/api/contract/` 实现冻结的 DTO/枚举及校验，通过 T010；不得把 commandId 直接当 wireTaskId。（FR-006、FR-007；依赖 T007、T010）
- [ ] T012 [P] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/persistence/OracleSemanticsIntegrationTest.java`，使用真实本地 Oracle 验证 ID/空值/时间/LOB/稳定分页、唯一约束与并发条件更新；包括绑定事实和重算同事务、多设备父授权引用、scoped 幂等冲突、不可变授权快照、生命周期冲突留证及设备事件去重恢复。无库明确未执行，禁止伪通过。（FR-001、FR-022；TEST-003～TEST-004、TEST-007、TEST-024～TEST-026、TEST-029；运行依赖 T002、T005、T007、T008）
- [ ] T013 依照已批准发布载体建立本地测试对象，在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/persistence/`、`smart-module/smart-lock/smart-lock-biz/src/main/resources/mapper/` 实现 Oracle 映射并通过 T012；发布载体版本和结果回填 oracle-release-manifest，未授权不得作用于真实业务 Schema。（FR-001、FR-007；依赖 T005、T008、T012）
- [ ] T014 [P] 先写 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/lock/LockAccessScopeTest.java`，覆盖管理员资源码/园区、本人身份、内部伪造服务身份和 export 越权，并在 `smart/smart-gateway/src/test/java/com/tce/smart/gateway/LockRouteIsolationTest.java` 检查公开路径只到 platform、内部/服务发现直连路径对用户拒绝，记录失败。（骨架前置 T007；FR-002、FR-013、FR-020；TEST-015、TEST-017）
- [ ] T015 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/lock/LockAccessScope.java` 与 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/security/TrustedLockContext.java` 实现服务端身份派生和受信 scope 二次校验，复用现有认证/UPMS，不创建新登录；按 T014 的失败证据最小化调整当前网关路由/安全配置，禁止用户直达 lock/bridge 内部端点，再通过 T014；如涉及运行配置写入须另获授权。（FR-002、FR-003、FR-013；依赖 T011、T014）
- [ ] T016 [P] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/security/SensitiveMaterialTest.java`，覆盖引用访问权限、摘要不可泄密、错误/日志脱敏和密钥版本失效，记录失败。（骨架前置 T007；FR-014；TEST-021、TEST-024）
- [ ] T017 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/security/CredentialMaterialAccess.java` 实现批准的最小化密文引用、轮换和访问审计；先用合成材料通过 T016，真实材料仍依赖 T004 批准。（FR-014、FR-020；依赖 T015、T016）

## Phase 3：US1 可靠住宿闭环（P1，首个 MVP）

独立验收：合成人员入住、模拟设备确认、退宿撤权；回滚、重放、乱序、重启和迟到回执不丢事件、不恢复失效权限。

- [ ] T018 [P] [US1] 先写 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/lock/AccommodationOutboxTest.java`，覆盖正常提交、事务回滚、Outbox 失败和提交前不发送，同时先写同目录 `AccommodationOutboxOracleIntegrationTest.java`。前置未齐时只允许编写测试和运行纯单测；Oracle 集成部分必须等 T005/T008 的平台测试对象与资源获批、准备就绪后，使用真实本地 Oracle 和平台实际事务管理器验证提交/回滚、提交前不发送以及进程退出恢复并记录可解释失败。未执行 Oracle 部分不得勾选整个 T018；平台映射由后续 T026 实现，不依赖 T013 的 lock-biz 持久层。（骨架前置 T007；FR-004；TEST-001、TEST-002）
- [ ] T019 [P] [US1] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/lifecycle/LifecycleOrderingTest.java`，覆盖重复、缺口、快速退宿/再入住和同 membership 新授权版本，记录失败。（骨架前置 T007；FR-006、FR-009；TEST-003、TEST-004、TEST-006）
- [ ] T020 [P] [US1] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/grant/GrantCredentialProjectionTest.java`，覆盖同一父 grant/revision 下多设备多凭据全部确认、部分完成后重启、多授权共用凭据、最后资格撤销、槽位复用和旧 revision 迟到新增，记录失败。（骨架前置 T007；FR-007、FR-009；TEST-006、TEST-026、TEST-031）
- [ ] T021 [P] [US1] 先写 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/lock/AccommodationEntrancesTest.java`，逐项覆盖正常/同房换床/跨房调宿/自动分配/Excel/定时退宿/离职/床位直接删除/人员转正/绑定变化；增加住房范围分页期间入住/退宿、租约超时及快照失效，未封存不得执行授权。明确断言同房不重复发放、跨房旧房撤权确认后才发新房，记录失败。（骨架前置 T007；FR-005；TEST-005、TEST-007）
- [ ] T022 [P] [US1] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/command/CommandRecoveryTest.java`，覆盖抢占、取消、发送前再校验、有限重试、重启、超时未知和过期开门不重放，记录失败。（骨架前置 T007；FR-008、FR-009；TEST-009、TEST-010）
- [ ] T023 [P] [US1] 先写 `smart-module/smart-bridge-lock/smart-bridge-lock-biz/src/test/java/com/tce/smart/bridge/lock/protocol/LockProtocolCodecTest.java`，覆盖分帧、非法长度、指纹分包、B8 22/24-hex、乱序/重复包与线任务号边界，使用合成/批准脱敏样本记录失败。（骨架前置 T007；FR-010；TEST-008、TEST-011）
- [ ] T024 [P] [US1] 先写 `smart-module/smart-bridge-lock/smart-bridge-lock-biz/src/test/java/com/tce/smart/bridge/lock/session/SingleActiveSessionTest.java`，覆盖默认禁发、同锁多网关、重复连接、身份冲突、断连与租约失效，不用 DB 锁声称硬件 fencing，记录失败。（骨架前置 T007；FR-008、FR-018；TEST-009、TEST-012）
- [ ] T025 [P] [US1] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/receipt/ReceiptCorrelationTest.java`，区分 transport/device 结果，覆盖丢回执、迟到/重复/矛盾 ACK、未知 attempt 和重启重投；在同目录 `DeviceEventDeduplicationTest.java` 覆盖不同 eventId 的同设备事件去重、同键异载荷隔离和重启恢复，记录失败。（骨架前置 T007；FR-007、FR-008、FR-020；TEST-006、TEST-008、TEST-009、TEST-024）
- [ ] T026 [US1] 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/lock/AccommodationEventRecorder.java` 接入实际住宿提交事务与版本分配，并在平台 biz 的 `src/main/java/com/tce/smart/platform/lock/persistence/` 新建 AccommodationOutboxEntity/Mapper、在 `src/main/resources/mapper/AccommodationOutboxMapper.xml` 实现平台拥有的 DL_OUTBOX 与聚合版本存储；将其绑定实际住宿事务管理器，只写平台事实和 Outbox。按 T005 发布载体独立准备平台本地测试表对象，不依赖 lock-biz，必须通过 T018 的纯单测与真实 Oracle 事务测试。（FR-003、FR-004；依赖 T005、T008、T018）
- [ ] T027 [US1] 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/lock/LockOutboxPublisher.java` 实现事务外领取、超时、退避、失败持久化和重复投递；平台不得直接写门锁授权表，通过 T018、T019。（FR-004、FR-006、FR-008；依赖 T026、T019）
- [ ] T028 [US1] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/lifecycle/LifecycleEventConsumer.java` 实现 Inbox 去重、稳定人员版本游标、缺口核验和 membership 围栏；只有同 ID 同载荷重用原结果，新 ID 旧版本落本事件处置，同 ID 异载荷保留原 Inbox 并追加隔离审计和不可变证据引用。授权/命令与消费记录在锁域本地事务内提交，通过 T019 和 T012 的对应持久化场景。（FR-003、FR-006；依赖 T013、T019）
- [ ] T029 [US1] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/grant/GrantProjectionService.java` 实现版本化父授权、每设备目标与凭据引用关系；同一 grant/revision 的全部必需目标和凭据均确认才聚合为 ACTIVE，并维护共同引用撤权规则，通过 T020，不把先收到的任一 ACK 当整项授权完成。（FR-007、FR-009；依赖 T020、T028）
- [ ] T030 [US1] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/command/LockCommandService.java` 实现持久命令、不可变目标/授权快照、attempt、有效期、scoped 幂等及摘要冲突检测和撤权优先；支持一个请求多目标命令及完整结果集合，不以请求 ID 阻断第二个目标。重启/重试保留原快照并重新裁定当前权限，不存长期 token、不把摘要加入幂等唯一键。通过 T010、T022，不按旧状态整表创建在线命令。（FR-007、FR-008、FR-009；依赖 T010、T022、T029）
- [ ] T031 [US1] 在 `smart-module/smart-bridge-lock/smart-bridge-lock-biz/src/main/java/com/tce/smart/bridge/lock/protocol/` 实现最小编解码与指纹 ACK 适配，参考重建源码而非最早 jar，默认未验证 profile 禁止真实发送，通过 T023。（FR-010；依赖 T011、T023）
- [ ] T032 [US1] 在 `smart-module/smart-bridge-lock/smart-bridge-lock-biz/src/main/java/com/tce/smart/bridge/lock/session/SingleActiveGatewaySession.java` 实现单活会话、稳定网关身份、唯一选择和发送前租约校验，通过 T024；不新增多活集群。（FR-008、FR-018；依赖 T024、T031）
- [ ] T033 [US1] 在 `smart-module/smart-bridge-lock/smart-bridge-lock-biz/src/main/java/com/tce/smart/bridge/lock/command/BridgeCommandReceiver.java` 实现内部服务身份验证、持久尝试接收去重、wireTaskId 映射与单活发送；从首版即硬性默认禁发，模拟通道不能回落真实 socket，通过 T022～T024；出线未知进入核验，不靠换网关盲重发。（FR-007、FR-008、FR-010；依赖 T030、T032）
- [ ] T034 [US1] 在 `smart-module/smart-bridge-lock/smart-bridge-lock-biz/src/main/java/com/tce/smart/bridge/lock/receipt/ReceiptPublisher.java` 和 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/receipt/ReceiptConsumer.java` 实现回执先可靠记录再重投、attempt/设备/profile 匹配和矛盾隔离；在后者同目录 `DeviceEventConsumer.java` 持久接收 LC-EVT-002 的 eventId 别名、来源去重键与冲突证据，只更新通信/告警投影，不直接推进命令终态，通过 T025。（FR-007、FR-008、FR-020；依赖 T025、T033）
- [ ] T035 [US1] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/command/CommandRecoveryScheduler.java` 实现崩溃恢复、有限重试、期限终止和未知结果对账；状态读取能力未验证时转人工，不重放开门，通过 T022、T025。（FR-008、FR-020；依赖 T030、T034）
- [ ] T036 [US1] 改造 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDormitoryStaffServiceImpl.java` 的正常、批量、自动分配和定时退宿调用入口，统一使用事件记录器，保留同房换床与跨房先撤后发语义，通过 T021。（FR-005、FR-009；依赖 T021、T026、T029）
- [ ] T037 [US1] 收口 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtDormitoryBedServiceImpl.java`、`smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtLeaveHandoverServiceImpl.java` 和 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtLeaveHandoverMapper.xml` 的直接住宿删除路径，通过 T021，不遗留离职/床位旁路。（FR-005；依赖 T036）
- [ ] T038 [US1] 改造 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtStaffServiceImpl.java` 的身份转正及 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/asset/RoomBindingService.java` 的绑定变化事件；绑定事实、独立 bindingVersion 和本地已知 membership 重算工作项同事务持久化。分开范围收集/执行领取，持久化分页游标、分项进度、领取超时及有限重试；在平台 `lock/AccommodationScopeQueryService.java` 适配受信住房查询，必须取得稳定快照或覆盖全部相关住宿变更的单调水位并完成遍历校验，否则不封存范围、不发新授权。保留旧设备撤权快照，不让旧版本覆盖新绑定。通过 T021 和 T012 的相关 Oracle 场景。（FR-003、FR-005、FR-009；依赖 T011、T021、T026、T029）
- [ ] T039 [US1] 在 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/integration/AccommodationLockFlowTest.java` 汇总以上已先写的测试场景，完成本地 Oracle+模拟桥的完整闭环和故障注入；把真实执行结果写 `smart-module/smart-lock/docs/superpowers/doorlock/acceptance.md`，不得标成真机通过。（FR-004、FR-008、FR-022；TEST-001～TEST-010；依赖 T027～T038）

## Phase 4：US2 Web 管理完整对版（P1）

独立验收：两个园区、只读/管理角色、合成资产和模拟命令，逐项验收八组页面；真实硬件操作仍被能力门禁控制。

- [ ] T040 [P] [US2] 先写 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/lock/LockAdminApiContractTest.java`，覆盖八组资源的查询、写入、幂等、批量部分失败、导出范围和命令确认状态，记录失败。（骨架前置 T007；FR-002、FR-011、FR-012；TEST-013～TEST-016、TEST-029、TEST-030）
- [ ] T041 [P] [US2] 先写 `smart-ui/src/api/lock/admin-contract.test.js`，覆盖 API envelope、分页、筛选、权限错误、202 待确认与轮询终止，记录失败。（FR-012；TEST-013、TEST-016、TEST-028）
- [ ] T042 [P] [US2] 先写 `smart-ui/src/views/lock/__tests__/assets-parity.test.js`，覆盖 WEB-L-001～004 的字段/按钮/弹窗/校验/禁用与空态，记录失败。（FR-011、FR-012；TEST-013）
- [ ] T043 [P] [US2] 先写 `smart-ui/src/views/lock/__tests__/operations-parity.test.js`，覆盖 WEB-L-005～008 的授权、下发、日志、告警、部分失败和脱敏，记录失败。（FR-007、FR-012、FR-020；TEST-014、TEST-016）
- [ ] T044 [US2] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/asset/LockAssetService.java` 实现设备/网关/型号/绑定领域管理；区分启停、连接和物理状态，绑定变化通过已定义事件收敛；通过 T040。（FR-010、FR-011；依赖 T015、T038、T040）
- [ ] T045 [US2] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/credential/CredentialAdministrationService.java` 实现受控凭据登记、导入失败清单、人工授权/撤权/重新授权；不写第二套人员主库，通过 T020、T040。（FR-003、FR-009、FR-014；依赖 T017、T029、T040）
- [ ] T046 [US2] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/query/LockHistoryQueryService.java` 与 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/alert/LockAlertService.java` 实现记录分页、脱敏导出、告警处理和设置；接口可查迁移历史但历史不驱动命令，通过 T040。（FR-011、FR-012、FR-020；依赖 T013、T040）
- [ ] T047 [US2] 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/LockAdminController.java` 实现平台管理门面、参数关联和园区范围，将受信上下文传锁域，不公开 bridge；通过 T014、T040。（FR-002、FR-011、FR-012；依赖 T015、T044～T046）
- [ ] T048 [US2] 在 `smart-ui/src/api/lock/index.js` 实现统一管理请求和结果状态适配，通过 T041；页面不选旧/新服务 URL。（FR-012、FR-021；依赖 T041、T047）
- [ ] T049 [US2] 实现 `smart-ui/src/views/lock/list/index.vue` 和详情页，完成 WEB-L-001 列表/详情/配置/受控开门；通过 T042，不能用新大屏替代旧入口。（FR-012；依赖 T009、T042、T048）
- [ ] T050 [US2] 实现 `smart-ui/src/views/lock/devices/index.vue`，完成 WEB-L-002 编辑、启停/解绑的确认与能力受限说明，通过 T042。（FR-011、FR-012；依赖 T042、T048）
- [ ] T051 [US2] 实现 `smart-ui/src/views/lock/gateways/index.vue` 与 `smart-ui/src/views/lock/models/index.vue`，完成 WEB-L-003 网关/型号，改址不绕过切换门禁，通过 T042。（FR-010、FR-012；依赖 T042、T048）
- [ ] T052 [US2] 实现 `smart-ui/src/views/lock/credentials/index.vue`，完成 WEB-L-004 人员查询和凭据操作，复用平台主数据，只显示批准摘要，通过 T042。（FR-003、FR-012、FR-014；依赖 T042、T048）
- [ ] T053 [US2] 实现 `smart-ui/src/views/lock/grants/index.vue`，完成 WEB-L-005 授权、续期、撤权、重授及失败反馈，通过 T043。（FR-009、FR-012；依赖 T043、T048）
- [ ] T054 [US2] 实现 `smart-ui/src/views/lock/commands/index.vue`，完成 WEB-L-006 命令/尝试/回执明细与受控重试取消，通过 T043，不显示伪成功。（FR-007、FR-008、FR-012；依赖 T043、T048）
- [ ] T055 [US2] 实现 `smart-ui/src/views/lock/records/index.vue`，完成 WEB-L-007 开门/密码/通信记录及受审计导出，通过 T043，敏感原文不可导出。（FR-012、FR-014、FR-020；依赖 T043、T048）
- [ ] T056 [US2] 实现 `smart-ui/src/views/lock/alarms/index.vue` 与设置页，完成 WEB-L-008 告警处置、阈值/通知设置，通过 T043，处理状态不冒充设备恢复。（FR-011、FR-012、FR-020；依赖 T043、T048）
- [ ] T057 [US2] 在 `smart-ui/docs/superpowers/doorlock/permission-and-route-map.md` 记录 UPMS 菜单/资源码、组件路径和旧 URL 映射；依据 T040/T041 先补路由兼容断言，再适配现有动态菜单与重定向，真实 UPMS 写入另获授权。（FR-002、FR-012；TEST-028；依赖 T049～T056）
- [ ] T058 [US2] 运行 Web 单测、构建和浏览器逐项对版，在 `smart-ui/docs/superpowers/doorlock/parity-acceptance.md` 登记八组结果、截图引用与安全差异；未有运行态证据项不得勾选完成。（FR-012、FR-022；TEST-013～TEST-016；依赖 T057）

## Phase 5：US4 全历史迁移（P1）

独立验收：同一合成快照重复导入、中断续跑、变更源行、删除记录和异常人员；来源去向完整可查、设备发送为零。

- [ ] T059 [P] [US4] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/migration/LegacyMappingTest.java`，覆盖 22 源实体及新增实库对象、重复/空工号、错园区、旧keyId、投影非物理列和敏感材料处置，记录失败。（骨架前置 T007；FR-015、FR-016；TEST-020、TEST-021）
- [ ] T060 [P] [US4] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/migration/MigrationResumeTest.java`，覆盖同快照重跑、中断、源行 hash 变化/删除、无更新时间和稳定目标 ID 不重复，记录失败。（骨架前置 T007；FR-015、FR-017；TEST-020）
- [ ] T061 [P] [US4] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/migration/LegacyTaskQuarantineTest.java`，覆盖旧任务所有版本状态、不确定设备效果、零发送、归档历史查询和差异核验，记录失败。（骨架前置 T007；FR-007、FR-016、FR-017；TEST-020、TEST-021）
- [ ] T062 [US4] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/migration/LegacySnapshotReader.java` 实现受控导出输入读取、来源版本/清单/摘要校验和 dry-run；没有授权时仅合成数据，不把生产访问封装成默认启动动作，通过 T059。（FR-015、FR-022；依赖 T003、T059）
- [ ] T063 [US4] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/migration/LegacyIdentityResolver.java` 实现稳定来源键到平台/目标 ID 映射、歧义隔离与处置理由，通过 T059；不按姓名自动归并。（FR-003、FR-015、FR-016；依赖 T059、T062）
- [ ] T064 [US4] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/migration/LegacyRowTransformer.java` 按字段映射转换资产、关系、凭据元数据、设置和全历史记录，旧账号仅归档，敏感引用受 T004 控制，通过 T059、T061。（FR-001、FR-014、FR-015；依赖 T004、T059、T061、T063）
- [ ] T065 [US4] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/migration/MigrationLedgerService.java` 实现批次/快照、水位、稳定 ID map、版本行账本、断点续跑和同目标更新；禁止仅靠 rowhash 去重创建新目标，通过 T060。（FR-015、FR-017；依赖 T013、T060、T064）
- [ ] T066 [US4] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/migration/LegacyTaskClassifier.java` 实现旧未完成任务隔离、旧物理效果核验和只读历史；导入不激活 sender，通过 T061。（FR-007、FR-016、FR-017；依赖 T061、T065）
- [ ] T067 [US4] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/migration/MigrationReconciler.java` 实现源行处置覆盖率、按园区/表精确计数、引用完整性、摘要及抽样对照；无可靠增量水位采用最终冻结快照全量比对，通过 T059～T061。（FR-015、FR-016；依赖 T066）
- [ ] T068 [US4] 在 `smart-module/smart-lock/docs/superpowers/doorlock/migration-rehearsal.md` 登记隔离 Oracle 迁移两次及中断恢复结果，说明所有未解释差异、耗时、吞吐和最终窗口估算；不以 TABLE_ROWS 估计验收。（FR-001、FR-015、FR-017、FR-022；TEST-020、TEST-021、TEST-025；依赖 T067）

## Phase 6：US5 执行方隔离与切换工具（P1）

独立验收：隔离通信域内验证旧/新互斥、影子零发送、冻结处理和产生新物理状态后的回退；生产操作另获授权。

- [ ] T069 [P] [US5] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/cutover/ExecutionModeTest.java`，覆盖默认禁发、LEGACY_ONLY/SHADOW/CUTOVER_FREEZE/NEW_ONLY、越权改模式、旧连接/队列未清不得放行，记录失败。（骨架前置 T007；FR-017、FR-018、FR-021；TEST-012、TEST-022）
- [ ] T070 [P] [US5] 先写 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/lock/LegacyExecutorCompatibilityTest.java`，覆盖提前部署 Outbox 时只调用旧执行方、旧HTTP未知结果不盲重试、重复事件隔离和冻结时暂停/可靠排队，记录失败。（骨架前置 T007；FR-004、FR-019、FR-021；TEST-009、TEST-022、TEST-028）
- [ ] T071 [P] [US5] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/cutover/CutoverRollbackDecisionTest.java`，覆盖全域成员缺失、地址未回读、在途未知、最终快照差异、新物理变化和回退增量，记录失败。（骨架前置 T007；FR-018、FR-019；TEST-022、TEST-023）
- [ ] T072 [US5] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/cutover/ExecutionModeGuard.java` 与 `smart-module/smart-bridge-lock/smart-bridge-lock-biz/src/main/java/com/tce/smart/bridge/lock/command/DeviceSendGuard.java` 实现控制面/桥接双重禁发和全域切换证据校验，通过 T069；不设计逐锁/逐网关灰度。（FR-017、FR-018、FR-021；依赖 T033、T069）
- [ ] T073 [US5] 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/lock/LegacyLockExecutor.java` 实现有界过渡适配与冻结响应，通过 T070；仅在提前上线事件链时启用，默认仍由现有旧链运行到统一切换，不能双路调用。（FR-004、FR-019、FR-021；依赖 T027、T070、T072）
- [ ] T074 [US5] 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/cutover/CutoverReadinessService.java` 实现成员/快照/在途/差异/回读证据的只读核验与回退决策，通过 T071，不自动改网关地址或恢复数据库。（FR-018、FR-019、FR-022；依赖 T067、T071、T072）
- [ ] T075 [US5] 在 `smart-module/smart-bridge-lock/docs/superpowers/doorlock/hardware-profile-evidence.md` 按网关/锁型号/固件列密码、卡、指纹、删除、ACK、读取和重连用例，完成获授权的隔离真机验证；记录未知项，不用一台成功代表全域。（FR-010、FR-018、FR-022；TEST-008、TEST-011、TEST-012；依赖 T031～T035、T072）
- [ ] T076 [US5] 在 `smart-module/smart-lock/docs/superpowers/doorlock/cutover-runbook.md` 固化冻结、应急通行、停旧任务/TCP、最终迁移、全量改址回读、禁发核验、放行和回退增量逐步清单；每步有责任、证据、失败停止条件。（FR-018、FR-019；依赖 T068、T074、T075）
- [ ] T077 [US5] 在 `smart-module/smart-lock/docs/superpowers/doorlock/cutover-rehearsal.md` 记录隔离全流程及故障回退演练；先旧停新禁发，再新接管，验证业务排队恢复与锁内差异，不执行生产切换。（FR-017、FR-018、FR-019、FR-022；TEST-022、TEST-023；依赖 T073、T076）

## Phase 7：US3 H5 本人自助（P2）

独立验收：已入住、未入住、退宿、身份篡改、查看/修改密码、校验失败和退出清理。可与其他故事的非共享文件并行，接入后必须回归。

- [ ] T078 [P] [US3] 先写 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/lock/MyLockApiContractTest.java`，覆盖服务端本人身份、verificationRef主体/用途/membership/期限/单次消费、受控短时读取与密码修改幂等，以及单请求多设备子命令、部分确认和重投不扩展目标集合，记录失败。（骨架前置 T007；FR-013、FR-014；TEST-003、TEST-017、TEST-018、TEST-027）
- [ ] T079 [P] [US3] 先写 `smart-h5/src/features/dorm/lock-state.test.ts`，覆盖确认中、限制状态、错误映射、重复提交、身份/住宿变化、登出缓存清除，记录失败。（FR-007、FR-013、FR-014；TEST-018、TEST-019）
- [ ] T080 [P] [US3] 先写 `smart-h5/e2e/dorm-lock.spec.ts`，覆盖路由兼容、本人查看/修改、人脸流程失败、网络超时及存储无敏感材料，记录失败；仅 Mock 不记真机通过。（FR-013、FR-014、FR-022；TEST-017～TEST-019、TEST-027、TEST-028）
- [ ] T081 [US3] 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/MyLockController.java` 实现本人状态、受控 reveal/修改和请求查询，复用已有校验服务并校验一次性引用绑定，不直接信任旧badge/校验结果，通过 T078。（FR-009、FR-013、FR-014；依赖 T015、T017、T030、T078）
- [ ] T082 [US3] 在 `smart-h5/src/features/dorm/api.ts` 接入本人契约、no-store短时读取和请求状态，复用当前 `smart-h5/src/lib/api/http.ts` 的 module=platform 请求封装，保留非门锁调用不变，通过 T079。（FR-013、FR-014；依赖 T079、T081）
- [ ] T083 [US3] 在 `smart-h5/src/features/dorm/lock-state.ts` 实现授权/命令/能力/冻结状态与界面映射、短时内存结果和失效清理，通过 T079；不新增token持久化。（FR-007、FR-013、FR-014；依赖 T079、T082）
- [ ] T084 [US3] 改造 `smart-h5/src/app/dorm/lock/page.tsx` 与 `smart-h5/src/app/dorm/get-code/page.tsx` 的本人展示、6位密码校验和现有人脸流程，真实能力未验证不伪造动态码，通过 T078～T080。（FR-010、FR-013、FR-014；依赖 T078～T083）
- [ ] T085 [US3] 在 `smart-h5/docs/superpowers/doorlock/test-and-acceptance.md` 登记 check/test/e2e/build 及本人/跨身份/冻结回归结果、与旧界面的差异；设备未测单列。（FR-013、FR-022；TEST-017～TEST-019、TEST-027、TEST-028；依赖 T084）

## Phase 8：跨模块收敛与上线前验收

完成开发后的总验收任务；本轮和自动开发均不得因此取得生产变更授权。

- [ ] T086 [P] 先写 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/observability/LockAuditMetricsTest.java`，覆盖事件到回执关联、积压/离线/未撤权/核验指标、导出审计和错误脱敏，记录失败。（骨架前置 T007；FR-020、FR-022；TEST-024）
- [ ] T087 在 `smart-module/smart-lock/smart-lock-biz/src/main/java/com/tce/smart/lock/observability/LockAuditMetrics.java` 实现最小审计/指标及告警，复用现有基础设施，通过 T086；不输出密码、指纹或可重放报文。（FR-014、FR-020；依赖 T034、T046、T086）
- [ ] T088 在 `smart-module/smart-lock/docs/superpowers/doorlock/performance-baseline.md` 根据 T002/T003 规模、增长率和 T068 演练确定 p95、积压清空、撤权时延、迁移窗口和容量阈值；压测前由业务/DBA确认，不先造承诺。（FR-001、FR-008、FR-022；依赖 T002、T003、T068）
- [ ] T089 在 `smart-module/smart-lock/smart-lock-biz/src/test/java/com/tce/smart/lock/performance/LockLoadScenarioTest.java` 建立合成峰值入住/撤权/回执/历史分页负载和重启恢复场景，按 T088 的已确认阈值运行 Oracle 压测；取得实际查询计划后再修改 SQL/索引并重测。（骨架前置 T007；FR-001、FR-008、FR-020；TEST-025；依赖 T013、T035、T067、T087、T088）
- [ ] T090 在 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/lock/LegacyRouteCompatibilityTest.java` 先补全部现有Web/H5调用方的旧路径、envelope和身份安全契约；再于 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/SmtDormitoryStaffController.java` 适配新门面并记录旧入口退出条件，不删除尚有调用者的接口。（FR-002、FR-005、FR-013、FR-021；TEST-028；依赖 T047、T081）
- [ ] T091 在 `smart-module/smart-lock/docs/superpowers/doorlock/release-readiness.md` 汇总跨模块编译/测试、Web/H5对版、Oracle真实计划、迁移、实锁、切换回退及隐私审查证据；逐项映射 FR-001～FR-022、SC-001～SC-006，不将未验证项写通过。（FR-022；依赖 T039、T058、T068、T077、T085、T087、T089、T090）
- [ ] T092 在 `smart-module/smart-lock/docs/superpowers/doorlock/legacy-retirement.md` 登记最终切换后待移除旧服务URL、兼容发送器、账号访问、MySQL在线配置及历史查询接管的精确目标与回滚边界；实际停用/删除须新授权并验证，不在开发阶段提前执行。（FR-001、FR-003、FR-021；依赖 T091）

## Dependencies & Execution Order

```text
输入核验 ─→ 已确认 Oracle 物理设计 ─→ 本地 Oracle 持久层
骨架/契约 ─→ US1 纯领域 + 模拟协议 ─→ US1 Oracle完整闭环
     ├────→ US2 Web契约/页面 Mock ─→ 管理端联调与对版
     ├────→ US4 合成转换/重跑 ────→ Oracle迁移演练
     ├────→ US5 禁发/冻结/回退测试 → 隔离设备与切换演练
     └────→ US3 H5 本人 Mock ─────→ 本人端联调
各故事实际验收 + 性能/审计/兼容 → 上线准备评审 → 另行批准现场切换
```

跨房调宿默认旧房撤权确认后才发新房；离线或未知结果走明确的应急/人工处置，不因演示方便调整为双房同时有效。同房换床不重新发放；正常有效入住内重新授权使用新 grant 版本，不复活已撤旧 grant。

## Parallel Examples

| 用户故事 | 可并行示例 | 必须串行汇合 |
| --- | --- | --- |
| US1 | T018～T025 各测试文件；平台Outbox与协议codec在契约冻结后分工 | 消费/授权/命令/回执按依赖集成，再做 T039 |
| US2 | T041～T043 测试；T049～T056 按页面文件分工 | 共用 API T048 完成后接入，菜单和整体验收统一收口 |
| US4 | T059～T061 独立测试；转换器可先用合成输入 | 目标 ID map/账本定版后再做重跑与核对 |
| US5 | T069～T071 独立测试；runbook 文案可先草拟 | 演练必须依赖完整成员、迁移结果及实锁证据 |
| US3 | T078～T080 后端/状态/浏览器测试 | api.ts、页面及登出清理共享文件只给一个责任人 |

## Implementation Strategy

建议首个实现交付聚焦 T006～T011 的骨架/契约及 T018～T025 的测试编写、纯单测和对应离线最小实现。T018 的 Oracle 集成部分不属于可提前执行的离线范围：必须等 T005/T008 完成、平台测试对象与资源获批并准备就绪后再执行；缺失该证据时 T018 整项保持未勾选。数据库证据齐全后执行该集成部分，再完成 T026～T039 的 Oracle 闭环。此 MVP 只用于验证可靠性，不是可替代生产的第一版上线。

US2/US3 可按冻结契约并行开发 Mock 页面，US4 同时准备可重跑数据转换；US5 的禁发约束必须从第一次可能发送的实现起生效，不能等最终演练才加入。全部开发和隔离测试通过后，用户另行批准最终迁移和全通信域操作。

## 覆盖与交付清单

- FR 与 TEST 的完整映射以 [test-matrix.md](test-matrix.md) 为准，各任务内保留具体关联。
- SC-001：T021、T036～T039、T090；SC-002：T018～T025、T034～T039。
- SC-003：T040～T058、T078～T085；SC-004：T059～T068。
- SC-005：T069～T077；SC-006：T001～T009、T091～T092。
- 完成定义包括实际测试、失败边界、中文注释和独立审查；环境未齐的任务保持未勾选。
