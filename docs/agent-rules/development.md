# 开发规则

本文件维护项目特有的代码、数据和验收约定。文件写入及交付按 [Git 与 worktree](git-worktree.md)，规格续作按 [规格工作流](spec-workflow.md)。

## 代码与资料

- 子项目平铺，不在子项目内另建 `.git`。模块入口及新模块登记维护在 [项目 README](../../README.md)，设计资料位置见 [文档目录](../README.md)。
- 新增或修改的注释使用中文，适用于 JavaDoc、JSDoc/TSDoc、SQL、配置和测试说明。解释代码不能直接表达的业务意图、外部协议约束和非直观失败条件；没有每函数或每步骤必须写注释的要求。
- 技术栈、目录职责和运行命令以 [项目 README](../../README.md)及模块文档为源；App 资源和打包要求见 [App README](../../smart-app-uniapp/README.md)。

| 子项目 | 技术与目录约定 |
| --- | --- |
| `smart-app/` | 新 uni-app x / Vue 3 客户端；`core/` 放跨端模型、权限、扫码和状态流转，`services/` 放 uni 传输与统一 `/api/v1/*` 适配，实际接入状态见客户端集成文档，`state/` 集中管理内存会话，`pages/` 和 `components/` 放跨端界面，`docs/` 记录集成与验证边界。HBuilderX 5.24 启用 Vapor 并保留字节码配置，Android 本轮源码编译输出 nativecode，Web/小程序以编译器实际 VDOM 输出为准。 |
| `smart-app-uniapp/` | 现有 App，uni-app / Vue 2 的 App 客户端，HBuilderX 工程；`api/` 接口、`pages/page/` 页面、`components/` 组件、`config/` 端点与权限字典、`tools/` 请求与存储、`static/` 资源。`unpackage/res/` 被 manifest 引用，必须入库，不整体忽略 `unpackage/` |

## 数据与权限

以下业务规则适用于管理端与平台后端的对应功能，不扩展为其他模块的技术栈要求：

- `smart-ui` 提交和展示接口契约；平台业务筛选、数据关联及园区可见范围由 `smart-platform` 服务端执行，不能用前端过滤代替权限控制。变更保留既有参数语义与调用方兼容，明确批准的破坏性变更除外。
- Oracle 查询结构与兼容性依据目标 schema 的字段、约束和相关数据库行为。涉及性能结论或优化时核对当前数据规模、索引、统计信息与可取得的执行计划；前置通配符搜索不能承诺由普通 B-tree 加速。缺少真实计划时标明未验证并提供现场复核方式。
- 生产 DML、DDL、统计信息刷新、索引创建/删除及其他真实数据写入，必须核实目标 schema、执行条件、影响范围、回滚与已有明确授权；不得在功能修复中隐式清理数据完整性问题。历史脚本是否执行以发布记录和已上线版本为准，文件存在不代表可以重跑。
- 应用代码不得携带真实数据库凭据；诊断输出不回显凭据或个人数据，使用脱敏证据。
- 本地数据库、第三方联调和线上编排的环境契约见 [项目 README](../../README.md#本地环境与线上运行)。外部资源归属见 [Git 与 worktree](git-worktree.md#外部资源)。

## 验证

- 行为变更应提供覆盖实际影响的回归验证；缺陷修复验证原失败场景，新功能验证正常路径及关键边界。测试与实现的先后顺序由任务需要决定，不以顺序作为验收条件。
- 文档和低风险配置使用链接、结构、解析、差异或相关现有检查；配置改变运行行为时验证受影响行为。不得削弱有效断言或安全校验来换取通过。
- 前端验证展示和请求契约，后端验证查询语义、权限与结果去重。具体命令从对应子项目或 Maven reactor 根目录运行，见 [README 的常用入口](../../README.md#常用入口)，不从仓库顶层误选 Maven 模块。
- `-DskipTests` 打包不代表测试通过；Mock、本地、CI、真实 Oracle、微信、App 真机和设备验证分别记录。交付说明实际运行结果、未验证边界和数据库操作状态，不将设计或本地结果标成生产可用。

| 范围 | 工作目录与常用验证 |
| --- | --- |
| 新统一客户端 | 从 `smart-app/` 运行 `npm test`、`npm run build:web`、`npm run build:weixin`、`npm run build:alipay` 和 `npm run build:android`；后者只编译 Android 原生源码，不生成签名 APK。`npm run preview` 默认使用 5179；测试依赖 Node 24+，HBuilderX 原生编译使用内置 Node 22。真实后端、真机、NFC、HarmonyOS/iOS、发布包和桌面打印分别记录，不把演示或源码编译写成生产验收。 |
