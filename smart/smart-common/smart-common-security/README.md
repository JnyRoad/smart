# smart-common-security

`smart-common-security` 是公共安全模块，POM 描述为“安全工具类”。

## 目录结构

```text
smart-common-security/
├── README.md
├── pom.xml
└── src/main/java/
```

## 模块边界

- 放 OAuth2 资源服务器、安全上下文、权限工具、Feign 安全调用支撑。
- 认证授权中心入口在 `../../smart-auth/`。
- 用户、角色、菜单数据来源在 `../../smart-upms/`。
