# Implementation Plan: 裕慧家园门锁统一接入与 Oracle 数据基线

**Branch**: `docs/doorlock-oracle-baseline` | **Date**: 2026-09-05 | **Spec**: [spec.md](spec.md)

**Input**: 已确认的统一产品、Oracle、Web/H5 和全通信域切换要求。

## Summary

将门锁能力作为裕慧家园内部业务域重建：`smart-platform` 保留人员、住宿事实和对外业务入口，`smart-lock` 管理门锁资产、凭据、授权、命令及审计，`smart-bridge-lock` 负责设备协议和连接。新在线数据统一落 Oracle，不保留独立账号、人员主库或 MySQL 在线双写。

本轮只形成源码盘点、候选数据模型、迁移设计、契约和开发任务，不创建可运行模块、不执行建表或迁移。正式实施唯一任务源为 [tasks.md](tasks.md)。旧设计保留为历史参考，冲突处理见 [design-revisions.md](design-revisions.md)。

## Technical Context

| 项目 | 基线与约束 |
| --- | --- |
| 后端 | 沿用 Smart 的 Java 8、Maven、Spring Boot 2.1 / Spring Cloud Greenwich、MyBatis/MyBatis-Plus、Feign；本项目不顺带升级平台框架 |
| 协议参考 | 重建门锁工程 Java 8 / Boot 2.1.3 / Netty 4.1.32；只选择协议及可靠投递逻辑，不整包复制旧安全配置与数据模型 |
| 前端 | Web 使用现有 Vue 2 / Element UI / Avue；微信 H5 使用现有 Next.js 16 / React 19 / TypeScript；不开发原生 App，也不依赖另一个 `smart-client` 任务 |
| 存储 | 新在线业务、Outbox、命令和审计均使用 Oracle；源 MySQL 只用于切换前旧系统执行及受控迁移输入 |
| 消息机制 | 首版使用同库事务 Outbox + 可恢复投递；不增加新消息中间件，不把内存事件监听当可靠消息 |
| 运行环境 | 使用 Smart 既有网关、认证、UPMS、Nacos；设备服务初期单活，端口和连接能力在隔离环境验证 |
| 测试 | Java 行为单测与契约测试、真实本地 Oracle 集成测试、Web 测试、H5 Vitest/Playwright、隔离实锁与切换演练；层级结果分开报告 |
| 规模与性能 | 生产表规模、网关数、吞吐及延迟均未验证。由数据基线和压测任务采集后锁定验收阈值；不编造 TPS，不以 H2/MySQL 测试替代 Oracle |
| 未验证外部输入 | 目标版本/Schema/字符集/权限、旧真实 DDL 与运行版本、设备型号固件、历史保留与敏感数据处置批准。见 [oracle-baseline.md](oracle-baseline.md) 的责任与阻塞范围 |

上述外部输入不是仍待选择的产品架构：Oracle、统一主数据及切换策略已确定。缺少环境证据只阻塞对应数据库发布、真实数据和设备验证，不阻塞纯领域、契约、模拟协议及 UI 原型开发。

## Constitution Check

以 [宪法 1.1.1](../../.specify/memory/constitution.md) 检查；设计前后均采用以下约束，不申请例外。

| 原则 | 设计落实 | 放行证据 |
| --- | --- | --- |
| I 展示与业务查询边界 | Web/H5 通过平台业务接口；园区过滤、身份派生与关联在平台后端；门锁内部再验证受信调用和目标范围 | 契约测试和跨园区/本人越权测试，不靠隐藏按钮 |
| II Oracle 实证 | 本轮仅候选模型；字段、索引和 SQL 以当前目标 Schema 元数据与实际计划定版 | 真实 Oracle 证据未齐前不得声称 SQL、容量或性能通过 |
| III 数据与 DDL 分离 | 无真实凭据、生产数据或可执行生产 DDL；运行迁移另获精确授权 | 迁移批次批准、只读核验、独立 DDL 发布记录 |
| IV 中文可维护性 | 后续新业务、测试意图、状态机与 SQL 均有中文注释任务 | 逐任务代码审查 |
| V 分层验证 | 任务严格测试先行；文档只运行结构、链接、映射和一致性检查 | 模拟、Oracle、真机、现场分别留证 |
| VI 隔离与持久化 | 从已核实的 main 建本任务 linked worktree；规格随 PR 入库，不依赖本机 feature 指针 | 本轮不直接写 main、不自动提交；合并后再核实新 worktree 可读 |

## Project Structure

### Documentation

- 本目录：`spec.md`、本计划、`research.md`、`tasks.md`、`quickstart.md`、`checklists/requirements.md`。
- 数据设计：[source-inventory.md](source-inventory.md)、[data-model.md](data-model.md)、[migration-mapping.md](migration-mapping.md)、[oracle-baseline.md](oracle-baseline.md)。
- 契约与验收：[contracts/lifecycle-and-device.md](contracts/lifecycle-and-device.md)、[contracts/web-h5.md](contracts/web-h5.md)、[test-matrix.md](test-matrix.md)。
- 本轮文档核验与后续任务覆盖：[validation.md](validation.md)；其中不包含业务实现或现场放行结论。
- 各子项目 `docs/superpowers/doorlock/` 保留本模块入口和历史设计，通过相对链接指向本规格；不再维护第二份当前 tasks。

### Source Code（未来实现目标，本轮尚未创建）

```text
smart-module/
├── smart-platform/
│   ├── smart-platform-api/       # 既有主数据查询、兼容契约
│   └── smart-platform-biz/       # 住宿事务、Outbox、公开门锁入口、范围校验
├── smart-lock/
│   ├── smart-lock-api/           # 领域契约、事件、设备命令结果 DTO
│   └── smart-lock-biz/           # Oracle 模型、授权、持久命令、历史查询、迁移作业
└── smart-bridge-lock/
    ├── smart-bridge-lock-api/    # 内部协议投递/回执契约
    └── smart-bridge-lock-biz/    # Netty、编解码、连接、设备认证、回执关联
smart-ui/src/{api/lock,views/lock}/
smart-h5/src/{features/dorm,app/dorm/lock}/
```

每个 Java 服务按当前标准 `src/main/java/com/tce/smart/...`、`src/main/resources/mapper/`、`src/test/java/com/tce/smart/...` 组织。模块间只依赖 `api`，禁止 `biz` 相互依赖或跨模块直接改表。暂不增加无人复用的 core/common 模块。DDL 的具体发布载体在数据库基线阶段选择当前项目已批准方式，不新增可人工反复运行的脚本目录。

## 架构与职责

```text
现有 Web / H5 → Smart 网关与认证 → smart-platform（业务入口、园区/本人范围）
                                      ├─ 住宿事实 + Outbox（一个 Oracle 事务）
                                      └─ 内部契约 → smart-lock（授权、凭据、命令、审计）
                                                        ↓ 持久命令 / 幂等回执
                                                 smart-bridge-lock（初期单活）
                                                        ↓ 厂商协议
                                                    网关 ↔ 门锁
```

1. **统一主数据**：员工、园区、房间与住宿以平台当前事实为准。门锁记录稳定 ID、历史快照和设备编号映射；工号是业务属性，不能充当唯一终身身份。导入异常先隔离，不造第二份在线人员表。
2. **业务可靠性**：在所有住宿变更入口的实际提交事务内写 Outbox；HTTP 投递在事务外。事件持久记录 `eventId`、`aggregateId`、`aggregateVersion`、`membershipId`，消费者有唯一约束与版本保护。重复收到后返回既有结果；接收成功才标记投递完成。
3. **状态拆分**：Grant 表示业务资格，每设备凭据表示锁内目标与确认状态，Command 和 Attempt 表示执行证据。受理、发送、真实生效不是同一状态。授权状态采用 `PENDING_PROVISION / ACTIVE / PENDING_REVOKE / REVOKED / RECONCILIATION_REQUIRED`；完整迁移和转换以数据模型为准。
4. **撤权与补偿**：删除/退宿优先，旧新增迟到成功不能恢复资格。保留设备钥匙槽位、历史绑定和入住实例，避免删除新入住或复用槽位的合法凭据。跨房调宿默认旧房撤权确认后才发新房，离线/未知走应急人工处置；同房换床不重复发放。同一次有效入住内重新授权创建新 grant 版本，不复活已撤旧记录。多个有效授权共用设备凭据时，仅最后资格结束才删除该凭据。无法确定物理状态进入核验，不通过数据库状态强行宣布收敛。
5. **设备侧边界**：内部 UUID 不假设可写进旧协议；持久关联业务命令、尝试、协议任务号、设备、槽位和会话。进程内连接表不能作为多副本安全依据。数据库租约只能协调应用任务，不能提供旧硬件不存在的 fencing 或 exactly-once。
6. **前端行为**：旧页面八组操作逐项对版，但独立登录和不受控密码暴露不复刻。新安全状态、权限差异在差异清单明示；UI 不能把 HTTP 受理直接展示为下发完成。旧路由与 H5 接口通过兼容门面过渡。
7. **安全与审计**：公开路径遵循 Web/H5 契约；内部 bridge 不公开给浏览器。身份与园区从服务端派生；密码与指纹按最小化原则处理，必要可恢复材料使用批准的密钥机制，不复制旧固定密钥。

## 新旧共存与切换

开发期间生产仍由旧系统执行，管理员只使用 Smart。旧管理账号关闭与网络隔离是运维措施，不能声称仅关闭账号即可停止旧定时任务或已有连接。

只有确需提前上线新的业务事件链时，才启用 `LEGACY_ONLY` 兼容适配；同一业务只能选择一个执行方，不做逐锁/逐网关路由。新系统 `SHADOW` 仅比较计算结果，不创建可被生产 sender 消费的任务。兼容适配不能宣称旧 HTTP 成功等于锁已生效，也不能无界重放未知结果。

正式切换顺序：

1. 完成开发、真实 Oracle 验证、隔离设备验证、迁移与回退演练，确定全通信域成员。
2. 冻结门锁相关管理及住宿写入；仅在已验证的可靠排队路径具备时继续受理。保留经批准的应急通行。
3. 停止旧业务发送器、定时任务和设备接入；隔离旧通道并清点在途命令。必要时按设备能力清理网关队列或重启，不能盲发清空钥匙。
4. 在最终静止源上完成历史迁移与核验。可先做全量演练/预装载；若可靠增量水位不能证明完整，最终采用完整冻结快照与全量差异校验，不能只取 `update_time > 上次时间`。
5. 全量修改网关服务地址并逐一回读，启动新连接但保持发送关闭，校验资产、权限目标、会话和旧执行隔离。
6. 批准放开新发送，按最终有效住宿计算差异，不重放旧任务表。完成实锁验收后恢复冻结业务并核验排队事件。
7. 已产生新业务或物理权限变化后，回退必须处理增量和设备差异；不能只恢复 MySQL 或修改 IP。旧查询资料最终由 Oracle 历史查询替代，旧在线依赖退出。

## 迭代与放行

| 迭代 | 独立交付 | 进入下一步的条件 |
| --- | --- | --- |
| A 设计与数据基线 | 本轮规格、源码盘点、候选 Oracle 模型、契约、任务；补齐真实环境证据为后续任务 | 文档一致性通过；未验证输入有责任与阻塞范围 |
| B 最小闭环 | 合成人员入住 → Outbox → 授权 → 模拟确认 → 退宿撤权 | TDD、重放/回滚/乱序/重启测试通过；不接生产网关 |
| C 数据库与完整后端 | 本地 Oracle、持久投递、资产/凭据/记录/审计及所有住宿入口 | Oracle 方言、并发、事务、权限隔离测试通过 |
| D Web/H5 | 旧 Web 八组操作对版；当前 H5 本人入口替换 | 按钮/字段/弹窗与安全差异逐项验收；真实设备状态展示正确 |
| E 迁移与实锁演练 | 可重跑迁移、全历史查询、隔离型号矩阵及切换/回退演练 | 无未解释数据差异；无高风险撤权、未知命令及权限隔离缺陷 |
| F 正式切换 | 经另行批准的全通信域操作 | DBA、业务和运维放行证据齐备；实际执行另获授权 |

Web/H5 可在冻结契约后并行开发；迁移转换器可用合成数据并行开发；Oracle Mapper、数据发布和实锁结果依赖对应环境证据。UI 完成不替代底层可靠性，源码测试不替代生产运行版本确认。

## Complexity Tracking

无宪法例外。只增加两个明确职责的后端模块，不增加第二套人员/账号服务、不要求新消息中间件、不启动通用多厂商框架或多活设备集群。任务按可测试的最小能力拆分，不整包搬运旧工程。
