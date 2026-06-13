# smart-gateway

`smart-gateway` 是基础平台统一 API 网关，POM 描述为“服务网关，基于 spring cloud gateway”。

它位于前端和后端微服务之间，负责请求路由、网关过滤器、验证码、Redis 网关状态和 Swagger 相关入口；具体业务实现不放在网关里。

## 目录结构

```text
smart-gateway/
├── README.md
├── pom.xml
├── Dockerfile
└── src/
    └── main/
        ├── java/com/tce/smart/gateway/
        │   └── SmartGatewayApplication.java
        └── resources/
```

## 模块边界

- 网关路由、过滤器、跨服务入口在本模块。
- 动态路由公共能力在 `smart-common-gateway`。
- 鉴权公共能力在 `smart-common-security`；业务权限判断不要写进网关。
- 后端业务 API 路径由 `smart-module` 各服务提供。

## 常用命令

在 `smart/` 目录执行：

```bash
mvn -pl smart-gateway -am package -DskipTests
```

构建产物位于：

```text
smart-gateway/target/smart-gateway.jar
```
