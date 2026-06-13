# smart-push-biz

`smart-push-biz` 是 App 推送业务服务。

## 目录结构

```text
smart-push-biz/
├── README.md
├── pom.xml
├── Dockerfile
└── src/main/java/com/tce/smart/push/SmartPushApplication.java
```

## 模块边界

- 放推送业务实现、推送渠道适配和运行配置。
- 对外契约放在 `../smart-push-api/`。
- 发布脚本收集 `target/smart-push-biz.jar`。
