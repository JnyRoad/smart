# smart-data-biz

`smart-data-biz` 是数据服务业务实现模块。

## 目录结构

```text
smart-data-biz/
├── README.md
├── pom.xml
├── Dockerfile
└── src/main/java/com/tce/smart/data/SmartDataApplication.java
```

## 模块边界

- 放数据同步、外部数据视图接口实现和运行配置。
- 对外契约放在 `../smart-data-api/`。
- 各类外部数据模型/访问逻辑按来源拆到对应 `*-core`。
- 发布脚本收集 `target/smart-data-biz.jar`。
