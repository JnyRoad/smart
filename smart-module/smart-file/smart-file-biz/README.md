# smart-file-biz

`smart-file-biz` 是历史文件服务实现目录。

## 目录结构

```text
smart-file-biz/
├── README.md
├── pom.xml
└── target/      # Maven 构建目录，忽略提交
```

## 模块边界

- 当前基线只看到 POM/构建产物目录，未看到 `src/` 源码。
- 当前发布清单未包含本模块。
- 如果要恢复文件服务，先补源码、测试、可执行 Jar 打包和调用方证据。
