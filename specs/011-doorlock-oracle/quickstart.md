# 开发交接与验证入口

## 本轮能做什么

本轮交付是文档，两个 Java 模块尚未加入 reactor，不应运行未来模块命令后把“找不到模块”认作实现缺陷。没有建表、迁移或设备测试结果。

从当前检出的项目根目录阅读：

1. [spec.md](spec.md)：范围、22 项需求与 5 个用户故事。
2. [design-revisions.md](design-revisions.md) 和 [research.md](research.md)：哪些旧结论已失效、参考源码在哪。
3. [plan.md](plan.md)、[data-model.md](data-model.md)、[contracts/](contracts/)：实施边界。
4. [oracle-baseline.md](oracle-baseline.md)、[migration-mapping.md](migration-mapping.md)：真实数据库门禁。
5. [tasks.md](tasks.md)、[test-matrix.md](test-matrix.md)：测试先行开发与验收。

使用当前 worktree 的相对路径，不将其他机器的绝对 worktree 路径写入接口、配置或代码。外部参考源码只有 research 的稳定源目录可用；若不在本机，要求提供相同版本源码，不猜路径。

## 文档检查（现在可运行）

从当前 worktree 根运行，不写本机功能指针：

```bash
SPECIFY_FEATURE_DIRECTORY="$PWD/specs/011-doorlock-oracle" \
  bash .specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks --no-persist

git diff --check
git status --short
```

预期：解析到本目录，包含 spec/plan/tasks 及配套资料；无差异格式错误。注意 `git diff --check` 不覆盖未跟踪文件，交付前还应对新文档执行链接、任务 ID、FR/TEST 对照及空白检查。文档检查不代表业务测试通过。

## 开发与测试前置

- 复用本任务规格，后续在自己的非 main linked worktree 开发。文档尚未合并时，新 worktree 不会自动拥有本轮未提交文件；须先经授权提交/PR，或明确使用提供的任务分支资料。
- 按任务清单先补可失败的行为测试，不运行 `speckit-implement`。
- 本地 Oracle 实例/Schema、端口、Compose 名称及测试数据归属必须独立核实；不能让多 worktree 共用不受控的数据库或真实设备。
- 仅使用合成数据，未批准时不复制完整指纹、密码、人员信息或生产配置。没有真实数据库证据，不勾选数据库发布任务。

## 模块建立后才可用的验证命令

以下为未来实施命令，当前不执行。先核实模块已加入 `smart-module/pom.xml`，从 Maven reactor 根 `smart-module/` 运行：

```bash
mvn -pl smart-lock/smart-lock-biz -am test
mvn -pl smart-bridge-lock/smart-bridge-lock-biz -am test
mvn -pl smart-platform/smart-platform-biz -am test
```

Oracle 集成测试需使用实施阶段新增并明确启用的测试配置，禁止因未连接目标测试库而静默跳过后宣布通过。测试名称见 tasks，不能用 `-DskipTests` 代替测试。

Web 从 `smart-ui/` 运行现有脚本 `pnpm lint`、`pnpm test`、`pnpm build`；H5 从 `smart-h5/` 运行 `pnpm check`、`pnpm test`、`pnpm e2e`、`pnpm build`。依赖及环境先按各 README 配置。Web 端不假设已经有 `pnpm e2e` 脚本，浏览器对版按验收任务补齐。

## 分层验收场景

| 层次 | 执行场景 | 通过条件与不能证明的内容 |
| --- | --- | --- |
| 纯领域/模拟 | 入住 → 待下发 → 模拟确认 → 退宿 → 模拟撤权；再加入重复、乱序、迟到回执、槽位复用 | 结果可解释且无错误恢复权限；不证明实锁支持 |
| 本地 Oracle | 同事务回滚、并发领取、重启恢复、空值/时间/ID/分页/LOB、隔离读写权限 | 目标测试版本实测通过；不证明生产规模性能 |
| Web/H5 | 八组 Web 对版、跨园区拦截、本人身份篡改、修改密码确认中与退出清理 | 前后端身份边界正确；Mock 不证明生产网关畅通 |
| 迁移 | 同批重跑、中断恢复、缺主数据、未完成旧任务、无可靠水位、历史查询 | 无未解释差异、无设备发送；不自动取得生产迁移授权 |
| 隔离硬件 | 仅批准的独立通信域：各型号密码/指纹/删除/离线/重启/回执丢失 | 逐型号固件留证；一个型号成功不代表全量 |
| 现场切换 | 已批准窗口内停旧、最终迁移、全量改址回读、禁发校验、放行与应急回退 | 所有成员和在途状态有证据；每次现场操作另按授权执行 |

## 开发交付要求

每完成任务立即记录实际测试和变更，再勾选 tasks。不得因为其他项目曾有同名测试或 PR 合并就替本模块勾选。遇到规格错误先修正规格；环境未验证只暂停依赖任务，不能把其余工作一并标记完成。
