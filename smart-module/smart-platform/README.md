# smart-platform

`smart-platform` 是管理后台主要业务聚合模块，POM 描述为“平台业务模块”。

它与 `smart-ui/` 管理端前端配合，承载园区平台业务 API、核心实体/规则和业务实现。

## 目录结构

```text
smart-platform/
├── README.md
├── pom.xml
├── smart-platform-api/    # 平台服务 API / Feign 契约
├── smart-platform-core/   # 平台核心实体、规则和复用逻辑
└── smart-platform-biz/    # 可部署平台业务服务
    ├── README.md
    ├── Dockerfile
    └── src/main/java/com/tce/smart/platform/SmartPlatformApplication.java
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-platform-api/` | 对其他服务暴露平台业务接口和类型。 |
| `smart-platform-core/` | 平台业务核心实体、规则、Mapper/Service 复用代码。 |
| `smart-platform-biz/` | 可部署平台业务服务，承载管理后台业务接口实现。 |

## 常用命令

在 `smart-module/` 目录执行：

```bash
mvn -pl smart-platform -am package -DskipTests
mvn -pl smart-platform/smart-platform-biz -am test
```

可部署 Jar：

```text
smart-platform/smart-platform-biz/target/smart-platform-biz.jar
```

## 模板设计器批次1

打印管理接口位于 `controller/print`，服务内前缀 `/print/v1`，通过现有网关映射为 `/platform/print/v1`。包括独立单面模板、不可变版本、系统正反面组合、静态图片与合成预览。默认关闭，权限/分类/内部渲染身份必须按目标环境配置。模板图片与预览PDF使用同一打印私有对象存储，不能通过新闻附件公开下载。

本轮离线测试使用隔离H2文件库验证Mapper/事务，不表示真实Oracle通过。表、权限与环境准备见[验收清单](../../specs/009-print-template-designer/quickstart.md)。已补齐显式DHR职级规则、可信人员及照片、预览与任务、设备API及切换状态；运行默认关闭。数据库版本发布工具见[发布与回退说明](docs/print-schema-release.md)，不会在应用启动时自动建表。真实DHR映射、供应商卡关系、Oracle及设备验收仍未通过。

打印配置参考 [默认关闭的示例](docs/print-configuration.example.yaml)，不会自动导入。`permissions` 须配置 `read/write/publish/preview/resource/execute/device/recover` 八项精确 UPMS 权限值。实际打印操作员需读取模板候选的 read、执行 execute、预览 preview、制品 resource；现场恢复另需 recover，设备维护需 device。DHR 园区代码、照片存储域和真实设备验收单独完成后再启用。
