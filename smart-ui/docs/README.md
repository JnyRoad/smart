# smart-ui/docs

`smart-ui/docs/` 保存管理端前端的项目文档和专项说明。

## 目录结构

```text
smart-ui/docs/
├── README.md                          本文件，目录说明
├── lint-baseline.json                 ESLint warning 数量基线（按文件计数，只允许减少不允许新增）
└── platform-router-fingerprint.json   src/router/platform/index.js 的路由结构指纹（顶层路由数、重复路径位置）
```

## 维护规则

- 管理端专项设计、接口对版记录和功能说明放在这里。
- `lint-baseline.json` 由 `scripts/check-lint-baseline.mjs`、`platform-router-fingerprint.json` 由 `scripts/check-platform-router-fingerprint.mjs` 在 `pnpm gate` 中读取，是门禁运行时依赖的数据文件，不是给人读的说明文档；只能用对应脚本的 `--update` 参数重新生成，不要手工编辑，也不要当成普通文档删除——删除会导致 `pnpm gate` 直接报错退出。
- 与项目级或后端共用的资料放到 [../../docs/](../../docs/) 或对应后端模块。
- 不提交生产账号、密钥或真实导出数据。
