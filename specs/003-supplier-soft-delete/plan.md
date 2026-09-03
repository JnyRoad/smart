# Implementation Plan: 保密供应商软删除

**Branch**: `003-supplier-soft-delete` | **Date**: 2026-09-02 | **Spec**: [spec.md](spec.md)

## Summary

将保密区供应商及供应商人员的现有删除动作改为逻辑删除，并在每一条手写 SQL 上显式排除失效记录。服务级人员读取使用有效供应商关联查询，人员写入与供应商删除锁定同一行以消除并发窗口。管理端不新增页面或接口；现有树、人员表、选择器、导出和通知候选通过原接口获得已过滤结果。PDA/扫码逻辑不在本次范围。

## Technical Context

**Language/Version**: Java 8、Vue 2；本次生产代码改 Java 实体、服务和 MyBatis XML。

**Primary Dependencies**: MyBatis-Plus 3.4.1、MyBatis、JUnit 4。

**Storage**: Oracle；`SMT_SECURITYAREA_SUPPLIER` 与 `SMT_SUPPLIER_PERSON` 各新增 `DEL_FLAG`，默认有效值为 `0`，删除值为 `1`。

**Testing**: JUnit 4；用 MyBatis 实际解析 XML 并取绑定 SQL，结合实体注解反射验证逻辑删除配置。

**Target Platform**: `smart-platform` 服务与现有 `smart-ui` 管理端。

**Performance Goals**: 不引入额外远程调用、批处理或全表扫描；常规读取仅增加逻辑删除谓词或必要的供应商关联，写入仅锁定一条供应商记录。

**Constraints**:

- 手写 XML 不受 MyBatis-Plus 逻辑删除自动过滤，必须显式加谓词。
- 人员写入与供应商删除必须在同一供应商行锁上串行化，避免已删除供应商仍产生有效人员。
- 生产 Oracle schema、索引和执行计划本地不可得，不新增未经实证的索引承诺。
- 不执行生产 DDL；只交付幂等正向/受保护回滚脚本。
- 不修改 PDA/扫码端、基础资料普通供应商域、协议到期与权限回收行为。

**Scale/Scope**: 两张表、三个自定义 Mapper XML、一个历史订单详情关联和现有管理端读取契约。

## Constitution Check

| 原则 | 结论 | 落实方式 |
|---|---|---|
| I. 展示与业务查询边界 | 通过 | 不在 Vue 端本地过滤；后端读取统一排除失效记录。 |
| II. Oracle 查询以实证为准 | 有发布前置条件 | 只新增谓词，不声明索引或性能收益；发布前由 DBA 核对 schema/计划。 |
| III. 真实数据与 DDL 分离 | 通过 | 仅提交脚本，禁止执行生产 DDL 或数据修复。 |
| IV. 中文可维护性 | 通过 | 新增字段、测试意图和发布脚本均为中文说明。 |
| V. 面向行为的分层验证 | 通过 | 先写会失败的注解/XML 契约测试，再写最小实体和 SQL 改动。 |

## Research Decisions

详见 [research.md](research.md)。核心决定：使用显式 `@TableLogic(value = "0", delval = "1")`，并在全部三份涉及表的 XML 中显式过滤；保密区订单详情仅隐藏已删除供应商的当前展示信息，不删除订单。

## Data Model

详见 [data-model.md](data-model.md)。`DEL_FLAG=0` 是有效记录，`DEL_FLAG=1` 是逻辑删除记录。

## Contracts

详见 [contracts/existing-supplier-reads.md](contracts/existing-supplier-reads.md)。所有现有 HTTP 路径和请求参数保持不变，响应只少掉失效记录。

## Project Structure

### Documentation (this feature)

```text
specs/003-supplier-soft-delete/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/existing-supplier-reads.md
└── tasks.md
```

### Source Code (repository root)

```text
smart-module/
├── database/manual/
│   ├── 20260902_add_supplier_soft_delete.sql
│   ├── 20260902_rollback_supplier_soft_delete.sql
│   └── README.md
└── smart-platform/
    ├── smart-platform-core/src/main/java/.../entity/
    │   ├── SmtSecurityAreaSupplier.java
    │   └── SmtSupplierPerson.java
    ├── smart-platform-core/src/main/resources/mapper/
    │   ├── SmtSecurityAreaSupplierMapper.xml
    │   ├── SmtSupplierPersonMapper.xml
    │   └── SmtSecurityAreaOrderMapper.xml
    └── smart-platform-core/src/test/java/.../mapper/
        └── SmtSecurityAreaSupplierSoftDeleteMapperXmlTest.java
```

**Structure Decision**: 在既有保密区供应商领域内实现。服务和 Controller 继续调用 MyBatis-Plus 的 `removeById`/`removeByIds`，实体逻辑删除配置改变其 SQL 语义；只扩展既有人员服务读取方法，不新增模块、接口或前端本地筛选。

## Implementation Phases

1. 先建立会失败的实体逻辑删除与 XML 过滤契约测试。
2. 增加两张表的字段发布/安全回滚脚本和实体逻辑删除字段。
3. 逐条修正供应商、人员和订单详情 XML 读取条件。
4. 运行核心模块测试，核对所有管理端读取接口仍采用已过滤的后端契约；不做 PDA 改动。

## Complexity Tracking

无宪法违例。本次不引入新模块、接口、配置或索引。
