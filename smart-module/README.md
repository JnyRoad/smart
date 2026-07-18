# smart-module

`smart-module/` 是智慧园区业务微服务聚合工程。它依赖同级 [../smart/](../smart/) 提供的网关、认证、UPMS API 和公共组件，承载 App、管理平台、数据同步、设备桥接、ISC、算法、推送、调度等业务能力。

## 目录结构

```text
smart-module/
├── README.md
├── pom.xml                                  # Maven 聚合 POM
├── FileReceiver/                           # 许昌打印机 Windows 电脑上的人脸照片接收程序
├── database/                               # 手工数据库脚本和本地数据库文件
├── scripts/                                # smart-module 内部脚本
├── smart-tool/                             # 业务公共工具包
├── smart-data/                             # 数据同步/外部数据视图聚合模块
├── smart-platform/                         # 管理后台业务服务
├── smart-app/                              # 裕慧家园 App / 微信 H5 后端服务
├── smart-schedule/                         # 定时任务服务
├── smart-push/                             # App 消息推送服务
├── smart-dispatcher/                       # 多园区请求调度/转发服务
├── smart-bridge/                           # 非 ISC 设备桥接与通行记录业务
├── smart-bridge-concentrator/              # 水电表集中器 TCP 接入服务
├── smart-bridge-isc/                       # 海康 ISC 设备桥接业务
├── smart-algorithm/                        # 算法业务服务
├── smart-file/                             # 历史文件服务模块，已弃用
├── smart-transfer/                         # 历史数据传输模块，已弃用
└── smart-park-service/                     # 历史园区服务模块，已弃用
```

## 模块说明

| 模块 | 用途 | 结构 |
| --- | --- | --- |
| `FileReceiver/` | 接收入厂申请人脸照片的独立 Spring Boot 程序，发布清单使用 `build/file.jar`。 | 独立服务 |
| `smart-tool/` | 二维码、Excel、图片、SMB/OBS 等业务公共工具。 | library |
| `smart-data/` | 员工、出差、EHR/DHR、安保、XCC6 等外部数据视图和同步服务；`smart-data/smart-xcvehicle-core` 物理在该目录下，但由根 POM 直接聚合。 | `api` + `biz` + 多个 `core` |
| `smart-platform/` | 管理后台主要业务服务，含平台 API、业务实现和核心实体/规则。 | `api` + `biz` + `core` |
| `smart-app/` | 裕慧家园 App、微信公众号/H5 相关接口与业务实现。 | `api` + `biz` |
| `smart-schedule/` | 分布式定时任务服务，依赖 ShedLock 和多个业务 API/Core。 | 独立服务 |
| `smart-push/` | App 消息推送 API 和业务服务。 | `api` + `biz` |
| `smart-dispatcher/` | 把跨园区请求路由到目标园区服务，例如通行记录图片、人脸下发等。 | `api` + `biz` |
| `smart-bridge/` | 非 ISC 通道的设备桥接、通行记录和图片相关业务。 | `api` + `biz` + `core` |
| `smart-bridge-concentrator/` | 水表/电表集中器和外置阀门 TCP 通信，采集结果上报 Kafka。 | `biz` + `core` |
| `smart-bridge-isc/` | 海康 ISC 通道的设备、人员、卡片、通行记录等桥接业务。 | `api` + `biz` + `core` |
| `smart-algorithm/` | 算法服务 API 和业务实现，按 POM 分为 API 与 BIZ。 | `api` + `biz` |
| `smart-file/` | 历史文件服务模块；发布清单未包含，默认不做新功能入口。 | `api` + `biz` |
| `smart-transfer/` | 历史数据传输模块；当前发布清单未包含。 | 独立模块 |
| `smart-park-service/` | 历史园区服务模块；当前无源码目录，默认视为废弃占位。 | 占位模块 |

## 常用命令

在本目录执行：

```bash
mvn clean package -DskipTests
mvn -pl smart-platform/smart-platform-biz -am package -DskipTests
mvn -pl smart-bridge-isc/smart-bridge-isc-biz -am test
```

项目发布包汇总使用：

```bash
../scripts/build-release-jars.sh
```

## 发布边界

可部署 Spring Boot Jar 以根 [../scripts/release-jars.manifest](../scripts/release-jars.manifest) 为准。`api`、`core`、`smart-tool` 这类普通库不要加入发布 Jar 清单。

## 维护规则

- 新业务服务优先按 `api` + `biz` + 必要 `core` 的现有结构拆分。
- `smart-tool` 只放跨模块共享工具，不放单一业务流程。
- 数据库结构变更以发布记录和已上线版本为准，不保留人工迁移脚本。
- `.flattened-pom.xml`、`target/`、本地数据库、日志和构建产物都不应提交。
