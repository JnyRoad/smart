# Implementation Plan: 入厂申请随行人员搜索

**Branch**: `feat/incoming-fellow-search` | **Date**: 2026-09-02 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-incoming-fellow-search/spec.md`

## Summary

在入厂申请记录中保留既有筛选参数和分页接口，使姓名或证件号命中任一随行人员时返回其
主申请单。查询使用相关子查询表达“存在匹配随行人员”，从列表主查询中移除仅作筛选的
随行人员外连接，避免一对多关系扩大主申请单行数；前端保留既有简短筛选文案和参数。

## Technical Context

**Language/Version**: Java 8、JavaScript（Vue 2）

**Primary Dependencies**: Spring Boot、MyBatis/MyBatis-Plus、Vue 2、Element UI、Vitest

**Storage**: Oracle（仅读取既有 `SMT_ADMITTANCE_APPLY` 与 `SMT_ADMITTANCE_FELLOW`）

**Testing**: 后端 Maven Mapper 动态 SQL 契约测试；前端标签通过页面验收清单复核

**Target Platform**: 管理端 Web 与 smart-platform 服务

**Project Type**: 前后端管理系统

**Performance Goals**: 维持一张主申请单至多一条结果；不因随行人员关联放大默认列表行数

**Constraints**: 保持 Oracle 兼容、既有包含式搜索和接口参数；不执行数据库 DML/DDL、
索引创建或统计信息刷新

**Scale/Scope**: 实库快照中主申请约 7,776 行、随行人员约 9,621 行；只改一个 Mapper SQL
及其定向测试，管理端沿用既有简短筛选文案

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- 前后端边界：通过。前端保留既有字段与参数，查询逻辑留在 platform Mapper。
- Oracle 实证：通过。已核对表、列、索引、统计信息和无计划权限边界；前置通配符不新增
  无效 B-tree 索引。
- 真实数据与 DDL：通过。实现不访问生产数据库、不含 DML/DDL 或凭据。
- 中文可维护性：通过。SQL 结构变化以中文测试意图说明。
- 分层验证：通过。先写可失败的 SQL/前端契约测试，再做最小实现。

## Project Structure

### Documentation (this feature)

```text
specs/001-incoming-fellow-search/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
```text
smart-module/smart-platform/
├── smart-platform-core/src/main/resources/mapper/SmtAdmittanceApplyMapper.xml
└── smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/
    └── SmtAdmittanceApplyMapperXmlTest.java

smart-ui/src/views/platform/visitor/incoming_record/
├── index.vue
└── index-static.test.js
```

**Structure Decision**: 现有管理端页面提交既有查询参数；平台核心 Mapper 持有筛选 SQL。
不新增服务、路由、数据表或接口。

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
