# smart-dispatcher

`smart-dispatcher` 是多园区请求调度聚合模块。现有说明指出它负责把园区请求转发到对应园区，例如查看通行记录图片、人脸下发等。

## 目录结构

```text
smart-dispatcher/
├── README.md
├── pom.xml
├── smart-dispatcher-api/   # 调度服务 API / Feign 契约
└── smart-dispatcher-biz/   # 可部署调度服务
    ├── Dockerfile
    └── src/main/java/com/tce/smart/dispatcher/SmartDispatcherApplication.java
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-dispatcher-api/` | 对其他服务暴露调度相关接口和类型。 |
| `smart-dispatcher-biz/` | 可部署服务，处理跨园区请求分发和调用编排。 |

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-dispatcher -am package -DskipTests
mvn -pl smart-dispatcher/smart-dispatcher-biz -am package -DskipTests
```

可部署 Jar：

```text
smart-dispatcher/smart-dispatcher-biz/target/smart-dispatcher-biz.jar
```
