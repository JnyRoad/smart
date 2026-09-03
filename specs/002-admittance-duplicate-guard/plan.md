# Implementation Plan: 入厂申请区域并发判重

**Branch**: `detached@909de24a` | **Date**: 2026-09-02 | **Spec**: [spec.md](spec.md)

**Input**: `specs/002-admittance-duplicate-guard/spec.md`

## Summary

在正式提交入厂申请的同一事务中，先按全部非空证件号的稳定顺序取得并发协调锁，再以“全体申请人员、有效状态、开区间时间重叠、区域精确交集”判断重复申请；任一冲突即回滚。区域为空或格式异常的当前/历史有效申请按用户确认的保守策略视为与全部区域冲突。提交前检查保留为提示，但不取得长期锁也不授予提交资格。

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 8

**Primary Dependencies**: Spring Boot 2.1、MyBatis/MyBatis-Plus、Hutool、JUnit 4、Mockito

**Storage**: Oracle；既有 `SMT_ADMITTANCE_APPLY`、`SMT_ADMITTANCE_FELLOW`，新增 `SMT_ADMITTANCE_CERT_LOCK` 作为按证件号串行化的锁锚点

**Testing**: Maven 单元测试、MyBatis 绑定 SQL 契约测试；具备独立 Oracle 测试库后执行双事务并发验收

**Target Platform**: `smart-platform-biz` 服务与既有管理端/H5 提交入口

**Project Type**: Spring Cloud 业务微服务

**Performance Goals**: 只串行化共享证件号的提交；不同证件号不互相阻塞；已存在锁行等待超时返回明确的可重试业务失败；首次锁行唯一键竞争的实际等待上界由 Oracle 验收确认

**Constraints**: Oracle 兼容；不改变提交 API；禁止证件号部分匹配；真实 DDL 不在本工作区执行；历史区域未知必须保守拦截

**Scale/Scope**: 仅人员入厂申请的重复校验，涉及 Mapper、服务、单测、SQL 契约测试和蓝图说明；不改货车预约、设备下发或外部 OA 协议

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- 前后端边界：通过。提交参数和页面流程不变，最终校验留在 platform 服务端。
- Oracle 查询实证：条件通过。源码已证实新申请 `areaType` 以逗号拼接保存；实际库中历史区域格式、索引和执行计划仍须在发布前以只读方式复核。
- 真实数据与 DDL：通过（受限）。实现只提供可审核的表定义；不执行任何真实 DDL、DML、索引或统计信息操作。
- 中文可维护性：通过。新增类、方法、SQL 分支和测试意图均使用中文说明。
- 面向行为的分层验证：通过。先增加会失败的 Mapper/服务测试，再实现最小事务锁与判重逻辑；Oracle 并发验收列为发布前硬门槛。

## Project Structure

### Documentation (this feature)

```text
specs/002-admittance-duplicate-guard/
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
├── smart-platform-core/
│   ├── src/main/java/com/tce/smart/platform/core/mapper/
│   │   └── SmtAdmittanceApplyMapper.java
│   ├── src/main/resources/mapper/
│   │   └── SmtAdmittanceApplyMapper.xml
│   └── src/test/java/com/tce/smart/platform/core/mapper/
│       └── SmtAdmittanceApplyMapperXmlTest.java
└── smart-platform-biz/
    ├── src/main/java/com/tce/smart/platform/service/admittance/impl/
    │   └── SmtAdmittanceApplyServiceImpl.java
    └── src/test/java/com/tce/smart/platform/service/admittance/impl/
        └── SmtAdmittanceApplyServiceImplTest.java
```

**Structure Decision**: 锁协调和判重都只服务入厂申请保存，维持在既有 `SmtAdmittanceApplyMapper` 与 `SmtAdmittanceApplyServiceImpl` 边界内；不新增 API、控制器、前端页面或通用锁框架。

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| 新增并发协调表 | Oracle 对不存在的业务申请行没有可锁定对象；需要稳定的每证件号锁锚点，才能消除“先查后写”的竞态 | 仅在现有申请/随行表 `FOR UPDATE` 会漏掉首笔并发申请；仅提高事务隔离级别无法形成可审核的每证件号互斥协议 |
