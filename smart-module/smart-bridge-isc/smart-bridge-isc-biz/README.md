# smart-bridge-isc-biz

`smart-bridge-isc-biz` 是 ISC 桥接业务服务。

## 目录结构

```text
smart-bridge-isc-biz/
├── README.md
├── pom.xml
├── Dockerfile
├── database/
├── hbase_shell/
└── src/main/java/com/tce/smart/bridge/isc/SmartBridgeISCApplication.java
```

## 模块边界

- 放海康 ISC 通道的设备、人员、卡片、通行记录等接口实现。
- 对外契约放在 `../smart-bridge-isc-api/`，核心复用逻辑放在 `../smart-bridge-isc-core/`。
- 发布脚本收集 `target/smart-bridge-isc-biz.jar`。
