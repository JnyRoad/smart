# smart-dispatcher-api

`smart-dispatcher-api` 是调度服务对外契约模块。

## 目录结构

```text
smart-dispatcher-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放跨园区调度相关 API、DTO、Feign Client 等契约。
- 不放请求转发实现。
- 被桥接、平台、调度服务调用时要保持接口兼容。
