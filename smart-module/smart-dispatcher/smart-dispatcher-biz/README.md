# smart-dispatcher-biz

`smart-dispatcher-biz` 是多园区请求调度业务服务。

## 目录结构

```text
smart-dispatcher-biz/
├── README.md
├── pom.xml
├── Dockerfile
└── src/main/java/com/tce/smart/dispatcher/SmartDispatcherApplication.java
```

## 模块边界

- 放跨园区请求转发、调用编排和调度服务实现。
- 对外契约放在 `../smart-dispatcher-api/`。
- 发布脚本收集 `target/smart-dispatcher-biz.jar`。
