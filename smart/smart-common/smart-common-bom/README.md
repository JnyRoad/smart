# smart-common-bom

`smart-common-bom` 是公共依赖版本管理模块，POM 描述为“公共版本控制”。

## 目录结构

```text
smart-common-bom/
├── README.md
└── pom.xml
```

## 模块边界

- 只管理 Maven dependencyManagement、依赖源和发布相关配置。
- 不放 Java 源码和业务逻辑。
- 新增公共依赖版本时优先在这里统一声明。
