# 研究与决策记录

## 证据边界

本轮为源码级设计，不连接 MySQL/Oracle，不读取生产秘密、不验证现场设备；源码中的行为不能直接推断当前生产版本。所有时间点基线重新用于开发前都应检查漂移。

| 来源 | 本轮核实的基线 | 使用方式 |
| --- | --- | --- |
| Smart 主项目 | `/Users/lvtu/source/YUTO/smart`，main `157d4ca1060873c480644714965093b81b514c2e` | 当前技术栈、住宿入口、认证/权限、前端与规格工作流；不修改其用户未提交内容 |
| 重建门锁工程 | `/Users/lvtu/source/YUTO/smart-lock`，HEAD `e236793` | 优先参考可维护源码与已有可靠投递修复；不是直接复制的最终模块 |
| 最早反编译参考 | `/Users/lvtu/source/门锁系统/smart-jar/smart-lock-biz-decompiled`、`/Users/lvtu/source/门锁系统/smart-jar/device-server-biz-decompiled` | 解释原协议与旧业务；不得用旧缺陷覆盖重建版本的修复 |
| Web 对标 | `/Users/lvtu/source/门锁系统/smart-ui` | 旧构建产物/操作参考，结合已存页面清单；动态菜单和运行态仍需现场证据 |
| 当前前端设计 | [Web](../../smart-ui/docs/superpowers/doorlock/README.md)、[H5](../../smart-h5/docs/superpowers/doorlock/README.md) | 保留页面对版历史，新契约和统一身份优先 |
| 源码盘点 | [source-inventory.md](source-inventory.md) | 表、字段、状态与来源路径的详细证据 |

图谱按 Tier 2 使用：Smart 项目 `Users-lvtu-source-YUTO-smart`（代次 `2026-09-05T04:00:36Z`），重建门锁项目 `doorlock-smart-lock-reconstructed-20260804`（代次 `2026-09-05T04:34:36Z`）。涉及实体、关键调用及配置均用精确源码补证。Java 动态/继承调用不能靠“入边为零”证明没有调用方；前端构建、文档、被排除目录采用文件级证据。全量实体清单仍不是实际数据库完整表清单。

## R-01 统一产品，保留清晰内部模块

- Decision：只保留一套人员/园区/住宿事实和一套账号权限；门锁域与协议桥在 Smart 内独立模块，可独立进程部署。
- Rationale：合并产品并不要求将 TCP 长连接塞入平台业务 JVM；两个内部模块能隔离协议故障，也消除跨产品主数据复制。
- Alternatives considered：继续两套后台双向同步（维护重复）；将旧工程整体复制并保留登录/人员表（没有完成统一）；拆出更多暂时无人消费的服务（增加运维负担）。
- Evidence：平台仍有 `RemoteSmartLockService` 和 `ConnectLockServiceImpl`；当前 reactor 尚未注册两个目标模块。

## R-02 Oracle 作为唯一新在线数据库

- Decision：按候选模型重建 Oracle 领域表，不逐表把 `lk_` 机械改名后迁移；源字段保留映射账本与完整历史归档语义。
- Rationale：旧人员/房间副本与新平台事实必须收敛；授权、设备凭据、命令尝试与历史数据有不同约束。
- Alternatives considered：保留 MySQL 作为新系统门锁专库（不满足已确认要求）；Oracle/MySQL 双写（增加一致性路径）；用 H2 模拟通过代表 Oracle 已验证（不能证明方言与锁语义）。
- 未验证部分：真实 DDL、索引、规模、Schema、版本、字符集和发布机制，已转化为 [oracle-baseline.md](oracle-baseline.md) 的前置证据任务，不填假值。

## R-03 同事务 Outbox，明确重复投递

- Decision：平台住宿写事务同时落待处理事件；消费者以事件编号、入住实例与版本幂等落库；事务外可恢复投递。
- Rationale：当前异步 HTTP 不能证明事务提交后必达，也不能在业务回滚时撤回已经发出的设备操作。
- Alternatives considered：只加重试注解或 afterCommit 内存事件（仍有崩溃窗口）；同步设备调用纳入数据库事务（硬件不支持事务回滚）；先引入新的 MQ（不是解决本轮问题的必要条件）。
- 性能选择：领取和分页 SQL 待目标 Oracle 验证；不预先承诺某版本专用锁语法。

## R-04 重用修复后的协议知识，不重用独立系统边界

- Decision：参考重建工程的指纹分包、B8 22/24-hex 回执、取消、重试、低频恢复和迟到补偿；重写持久层和身份边界。
- Rationale：当前源码已超出早期“只有两个不可维护 jar”的状态，旧设计的“一律无远程指纹”和“失败绝不重试”不再是可靠结论。
- Alternatives considered：从最早反编译重新修所有缺陷（遗漏已修复路径）；直接把新旧所有状态视为相同（会把旧库标志误当成设备确认）。
- 保留限制：代码存在不代表每种型号/固件都支持；DB 任务租约不代表协议具备 fencing；初版桥单活，断连、恢复和未知结果专门测试。

## R-05 全通信域切换，不做单网关灰度

- Decision：开发和影子阶段新系统不能向生产发送；正式切换前停旧执行、冻结迁移、全量改址、回读验证，最后放开新发送。
- Rationale：一把锁可被多个网关到达；按网关选择新旧执行方不能隔离物理目标。
- Alternatives considered：单锁/单网关灰度（与已确认拓扑冲突）；上线即回放旧未完成任务（不能判断旧任务已产生的物理效果）。
- 恢复边界：切换后产生新状态时，回退包括业务增量与锁内差异，不只是恢复旧数据库。

## R-06 保留操作体验，统一服务端身份

- Decision：Web 八组旧操作逐项对版；H5 保持本人入口。外部业务接口和范围由平台负责，锁域和桥只暴露明确内部契约。
- Rationale：复刻操作不等于复刻独立登录、不受控明文或信任客户端工号。现有 H5 修改密码的 HTTP 成功反馈要拆成受理/真实确认状态。
- Alternatives considered：新造管理大屏替代旧页面（违背第一版要求）；让前端分别访问旧服务、新领域和桥（重新引入耦合）。
- 本轮不扩展 App，也不依赖正在另行设计的统一客户端底座。

## 数据库语义的版本化参考

以下为候选设计参考，并非已确认生产 Oracle/MySQL 版本：

- Oracle 19c 将零长度字符值视为 NULL，不能把数值 0 等同于 NULL；导入要明确空值规则并测试。[Oracle Nulls](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Nulls.html)
- 字符长度需显式考虑 CHAR/BYTE；数值精度、时间精度、时区及 LOB 要按字段用途选择并在目标库验证。[Oracle 19c Data Types](https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Data-Types.html)
- InnoDB 的 `INFORMATION_SCHEMA.TABLES.TABLE_ROWS` 是估计值，不得作为迁移精确行数验收。[MySQL 8.0 TABLES](https://dev.mysql.com/doc/refman/8.0/en/information-schema-tables-table.html)

## 决策与未决输入

产品与架构决策已明确；环境事实仍是 UNVERIFIED，不应冒充“研究已完成所以数据库可发布”。上线前必须补目标环境与设备证据。所有选择维持默认禁止真实发送；输入不齐时继续允许无关的离线任务。
