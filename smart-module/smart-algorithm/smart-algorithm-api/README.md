# smart-algorithm-api

`smart-algorithm-api` 是算法服务对外契约模块。

## 目录结构

```text
smart-algorithm-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放算法服务 API、DTO、Feign Client 等调用契约。
- 不放算法业务实现和启动入口。
- 被其他模块依赖时应保持接口兼容。
