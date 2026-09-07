# Tasks: 可靠权限删除与批量调度

输入：[spec.md](spec.md)、[plan.md](plan.md)、[data-model.md](data-model.md)、[contracts](contracts/permission-operations.md)。所有行为变更先写能失败的测试，再实现；主代理维护勾选。

路径缩写：`PC` = `smart-module/smart-platform/smart-platform-core`；`PB` = `smart-module/smart-platform/smart-platform-biz`；`SCH` = `smart-module/smart-schedule`。Java 路径分别位于 `src/main/java/com/tce/smart/platform/core`、`src/main/java/com/tce/smart/platform`、`src/main/java/com/tce/smart/schedule`；测试对应 `src/test/java`。

## Phase 1: 规格与证据

- [X] T001 核对工作区、源码基线和蓝图，固化 specs/012-reliable-auth-batch/spec.md 与 checklists/requirements.md。
- [X] T002 固化 specs/012-reliable-auth-batch/plan.md、research.md、data-model.md、contracts/permission-operations.md 和 quickstart.md。
- [X] T003 对 specs/012-reliable-auth-batch/spec.md、plan.md、tasks.md 进行 Spec Kit 一致性分析并记录阶段交接。

## Phase 2: US1 / US5 现有回执断点修复（不依赖改表）

目标：成功任务与本地下发记录原子提交；失败不能丢失下轮处理机会。此阶段是 US1、US5 的前置增量，不等于故事整体完成。

- [X] T004 [US1] 在 PC/src/test/java/com/tce/smart/platform/core/service/impl/IscTaskCompletionServiceTest.java 与 SCH/src/test/java/com/tce/smart/schedule/service/platform/impl/ISCDeviceTaskServiceImplTest.java 补失败测试，覆盖记录异常、条件未命中和各成功出口（FR-007/008）。
- [X] T005 [US1] 在 PC/src/main/java/com/tce/smart/platform/core/service/impl/IscTaskCompletionService.java 实现可代理的同库事务完成器，接入 SCH/src/main/java/com/tce/smart/schedule/service/platform/impl/ISCDeviceTaskServiceImpl.java；修复 PC 下 SmtIscDownRecordServiceImpl 的静默写失败（FR-007）。
- [X] T006 [US5] 在 SCH 的 ISCDeviceTaskServiceImplTest.java 先验证平台人员/数据不存在不能视作设备成功，再调整 ISCDeviceTaskServiceImpl 对应分支（FR-008/014）。
- [X] T007 [US1] 运行上述测试及 PC 的 SmtIscDownRecordServiceImplTest.java，审查该阶段差异并在 specs/012-reliable-auth-batch/quickstart.md 记录真实结果。

## Phase 3: US2 批内查询优化（接续 Phase 2）

- [X] T008 [US2] 在 SCH/src/test/java/com/tce/smart/schedule/service/platform/impl/ISCDeviceTaskServiceImplTest.java 先补多设备同员工只远查一次、园区隔离、任务 personId 优先及异常保持未知的失败测试（FR-006，SC-001）。
- [X] T009 [US2] 在 SCH/src/main/java/com/tce/smart/schedule/service/platform/impl/ISCDeviceTaskServiceImpl.java 实现有界轮次身份复用，保持权限窗口和既有批量 API，运行针对性回归（FR-006）。

## Phase 4: 数据库与共用基础

- [X] T010 在本任务临时 Oracle 容器构造最小合成 schema，验证现有字段映射、事务与查询计划；将元数据证据和限制记入 specs/012-reliable-auth-batch/research.md，固化 data-model.md（用户已授权本机临时 Docker 测试，禁止使用真实数据）。
- [X] T011 在 PC/src/test/java/com/tce/smart/platform/core/service/impl/AuthOperationServiceTest.java 与 SCH/src/test/java/com/tce/smart/schedule/service/platform/impl/AuthOperationOracleTest.java 先验证分片原子性、展开/预期数、零任务不可成功、租约及幂等约束（FR-001/003/007/008）；Oracle 集成测试复用 SCH 的既有 JDBC 依赖。
- [X] T012 在 PC/src/main/java/com/tce/smart/platform/core/entity、mapper、service/impl 和 src/main/resources/mapper 中实现已固化批次/请求/目标/尝试与证据持久化；迁移契约随 specs/012-reliable-auth-batch 的发布说明交付（FR-001/002/003/007/008）。

## Phase 5: US3 版本与来源协调

- [X] T013 [US3] 在 PC/src/test/java/com/tce/smart/platform/core/service/impl/AuthOperationVersionTest.java 先覆盖共享设备同时撤权、旧 ADD/DELETE、跨批次重复与版本重用（FR-010/011）。
- [ ] T014 [US3] 在 PC 的 service/impl/AuthOperationService.java、授权版本 Mapper 和两种 DownRecordServiceImpl 中实现版本与接管；PB 的 SmtStaffDeviceAuthServiceImpl、SmtDeviceAuthorityServiceImpl、SmtOrganizeRelationServiceImpl 的新增/覆盖/重发受保护（FR-010/011）。

## Phase 6: US1 / US5 撤权入口和双接入

- [ ] T015 [US1] 在 PB/src/test/java/com/tce/smart/platform/service/impl 下先补权限组删除/清空、无设备/人脸、离职、访客及车辆入口保留依据的失败测试（FR-001/012/014）。
- [ ] T016 [US1] 在 PB 的 SmtStaffDeviceAuthServiceImpl、SmtDeviceAuthorityServiceImpl、SmtStaffServiceImpl、SmtVehicleApplyServiceImpl、securityzone/impl/SmtSecurityAuthDeleteServiceImpl、securityzone/impl/SmtSecurityPersonRelationServiceImpl、admittance/impl/SmtAdmittanceApplyServiceImpl、SmtVisitorServiceImpl、VisitorTaskServiceImpl 接入稳定选择与异步展开，移除先删依据路径；核对 SmtSnapPersonServiceImpl 的访问状态变化与到期清理衔接（FR-001/012/014）。
- [ ] T017 [US5] 在 PC/src/test/java/com/tce/smart/platform/core/service/impl/AuthOperationTransportTest.java 先验证两任务表关联、缺记录任务创建、未知结果和直连命令映射（FR-007/008/014）。
- [ ] T018 [US5] 在 PC 的 SmtDeviceTaskServiceImpl、SmtIscDeviceTaskServiceImpl、两种 DownRecordServiceImpl 与 SCH 的 ISCDeviceTaskServiceImpl 中接入目标、尝试与可信证据，直连按实际支持操作适配（FR-007/008/014）。

## Phase 7: US2 完整批量调度

- [ ] T019 [US2] 在 SCH/src/test/java/com/tce/smart/schedule/service/platform/impl/AuthOperationSchedulerTest.java 先验证撤权/新增/回执保留额度、园区设备轮转、离线退避和在途借用上限（FR-004/005/009/015）。
- [ ] T020 [US2] 在 SCH 的 service/platform/impl/AuthOperationScheduler.java、ISCDeviceTimerTask、PlatformTimerTask 与 PC 任务 Mapper 中实现有界领取、实例限流、进度独立预算及分段恢复（FR-004/005/009/015）。

## Phase 8: US4 可见性与对账

- [ ] T021 [US4] 在 PB/src/test/java/com/tce/smart/platform/service/impl/AuthOperationManagementTest.java 先覆盖园区权限、分页、只重发未完成、人工核验、告警与孤儿补偿（FR-002/009/012/013/016）。
- [ ] T022 [US4] 在 PB 的 controller/AuthOperationController.java、service/impl/AuthOperationManagementService.java、PC 结果服务及 SCH/PlatformTimerTask 实现进度、重发、告警、超时扫描与分段对账（FR-002/009/012/013/016）。
- [ ] T023 [US4] 在 smart-ui/src/views/platform/area/limit/personDetail.vue、vechileDetail.vue 及对应 src/api/platform/area/limit.js 接入批次状态、分页失败明细和操作反馈，验证正常列表无需全量加载万条目标（FR-013，SC-003）。

## Phase 9: 交叉验证与交付

- [ ] T024 在 SCH/src/test/java/com/tce/smart/schedule/service/platform/impl/AuthOperationCapacityTest.java 与临时 Oracle 中验证 10k/20k 目标、100k 展开、崩溃恢复及阶段耗时（SC-001/002/003/004/005）。
- [ ] T025 按 specs/012-reliable-auth-batch/quickstart.md 在明确授权的 ISC/直连测试环境进行型号与混合负载联调，分别报告真实完成速率和未支持能力（SC-006，现场前置，未授权不执行设备写入）。
- [ ] T026 完成独立代码审查、相关回归和 git diff --check，按事实更新 specs/012-reliable-auth-batch/tasks.md 与 docs/yuhui-prototype/yuhui-blueprint.html，区分代码、数据库和设备验收。

## Dependencies & Execution Order

- T003 通过后可执行 T004–T009；T010 可独立并行，不写这些代码文件。
- T010 → T011/T012 → T013/T014；版本保护先于入口灰度。
- T015/T016 与 T017/T018 依赖共用基础和版本契约，明确文件所有权后可协调。
- T019/T020、T021/T022/T023 接续入口与回执；T024/T025/T026 为完成门槛。
- 同一源文件不得由两个实现代理同时写入；主代理统一维护本清单。

## Execution Record

- 2026-09-05：用户授权本机临时 Docker Oracle，仅合成测试数据；数据库任务不再等待测试库连接配置。
- 2026-09-05：Spec Kit 分析确认 16/16 功能需求有任务覆盖，26 个任务编号唯一；A 阶段可交接执行，完整数据库阶段仍须 T010 固化物理契约后开始。未发现阻止 A 阶段的规格冲突。
- 2026-09-05：T010 已完成合成表元数据、关键约束及单批 10k 基线核对；五张新表的物理契约已固化。真实 Oracle ISC 事务用例通过；完整 Java 持久生命周期、竞争领取、容量与设备验收仍为后续门槛。
- 2026-09-05：补查普通访客到期与保密区人员独立撤权入口，纳入 T015/T016。普通访客同行人和入厂同行人使用各自稳定主体类型。首次出门是否立即撤权沿用已有业务规则；本任务保证明确撤权及到期清理可恢复，不凭注释恢复停用行为。
- 2026-09-05：T011/T012 经两轮独立修复复审通过；最新 core 19 与真实 Oracle 18 项统一运行成功，零失败/错误/跳过。五表受理、展开、领取和回执基础完成，不代表版本保护、实际业务入口、全量调度或设备验收完成。
- 全部代码、Oracle 与设备测试在执行前保持未完成，不能凭规划打勾。

- 2026-09-05：T013 版本测试与独立协调层经两轮修复复审通过，PC8+真实Oracle20全部通过；T014仍待业务工作流、旧任务与实际入口接入。
- 2026-09-05：员工入口4项P1经fix1限定复审通过，47项隔离及13项Oracle通过；缺照片且零资源组合随后通过2项隔离与2项Oracle RED→GREEN及fix2限定复审。有界受理的全量内存展开问题仍未解决。界面fix2限定复审通过，41项相关测试通过；写入批次标识返回、管理写操作和现场界面验收尚未完成。
- 2026-09-05：完整ISC受控管道发现并推进两个集成修复：不同事件键的重复成功回执已修复并限定复审通过；正常撤权展开期间错误创建补偿、合法等待被错误核验的组合问题随后经workflow fix4及ISC两项管道验证通过；10k/20k/100k容量尚未验收，不能按局部测试勾选完整任务。

- 2026-09-05：201直连完整管道与1000人单次受理诊断通过，均非万级容量验收；201采用服务循环展开，新增实际Timer展开节奏验证，ISC容量须另测。UI生产构建与合成响应浏览器验证通过，真实API及设备尚未联调。调度删除保留份额、精确优先级与实际HTTP总截止仍在fix3修复；transport跨园区身份与坏成员隔离fix2进入真实Oracle验证。

- 2026-09-05：201实际Timer旧调度对照确认RED：120tick仅来源游标60，目标及HTTP0，超过原120秒展开门槛；自有数据清理及线程退出已核。持续展开正在修复，T020/T024保持未完成。
- 2026-09-05：工作流最终冻结55+version2通过；Transport fix3限定复审通过。Scheduler fix3回归39+DIRECT1通过，独立复审新增全局Feign Bean隔离问题进入fresh fix4。治理动作及通用非员工投影前置分别在独立文件边界内实施，尚不勾T015/T016/T022。
- 2026-09-05：HTTP Bean fix4与持续展开均限定复审通过；同201实际Timer从120秒无目标的RED转为18705ms展开、143613ms完整管道GREEN，来源清理归零。发现逐tick领取/发送仍限吞吐，正在实施有界连续推进；10k/20k、100k与ISC容量未通过。
- 2026-09-05：治理首增量23隔离与3真实Oracle通过，24列ACTION已安装，AUTH表实查21张；最后输入校验收紧后23隔离通过，独立复审中。通用来源离线25项通过含真实Spring事务重放，合成Oracle迁移尚未执行；业务family入口和T022完整对账仍未完成。
- 后续验证：治理fix2最终30隔离及3真实Oracle通过，原三个问题均经限定复审关闭。通用来源32/18列迁移和4新+7旧Oracle通过，27离线通过，旧窗口CAS测试缺陷已修复复审；不能据此前置勾选完整family或T022。
- 后续验证：有界连续推进52项组合、DIRECT1项、新窗口UNKNOWN排除1项通过并限定复审；201直连TIMER管道88745ms，201 ISC TIMER管道52956ms/36 HTTP，全部回执齐全、来源归零。
- 万级反例：500人×20设备ISC删除在900秒展开诊断截止失败，仅250/10000目标、HTTP0。lanes累计647311ms/250次为实测主导成本；20k暂停扩大，先修复候选绑定热路径，不放宽代次或回执门禁。T020/T024仍未完成。
- 人员回执增量56 Java与58相关UI通过并限定复审；HTTP幂等受理头另行实现，PERSON-only共享身份169离线通过、Oracle及独立审查仍待完成；真实设备与最终整合回归仍未执行。

- 后续验证：PERSON共享身份28项Oracle通过；配置异常路径修复后171离线及限定复审通过。请求幂等受理头12列及子批次查询索引已迁入临时Oracle，81后端/68前端/3Oracle通过；评审另发现两个页面原键竞争问题正在修复。
- 候选热查询修复：早解析再增长至10k资源的12组SQL结果一致，原查询约2.2–2.6秒，集合差查询4–29毫秒；最终27离线及6Oracle边界测试通过并限定复审。正式10k TIMER已安排新冻结输入重新验证，不能以SQL对照勾选容量。
- 页面补测：本地真实Vue配合合成响应，原请求响应丢失后刷新恢复、独立新意图、账号隔离和大整数批次ID通过；跨页面原键竞争修复另测，真实后端和设备仍未联调。
- 后续收敛：请求原键竞争fix1通过77项UI与限定复审；最终代码的原生IndexedDB双标签页及真实Vue合成响应恢复验证通过。员工历史证据fix1通过126离线、6Oracle和限定复审；设备迁园、园区为空且旧关系保留不会使有历史证据的删除整批中止，显式越权新设备仍拒绝。
- 最新万级验证：通用来源与MINUS输入的10k ISC DELETE/TIMER仍失败，900秒停止等待后9127/10000目标，HTTP0，来源仍保留。四条资源查询访问路径与空页重复扫描进入独立修复；空页增量36项实执行通过、1既有Oracle跳过，限定复审通过，容量尚未重跑。不能据局部改进勾选T020/T024。
- 后续万级验证：空页停止与四条资源索引提示均完成限定验证后，仅覆层三个生产文件重跑10k ISC DELETE/TIMER；524510ms完整展开10000目标，原1800秒截止仍失败，3742确认、6143排队、115等待确认，来源500。明细2222次与50504条曝光显示大量重复处理；正在修复可信设备回执阶段结束错误依赖全部来源收敛的接缝，仍不勾T020/T024。
- 后续基础增量：DIRECT claim及强类型读取器16项离线测试、局部车牌修复复审通过，Oracle待验；DIRECT旧出口接管82项离线通过、1Oracle跳过，独立复审及真实begin接入未完。历史五流盘点core23项离线通过、Oracle及审查待验；不等同完整对账补偿或真实残留清除。

- 后续局部验证：可信物理结算三Java增量8项离线及8项真实Oracle通过，独立复审关闭并发重复ACK的CAS冲突。正式10k已准备最小三文件新输入，容量尚未复验；T020/T024继续未完成。STAFF canonical/V2 142项离线及SLO计量14项离线通过限定复审，各自Oracle与正式负载接缝不据此完成。
- 后续万级验证：最终调度与ISC分片配置在临时Oracle、合成身份和受控ISC协议端完成10,000个DELETE目标复跑。487,368ms展开、1,231,086ms全链路收敛；每阶段60次请求、总240次，全部目标CONVERGED、来源归零，峰值堆约296MB、连接4、实际在途400。该结果只关闭本固定ISC DELETE受控路径；20k新增积压中的小撤权、正常列表、混合失败、JVM恢复、真实ISC/直连与现场数据库均未验，T020/T024保持未完成。
- 后续SC-002受控验证：20,000个ISC ADD目标（100人×200设备）保持未确认积压时，一个MVC批量请求提交20人单设备撤权，20个DELETE目标均在5,581ms首次外发ISC配置；当时19,800个ADD目标仍排队，连接4、在途220。20个独立撤权请求且每人200设备的4,000个DELETE目标反例在120秒内没有首次外发，定位为来源展开的批次游标和全批绑定门槛，不能靠HTTP配额解决。T024仍未完成：需实现持久来源领取后复测独立请求并发、20k全链路确认、SC003/SC005、JVM恢复与真实设备/数据库。
- 提交前修复：访客、访客车辆和随行访客的离场撤权改用统一的 ISO 本地时间解析后绑定 JDBC 时间参数，整分钟结束时间不再因缺少秒字段阻断撤权；定向回归 28 项及 PC 全量 408 项通过，后者有 6 项 Oracle 候选跳过。该修复不替代真实 Oracle、设备或完整入口验收。
