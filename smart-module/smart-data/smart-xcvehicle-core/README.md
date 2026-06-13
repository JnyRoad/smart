# smart-xcvehicle-core

`smart-xcvehicle-core` 是许昌车辆数据相关 core 模块。

## 目录结构

```text
smart-xcvehicle-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放车辆数据模型、Mapper/Service 和访问逻辑。
- 不放可部署服务启动入口。
- 外部数据库连接信息应通过环境配置或 Nacos 注入，不写入 README。
