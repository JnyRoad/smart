# smart

`smart` 是裕同智慧园区项目代码目录，包含后端基础平台、业务微服务、管理端前端、微信 H5、历史 Vue2 H5 参考工程、本地 Docker 环境、发布脚本和项目文档。

## 目录结构

```text
smart/
├── AGENTS.md                 # 项目 Agent / 协作规则
├── README.md                 # 项目入口说明
├── .gitignore                # 提交忽略规则
├── docker-compose.dev.yml    # 本地 Docker 编排：Nacos / Redis / Kafka / 后端 / 前端
├── docker/                   # Docker 本地环境辅助配置
├── docs/                     # 项目级资料和跨模块文档
├── scripts/                  # 发布包构建和校验脚本
├── smart/                    # 基础平台后端：网关、认证、UPMS、公共组件
├── smart-module/             # 业务微服务后端：App、平台、桥接、数据、调度等
├── smart-ui/                 # 管理后台前端：Vue 2 + Element UI + Avue
├── smart-h5/                 # 当前维护的微信 H5：Next.js + React + antd-mobile
└── smart-h5-vue2/            # 历史 Vue2 微信 H5，仅作功能参考
```

## 模块说明

| 目录 | 用途 | 主要技术 |
| --- | --- | --- |
| `smart/` | 基础平台后端，提供 API 网关、OAuth2 认证、UPMS 权限和 `smart-common-*` 公共组件。 | Java 8、Spring Boot 2.1、Spring Cloud Greenwich、Maven |
| `smart-module/` | 园区业务微服务聚合工程，覆盖 App、管理平台、数据同步、设备桥接、ISC、算法、推送、调度和发布 Jar 清单中的可部署服务。 | Java 8、Spring Boot、Spring Cloud、MyBatis/MyBatis-Plus、Maven |
| `smart-ui/` | 管理端中后台 SPA，对接 `smart-gateway` 后的认证、UPMS 和园区业务 API。 | Vue 2.7、Element UI、Avue、Vue CLI、pnpm |
| `smart-h5/` | 当前维护的微信内嵌移动 H5，覆盖登录、首页、访客、宿舍、放行、待办等移动端流程；后续 H5 功能更新统一在这里开发。 | Next.js 16、React 19、TypeScript、antd-mobile、Tailwind CSS |
| `smart-h5-vue2/` | 历史 Vue2 微信公众号版 H5，仅用于查阅旧页面、旧交互和接口调用方式；不再维护、不再用于新功能开发或发布。 | Vue 2、Vue CLI、Vue Router、Vuex、cube-ui |
| `docker/` | 本地 Docker Compose 的 Nacos 配置初始化脚本和配置文件。 | Docker Compose、Nacos、shell |
| `scripts/` | 后端发布 Jar 汇总脚本、脚本测试和发布清单。 | Bash、Maven、zip/sha256 |
| `docs/` | 项目级文档和跨模块资料，目前包含管理端页面功能清单。 | Markdown |

## 常用入口

后端基础平台：

```bash
cd smart
mvn clean install -DskipTests
```

业务微服务：

```bash
cd smart-module
mvn clean package -DskipTests
```

管理端前端：

```bash
cd smart-ui
pnpm install
pnpm dev
pnpm build
```

微信 H5：

```bash
cd smart-h5
pnpm install
pnpm check
pnpm test
pnpm build
```

历史 Vue2 微信 H5 参考：

```bash
cd smart-h5-vue2
pnpm install
pnpm run serve
pnpm run test
```

`smart-h5-vue2/` 仅作为旧版本页面参考。新需求、缺陷修复和发布验证应落在 `smart-h5/`，不要把新业务继续加回历史 Vue2 工程。

本地 Docker 环境：

```bash
cp docker/.env.local.example .env.local
docker compose --env-file .env.local -f docker-compose.dev.yml up smart-nacos smart-nacos-init smart-redis smart-kafka
docker compose --env-file .env.local -f docker-compose.dev.yml --profile backend up
docker compose --env-file .env.local -f docker-compose.dev.yml --profile backend --profile frontend up
```

桥接服务按接入场景启用：`--profile bridge` 启动直连海康设备终端的 `smart-bridge`，`--profile bridge-isc` 启动对接海康 ISC 平台的 `smart-bridge-isc`。水电表集中器使用 `--profile bridge-concentrator` 单独启动 `smart-bridge-concentrator`。

后端发布包：

```bash
scripts/build-release-jars.sh
```

## Git 忽略规则

项目 `.gitignore` 忽略以下内容：

- macOS / IDE / Agent 工作树本地文件。
- Node 依赖、缓存、构建产物、测试报告，包括旧 Vue2 H5 的 `dist-h5/` 和 `node_modules/`。
- Maven / Gradle `target/`、`.flattened-pom.xml`、压缩包、发布产物。
- 真实 `.env` / `.env.local` / `.env.*.local`，但保留 `.env.example` 和 `.env.local.example`。
- 本地数据库、日志、临时文件、release-artifacts。

提交前至少检查：

```bash
git status --short --ignored
```

不要把依赖目录、构建产物、测试报告、环境文件、日志、证书或数据库快照纳入提交。
