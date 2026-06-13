# smart-bridge-concentrator

## 模块介绍
许昌园区项目水电表集中器接入模块，负责接收水表集中器、电表集中器和外置水阀控制箱的 TCP 连接或主动连接，并为平台主服务提供 HTTP 调用入口。

该模块只负责设备侧通信、协议报文封装解析和采集结果上报，不负责平台业务数据落库和页面查询。采集成功后的数据通过 Kafka topic `BRIDGE_EVENT_TOPIC` 发给 `smart-bridge` / `smart-bridge-isc` 等主业务服务继续处理。

## 目录结构

```text
smart-bridge-concentrator/
├── README.md
├── pom.xml
├── smart-bridge-concentrator-core/   # DTO、枚举、统一返回和异常定义
└── smart-bridge-concentrator-biz/    # Spring Boot 服务、HTTP 接口、Netty、Kafka 上报
    └── src/main/
```

## 子模块

### smart-bridge-concentrator-biz
Spring Boot 启动模块，包含 HTTP 接口、Netty 服务端、外置阀门 TCP 客户端、协议报文工具和 Kafka 上报逻辑。

### smart-bridge-concentrator-core
提供 DTO、枚举、统一返回对象和异常定义。

## 通信链路

### 水电表集中器
- 集中器主动连接本服务 Netty 端口，默认 `smart.server.port=6001`。
- 集中器上线后通过心跳/注册帧建立 IP 与通道关系。
- 平台主服务调用本模块 `/bridge/**` HTTP 接口后，本模块按集中器 IP 找到对应 Netty 通道，下发报文并等待响应。
- 水表、电表读数解析成功后，通过 Kafka 上报到 `BRIDGE_EVENT_TOPIC`。

### 水表外置阀门
- 外置阀门控制箱不是水表读数集中器协议。
- 本模块按 `deviceIp:devicePort` 主动建立 TCP 客户端连接。
- `/bridge/checkOnlineValve` 会检查连接状态，未连接时触发异步连接。
- `/bridge/watermeter/out_valve/control` 和 `/bridge/watermeter/out_valve_remote/control` 通过 `NettyTcpClientUtils` 下发外置阀门控制帧。

### 水表内置阀门与电表闸门
- 水表内置阀门控制走水表集中器通道。
- 电表闸门状态读取和控制走电表集中器通道。
- 不要把外置阀门协议与水表/电表集中器协议合并处理。

## 技术栈
- Java 8
- Spring Boot 2.1.7
- Netty TCP
- Spring Kafka
- Hutool
- Guava

## 运行配置

默认配置位于 `smart-bridge-concentrator-biz/src/main/resources/application.yml`。

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `6062` | HTTP 接口端口 |
| `server.servlet.context-path` | `/` | HTTP context path |
| `smart.server.port` | `6001` | 水表/电表集中器 Netty 服务端口 |
| `spring.kafka.bootstrap-servers` | `smart-kafka:9092` | Kafka 地址 |
| `spring.kafka.listener.concurrency` | `5` | Kafka listener 预留配置；当前模块只生产消息 |

生产部署时需要确认：
- 集中器网络能访问本服务 `smart.server.port`。
- 本服务能访问 Kafka。
- 外置阀门控制箱的 `deviceIp:devicePort` 可从本服务主动连接。
- 该模块按独立 Spring Boot 服务打包运行，不依赖 Nacos 注册。

## 打包

在 `smart-module` 目录执行：

```bash
mvn -f smart-bridge-concentrator/pom.xml clean package
```

构建产物：

```text
smart-bridge-concentrator/smart-bridge-concentrator-biz/target/smart-bridge-concentrator-biz.jar
```

当前打包方式保持独立 Spring Boot fat jar，入口类为：

```text
com.tce.smart.bridge.SmartBridgeConcentratorApplication
```

## HTTP 接口

所有接口前缀为 `/bridge`。

| 接口 | 作用 | 通信对象 |
| --- | --- | --- |
| `POST /checkOnline` | 检查水表/电表集中器是否在线 | 水表/电表集中器 |
| `POST /checkOnlineValve` | 检查外置阀门控制箱是否在线 | 外置阀门控制箱 |
| `POST /ele/issue/file` | 下发电表集中器档案 | 电表集中器 |
| `POST /ele/del/file` | 删除电表集中器档案 | 电表集中器 |
| `POST /ele/query/file` | 查询电表集中器档案 | 电表集中器 |
| `POST /elemeter/read` | 读取电表小时冻结数据 | 电表集中器 |
| `POST /ele/meter/brake/read` | 查询电表闸门状态 | 电表集中器 |
| `POST /ele/brake/control` | 控制电表闸门 | 电表集中器 |
| `POST /water/issue/file` | 下发水表集中器档案 | 水表集中器 |
| `POST /water/del/file` | 删除水表集中器档案 | 水表集中器 |
| `POST /water/query/file` | 查询水表集中器档案 | 水表集中器 |
| `POST /watermeter/read` | 读取水表数据 | 水表集中器 |
| `POST /watermeter/in_valve/control` | 控制水表内置阀门 | 水表集中器 |
| `POST /watermeter/out_valve/control` | 控制水表外置阀门 | 外置阀门控制箱 |
| `POST /watermeter/out_valve_remote/control` | 控制水表外置阀门远程功能 | 外置阀门控制箱 |

## Kafka 上报

采集和部分控制状态通过 `KafkaProducer` 上报到 `BRIDGE_EVENT_TOPIC`。

| key | 说明 |
| --- | --- |
| `water_repeater_read_nty` | 水表读数上报 |
| `water_repeater_inValveState_nty` | 水表内置阀门状态上报 |
| `electric_repeater_read_nty` | 电表读数上报 |
| `electric_repeater_brakeState_nty` | 电表闸门状态上报 |

`water_repeater_outValveState_nty` 在枚举中保留，但当前外置阀门控制方法未实际发送该 Kafka 消息。

## 协议实现位置

| 类 | 说明 |
| --- | --- |
| `ServerHandler` | 接收集中器注册、心跳和响应帧，并按 `clientIp + tp` 唤醒等待中的请求 |
| `NettyServerUtils` | 维护水表/电表集中器通道、同步等待响应、集中器在线状态 |
| `NettyTcpClientUtils` | 维护外置阀门 TCP 客户端通道、同步等待响应 |
| `SendEleMessageUtils` | 电表集中器读数、档案、闸门状态和闸门控制报文 |
| `SendWaterMessageUtils` | 水表集中器读数、档案、内置阀门控制报文 |
| `SendMessageUtils` | 通用帧组装、校验和时间片处理 |
| `SeqUtils` | 报文序号生成 |

协议报文修改风险较高。调整 `SendEleMessageUtils`、`SendWaterMessageUtils`、`SendMessageUtils` 或 `SeqUtils` 前，必须先确认对应集中器协议文档和生产设备行为。
