# smart-h5/scripts

`smart-h5/scripts/` 保存微信 H5 构建/部署辅助脚本。

## 目录结构

```text
smart-h5/scripts/
├── README.md
├── build.mjs                       # 读取 .env.* 并执行 Next standalone 构建
├── deploy-env.mjs                  # 解析/校验生产和测试环境变量
└── prepare-standalone-deploy.mjs   # 整理 Next standalone 部署产物
```

## 使用范围

- `pnpm build` / `pnpm build:prod` 读取 `.env.production.local` 后构建生产包。
- `pnpm build:test` 读取 `.env.test.local` 后构建测试包。
- 构建缺少 `NEXT_PUBLIC_SECURITY_ENCODE_KEY` 时会快速失败，避免上线后门锁动态码页面才报错。
- standalone 构建后会把密钥注入 `.next/standalone/public/config.js`，打 tar 后上传即可使用。
- 新增脚本时写清输入、输出、是否修改构建产物。
- 构建产物和压缩包不要提交。
