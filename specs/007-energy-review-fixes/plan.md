# Implementation Plan: 园区能耗评审修复

**Branch**: fix/energy-projection-review | **Date**: 2026-09-05 | **Spec**: [spec.md](spec.md)

## Summary
在最新基线 157d4ca1 上修复读取契约、父级去重、过期重算、跨批初始化、回算失败与 OAuth 缓存/撤销一致性。开发起点 cb47dc74 已无冲突快进，不引入新服务或消息组件。

## Technical Context
Java 8 / Spring Boot 2.1 / Spring Security OAuth2 / MyBatis-Plus / Oracle / Redis。JUnit4、Mockito 与 MockMvc。
规模约 1000 块表，预算按表计日而非表数计算。生产 SQL 计划不可在本地取得，不承诺性能。

## Global Constraints
- 保留 server 为正常授权域，不新增逐模块 scope。
- 保留 GET /sd/statistics/month/{parkId} 用户入口；新增 GET /open/energy/month/{parkId} 应用入口，网关统一加 /platform。
- 新入口只接受 client_credentials + server，园区来自 token app_park_ids；不回退到请求参数或用户园区，不放宽全局拦截器。
- 显式 parentMeterId 缺父规则按 INVALID，不计入；未配任何规则的独立表仍 DEFAULT_INCLUDED。
- 现有 Oracle 表、日事实/明细/队列仍为业务依据；沿用幂等、租约及事务锁，不删除质量校验。
- 补齐应定期短批推进并报告进度，错误或旧规则/倍率事实可重算，活跃队列不反复重新排队；失败要可持久化恢复。
- OAuth 补偿不依赖单次方法成功返回的 CacheEvict，重复执行应安全；不输出 token/secret。
- 不修改生产；已有隔离 worktree 复用。提交/推送/PR 需独立授权（本地修复验收后已获用户授权提交 PR）；必要新增迁移只交付代码与步骤，不执行真实 DDL。
- 各任务先 RED 后 GREEN，禁止删有效断言或靠放宽超时通过。

## Constitution Check
I：范围校验在后端；II/V：优先复用已存在 Mapper 契约，新增 SQL 语义做契约/隔离测试，现场 Oracle 索引/计划验收未执行必须明确；III：本轮无真实 DB 操作；IV：新函数与复杂流程中文注释；VI：linked worktree 非 main。保持有效。

## Project Structure
- 应用读取：smart-module/smart-platform/smart-platform-biz 的新 EnergyUsageOpenController 与对应测试。
- 去重：同模块 service/energy/EnergyScopeDecision.java 及测试。
- 恢复：同模块 EnergyProjectionServiceImpl、core/mapper/energy、Mapper XML、schedule/SmartMeterTimerTask、必要 DTO 与进度持久化。
- 授权恢复：smart/smart-upms/smart-upms-biz 的 SysOauthClientDetailsServiceImpl、待办/补偿实现及测试。
- 验收资料：本 specs 目录。

## Phases
Task 1: 应用只读接口（FR-001），与 Task 2/3/4 独立。
Task 2: 父级规则断裂修复（FR-002），不修改投影 Service。
Task 3: 补齐与重算可靠性（FR-003/004/005），同一 worker 统一拥有调度/服务/Mapper，避免跨任务接口冲突。
Task 4: OAuth 编辑与吊销补偿（FR-006），独立基础平台模块。使用同库事务 outbox，使客户端变更和撤销待办原子提交；提交后先清共享缓存再调用认证服务，失败保留待办，后台重试。待办记录 NEXT_RETRY_AT，按到期时间稳定排序取有限批次，失败按 taskId 推迟重试，防止最早失败任务永久占满批次。
Task 5: 集成验证、独立评审与 quickstart（FR-007）。

## Complexity Tracking
不新建通用任务引擎。能耗进度复用 Redis 与现有表计日队列。OAuth 新增同库撤销待办表是必要例外：Redis PREPARED/READY 无法原子协调授权数据库提交，跳过 PREPARED 会遗漏“提交后崩溃”，重试 PREPARED 又可能“提交前清待办”。新增表只存客户端 ID、版本及重试所需元数据，不保存 token/secret，不重写历史迁移。建表前不能上线新版 UPMS，完整 DDL 作为本规格升级契约交付，不执行生产 DDL。
非 READY 本身不是无限重试条件，需要源/规则变化或有界重算周期，避免错误事实饿死全月补齐。

## PR #186 增量修订

- 审查基于 `e5611c77`：修正两处测试反射对 Mockito 子类形态的依赖，使用字段声明类；不改变运行时 OAuth 行为。
- 文档明确“本轮未执行生产写入/DDL”而非“功能没有写入”；新部署使用 `server` 默认值，已有显式历史调度 scope 保留并核对资源端兼容开关，迁移授权需单独确认。
- 保留现有日事实首次记录的园区快照，不因设备当前园区变化迁移历史用量；添加服务行为和真实 MyBatis SQL 展开契约测试及注释，防止未授权改写历史口径。历史归属纠正或生效日期模型不属于本次审查修复。
- 本次仅修复并推送同一 PR，不合并、不部署、不执行生产 SQL。
