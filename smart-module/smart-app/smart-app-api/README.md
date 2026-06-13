# smart-app-api

`smart-app-api` 是裕慧家园 App 服务对外契约模块。

## 目录结构

```text
smart-app-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 App/微信/H5 相关 API、DTO、Feign Client 等契约。
- 不放 Controller 业务实现。
- 变更契约前要检查 `smart-h5`、`smart-ui` 和其他后端调用方。
