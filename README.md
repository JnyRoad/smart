# smart

`smart` 是裕同智慧园区项目代码目录，包含基础平台后端、业务微服务、管理端前端、微信 H5、「裕慧家园」移动 App、历史 Vue2 H5 参考工程、本地 Docker 环境、发布脚本和项目级文档。

本 README 是模块目录、声明版本和常用入口的集中说明。模块的接口、页面和专项约定以对应模块 README 为准；本文中的版本来自当前 `pom.xml` 或 `package.json` 声明。

## 目录结构

```text
smart/
├── AGENTS.md                 # 项目 Agent / 协作规则入口
├── README.md                 # 模块总览、版本和运行入口
├── .gitignore                # 仓库级忽略规则
├── docker-compose.dev.yml    # 本地 Docker 编排
├── docker/                   # 本地 Docker 配置和 Nacos 初始化
├── docs/                     # 项目级规则与跨模块资料
├── scripts/                  # 发布包构建和校验脚本
├── smart/                    # 基础平台后端：网关、认证、UPMS、公共组件
├── smart-module/             # 业务微服务后端及 FileReceiver
├── smart-ui/                 # 管理端前端：Vue 2 + Element UI + Avue
├── smart-print-renderer/     # 受控 PDF 渲染，业务授权由平台服务负责
├── smart-print-client/       # Windows 工作站、持久命令日志及设备适配
├── smart-h5/                 # 当前维护的微信 H5：Next.js + React
├── smart-h5-vue2/            # 历史 Vue2 微信 H5，只读参考
├── smart-app/                 # 新统一客户端：uni-app x + Vue 3
└── smart-app-uniapp/         # 「裕慧家园」App：uni-app + Vue 2
```

## 模块总览

| 目录 | 责任 | 当前技术与声明版本 |
| --- | --- | --- |
| [`smart/`](smart/README.md) | 基础平台后端，提供网关、OAuth2 认证、UPMS 和公共组件。 | Java 8、Maven、Spring Boot `2.1.3.RELEASE`、Spring Cloud `Greenwich.RELEASE`（见 [`smart/pom.xml`](smart/pom.xml)） |
| [`smart-module/`](smart-module/README.md) | 业务微服务聚合工程，包含 App、平台、数据、桥接、算法、推送、调度等模块。 | Java 8、Spring Cloud 业务服务、Maven；聚合关系见 [`smart-module/pom.xml`](smart-module/pom.xml) |
| [`smart-ui/`](smart-ui/README.md) | 管理端中后台 SPA，对接网关、认证、UPMS 和园区业务 API。 | Vue `^2.7.16`、Element UI `^2.4.11`、Avue、Vue CLI、pnpm `11.3.0`（见 [`smart-ui/package.json`](smart-ui/package.json)） |
| [`smart-print-renderer/`](smart-print-renderer/README.md) | 独立单面模板及双面组合的私有 PDF 渲染；不读取人员库或调用打印机。 | Node.js 24、pdfme `6.1.12` |
| [`smart-print-client/`](smart-print-client/README.md) | 独立设备身份领取任务、持久命令去重、手动/自动厂牌及单面访客适配；实机能力按档案验收。 | .NET 10、Windows 官方驱动、Brother b-PAC |
| [`smart-h5/`](smart-h5/README.md) | 当前维护的微信公众号 / 微信内嵌移动 H5。 | Next.js `16.2.9`、React `19.2.4`、TypeScript `^5`、antd-mobile `^5.42.3`、Tailwind CSS `^4`（见 [`smart-h5/package.json`](smart-h5/package.json)） |
| [`smart-h5-vue2/`](smart-h5-vue2/README.md) | 历史 Vue2 微信 H5，仅用于查阅旧页面和调用方式。 | Vue `2.6.11`、Vue Router `3.1.3`、Vuex `3.1.2`、cube-ui `^1.12.44`、pnpm `11.4.0`（见 [`smart-h5-vue2/package.json`](smart-h5-vue2/package.json)） |
| [`smart-app/`](smart-app/README.md) | 新统一客户端，首期覆盖物品放行申请、审批、执行，以及供应商厂牌扫码核验和进出事件记录；复用 Web、Android、iOS 与小程序业务核心。 | uni-app x、Vue 3、UTS、Vapor（原生 App） |
| [`smart-app-uniapp/`](smart-app-uniapp/README.md) | 「裕慧家园」Android / iOS App 客户端。 | uni-app / Vue 2 / Vuex，HBuilderX 可视化工程；无命令行构建脚本 |
| [`docker/`](docker/README.md) | 本地 Docker Compose 的依赖服务和 Nacos 初始化配置。 | Docker Compose、Nacos、shell |
| [`scripts/`](scripts/README.md) | 后端发布 Jar 汇总、构建和校验脚本。 | Bash、Maven、zip / sha256 |
| [`docs/`](docs/README.md) | 项目级规则、跨模块说明和资料导航。 | Markdown |

### 目录约定

- `smart-ui/` 的 `src/api/` 放接口封装，`src/views/` 放业务页面，`src/router/` 放路由与 axios 配置，`src/store/` 放 Vuex，`public/` 保存原样静态资源。
- `smart-h5/` 使用 TypeScript strict；`src/app/` 是页面壳，`src/features/` 是业务域，`src/lib/` 是共享设施，`src/components/` 是通用组件。
- `smart-h5-vue2/` 是历史只读工程，旧页面、路由、接口和组件分别位于 `src/views-mobile/`、`src/router/`、`src/services/`、`src/components/`。
- `smart-app/` 的 `core/` 放跨端模型、权限、扫码和状态流转，`services/` 放 uni 传输与统一 `/api/v1/*` 适配，`state/` 集中管理内存会话，`pages/` 和 `components/` 放跨端界面，`docs/` 记录集成与验证边界。
- `smart-app-uniapp/` 的客户端目录包括 `api/`、`pages/page/`、`components/`、`config/`、`tools/` 和 `static/`；资源保留约定见 [App README](smart-app-uniapp/README.md)。

### 基础平台后端

`smart/` 的本地 Maven reactor 包含以下一级模块：

| 模块 | 用途 |
| --- | --- |
| `smart-gateway` | API 网关、路由和网关过滤器。 |
| `smart-auth` | OAuth2 认证授权和令牌相关能力。 |
| `smart-upms` | 用户、角色、菜单、部门、客户端等通用权限管理。 |
| `smart-common` | BOM、核心工具、数据、安全、网关、日志和 Swagger 等公共组件。 |

### 业务后端模块

`smart-module/` 按一级目录划分业务；拆分为 `api`、`biz`、`core` 的模块中，`api` 放对外契约，`biz` 放可部署服务，`core` 放领域复用代码。下表模块均位于该目录，模块有自己的特殊结构时，以对应模块 README 为准。

| 模块 | 用途 |
| --- | --- |
| `smart-app` | App 业务模块。 |
| `smart-platform` | 平台业务模块。 |
| `smart-data` | 数据通讯模块。 |
| `smart-push` | App 消息推送模块。 |
| `smart-schedule` | 定时任务模块。 |
| `smart-algorithm` | 算法模块。 |
| `smart-tool` | 智慧园区服务公共模块。 |
| `smart-bridge` | 设备桥接。 |
| `smart-bridge-isc` | ISC（综合安防平台）集成桥接。 |
| `smart-bridge-concentrator` | 设备集中器（YUTO Nexus）。 |
| `smart-dispatcher` | 调度 / 分发。 |
| `smart-file` | 文件服务。 |
| `smart-transfer` | 数据传输。 |
| `smart-park-service` | 历史园区服务模块；当前仅存 POM、没有 `src/` 源码，是废弃占位。 |
| [`FileReceiver`](smart-module/FileReceiver/README.md) | 独立 Spring Boot 程序，接收入厂申请的人脸照片，部署在许昌打印机 Windows 机；启动类为 `FileApplication`，接口类为 `FileController`，发布产物为 `build/file.jar`。 |

`smart-bridge`、`smart-bridge-isc`、`smart-bridge-concentrator`、`smart-dispatcher`、`smart-file`、`smart-transfer` 的 POM 未写描述，上表用途按模块名标注；需要精确判断时阅读对应模块 README、POM 和源码。

后端服务包名约定为 `com.tce.smart`。`FileReceiver` 是独立程序，目录结构和自身源码包名以其 [README](smart-module/FileReceiver/README.md) 为准。

## 客户端关系

`smart-app-uniapp/` 是移动客户端，`smart-module/smart-app/` 是后端业务模块，两者不是同一个工程。App 与微信 H5 并行使用、互不替代：App 场景使用 uni-app 客户端，公众号场景使用 [`smart-h5/`](smart-h5/)。

## 常用入口

Maven 命令从对应 reactor 根目录运行：基础平台从 `smart/`，业务服务从 `smart-module/`。具体模块的接口和打包产物见模块 README。

基础平台：

```bash
cd smart
mvn -pl smart-gateway -am test
mvn -pl smart-auth -am package -DskipTests
```

业务微服务：

```bash
cd smart-module
mvn -pl smart-app/smart-app-biz -am test
mvn -pl smart-platform/smart-platform-biz -am package -DskipTests
```

`smart-data/smart-xcvehicle-core/` 物理上位于 `smart-data/` 下，但由 `smart-module/pom.xml` 直接聚合；构建它时使用该业务 reactor 路径：

```bash
cd smart-module
mvn -pl smart-data/smart-xcvehicle-core -am package -DskipTests
```

FileReceiver 使用自己的 POM：

```bash
cd smart-module/FileReceiver
mvn test
mvn clean package -DskipTests
```

管理端前端：

```bash
cd smart-ui
pnpm install
pnpm dev
pnpm lint
pnpm test
pnpm build
pnpm gate
```

当前微信 H5：

```bash
cd smart-h5
pnpm install
pnpm dev
pnpm check
pnpm test
pnpm e2e
pnpm build
```

历史 Vue2 H5 仅作参考：

```bash
cd smart-h5-vue2
pnpm install
pnpm run serve
pnpm run lint
pnpm run test
```

App 是 HBuilderX 可视化工程，没有 CLI 构建脚本。首次安装依赖可在 [`smart-app-uniapp/`](smart-app-uniapp/) 目录执行 `npm install`，运行到真机 / 模拟器和云打包 APK / IPA 使用 HBuilderX，细节见 [App README](smart-app-uniapp/README.md)。

新统一客户端：`smart-app/` 使用 uni-app x 和 Vue 3。Node 测试、Web/小程序源码编译和 Android 原生源码编译命令详见 [`smart-app/README.md`](smart-app/README.md)；真实后端、设备和发布包验收边界见 [`smart-app/docs/validation.md`](smart-app/docs/validation.md)。`smart-app-uniapp/` 继续保留，不因新客户端骨架自动迁移或删除。

后端发布包：

```bash
scripts/build-release-jars.sh
```

## 本地环境与线上运行

本地 Docker 配置由 [`docker/.env.local.example`](docker/.env.local.example) 派生：

```bash
cp docker/.env.local.example .env.local
docker compose --env-file .env.local -f docker-compose.dev.yml up smart-nacos smart-nacos-init smart-redis smart-zookeeper smart-kafka
docker compose --env-file .env.local -f docker-compose.dev.yml --profile local-db up smart-oracle
docker compose --env-file .env.local -f docker-compose.dev.yml --profile backend up
docker compose --env-file .env.local -f docker-compose.dev.yml --profile backend --profile frontend up
```

桥接服务按接入场景启用：

```bash
# 直连海康设备终端
docker compose --env-file .env.local -f docker-compose.dev.yml --profile bridge up smart-bridge

# 对接海康 ISC 平台
docker compose --env-file .env.local -f docker-compose.dev.yml --profile bridge-isc up smart-bridge-isc

# 水电表集中器
docker compose --env-file .env.local -f docker-compose.dev.yml --profile bridge-concentrator up smart-bridge-concentrator
```

本地核心系统数据库使用 Oracle。OA、EHR、DHR、XCC6、BG/出差、考勤、临时人员、门禁等第三方系统不由本项目容器化部署，也不提供本地数据库替身；专项联调时只在本地 env 中填写对应第三方测试环境地址。Compose 中的 `smart-mock-http` 只用于本地测试桩，不代表第三方系统。

线上测试和线上生产使用独立 runtime 编排和独立 env，不复用 `docker-compose.dev.yml`。运行时注入镜像版本、域名、中间件地址和密钥；线上环境文件不放在仓库中。

更多本地服务、profile 和配置项见 [docker/README.md](docker/README.md)。

## 生成物与资源

仓库级 `.gitignore` 覆盖 Node 依赖、前端构建产物、Maven `target/`、测试报告、日志和本地环境文件；样例环境文件保留在仓库中。App 的 `unpackage/res/` 是 manifest 引用的资源，保留约定见 [smart-app-uniapp/README.md](smart-app-uniapp/README.md)。

## 规则与资料入口

- [AGENTS.md](AGENTS.md)：项目任务入口和规则路由。
- [Git 与 worktree](docs/agent-rules/git-worktree.md)：工作区隔离、分支和交付边界。
- [规格工作流](docs/agent-rules/spec-workflow.md)：规格复用、Spec Kit 产物和执行交接。
- [开发规则](docs/agent-rules/development.md)：代码中文约定、权限 / Oracle 边界和验证政策。
- [docs/README.md](docs/README.md)：项目级文档归属和资料导航。
- [specs/](specs/) 与 [.specify/](.specify/)：项目规格和 Spec Kit 工作流资料。
