# Tasks: 园区能耗评审修复

## Phase 1: Setup
- [X] T001 同步基线并建立隔离修复分支，生成 specs/007-energy-review-fixes/spec.md 与 plan.md（起点 cb47dc74，收尾再次核验并快进到 157d4ca1）。

## Phase 2: Foundations
- [X] T002 校验规格一致性与所有权，固定 specs/007-energy-review-fixes/contracts/energy.md。

## Phase 3: US1 应用读取
- [X] T003 [P] [US1] 在 platform-biz/src/test 的 EnergyUsageOpenControllerTest.java 先复现应用入口缺失及授权矩阵（RED：新控制器不存在）。
- [X] T004 [US1] 在 platform-biz/src/main 的 controller/energy/EnergyUsageOpenController.java 实现只读 server 与园区校验（9 项通过，任务独立评审通过）。

## Phase 4: US2 总分表去重
- [X] T005 [P] [US2] 在 EnergyScopeDecisionTest.java 添加缺父与断链失败测试（8 项中 2 项按预期失败）。
- [X] T006 [US2] 修复 EnergyScopeDecision.java 的显式父级缺失处理（8 项通过，任务独立评审通过）。

## Phase 5: US3 恢复可靠性
- [X] T007 [P] [US3] 在 platform-biz service/energy 与 smart-schedule 测试目录复现旧事实跳过、跨批停滞、失败伪成功；另以 RED 覆盖完成后次日重扫和缺读数空值拆箱。
- [X] T008 [US3] 修复 EnergyProjectionServiceImpl.java 与必要 Mapper/进度实现，支持有界续跑及过期重算（恢复 21 项、Mapper 2 项通过，包含跨日质量及跨月完成标记修复）。
- [X] T009 [US3] 修复 SmartMeterTimerTask.java 持续推进、失败可见和实时处理配额，补齐内部契约兼容测试（调度 12 项通过，已纳入最新基线 95 项能耗集成验证）。

## Phase 6: US4 授权撤销恢复
- [X] T010 [P] [US4] 在 SysOauthClientDetailsServiceImplTest.java 复现异常不清缓存与相同 scope 重试遗漏（RED：26 项中新增 7 项失败）。
- [X] T011 [US4] 在 UPMS service 中实现客户端缓存失效与同库事务撤销待办/补偿，补异常与幂等回归（服务 34 项、Mapper 2 项通过，公平重试修复复审通过）。

## Phase 7: 验证
- [X] T012 按 quickstart.md 运行受影响模块测试/编译与独立评审；修复发现后重跑（最新基线 152 项、三个服务 package 成功，最终复审通过）。
- [X] T013 在 quickstart.md 与 data-model.md 记录真实验证、配置/迁移要求和未验证边界。

## Dependencies / Parallel
T001→T002→(T003→T004 | T005→T006 | T007→T008→T009 | T010→T011)→T012→T013。
前四条实现路径文件所有权独立；platform-biz Maven 测试需协调避免同 target 并发写入。
FR-001=T003/004；FR-002=T005/006；FR-003/004/005=T007/008/009；FR-006=T010/011；FR-007=T012/013。
