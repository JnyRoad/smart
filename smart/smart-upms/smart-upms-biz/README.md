# smart-upms-biz

`smart-upms-biz` 是 UPMS 可部署业务服务。

## 目录结构

```text
smart-upms-biz/
├── README.md
├── pom.xml
├── Dockerfile
└── src/main/java/com/tce/smart/admin/SmartAdminApplication.java
```

## 模块边界

- 放用户、角色、菜单、部门、客户端、字典、日志等 UPMS 管理实现。
- 对外契约放在 `../smart-upms-api/`。
- 权限公共工具放在 `../../smart-common/smart-common-security/`。

## 常用命令

在 `smart/` 目录执行：

```bash
mvn -pl smart-upms/smart-upms-biz -am package -DskipTests
```
