# smart-common-core

`smart-common-core` 是公共核心工具包，POM 描述为“公共工具类核心包”。

## 目录结构

```text
smart-common-core/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放统一返回、异常、基础工具、通用类型和跨模块复用代码。
- 不放需要访问具体业务表的逻辑。
- 不放只服务单个业务模块的工具。
