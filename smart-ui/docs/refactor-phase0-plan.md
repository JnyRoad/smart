# smart-ui 重构 Phase 0（安全网）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在动任何业务代码之前，先建立"行为不变可被自动验证"的安全网——给 Phase 1 路由/util 拆分提供契约测试、给 API 重构提供请求签名测试、给质量门禁提供 lint 基线守卫、并明确"行为快照"的验证标准。

**Architecture:** 全部是**新增测试 / 工具 / 文档**，**零业务代码改动**。其中路由装配契约测试与 util 副作用契约测试是 Phase 1 拆分的**前置硬门槛**（来自 Codex 评审条件 1、2）。本计划只覆盖"不依赖后端、不依赖 CI 平台"的可完全落地部分；依赖环境决策的两块（e2e 冒烟、CI 接线）列在末尾 §Track B，待【旅途】定平台后再单独成计划。

**Tech Stack:** Vitest 4 + `@vitejs/plugin-vue2` + jsdom（已配置，见 `vitest.config.mjs`）；ESLint（`eslint --ext .js,.vue src`，基线见 `docs/lint-baseline.md`）；husky + lint-staged（已装）；pnpm。

**约定（遵循项目 + 旅途全局规则）：**
- 每个任务一个独立分支 + 一个 PR，可单独验证、单独 revert。
- commit message 用英文 Conventional Commits，**不加任何署名/营销 trailer**。
- 分支前缀：测试用 `test/`，工具/配置用 `chore/`。
- **这些是"钉住现有行为"的表征测试（characterization tests）**：写完应**立即变绿**（它锁定的是当前真实行为）。若意外变红，说明对现有行为的理解有误 → 先排查，别改测试将就。

---

## File Structure（本阶段新增物）

- `src/util/util.contract.test.js` — util.js 模块副作用 + 导出契约（Task 1）
- `src/router/avue-router.contract.test.js` — 动态路由运行期装配契约（Task 2）
- `src/api/admin/user.test.js` — API 请求签名测试范式样本（Task 3）
- `scripts/check-lint-baseline.js` — lint 基线守卫脚本（Task 4）
- `scripts/check-lint-baseline.test.js` — 守卫脚本核心比对逻辑单测（Task 4）
- `docs/lint-baseline.json` — 机器可比对的逐文件 warning 基线（Task 4 生成）
- `.husky/pre-push` — 推送前跑基线守卫 + 测试（Task 4）
- `docs/refactor-verification-standard.md` — "行为快照"验证标准（Task 5）

---

## Task 1: util.js 模块副作用 + 导出契约测试

> 锁定 Codex 条件 2：`util.js` **无 default export、仅命名导出**，且顶层 `import request from '@/router/axios'`（求值时即触发，有副作用）。拆分前钉住这些，拆分后此测试必须仍绿。

**Files:**
- Create: `src/util/util.contract.test.js`
- 参考（只读）：`src/util/util.js:1-2`（顶层 import）、现有范式 `src/util/util.test.js:1-6`

- [ ] **Step 1: 写契约测试**

```js
// src/util/util.contract.test.js
import { describe, it, expect, vi } from 'vitest'

// util.js 顶层 `import request from '@/router/axios'` 是有副作用的导入；
// 用 vi.hoisted 记录该副作用是否在 util 求值时被触发，作为"副作用契约"的断言依据。
const hoisted = vi.hoisted(() => ({ axiosLoaded: false }))
vi.mock('@/router/axios', () => {
  hoisted.axiosLoaded = true
  return { default: {} }
})

// util.js 当前的全部命名导出（拆分时若新增/删除导出，必须显式更新本清单）
const EXPECTED_EXPORTS = [
  'serialize', 'getObjType', 'deepClone', 'diff', 'toggleGrayMode', 'setTheme',
  'encryption', 'fullscreenToggel', 'listenfullscreen', 'fullscreenEnable',
  'reqFullScreen', 'exitFullScreen', 'findParent', 'loadStyle', 'isObjectValueEqual',
  'findByvalue', 'findArray', 'randomLenNum', 'openWindow', 'handleImg', 'isArrayFn',
  'dateFormat', 'getDateMonth', 'getDatePreMonth', 'getDatePreDay', 'formatNumber',
  'getProportion', 'floatNumMinus', 'getLabel'
].sort()

describe('util.js 模块契约（拆分前钉死）', () => {
  it('求值时触发 @/router/axios 的副作用导入', async () => {
    await import('./util')
    expect(hoisted.axiosLoaded).toBe(true)
  })

  it('没有 default export（禁止 import x from "@/util/util"）', async () => {
    const mod = await import('./util')
    expect(mod.default).toBeUndefined()
  })

  it('命名导出清单与基线完全一致', async () => {
    const mod = await import('./util')
    const actual = Object.keys(mod).filter((k) => k !== 'default').sort()
    expect(actual).toEqual(EXPECTED_EXPORTS)
  })
})
```

- [ ] **Step 2: 跑测试，应直接变绿（表征测试）**

Run: `pnpm test src/util/util.contract.test.js`
Expected: PASS（3 个用例全过）。若 `命名导出清单` 失败 → 说明 `EXPECTED_EXPORTS` 与现状不符，按实际 `grep -nE '^export ' src/util/util.js` 修正清单（不要改 util.js）。

- [ ] **Step 3: Commit**

```bash
git checkout -b test/util-module-contract
git add src/util/util.contract.test.js
git commit -m "test(util): pin util.js export and side-effect import contract"
```

---

## Task 2: 路由动态装配契约测试

> 锁定 Codex 条件 1：`avue-router.js` 在运行期**先**用后端菜单 `formatRoutes` 生成动态路由并 `addRoutes`，**再** `addRoutes(PlatformRouter)`。仅对 `platform/index.js` 做静态数组 diff **证明不了**最终装配结果。本测试钉住"装配产物 + 顺序"，是 Phase 1 拆 `router/platform` 的前置门槛。

**Files:**
- Create: `src/router/avue-router.contract.test.js`
- 参考（只读）：`src/router/avue-router.js:78-163`（formatRoutes / addRoutes 顺序）、`src/router/router.js:22-24`

- [ ] **Step 1: 写装配契约测试**

```js
// src/router/avue-router.contract.test.js
import { describe, it, expect, vi } from 'vitest'

// PlatformRouter 是"菜单之后追加"的静态自定义路由；用哨兵 mock 它，
// 以便断言它在动态菜单路由之后被 addRoutes（顺序是行为关键点）。
vi.mock('./platform/', () => ({
  default: [{ path: '/__platform_sentinel__', name: 'platform-sentinel' }]
}))

const AvueRouter = (await import('./avue-router')).default

// 构造一个可记录 addRoutes 调用的假 router 和带 website 配置的假 store
function makeFakeRouter () {
  const calls = []
  return { addRoutes: (routes) => calls.push(routes), $addRoutesCalls: calls }
}
function makeFakeStore () {
  return {
    getters: { website: { menu: { props: {} } }, tag: {} },
    commit: vi.fn()
  }
}

describe('avue-router 动态装配契约（拆分前钉死）', () => {
  it('一级菜单：path 去 /index、redirect 指向 /index、keepAlive 取反、生成 index 子路由', () => {
    const router = makeFakeRouter()
    AvueRouter.install(router, makeFakeStore())

    const menu = [
      { label: '设备', path: '/platform/device/index', icon: 'i1', keepAlive: 0, component: 'views/platform/device/gate', children: [] }
    ]
    router.$avueRouter.formatRoutes(menu, true)

    // 第 1 次 addRoutes = 动态菜单路由
    const dynamic = router.$addRoutesCalls[0]
    expect(dynamic).toHaveLength(1)
    expect(dynamic[0].path).toBe('/platform/device')
    expect(dynamic[0].redirect).toBe('/platform/device/index')
    expect(dynamic[0].name).toBe('设备')
    expect(dynamic[0].meta).toEqual({ keepAlive: true }) // Number(0)===0 → true
    expect(dynamic[0].children).toHaveLength(1)
    expect(dynamic[0].children[0].path).toBe('index')
  })

  it('PlatformRouter 必须在动态菜单路由之后追加（顺序契约）', () => {
    const router = makeFakeRouter()
    AvueRouter.install(router, makeFakeStore())
    router.$avueRouter.formatRoutes(
      [{ label: 'A', path: '/platform/a/index', icon: '', keepAlive: 1, component: 'views/platform/a/index', children: [] }],
      true
    )

    expect(router.$addRoutesCalls).toHaveLength(2)
    // 第 2 次 addRoutes 必须是 PlatformRouter 哨兵
    expect(router.$addRoutesCalls[1][0].path).toBe('/__platform_sentinel__')
  })

  it('keepAlive 非 0 时为 false', () => {
    const router = makeFakeRouter()
    AvueRouter.install(router, makeFakeStore())
    router.$avueRouter.formatRoutes(
      [{ label: 'B', path: '/platform/b/index', icon: '', keepAlive: 1, component: 'views/platform/b/index', children: [] }],
      true
    )
    expect(router.$addRoutesCalls[0][0].meta).toEqual({ keepAlive: false })
  })
})
```

- [ ] **Step 2: 跑测试，应直接变绿**

Run: `pnpm test src/router/avue-router.contract.test.js`
Expected: PASS（3 个用例）。若变红 → 不要改 avue-router.js，先核对 `src/router/avue-router.js:88-163` 的真实分支，把断言对齐到当前真实行为（这正是契约测试的价值：暴露我们对现状的误解）。

- [ ] **Step 3: Commit**

```bash
git checkout -b test/router-assembly-contract
git add src/router/avue-router.contract.test.js
git commit -m "test(router): pin dynamic route assembly and PlatformRouter append order"
```

---

## Task 3: API 层请求签名测试范式（样本：admin/user.js）

> 给 Phase 1/2 的 API 重构提供"请求 URL/method/params 不变"的验证范式（Codex 指出快照测不到请求行为）。本任务只立一个**可复制的样板**，不要求覆盖全部 112 个 api 文件。

**Files:**
- Create: `src/api/admin/user.test.js`
- 参考（只读）：`src/api/admin/user.js:1-47`

- [ ] **Step 1: 写请求签名测试**

```js
// src/api/admin/user.test.js
import { describe, it, expect, vi, beforeEach } from 'vitest'

// 每个 api 文件顶层 `import request from '@/router/axios'`；mock 成可记录的 spy，
// 断言每个 api 函数把正确的 url/method/params/data 传给 request。
const request = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/router/axios', () => ({ default: (cfg) => request(cfg) }))

const api = await import('./user')

describe('api/admin/user 请求签名契约', () => {
  beforeEach(() => request.mockClear())

  it('fetchList → GET /admin/user/page，query 走 params', () => {
    api.fetchList({ current: 1, size: 10 })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/user/page', method: 'get', params: { current: 1, size: 10 }
    })
  })

  it('addObj → POST /admin/user/save，obj 走 data', () => {
    api.addObj({ username: 'u' })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/user/save', method: 'post', data: { username: 'u' }
    })
  })

  it('getObj → GET /admin/user/:id', () => {
    api.getObj(42)
    expect(request).toHaveBeenCalledWith({ url: '/admin/user/42', method: 'get' })
  })

  it('delObj → POST /admin/user/:id（当前用 post，不是 DELETE）', () => {
    api.delObj(42)
    expect(request).toHaveBeenCalledWith({ url: '/admin/user/42', method: 'post' })
  })

  it('putObj → POST /admin/user/update，obj 走 data', () => {
    api.putObj({ id: 1 })
    expect(request).toHaveBeenCalledWith({
      url: '/admin/user/update', method: 'post', data: { id: 1 }
    })
  })

  it('getDetails → GET /admin/user/details/:obj', () => {
    api.getDetails(7)
    expect(request).toHaveBeenCalledWith({ url: '/admin/user/details/7', method: 'get' })
  })
})
```

- [ ] **Step 2: 跑测试，应直接变绿**

Run: `pnpm test src/api/admin/user.test.js`
Expected: PASS（6 个用例）。

- [ ] **Step 3: Commit**

```bash
git checkout -b test/api-request-signature-pattern
git add src/api/admin/user.test.js
git commit -m "test(api): add request-signature contract pattern for admin/user"
```

---

## Task 4: lint 基线守卫脚本 + pre-push 钩子

> 落实 Codex 条件 5：门禁语义是"**基于基线的不新增**"，不是清零历史 25881 条。生成逐文件 warning 基线 JSON，比对时只在"某文件 warning 增加"或"新文件带 warning"时失败。平台无关（pre-push hook），CI 接线见 §Track B。

**Files:**
- Create: `scripts/check-lint-baseline.js`
- Create: `scripts/check-lint-baseline.test.js`
- Create: `docs/lint-baseline.json`（脚本 `--update` 生成）
- Create: `.husky/pre-push`
- Modify: `package.json:6-18`（加 `check:lint-baseline` 脚本）
- 参考（只读）：`docs/lint-baseline.md`、`.eslintrc.js`、现有脚本风格 `scripts/check-records-contracts.js`

- [ ] **Step 1: 先为"比对纯函数"写单测**

```js
// scripts/check-lint-baseline.test.js
import { describe, it, expect } from 'vitest'
import { diffAgainstBaseline } from './check-lint-baseline.js'

describe('diffAgainstBaseline', () => {
  it('文件 warning 数未增 → 无违规', () => {
    const baseline = { 'src/a.js': 3 }
    const current = { 'src/a.js': 3 }
    expect(diffAgainstBaseline(baseline, current)).toEqual([])
  })

  it('文件 warning 数下降 → 无违规（允许变好）', () => {
    expect(diffAgainstBaseline({ 'src/a.js': 3 }, { 'src/a.js': 1 })).toEqual([])
  })

  it('文件 warning 数上升 → 报违规', () => {
    const v = diffAgainstBaseline({ 'src/a.js': 3 }, { 'src/a.js': 5 })
    expect(v).toEqual([{ file: 'src/a.js', baseline: 3, current: 5 }])
  })

  it('新文件带 warning → 报违规（基线视为 0）', () => {
    const v = diffAgainstBaseline({}, { 'src/new.js': 2 })
    expect(v).toEqual([{ file: 'src/new.js', baseline: 0, current: 2 }])
  })
})
```

- [ ] **Step 2: 跑单测确认 RED**

Run: `pnpm test scripts/check-lint-baseline.test.js`
Expected: FAIL（`diffAgainstBaseline` 尚未定义 / 模块不存在）。

- [ ] **Step 3: 写守卫脚本（含被测纯函数 + CLI）**

```js
// scripts/check-lint-baseline.js
// lint 基线守卫：对比逐文件 warning 数，只在"某文件增加"或"新文件带 warning"时失败。
// 用法：
//   node scripts/check-lint-baseline.js            # 比对，违规则退出码 1
//   node scripts/check-lint-baseline.js --update    # 重新生成基线 JSON
import { execFileSync } from 'node:child_process'
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { relative, resolve } from 'node:path'

const ROOT = resolve(import.meta.dirname, '..')
const BASELINE_PATH = resolve(ROOT, 'docs/lint-baseline.json')

// 跑 eslint 输出 JSON，聚合成 { 相对路径: warningCount }
export function collectWarnings () {
  let raw
  try {
    raw = execFileSync(
      'pnpm',
      ['exec', 'eslint', '--ext', '.js,.vue', 'src', '-f', 'json'],
      { cwd: ROOT, encoding: 'utf8', maxBuffer: 64 * 1024 * 1024 }
    )
  } catch (e) {
    // eslint 有 warning/error 时退出码非 0，但 stdout 仍是有效 JSON
    raw = e.stdout
    if (!raw) throw e
  }
  const report = JSON.parse(raw)
  const counts = {}
  for (const f of report) {
    if (f.warningCount > 0) counts[relative(ROOT, f.filePath)] = f.warningCount
  }
  return counts
}

// 纯函数：对比基线与当前，返回违规清单（可单测）
export function diffAgainstBaseline (baseline, current) {
  const violations = []
  for (const file of Object.keys(current)) {
    const base = baseline[file] || 0
    if (current[file] > base) {
      violations.push({ file, baseline: base, current: current[file] })
    }
  }
  return violations
}

function main () {
  const update = process.argv.includes('--update')
  const current = collectWarnings()
  if (update) {
    const sorted = Object.fromEntries(Object.keys(current).sort().map((k) => [k, current[k]]))
    writeFileSync(BASELINE_PATH, JSON.stringify(sorted, null, 2) + '\n')
    console.log(`baseline updated: ${Object.keys(sorted).length} files`)
    return
  }
  if (!existsSync(BASELINE_PATH)) {
    console.error('缺少 docs/lint-baseline.json，先运行：node scripts/check-lint-baseline.js --update')
    process.exit(1)
  }
  const baseline = JSON.parse(readFileSync(BASELINE_PATH, 'utf8'))
  const violations = diffAgainstBaseline(baseline, current)
  if (violations.length) {
    console.error('lint 基线被突破（只允许减少，不允许新增）：')
    for (const v of violations) console.error(`  ${v.file}: ${v.baseline} → ${v.current}`)
    process.exit(1)
  }
  console.log('lint 基线 OK：无文件新增 warning')
}

// 仅作为 CLI 运行时执行 main（被测试 import 时不执行）
if (process.argv[1] && process.argv[1].endsWith('check-lint-baseline.js')) main()
```

- [ ] **Step 4: 跑单测确认 GREEN**

Run: `pnpm test scripts/check-lint-baseline.test.js`
Expected: PASS（4 个用例）。

- [ ] **Step 5: 生成基线 JSON 并验证守卫自洽**

Run: `node scripts/check-lint-baseline.js --update && node scripts/check-lint-baseline.js`
Expected: 先打印 `baseline updated: N files`，再打印 `lint 基线 OK：无文件新增 warning`（退出码 0）。

- [ ] **Step 6: 接入 package.json 脚本**

在 `package.json` scripts 增加（与既有 `check:*` 同风格）：

```json
"check:lint-baseline": "node scripts/check-lint-baseline.js",
```

- [ ] **Step 7: 加 pre-push 钩子**

```sh
# .husky/pre-push
pnpm test
node scripts/check-lint-baseline.js
```

- [ ] **Step 8: 验证钩子可执行 + 整体跑通**

Run: `chmod +x .husky/pre-push && pnpm test && node scripts/check-lint-baseline.js`
Expected: 测试全绿 + 基线 OK。

- [ ] **Step 9: Commit**

```bash
git checkout -b chore/lint-baseline-guard
git add scripts/check-lint-baseline.js scripts/check-lint-baseline.test.js docs/lint-baseline.json package.json .husky/pre-push
git commit -m "chore(lint): add per-file warning baseline guard and pre-push hook"
```

---

## Task 5: 行为快照（验证标准）定义文档

> 落实 Codex 条件 6 与"漏点"：给"行为不变"一个**可执行的判定标准**，明确各手段覆盖什么、主次如何。这是后续所有重构 PR 的验收清单来源。

**Files:**
- Create: `docs/refactor-verification-standard.md`

- [ ] **Step 1: 写验证标准文档**

内容必须包含以下小节（每节给"覆盖什么 / 用什么手段 / 算不算主手段"）：

1. **导出签名契约**（主）：模块命名导出清单、有无 default、副作用 import 顺序——见 Task 1 范式。
2. **路由装配契约**（主）：动态+静态路由最终装配产物与顺序——见 Task 2 范式。
3. **API 请求签名契约**（主）：url/method/params/data 不变——见 Task 3 范式。
4. **纯函数单测**（主）：抽离出的业务规则函数输入输出不变。
5. **DOM 快照**（辅）：`shallowMount + toMatchSnapshot`，只用于发现粗暴结构回归，**不作为行为不变的证明**。
6. **手工回归清单**（主，针对业务页）：每个重构页面列出关键交互路径（增/删/改/查/批量/导入/导出/弹窗）逐条人工过。
7. **e2e 冒烟**（主，见 Track B）：登录→菜单→1 个 CRUD 主流程。
8. **删除类准入**（主）：见方案 v2 §5.3——静态引用检索归零（含动态字符串/`require.context`/`window.tce.*`）+ build + 冒烟。
9. **每个重构 PR 的 Definition of Done**：上述相关项全绿 + `pnpm test` + `node scripts/check-lint-baseline.js` 通过 + 可单独 revert。

- [ ] **Step 2: Commit**

```bash
git checkout -b docs/refactor-verification-standard
git add docs/refactor-verification-standard.md
git commit -m "docs(refactor): define behavior-preservation verification standard"
```

---

## Track B — 依赖环境决策的安全网（待【旅途】定平台后单独成计划）

这两块属于 Phase 0 安全网，但需要先定环境事实，**不在本计划里硬编命令**（避免写出跑不通的步骤）：

1. **CI 接线**：当前**无任何 CI 配置**，远端为内网自建 git（`http://10.13.21.6/lisx/smart.git`，PR 风格疑似 Gitea）。需确认 CI 平台（Gitea Actions / GitLab CI / Jenkins / Drone）后，再把 `pnpm test` + `node scripts/check-lint-baseline.js` 接进流水线（pre-push 钩子已是平台无关的兜底）。
2. **e2e 冒烟（Playwright）**：需确认测试环境——能否连一套非生产后端、是否有测试账号、`vue.config.js` proxy 指向哪。最小目标：登录→菜单加载→进入 1 个列表页查询。若暂无测试后端，先退化为"app 能 build + dev server 起得来 + 登录页渲染无致命 console 错误"的轻冒烟。

---

## Self-Review（against 方案 v2）

- **§5.1 路由契约** → Task 2 ✅
- **§5.2 util 副作用契约** → Task 1 ✅
- **§5 API 单测** → Task 3 ✅
- **§5 CI ratchet 语义（不新增）** → Task 4 ✅（pre-push 兜底；CI 接线在 Track B）
- **§5 快照降为辅助 + 行为快照定义** → Task 5 ✅
- **§5.3 删除准入** → 写入 Task 5 文档 ✅（具体删除动作在 Phase 1）
- **e2e 冒烟** → Track B（依赖测试后端，诚实标注）
- **零业务代码改动** → 全部任务仅新增 test/script/doc ✅
- **占位扫描** → 各步均有真实代码与命令，无 TBD/TODO ✅
