# smart-auth

`smart-auth` 是基础平台的认证授权中心，POM 描述为“认证授权中心，基于 spring security oAuth2”。

它负责登录、OAuth2 token 签发/校验相关能力，依赖 `smart-upms-api` 查询用户与权限数据，并依赖 `smart-common-security`、`smart-common-data` 等公共组件。

## 目录结构

```text
smart-auth/
├── README.md
├── pom.xml
├── Dockerfile
└── src/
    └── main/
        ├── java/com/tce/smart/auth/
        │   └── SmartAuthApplication.java
        └── resources/
```

## 模块边界

- 认证授权入口、OAuth2 配置、token 相关逻辑在本模块。
- 用户、角色、菜单、客户端等基础数据不在本模块维护，归 `smart-upms`。
- 权限注解、资源服务器通用逻辑应优先放在 `smart-common-security`。

## 常用命令

在 `smart/` 目录执行：

```bash
mvn -pl smart-auth -am package -DskipTests
```

构建产物位于：

```text
smart-auth/target/smart-auth.jar
```
