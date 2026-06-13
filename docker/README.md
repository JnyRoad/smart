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
docker compose -f docker-compose.dev.yml up smart-nacos smart-nacos-init smart-redis smart-kafka
```

启用后端容器时使用 `backend` profile：

```bash
docker compose -f docker-compose.dev.yml --profile backend up
```

## 注意事项

- `.env.local.example` 是样例文件，可以提交；根目录 `.env.local` 是真实本地配置，必须忽略。
- `nacos/config/dev/*.yml` 是本地开发配置模板，真实生产配置不要放进本目录。
- Nacos 初始化脚本用 `curl` 调 Nacos OpenAPI 发布配置；如果新增服务配置，需要同步增加对应 YAML。
