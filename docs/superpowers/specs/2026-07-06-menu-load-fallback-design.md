# smart-ui 菜单组件加载失败快速失败设计（含遗留菜单处置方案）

日期：2026-07-06
状态：已实现（前端加固部分）；数据处置部分待业务拍板

## 背景与根因

生产 smart-ui 点击「应用管理」（sys_menu 300301）后页面无限加载。根因链：

1. `sys_menu` 中 18 条 2019 年创建的菜单（300101~300402，「集团公告」「应用管理」「banner管理」等）的
   `component` 指向 `views/app/...`，这批组件只存在于老前端 smart-app-ui
   （113 上 `/home/yuto/smart/smart-app-ui/dist`，2023-02-09 构建），smart-ui 仓库从首个提交起就没有
   `src/views/app/` 目录。
2. `avue-router.js` 按菜单数据动态 `import('../' + component + '.vue')`，组件缺失时 Promise reject。
3. `permission.js` 在 `beforeEach` 里 `NProgress.start()`，只有导航成功的 `afterEach` 才 `NProgress.done()`；
   全局无 `router.onError`，也无 import 失败兜底 → 导航静默中止，加载条永远转。

衍生发现：`SYS_OAUTH_CLIENT_DETAILS`（OAuth 客户端/应用）的管理页面 `views/admin/client/index.vue`
及后端 `/admin/client/*`（权限码 `sys_client_add/edit/del`）均已存在（commit fff40c38），
但 `sys_menu` 从未为它配置菜单和按钮权限行，导致 UI 无入口、只能直连数据库维护。

## 方案对比（前端加固）

- A. 仅加全局 `router.onError`：改动最小，但用户停留原页面、无可见反馈（标签页已被
  `beforeEach` 的 ADD_TAG 提交，留下点了没反应的死标签）。
- B. 仅在 `loadMenuComponent` 的 import 上加 `.catch` 落 404 错误页：在问题源头修复，
  导航正常完成（`afterEach` 触发、进度条收尾），用户看到明确的 404 页；纯函数可单测。
  但不覆盖布局 chunk 网络加载失败等其他导航错误。
- C.（采用）B + A 纵深防御：源头 catch 落 404 页并经 `error-reporter` 上报；
  全局 `onError` 兜底收尾进度条并上报，覆盖其余导航错误。

## 实现要点

1. `src/router/avue-router.js`
   - `loadMenuComponent` 的动态 import 增加 `.catch`：上报 `reportCaughtError(error, '菜单组件加载失败: <component>')`，
     返回 `../components/error-page/404.vue` 作为该路由组件。
   - 保留 webpackChunkName/webpackMode 魔法注释，不改变现有 chunk 命名。
2. `src/permission.js`
   - 注册 `router.onError`：`NProgress.done()` + `reportCaughtError(error, '路由导航失败')`。
3. 测试（vitest，jsdom + @vitejs/plugin-vue2 可编译 SFC）
   - 新增 `src/router/avue-router.fallback.test.js`：
     - 组件路径不存在 → loader resolve 为 404 组件模块（而非 reject/悬挂），且上报被调用、信息含组件路径；
     - 组件路径存在（复用 `components/error-page/403`）→ 返回原组件，不触发上报。
   - `permission.js` 的 onError 两行胶水不做单测（模块导入副作用重），随 PR 说明。

## 数据处置方案（不随本 PR 执行，需业务确认）

### 1. OAuth 客户端管理入口缺失

推荐纯 UI 操作（无需连库），在「权限管理→菜单管理」下新增：

| 节点ID | 父节点 | 标题 | 类型 | 权限标识 | 前端组件 | 前端地址 |
|---|---|---|---|---|---|---|
| 2700 | 2000(系统管理) | 客户端管理 | 菜单 | — | views/admin/client/index | client |
| 2701 | 2700 | 客户端新增 | 按钮 | sys_client_add | — | — |
| 2702 | 2700 | 客户端修改 | 按钮 | sys_client_edit | — | — |
| 2703 | 2700 | 客户端删除 | 按钮 | sys_client_del | — | — |

（2300~2599、2700 号段经查未占用；2800 已被删除节点占用。）
然后在「角色管理」给管理员角色勾选上述节点。等效 SQL 见排查记录，二选一。

### 2. 18 条 views/app/* 老菜单

老「应用管理」维护的 `app_module_info` 数据仍被 smart-h5 首页 `/app/service/module/list` 消费，
功能未死、维护入口只在老后台 smart-app-ui。两个选项：

- 选项甲（推荐）：保留菜单行，仅从 smart-ui 主力角色（role 1 管理员等）收回授权，
  保留给 role 13（园区APP管理员）供老后台继续使用。风险：若同一账号跨两套后台使用会同时失去入口。
- 选项乙：整体 `del_flag='1'` 逻辑删除。风险：老 smart-app-ui 与 smart-ui 共用 `/admin/menu`
  接口取菜单，删除后老后台导航同样消失，等于废掉 App 内容维护入口，除非同步把 appserve
  管理页迁移进 smart-ui（另立项目）。

## 验收标准

- 点击组件缺失的菜单：进度条正常收尾，页面显示 404 错误页，错误经 error-reporter 记录。
- 现有菜单路由行为不变（契约测试 avue-router.contract.test.js 全绿）。
- `pnpm test`、`pnpm lint` 通过。
