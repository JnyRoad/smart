# smart-temporary-core

`smart-temporary-core` 是临时/中间表数据 core 模块。

## 目录结构

```text
smart-temporary-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放临时数据、中间表或阶段性同步数据相关模型和访问逻辑。
- 不放可部署服务启动入口。
- 外部数据库连接信息应通过环境配置或 Nacos 注入，不写入 README。
