# AGENTS.md

本文件是 `smart` 项目的项目级规则，适用于整个仓库。这里只记录仓库组织方式和各子项目入口；通用协作、代码风格、Git 和提交规则走全局规则。

## 仓库组织

- 根目录是统一 Git 仓库；子项目按目录平铺，例如 `smart-h5/`，后续新增模块继续作为根目录同级子目录。
- 子项目不要再初始化独立 `.git`，也不要默认使用 submodule。需要例外时先说明原因。
- 新增子项目时，同步更新根 `README.md` 和本文件的“子项目”清单。
- 历史设计文档放在对应子项目自己的 `docs/` 下；实现时以当前代码、当前 README、当前配置为准。

## 通用开发约定

- 先确认改动属于哪个子项目，在该子项目目录内执行安装、检查、测试和构建命令。
- 测试按金字塔组织：纯规则和工具优先单测，模块协作用集成测试，关键用户路径再补 E2E。
- 影响业务行为的改动按 TDD 走：先补能暴露问题的测试，再实现，再重跑相关验证。
- 提交前确认没有依赖、构建产物、测试报告、环境文件、日志、证书进入暂存区。

## 子项目

### `smart-h5`

- 用途：智慧园区微信 H5 应用。
- 技术栈：Next.js 16、React 19、TypeScript strict、antd-mobile 5、Tailwind CSS 4、TanStack Query、Zustand、Vitest、Playwright。
- 常用命令：在 `smart-h5/` 内执行 `pnpm check`、`pnpm test`、`pnpm e2e`、`pnpm build`。
- 目录边界：`src/app/` 放路由页面，`src/features/<module>/` 放业务域 API/状态/纯函数，`src/lib/` 放共享基础设施，`src/components/` 放跨模块组件，`e2e/` 放 Playwright。
