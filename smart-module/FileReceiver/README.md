# FileReceiver

`FileReceiver` 是一个独立 Spring Boot 程序，运行在许昌打印机 Windows 电脑上，用于获取入厂申请相关的人脸照片。
根发布清单使用 `build/file.jar` 作为发布产物。

自本版本起，主链路切换为 **拉取模式**：程序定时以 OAuth2 client_credentials 方式向网关换取 token，
主动拉取待处理照片清单并下载到本地目录；旧的 `/file/upload` 推送接口保留但已废弃，将在下个版本移除。

## 目录结构

```text
FileReceiver/
├── README.md
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── FileApplication.java              # 启动类，开启 @EnableScheduling
│   │   │   ├── controller/FileController.java     # 旧 /file/upload 推送接口（已废弃）
│   │   │   └── pull/                              # 拉取模式相关代码
│   │   │       ├── PhotoPullProperties.java        # file-receiver.* 配置项
│   │   │       ├── PhotoServerClient.java          # 服务端 HTTP 调用抽象接口
│   │   │       ├── HutoolPhotoServerClient.java    # 基于 Hutool 的实现
│   │   │       ├── OpenApiTokenClient.java         # token 获取与缓存
│   │   │       ├── PhotoPullTask.java              # 定时拉取任务
│   │   │       └── PhotoCleanupTask.java           # 每日过期清理任务
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/com/example/demo/pull/            # 拉取/清理任务单测
├── build/       # Spring Boot 可执行 Jar 输出目录，忽略提交
└── target/      # Maven 构建目录，忽略提交
```

## 模块边界

- 只处理照片接收程序自身的 HTTP 服务、拉取任务和文件接收逻辑。
- 不属于 `smart-module` 主聚合父 POM 的常规业务服务结构，打包路径也与其他服务不同。
- 不要把管理后台、App 或桥接业务写入本模块。

## 部署与配置说明（拉取模式，主链路）

程序定时执行以下流程：取 token → 拉取待处理照片清单 → 与本地目录 diff → 逐张下载（临时文件 + 原子改名落盘）。
每日 03:00 额外执行一次过期清理：本地照片满足「超过保留天数」且「不在最新待处理清单中」两个条件才会被删除。

在 `application.properties`（或对应环境变量）中配置：

| 配置键 | 环境变量 | 默认值 | 说明 |
|---|---|---|---|
| `file-receiver.pull.enabled` | `FILE_RECEIVER_PULL_ENABLED` | `false` | 是否启用拉取任务，默认关闭 |
| `file-receiver.pull.server-url` | `FILE_RECEIVER_PULL_SERVER_URL` | 空 | 网关地址，如 `http://gateway-host:port`；`/auth`、`/platform` 为网关路由前缀 |
| `file-receiver.pull.app-id` | `FILE_RECEIVER_PULL_APP_ID` | 空 | OAuth2 client_credentials 的 app-id（Basic 认证用户名） |
| `file-receiver.pull.app-secret` | `FILE_RECEIVER_PULL_APP_SECRET` | 空 | OAuth2 client_credentials 的 app-secret（Basic 认证密码，禁止出现在日志中） |
| `file-receiver.pull.interval-seconds` | `FILE_RECEIVER_PULL_INTERVAL_SECONDS` | `30` | 拉取轮询间隔（秒） |
| `file-receiver.photo-dir` | `FILE_RECEIVER_PHOTO_DIR` | `D:/visitor` | 本地照片存放目录（许昌打印机 Windows 机部署路径） |
| `file-receiver.cleanup.retention-days` | `FILE_RECEIVER_CLEANUP_RETENTION_DAYS` | `7` | 本地照片保留天数，`0` 表示关闭清理任务 |

接口约定：

- token：`POST {server-url}/auth/oauth/token?grant_type=client_credentials`，Basic 认证（app-id / app-secret）。
- 待处理清单：`GET {server-url}/platform/open/admittance/photo/pending`，返回 `Result` 包装的 `photoId` 数组。
- 下载：`GET {server-url}/platform/open/admittance/photo/download/{photoId}`，200 返回 PNG 字节，404 表示缺图（跳过不重试），401/403 表示 token 失效（刷新后重试一次）。

启动自检（快速失败）：

- `pull.enabled=true` 但 `server-url` / `app-id` / `app-secret` 任一缺失时，**启动直接失败**，
  报错一次性点名全部缺失的配置键（写入日志文件），避免部署后任务静默空转。
- `pull.enabled=false` 时正常启动，但日志会打 WARN 提示「不会主动下载任何照片」。

## 日志

日志同时输出到控制台和文件；文件日志按天轮转，**每天一个文件**：

- 位置：默认在**软件目录**（`file.jar` 所在目录）下的 `Logs/` 子目录，文件名 `file-receiver.yyyy-MM-dd.log`。
- 覆盖方式：环境变量 `FILE_RECEIVER_LOG_DIR` 或启动参数 `java -DLOG_DIR=<目录> -jar file.jar`。
- 保留策略：30 天，总量封顶 1GB（防止占满打印机电脑磁盘）；文件编码 UTF-8。

排查故障时的关键日志（均在 `Logs/` 当天文件中）：

| 日志 | 级别 | 含义 |
|---|---|---|
| `拉取模式已启用：server-url=...` / `拉取模式未启用` | INFO / WARN | 启动配置摘要（app-secret 永不打印） |
| `拉取轮完成：待处理 N 张，...` | INFO | 每轮心跳，30s 一条；长时间没有该行说明任务没在跑 |
| `照片下载成功：photoId=...` | INFO | 单张成功，含大小与耗时 |
| `下载照片失败，photoId=...` | ERROR | 单张失败，带堆栈，不影响本轮其余照片 |
| `本轮照片拉取失败（token 或清单环节异常...）` | ERROR | 整轮失败（换 token、拉清单出错），下一轮自动重试 |
| `已刷新 access token` | INFO | token 获取/刷新成功 |
| `开始照片过期清理` / `清理轮完成` | INFO | 每日 03:00 清理任务 |

## 部署与配置说明（推送模式，已废弃）

`POST /file/upload`（`file` + `filePath` 表单参数）仍然可用，但调用时会打印 WARN 日志提示废弃，
将在下个版本移除，请尽快迁移到上面的拉取模式。

| 配置键 | 环境变量 | 默认值 | 说明 |
|---|---|---|---|
| `file-receiver.upload-root` | `FILE_RECEIVER_UPLOAD_ROOT` | `${java.io.tmpdir}/file-receiver` | 推送模式的文件保存根目录（已废弃） |

## 常用命令

在 `smart-module/FileReceiver/` 目录执行：

```bash
mvn test               # 跑单测（拉取/清理任务的纯单测，不依赖真实网络）
mvn clean package -DskipTests
```

发布脚本读取的可部署产物：

```text
build/file.jar
```
