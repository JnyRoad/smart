# smart-bridge

`smart-bridge` 是非 ISC 通道的设备桥接与园区通行业务聚合模块，现有说明指出它负责各园区业务，包括通行记录图片存储、园区设备任务等。

与 `smart-bridge-isc` 的主要区别是：本模块不走海康 ISC 通道。

## 目录结构

```text
smart-bridge/
├── README.md
├── pom.xml
├── smart-bridge-api/    # 对外 API / Feign 契约
├── smart-bridge-core/   # 设备桥接和通行业务核心类型/规则
└── smart-bridge-biz/    # 可部署业务服务
    ├── Dockerfile
    ├── database/
    ├── hbase_shell/
    └── src/main/
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-bridge-api/` | 对外暴露桥接相关接口和类型。 |
| `smart-bridge-core/` | 桥接领域核心代码，被 BIZ 和其他模块复用。 |
| `smart-bridge-biz/` | 可部署服务，入口为 `SmartBridgeApplication`。 |

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-bridge -am package -DskipTests
mvn -pl smart-bridge/smart-bridge-biz -am package -DskipTests
```

可部署 Jar：

```text
smart-bridge/smart-bridge-biz/target/smart-bridge-biz.jar
```
