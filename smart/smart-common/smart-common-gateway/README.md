# smart-common-gateway

`smart-common-gateway` 是网关公共支撑模块，POM 描述为“smart gateway”。

## 目录结构

```text
smart-common-gateway/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 Spring Cloud Gateway 相关的公共过滤器、路由和 Redis 网关支撑。
- 可部署网关服务入口在 `../../smart-gateway/`。
- 不放业务服务接口实现。
