# smart-push

`smart-push` 是 App 消息推送聚合模块，POM 描述为“App消息推送模块”。

## 目录结构

```text
smart-push/
├── README.md
├── pom.xml
├── smart-push-api/    # 推送服务 API / Feign 契约
└── smart-push-biz/    # 可部署推送服务
    ├── Dockerfile
    └── src/main/java/com/tce/smart/push/SmartPushApplication.java
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-push-api/` | 对其他服务暴露推送相关接口和类型。 |
| `smart-push-biz/` | 可部署推送业务服务。 |

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-push -am package -DskipTests
mvn -pl smart-push/smart-push-biz -am package -DskipTests
```

可部署 Jar：

```text
smart-push/smart-push-biz/target/smart-push-biz.jar
```
