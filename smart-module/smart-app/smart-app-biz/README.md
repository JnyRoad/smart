# smart-app-biz

`smart-app-biz` 是裕慧家园 App 业务服务。

现有历史说明表明它包含 App 管理后台功能、微信公众号链接页面接口和官网链接页面接口。

## 目录结构

```text
smart-app-biz/
├── README.md
├── pom.xml
├── Dockerfile
└── src/main/java/com/tce/smart/app/SmartAppApplication.java
```

## 模块边界

- 放 App、微信公众号/H5、官网链接页面相关后端业务实现。
- 对外契约放在 `../smart-app-api/`。
- 发布脚本收集 `target/smart-app-biz.jar`。
