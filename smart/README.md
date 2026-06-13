# smart

`smart/` 是智慧园区基础平台后端聚合工程，提供所有业务模块共同依赖的网关、认证、UPMS 权限管理和公共组件。

它不承载具体园区业务流程；业务 API 在同级 [../smart-module/](../smart-module/) 中实现，管理端前端在 [../smart-ui/](../smart-ui/)，微信 H5 在 [../smart-h5/](../smart-h5/)。

## 目录结构

```text
smart/
├── README.md
├── pom.xml                         # Maven 聚合 POM：smart-gateway / smart-auth / smart-upms / smart-common
├── docker-compose.yml              # Docker Compose 示例
├── docs/                           # 基础平台回溯文档、模块边界、API、安全、部署说明
├── scripts/                        # 基础平台脚本
├── smart-auth/                     # OAuth2 认证授权中心，可部署服务
├── smart-gateway/                  # Spring Cloud Gateway 网关，可部署服务
├── smart-upms/                     # 用户、角色、菜单、部门、客户端、日志等 UPMS 聚合模块
└── smart-common/                   # 公共组件聚合模块，被 smart-module 依赖
```

## 模块说明

| 模块 | 用途 | 产物形态 |
| --- | --- | --- |
| `smart-gateway/` | 统一 API 网关，处理路由、验证码、Redis 网关状态和 Swagger 聚合入口。 | Spring Boot 可执行 Jar |
| `smart-auth/` | OAuth2 认证授权中心，依赖 UPMS API、公共安全、公共数据组件。 | Spring Boot 可执行 Jar |
| `smart-upms/` | 通用用户权限管理，包含 API 包和可启动业务服务。 | 聚合模块 |
| `smart-common/` | BOM、核心工具、数据访问、安全、网关、日志、Swagger 等公共组件。 | 聚合模块 / library jars |
| `docs/` | 基础平台回溯文档。 | Markdown |
| `scripts/` | Maven 版本校验、静态检查脚本。 | shell / Ruby / JS |

## 常用命令

在本目录内执行：

```bash
mvn clean install -DskipTests
mvn -pl smart-gateway -am package -DskipTests
mvn -pl smart-auth -am package -DskipTests
mvn -pl smart-upms/smart-upms-biz -am package -DskipTests
```

如需整套本地依赖环境，优先从根目录使用 [../docker-compose.dev.yml](../docker-compose.dev.yml)。

## 开发边界

- 新增基础能力放在 `smart-common-*`，但只有被多个服务复用时才抽公共组件。
- 认证、权限、路由相关变更要同时检查 `smart-auth`、`smart-gateway`、`smart-upms` 和 `smart-common-security`。
- 业务微服务不要写进本目录；应放在 [../smart-module/](../smart-module/)。
- `.flattened-pom.xml`、`target/`、日志、真实环境文件和本地 IDE 文件都由根 `.gitignore` 忽略。
