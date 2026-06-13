# smart-h5/e2e

`smart-h5/e2e/` 保存微信 H5 的 Playwright 端到端测试。

## 目录结构

```text
smart-h5/e2e/
├── README.md
├── helpers.ts
├── auth.spec.ts
├── home.spec.ts
├── visitor-*.spec.ts
├── dorm-*.spec.ts
├── good-release-*.spec.ts
└── ...
```

## 运行方式

在 `smart-h5/` 目录执行：

```bash
pnpm e2e
```

测试报告、截图、trace 和 `test-results/` 由 `.gitignore` 忽略。
