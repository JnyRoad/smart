# smart-platform-biz

`smart-platform-biz` 是智慧园区平台业务服务。

## 目录结构

```text
smart-platform-biz/
├── README.md
├── pom.xml
├── Dockerfile
└── src/main/java/com/tce/smart/platform/SmartPlatformApplication.java
```

## 模块边界

- 放管理后台平台业务 Controller、Service、配置和启动入口。
- 对外契约放在 `../smart-platform-api/`，核心复用逻辑放在 `../smart-platform-core/`。
- 发布脚本收集 `target/smart-platform-biz.jar`。
