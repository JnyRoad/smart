# smart-bridge-isc-api

`smart-bridge-isc-api` 是 ISC 桥接服务对外契约模块。

## 目录结构

```text
smart-bridge-isc-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 ISC 桥接相关 API、DTO、Feign Client 等契约。
- 不放海康 ISC 业务实现或设备通信细节。
- 变更契约前要检查平台、调度、App 和其他桥接调用方。
