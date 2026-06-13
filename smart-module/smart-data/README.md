# smart-data

`smart-data` 是裕同数据通讯聚合模块，POM 描述为“裕同数据通讯模块”。它承载员工、出差、人事视图、安保等外部数据视图和同步相关代码；车辆 core 物理放在本目录下，但 Maven 聚合关系见下方特殊说明。

## 目录结构

```text
smart-data/
├── README.md
├── pom.xml
├── smart-data-api/            # 数据服务 API / Feign 契约
├── smart-data-biz/            # 可部署数据服务
├── samrt-businesstrip-core/   # 出差数据 core；目录名沿用历史拼写 samrt
├── smart-ehrview-core/        # EHR 视图 core
├── smart-dhrview-core/        # DHR 视图 core
├── smart-guard-core/          # 安保数据 core
├── smart-temporary-core/      # 临时数据 core
├── smart-xcc6-core/           # XCC6 数据 core
└── smart-xcvehicle-core/      # 车辆数据 core；物理在本目录下，但由 smart-module 根 POM 直接聚合
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-data-api/` | 对其他服务暴露数据相关接口和类型。 |
| `smart-data-biz/` | 可部署数据服务，入口为 `SmartDataApplication`。 |
| `samrt-businesstrip-core/` | 出差数据相关 core，目录名保持历史拼写。 |
| `smart-ehrview-core/` | EHR 数据视图相关 core。 |
| `smart-dhrview-core/` | DHR 数据视图相关 core。 |
| `smart-guard-core/` | 安保数据相关 core。 |
| `smart-temporary-core/` | 临时数据相关 core。 |
| `smart-xcc6-core/` | XCC6 数据相关 core。 |

## 特殊聚合关系

`smart-xcvehicle-core/` 物理位于 `smart-data/` 下，但它没有出现在 `smart-data/pom.xml` 的 `<modules>` 中；当前由 `smart-module/pom.xml` 通过 `smart-data/smart-xcvehicle-core` 直接聚合。需要构建车辆 core 时，不要只依赖 `mvn -pl smart-data` 覆盖它。

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-data -am package -DskipTests
mvn -pl smart-data/smart-data-biz -am package -DskipTests
mvn -pl smart-data/smart-xcvehicle-core -am package -DskipTests
```

可部署 Jar：

```text
smart-data/smart-data-biz/target/smart-data-biz.jar
```
