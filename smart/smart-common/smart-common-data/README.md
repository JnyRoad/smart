# smart-common-data

`smart-common-data` 是公共数据访问基础模块，POM 描述为“数据操作相关”。

## 目录结构

```text
smart-common-data/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 Redis、Spring Data、ShedLock、数据访问增强等跨服务基础能力。
- 不放具体业务数据同步逻辑；业务同步在 `smart-module/smart-data/`。
- 修改公共数据行为时要检查 `smart/` 和 `smart-module/` 的编译影响。
