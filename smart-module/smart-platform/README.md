# smart-platform

`smart-platform` 是管理后台主要业务聚合模块，POM 描述为“平台业务模块”。

它与 `smart-ui/` 管理端前端配合，承载园区平台业务 API、核心实体/规则和业务实现。

## 目录结构

```text
smart-platform/
├── README.md
├── pom.xml
├── smart-platform-api/    # 平台服务 API / Feign 契约
├── smart-platform-core/   # 平台核心实体、规则和复用逻辑
└── smart-platform-biz/    # 可部署平台业务服务
    ├── README.md
    ├── Dockerfile
    └── src/main/java/com/tce/smart/platform/SmartPlatformApplication.java
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-platform-api/` | 对其他服务暴露平台业务接口和类型。 |
| `smart-platform-core/` | 平台业务核心实体、规则、Mapper/Service 复用代码。 |
| `smart-platform-biz/` | 可部署平台业务服务，承载管理后台业务接口实现。 |

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-platform -am package -DskipTests
mvn -pl smart-platform/smart-platform-biz -am test
```

可部署 Jar：

```text
smart-platform/smart-platform-biz/target/smart-platform-biz.jar
```
