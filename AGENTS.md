# AGENTS.md

裕同智慧园区 `smart` 项目，子项目平铺为同级目录。

> 边界：通用方法论（工程原则、TDD、代码评审、提交/PR 规范、分支命名、Git 认证）走各 agent
> 自己的全局规则；详细介绍看 README；本文件只记本项目特有的规则、结构、技术栈和架构。

## 仓库组织

- 子项目按目录平铺，例如 `smart/`、`smart-module/`、`smart-ui/`、`smart-h5/`、`smart-h5-vue2/`、`smart-app-uniapp/`，后续新增模块继续作为根目录同级子目录。
- 子项目不要初始化独立 `.git`；需要例外时先说明原因。
- 新增子项目时，同步更新根 `README.md` 和本文件的“子项目”清单。
- 设计文档放在对应子项目自己的 `docs/` 下；实现时以当前代码、当前 README、当前配置为准。

## 开发约定

- 代码必须充分写注释，且注释一律用中文（行/块注释、JSDoc / TSDoc、JavaDoc、SQL、配置、测试注释）；历史代码普遍缺注释，新开发把补齐必要注释作为完成标准，改到的复杂逻辑同步补上。
- 所有改动走功能分支 + PR 合并 `main`，禁止直推 `main`；提交前确认依赖、构建产物、测试报告、环境文件、日志、证书、数据库快照都没进暂存区。

## 子项目与技术栈

| 目录 | 用途 | 技术栈 | 常用命令 |
|---|---|---|---|
| `smart/` | 基础平台后端，包含网关、认证、UPMS 和公共组件 | Java 8、Maven、Spring Boot 2.1、Spring Cloud Greenwich、Nacos、Redis | `mvn clean install -DskipTests`；单服务 `mvn -pl <module> -am package -DskipTests` |
| `smart-module/` | 业务微服务后端，包含 App、平台、数据、桥接、ISC、推送、调度等业务模块（清单见下） | Java 8、Maven、Spring Boot、Spring Cloud、MyBatis / MyBatis-Plus、Nacos、Kafka | `mvn clean package -DskipTests`；单服务 `mvn -pl <module>/<service> -am test` 或 `package` |
| `smart-ui/` | 管理后台前端 | Vue 2、Element UI、Avue、Vue CLI、pnpm | `pnpm install`、`pnpm dev`、`pnpm lint`、`pnpm test`、`pnpm build` |
| `smart-h5/` | 当前维护的微信 H5 应用，本次开发的模块（功能与目录见下） | Next.js 16、React 19、TypeScript strict、antd-mobile 5、Tailwind CSS 4、TanStack Query、Zustand、Vitest、Playwright | `pnpm check`、`pnpm test`、`pnpm e2e`、`pnpm build` |
| `smart-h5-vue2/` | 历史 Vue2 微信公众号版 H5，只读参考，不再维护/发布 | Vue 2、Vue CLI、Vue Router、Vuex、cube-ui、pnpm | 默认不执行；`pnpm install`、`pnpm run serve`、`pnpm run test`、`pnpm run build` |
| `smart-app-uniapp/` | 「裕慧家园」移动 App 客户端（Android / iOS），与 `smart-h5` 并行使用：App 场景用它、公众号场景用 H5；对接 `smart-module/smart-app` 后端（注意二者是客户端与后端模块的关系，勿混淆） | uni-app（HBuilderX 可视化工程、App-plus）、Vue 2、Vuex、UniPush | 无 CLI 构建；HBuilderX 打开目录，`npm install` 后用 IDE 运行 / 云打包 |

### `smart-module` 模块清单

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

### `smart-h5` 功能清单

> 当前已覆盖以下移动端流程，后续在 `src/app/` 路由 + `src/features/` 业务域结构内扩展。

| 功能 | 路由 `src/app/` |
|---|---|
| 认证 / 登录 | `login`、`dev-login` |
| 首页 / 公告 | `home` |
| 我的 | `mine` |
| 访客（邀请 / 通行） | `visitor` |
| 通行码 | `code` |
| 帮助中心 | `help` |
| 宿舍 / 门锁 | `dorm` |
| 宿舍报修 | `dorm-repairs` |
| 入住 / 退宿 | `check-in`、`dorm-exit` |
| 物品放行 | `good-release` |
| 返厂 | `return-factory` |
| 待办审批 | `backlog` |

### `smart-h5` 目录结构（以实际代码为准）

- `src/app/`：App Router 路由页面壳，每个功能一个目录（共 14 个，见功能清单）+ `layout.tsx` / `page.tsx` / `globals.css`；无 `api/` 路由，接口经 `next.config` rewrites 代理到网关，非 BFF。
- `src/features/<域>/`：业务域逻辑，每域含 `api.ts`（接口调用）、纯函数业务规则、`flow-store.ts`（Zustand 流程状态）、`*-mock.ts`（mock 数据）和大量 `*.test.ts`（Vitest 单测）。现有 9 域：`auth`、`home`、`visitor`、`dorm`、`dorm-services`、`employee`、`good-release`、`help`、`backlog`。
- `src/lib/`：共享基础设施。
  - `api/`：API 兼容层（`client` 请求封装、`endpoints` 端点、`types`）
  - `auth/`：认证态（`session` token 存储、`tenant` 租户、`wx-oauth` 微信网页授权）
  - `wechat/`：微信 JS-SDK（`jssdk`）
  - `config/`：运行时 / 环境配置（`runtime`、`env`）
  - `crypto/`：加解密（`aes`、国密 `sm`）
  - `format/`：格式化（`datetime`、`photo`）
  - 通用 hooks 与工具：`use-mounted`、`use-list-pager`、`react19-compat`、`photo-id`、`text`
- `src/components/`：跨模块通用组件 —— `page-shell`（页面壳）、`face-upload` / `face-avatar`（人脸采集 / 头像）、`plate-input`（车牌输入）、`sms-code-field`（短信验证码）、`approval-timeline`（审批时间线）、`visitor-steps`（访客步骤）、`image-list-upload`、`segment-tabs`、`rich-text-body`、`query-provider` 等。

## 目录边界（各子项目，原版逐字保留）

- `smart/`：`smart-gateway/` 放网关服务，`smart-auth/` 放认证服务，`smart-upms/` 放用户权限服务，`smart-common/` 放跨服务公共组件。
- `smart-module/`：业务按一级目录划分；`api` 放服务契约，`biz` 放可部署服务，`core` 放领域复用代码，`database/manual/` 放人工数据库脚本。
- `smart-ui/`：`src/api/` 放接口封装，`src/views/` 放业务页面，`src/router/` 放路由和 axios 配置，`src/store/` 放 Vuex，`public/` 放原样发布静态资源。
- `smart-h5/`：见上方「`smart-h5` 目录结构」。
- `smart-h5-vue2/`：`src/views-mobile/` 放旧移动端页面，`src/router/` 放旧路由，`src/services/` 放旧接口封装，`src/components/` 放旧通用组件；只读对标优先，不主动扩展业务。
- `smart-app-uniapp/`：`api/` 放接口封装，`pages/page/` 放业务页面，`components/` 放通用组件，`config/` 放端点常量与权限字典，`tools/` 放请求与存储封装，`static/` 放运行时静态资源，`unpackage/res/` 放 HBuilderX 生成的图标与启动图（被 manifest.json 引用必须入库，勿把 `unpackage/` 整体加入忽略）。

## 架构骨架

后端是 Spring Cloud 微服务：Nacos 做注册 / 配置中心；`smart-gateway`（Spring Cloud Gateway）是统一入口；`smart-auth` 负责认证、签发 token，`smart-upms` 管权限；服务间走 OpenFeign 调用；后端包名统一 `com.tce.smart`。
