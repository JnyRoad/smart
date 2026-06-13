# smart-upms

`smart-upms` 是通用用户权限管理聚合模块，POM 描述为“通用用户权限管理聚合模块”。

它维护用户、角色、菜单、部门、客户端、日志、字典等基础管理能力，并向认证中心、网关和业务服务暴露 API。

## 目录结构

```text
smart-upms/
├── README.md
├── pom.xml
├── smart-upms-api/     # Entity / DTO / Feign API 等对外契约
└── smart-upms-biz/     # 可启动 UPMS 业务服务
    ├── Dockerfile
    └── src/main/java/com/tce/smart/admin/SmartAdminApplication.java
```

## 子模块

| 子模块 | 用途 |
| --- | --- |
| `smart-upms-api/` | 对其他服务暴露的 UPMS 类型、接口和 Feign 调用契约。 |
| `smart-upms-biz/` | UPMS 业务实现和 Spring Boot 启动入口。 |

## 常用命令

在 `smart/` 目录执行：

```bash
mvn -pl smart-upms -am package -DskipTests
mvn -pl smart-upms/smart-upms-biz -am package -DskipTests
```

可部署 Jar 位于：

```text
smart-upms/smart-upms-biz/target/smart-upms-biz.jar
```
