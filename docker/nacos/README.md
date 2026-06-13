# docker/nacos

`docker/nacos/` 保存本地 Docker 环境的 Nacos 初始化逻辑和配置文件。

## 目录结构

```text
docker/nacos/
├── README.md
├── init-nacos.sh      # 等待 Nacos 就绪并发布配置
└── config/
    └── dev/           # 本地 dev group 的 YAML 配置
```

## 维护规则

- 新增后端服务本地配置时，在 `config/dev/` 增加对应 YAML。
- `init-nacos.sh` 只负责本地初始化，不作为生产配置发布工具。
- 不提交真实生产密钥、账号或导出的 Nacos 配置。
