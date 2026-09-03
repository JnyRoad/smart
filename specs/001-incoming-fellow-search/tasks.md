# Tasks: 入厂申请随行人员搜索

**Feature Branch**: `feat/incoming-fellow-search`
**Input**: `specs/001-incoming-fellow-search/` 的规格、研究、数据模型和接口契约

## Phase 1: 已完成的设计准备

- [X] T001 记录 Oracle 表、索引、统计信息和无 DDL 决策到 `specs/001-incoming-fellow-search/research.md`

---

## Phase 2: User Story 1 - 按随行人员姓名定位申请单（P1）

**Goal**: 姓名命中任一随行人员时，列表返回且仅返回其所属主申请单。

**Independent Test**: 用随行人员姓名生成 `getSmtVisitorPage` 的真实 MyBatis 绑定 SQL，确认其采用
主申请人 `OR EXISTS` 随行人员姓名条件，且主查询没有随行人员外连接。

- [X] T002 [US1] 在 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtAdmittanceApplyMapperXmlTest.java` 先增加姓名搜索的失败 Mapper 动态 SQL 契约测试
- [X] T003 [US1] 在 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtAdmittanceApplyMapper.xml` 用姓名相关 `EXISTS` 替换随行人员外连接筛选
- [X] T004 [P] [US1] 将 `smart-ui/src/views/platform/visitor/incoming_record/index.vue` 的姓名筛选标签改为“访客/随行姓名”

---

## Phase 3: User Story 2 - 按随行人员证件号定位申请单（P1）

**Goal**: 证件号命中任一随行人员时，列表返回对应主申请单并保留其他筛选语义。

**Independent Test**: 用随行人员证件号生成 `getSmtVisitorPage` 的真实 MyBatis 绑定 SQL，确认它以
相关 `EXISTS` 作为筛选条件，且参数仍通过现有 `query.certNo` 绑定。

- [X] T005 [US2] 在 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtAdmittanceApplyMapperXmlTest.java` 先增加证件号搜索的失败 Mapper 动态 SQL 契约测试
- [X] T006 [US2] 在 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtAdmittanceApplyMapper.xml` 将证件号筛选改为相关 `EXISTS`

---

## Phase 4: 验证与交付

- [ ] T007 在 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtAdmittanceApplyMapperXmlTest.java` 运行定向 Maven 测试并执行 `specs/001-incoming-fellow-search/quickstart.md` 的可执行验证项
- [X] T008 在 `specs/001-incoming-fellow-search/` 复核实现、测试结果和数据库零变更范围，确认未引入 DML、DDL、索引或统计信息变更

## Dependencies & Execution Order

- T001 已完成，T002 → T003 完成姓名搜索主链路；T004 可与 T002/T003 并行。
- T005 → T006 在 T003 后执行，因为证件号条件依赖已移除的随行人员外连接。
- T007、T008 依赖全部实现任务完成。

## Implementation Strategy

先完成并验证 User Story 1，再完成 User Story 2；两条故事共用同一 Mapper，但每项测试均先于对应
SQL 变更执行。最终只交付 Mapper 查询条件和页面文案，不触碰数据库对象或业务数据。
