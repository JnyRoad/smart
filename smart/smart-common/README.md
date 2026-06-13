# smart-common

`smart-common` 是基础平台公共组件聚合模块，POM 描述为“公共聚合模块”。

它提供 Maven BOM、通用工具、数据访问、安全、网关、日志和 Swagger 等跨服务基础能力。只有明确被多个模块复用的能力才应放入这里。

## 目录结构

```text
smart-common/
├── README.md
├── pom.xml
├── smart-common-bom/       # 公共依赖版本管理
├── smart-common-core/      # 核心工具、异常、统一响应等
├── smart-common-data/      # Redis / 数据访问 / 分布式锁相关基础能力
├── smart-common-gateway/   # 网关动态路由等公共能力
├── smart-common-log/       # 操作日志 AOP 和日志基础能力
├── smart-common-security/  # OAuth2 资源服务器、安全工具、权限支撑
└── smart-common-swagger/   # Swagger 2 接口文档配置
```

## 子模块

| 子模块 | POM 描述 | 用途 |
| --- | --- | --- |
| `smart-common-bom/` | 公共版本控制 | 统一依赖版本，供平台和业务模块导入。 |
| `smart-common-core/` | 公共工具类核心包 | 基础类型、工具、异常、返回对象和通用依赖。 |
| `smart-common-data/` | 数据操作相关 | Redis、数据访问增强、ShedLock 相关基础能力。 |
| `smart-common-gateway/` | smart gateway | 网关公共支撑，例如动态路由。 |
| `smart-common-log/` | 日志服务 | 操作日志、审计日志相关公共代码。 |
| `smart-common-security/` | 安全工具类 | OAuth2、资源服务器和权限校验支撑。 |
| `smart-common-swagger/` | 接口文档 | Swagger 2 / bootstrap-ui 配置。 |

## 维护规则

- 不要把单一业务服务的代码上移到 common。
- 公共模块变更要同时考虑 `smart/` 和 `smart-module/` 的编译影响。
- 新增依赖版本优先放在 `smart-common-bom/`，避免各服务重复声明。
