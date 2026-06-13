# smart-platform-api

`smart-platform-api` 是平台业务对外契约模块。

## 目录结构

```text
smart-platform-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放平台业务 API、DTO、Feign Client 等契约。
- 不放管理后台业务实现。
- 变更契约前要检查 `smart-ui`、`smart-app`、调度和其他业务调用方。
