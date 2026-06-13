# smart-upms-api

`smart-upms-api` 是 UPMS 对外契约模块。

## 目录结构

```text
smart-upms-api/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 UPMS Entity、DTO、Feign Client、接口常量等可被其他服务依赖的契约。
- 不放 Controller 业务实现。
- 变更字段或接口时要检查 `smart-auth`、`smart-common-security` 和 `smart-module` 调用方。
