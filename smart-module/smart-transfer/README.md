# smart-transfer

`smart-transfer` 是历史数据传输模块。当前 POM 显示它依赖 Oracle、HBase、MyBatis、HTTP client、Thumbnailator、`smart-tool` 等，入口类为 `TransferApplication`。

当前根发布清单未包含它，默认不作为新功能入口。

## 目录结构

```text
smart-transfer/
├── README.md
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/tce/smart/transfer/TransferApplication.java
    │   └── resources/
    └── test/
```

## 模块边界

- 历史数据传输、HBase/外部数据搬迁相关逻辑在本模块。
- 当前数据同步主线优先查 `smart-data/`。
- 发布前必须确认它能产出 Spring Boot 可执行 Jar；当前 `release-jars.manifest` 未收集它。

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-transfer -am package -DskipTests
```
