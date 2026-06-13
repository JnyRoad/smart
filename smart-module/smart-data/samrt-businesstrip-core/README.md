# samrt-businesstrip-core

`samrt-businesstrip-core` 是出差数据相关 core 模块。目录名沿用历史拼写 `samrt`，不要在未统一改引用前单独重命名。

## 目录结构

```text
samrt-businesstrip-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放出差数据视图/模型/访问相关核心代码。
- 不放可部署服务启动入口。
- 外部数据库连接信息应通过环境配置或 Nacos 注入，不写入 README。
