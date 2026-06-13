# smart-guard-core

`smart-guard-core` 是安保/物流车辆等外部数据 core 模块。

## 目录结构

```text
smart-guard-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放安保相关外部数据模型、Mapper/Service 和访问逻辑。
- 不放可部署服务启动入口。
- 外部数据库连接信息应通过环境配置或 Nacos 注入，不写入 README。
