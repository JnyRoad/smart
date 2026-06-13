## smart-h5-vue2

`smart-h5-vue2` 是智慧园区旧版本 Vue2 微信公众号 H5 工程，来源于旧版移动端页面实现。

## 维护状态

此模块从现在开始不再维护、不再发布、不再承接新功能。后续微信 H5 的新需求、缺陷修复和发布验证统一在 `../smart-h5` 中完成。

保留此目录只用于参考旧版页面、旧交互、旧路由和旧接口调用方式。除非明确要求做历史对标或迁移核查，否则不要在此模块继续开发业务功能。

## 技术栈

- Vue 2.6
- Vue CLI 3
- Vue Router 3
- Vuex 3
- cube-ui
- pnpm 11

## 基础环境

```bash
node >=22
pnpm 11.x
```

首次使用建议通过 Corepack 启用 pnpm：

```bash
corepack enable
corepack prepare pnpm@11.4.0 --activate
```

## 参考命令

仅在需要核对旧版行为时执行：

```bash
pnpm install
pnpm run serve
pnpm run test
pnpm run build
```

构建输出目录为 `dist-h5/`，依赖目录为 `node_modules/`。这些都是本地生成内容，不应提交。
