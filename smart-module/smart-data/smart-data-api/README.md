# smart-data-api

`smart-data-api` 是数据服务对外契约模块。

## 目录结构

```text
smart-data-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放数据同步/数据视图相关 API、DTO、Feign Client 等契约。
- 不放外部库连接配置和同步实现。
- 变更契约前要检查调度、平台、App 等调用方。
