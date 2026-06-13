# smart-ui/scripts

`smart-ui/scripts/` 保存管理端前端的静态检查和专项验证脚本。

## 目录结构

```text
smart-ui/scripts/
├── README.md
├── check-admin-search.js
├── check-bundle-optimization.js
├── check-isc-card-fast-add-ui.js
└── check-isc-card-ui.js
```

## 使用方式

在 `smart-ui/` 目录执行：

```bash
pnpm check:admin-search
pnpm check:bundle
pnpm check:isc-card-ui
pnpm check:isc-card-fast-add-ui
```

## 维护规则

- 脚本应快速失败，发现不满足规则时返回非零退出码。
- 新增脚本要同步加入 `package.json` scripts，避免没人知道入口。
