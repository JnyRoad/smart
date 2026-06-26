# smart-ui 后续重构实施方案 v3

> 范围：`smart-ui/`（Vue2 + Element UI + Avue 管理后台）。
> 基线：以 `origin/main` 的 `e33b1646` 为当前主线事实；本地当前 worktree 仍是 detached HEAD，开工前必须先同步到 `main`。
> 原则：生产系统，行为零回归、功能零缺失；每个 PR 只做一件事，可独立验证、可单独回滚。

---

## 0. 本方案状态

本方案基于以下来源合并：

1. `docs/refactor-plan-v1.md` v2：Claude 与 Codex 已交叉评审收敛的总方案。
2. `docs/refactor-verification-standard.md`：行为不变验证标准。
3. `origin/main` 当前 SmartUI 提交历史与文件现状。
4. Codex 对当前剩余工作的复核。

本轮按要求尝试调用 `claude -p` 生成新一轮独立实施方案。第一次在 Claude 未登录时，命令在沙箱内和沙箱外均返回：

```text
Not logged in · Please run /login
```

完成登录后再次调用 Claude。默认 agent 模式会尝试读取文件并输出伪工具调用，因此最终用纯文本评审模式让 Claude 基于本方案摘要给出结论。Claude 新一轮结论为：

```text
AGREE（附条件）
```

Claude 提出的有效修订点：

1. `util/store.js -> storage.js` PR 的 checklist 必须明确禁止触碰 `eval()` 周边逻辑。
2. `isc_card_fast_add` 的 queue/paste 纯函数抽离必须先于 staff 编排抽离，二者禁止并行。
3. 路由 AST 指纹守卫必须接入 `pnpm gate`，否则只是“有脚本但无强制”的虚假安全感。

Codex 对 Claude 首轮“把路由指纹守卫放到路由拆分后”的建议提出反驳：这违反“先建网，后拆墙”。Claude 第二轮接受反驳，最终一致：**PR 2 必须先于 PR 3，且 PR 2 必须把指纹守卫接入 `pnpm gate`。**

---

## 1. 总体路线

后续不再按“大阶段”粗推，而按“可回滚 PR 队列”推进。优先级如下：

1. 先恢复工作区到最新 `main`，确保所有后续工作基于 `origin/main`。
2. 先做路由拆分安全网，再拆路由，因为它是 Phase 1 最大未完成项，也是后续页面重构风险的共同入口。
3. 再处理低风险兼容搬运：`util/store.js -> util/storage.js`。
4. 再做删除型 PR：`lrz.all.bundle.js`。删除型 PR 必须证据齐全，不准夹带重构。
5. 然后继续 B 类结构债：crud 去重、`isc_card_fast_add` 收尾、`bed_mng` 收尾。
6. 最后按风险顺序推广到其他 god file。
7. A 类行为变更全部另开 `fix/` 分支，不得混入结构重构 PR。

CI 平台暂不接。当前门禁以 `pnpm gate` 为主，必要时退化为：

```bash
pnpm test
node scripts/check-lint-baseline.mjs
```

涉及业务页面的 PR，PR 描述必须附手工回归清单。涉及删除的 PR，必须附删除准入四项证据。

进度维护规则：

- 每个实施 PR 合并后，后续执行者必须回到本文档对应 PR 小节追加真实 PR 号、合并提交和验证摘要。
- 如果执行中改变 PR 顺序或拆分粒度，必须先在本文档记录原因，再继续开新分支。
- 本文档只记录已验证事实，不写“预计已完成”“应该通过”这类推测状态。

---

## 2. 每个 PR 的固定流程

每个 PR 都执行同一套流程：

```bash
git fetch origin --prune
git switch main
git pull --ff-only origin main
git status --short
```

如果当前 linked worktree 无法切到 `main`，并出现类似错误：

```text
fatal: 'main' is already used by worktree at '<other-worktree>'
```

则不要移动或干扰其他 worktree，改用等价路径从远端主线直接创建本 PR 分支：

```bash
git fetch origin --prune
git switch -c test/smart-ui-platform-route-fingerprint origin/main
```

随后按当前 PR 使用下文列出的固定分支名创建分支，例如：

```bash
git switch -c test/smart-ui-platform-route-fingerprint
```

开发顺序固定为：

1. 先补表征测试或契约检查。
2. 跑新增测试，确认它锁住当前行为。
3. 做最小实现。
4. 跑相关测试。
5. 跑全量门禁。
6. 启动独立评审，只评审本 PR diff。
7. 修复明确问题后重跑门禁。
8. 提交、推送、建 PR、合并、回 main 拉最新。

推荐验证命令：

```bash
cd smart-ui
pnpm gate
```

若 `pnpm gate` 因本地 `pnpm` 入口异常不可用，记录原因，并退化为：

```bash
cd smart-ui
./node_modules/.bin/vitest run
node scripts/check-lint-baseline.mjs
node scripts/check-admin-search.js
node scripts/check-bundle-optimization.js
node scripts/check-isc-card-fast-add-ui.js
node scripts/check-isc-card-ui.js
node scripts/check-isc-device-id-readonly.js
node scripts/check-records-contracts.js
```

回滚策略：每个 PR 必须能用单个 revert 回退，不依赖后续 PR 才能恢复系统。

---

## 3. 未来 12 个 PR 队列

### PR 1: 同步主线与确认本地门禁

分支：`chore/smart-ui-sync-main-gate`

目标：不改业务逻辑，只确认本地工作区与 `origin/main` 对齐，并把后续执行基线写清楚。

文件范围：

- 可选修改：`smart-ui/docs/refactor-implementation-plan-v3.md`
- 不改 `src/`

执行：

```bash
git fetch origin --prune
git switch main
git pull --ff-only origin main
cd smart-ui
pnpm gate
```

DoD：

- 当前分支基于 `origin/main e33b1646` 或更新提交。
- 工作区干净。
- `pnpm gate` 通过；若不通过，先归类是环境问题、历史问题还是本次问题。
- 不产生业务代码 diff。

风险：低。  
回滚：文档型 PR 可直接 revert；如果没有文件变更，则不需要 PR。

完成状态：

- 已合并：PR #11 `docs(smart-ui): add refactor implementation plan`
- 合并提交：`2b13736534a7f0b506623b50ca343064fe3f944e`
- 验证摘要：`pnpm gate` 通过；仅更新实施方案文档，不改 `src/` 业务代码。

---

### PR 2: 路由拆分前的 AST 指纹守卫

分支：`test/smart-ui-platform-route-fingerprint`

目标：在真正拆 `src/router/platform/index.js` 前，增加专门锁定当前静态路由数组的指纹检查，补足现有 `avue-router.contract.test.js` 对“静态数组内容与顺序”的覆盖不足。

文件范围：

- 新增：`smart-ui/scripts/check-platform-router-fingerprint.mjs`
- 新增：`smart-ui/scripts/check-platform-router-fingerprint.test.mjs`
- 修改：`smart-ui/package.json`
- 修改：`smart-ui/scripts/gate.mjs`
- 修改：`smart-ui/scripts/gate.test.mjs`
- 可选新增：`smart-ui/docs/platform-router-fingerprint.json`

检查内容：

- 顶层 route 数量不变。
- 每个 route 的 `path` 序列不变。
- 重复 `path` 的出现次数和相对位置不变。
- 每个 route 的 `children.path` 序列不变。
- `redirect`、`name`、`meta.keepAlive` 等可静态提取字段不变。
- 动态 `component` 函数不做字符串重写，只校验对应导入标识和对象位置。

DoD：

```bash
cd smart-ui
pnpm exec vitest run scripts/check-platform-router-fingerprint.test.mjs
node scripts/check-platform-router-fingerprint.mjs
pnpm gate
```

强制要求：

- `scripts/gate.mjs` 必须包含 `platform-router-fingerprint` 步骤。
- `scripts/gate.test.mjs` 必须覆盖该步骤存在，避免后续误删。
- PR 2 合并前，PR 3 不允许开工。

风险：低。  
回滚：删除脚本和 package script 即可。

完成状态：

- 已合并：PR #12 `test(smart-ui): add platform router fingerprint guard`
- 合并提交：`b4851729317c1a8f63cc0d3f624b9e997d5b28c1`
- 验证摘要：`node scripts/check-platform-router-fingerprint.mjs` 通过；`pnpm test` 通过；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。

---

### PR 3: 拆分 `src/router/platform/index.js`

分支：`refactor/smart-ui-split-platform-router`

目标：把 897 行平台静态路由拆到按业务域命名的模块中，但保持最终导出的路由数组完全等价。

文件范围：

- 修改：`smart-ui/src/router/platform/index.js`
- 新增：`smart-ui/src/router/platform/records.js`
- 新增：`smart-ui/src/router/platform/basic.js`
- 新增：`smart-ui/src/router/platform/dormitory.js`
- 新增：`smart-ui/src/router/platform/device.js`
- 新增：`smart-ui/src/router/platform/security-area.js`
- 新增：`smart-ui/src/router/platform/recruit.js`
- 新增：`smart-ui/src/router/platform/vehicle.js`
- 新增：`smart-ui/src/router/platform/visitor.js`
- 新增：`smart-ui/src/router/platform/panel.js`
- 新增：其他实际从原文件提取出的业务域模块

关键设计：

- 不按“业务域整体 concat”直接重排。
- 若原文件业务域交错，则域模块导出具名 route 常量，`index.js` 按原始顺序重新组装。
- 允许同一域模块导出多个 route，但 `index.js` 的数组顺序必须等于原文件顺序。
- 不改任何 `path`、`name`、`redirect`、`component`、`children`。

DoD：

```bash
cd smart-ui
node scripts/check-platform-router-fingerprint.mjs
pnpm test src/router/avue-router.contract.test.js
pnpm gate
```

手工核对：

- 抽样打开 5 个高频菜单路径：基础资料、宿舍、设备、招聘、记录。
- 确认首次进入菜单不白屏、不 404。

风险：中高。主要风险是数组顺序变化和重复 path 覆盖。  
回滚：单 PR revert 恢复原 897 行文件。

完成状态：

- 已合并：PR #13 `refactor(smart-ui): split platform router by domain`
- 实际分支：`refactor/smart-ui-platform-router-split`
- 合并提交：`b924d8bf58b93b45e923aff823664a2185f126e4`
- 验证摘要：`node scripts/check-platform-router-fingerprint.mjs` 通过，锁定 90 个顶层路由和 11 组重复顶层 path；`pnpm test src/router/avue-router.contract.test.js` 通过；`pnpm test` 通过；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 手工核对：本轮未连接可用测试后端和浏览器会话，未执行 5 个高频菜单路径真实点击冒烟；PR 描述已如实记录该边界。

---

### PR 4: `util/store.js` 兼容重命名为 `storage.js`

分支：`refactor/smart-ui-rename-storage-util`

目标：把命名不准确的 `util/store.js` 搬到 `util/storage.js`，旧路径保留 re-export，不改变调用方。

文件范围：

- 新增：`smart-ui/src/util/storage.js`
- 修改：`smart-ui/src/util/store.js`
- 新增或修改：`smart-ui/src/util/store.contract.test.js`

边界：

- 本 PR 不修 `eval()`，因为它是 A 类行为变更。
- 旧路径至少保留一个版本周期。
- PR checklist 必须人工确认 diff 未改动 `getStore` 的 boolean 解析、`eval()` 表达式、返回值兼容行为。

DoD：

```bash
cd smart-ui
pnpm test src/util/store.contract.test.js
pnpm gate
```

风险：低。  
回滚：恢复旧文件内容，删除新文件。

完成状态：

- 已合并：PR #15 `refactor(smart-ui): rename storage utility compatibly`
- 合并提交：`8fd2ea6661d9441a71c219420f08265af68d8873`
- 验证摘要：`pnpm test src/util/store.contract.test.js` 通过；`pnpm test` 通过；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 边界核对：`getStore` 的 boolean 分支仍保留 `content = eval(obj.content)`，返回值兼容逻辑未改；`eval()` 安全问题继续保留在 A 类 `fix/smart-ui-storage-eval` 队列。

---

### PR 5: 删除 `lrz.all.bundle.js`

分支：`refactor/smart-ui-remove-lrz-bundle`

目标：删除 `smart-ui/src/util/lrz.all.bundle.js`，确认全站只走 npm 包 `lrz`。

文件范围：

- 删除：`smart-ui/src/util/lrz.all.bundle.js`
- 修改：`smart-ui/docs/lint-baseline.json`
- 可选修改：`smart-ui/docs/lint-baseline.md`

删除准入证据：

```bash
cd smart-ui
node scripts/check-bundle-optimization.js
rg "lrz\\.all\\.bundle|src/util/lrz|@/util/lrz" src public
pnpm build
pnpm gate
```

如果无法跑真实业务冒烟，退化为：

- `pnpm build` 通过。
- 本地 dev server 能启动。
- 登录页或入口页无致命 console error。
- PR 描述明确“无测试后端，未做真实登录冒烟”。

风险：中。删除第三方 bundle 一旦存在动态引用，构建未必覆盖到。  
回滚：单 PR revert 恢复文件。

完成状态：

- 已合并：PR #17 `refactor(smart-ui): remove bundled lrz copy`
- 合并提交：`4ab4a21322eb7bbe5821300babe34e5b730b4959`
- 实际变更：仅删除 `smart-ui/src/util/lrz.all.bundle.js`；未修改依赖、API、路由、组件或业务逻辑。
- 验证摘要：`node scripts/check-bundle-optimization.js` 通过；`rg -n "lrz\\.all\\.bundle|src/util/lrz|@/util/lrz" src public` 无匹配；`VUE_APP_PLATFORM_URL=http://platform.example.com VUE_APP_BASE_URL=http://api.example.com pnpm build` 通过；`pnpm gate` 通过（34 个测试文件、202 个测试用例、lint baseline 与静态契约检查全绿）。
- 冒烟边界：无可用测试后端，未做真实登录冒烟；退化验证为 `VUE_APP_PLATFORM_URL=http://platform.example.com VUE_APP_BASE_URL=http://api.example.com pnpm exec vue-cli-service serve --host 127.0.0.1 --port 8088` 启动成功，`curl -I http://127.0.0.1:8088/` 返回 HTTP 200。
- 独立评审：Claude 纯文本只读评审结论 `AGREE`；P2 提醒 `lrz` 依赖版本为 `^4.9.40`，本 PR 不夹带依赖锁定，因为删除前运行路径已经通过 `src/util/load-lrz.js` 动态加载 npm `lrz`。
- 边界核对：`docs/lint-baseline.json` 无 `src/util/lrz.all.bundle.js` 条目，本 PR 无需更新基线；A 类行为变更未夹带。

---

### PR 6: crud 配置去重库存与下一批 `_base.js`

分支：`refactor/smart-ui-crud-base-inventory`

目标：在 admin/business 已完成的基础上，盘点剩余 crud 配置重复块，并只抽一批低风险同域 `_base.js`。

文件范围：

- 修改：`smart-ui/src/const/crud/**` 中同域、同前缀、重复头部明确的文件。
- 新增：对应目录 `_base.js`。
- 新增：必要的导出契约测试或静态检查脚本。

执行策略：

- 先生成清单，按目录聚类。
- 一次只处理一个业务域。
- 只抽纯配置重复项，不碰远程校验、副作用 API、formatter 逻辑。

DoD：

```bash
cd smart-ui
pnpm gate
```

风险：中。Avue 配置变更容易影响列展示和校验。  
回滚：单 PR revert。

完成状态：

- 已合并：PR #19 `refactor(smart-ui): extract work crud base config`
- 合并提交：`b816759c8a334799a509fcba4b14fe2b6cb0ae9f`
- 实际范围：只处理 `platform/work` 业务域；新增 `smart-ui/src/const/crud/platform/work/_base.js` 和 `work-crud-base.test.js`，8 个 work crud 配置改为 `...baseTableOption` 后接原 `column`。
- 验证摘要：重构前先新增 `work-crud-base.test.js` 表征现有 8 个文件非 `column` 顶层配置；重构后 `pnpm test src/const/crud/platform/work/work-crud-base.test.js` 通过（9 个用例）；AST 对比 `origin/main` 确认 8 个 work crud 的 `column` 块源码完全未变；`git diff --check` 通过；`pnpm test` 通过（35 个测试文件、211 个测试用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：Claude 纯文本只读评审结论 `AGREE`；P2 提醒 `_base.js` 导出可增加直接断言，已采纳并补入测试。`Object.freeze(baseTableOption)` 被评估为可能改变可变性语义，本 PR 不夹带。
- 边界核对：未改 API、路由、视图、formatter、validator、业务文案或任何 A 类行为修复。

---

### PR 7: `isc_card_fast_add` 队列构造与粘贴解析收尾

分支：`refactor/smart-ui-isc-card-fast-add-queue-flow`

目标：继续瘦身 `isc_card_fast_add/index.vue`，把仍留在页面内的队列构造、粘贴解析、提交结果状态变更抽成纯函数模块。

文件范围：

- 新增：`smart-ui/src/views/platform/basic/isc_card_fast_add/queue-flow.js`
- 新增：`smart-ui/src/views/platform/basic/isc_card_fast_add/queue-flow.test.js`
- 修改：`smart-ui/src/views/platform/basic/isc_card_fast_add/index.vue`
- 可能修改：`smart-ui/scripts/check-isc-card-fast-add-ui.js`

可抽函数：

- `buildQueueRow`
- `buildInvalidQueueRow`
- `parsePasteText`
- 提交后 row 状态归并逻辑

边界：

- 不改消息文案。
- 不改 `saveStaffCard` 调用参数。
- 不补 catch 上报。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/basic/isc_card_fast_add/queue-flow.test.js
pnpm test src/views/platform/basic/isc_card_fast_add/flow.test.js
node scripts/check-isc-card-fast-add-ui.js
pnpm gate
```

手工回归：

- 单卡录入。
- 重复卡号提示。
- 粘贴 1 行、批量 200 行边界。
- 粘贴非法行。
- 批量提交成功、失败混合。

风险：中。该页仍是高频业务页。  
回滚：单 PR revert。

完成状态：

- 已合并：PR #21 `refactor(smart-ui): extract isc card queue flow`
- 合并提交：`61e420c543f6768cd9decac55691916bdeadf59b`
- 实际范围：新增 `smart-ui/src/views/platform/basic/isc_card_fast_add/queue-flow.js` 和 `queue-flow.test.js`；`index.vue` 保留原页面方法名作为 wrapper，把队列行构造、粘贴解析、提交状态 patch 委托给纯函数；同步更新 `smart-ui/scripts/check-isc-card-fast-add-ui.js` 的静态守卫定位。
- 验证摘要：先新增 `queue-flow.test.js` 并确认缺实现时红测；实现后 targeted tests 通过（`queue-flow.test.js`、`flow.test.js`、`index.test.js`，共 12 个用例）；`node scripts/check-isc-card-fast-add-ui.js` 通过；`git diff --check` 通过；`pnpm test` 通过（36 个测试文件、215 个测试用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：Claude 纯文本只读评审结论 `AGREE`，无 P0/P1；P2 关于队列响应式的担忧经核对不适用，本 PR 未把 `queue` 移出 Vue `data()`，页面流程测试仍覆盖队列更新。
- 边界核对：未改 API、路由、业务文案、粘贴上限、园区快照、提交参数或成功/失败消息归并语义；未夹带 A 类 bug 修复。

---

### PR 8: `isc_card_fast_add` 搜人/卡片读取服务编排收尾

分支：`refactor/smart-ui-isc-card-fast-add-staff-flow`

目标：把页面内搜索员工、选择员工、读取员工卡片、刷新任务列表的编排收束到更小的页面方法或独立服务包装，进一步降低 `index.vue` 体积。

文件范围：

- 新增：`smart-ui/src/views/platform/basic/isc_card_fast_add/staff-flow.js`
- 新增：`smart-ui/src/views/platform/basic/isc_card_fast_add/staff-flow.test.js`
- 修改：`smart-ui/src/views/platform/basic/isc_card_fast_add/index.vue`

边界：

- 不改变 badge/name 搜索优先级。
- 不改变无结果、多结果、精确命中的用户提示。
- 不改变 loading 开关时机。
- 必须在 PR 7 合并并同步 `main` 后才能开工，禁止与 PR 7 并行。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/basic/isc_card_fast_add/staff-flow.test.js
pnpm test src/views/platform/basic/isc_card_fast_add/index.test.js
node scripts/check-isc-card-fast-add-ui.js
pnpm gate
```

手工回归：

- 工号精确搜索。
- 姓名搜索单结果。
- 姓名搜索多结果。
- 无结果。
- 切换园区后状态清空。

风险：中。  
回滚：单 PR revert。

完成状态：

- 已合并：PR #23 `refactor(smart-ui): extract isc card staff flow`
- 合并提交：`f890e0ecac37bffb941486d5aadd14d2c19b6087`
- 实际范围：新增 `smart-ui/src/views/platform/basic/isc_card_fast_add/staff-flow.js` 和 `staff-flow.test.js`；`index.vue` 保留原页面方法名与 UI 副作用，把最近任务查询参数、姓名搜索结果判定、工号搜索 fallback 编排、员工卡片异步防陈旧判断委托给纯函数；同步更新 `smart-ui/scripts/check-isc-card-fast-add-ui.js`，让静态守卫检查新的模块边界。
- 验证摘要：先新增 `staff-flow.test.js` 并确认缺实现时红测；又用红测锁定工号未命中后 fallback 到姓名搜索时重新读取当前园区的旧时序；实现后 targeted tests 通过（`staff-flow.test.js`、`index.test.js`、`flow.test.js`，共 15 个用例）；`node scripts/check-isc-card-fast-add-ui.js` 通过；`git diff --check` 通过；`pnpm test` 通过（37 个测试文件、222 个测试用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：向 Claude CLI 外发 staged diff 被系统按私有源码外传风险拒绝；改用本地 Codex 子代理只读评审当前 staged diff，结论 `AGREE`，无 P0/P1，确认 park fallback 时序、badge/name 选择规则、loading 与 stale guard 语义保持一致。
- 边界核对：未改 API、路由、业务文案、员工搜索提示、员工卡片删除逻辑、队列提交逻辑或任何 A 类 bug 修复。

---

### PR 9: `bed_mng` 删除遗留死弹窗状态与补齐组件接线

分支：`refactor/smart-ui-bed-mng-cleanup-after-dialog-split`

目标：在已抽 3 个编辑弹窗后，继续清理页面中残留的死状态和旧方法，确保 `index.vue` 不再保留已组件化弹窗的旧表单字段。

文件范围：

- 修改：`smart-ui/src/views/platform/dormitory/bed_mng/index.vue`
- 修改或新增：`smart-ui/src/views/platform/dormitory/bed_mng/*.test.js`
- 可能修改：`smart-ui/src/views/platform/dormitory/bed_mng/components/*.test.js`

边界：

- 不改入住、退宿、换宿、导出业务逻辑。
- 不改 API 请求签名。
- 不删除仍被模板引用的状态。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/bed_mng/bed-rules.test.js
pnpm test src/views/platform/dormitory/bed_mng/components/dlg_edit_time.test.js
pnpm test src/views/platform/dormitory/bed_mng/components/dlg_edit_remark.test.js
pnpm test src/views/platform/dormitory/bed_mng/components/dlg_edit_check_in.test.js
pnpm gate
```

手工回归：

- 编辑入住日期。
- 编辑备注。
- 编辑非员工入住信息。
- 查询、重置、分页。

风险：中。  
回滚：单 PR revert。

完成状态：

- 已合并：PR #25 `refactor(smart-ui): remove bed mng dead dialog state`
- 合并提交：`9bcec3b47ee4d3f88c0dfa76c62d2ad9c928e657`
- 实际范围：新增 `smart-ui/src/views/platform/dormitory/bed_mng/index-static.test.js`；删除 `index.vue` 中已确认零引用的 `checkInLoading`、父级 `obj`、`rangIDTemp` 三个残留 `data()` 字段；保留三个已拆分编辑弹窗的父级 row/visible/refresh 接线。
- 验证摘要：先新增静态测试并确认残留字段存在时红测；删除字段后 targeted tests 通过（`index-static.test.js`、`bed-rules.test.js`、三个编辑弹窗组件测试，共 24 个用例）；`git diff --check` 通过；`pnpm test` 通过（38 个测试文件、224 个测试用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：本地 Codex 子代理三轮只读评审；前两轮 P2 分别要求放宽静态测试的标签格式耦合、改用 ESM 测试里的 `fileURLToPath(import.meta.url)` 路径写法，均已修复；最终评审结论 `AGREE`，无 P0/P1/P2。
- 边界核对：未改 API、路由、入住、退宿、换宿、导出、备注、修改入住时间、编辑非员工入住逻辑；未夹带 `tableLoading` 等潜在行为修复或 A 类 bug 修复。

---

### PR 10: `room/list.vue` 试点安全网

分支：`test/smart-ui-room-list-safety-net`

目标：在继续拆下一批大页面前，先给 `views/platform/dormitory/room/list.vue` 建安全网，不动业务逻辑。

选择理由：

1. 当前仍约 1385 行，结构债重。
2. 属于宿舍域，已经有 `bed_mng` 试点经验可复用。
3. 主要依赖 `room.js`、`dormitory.js`、`floor.js` 三组 API，边界比 `resume/info.vue` 更清楚。

文件范围：

- 新增：`smart-ui/src/views/platform/dormitory/room/list.test.js`
- 新增：`smart-ui/src/api/platform/dormitory/room.test.js`
- 新增：`smart-ui/src/api/platform/dormitory/dormitory.test.js`
- 新增：`smart-ui/src/api/platform/dormitory/floor.test.js`
- 新增：`smart-ui/scripts/check-room-list-ui.js`

安全网内容：

- 页面浅挂载：搜索区、树、表格、导入/导出按钮、核心弹窗入口存在。
- API 请求签名：锁定 `fetchRoomList`、`floorList`、`delObj`、`putObj`、`putBatchObj`、`putSDBatchObj`、`putDormObj`、`addObj`、`delFloor`、`addFloor` 等高频函数的 `url/method/params/data`。
- 静态检查：核心按钮事件名和 `avue-crud` slot 名不丢。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/list.test.js
pnpm test src/api/platform/dormitory/room.test.js
pnpm test src/api/platform/dormitory/dormitory.test.js
pnpm test src/api/platform/dormitory/floor.test.js
node scripts/check-room-list-ui.js
pnpm gate
```

风险：低。  
回滚：删除新增测试和脚本。

完成状态：

- 已合并：PR #27 `test(smart-ui): add room list safety net`
- 合并提交：`5c7420f50b63db1688daf41487ec9bc913d8a2e0`
- 实际范围：新增 `room`、`dormitory`、`floor` 三组宿舍 API 契约测试；新增 `views/platform/dormitory/room/list.test.js` 浅挂载安全网；新增 `scripts/check-room-list-ui.js` 静态守卫并接入 `scripts/gate.mjs`；未改业务代码。
- 验证摘要：`node scripts/check-room-list-ui.js` 通过；新增 room/list 安全网专项测试通过（4 个测试文件、15 个用例）；`pnpm test scripts/gate.test.mjs src/views/platform/dormitory/room/list.test.js src/api/platform/dormitory/room.test.js src/api/platform/dormitory/dormitory.test.js src/api/platform/dormitory/floor.test.js` 通过（5 个测试文件、20 个用例）；`pnpm test` 通过（42 个测试文件、239 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`git diff --check` 通过；`pnpm gate` 通过。
- 独立评审：本地 Codex 子代理两轮只读评审；第一轮 P1 要求静态守卫限定 `getList(params)` 方法块、P2 要求补足 created 阶段默认状态断言，均已修复；第二轮评审结论 `AGREE`，无 P0/P1/P2。
- 边界核对：未改 API 实现、`room/list.vue`、路由、业务逻辑、导出、批量编辑、宿舍楼、楼层或房间操作。

---

### PR 11: `room/list.vue` 第一刀：抽纯规则

分支：`refactor/smart-ui-room-list-extract-rules`

目标：只抽 `room/list.vue` 内不依赖 `this` 的纯规则，不抽组件、不改 API、不改 UI。

文件范围：

- 新增：`smart-ui/src/views/platform/dormitory/room/room-rules.js`
- 新增：`smart-ui/src/views/platform/dormitory/room/room-rules.test.js`
- 修改：`smart-ui/src/views/platform/dormitory/room/list.vue`

可抽类型：

- 状态文字映射。
- 表单输入归一化。
- 查询参数构造。
- 表格行 class 计算。
- 不依赖 `this` 的校验规则。
- 导出表头和字段映射的纯配置生成。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/room-rules.test.js
pnpm test src/views/platform/dormitory/room/list.test.js
pnpm gate
```

手工回归：

- 查询。
- 重置。
- 分页。
- 页面内关键按钮打开。

风险：中低。  
回滚：单 PR revert。

完成状态：

- 已合并：PR #30 `refactor(smart-ui): extract room list rules`
- 合并提交：`5d5719fe502c90adda896deb74252400e6d7e777`
- 实际范围：新增 `smart-ui/src/views/platform/dormitory/room/room-rules.js` 和 `room-rules.test.js`；`room/list.vue` 保留原方法名和模板绑定，把 `hasData`、房间属性 class、树节点过滤、列表查询参数、全选/半选状态、导出枚举格式化、批量编辑空表单判断委托给纯函数；同步更新 `scripts/check-room-list-ui.js`，让静态守卫检查新的 `buildRoomListQuery` 调用边界。
- 验证摘要：先新增 `room-rules.test.js` 并确认缺实现时红测；实现后 `pnpm test src/views/platform/dormitory/room/room-rules.test.js src/views/platform/dormitory/room/list.test.js` 通过（2 个测试文件、11 个用例）；`node scripts/check-room-list-ui.js` 通过；`pnpm test` 通过（43 个测试文件、248 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`git diff --check` 通过；`pnpm gate` 通过。
- 独立评审：本地 Codex 子代理只读评审 staged diff，结论 `AGREE`，无 P0/P1/P2；确认查询参数、导出枚举原地修改、选择状态和批量编辑空表单判断保持旧行为。
- 边界核对：未改 API 实现、接口签名、模板事件绑定、页面文案、弹窗流程、路由、导出字段或任何 A 类 bug 修复。

---

### PR 12: `room/list.vue` 第二刀：抽查询工具栏组件

分支：`refactor/smart-ui-room-list-extract-search-toolbar`

目标：在 PR 11 基础上，只抽 `room/list.vue` 的查询工具栏和顶部按钮区，复用 `isc_card_fast_add/PageToolbar.vue` 的 props/events 模式。

文件范围：

- 新增：`smart-ui/src/views/platform/dormitory/room/components/RoomSearchToolbar.vue`
- 新增：`smart-ui/src/views/platform/dormitory/room/components/RoomSearchToolbar.test.js`
- 修改：`smart-ui/src/views/platform/dormitory/room/list.vue`

选择标准：

- props/events 能说清楚。
- 内部不直接改父级复杂状态。
- 不直连 API，API 调用仍留父组件或已有 service。
- 抽完父组件减少至少 80 行。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/components/RoomSearchToolbar.test.js
pnpm test src/views/platform/dormitory/room/list.test.js
pnpm gate
```

手工回归：

- 查询按钮仍触发列表查询。
- 重置按钮仍清空搜索条件并刷新列表。
- 导入入口仍只在选中宿舍树节点后打开。
- 两个导出按钮仍触发原导出流程。

风险：中。  
回滚：单 PR revert。

完成状态：

- 已合并：PR #32 `refactor(smart-ui): extract room search toolbar`
- 合并提交：`bf7d988c675373d9192ce54904295862d44985e4`
- 实际范围：新增 `RoomSearchToolbar.vue` 和 `RoomSearchToolbar.test.js`；`room/list.vue` 保留搜索、清空、导出、批量设置房间类型、批量设置房间水电模板等父级方法与 API 调用，只把查询工具栏和搜索表单渲染下沉到子组件；同步增强 `room/list.test.js` 和 `scripts/check-room-list-ui.js`，锁住 toolbar props/events、字段更新、清空 reset 链路和核心按钮字段。
- 验证摘要：先新增 `RoomSearchToolbar.test.js` 并确认组件缺失时红测；实现后 `pnpm test src/views/platform/dormitory/room/components/RoomSearchToolbar.test.js src/views/platform/dormitory/room/list.test.js` 通过（2 个测试文件、7 个用例）；`node scripts/check-room-list-ui.js` 通过；`node scripts/check-lint-baseline.mjs` 通过；`git diff --check` 通过；`pnpm gate` 通过。
- 独立评审：本地 Codex 子代理三轮只读评审；第一轮 P2 要求补齐清空 `resetFields` 链路测试和静态守卫，第二轮 P1 要求删除无用 `resetFrom(formName)` 参数，均已修复；第三轮评审结论 `AGREE`，无 P0/P1/P2。
- 边界核对：未改 API 实现、接口签名、路由、导出字段、批量编辑 API、弹窗流程、页面文案或任何 A 类 bug 修复；搜索表单 label 宽度样式已迁入子组件，避免 scoped CSS 迁移后的布局回归。
- 计划偏差：原小节写“抽完父组件减少至少 80 行”，实际 `list.vue` 统计为 `+23/-42`，净减少 19 行。为保持 reset、props/events 接线和样式迁移显式可测，本 PR 未继续追求行数指标；后续拆分不能把该 80 行目标视为已达成。

### PR 13: `room/list.vue` 第三刀：抽左侧宿舍树组件

分支：`refactor/smart-ui-room-list-extract-tree`

目标：只抽 `room/list.vue` 左侧楼栋/楼层树到独立组件，父页继续保留 `handleNodeClick`、`treeNodeOption`、API 查询和所有弹窗提交逻辑。

文件范围：

- 新增：`smart-ui/src/views/platform/dormitory/room/components/RoomTreePanel.vue`
- 新增：`smart-ui/src/views/platform/dormitory/room/components/RoomTreePanel.test.js`
- 修改：`smart-ui/src/views/platform/dormitory/room/list.vue`
- 修改：`smart-ui/src/views/platform/dormitory/room/list.test.js`
- 修改：`smart-ui/scripts/check-room-list-ui.js`

边界：

- 不改 `floorList`、`fetchRoomList`、楼栋/楼层/房间增删改接口调用。
- 不改 `handleNodeClick`、`treeNodeOption` 的业务实现，只通过组件事件转发原参数。
- 不改搜索工具栏、房间列表、导出、批量编辑和各弹窗流程。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/components/RoomTreePanel.test.js
pnpm test src/views/platform/dormitory/room/components/RoomTreePanel.test.js src/views/platform/dormitory/room/components/RoomSearchToolbar.test.js src/views/platform/dormitory/room/list.test.js src/views/platform/dormitory/room/room-rules.test.js
node scripts/check-room-list-ui.js
pnpm gate
```

手工回归：

- 楼栋/楼层关键字过滤。
- 点击园区、楼栋、楼层后范围切换。
- 编辑楼栋/楼层。
- 删除楼栋/楼层。
- 新增楼栋/楼层。

风险：中低。树操作入口多，但父页业务方法未迁移，自动测试和静态守卫锁住了 props/events。
回滚：单 PR revert。

完成状态：

- 已合并：PR #42 `refactor(smart-ui): extract room tree panel`
- 合并提交：`e295819c3b079d63594d54f34e8b48e47faf7c0e`
- 实际范围：新增 `RoomTreePanel.vue` 和 `RoomTreePanel.test.js`；`list.vue` 只把左侧树模板替换为 `<room-tree-panel>`，父页继续持有 `handleNodeClick`、`treeNodeOption`、查询、导出、批量编辑和弹窗提交逻辑；`scripts/check-room-list-ui.js` 改为分别守卫父页组件接线和子组件树绑定/按钮事件。
- 验证摘要：先新增 `RoomTreePanel.test.js` 并确认组件缺失时红测；实现后 `pnpm test src/views/platform/dormitory/room/components/RoomTreePanel.test.js` 通过（3 个用例）；room 相关 4 个测试文件通过（19 个用例）；`node scripts/check-room-list-ui.js` 通过；新组件 eslint 零 warning；`git diff --check` 通过；`pnpm test` 通过（47 个测试文件、263 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：Claude 纯文本只读评审结论 `AGREE`，确认树过滤、`node-click`、编辑/删除/新增事件、`node.parent.parent` 分支、scoped 样式迁移和父页业务边界无 P0/P1/P2。
- 手工回归边界：本轮无可用测试后端和浏览器会话，未执行真实页面点击回归；PR 描述已如实记录该边界。
- 边界核对：未改 API 实现、接口签名、列表查询、房间增删改、楼栋/楼层增删改、导出、批量编辑、弹窗流程、页面文案或任何 A 类 bug 修复。

### PR 14: `room/list.vue` 第四刀：抽房间卡片列表组件

分支：`refactor/smart-ui-room-list-extract-grid`

目标：只抽 `room/list.vue` 房间卡片列表区到独立组件，父页继续持有选择状态、编辑/删除房间方法、API 查询、导出、批量编辑和所有弹窗提交逻辑。

文件范围：

- 新增：`smart-ui/src/views/platform/dormitory/room/components/RoomGridPanel.vue`
- 新增：`smart-ui/src/views/platform/dormitory/room/components/RoomGridPanel.test.js`
- 修改：`smart-ui/src/views/platform/dormitory/room/list.vue`
- 修改：`smart-ui/src/views/platform/dormitory/room/list.test.js`
- 修改：`smart-ui/scripts/check-room-list-ui.js`

边界：

- 不改 `fetchRoomList`、`putObj`、`putBatchObj`、`putSDBatchObj`、`delObj` 等 API 调用。
- 不改 `checkAllChange`、`roomChange`、`handleEdit`、`rowDel` 的业务实现，只通过组件事件转发原参数。
- 不改搜索工具栏、左侧树、导出、批量编辑和各弹窗流程。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/components/RoomGridPanel.test.js
pnpm test src/views/platform/dormitory/room/components/RoomGridPanel.test.js src/views/platform/dormitory/room/components/RoomTreePanel.test.js src/views/platform/dormitory/room/components/RoomSearchToolbar.test.js src/views/platform/dormitory/room/list.test.js src/views/platform/dormitory/room/room-rules.test.js
node scripts/check-room-list-ui.js
pnpm gate
```

手工回归：

- 空态提示。
- 房间卡片展示、锁标记、男/女/夫妻混住颜色。
- 全选和单选。
- 编辑房间。
- 删除房间。

风险：中低。卡片区含 `v-model` 选择状态，已通过显式 `update-*` 事件和父页接线测试锁住。
回滚：单 PR revert。

完成状态：

- 已合并：PR #44 `refactor(smart-ui): extract room grid panel`
- 合并提交：`8343159b5982eb41214660fd663001ab664afef6`
- 实际范围：新增 `RoomGridPanel.vue` 和 `RoomGridPanel.test.js`；`list.vue` 只把房间卡片列表模板替换为 `<room-grid-panel>`，父页继续持有 `checkedRoom`、`checkAll`、`isIndeterminate`、`checkAllChange`、`roomChange`、`handleEdit`、`rowDel`、查询、导出、批量编辑和弹窗提交逻辑；`scripts/check-room-list-ui.js` 改为分别守卫父页组件接线和子组件选择/卡片/编辑删除事件。
- 验证摘要：先新增 `RoomGridPanel.test.js` 并确认组件缺失时红测；实现后 `pnpm test src/views/platform/dormitory/room/components/RoomGridPanel.test.js` 通过（3 个用例）；room 相关 5 个测试文件通过（23 个用例）；`node scripts/check-room-list-ui.js` 通过；新组件 eslint 零 warning；`git diff --check` 通过；`pnpm test` 通过（48 个测试文件、267 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：完整 diff Claude 评审两次在超时内无输出并已中断；改用摘要型子代理独立复核，结论 `AGREE`，同时明确其未直接读源码。最终合并依据为本地源码自查、组件测试、父页接线测试、静态守卫、lint baseline 和 `pnpm gate`。
- 手工回归边界：本轮无可用测试后端和浏览器会话，未执行真实页面点击回归；PR 描述已如实记录该边界。
- 边界核对：未改 API 实现、接口签名、列表查询、房间增删改、楼栋/楼层增删改、导出、批量编辑、弹窗流程、页面文案或任何 A 类 bug 修复。

### PR 15: `room/list.vue` 第五刀：抽楼层/房间表单校验器

分支：`refactor/smart-ui-room-list-extract-validators`

目标：只把 `room/list.vue` 中不依赖 `this` 的楼层起始编号、楼层数量、房间数量校验器抽到 `room-rules.js`，父页继续保留所有表单、提交和 API 调用。

文件范围：

- 修改：`smart-ui/src/views/platform/dormitory/room/list.vue`
- 修改：`smart-ui/src/views/platform/dormitory/room/room-rules.js`
- 修改：`smart-ui/src/views/platform/dormitory/room/room-rules.test.js`

边界：

- 不改表单 `rules` 字段、`trigger`、required 文案或绑定字段。
- 不修复现有 `0` 分支的双 callback 行为；这属于潜在 A 类行为变化，必须另开窗口。
- 不改模板、API、弹窗、提交、查询、导出或批量编辑流程。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/room-rules.test.js
pnpm test src/views/platform/dormitory/room/room-rules.test.js src/views/platform/dormitory/room/list.test.js
node scripts/check-room-list-ui.js
pnpm gate
```

风险：低。纯函数抽离，但刻意保留了历史校验语义和 callback 次序。
回滚：单 PR revert。

完成状态：

- 已合并：PR #46 `refactor(smart-ui): extract room form validators`
- 合并提交：`ca0b38bb1ee5536e6993257b0f91e2acc4ab174c`
- 实际范围：新增导出 `validateFloorStartNumber`、`validateFloorCount`、`validateRoomCount`；`list.vue` 只把 `floorAddRules.startNum`、`floorAddRules.floorNum`、`floorEditRules.roomNum` 的 validator 引用切到新函数；新增测试锁定错误文案、正则、数字 `0`、字符串 `'0'`、负数、小数和楼层上限行为。
- 验证摘要：先新增 validator 行为测试并确认缺导出时红测；实现后 `pnpm test src/views/platform/dormitory/room/room-rules.test.js` 通过（12 个用例）；`room-rules.test.js` + `list.test.js` 通过（16 个用例）；`node scripts/check-room-list-ui.js` 通过；`git diff --check` 通过；`pnpm test` 通过（48 个测试文件、270 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：Claude 纯文本只读 diff 评审结论 `AGREE`，确认 callback 次数、错误文案、正则、边界值、表单 rules 结构均保持旧行为，且未夹带校验语义修复。
- 边界核对：未改 API 实现、接口签名、列表查询、房间增删改、楼栋/楼层增删改、导出、批量编辑、弹窗流程、页面文案或任何 A 类 bug 修复。

### PR 16: `room/list.vue` 第六刀：抽表单状态工厂

分支：`refactor/smart-ui-room-list-extract-form-state`

目标：只把 `room/list.vue` 中重复的表单初始状态对象抽到 `room-rules.js` 纯工厂函数，父页继续保留所有表单 rules、提交、重置、弹窗和 API 调用。

文件范围：

- 修改：`smart-ui/src/views/platform/dormitory/room/list.vue`
- 修改：`smart-ui/src/views/platform/dormitory/room/room-rules.js`
- 修改：`smart-ui/src/views/platform/dormitory/room/room-rules.test.js`

边界：

- 不改 `resetFields`、`validate`、`rules`、`trigger`、required 文案或绑定字段。
- 不改新增楼栋/楼层时 `parkId`、`dormitoryId` 的来源，只把旧内联对象替换为等价工厂调用。
- 不改模板、API、弹窗、提交、查询、导出或批量编辑流程。
- 不修复任何现有表单语义问题或 A 类 bug。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/room-rules.test.js
pnpm test src/views/platform/dormitory/room/list.test.js src/views/platform/dormitory/room/room-rules.test.js
node scripts/check-room-list-ui.js
pnpm gate
```

风险：低。只把对象字面量搬到纯函数，测试锁定字段、`undefined` 默认值、对象实例隔离、`roomIds` 数组实例隔离和新增节点 id 传递。
回滚：单 PR revert。

完成状态：

- 已合并：PR #48 `refactor(smart-ui): extract room form state factories`
- 合并提交：`327fc82dd96c39152848d457a7c28f595e5d818f`
- 实际范围：新增导出 `createEmptyFloorForm`、`createFloorFormForDormitory`、`createEmptyDormForm`、`createDormFormForPark`、`createEmptyBatchEditForm`、`createEmptyRoomEditForm`；`list.vue` 只把初始 `floorForm`、`dormForm`、`batchEditForm`、`editForm` 和树节点新增楼栋/楼层时的内联对象替换为工厂调用。
- 验证摘要：先新增表单状态工厂测试并确认缺导出时红测；实现后 `pnpm test src/views/platform/dormitory/room/room-rules.test.js` 通过（17 个用例）；`room-rules.test.js` + `list.test.js` 通过（21 个用例）；`node scripts/check-room-list-ui.js` 通过；`git diff --check` 通过；`pnpm test` 通过（48 个测试文件、275 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：只读子代理 diff 评审结论 `AGREE`，确认字段和值、`roomIds` 新数组、楼栋/楼层 id 带入、`resetFields`/验证/提交/API 参数均未改变。
- 边界核对：未改 API 实现、接口签名、列表查询、房间增删改、楼栋/楼层增删改、导出、批量编辑、弹窗流程、页面文案或任何 A 类 bug 修复。

### PR 17: `room/list.vue` 第七刀：抽表单 rules 工厂

分支：`refactor/smart-ui-room-list-extract-form-rules`

目标：只把 `room/list.vue` 中重复的 Element UI 表单 rules 对象抽到 `room-rules.js` 纯工厂函数，父页继续保留原 `:rules` 绑定名、表单实例、提交、重置、弹窗和 API 调用。

文件范围：

- 修改：`smart-ui/src/views/platform/dormitory/room/list.vue`
- 修改：`smart-ui/src/views/platform/dormitory/room/room-rules.js`
- 新增：`smart-ui/src/views/platform/dormitory/room/room-form-rules.test.js`

边界：

- 不改任何 rules 字段名、`required`、`message`、`trigger` 或 validator 引用。
- 不改 `editRules`、`batchEditRules`、`floorAddRules`、`floorEditRules`、`dormRules` 的 data 字段名，避免影响模板绑定和 Element UI 表单行为。
- 不改 `resetFields`、`validate`、提交方法、API 参数、弹窗流程或页面文案。
- 不修复任何现有表单校验语义问题或 A 类 bug。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/room-form-rules.test.js
pnpm test src/views/platform/dormitory/room/room-form-rules.test.js src/views/platform/dormitory/room/room-rules.test.js src/views/platform/dormitory/room/list.test.js
node scripts/check-room-list-ui.js
pnpm exec eslint src/views/platform/dormitory/room/room-form-rules.test.js
pnpm gate
```

风险：低。只搬运 rules 字面量，测试锁定字段、文案、触发方式、validator 引用和工厂返回的新对象/新数组。
回滚：单 PR revert。

完成状态：

- 已合并：PR #50 `refactor(smart-ui): extract room form rule factories`
- 合并提交：`f7eb111747b241fb1dd33f64847edbd1f9c21875`
- 实际范围：新增导出 `createFloorAddRules`、`createFloorEditRules`、`createDormRules`、`createBatchEditRules`、`createRoomEditRules`；`list.vue` 只把旧内联 rules 对象替换为工厂调用，并保留原 data 字段名与模板绑定。
- 验证摘要：先新增 `room-form-rules.test.js` 并确认缺导出时红测；实现后 `pnpm test src/views/platform/dormitory/room/room-form-rules.test.js` 通过（5 个用例）；room 相关 3 个测试文件通过（26 个用例）；`node scripts/check-room-list-ui.js` 通过；新测试文件 eslint 零 warning；`git diff --check` 通过；`pnpm test` 通过（49 个测试文件、280 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：只读子代理 diff 评审结论 `AGREE`，确认 5 组 rules 与旧 `list.vue` 一致，工厂每次返回新对象和新数组，未引入跨实例共享 rules 风险。
- 边界核对：未改 API 实现、接口签名、列表查询、房间增删改、楼栋/楼层增删改、导出、批量编辑、弹窗流程、页面文案或任何 A 类 bug 修复。

### PR 18: `room/list.vue` 抽弹窗前安全网

分支：`test/smart-ui-room-dialog-safety-net`

目标：在抽离编辑房间、批量编辑、楼层、楼栋 4 类弹窗前，先补页面级契约测试和静态守卫，不改生产代码。

文件范围：

- 新增：`smart-ui/src/views/platform/dormitory/room/room-dialogs.contract.test.js`
- 修改：`smart-ui/scripts/check-room-list-ui.js`

安全网内容：

- 页面浅挂载锁定 4 个弹窗的标题、visible、`el-form` rules/model、label width、关键字段文案和 footer reset/submit 委托。
- 覆盖批量编辑 `isHandelSD` 条件字段和楼层 `editFloor` 条件字段。
- 静态守卫新增每个弹窗的 form ref/model/rules、关键 `prop`、change handler 和条件字段检查，并接入既有 `pnpm gate` 的 `room-list-ui` 步骤。

边界：

- 不抽组件，不改 `room/list.vue` 生产代码。
- 不改 API、提交、重置、校验、弹窗 visible、页面文案或任何 A 类 bug。
- 后续真正抽弹窗组件时，必须同步把本 PR 的父页顺序断言和静态守卫迁移到新组件级契约。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/room-dialogs.contract.test.js
pnpm test src/views/platform/dormitory/room/room-dialogs.contract.test.js src/views/platform/dormitory/room/list.test.js
node scripts/check-room-list-ui.js
pnpm exec eslint src/views/platform/dormitory/room/room-dialogs.contract.test.js
pnpm gate
```

风险：低。仅新增测试和静态守卫；风险是测试严格度会在后续组件化时要求同步迁移断言，这正是本安全网的目的。
回滚：单 PR revert，删除新增测试并回退静态守卫。

完成状态：

- 已合并：PR #52 `test(smart-ui): add room dialog safety net`
- 合并提交：`efc291e21af841ec1d766d5c9d3724637522dd5b`
- 实际范围：新增 `room-dialogs.contract.test.js`；增强 `scripts/check-room-list-ui.js` 的弹窗 form/ref/model/rules、字段、条件分支和 handler 守卫；未改任何生产源码。
- 验证摘要：`pnpm test src/views/platform/dormitory/room/room-dialogs.contract.test.js` 通过（3 个用例）；`room-dialogs.contract.test.js` + `list.test.js` 通过（7 个用例）；`node scripts/check-room-list-ui.js` 通过；新测试文件 eslint 零 warning；`git diff --check` 通过；`pnpm test` 通过（50 个测试文件、283 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：只读子代理 diff 评审结论 `AGREE`，确认新增测试 mount 真实 `list.vue` 而不是只测 stub，未夹带生产行为变化；提醒后续抽组件时要把父页顺序断言和按注释切块的静态守卫迁移到组件级契约。
- 边界核对：未改 API 实现、接口签名、列表查询、房间增删改、楼栋/楼层增删改、导出、批量编辑、弹窗流程、页面文案或任何 A 类 bug 修复。

### PR 19: `room/list.vue` 抽楼栋弹窗组件

分支：`refactor/smart-ui-room-list-extract-dorm-dialog`

目标：把 `room/list.vue` 中最小的楼栋添加/编辑弹窗抽成受控组件 `RoomDormitoryDialog`，父页继续持有 `dormForm`、`dormRules`、`resetDormForm('dormForm')`、`dormSubmit('dormForm')` 和 API 提交流程。

文件范围：

- 新增：`smart-ui/src/views/platform/dormitory/room/components/RoomDormitoryDialog.vue`
- 新增：`smart-ui/src/views/platform/dormitory/room/components/RoomDormitoryDialog.test.js`
- 修改：`smart-ui/src/views/platform/dormitory/room/list.vue`
- 修改：`smart-ui/src/views/platform/dormitory/room/room-dialogs.contract.test.js`
- 修改：`smart-ui/scripts/check-room-list-ui.js`

边界：

- 不把 `addObj`、`putDormObj`、`dormAdd`、`dormEdit` 或 `dormSubmit` 迁入子组件。
- 不改 `resetDormForm`、`dormSubmit` 方法签名；通过组件 `ref="dormForm"` 代理 `validate` / `resetFields`，保持父页 `$refs.dormForm` 契约。
- 不改弹窗 title、visible、width、rules/model、`label-position="left"`、`dormitoryName` 字段、按钮文案或旧 `floorLoading` loading 绑定。
- 不修复任何现有楼栋提交流程或 A 类 bug。

DoD：

```bash
cd smart-ui
pnpm test src/views/platform/dormitory/room/components/RoomDormitoryDialog.test.js
pnpm test src/views/platform/dormitory/room/components/RoomDormitoryDialog.test.js src/views/platform/dormitory/room/room-dialogs.contract.test.js src/views/platform/dormitory/room/list.test.js
node scripts/check-room-list-ui.js
pnpm exec eslint src/views/platform/dormitory/room/components/RoomDormitoryDialog.vue src/views/platform/dormitory/room/components/RoomDormitoryDialog.test.js
pnpm gate
```

风险：中低。弹窗内部模板已迁入子组件，但提交、重置和 API 仍留父页；主要风险是 `$refs.dormForm` 代理和 `v-model` 替代路径，已由组件测试和父页契约测试覆盖。
回滚：单 PR revert。

完成状态：

- 已合并：PR #54 `refactor(smart-ui): extract room dormitory dialog`
- 合并提交：`190dfb115c0fbc2eefe1ce87cb7df31ac6328c6d`
- 实际范围：新增受控组件 `RoomDormitoryDialog.vue` 和组件测试；`list.vue` 只把楼栋弹窗模板替换为 `<room-dormitory-dialog>`，新增 `updateDormFormField` 写回原 `dormForm` 对象；`room-dialogs.contract.test.js` 和 `check-room-list-ui.js` 从父页内联弹窗检查迁移为父页组件接线 + 子组件内部契约检查。
- 验证摘要：先新增 `RoomDormitoryDialog.test.js` 并确认组件缺失时红测；实现后组件测试通过（3 个用例）；room 弹窗相关 3 个测试文件通过（11 个用例）；`node scripts/check-room-list-ui.js` 通过；新组件和新测试 eslint 零 warning；`git diff --check` 通过；`pnpm test` 通过（51 个测试文件、287 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm gate` 通过。
- 独立评审：只读子代理 diff 评审结论 `AGREE`，确认 `$refs.dormForm.validate/resetFields` 代理、旧弹窗契约、`@update-form-field` 替代旧 `v-model`、Element UI close 行为和静态守卫均无阻塞问题；提醒新增文件提交时不得遗漏，已纳入提交。
- 边界核对：未改 API 实现、接口签名、列表查询、房间增删改、楼栋新增/编辑 API、楼层增删改、导出、批量编辑、页面文案或任何 A 类 bug 修复。

---

## 4. A 类行为变更队列

以下工作全部另开 `fix/` 分支，不能混入上述 `refactor/` PR：

1. ~~`fix/smart-ui-user-menu-error-handling`：`store/modules/user.js` `GetMenu` 无 `.catch`。~~ 已完成，见下方完成状态。
2. ~~`fix/smart-ui-storage-eval`：`util/storage.js` 或旧 `store.js` 中 `eval()`。~~ 已完成，见下方完成状态。
3. `fix/smart-ui-electric-manage-scope`：`device/electric_manage` `_this/this`。
4. `fix/smart-ui-login-credential-config`：`api/login.js` 硬编码租户和 Basic 凭据。
5. `fix/smart-ui-resize-timer-cleanup`：`page/` resize、`panel/bigdata.vue` timer 未解绑。
6. `fix/smart-ui-dangerous-html`：`dangerouslyUseHTMLString` 字符串拼接 XSS 面。
7. `fix/smart-ui-catch-reporting`：catch 上报和用户提示补齐。

A 类 DoD：

- 先写复现测试或行为表征测试。
- 明确是否用户可见行为变化。
- 需要产品或业务确认的，在 PR 描述写清确认结论。
- 能灰度就加开关；不能灰度必须说明回滚方式。

完成状态（`fix/smart-ui-storage-eval`）：

- 已合并：PR #34 `fix(smart-ui): remove storage boolean eval`
- 合并提交：`cb75c996e912a2eb3bd9d53a7808be6b35ef316a`
- 实际范围：仅修改 `smart-ui/src/util/storage.js` 和 `smart-ui/src/util/store.contract.test.js`；把 boolean 反序列化从 `eval(obj.content)` 收窄为显式解析 `true` / `false`，旧 `store.js` 继续 re-export `storage.js`，未改调用方。
- 验证摘要：先新增恶意 boolean 包装对象测试并确认红测；修复后 `pnpm test src/util/store.contract.test.js` 通过（9 个用例）；`pnpm test` 通过（44 个测试文件、255 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm exec eslint src/util/storage.js src/util/store.contract.test.js` 通过；`pnpm gate` 通过。
- 独立评审：本地 Codex 子代理第一次指出未提交 diff 无法审查，按建议先提交；第二轮只读评审结论 `AGREE`。
- 边界核对：正常 `setStore` 写入的 boolean `true` / `false` 行为不变；历史包装对象里的字符串 `'true'` / `'false'` 继续兼容；仅对此前依赖任意 JavaScript 表达式求值的异常 boolean 包装对象发生行为变化，读取结果为 `undefined` 且不执行表达式。

完成状态（`fix/smart-ui-user-menu-error-handling`）：

- 已合并：PR #36 `fix(smart-ui): reject failed menu loads`
- 合并提交：`5950ab87e47fe946692351a23ca38ed96c2c80cb`
- 实际范围：仅修改 `smart-ui/src/store/modules/user.js` 和 `smart-ui/src/store/modules/user.test.js`；`GetMenu` action 在菜单接口失败时 reject 原始错误，成功路径的菜单 `addPath` 归一化与 `SET_MENU` 提交保持不变。
- 验证摘要：先新增失败路径测试并确认红测（旧实现 Promise 保持 pending 且产生未处理拒绝）；修复后 `pnpm test src/store/modules/user.test.js` 通过（5 个用例）；`pnpm test` 通过（44 个测试文件、257 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`git diff --check` 通过；`pnpm gate` 通过。
- 独立评审：第一轮本地 Codex 子代理提出测试需用 `toBe(error)` 严格断言原始错误对象，已修复；第二轮因工具卡住未产出，改用新的窄范围只读子代理，结论 `AGREE`。一次 `claude -p` 只读评审尝试 90 秒无输出后已中断，无产出。
- 边界核对：本 PR 只让调用方收到 rejected Promise，避免菜单请求失败时无限 pending；未新增用户提示、catch 上报、路由重构或菜单渲染逻辑变更。

完成状态（`fix/smart-ui-resize-timer-cleanup` 子项：`page/index` resize handler）：

- 已合并：PR #38 `fix(smart-ui): clean up index resize handler`
- 合并提交：`dfdfb19091e1e33f18e7e2002161b49dc8ac9852`
- 实际范围：仅修改 `smart-ui/src/page/index/index.vue` 和新增 `smart-ui/src/page/index/index.test.js`；页面壳在 `destroyed` 时恢复进入页面前的 `window.onresize`，且只在当前 resize handler 仍归本组件所有时恢复。
- 验证摘要：先新增生命周期测试并确认红测（旧实现销毁后仍保留本组件 resize handler）；修复后 `pnpm test src/page/index/index.test.js` 通过（2 个用例）；`pnpm test` 通过（45 个测试文件、259 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm exec eslint src/page/index/index.test.js` 通过；`pnpm gate` 通过。
- 独立评审：第一次本地 Codex 子代理 90 秒未返回，已关闭；第二次窄范围只读评审结论 `AGREE`。
- 边界核对：初次 `SET_SCREEN` 和 resize 后 `SET_SCREEN` 行为不变；未修改 token 刷新、路由、菜单或 UI。`fix/smart-ui-resize-timer-cleanup` 总项仍未完成，`panel/bigdata.vue` timer 等剩余清理需另开 PR。

完成状态（`fix/smart-ui-resize-timer-cleanup` 子项：`panel/index` clock interval）：

- 已合并：PR #40 `fix(smart-ui): clean up panel clock interval`
- 合并提交：`a75c3cba04961ad20cda410f4b2c2cc57cb8c3bd`
- 实际范围：仅修改 `smart-ui/src/views/platform/panel/index.vue` 和新增 `smart-ui/src/views/platform/panel/index.test.js`；顶层可视化面板把 1 秒时钟 interval id 保存到组件实例，并在 `beforeDestroy` 清理后置空。
- 验证摘要：先新增生命周期测试并确认红测（旧实现没有保留 interval id，无法销毁清理）；修复后 `pnpm test src/views/platform/panel/index.test.js` 通过（1 个用例）；`pnpm test` 通过（46 个测试文件、260 个用例）；`node scripts/check-lint-baseline.mjs` 通过；`pnpm exec eslint src/views/platform/panel/index.test.js` 通过；`pnpm gate` 通过。
- 独立评审：本地 Codex 窄范围只读评审结论 `AGREE`。
- 边界核对：初次 `getTime()` 和每秒更新时间行为不变；未修改路由跳转、退出登录、子面板或 UI。`fix/smart-ui-resize-timer-cleanup` 总项仍未完成，`bigdata/accessto` 的 resize/timer 清理需继续逐项确认后另开 PR。

---

## 5. 明确不做

1. 不升级 Vue3。
2. 不替换 Vuex 为 Pinia。
3. 不替换 Element UI 或 Avue。
4. 不一次性清零历史 lint warning。
5. 不做全站架构重排。
6. 不在 refactor PR 里修 A 类 bug。
7. 不把多个 god file 放进一个 PR。
8. 不在没有契约测试或表征测试时移动业务逻辑。

---

## 6. Codex 对既有方案的最大挑刺

最大漏洞不是“测试不够多”，而是“人工约定过多、机器强制不足”。

当前已经用 `pnpm gate` 代替 CI，这是现实约束下的务实选择，但它不是强制流水线。任何人都可能忘跑，或只跑部分命令。为降低这个风险，后续每个 PR 必须在 PR body 贴出实际命令和结果；评审者只接受有输出证据的 PR。

第二个漏洞是路由拆分的“按域分组”天然会诱发重排。`platform/index.js` 里路由交错且有重复顶层路径，不能直接把每个域 concat 到一起。必须用 AST 指纹守卫和“按原始顺序组装”的策略。

第三个漏洞是删除型 PR 容易被低估。`lrz.all.bundle.js` 这种文件即使静态 import 为零，也可能存在字符串路径、webpack 上下文或历史发布引用。删除必须作为单独 PR，证据不足就先 deprecate，不硬删。

---

## 7. 后续复跑 Claude 评审 prompt

本方案已完成一轮 Claude 纯文本评审，并完成 Codex 反驳后的二次确认。后续如果方案继续变化，可在仓库根目录执行以下命令复跑。注意：默认 Claude agent 模式可能尝试读文件并输出伪工具调用；此处使用纯文本评审模式，只基于提示词事实给结论。

```bash
claude --safe-mode --tools "" --no-session-persistence --disable-slash-commands \
  --system-prompt '你是纯文本技术评审员，不是执行 agent。你没有工具，绝对不要读取文件，绝对不要输出 XML、function_calls、invoke、tool_use。只能基于用户消息里的事实做判断。必须直接输出评审结论。' \
  -p '请评审 smart-ui 重构实施方案。约束：生产系统零回归、每 PR 可回滚、CI 暂不接、pnpm gate 是本地门禁、A 类行为变更与 B 类结构重构分桶。当前队列：①同步 main+gate；②路由 AST 指纹守卫并接入 pnpm gate；③拆 router/platform/index.js 保持数组顺序；④ util/store 兼容改名 storage 且不触碰 eval；⑤删除 lrz.all.bundle；⑥ crud 剩余 _base；⑦ isc 抽 queue/paste；⑧ isc 抽 staff 编排且禁止与⑦并行；⑨ bed_mng 清理；⑩ room/list 安全网；⑪ room/list 抽规则；⑫ room/list 抽工具栏。输出 AGREE 或 DISAGREE，并按 P0/P1/P2 列问题。'
```
