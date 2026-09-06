# Implementation Plan: 可靠权限删除与批量调度

**Branch**: `docs/reliable-auth-batch-design` | **Date**: 2026-09-05 | **Spec**: [spec.md](spec.md)

## Summary

实现蓝图 5.8 的可靠删除与万级批量处理。按依赖分两层：先修复现有 ISC 成功后收敛丢失、批内重复身份查询等无需改表的问题；随后以持久批次、目标、尝试和授权版本统一新增与撤权。第一层不能标记为 5.8 完成。

## Technical Context

- Java 8、Spring Boot 2.1、MyBatis-Plus、Oracle；界面 Vue 2 / Element UI。
- platform-core 持有领域状态、任务及 Mapper；platform-biz 持有入口、权限校验和业务来源；smart-schedule 执行有界调度及 ISC 适配；直连复用现有 bridge/dispatcher。
- 现有 schedule 已依赖 platform-core，可在同一数据库事务内提交任务结果和下发记录；跨服务操作使用持久待处理结果，不依赖瞬时 Feign 成功。
- 验证使用已有 JUnit/Mockito、Mapper 解析检查；真实 Oracle 事务/索引与真实设备能力单列验收。
- 主容量 10k/20k 展开后目标；100k 仅放大展开场景。受理 P95 2s、撤权首次提交 P95 30s 为待测门槛。

## Constitution Check

| 原则 | 处理与门槛 |
| --- | --- |
| 业务查询归后端 | 批次和目标分页由 platform 校验园区，前端不自行拼接授权归属 |
| Oracle 实证 | 用户已授权本机临时 Oracle 容器，使用合成 schema 核对事务、字段和查询计划；生产结构及规模仍未验证，不能仅据本地 Mapper 或压测承诺现网性能 |
| 真实数据与 DDL 分离 | 临时 Oracle 的建表及测试数据属于已有授权；不连接真实设备、不改生产库。迁移随发布记录交付，不新建人工 SQL 目录 |
| 中文与分层验证 | 测试先失败再实现，函数和流程使用中文注释 |
| 工作区 | 复用 9d51 linked worktree 与当前任务分支；此前蓝图改动属于本任务，保留 |

规划校验：无数据库结构依赖的实施可以开始。数据库适配阶段有明确前置，不代表现场验证已通过；后续不得以该前置阻止独立代码修复。

## Project Structure

```text
specs/012-reliable-auth-batch/
  spec.md, plan.md, tasks.md, research.md, data-model.md, quickstart.md
  contracts/permission-operations.md
  checklists/requirements.md
smart-module/smart-platform/smart-platform-core/src/{main,test}/java/com/tce/smart/platform/core/
smart-module/smart-platform/smart-platform-biz/src/{main,test}/java/com/tce/smart/platform/
smart-module/smart-schedule/src/{main,test}/java/com/tce/smart/schedule/
smart-ui/src/views/platform/area/limit/
```

## Implementation Phases

### A. 无 DDL 的现有链路修复

1. 在现有 ISC 所有成功出口统一调用独立 Spring 服务，任务条件更新与下发记录维护同事务；下发记录异常或保存失败必须回滚，保持可重查状态。不得把事务注解加在同类自调用私有方法上。
2. 缺人员/无配置数据仅有平台证据时保留核验/失败状态，不新增成功捷径。补对应行为测试及所有出口覆盖。
3. 删除身份解析在有界处理批次内复用同园区同稳定身份的员工资料，避免多设备重复远查；不全局缓存身份证明，不改变已存 task.personId 的优先级或权限窗口。
4. 单独报告 A 的收益：减少因本地收敛异常丢失删除状态和重复员工查询；未解决完整请求/版本/容量隔离之前不能宣称残留已彻底解决。

### B. 持久批次和版本基础

依据 [data-model.md](data-model.md) 固化真实表结构后，实现批次、删除请求、目标/尝试、授权版本和证据事件。用短事务、条件领取、令牌和租约维护排他性，按稳定选择集分段展开。

授权来源保留或撤销必须在主体协调锁内计算。跨批次相同操作幂等，旧 ADD/DELETE 结果不直接写当前授权；在途未知先核验再补偿。

### C. 入口与双接入调度

员工删除/清空先灰度；同轮加入新增、覆盖、重发的状态识别，避免旧入口绕过。推广离职、保密到期、访客作废和车辆时使用来源类型，不能伪造员工授权行。

ISC 保留兼容目标批量提交；分别控制提交速率、在途额度、回执预算，按实例总量分园区设备轮转。直连按设备能力使用独立额度与命令映射。离线、限流和重试延期后释放线程；只补未完成目标。

### D. 运维闭环与验收

实现可见批次、目标详情、失败重发、人工核验和告警；分钟扫描处理超时/待收敛，每日分段盘点历史残留。运行有界模拟，再在已确认测试环境执行 Oracle 和设备联调、10k/20k 容量测试。

## Global Constraints

- 任何入口不能把任务入库、取消、平台无条目或空列表当作设备删除成功。
- 未展开目标和未知结果不能被成功计数跳过。
- 保留新增、撤权和回执容量；借用资源不能占光撤权在途槽位。
- 同主体设备操作有序，其他目标受控并行；不等待设备时持有事务或工作线程。
- 全部本地测试不代替真实数据库/设备验收。未验证项保留为未完成任务。
- 不提交、不 push、不建 PR、不部署；主代理统一维护 tasks，子代理不覆盖其他文件。

## Execution Ownership

主代理负责规格、集成和交付。ISC 回执修复与批量身份解析按同一文件的顺序阶段执行，避免并发写 ISCDeviceTaskServiceImpl；其余独立模块可在明确契约后并行。
