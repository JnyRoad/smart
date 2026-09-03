# 实施计划：手动下发权限增加起止日期

**分支**：当前隔离工作区 | **日期**：2026-09-03 | **规格**：[spec.md](spec.md)

## 摘要

为两条人工下发链路补齐一致的有效期：员工信息页的通关权限分配，以及权限组人员的批量粘贴授权。前端提供日期区间；后端验证和缺省归一化后写入关联记录。若多个权限组命中同一设备，任务采用最后一次授权的窗口覆盖此前窗口。到期由设备端权限窗口失效，不增加本地删除编排。

## 技术上下文

**语言/版本**：Vue 2.7 / Java 8。

**主要依赖**：Element UI、Vitest、Spring Boot、MyBatis-Plus、JUnit 4 / Mockito。

**存储**：Oracle；`SMT_STAFF_DEVICE_AUTH` 增加两列，`SMT_ISC_DEVICE_TASK` 复用既有 `START_TIME` / `OVER_TIME`。

**测试**：smart-ui Vitest；smart-platform Maven 单元测试。

**目标平台**：管理端浏览器、平台服务、ISC 设备授权接口。

**性能目标**：不改变现有批量上限和任务数量级；日期处理只增加常数时间校验。

**约束**：不执行真实 DDL、不连接生产数据库、不生成 action=12 延迟删除任务；起止时间传入任务时转换为秒。

**范围**：员工通关权限分配窗口、权限组人员批量粘贴窗口及其共用后端链路。

## 宪法检查

- 前后端边界：通过。前端仅提交日期字段；日期默认、校验和任务语义由平台服务保证。
- Oracle 约束：通过。仅新增展示字段，不变更查询算法或索引；SQL 由 DBA 在目标 schema 执行。
- 数据安全：通过。仓库只交付可审阅的正向和回滚前置 SQL，不执行真实 DDL/DML。
- 中文可维护性：通过。新增 JavaScript、Java 和 SQL 注释保持中文。
- 行为验证：通过。每个新增行为先以失败测试锁定，再写最小实现。

## 项目结构

```text
smart-ui/src/views/platform/basic/staff_info/        # 员工通关权限窗口
smart-ui/src/views/platform/area/limit/              # 三类权限组人员批量窗口
smart-module/smart-platform/smart-platform-api/      # 权限组请求/响应 DTO
smart-module/smart-platform/smart-platform-core/     # 实体、任务服务、Mapper
smart-module/smart-platform/smart-platform-biz/      # 控制器、服务、测试
smart-module/database/manual/                        # Oracle 正向与回滚前置脚本
```

**结构决定**：沿用两条现有 API 和共享权限组批量粘贴组件；不新建服务、接口或定时任务。

## 阶段 0：已确认的技术决策

见 [research.md](research.md)。所有默认值、日期语义和设备端到期策略已经由 5.3 蓝图锁定，不留未决技术问题。

## 阶段 1：数据和接口设计

- 数据字段与约束：见 [data-model.md](data-model.md)。
- HTTP 请求契约：见 [contracts/manual-permission-window.md](contracts/manual-permission-window.md)。
- 验证步骤：见 [quickstart.md](quickstart.md)。

## 实现顺序

1. 先写后端 DTO/服务测试，固定默认、日期倒置回滚和任务透传行为。
2. 以最小 Oracle 迁移和实体/Mapper/任务服务实现使测试变绿。
3. 写两处前端窗口测试，再添加共享日期表单、校验和请求字段。
4. 跑受影响模块测试和构建，最后在可控 ISC 环境做时间窗口联调。

## 风险与非范围

- 到期后本地关联记录仍存在；它不代表设备端仍可通行。这是 5.3 的既定边界。
- 多个权限组映射同一设备时不做日期区间合并或断档拒绝；按关联创建时间、同秒时按更大关联主键选择最后一次授权，避免阻塞人工操作。
- 实际 ISC `auth_config` 及设备到期行为必须在集成环境验证，自动化测试只能证明任务数据已正确生成。
- 重新下发不新增日期交互；其语义继续使用既有关系和下发链路。
