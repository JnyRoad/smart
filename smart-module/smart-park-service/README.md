# smart-park-service

`smart-park-service` 是历史园区服务模块。当前目录只有 Maven POM 和构建产物痕迹，未看到 `src/` 源码目录，默认视为废弃占位模块。

## 目录结构

```text
smart-park-service/
├── README.md
├── pom.xml
└── target/      # Maven 构建目录，忽略提交
```

## 模块边界

- 新功能不要写入本模块。
- 园区平台业务优先查 `smart-platform/`。
- 设备桥接相关能力优先查 `smart-bridge/` 或 `smart-bridge-isc/`。
- 如果确需恢复此模块，先补源码、测试、发布方式和调用方证据。
