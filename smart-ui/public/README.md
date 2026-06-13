# smart-ui/public

`smart-ui/public/` 保存 Vue CLI 原样拷贝到 `dist/` 的静态资源和运行时配置。

## 目录结构

```text
smart-ui/public/
├── README.md
├── index.html       # HTML 模板
├── config.js        # 运行时配置覆盖
├── cdn/             # 第三方库 CDN 副本
├── img/             # 静态图片
├── resource/        # 业务静态资源
├── svg/             # SVG 资源
└── util/            # 早期加载工具脚本
```

## 维护规则

- 本目录内容会进入生产 `dist/`，不要放开发临时文件。
- `config.js` 可部署后覆盖，不要提交真实密钥。
- CDN 副本变更要同步检查 `vue.config.js` 的 externals 配置。
