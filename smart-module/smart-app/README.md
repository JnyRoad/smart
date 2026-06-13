# smart-app

`smart-app` 是裕慧家园 App / 微信相关业务聚合模块，POM 描述为“App业务模块”。

现有 `smart-app-biz/README.md` 说明它包含 App 管理后台功能、微信公众号链接页面接口和官网链接页面接口。

## 目录结构

```text
smart-app/
├── README.md
├── pom.xml
├── smart-app-api/      # App 服务对外 API / Feign 契约
└── smart-app-biz/      # App 业务实现和 Spring Boot 启动入口
    ├── README.md
    ├── Dockerfile
    └── src/main/java/com/tce/smart/app/SmartAppApplication.java
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-app-api/` | 对其他服务暴露 App 相关接口和类型。 |
| `smart-app-biz/` | 可部署 App 业务服务，处理移动端、微信公众号/H5 相关后端接口。 |

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-app -am package -DskipTests
mvn -pl smart-app/smart-app-biz -am package -DskipTests
```

可部署 Jar：

```text
smart-app/smart-app-biz/target/smart-app-biz.jar
```
