<!--
Sync Impact Report
- Version change: template → 1.0.0
- Added principles: frontend/backend boundary, Oracle query evidence, data-change safety,
  Chinese documentation, focused validation
- Removed sections: none
- Follow-up TODOs: none
-->
# Smart Constitution

## Core Principles

### I. 明确的展示与业务查询边界

`smart-ui` 只负责展示和提交既有接口契约；业务筛选、数据关联和园区可见范围必须由
`smart-platform` 后端负责。每项改动必须保持参数语义和既有调用方兼容，除非规格明确
声明了破坏性变更。

### II. Oracle 查询以实证为准

涉及 Oracle 的 SQL 必须先核对目标 schema 的字段、约束、索引、统计信息与可取得的
执行计划，不能只依据 Mapper 推断性能。保留前置通配符的模糊搜索不得以普通 B-tree
索引作为性能承诺；索引、统计信息或 SQL 变更必须有当前数据规模和计划证据。

### III. 真实数据与 DDL 分离

应用代码不得携带真实数据库凭据。生产 DML、DDL、统计信息刷新、索引创建及删除都需
当轮明确授权；只读诊断不应回显个人数据或凭据。数据完整性问题必须单独列明，不得在
功能修复中隐式清理。

### IV. 中文可维护性

项目新增或修改的业务逻辑、测试意图和非直观 SQL 必须使用准确中文说明。代码与注释
必须同步，避免把历史实现细节或无归属 TODO 当作注释保留。

### V. 面向行为的分层验证

改动必须先有能够失败的测试，再写最小实现。前端验证展示和请求契约，后端验证查询
语义与结果去重；无法在本地获得真实 Oracle 计划时，必须如实记录限制并提供现场
复核步骤。

## 技术与数据约束

管理端使用 Vue 2 与 Element UI；平台服务使用 Java 8、MyBatis/MyBatis-Plus 和 Oracle。
功能规格以当前源码和已确认蓝图为准。SQL 应保持 Oracle 兼容，分页和排序不应依赖
未验证的数据库方言行为。

## 交付约束

功能在独立分支完成并经 PR 合并。每次交付至少报告改动文件、已运行验证、未验证边界和
数据库操作状态；测试、构建产物、环境文件、日志、证书和数据库快照不得进入版本控制。

## Governance

本宪法约束 Smart 的 Spec Kit 规格、计划、任务和实现。修订必须说明变更原因、版本号和
对现有规格的影响；新增或重定义强制性原则按语义版本提升，所有 PR 评审必须检查其符合性。

**Version**: 1.0.0 | **Ratified**: 2026-09-02 | **Last Amended**: 2026-09-02
