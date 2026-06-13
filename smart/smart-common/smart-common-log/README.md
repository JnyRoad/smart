# smart-common-log

`smart-common-log` 是公共日志模块，POM 描述为“日志服务”。

## 目录结构

```text
smart-common-log/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放操作日志、审计日志、日志注解/AOP 等跨服务能力。
- 不放单个业务模块私有的日志落库逻辑。
- 修改日志模型时要确认 UPMS 日志表和调用方兼容性。
