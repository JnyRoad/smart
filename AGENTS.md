# AGENTS.md

本文件是 `smart` 项目的项目级规则，适用于整个仓库。这里只记录仓库组织方式和各子项目入口；通用协作、代码风格、Git 和提交规则走全局规则。

## 仓库组织

- 子项目按目录平铺，例如 `smart/`、`smart-module/`、`smart-ui/`、`smart-h5/`、`smart-h5-vue2/`，后续新增模块继续作为根目录同级子目录。
- 子项目不要初始化独立 `.git`；需要例外时先说明原因。
- 新增子项目时，同步更新根 `README.md` 和本文件的“子项目”清单。
- 设计文档放在对应子项目自己的 `docs/` 下；实现时以当前代码、当前 README、当前配置为准。

## 通用开发约定

- 先确认改动属于哪个子项目，在该子项目目录内执行安装、检查、测试和构建命令。
- 测试按金字塔组织：纯规则和工具优先单测，模块协作用集成测试，关键用户路径再补 E2E。
- 影响业务行为的改动按 TDD 走：先补能暴露问题的测试，再实现，再重跑相关验证。
- 提交前确认没有依赖、构建产物、测试报告、环境文件、日志、证书进入暂存区。

## 子项目

### `smart`

- 用途：智慧园区基础平台后端，包含网关、认证、UPMS 和公共组件。
- 技术栈：Java 8、Maven、Spring Boot 2.1、Spring Cloud Greenwich、Nacos、Redis。
- 常用命令：在 `smart/` 内执行 `mvn clean install -DskipTests`；单服务用 `mvn -pl <module> -am package -DskipTests`。
- 目录边界：`smart-gateway/` 放网关服务，`smart-auth/` 放认证服务，`smart-upms/` 放用户权限服务，`smart-common/` 放跨服务公共组件。

### `smart-module`

- 用途：智慧园区业务微服务后端，包含 App、平台、数据、桥接、ISC、推送、调度等业务模块。
- 技术栈：Java 8、Maven、Spring Boot、Spring Cloud、MyBatis / MyBatis-Plus、Nacos、Kafka。
- 常用命令：在 `smart-module/` 内执行 `mvn clean package -DskipTests`；单服务用 `mvn -pl <module>/<service> -am test` 或 `package`。
- 目录边界：业务按一级目录划分；`api` 放服务契约，`biz` 放可部署服务，`core` 放领域复用代码，`database/manual/` 放人工数据库脚本。

### `smart-ui`

- 用途：智慧园区管理后台前端。
- 技术栈：Vue 2、Element UI、Avue、Vue CLI、pnpm。
- 常用命令：在 `smart-ui/` 内执行 `pnpm install`、`pnpm dev`、`pnpm lint`、`pnpm test`、`pnpm build`。
- 目录边界：`src/api/` 放接口封装，`src/views/` 放业务页面，`src/router/` 放路由和 axios 配置，`src/store/` 放 Vuex，`public/` 放原样发布静态资源。

### `smart-h5`

- 用途：当前维护的智慧园区微信 H5 应用；后续移动端 H5 功能更新统一落在这里。
- 技术栈：Next.js 16、React 19、TypeScript strict、antd-mobile 5、Tailwind CSS 4、TanStack Query、Zustand、Vitest、Playwright。
- 常用命令：在 `smart-h5/` 内执行 `pnpm check`、`pnpm test`、`pnpm e2e`、`pnpm build`。
- 目录边界：`src/app/` 放路由页面，`src/features/<module>/` 放业务域 API/状态/纯函数，`src/lib/` 放共享基础设施，`src/components/` 放跨模块组件，`e2e/` 放 Playwright。

### `smart-h5-vue2`

- 用途：历史 Vue2 微信公众号版 H5，仅作为旧页面、旧交互和旧接口调用方式的参考。
- 维护状态：不再维护、不再发布、不承接新功能；新需求和缺陷修复应改在 `smart-h5/`。
- 技术栈：Vue 2、Vue CLI、Vue Router、Vuex、cube-ui、pnpm。
- 常用命令：默认不执行；如需本地核对历史行为，可在 `smart-h5-vue2/` 内执行 `pnpm install`、`pnpm run serve`、`pnpm run test`、`pnpm run build`。
- 目录边界：`src/views-mobile/` 放旧移动端页面，`src/router/` 放旧路由，`src/services/` 放旧接口封装，`src/components/` 放旧通用组件；只读对标优先，不主动扩展业务。
