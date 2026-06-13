# smart-schedule

`smart-schedule` 是定时任务服务，POM 描述为“定时任务模块”。

它是独立 Spring Boot 服务，不再拆 `api/biz/core`，依赖平台、数据、调度、工具等模块执行定时任务；POM 中包含 ShedLock 相关依赖，用于分布式任务互斥。

## 目录结构

```text
smart-schedule/
├── README.md
├── pom.xml
├── Dockerfile
└── src/
    ├── main/
    │   ├── java/com/tce/smart/schedule/SmartScheduleApplication.java
    │   └── resources/
    └── test/
```

## 模块边界

- 定时任务编排、任务入口、调度配置放在本模块。
- 被调业务规则仍应放在对应业务模块，例如平台规则在 `smart-platform`，数据同步规则在 `smart-data`。
- 不要把定时任务专用逻辑上移到 `smart-tool`，除非已被多个模块复用。

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-schedule -am package -DskipTests
mvn -pl smart-schedule -am test
```

可部署 Jar：

```text
smart-schedule/target/smart-schedule.jar
```
