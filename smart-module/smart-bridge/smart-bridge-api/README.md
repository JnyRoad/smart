# smart-bridge-api

`smart-bridge-api` 是非 ISC 桥接服务对外契约模块。

## 目录结构

```text
smart-bridge-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放桥接服务 API、DTO、Feign Client 等契约。
- 不放设备通信实现。
- 变更契约前要检查平台、调度、业务服务调用方。
