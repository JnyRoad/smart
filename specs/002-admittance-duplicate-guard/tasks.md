# Tasks: 入厂申请区域并发判重

**Input**: `specs/002-admittance-duplicate-guard/` 中已批准的规格、方案、研究、数据模型、契约与验证指南。

**Tests**: 本功能要求 TDD。每项行为测试必须先运行并因目标功能缺失而失败，再写最小实现；真实 Oracle 双事务测试是发布前硬门槛。

## Phase 1: 准备与基线

**Purpose**: 固化发布边界并确认当前针对性测试基线。

- [x] T001 复核 `specs/002-admittance-duplicate-guard/data-model.md` 中锁表 DDL 仅为发布评审材料，确认本工作区不执行任何真实 DDL/DML。
- [X] T002 运行 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtAdmittanceApplyMapperXmlTest.java` 与 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImplTest.java` 的现有 Maven 基线测试。

---

## Phase 2: 基础判重与锁协议（阻塞所有用户故事）

**Purpose**: 建立统一的区域精确交集 SQL、全人员判重、证件号锁和可审核的失败语义。

- [X] T003 在 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtAdmittanceApplyMapperXmlTest.java` 先增加会失败的动态 SQL 契约测试：全体随行人员参与、区域 `1`/`11` 边界精确、区域未知保守冲突、拒绝/作废排除和开区间时间条件。
- [X] T004 在 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImplTest.java` 先增加会失败的服务测试：非主随行人员命中重复、空证件号拒绝、区域参数透传和保存前协调锁。
- [X] T005 在 `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/mapper/SmtAdmittanceApplyMapper.java` 与 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtAdmittanceApplyMapper.xml` 实现最小 Mapper 契约：区域列表精确交集的重复计数、区域未知回退、锁锚点查询/创建与行锁获取。
- [X] T006 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImpl.java` 实现最小的证件号规范化、去重排序、协调锁获取与全人员区域判重，并只在正式保存事务中持有锁。
- [X] T007 运行 T003/T004 对应的两组 Maven 定向测试，确认红灯已转绿且未弱化断言。

**Checkpoint**: 任何正式提交都先取得稳定顺序的证件号协调锁；同区域重复和区域未知绕过均被阻止。

---

## Phase 3: User Story 1 - 同区域的人员重复申请被阻止（P1）

**Goal**: 主申请人与全部随行人员在同区域、重叠时间内都无法重复提交。

**Independent Test**: 模拟主申请人与非主随行人员各自命中有效申请，验证均阻止整单保存且不写入申请。

- [X] T008 [US1] 在 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImplTest.java` 增加主申请人/随行人员角色交叉、拒绝/作废排除、时间端点相接的失败回归测试。
- [X] T009 [US1] 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImpl.java` 补齐满足 T008 的最小业务错误文案与保存前回滚行为。
- [X] T010 [US1] 运行 `SmtAdmittanceApplyServiceImplTest` 的定向 Maven 测试，验证 US1 通过。

---

## Phase 4: User Story 2 - 跨区域的申请可以共存（P1）

**Goal**: 相同证件号与重叠时间在无共同区域时仍可提交，同时不把数字前缀误判为相同区域。

**Independent Test**: 对区域完全不交集、多个区域有一个交集、`1` 与 `11` 三种场景生成真实 MyBatis 绑定 SQL 并验证服务结果。

- [X] T011 [US2] 在 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtAdmittanceApplyMapperXmlTest.java` 增加跨区域允许、任一共同区域阻止、`1`/`11` 不冲突的失败 SQL 契约测试。
- [X] T012 [US2] 在 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtAdmittanceApplyMapper.xml` 调整区域交集条件至满足 T011，保持 Oracle 绑定参数与未知区域的保守分支。
- [X] T013 [US2] 运行 `SmtAdmittanceApplyMapperXmlTest` 的定向 Maven 测试，验证 US2 通过。

---

## Phase 5: User Story 3 - 并发提交只允许一方成功（P1）

**Goal**: 正式保存入口对共享任一证件号的同时提交严格串行化，不产生死锁或双成功。

**Independent Test**: 用 Mockito 验证同单证件号去重排序、重复键竞争后的行锁重试和保存入口先锁后查；在独立 Oracle 测试库完成 20 轮双事务验收。

- [X] T014 [US3] 在 `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImplTest.java` 先增加证件号去重排序、首笔插入竞争重试、锁等待业务失败及保存入口顺序的失败测试。
- [X] T015 [US3] 在 `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImpl.java` 实现满足 T014 的最小重试和异常转换，避免不同人员顺序导致死锁。
- [X] T016 [US3] 在 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtAdmittanceApplyMapperXmlTest.java` 增加锁表创建与 `FOR UPDATE` 绑定 SQL 的契约测试，并在 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtAdmittanceApplyMapper.xml` 实现相应语句。
- [X] T017 [US3] 运行核心与业务的定向 Maven 测试，验证锁协议的单元/契约覆盖均通过。
- [ ] T018 [US3] 依据 `specs/002-admittance-duplicate-guard/quickstart.md` 在隔离 Oracle 测试库执行 20 轮双事务并发验收，并记录结果；该任务不执行生产 DDL/DML。

**Checkpoint**: T018 通过前不得宣称 Oracle 层并发保证已完成验证。

---

## Phase 6: 蓝图收口与交付验证

**Purpose**: 将实际设计与验证边界同步回蓝图，完成全链路检查。

- [X] T019 在 `docs/yuhui-prototype/yuhui-blueprint.html` 更新 2.7：以区域 ID 精确交集替换子串匹配、声明全人员校验与证件号锁协议、注明区域未知保守策略和 Oracle 并发验收前置。
- [X] T020 已于 2026-09-02 从 `smart-module` 运行 `quickstart.md` 的 Maven 命令：核心模块 Mapper 5 项；业务模块共 74 项（目标服务 65 项、同名历史服务 9 项）；两个目标模块合计 79 项，均为 0 failures / 0 errors。Oracle 验收因当前工作区没有隔离 Oracle 测试库而未执行，现场步骤保留在 `quickstart.md`。
- [X] T021 运行 `git diff --check`，执行规格、方案、任务与实际改动的一致性复核，确保未写入 DDL/DML、密钥或无关文件。

## Dependencies & Execution Order

- T001 → T002 → T003/T004；T003、T004 都必须先观察到正确失败。
- T005、T006 依赖 T003/T004；T007 验证基础层后才能进入各用户故事。
- US1（T008-T010）和 US2（T011-T013）均依赖基础层；二者共享文件，当前工作区按顺序执行。
- US3（T014-T018）依赖基础层；T018 是外部测试环境门槛，不得以 Mock 代替。
- T019-T021 依赖所有代码与测试任务；T019 不可早于实际行为稳定。

## Implementation Strategy

1. 先用 Mapper/服务测试锁定原始失败行为，再实现区域精确交集与全人员判重。
2. 仅在正式保存事务使用证件号锁；保留提交前检查的即时提示语义。
3. 单元与 SQL 契约测试通过后，才在隔离 Oracle 环境做并发竞争验收。
4. Oracle 验收未执行或失败时，交付必须明确标为“代码已验证、数据库并发语义未完成验收”。
