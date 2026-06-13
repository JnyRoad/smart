# smart-xcc6-core

`smart-xcc6-core` 是 XCC6 数据相关 core 模块。

## 目录结构

```text
smart-xcc6-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 XCC6 数据模型、Mapper/Service 和访问逻辑。
- 不放可部署服务启动入口。
- 外部数据库连接信息应通过环境配置或 Nacos 注入，不写入 README。
