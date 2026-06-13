# smart-h5/public

`smart-h5/public/` 保存 Next.js 原样发布的静态资源和运行时配置。

## 目录结构

```text
smart-h5/public/
├── README.md
├── config.js     # 运行时租户配置，挂载 window.__SMART_CONFIG__
├── file.svg
├── globe.svg
├── next.svg
├── vercel.svg
└── window.svg
```

## 维护规则

- `config.js` 是部署期可覆盖配置，不要写真实密钥。
- 放在本目录的文件会按路径公开访问。
- 业务图片优先按功能归属放置并命名清楚。
