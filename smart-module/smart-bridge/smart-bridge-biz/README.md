# smart-bridge-biz

`smart-bridge-biz` 是非 ISC 桥接业务服务。

## 目录结构

```text
smart-bridge-biz/
├── README.md
├── pom.xml
├── Dockerfile
├── database/
├── hbase_shell/
└── src/main/java/com/tce/smart/bridge/SmartBridgeApplication.java
```

## 模块边界

- 放非 ISC 设备桥接、通行记录、图片相关业务实现。
- 对外契约放在 `../smart-bridge-api/`，核心复用逻辑放在 `../smart-bridge-core/`。
- 发布脚本收集 `target/smart-bridge-biz.jar`。
