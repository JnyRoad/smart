# smart-ui 重构「行为不变」验证标准

> 本文件是 smart-ui 所有重构 PR 的**验收清单来源**。配套方案见 `refactor-plan-v1.md`（v2）、`refactor-phase0-plan.md`。
> 核心立场（来自 Codex 评审条件 6）：**DOM 快照不能证明"行为不变"**。验证手段分"主"与"辅"，**主次不可颠倒**。

---

## 0. 一句话原则

一个重构 PR 能合并，当且仅当：它声称"行为不变"的部分，被下列**主手段**中相关项**自动或人工验证**为不变；它声称"删除"的部分，满足 §8 删除准入。**做不到验证，就不许动。**

---

## 1. 导出签名契约（主）

- **覆盖什么**：模块对外契约——命名导出清单、有无 default export、顶层副作用 import 的加载时机。
- **用什么**：Vitest 断言 `Object.keys(module)`、`module.default`、用 `vi.hoisted` 记录副作用 import 是否触发。
- **范式**：`src/util/util.contract.test.js`（Task 1）。
- **何时必须有**：拆分/重命名任何被多处 import 的模块前（尤其顶层有副作用 import 的，见 §2 关联）。

## 2. 路由装配契约（主）

- **覆盖什么**：动态菜单路由 + 静态追加路由的**最终装配产物与顺序**。`avue-router` 运行期先 `addRoutes(动态菜单)` 再 `addRoutes(PlatformRouter)`——顺序错会导致首次点菜单重定向到子页/白屏。
- **用什么**：mock `./platform/` 哨兵 + 假 router 记录 `addRoutes` 调用序列，断言 path/redirect/`meta.keepAlive`/子路由/两次 addRoutes 的顺序。
- **范式**：`src/router/avue-router.contract.test.js`（Task 2）。
- **何时必须有**：拆分 `router/platform/index.js` 或改动 `avue-router.js` 前。**仅对 `platform/index.js` 做静态数组 diff 不算通过。**

## 3. API 请求签名契约（主）

- **覆盖什么**：每个 api 函数传给 `request` 的 `url / method / params / data` 不变。
- **用什么**：mock `@/router/axios` 为 spy，`expect(request).toHaveBeenCalledWith({...})`。
- **范式**：`src/api/admin/user.test.js`（Task 3）。
- **何时必须有**：改动任何 `src/api/**` 文件，或重构调用它的页面时（防接口签名漂移）。

## 4. 纯函数单测（主）

- **覆盖什么**：从 god 组件里抽离出来的业务规则/校验/计算函数，输入→输出不变。
- **用什么**：常规 Vitest 单测，覆盖正常 + 边界 + 异常分支。
- **参考**：现有 `src/util/util.test.js`、`common-area-config.test.js`。
- **何时必须有**：把组件内联逻辑抽成独立函数/模块时（抽离前先对原行为补测，抽离后测必须仍绿）。

## 5. DOM 快照（辅，不可当主证据）

- **覆盖什么**：仅"粗暴的 DOM 结构回归"（整块元素消失/错位）。
- **用什么**：`@vue/test-utils` `shallowMount` + `toMatchSnapshot`。
- **明确不能证明**：Avue 表格列行为、权限分支、路由跳转、请求参数、交互结果——这些必须靠 §2/§3/§4/§6/§7。
- **定位**：辅助信号，快照变化只触发"人工确认是否预期"，**不作为行为不变的充分证明**。

## 6. 手工回归清单（主，针对业务页面）

- **覆盖什么**：自动化测不到的真实交互路径。
- **怎么做**：每个被重构的页面，PR 描述里**逐条列出**关键路径并人工过一遍：增 / 删 / 改 / 查 / 分页 / 批量操作 / 导入 / 导出 / 各弹窗 / 关键校验提示。
- **何时必须有**：任何 `src/views/**` 业务页面重构。

## 7. e2e 冒烟（主，端到端最低门槛）

- **覆盖什么**：系统"还能用"的最小闭环——登录 → 菜单加载 → 进入 1 个列表页 → 1 次查询。
- **用什么**：Playwright（待定测试后端环境，见 `refactor-phase0-plan.md` Track B）。
- **退化方案**：暂无测试后端时，至少保证"app 能 build + dev server 起得来 + 登录页渲染无致命 console 错误"。

## 8. 删除类变更准入（主，对一切"删除"）

适用：死代码、`util/lrz.all.bundle.js`、孤立组件/配置等。**准入证据，缺一不可：**
1. **静态引用检索归零**：ES `import`、`require`、**动态字符串路径**（模板/路由里 `@/views/...` 拼接、`require.context`）、全局 `window.tce.*` 访问——全部确认无引用。
2. `pnpm build` 通过。
3. 运行冒烟通过。
4. 独立 PR、可单独 revert。

任一证据不足 → **不删**，先标 `@deprecated` 观察一个版本周期。

## 9. 每个重构 PR 的 Definition of Done

- [ ] 本 PR 声称"行为不变"的部分，§1–§4 中相关契约测试已新增/已绿。
- [ ] 涉及业务页面的，§6 手工回归清单已在 PR 描述列出并执行。
- [ ] `pnpm test` 全绿。
- [ ] `pnpm check:lint-baseline` 通过（无文件新增 warning；基线见 `docs/lint-baseline.json`）。
- [ ] 涉及删除的，§8 四项准入证据齐全。
- [ ] 一个 PR 只做一件事，可单独 revert。
- [ ] **A 类行为变更（bug 修复、catch 上报、安全修复等）未夹带在结构重构 PR 里**（A 类另开窗口，走产品确认 + 灰度）。

---

## 附：质量门禁现状（事实备注）

- **`pnpm gate`：一键本地质量门禁（开发完跑这一条）。** 聚合执行 `vitest run` + `check:lint-baseline` + 6 个静态契约检查（admin-search / bundle / isc-card-fast-add-ui / isc-card-ui / isc-device-id-readonly / records-contracts），某步失败也跑完其余、最后汇总 ✓/✗ 清单，任一红则退出码 1。脚本见 `scripts/gate.mjs`，编排逻辑由 `scripts/gate.test.mjs` 单测兜底。
- `pnpm check:lint-baseline`：**warning ratchet**——只拦"某文件 warning 增加 / 新文件带 warning"，不负责清零历史 25881 条。**这是权威闸门，任何人都可直接调用，不依赖 git 钩子是否激活。**
- **本地 git 钩子当前不可靠（重要）**：本仓库是 monorepo，`smart-ui/` 是子目录而非独立 git 仓库；实测 `core.hooksPath` 指向主仓库 `smart/.git/hooks`（只有 `*.sample`，husky 未接管）。因此 `smart-ui/.husky/pre-commit`、`pre-push` 在该环境**不会自动触发**——只是 best-effort，**不能当作可靠安全网**。本地激活需在 `smart-ui/` 跑一次 `pnpm install`（husky `prepare`）并确认 `core.hooksPath` 指向 `smart-ui/.husky`；即便激活也能被 `--no-verify` 绕过。
- **结论：本项目刻意不上 CI**——远端为内网自建 git 无可用 runner、GitHub Actions 收费且不一定能内网访问，故**不接 CI**，改以本地手动跑 `pnpm gate` 作为提交前门禁，开发完跑一遍、在 PR 里贴结果。**诚实边界：本地门禁靠"约定执行"，非技术强制——可被遗漏或 `--no-verify` 绕过，可靠性依赖提交者自觉。** 这是在"无 CI 环境"下的务实取舍，不是等价于流水线强制。
- **已知边界 1**：守卫只统计 warning，不拦新增 eslint *error*（error 由 `pnpm lint` 体现）；若将来要把 error 纳入 push 门禁，再单独扩展。
- **已知边界 2（warning 跨文件迁移）**：Phase 1 拆分把带 warning 的代码从旧文件搬到新文件时，旧文件 warning 减少（无害），新文件 0→N 会被守卫拦下（保守误报，方向正确）。处理：**人工确认确为搬运、未劣化后**再 `node scripts/check-lint-baseline.mjs --update` 重置基线，不要无脑 `--update`，否则会把本该清的 warning 一起固化。
