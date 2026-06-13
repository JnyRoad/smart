# FileReceiver

`FileReceiver` 是一个独立 Spring Boot 程序，用于接收入厂申请相关的人脸照片。现有说明表明它运行在许昌打印机 Windows 电脑上，根发布清单使用 `build/file.jar` 作为发布产物。

## 目录结构

```text
FileReceiver/
├── README.md
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/example/demo/
│       │   └── FileApplication.java
│       └── resources/
├── build/       # Spring Boot 可执行 Jar 输出目录，忽略提交
└── target/      # Maven 构建目录，忽略提交
```

## 模块边界

- 只处理照片接收程序自身的 HTTP 服务和文件接收逻辑。
- 不属于 `smart-module` 主聚合父 POM 的常规业务服务结构，打包路径也与其他服务不同。
- 不要把管理后台、App 或桥接业务写入本模块。

## 常用命令

在 `smart-module/FileReceiver/` 目录执行：

```bash
mvn clean package -DskipTests
```

发布脚本读取的可部署产物：

```text
build/file.jar
```
