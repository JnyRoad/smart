# 技术栈（STACK）

> 所有版本以 `pom.xml` 为准。多数版本已 EOL 或存在已知 CVE，详见 [12-risks/tech-debt.md](../12-risks/tech-debt.md)。

## 运行时

| 组件 | 版本 | 状态 |
|------|------|------|
| Java | 1.8 | EOL（公开支持），仍有商业支持 |
| Spring Boot | 2.1.3.RELEASE | **EOL**（2020-08） |
| Spring Cloud | Greenwich.RELEASE | **EOL** |
| Spring Cloud Alibaba | 2.1.4.RELEASE | 老版本 |
| Spring Platform | Cairo-SR7 | 老版本 |

## 核心依赖

| 类别 | 库 | 版本 | 说明 |
|------|----|------|------|
| Web | spring-cloud-starter-gateway | 随 Spring Cloud | Reactor Netty，替代了 Tomcat |
| 注册中心 | spring-cloud-starter-alibaba-nacos-discovery | 随 SCA | |
| 配置中心 | spring-cloud-starter-alibaba-nacos-config | 随 SCA | |
| 安全 | spring-security-oauth2 | 2.3.4.RELEASE | OAuth2 AuthServer + Resource Server |
| 安全 | spring-security-oauth2-autoconfigure | 2.1.2.RELEASE | |
| 数据 | mybatis-plus | 由 common-bom 锁定 | |
| 数据库驱动 | （Oracle/MySQL 驱动，来自 Nacos 配置） | - | 仓库 pom 未直接声明 |
| 缓存 | spring-boot-starter-data-redis | 随 SB | RedisTokenStore |
| 工具 | hutool-all | 4.4.5 | |
| 工具 | lombok | provided | |
| 模板 | velocity | 1.7 | 过时；用途位置待确认 |
| 协调 | curator | 2.10.0 | ZooKeeper 客户端 |
| 序列化 | xstream | 1.4.14 | **有 CVE** |
| 加密 | jasypt-spring-boot-starter | 2.1.1 | 配置文件加解密 |
| TTL | transmittable-thread-local | 2.10.2 | 线程上下文传递 |
| 文档 | springfox-swagger2 | 2.9.2 | 老版本 |
| 验证码 | kaptcha | 0.0.9 | |
| 监控 | spring-boot-admin-starter-client | 2.1.3 | |
| 链路 | spring-cloud-starter-sleuth + sleuth-zipkin | 随 SC | |
| 断路器 | spring-cloud-starter-netflix-hystrix | 随 SC | Netflix Hystrix 已停止维护 |

## 构建

| 工具 | 版本/插件 | 用途 |
|------|----------|------|
| Maven | 3.x | 项目构建 |
| maven-compiler-plugin | 3.8.0 | 编译；**注意**：根 pom 中 `<skip>true</skip>` 实际跳过了顶层编译（依赖各子模块自身重写） |
| spring-boot-maven-plugin | 2.1.3 | 可执行 jar 打包 |
| git-commit-id-plugin | 2.2.5 | 注入 git 信息（actuator info 端点用） |
| pom 打包 | `<packaging>pom</packaging>` | 顶层聚合 |

## 私服

- **Maven 仓库**：`https://10.13.21.7/repository/maven-public/`（内部 Nexus）
- **发布仓库**：`https://10.13.21.7/repository/maven-releases/`

## 关键技术选择背后的理由（推断）

| 选择 | 推断理由 | 风险 |
|------|---------|------|
| Spring Cloud Gateway（而非 Zuul） | 高并发 + 反应式 + Spring Cloud 官方推荐 | 学习曲线 |
| Nacos（而非 Eureka + Spring Cloud Config） | 单实例双角色、运维简化、阿里中文社区 | 与 SC Greenwich 兼容性需关注 |
| RedisTokenStore（而非 JWT） | Token 可撤销、登出即失效 | Redis 故障即影响登录 |
| 动态路由 | 业务子系统频繁增减，避免重启网关 | 路由配置变更引入风险 |
| MyBatis-Plus | 国内主流、减少样板 | 与 SQL Server 等异构支持需测试 |
| Hutool | 工具齐全、中文文档 | 单一第三方依赖深度耦合 |
| Hystrix | 当年默认 | **已 EOL**，应迁移到 Resilience4j |
