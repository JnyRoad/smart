# smart-tool

`smart-tool` 是智慧园区业务公共工具模块，POM 描述为“智慧园区服务公共模块”。

它是普通 library，不是可部署服务。POM 依赖显示它承载二维码、EasyPoi、Thumbnailator、JCIFS、华为 OBS 等业务工具能力。

## 目录结构

```text
smart-tool/
├── README.md
├── pom.xml
└── src/
    ├── main/java/
    └── test/java/
```

## 模块边界

- 只放跨多个业务模块复用的工具、常量、通用适配。
- 单一业务流程不要放入 `smart-tool`。
- 如果代码需要访问业务表或承载业务状态，优先放回对应业务模块。

## 使用方式

在需要复用的 Maven 模块中引入：

```xml
<dependency>
  <groupId>com.tce</groupId>
  <artifactId>smart-tool</artifactId>
  <version>${smart.version}</version>
</dependency>
```
