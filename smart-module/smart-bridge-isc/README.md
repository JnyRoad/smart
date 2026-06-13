# smart-bridge-isc

`smart-bridge-isc` 是海康 ISC 通道的设备桥接业务聚合模块。现有说明明确：`smart-bridge` 不支持 ISC 途径，`smart-bridge-isc` 支持 ISC 途径。

## 目录结构

```text
smart-bridge-isc/
├── README.md
├── pom.xml
├── smart-bridge-isc-api/    # ISC 对外 API / Feign 契约
├── smart-bridge-isc-core/   # ISC 领域核心类型/规则
└── smart-bridge-isc-biz/    # 可部署 ISC 业务服务
    ├── Dockerfile
    ├── database/
    ├── hbase_shell/
    └── src/main/java/com/tce/smart/bridge/isc/SmartBridgeISCApplication.java
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-bridge-isc-api/` | 对外暴露 ISC 相关接口和类型。 |
| `smart-bridge-isc-core/` | ISC 业务核心类型、实体、规则和复用逻辑。 |
| `smart-bridge-isc-biz/` | 可部署服务，承载 ISC 设备/人员/卡片/通行记录等接口实现。 |

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-bridge-isc -am package -DskipTests
mvn -pl smart-bridge-isc/smart-bridge-isc-biz -am test
```

可部署 Jar：

```text
smart-bridge-isc/smart-bridge-isc-biz/target/smart-bridge-isc-biz.jar
```
