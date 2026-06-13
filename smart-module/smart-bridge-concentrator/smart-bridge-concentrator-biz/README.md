# smart-bridge-concentrator-biz

`smart-bridge-concentrator-biz` 是水电表集中器可部署服务。

## 目录结构

```text
smart-bridge-concentrator-biz/
├── README.md
├── pom.xml
└── src/main/java/com/tce/smart/bridge/SmartBridgeConcentratorApplication.java
```

## 模块边界

- 放 HTTP 接口、Netty 服务端、外置阀门 TCP 客户端、协议报文工具和 Kafka 上报逻辑。
- 核心 DTO/枚举/异常放在 `../smart-bridge-concentrator-core/`。
- 发布脚本收集 `target/smart-bridge-concentrator-biz.jar`。
