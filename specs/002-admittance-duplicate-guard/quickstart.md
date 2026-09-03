# Quickstart: 入厂申请区域并发判重验证

## Prerequisites

1. 使用隔离的 Oracle 测试库，先由数据库负责人执行并复核 [data-model.md](data-model.md) 中的协调表定义；不要连接或写入生产库。
2. 准备一个有效被访人、至少 20 个彼此不同的测试证件号、两个不相同的区域 ID（例如 `1` 与 `11`）和可控预约时间。
3. 确认测试环境中不存在同证件号的有效重叠申请，或记录该基线。

## Automated checks

```bash
cd smart-module
mvn -pl smart-platform/smart-platform-core,smart-platform/smart-platform-biz -am test \
  -Dtest=SmtAdmittanceApplyMapperXmlTest,SmtAdmittanceApplyServiceImplTest \
  -Dsurefire.failIfNoSpecifiedTests=false
```

本工作区于 2026-09-02 从 `smart-module` 目录执行上述命令：核心模块 Mapper 测试 5 项；业务模块共 74 项（目标服务测试 65 项、同名历史服务测试 9 项）。两个目标模块合计 79 项，均为 0 failures / 0 errors。真实 Oracle 验收仍未执行。

## Functional checks

1. 主申请人同证件号、同区域、时间重叠：第二份提交被拒绝。
2. 已有随行人员与新申请主申请人/随行人员同证件号、同区域、时间重叠：新申请被拒绝。
3. 同证件号、时间重叠、区域没有交集：两份申请均可提交。
4. 区域 `1` 与 `11`：不得被视为同一区域。
5. 已拒绝、已作废或仅在时间端点相接的申请：不阻止新申请。
6. 对区域为空或格式异常的有效历史申请：同证件号、时间重叠的新申请被拒绝。

## Concurrent submission check

1. 使用两个独立数据库连接或两个独立 HTTP 客户端，在屏障放行后同时提交同一证件号、相同区域和重叠时间段的申请；每一轮使用本次验收中未出现过的独立测试证件号。
2. 连续运行 20 轮；每轮应恰好一个成功、一个返回重复申请或“当前证件号申请处理中，请稍后重试”的业务结果。
3. 每轮结束后只查询该轮证件号的测试数据，确认恰有一张有效申请。若测试环境必须复用证件号，必须先由数据库负责人按既有测试数据策略清理或作废上一轮申请；协调锁行不是申请业务数据，不应删除。

若任一轮出现两个请求同时成功、无限等待、死锁或非业务错误，停止发布并保留请求时间线与数据库错误信息供诊断。
