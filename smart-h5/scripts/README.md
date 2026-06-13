# smart-h5/scripts

`smart-h5/scripts/` 保存微信 H5 构建/部署辅助脚本。

## 目录结构

```text
smart-h5/scripts/
├── README.md
└── prepare-standalone-deploy.mjs   # 整理 Next standalone 部署产物
```

## 使用范围

- `pnpm build` 会调用 standalone 部署产物整理逻辑。
- 新增脚本时写清输入、输出、是否修改构建产物。
- 构建产物和压缩包不要提交。
