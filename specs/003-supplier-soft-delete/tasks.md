---

description: "保密供应商软删除实现任务"
---

# Tasks: 保密供应商软删除

**Input**: `spec.md`、`plan.md`、`research.md`、`data-model.md`、`contracts/`。

**Tests**: 必须按 TDD 执行：先写并运行失败的契约测试，再写最小实现。

## Phase 1: Setup

- [X] T001 核对现有供应商读取链和发布脚本约束，确认 `specs/003-supplier-soft-delete/plan.md` 中的文件范围。

---

## Phase 2: User Story 1 - 删除保留历史 (Priority: P1) 🎯 MVP

**Goal**: 供应商和人员删除改为可追溯的逻辑删除，且保留供应商在册人员校验。

**Independent Test**: 运行实体注解与 XML 契约测试，确认逻辑删除字段的有效/删除值和删除后的标准读取语义。

- [X] T002 [US1] 先在 `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtSecurityAreaSupplierSoftDeleteMapperXmlTest.java` 写并运行失败的实体逻辑删除与 Mapper SQL 契约测试。
- [X] T003 [P] [US1] 在 `smart-module/database/manual/20260902_add_supplier_soft_delete.sql` 与 `smart-module/database/manual/20260902_rollback_supplier_soft_delete.sql` 编写幂等正向和受保护回滚脚本，并更新 `smart-module/database/manual/README.md`。
- [X] T004 [US1] 在 `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/SmtSecurityAreaSupplier.java` 与 `SmtSupplierPerson.java` 增加显式 0/1 逻辑删除字段，使既有单条/批量删除方法不再物理删除。

---

## Phase 3: User Story 2 - 管理端隐藏失效数据 (Priority: P1)

**Goal**: 所有当前管理端供应商读取结果都不包含失效供应商或人员。

**Independent Test**: 解析现有 Mapper 生成的 SQL，确认供应商树、分页、导出/通知、人员页及订单详情都具备正确的失效过滤。

- [X] T005 [US2] 在 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtSecurityAreaSupplierMapper.xml` 为分页、通知与树/下拉查询加供应商有效过滤。
- [X] T006 [P] [US2] 在 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtSupplierPersonMapper.xml` 为人员和所属供应商同时加有效过滤。
- [X] T007 [P] [US2] 在 `smart-module/smart-platform/smart-platform-core/src/main/resources/mapper/SmtSecurityAreaOrderMapper.xml` 的供应商左连接中排除失效供应商，保留订单本身。
- [X] T008 [US2] 运行 `SmtSecurityAreaSupplierSoftDeleteMapperXmlTest`，确认先失败后通过，并回写本文件的完成状态。

---

## Phase 4: Verification

- [X] T009 运行 `smart-module` 中的目标模块测试和编译验证，复核 `rg` 结果确认三份手写 Mapper XML 均已覆盖。
- [X] T010 以 `specs/003-supplier-soft-delete/quickstart.md` 核对需求、范围和未验证的生产 Oracle/PDA 边界。

## Dependencies & Execution Order

- T002 必须在 T004-T007 前完成并出现预期失败。
- T003 可与 T002 并行，但不执行生产数据库操作。
- T004 是 T005-T007 的逻辑删除基础。
- T005-T007 可并行修改不同 XML；本轮由主 Agent 串行集成以避免冲突。
- T008-T010 在所有实现任务后执行。

## Scope Guard

- 不修改 PDA、扫码、微信 H5 或基础资料普通供应商域。
- 不提交或执行生产 DDL、真实数据、数据库快照、配置或凭据。
