# Smart App 本机演示环境

本目录只运行 `smart-app-demo`，容器、网络、卷、Nacos 命名空间和 Oracle schema 都与既有 `smart-client-008` 隔离。它只写入虚构人员、虚构访客申请和虚构厂牌主键 `900000001`，不会读取 DHR、生产访客数据或旧客户端环境。资源带有 `com.yuto.smart.environment=app-demo-local` 标签，启动脚本拒绝接管其他项目资源。

从仓库根目录先生成一次本机凭据。脚本会拒绝覆盖环境文件、拒绝接管同名资源，并且只检查 `smart-app-demo` 资源；不会读取旧 `smart-client-008` 的配置或凭据。

```bash
node scripts/app-demo-env.mjs
```

随后构建认证、网关、UPMS 和平台服务，验证 Compose 并启动隔离环境：

```bash
cd smart
mvn -o -pl smart-auth,smart-gateway,smart-upms/smart-upms-biz -am -DskipTests package
cd ../smart-module
mvn -o -pl smart-platform/smart-platform-biz -am -DskipTests package
cd ..
docker compose -p smart-app-demo --env-file docker/app-demo/.env.local -f docker/app-demo/compose.yml config --quiet
docker compose -p smart-app-demo --env-file docker/app-demo/.env.local -f docker/app-demo/compose.yml up -d --build
node docker/app-demo/verify.mjs
```

`verify.mjs` 从本机网关自动验证外包、派遣、正式员工三类统一登录；物品申请、审批、东门出发、西门到达；供应商厂牌核验、进入、离开和记录；以及预约码与未知账号的拒绝路径。它不输出环境密码或会话 token。

App 适配器使用本机网关时，必须显式开启受限回环配置，并在完成后恢复默认配置：

```bash
cd smart-app
npm run demo:enable
npm run test:demo-api
npm run demo:disable
```

`demo:enable` 只接受环境文件声明的精确 `127.0.0.1` 网关端口；默认 App 配置为空地址并强制 HTTPS，不能把本机 HTTP 配置带到其他环境。

仅网关和 Nacos 映射到 `127.0.0.1`；Oracle 和 Redis 不映射主机端口。演示停止时使用 `docker compose -p smart-app-demo --env-file docker/app-demo/.env.local -f docker/app-demo/compose.yml stop`，不要使用带卷清理参数的命令。

真实 DHR、真实供应商厂牌、PDA 扫码头、摄像头和生产环境均不属于该本机演示验收范围。
