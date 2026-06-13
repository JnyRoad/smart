# smart-dhrview-core

`smart-dhrview-core` 是 DHR 数据视图 core 模块。

## 目录结构

```text
smart-dhrview-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 DHR 数据视图相关模型、Mapper/Service 和访问逻辑。
- 不放可部署服务启动入口。
- 外部数据库连接信息应通过环境配置或 Nacos 注入，不写入 README。
