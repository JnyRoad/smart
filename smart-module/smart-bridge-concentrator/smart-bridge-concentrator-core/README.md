# smart-bridge-concentrator-core

`smart-bridge-concentrator-core` 是水电表集中器模块的核心类型包。

## 目录结构

```text
smart-bridge-concentrator-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 DTO、枚举、统一返回对象和异常定义。
- 不放 Netty 服务端、TCP 客户端、HTTP Controller 或 Kafka 上报实现。
- 可部署服务入口在 `../smart-bridge-concentrator-biz/`。
