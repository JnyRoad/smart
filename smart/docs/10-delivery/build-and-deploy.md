# 构建与部署

## 本地构建

前置：
- JDK 1.8
- Maven 3.6+
- 能访问内部 Nexus `https://10.13.21.7/repository/maven-public/`（在 `~/.m2/settings.xml` 配置）

```bash
# 全量构建（跳过测试）
mvn clean package -DskipTests

# 单模块
mvn -pl smart-upms/smart-upms-biz -am clean package -DskipTests
```

> **注意**：根 pom 的 `maven-compiler-plugin` 配置了 `<skip>true</skip>`，顶层不编译；各子模块自身的 plugin 配置才生效。这是当前一处易让人困惑的设置，**修改前请评估对 CI 的影响**。

构建产物：
- `smart-gateway/target/smart-gateway.jar`
- `smart-auth/target/smart-auth.jar`
- `smart-upms/smart-upms-biz/target/smart-upms-biz.jar`

## 镜像构建

各服务自带 Dockerfile：

```bash
# 以 upms 为例
docker build \
  --build-arg APP_ENV=prod \
  -f smart-upms/smart-upms-biz/Dockerfile \
  -t smart-upms:yuto-3.0 .
```

Dockerfile 共同特征：
- Base: `anapsix/alpine-java:8_server-jre_unlimited`
- 时区：`Asia/Shanghai`
- 日志：宿主机挂载 `/home/tce/smart/logs`
- 启动：`java -Dspring.profiles.active=$APP_ENV -Djava.security.egd=file:/dev/./urandom -jar xxx.jar`

## docker-compose（仅供本地试运行）

仓库根 `docker-compose.yml` 仍含 `smart-eureka` 与 `smart-config` 两个**废弃服务**，启动前需要：

1. 注释掉 `smart-eureka` 与 `smart-config` 块；
2. 自备 Nacos / Redis / 数据库；
3. 通过环境变量 `NACOS_URL`、`NACOS_PORT`、`NACOS_GROUP` 注入。

或更推荐直接以 Kubernetes / 集团已有部署体系运行。

## 配置

所有运行时配置走 **Nacos**：

- namespace：`eda914a9-b100-427b-9d37-4d7da89b841f`（生产固定）
- group：随 `APP_ENV` 切换（dev / test / prod）
- 公共配置：`common.yml`（数据库、Redis、Jasypt 公私钥）
- 服务私有：`smart-gateway-<env>.yml`、`smart-auth-<env>.yml`、`smart-upms-biz-<env>.yml`

**敏感字段**（数据库密码、Jasypt 主秘钥本身）通过 Jasypt 加密成 `ENC(...)` 形式，秘钥由环境变量 `JASYPT_ENCRYPTOR_PASSWORD` 注入。

## 启动顺序

1. 确保 Nacos / Redis / 数据库就绪；
2. 启动 smart-upms-biz、smart-auth；
3. 启动 smart-gateway；
4. 通过 `:9990/actuator/health` 验证健康。

## 发布流程（建议形式化）

仓库内未见 CI / CD 配置（`.github/`、`.gitlab-ci.yml`、`Jenkinsfile` 均不存在）。建议规划：

1. 主干合并触发 Jenkins / GitLab CI；
2. mvn build → mvn deploy 推送 jar 到内部 Nexus；
3. docker build + push 到内部 Harbor；
4. 通过 Ansible / K8s manifest 部署到目标环境；
5. 灰度：先 dev → test → 生产（许昌园区）。
