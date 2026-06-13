# smart-bridge-isc-core

`smart-bridge-isc-core` 是 ISC 桥接业务核心模块。

## 目录结构

```text
smart-bridge-isc-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 ISC 领域核心类型、实体、规则和复用逻辑。
- 可部署服务入口在 `../smart-bridge-isc-biz/`。
- 不放 Controller 和服务启动类。
