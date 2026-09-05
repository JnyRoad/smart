# 开发与模块边界

由 [根规则](../../AGENTS.md) 按任务加载。进入实现前先遵守 [Git 与 worktree](git-worktree.md) 和 [规格工作流](spec-workflow.md)，读取目标模块当前 README、配置及相关设计。

## 仓库组织与实现

- 子项目平铺，禁止在子项目内另建 `.git`。新增子项目同步更新根 README 与 AGENTS 的模块入口。
- 设计文档放对应子项目自己的 `docs/`；项目级规则与跨模块资料放根 `docs/`。不删除已完成设计历史。
- 以当前源码、README 和配置核实技术栈、架构与功能；历史设计、Mock、本地检查不能写成生产可用。
- 只做当前任务所需的最小改动，不覆盖用户或其他 Agent 的未提交内容，不顺手扩大业务范围。
- 注释一律用中文，覆盖 JavaDoc、JSDoc / TSDoc、行注释、块注释、SQL、配置及测试注释；新开发补齐必要说明，改动复杂逻辑时同步补充业务意图、边界与失败条件，避免注释与实现失配。

## 目录边界

| 子项目 | 技术与目录约定 |
| --- | --- |
| `smart/` | Java 8、Maven、Spring Boot 2.1 / Spring Cloud Greenwich；`smart-gateway/` 网关、`smart-auth/` 认证、`smart-upms/` 权限、`smart-common/` 公共组件 |
| `smart-module/` | Java / Spring Cloud 业务服务；业务按一级目录划分，`api` 放契约，`biz` 放可部署服务，`core` 放领域复用代码 |
| `smart-ui/` | Vue 2、Element UI、Avue；`src/api/` 接口，`src/views/` 页面，`src/router/` 路由与 axios，`src/store/` Vuex，`public/` 原样静态资源 |
| `smart-h5/` | Next.js 16、React 19、TypeScript strict、antd-mobile 5、Tailwind CSS 4；`src/app/` 页面壳、`src/features/` 业务域、`src/lib/` 共享设施、`src/components/` 通用组件，功能与细节以 [当前 README](../../smart-h5/README.md) 为准 |
| `smart-h5-vue2/` | Vue 2 历史 H5，仅只读参考，不再维护或发布；旧页面、路由、接口与组件分别在 `src/views-mobile/`、`src/router/`、`src/services/`、`src/components/` |
| `smart-app-uniapp/` | uni-app / Vue 2 的 App 客户端，HBuilderX 工程；`api/` 接口、`pages/page/` 页面、`components/` 组件、`config/` 端点与权限字典、`tools/` 请求与存储、`static/` 资源。`unpackage/res/` 被 manifest 引用，必须入库，不整体忽略 `unpackage/` |

App 与微信 H5 并行使用，`smart-app-uniapp/` 是客户端，`smart-module/smart-app` 是后端模块，不得混淆。后端服务包名统一 `com.tce.smart`；Nacos、网关、认证与 Feign 等架构判断以当前 README、源码和配置核实。

### 业务后端模块入口

| 模块 | 用途 |
|---|---|
| `smart-app` | App 业务模块 |
| `smart-platform` | 平台业务模块 |
| `smart-data` | 数据通讯模块 |
| `smart-push` | App 消息推送模块 |
| `smart-schedule` | 定时任务模块 |
| `smart-algorithm` | 算法模块 |
| `smart-tool` | 智慧园区服务公共模块 |
| `smart-bridge` | 设备桥接 |
| `smart-bridge-isc` | ISC（综合安防平台）集成桥接 |
| `smart-bridge-concentrator` | 设备集中器（YUTO Nexus，含协议层 `-protocol`） |
| `smart-dispatcher` | 调度 / 分发 |
| `smart-file` | 文件服务 |
| `smart-transfer` | 数据传输 |
| `smart-park-service` | 历史园区服务模块，仅存 pom、无 `src/` 源码，视为废弃占位 |
| `FileReceiver` | 独立 Spring Boot 程序，接收入厂申请的人脸照片，部署在许昌打印机 Windows 机（`FileController` / `FileApplication`，产物 `build/file.jar`） |

> `smart-bridge`/`-isc`/`-concentrator`/`smart-dispatcher`/`smart-file`/`smart-transfer` 的 pom 未写描述，用途按模块名标注，要精确可逐个核实。

## 数据与外部环境

- 数据库变更以发布记录和已上线版本为准，不保留人工脚本目录。分析不授权 DDL、DML 或重跑历史变更；涉及真实数据写入时核实目标、影响范围、回滚和当轮已有授权。
- 本地开发测试使用 `docker/.env.local.example` 派生的 `.env.local`；核心系统数据库只使用本地 Oracle。OA、EHR、DHR、XCC6、BG/出差、考勤、临时人员、门禁等第三方系统不由本项目容器化部署，也不提供本地数据库替身；专项联调只在本地 env 指向对应测试环境。
- 线上测试及生产使用独立 runtime 编排和 env，不复用 `docker-compose.dev.yml`；真实环境文件、账号、密钥、日志、证书、客户数据与数据库快照不入库。
- worktree 不隔离端口、容器、数据库或真实设备。启动、联调及部署前单独确认外部资源归属；涉及外部写入或服务变更时按明确授权执行。

## 验证

按修改模块及风险选择最小有效验证；命令必须从对应子项目或 Maven reactor 根目录运行，不从仓库顶层误选 Maven 模块。命令与可用脚本以当前 README / package.json / pom 为准。

| 范围 | 工作目录与常用验证 |
| --- | --- |
| 基础平台 Java | 从 `smart/` 运行 `mvn -pl <module> -am test`；需要打包时运行对应 `package` |
| 业务微服务 Java | 从 `smart-module/` 运行 `mvn -pl <module>/<service> -am test` 或 `package`，先核对实际 reactor 模块路径 |
| 管理端 | 从 `smart-ui/` 按影响执行 `pnpm lint`、`pnpm test`、`pnpm build` |
| 当前 H5 | 从 `smart-h5/` 按影响执行 `pnpm check`、`pnpm test`、`pnpm e2e`、`pnpm build`；Mock E2E 不代表真实微信、设备或后端验收 |
| App | `smart-app-uniapp/` 无 CLI 构建；使用 HBuilderX 运行或打包，真实设备结果单独说明 |
| 文档与低风险配置 | 链接、内容、语法、差异与相关现有检查；不写镜像实现的测试，不跑不相关的全业务构建 |

代码行为修改优先补可复现失败的测试，再实现并运行对应回归；禁止削弱有效断言或安全校验换取通过。`-DskipTests` 打包不等于测试通过；本地成功不等于 CI 或生产通过。报告实际运行的检查、失败与未验证范围，只有出现新修改、失败或未决风险才扩大或重复验证。
