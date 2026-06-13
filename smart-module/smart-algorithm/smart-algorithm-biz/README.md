# smart-algorithm-biz

`smart-algorithm-biz` 是算法服务业务实现模块。

## 目录结构

```text
smart-algorithm-biz/
├── README.md
├── pom.xml
├── Dockerfile
└── src/main/java/com/tce/smart/algorithm/SmartAlgorithmApplication.java
```

## 模块边界

- 放算法服务 Controller、Service、配置和启动入口。
- 对外契约放在 `../smart-algorithm-api/`。
- 发布脚本收集 `target/smart-algorithm-biz.jar`。
