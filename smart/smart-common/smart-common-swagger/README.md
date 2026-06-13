# smart-common-swagger

`smart-common-swagger` 是公共接口文档模块，POM 描述为“接口文档”。

## 目录结构

```text
smart-common-swagger/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 Swagger 2 / swagger-bootstrap-ui 相关公共配置。
- 不放业务接口定义；接口定义在各业务模块的 `api` 子模块。
- 新增接口文档能力时注意 Spring Boot 2.1 / Swagger 2.9 的兼容性。
