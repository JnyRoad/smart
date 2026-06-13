# smart-bridge-core

`smart-bridge-core` 是非 ISC 桥接业务核心模块。

## 目录结构

```text
smart-bridge-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放桥接领域核心类型、实体、规则和可复用逻辑。
- 可部署服务入口在 `../smart-bridge-biz/`。
- 不放 Controller 和外部发布入口。
