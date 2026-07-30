# scripts

`scripts/` 保存项目级自动化脚本，当前重点是后端可部署 Spring Boot Jar 的构建、校验和发布目录汇总。

## 目录结构

```text
scripts/
├── README.md
├── build-release-jars.sh          # 构建后端模块并收集可部署 Jar
├── restartService.sh               # 受控服务 stop/restart，调用特权 watchdog 预检
├── verify-watchdog-stopped.sh      # root 预检 cron、systemd 与运行中的 watchdog
├── verify-release-runtime.sh       # 只读校验发布包与目标 smart-jar 是否一致
├── security/                      # 本地安全基线检查脚本
├── release-jars.manifest          # 发布 Jar 白名单：service|jar path
├── test-build-release-jars.sh     # build-release-jars.sh 的脚本级回归测试
├── test-restart-service.sh         # restartService.sh 的脚本级回归测试
├── test-verify-watchdog-stopped.sh # watchdog 预检的脚本级回归测试
└── test-docker-compose-dev.sh     # docker-compose.dev.yml 模块覆盖回归测试
```

## 主要脚本

`build-release-jars.sh` 会：

- 构建 `smart/`、`smart-module/` 和 `smart-module/FileReceiver/`。
- 只收集 `release-jars.manifest` 中列出的 Spring Boot 可执行 Jar。
- 校验 Jar 里是否包含 Spring Boot loader 入口，避免把 API/Core 普通库误当部署包。
- 输出 `smart-jar/`、`runtime/`、`manifest.csv`、`sha256sums.txt` 和 `build-info.txt` 到 `release-artifacts/backend/<timestamp>/`。
- `runtime/` 同时包含 `restartService.sh`、`verify-watchdog-stopped.sh` 与 `verify-release-runtime.sh`，并写入校验和；运行时必须保持三者同目录，并通过 `SMART_APP_ROOT` 指向实际包含 `app.sh` 与 `smart-jar/` 的服务目录。

在本节点需要的 Jar 已复制完成、尚未执行任何 stop/restart 前，先执行只读预检：

```bash
release-artifacts/backend/<version>/runtime/verify-release-runtime.sh \
  --app-root /实际/服务目录 \
  --service manifest.csv中的服务名
```

分节点部署时可重复传入本节点实际部署的多个 `--service`，只校验这些 Jar；不传 `--service` 时才要求目标节点包含全量发布 Jar。预检会校验发布包内的运行时脚本、所选 Jar 及目标目录的 `app.sh`；不会复制文件、停止服务或修改生产状态。

### 受控重启契约

从 `runtime/` 运行 `restartService.sh` 时，必须保留同目录的
`verify-watchdog-stopped.sh`。`stop` 和 `restart` 会通过 `sudo` 运行 root 预检；
root 与实际服务账号的 cron、常见 systemd/init/supervisor/monit 配置以及正在运行的
实际应用目录中的 `checksmart` / `watchdog` 启动器均无命中才会继续。主机硬件
`watchdogd` 不属于 smart 自动拉起器，不会单独阻断发布。环境变量不能跳过该预检。

`restart` 不会先停止旧 Java 再“赌”新启动能成功：它先创建一个尚未获准执行 `app.sh`
的独立会话。该 wrapper 必须从 `/proc` 证明其 session、starttime 和直接父进程都仍与
当前重启脚本一致；若旧版 `setsid` 发生 fork、收据超时或身份发生变化，脚本保持旧服务
运行并拒绝重启。只有旧实例被精确停止后，才会原子写入 `go` 决策启动 `app.sh`。

受控会话在调用 `app.sh` 前会切换到 `SMART_APP_ROOT`。这是生产 `app.sh` 使用 `pwd`
推导 `APP_HOME`、`config/` 和 Jar 相对路径的必要条件；不要在发布包的 `runtime/` 目录
或任意运维终端目录中直接替代该调用方式。

启动失败、`app.sh` 非零退出或就绪超时时，脚本仅向本次受控 session 的成员发送
`SIGTERM`，并反复确认该 session 已清空；不会向不同 session 的同名 Jar 发送信号，也
不会自动升级为 `SIGKILL`。如果预检提示 session 父链不成立，请保留旧服务并检查调用
方式（例如交互式 job-control 环境），不要通过修改环境变量或手工跳过脚本来强行启动。

常用命令：

```bash
scripts/build-release-jars.sh
scripts/build-release-jars.sh --skip-build
scripts/test-build-release-jars.sh
scripts/test-docker-compose-dev.sh
npm ci --prefix scripts/security
node scripts/security/check-nacos-ignore-urls.mjs docker/nacos/config/dev
```

## 维护规则

- 新增可部署后端服务时，先确认它会产出 Spring Boot executable jar，再加入 `release-jars.manifest`。
- 新增 Docker Compose 服务时，同步更新 `scripts/test-docker-compose-dev.sh` 的模块映射。
- 发布 Nacos 配置前先运行 `npm ci --prefix scripts/security`，再运行 `check-nacos-ignore-urls.mjs`；命中禁止的匿名规则时会以非零状态退出，不能据此跳过收口流程。
- 不要把 `release-artifacts/`、`smart-jar/` 或脚本测试临时目录提交。
