# smart-platform-core

`smart-platform-core` 是平台业务核心模块。

## 目录结构

```text
smart-platform-core/
├── README.md
├── pom.xml
└── src/
    ├── main/java/
    └── test/java/
```

## 模块边界

- 放平台核心实体、规则、Mapper/Service 复用逻辑。
- 可部署服务入口在 `../smart-platform-biz/`。
- 不放前端页面逻辑；管理端页面在 `../../../smart-ui/`。
