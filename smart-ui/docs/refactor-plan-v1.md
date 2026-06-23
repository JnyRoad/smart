# smart-ui 重构方案 v2（Claude + Codex 双方达成一致）

> 状态：**v2 · 已交叉评审收敛**。Codex 独立评审结论为"有条件同意"，其 6 条修订条件已全部采纳并并入本版（见 §10 评审共识记录）。
> 日期：2026-06-22。范围：`smart-ui/`（Vue2 + Element UI + Avue 管理后台）。
> 核心前提：**生产线系统，行为零回归、功能零缺失**。任何一步都必须可独立验证、可回滚。
>
> v2 相对 v1 的关键收紧（均来自 Codex 评审）：
> 1. 路由拆分从"数组 diff + 冒烟"升级为**动态路由运行期装配契约测试**（§4 Phase 1 + §5.1）。
> 2. `util.js` 拆分前必须先有**模块副作用契约测试**（无 default / 命名导出清单 / 顶层 `@/router/axios` 副作用加载顺序）（§4 Phase 1 + §5.2）。
> 3. **删除类变更（死代码、`lrz.all.bundle.js`）独立成删除 PR**，不得混进"纯搬运"PR（§4 Phase 1 + §5.3）。
> 4. **"补 catch 上报"从结构重构里剥离**，归入 A 类行为变更、单独走（§4 Phase 2 + §2）。
> 5. CI 门禁明确为**"基于 baseline 的不新增 + test"**，不是清零历史 25881 warning（§5）。
> 6. **快照测试降级为辅助手段**，主验证靠运行期契约 + API 请求签名 + 手工回归清单（§5）。

---

## 0. 定调（先质疑前提）

用户的原始判断是"代码很混乱、不符合工程实践、注释都没有"。经过对 909 个源文件、约 15.5 万行的测绘核实，这个判断**只对了一半**：

- ✅ **结构债是真的**：大量 1000~1700 行的 god 组件、`util.js`/`filters/index.js` 杂揉、路由单文件 897 行、公共组件沉淀严重不足（业务页 424，公共组件仅 7）。
- ❌ **"零工程实践"不成立**：项目里**已经存在一条成体系的加固线**（提交里带 `CR-C3 / C5 / C14` 代号）：
  - `docs/lint-baseline.md`：ESLint 基线已冻结 **25881 条 warning**，规则"**新代码不得新增**"；
  - `.eslintrc.js`：`no-empty` 与空 `.catch()` 已设为 **error** 并清理过 138 处空 catch；`max-lines:300`、`complexity:15`、`id-length` 已是 warn；**views 禁止直连 axios**（`CR-C5-005`）；
  - 已有 husky + lint-staged pre-commit；
  - 已有 9 个**核心基建测试**（util / validate / axios 拦截器 / error / user / logs / common-area-config 等），覆盖了最脆弱的请求与错误链路；
  - 错误处理已有灰度开关先例（`SMART_UI_STRICT_REJECT`，`CR-C14-002`）。

**结论：本方案不是推倒重来，而是这条加固线的"第二期"——从"止血 + 基线锁定"推进到"结构债偿还"。** 所有动作必须复用既有机制（lint ratchet、灰度开关、测试基建），不另起炉灶。

> 这一条直接否决了"大重构 / 架构重组"的冲动。详见 §7 不做什么。

---

## 1. 现状分析报告（模块级 + god file 清单）

### 1.1 规模

| 指标 | 数值 |
|---|---|
| `src/` 文件总数 | 909 |
| `.vue` 组件 | 453 |
| `.js` | 363（含 1 个 2912 行的第三方 bundle） |
| 代码行数（vue+js） | ≈ 155,502 |
| 测试文件 | 9（覆盖基建，业务页≈0） |
| 公共组件 | 7（实际在用） |

### 1.2 各模块职责与主要问题（逐目录）

| 目录 | 文件数 | 职责 | 主要问题 |
|---|---|---|---|
| `views/` | 541 | 业务页面（admin / gen / platform 三大块，platform 下 ~12 个业务域） | **god 组件重灾区**；模板+状态+业务规则+请求+列配置+弹窗全塞一个 .vue；同域 30~40% 样板复制 |
| `const/` | 133 | Avue `tableOption` 列配置 / 字典 / logs / setting / website / errorCode / iconList | 公共配置未抽（10+ 文件前 15 行完全相同）；部分 crud 文件内嵌副作用（直接调 API 做远程校验）；与 api 目录未严格一一对应，疑似孤立配置 |
| `api/` | 112 | 接口封装层，统一走 `router/axios` | 命名不统一（fetchList/getList/roleList、getObj/getById、putObj/updateXxx）；`delObj` 全用 POST；JSDoc 覆盖率 ≈17%；`login.js` 硬编码 `TENANT_ID:'1'` 与 Basic 凭据（Base64 可解） |
| `util/` | 17 | 工具集 | **杂物堆**：`util.js`(433 行/29 函数/10+ 领域)、`validate.js`(263 行/命名三套混用)、`lrz.all.bundle.js`(2912 行第三方库，疑死代码——npm 已装 `lrz`)；`formRules.js:2` **真 bug**；`store.js:54` `eval()`；命名违规（`util.js`、`store.js` 实为 storage）；死代码 ~19 个导出函数 |
| `router/` | 7 | 静态 + 动态（后端菜单驱动）路由 | `platform/index.js` **897 行/88 条路由**，12 业务域混在一文件；`avue-router.js` 菜单→路由转换有静默失败（`CR` 注释提示时序敏感） |
| `store/` | 8 | Vuex（user/common/tags/logs + getters） | 基本健康；`user.js` 混入 `addPath` 工具函数与 `GetMenu` 无 `.catch`；`common.js` 把业务常量 `website` 揉进 state |
| `styles/` | 45 | 全局/主题/框架/业务样式 | `platform/public.scss` **1400+ 行**工具类+业务类混杂、无命名空间；`theme/yutong.scss` 370 行全量覆盖 Element；`!important` ~25 处；魔法值与重复 |
| `page/` | 20 | **全局布局壳**（top/sidebar/tags/layout/login/lock） | 单点高耦合：所有业务路由都被 `Layout` 包裹；token 刷新轮询、`window.onresize` 未解绑、硬编码园区 ID |
| `components/` | 10 | 公共组件（basic-container 等 7 个在用） | 沉淀不足；`tce-img`/`tce-search-bar` 孤立未用；注释/props 约定缺失 |
| `mixins/` | 4 | list / executeOnce / color | 隐式耦合（假设宿主有 `getList`/`$refs`）；`color.js` 168 行混 DOM+Vuex+请求 |
| `filters/` | 1 | 25+ 全局过滤器 | `index.js` **433 行**混日期/状态/金额/枚举多领域 |
| `vendor/` | 2 | Blob 垫片 / Export2Excel | 老旧垫片，疑可清理 |
| `config/` | 1 | 运行时 env | 小，依赖 `window.location` |

### 1.3 God file 重点清单（≥800 行，逐一点名）

| 文件 | 行数 | 病灶摘要 |
|---|---|---|
| `util/lrz.all.bundle.js` | 2912 | 第三方库源码混入 util（npm 已装 lrz，疑死代码） |
| `views/platform/resume/info.vue` | 1673 | 5 个子业务（紧急联系人/家庭/教育/工作/任职）揉一起，107 方法，localStorage 直耦合 |
| `views/platform/outsourcing/onwork/index.vue` | 1581 | CRUD+树形组织+照片导入+批量离职，99 方法，HTML 字符串拼接弹窗 |
| `views/platform/basic/personnel_manage/index.vue` | 1575 | 5 个内联弹窗、50+ 方法、内联校验器、`dangerouslyUseHTMLString` 拼接 |
| `views/platform/basic/isc_card_fast_add/index.vue` | 1481 | 队列/卡片双表格、内联校验、样板重复（**试点候选**，独立性强） |
| `views/platform/dormitory/room/list.vue` | 1581 | 重复样板最多 |
| `views/platform/basic/staff_info/index.vue` | 1340 | 同源问题 |
| `views/platform/dormitory/ks_checkIn/index.vue` | 1122 | 入住登记，事务一致性风险 |
| `views/platform/security_area/.../authPersonList.vue` | 1093 | 权限人员列表 |
| `views/platform/dormitory/bed_mng/index.vue` | 1071 | 床位管理 |
| `views/platform/device/electric_manage/index.vue` | 1067 | 批量操作样板；`_this` 作用域 bug |
| `views/platform/recruit/applicant/index.vue` | 1027 | 状态机硬编码数字 |
| `views/platform/device/water_manage/index.vue` | 1009 | 同源 |
| `router/platform/index.js` | 897 | 88 条路由 / 12 域单文件 |
| 其余 800~994 行 | — | `panel/bigdata.vue`、`outsourcing/onwork/leave.vue`、`device/gate.vue`、`device/entrance_guard.vue` 等 |

### 1.4 横切问题（七项工程原则逐条对照）

- **SRP**：god 组件 / `util.js` / `filters/index.js` / `color.js` / 路由表 多职责揉合。
- **高内聚**：同业务的列配置、校验、请求散落多处；同领域函数分散。
- **低耦合**：`Vue.prototype` 原型污染（9 个 errorImg + filters + env）、`window.tce` 全局、全局 mixin 隐式依赖、`page/Layout` 单点。
- **命名讲人话**：`util.js`、`store.js`(实为 storage)、`data`/`temp` 类、api 命名三套混用、`isIentity` 拼写错误。
- **快速失败**：空 catch 已被既有 lint 治理；**残留**的是"catch 有体但只关 loading、不上报/不提示"（lint 规则覆盖不到）；`avue-router` 静默 return；`GetMenu` 无 `.catch`。
- **KISS**：HTML 字符串拼接弹窗、手写字段映射、重复样板。
- **YAGNI**：死代码（19+ util 导出、孤立组件、孤立 crud 配置、第三方 bundle）。

### 1.5 已有的"好东西"（明确保留，不得推翻）

lint baseline ratchet、pre-commit、views 禁直连 axios、9 个基建测试、`SMART_UI_STRICT_REJECT` 灰度先例、`basic-container`/`tce-label-justify` 等高频公共组件、`splitChunks` 分包、Docker 两段构建。

---

## 2. 问题分级（关键：区分"重构"与"改 bug"）

> 对生产系统，**"修复一个静默 bug"本身就是行为变更**。必须分桶处理，不能混为一谈。

- **A 类 · 真 bug / 隐患（修复=行为变更，需产品/业务确认后单独走）**
  - `util/formRules.js:2` 身份证校验规则一直失效（`const { isIentity } = '...'`）。修复后表单会开始拦截非法身份证 → 可能影响存量流程，**必须确认**。
  - `store/modules/user.js` `GetMenu` 无 `.catch` → 菜单接口异常时白屏。
  - `util/store.js:54` `eval()` 安全隐患。
  - `device/electric_manage` `_this/this` 作用域 bug（批量操作回馈）。
  - `api/login.js` 硬编码 `TENANT_ID`/Basic 凭据。
  - `page/` `window.onresize` 等未解绑（内存泄漏）、`panel/bigdata.vue` 定时器清理。
  - `dangerouslyUseHTMLString` 字符串拼接（XSS 面）。
  - **（Codex 补充）残留 catch 的"补救上报/提示"**：现存 catch 多是"只复位 loading、不提示不上报"。给它们补 `console.error`/用户提示/监控上报，**会改变用户可见行为或监控侧副作用，属 A 类，不得夹带进 B 类结构重构**。

> **A 类默认窗口策略（Codex 建议，本版采纳）**：本轮**默认只做 B 类结构债**；A 类（身份证校验、`eval`、catch 上报、硬编码凭据等）**一律另开窗口**，逐项走产品/业务确认 + 灰度开关，不与重构 PR 混合。

- **B 类 · 纯结构债（可行为不变重构，本方案主战场）**
  - god 组件瘦身（抽弹窗子组件 / 抽校验与请求 / 抽列配置到 const）。
  - `util.js`、`filters/index.js`、`router/platform/index.js` 按领域/业务域拆分 + re-export，导出不变。
  - 公共样板（分页/搜索/删除确认/导出/图片导入）下沉为复用单元。
  - 死代码清理、第三方 bundle 移除。

- **C 类 · 横切治理（低风险、渐进）**
  - 补中文注释/JSDoc（项目要求中文注释）、统一命名、`styles/public.scss` 拆分与变量化、统一 z-index。

---

## 3. 重构原则与"行为不变"判定

1. **行为不变优先**：B/C 类默认零行为变更——只移动代码、改 import、加注释、抽组件但渲染输出与交互一致。A 类单独立项、单独确认、单独灰度。
2. **每步独立可验证 + 可回滚**：一个 PR 只做一件事，能单独 build + lint + test 通过，能单独 revert。
3. **小步快跑**：禁止"一个 PR 拆 10 个 god 组件"。先试点 1 个，跑通模式再复制。
4. **复用既有机制**：行为变更走 `SMART_UI_STRICT_REJECT` 式灰度开关；质量管控走 lint ratchet；安全网走 vitest。
5. **先建网，后拆墙**：没有对应安全网（快照/单测/冒烟）之前，不动该模块的业务逻辑。

---

## 4. 分阶段计划（保守增量）

> 阶段之间**严格串行**：上一阶段的安全网没到位，不开下一阶段。每阶段产出多个小 PR。

### Phase 0 — 安全网 + 零风险准备（不碰业务逻辑）
- 目标：把"重构安全网"从 2/10 拉到能支撑页面级改动。
- 动作：
  1. CI 强制 `pnpm lint` + `pnpm test`（当前生产构建跳过 lint，pre-commit 可被 `--no-verify` 绕过）。
  2. 关键路径 e2e 冒烟（Playwright）：登录 → 菜单加载 → 进入 1 个列表页 → 1 次查询。这是"系统还能用"的最低验证线。
  3. 给**即将重构的模块**补快照测试 + 纯函数单测（参考已有 `common-area-config.test.js` 模式）。
  4. 为 god file 现状打"行为快照"基线（重构前后 diff 对比）。
- 验证：CI 绿；冒烟通过。
- 风险：极低（只加测试与流程）。

### Phase 1 — 低风险结构整理（行为不变）
- 目标：把"杂物堆"拆成讲人话的小模块，**对外可观察行为完全不变**。
- **PR 分类铁律（Codex 条件 3）**：本阶段严格区分两种 PR，**绝不混在一个 PR 里**：
  - **搬运型 PR**：只移动/拆分代码 + 原路径 re-export，不删任何东西。
  - **删除型 PR**：删死代码 / 移除文件。每个删除型 PR 必须附：①全仓静态引用检索（含动态字符串引用、`require`、模板里的字符串路径）②构建通过 ③运行冒烟。删除是"行为删除"，准入标准见 §5.3。

- 搬运型动作（每项一个 PR）：
  1. `filters/index.js`(433) → 按 date/status/number/text 拆分，`index.js` 汇总 re-export。
  2. `util.js`(433) → 按 dom/number/object/crypto/theme/fullscreen 拆分；高频函数（`validatenull`/`deepClone` 等）**保持原路径 re-export**，绝不改调用方。**前置硬门槛**：先补 §5.2「模块副作用契约测试」——固化"无 default export / 命名导出清单 / 顶层 `import @/router/axios` 的副作用加载时机 / 现有各种 import 写法"，拆分后契约测试必须全绿（Codex 条件 2）。
  3. `router/platform/index.js`(897) → 按业务域拆成 `platform/<域>.js`，`index.js` `concat` 聚合。**前置硬门槛**：先补 §5.1「动态路由运行期装配契约测试」——mock 后端菜单跑完整 `avue-router` 装配，断言最终 `router` 实例的动态菜单路由 + 静态追加路由 + redirect/keepAlive/404/白屏路径全部一致。**仅"静态数组逐条 diff"不算通过**（Codex 条件 1：avue-router 是运行期先加菜单路由再追加静态路由，数组 diff 证明不了动态注入行为不变）。
  4. `const/crud` 公共配置抽 `_base.js`，存量文件渐进 spread 引用（不强制一次性改）。
  5. 重命名 `util/store.js → util/storage.js`（保留旧路径 re-export 一个版本周期）。
- 删除型动作（独立 PR，走 §5.3 准入）：
  6. 移除 `util/lrz.all.bundle.js`（先证明 `load-lrz.js` 走 npm `lrz` 且无任何对 bundle 的直接引用）。
  7. 清理已确认无引用的 util 死代码（~19 个导出）、孤立组件（`tce-img`/`tce-search-bar`）、孤立 crud 配置——逐批独立 PR。
- 验证：运行期契约测试 + 模块契约测试 + build + lint(baseline 不新增) + test + 冒烟。
- 风险：搬运型低；删除型与路由拆分中（有契约测试兜底）。

### Phase 2 — 试点 god 组件瘦身（建立可复制模式）
- 目标：用 **1 个**中等独立页面跑通"god 组件 → 瘦页面 + 子组件 + 复用单元"的标准动作，沉淀为模板。
- 试点候选：`isc_card_fast_add/index.vue`（1481 行，独立性强、无外部子组件噪音、核心逻辑可纯函数化、已有 `check:isc-card-*` 校验脚本与相关测试）。
- 动作：抽内联校验/队列逻辑为纯函数模块 + 单测；抽弹窗为子组件；补中文注释；行为前后对比。
- **不做（Codex 条件 4）**：**本阶段不补 catch 上报/提示**——那是 A 类行为变更，按 §2 默认窗口策略另开窗口，绝不夹带进试点的结构瘦身。试点只做"行为不变"的拆分与子组件化。
- 验证（主次分明）：**主**=核心纯函数单测 + 既有 `check:isc-card-*` 脚本 + API 请求签名不变 + 手工回归（卡片增删/批量提交/粘贴解析）；**辅**=DOM 快照（只用于发现粗暴回归，不作为"行为不变"的证明）。
- 风险：中（高频业务页，但有 Phase 0 安全网兜底）。

### Phase 3 — 模式推广 + 公共沉淀
- 目标：把 Phase 2 模板复制到其余 god 组件；沉淀公共组件/复用单元。
- 动作：分页/搜索/删除确认/导出/图片上传下沉为复用 mixin 或组件；高频重复结构（badge-label / image-box）上抽公共组件；逐个域推进，每页一个 PR。
- 验证：同 Phase 2，逐页快照 + 冒烟。
- 风险：中，但已模板化、可控。

---

## 5. 安全网建设（Phase 0 细化，gating 全局）

> Codex 评审的核心提醒：**快照测试不能证明"行为不变"**（它防不住 Avue 表格/权限/路由/请求参数的行为漂移）。所以本版把验证分为"主手段（运行期契约 + 请求签名 + 手工回归）"与"辅手段（DOM 快照）"，主次不能颠倒。

- **CI 门禁（Codex 条件 5，措辞要精确）**：CI 强制 `pnpm lint` 与 `pnpm test`。lint 的语义是**"基于 `docs/lint-baseline.md` 基线的不新增（ratchet）"**——即新代码/改动文件不得增加 warning，**不是**要把历史 25881 条清零、更不是用历史总量堵主干。具体命令与基线比对脚本在 PR 里写清。
- **lint ratchet 再收一格**：对**新增/重构后的文件**把 `max-lines`/`complexity` 从 warn 提为 error（用 overrides 限定新文件，存量不动），防止 god 组件复发。
- **API 层单测**：mock `@/router/axios`，断言请求 URL/method/params，防接口签名漂移（这是验证业务页重构"请求行为不变"的主力之一）。
- **e2e 冒烟**：Playwright，覆盖登录/菜单加载/一个 CRUD 主流程（系统"能用"的最低门槛）。
- **DOM 快照（辅）**：`@vue/test-utils` `shallowMount` + `toMatchSnapshot`，**仅用于发现粗暴 DOM 回归**，不作为行为不变的证明。
- 成本估算：Phase 0 基础（CI + 冒烟 + 契约测试脚手架 + 试点模块测试）约 9~12h。

### 5.1 动态路由运行期装配契约（路由拆分的硬门槛 · Codex 条件 1）
- 起因：`avue-router` 在运行期**先**用后端菜单 `formatRoutes` 生成动态路由，**再** `addRoutes` 追加 `platform/index.js` 静态路由；静态数组逐条 diff **证明不了**最终注入到 `router` 实例的结果一致。
- 契约：用 mock 的后端菜单数据，跑完整安装流程（`AvueRouter.install` → `formatRoutes` → `addRoutes(动态)` → `addRoutes(静态 platform)`），断言最终 `router` 的：①全部 path→component 映射 ②redirect 关系 ③`meta.keepAlive` ④白名单/404/lock 路径 ⑤动态与静态的叠加顺序——拆分前后逐项一致。
- 任何 router 拆分 PR 必须先有此测试且全绿，方可合并。

### 5.2 原模块副作用契约（util.js 等的硬门槛 · Codex 条件 2）
- 起因：`util.js` **无 default export、仅命名导出**，且**顶层 `import @/router/axios`（有副作用）**。"原路径 re-export"若只对函数名，会漏掉副作用加载时机与 import 形态变化。
- 契约：固化 ①完整命名导出清单（名字 + 是否函数）②确认无 default export（防有人 `import x from '@/util/util'`）③顶层 `@/router/axios` 的副作用在模块求值时仍按原顺序触发 ④项目里实际出现的各种 import 写法（命名解构 / `import *`）拆分后仍可用。
- 凡顶层有副作用 import 的模块（不止 util.js），拆分前都要先建此类契约测试。

### 5.3 删除类变更准入标准（Codex 漏点补充）
- 适用：死代码、`lrz.all.bundle.js`、孤立组件/配置等一切"删除"。
- 准入证据（缺一不可）：①全仓静态引用检索为零——含 ES import、`require`、**动态字符串路径**（如模板/路由里 `@/views/...` 拼接）、webpack `require.context`、全局 `window.tce.*` 访问 ②删除后 `pnpm build` 通过 ③运行冒烟通过 ④独立 PR、可单独 revert。
- 任一证据不足 → 不删，先标记 `@deprecated` 观察一个版本周期。

---

## 6. 试点选择（待 Codex/用户拍板）

推荐 `isc_card_fast_add`。备选：找一个 600~800 行的中等页面进一步降低试点风险。判据：独立性、逻辑可纯函数化、已有测试/校验脚本、改坏影响面小。

---

## 7. 不做什么（YAGNI / 明确排除）

- ❌ 不升级 Vue3 / 不换 Vuex→Pinia（伤筋动骨，与"零回归"冲突）。
- ❌ 不替换 Avue / avue-crud（全站表格地基）。
- ❌ 不做全站架构重排 / 统一数据层重写。
- ❌ 不一次性消灭 25881 条 lint warning（保持 ratchet 渐进）。
- ❌ 不在没有安全网时动业务逻辑。
- ❌ A 类 bug 不夹带在 B 类重构 PR 里"顺手改"。

---

## 8. 风险登记册与回滚

| 风险 | 触发 | 影响 | 缓解 | 回滚 |
|---|---|---|---|---|
| 路由拆分错漏 | `platform/index.js` 拆分顺序/内容变化 | 菜单点击白屏/404 | 逐条 diff + 导出快照 + 冒烟 | 单 PR revert |
| 全局样式回归 | 动 `theme/yutong.scss`/`public.scss`/`basic-container` | 整站视觉错乱 | 推迟到后期、截图回归、灰度 | 单 PR revert |
| 高频 util 改签名 | 动 `validatenull` 等 | 大面积崩 | 只 re-export 不改签名 | revert |
| A 类修复改变行为 | 修 isIentity/strict reject 等 | 存量流程被拦截 | 产品确认 + 灰度开关 | 关开关 |
| 安全网不足误判通过 | 快照覆盖不全 | 隐性 bug 上线 | 关键路径 e2e + 手工回归清单 | revert |

**回滚总原则**：一 PR 一事；主干随时可回退到上一个绿色版本；A 类变更全部走开关，能秒级关闭。

---

## 9. 待决策项（留给用户【旅途】拍板）

> Codex 已就第 1 项给出建议并被本版采纳为默认；其余仍需你定。

1. ~~A 类 bug 本轮处不处理？~~ **已定（Codex 建议 + 采纳）：本轮默认只做 B 类结构债，A 类一律另开窗口走确认+灰度。** 若你想把某个 A 类（如菜单白屏 `GetMenu` 无 catch）提前修，请单独点名。
2. 试点页面定 `isc_card_fast_add`，还是先拿一个更小（600~800 行）的页面进一步降风险？
3. Phase 0 的 e2e 冒烟范围：最小（登录+菜单+1 个 CRUD），还是覆盖几个核心域？
4. lint ratchet 对"新文件"升 error 的力度（`max-lines`/`complexity` 是否纳入、阈值多少）。
5. 节奏：每周几个 PR？是否需要单独的 release/灰度通道？
6. 是否需要我现在就进入 `writing-plans`，把 Phase 0 拆成可执行的逐步实施计划（含每步的契约测试脚手架）。

---

## 10. 评审共识记录（Claude × Codex）

- **评审方**：Codex（独立模型，`gpt-5` 系，只读对抗性评审，自行读源码核实证据）。
- **评审结论**：**有条件同意**。方向认可，6 条修订条件已全部并入 v2。
- **双方一致性**：Claude 对 Codex 全部 6 条无异议（路由动态装配契约、util.js 副作用契约、删除独立 PR、catch 上报剥离归 A 类、CI ratchet 措辞、快照降为辅助）。**至此达成"双方都同意"的修订版**。
- **过程注记（透明）**：Codex 首轮因共享运行时 broker socket 失效而 hang（约 26 分钟零产出）；清理重拉后运行时恢复；正式评审跑通但收尾 turn 被空闲中断，实质发现已留存于会话 rollout；最终用自包含 prompt 重跑得到完整结构化裁决。结论与中途发现一致，无矛盾。
- **条件落点对照**：条件1→§4 Phase1 路由门槛 + §5.1；条件2→§4 Phase1 util 门槛 + §5.2；条件3→§4 Phase1 PR 分类铁律 + §5.3；条件4→§2 A 类 + §4 Phase2；条件5→§5 CI 门禁；条件6→§5 验证主次。

---

## 附：本方案的事实核实基线

以下结论已由主 Agent 亲自读源码核实（非仅子 agent 转述）：`formRules.js:2` 断裂导入、`store.js:54` eval、`docs/lint-baseline.md` 加固线、`.eslintrc.js` 规则集、`package.json` 已含 `lrz` 依赖。其余 god file 行数、目录文件数来自 `find/wc` 实测。
