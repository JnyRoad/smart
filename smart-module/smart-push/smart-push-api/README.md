# smart-push-api

`smart-push-api` 是 App 推送服务对外契约模块。

## 目录结构

```text
smart-push-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放推送相关 API、DTO、Feign Client 等契约。
- 不放推送渠道实现。
- 变更契约前要检查 App、平台和调度调用方。
