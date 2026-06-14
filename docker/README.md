# docker

`docker/` 保存本地 Docker 开发环境的辅助文件，配合根目录 [../docker-compose.dev.yml](../docker-compose.dev.yml) 使用。

它只负责本地依赖服务和配置初始化，不保存生产部署脚本。

## 目录结构

```text
docker/
├── README.md
├── .env.local.example       # 本地 Docker 环境变量样例，复制到根目录 .env.local 后使用
└── nacos/
    ├── init-nacos.sh        # 等待 Nacos 就绪、创建 namespace、发布本地配置
    └── config/
        └── dev/             # 本地 dev group 的 Nacos YAML 配置
```

## 使用方式

```bash
cp docker/.env.local.example .env.local
docker compose --env-file .env.local -f docker-compose.dev.yml up smart-nacos smart-nacos-init smart-redis smart-zookeeper smart-kafka
```

如果要让后端服务完全在本机容器内连本地核心数据库，先启用本地 Oracle：

```bash
docker compose --env-file .env.local -f docker-compose.dev.yml --profile local-db up smart-oracle
```

OA、EHR、DHR、XCC6、BG/出差、考勤、临时人员、门禁等第三方系统不由本项目容器化部署，也不提供本地数据库替身。专项联调时只在本地 env 中填写对应第三方测试环境地址。

启用常规后端容器时使用 `backend` profile：

```bash
docker compose --env-file .env.local -f docker-compose.dev.yml --profile backend up
```

门禁桥接按园区场景二选一。`bridge` 和 `bridge-isc` profile 会使用本地 Oracle、Kafka 和 `smart-mock-http`。`smart-mock-http` 只是本地测试桩，不代表第三方系统，也不保存第三方数据库：

```bash
# 直连海康设备终端
docker compose --env-file .env.local -f docker-compose.dev.yml --profile bridge up smart-bridge

# 对接海康 ISC 平台
docker compose --env-file .env.local -f docker-compose.dev.yml --profile bridge-isc up smart-bridge-isc
```

水电表集中器是独立服务，不依赖 Nacos 注册，需要时单独启用：

```bash
docker compose --env-file .env.local -f docker-compose.dev.yml --profile bridge-concentrator up smart-bridge-concentrator
```

前端联调同时启用 `backend` 和 `frontend` profile。`smart-ui` 镜像在 Docker 内执行 `pnpm build` 后交给 nginx，`smart-h5` 镜像运行 Next.js standalone server：

```bash
docker compose --env-file .env.local -f docker-compose.dev.yml --profile backend --profile frontend up
```

如果后端已经由其它环境提供，也可以只启动前端 profile；此时要确保 `smart-gateway` 能访问到对应后端服务。

## 校验方式

```bash
scripts/test-docker-compose-dev.sh
```

该脚本会检查本地 Compose 服务覆盖、profile 依赖、`.env.local.example` 是否只保留自有依赖默认值、核心数据库是否为 Oracle、第三方数据源是否未被本地化部署，以及 Compose 服务镜像和 Dockerfile 基础镜像 tag 是否可拉取。

## 线上测试和生产边界

`docker-compose.dev.yml` 只用于本机开发测试。线上测试和线上生产应使用独立 runtime 编排：

- 不启动本地 Oracle、MockServer 这类开发替身服务。
- 不提交真实 env、密钥、证书、数据库地址和 Nacos 导出配置。
- 镜像版本由 CI/CD 或发布清单注入，线上 env 文件由部署系统管理。
- 线上测试和生产使用相同 runtime 模板，但使用不同 env、域名、证书、namespace、数据库和消息队列地址。

## 注意事项

- `.env.local.example` 是样例文件，可以提交；根目录 `.env.local` 是真实本地配置，必须忽略。
- `nacos/config/dev/*.yml` 是本地开发配置模板，真实生产配置不要放进本目录。
- Nacos 初始化脚本用 `curl` 调 Nacos OpenAPI 发布配置；如果新增服务配置，需要同步增加对应 YAML。
- Java 后端 Dockerfile 使用已构建出的 Spring Boot Jar；构建镜像前先在对应子项目执行 Maven 打包，或使用根目录 `scripts/build-release-jars.sh` 生成后端 Jar。
- `smart-bridge` 是直连海康设备终端的桥接服务；`smart-bridge-isc` 是对接海康 ISC 平台的桥接服务。两者都保留镜像构建，但实际园区按接入方式选择启用。
- `smart-ui` 的 Docker 本地默认 API 目标是 `http://smart-gateway:9990`；真实部署可通过 `.env.local` 或部署期运行时配置覆盖。
- `smart-h5` 的 Docker API 代理目标在镜像构建期写入 Next.js rewrites，默认是 `http://smart-gateway:9990`；变更 `SMART_H5_API_PROXY_TARGET` 后需要重建镜像。
- 本次 Compose 覆盖范围按项目当前容器化清单执行，不包含 `FileReceiver`；它是发布清单里的独立 Windows 打印机照片接收程序，仍按自身 README 打包部署。
