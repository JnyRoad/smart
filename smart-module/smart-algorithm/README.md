# smart-algorithm

`smart-algorithm` 是算法业务聚合模块，POM 描述为“算法模块”。

## 目录结构

```text
smart-algorithm/
├── README.md
├── pom.xml
├── smart-algorithm-api/   # 算法服务对外 API / Feign 契约
└── smart-algorithm-biz/   # 算法业务实现和 Spring Boot 启动入口
    ├── Dockerfile
    └── src/main/java/com/tce/smart/algorithm/SmartAlgorithmApplication.java
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-algorithm-api/` | 对其他服务暴露算法相关接口和类型。 |
| `smart-algorithm-biz/` | 可部署算法服务，承载接口实现和运行配置。 |

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-algorithm -am package -DskipTests
mvn -pl smart-algorithm/smart-algorithm-biz -am package -DskipTests
```

可部署 Jar：

```text
smart-algorithm/smart-algorithm-biz/target/smart-algorithm-biz.jar
```
