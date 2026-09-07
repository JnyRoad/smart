# 快速验证
打开本worktree下smart-app作为HBuilderX工程，保持Vapor、字节码、样式隔离2.0。
先运行到Web，登录页显式进入演示；分别体验正式员工、外包／派遣、主管、安检账号。
检查全部应用搜索/收藏、物品申请、切换主管审批、安检选岗后执行；供应商在厂牌核验选东门，输入 DEMO-BADGE-001 后明确登记进入，再次扫码登记离开并查看独立事件记录；演示全程不产生真实业务。
生产登录与真实业务在docs/integration.md列明契约与未完成项。不要填写生产账号验证骨架。
自动化与编译命令以smart-app/README.md最终记录为准。


## Phase 10 联调前置验证
普通申请人的 Identity.posts 为空，申请候选接口返回两个点位时，仍可填写起终点并提交；设置不能出现安检岗位。候选加载失败保留表单内容并阻止提交，重试可恢复；旧账号迟到响应不能写入新账号。现场查询应带当前岗位，空岗位不发送请求。

真实业务闭环还需目标测试网关、安全配置位置及脱敏厂牌二维码。测试环境尚未确定时，不启动默认连接外部数据库的服务，也不将演示闭环记录为联调通过。

## Phase11 服务端规则验证
从本worktree的 `smart-module/` 运行供应商和物品纯领域测试：

```bash
mvn -o -pl smart-platform/smart-platform-core -am test \
  -Dtest=SupplierAccessWorkflowTest,ConfidentialReleaseWorkflowTest \
  -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false
```

核对资格停用/审批/有效期/区域、岗位授权、核验与操作人绑定、人员区域状态和版本、登记前资格变化及独立事件。该命令不启动Spring服务，不连接数据库、DHR或厂牌服务；通过后仍需在T041记录真实接口和持久化验收。

## 本任务独立Oracle
用户已确认新建本任务本机环境，替代此前未指定测试环境的状态。环境生成、归属校验、启动与恢复见 [专用环境说明](../../docker/client-integration/README.md)。仅本任务 `smart-client-008` 使用 `127.0.0.1:15218/FREEPDB1` 和 `SMART_CLIENT_008`；本机凭据被精确忽略，不打印或提交。

T051已实测Oracle AI Database 26ai Free / VERSION_FULL 23.26.3.0.0在本机ARM健康，初始schema无业务表。Phase12在此目标新增版本化资源和生产JDBC仓储集成测试；具体执行入口按该增量实现后说明，不能用普通领域测试代替数据库事务验收。真实身份、厂牌来源、卡证与HTTP仍单独联调。

## Phase12 真实Oracle验证
完成本任务环境启动后，从本worktree的 `smart-module/` 执行：

```bash
mvn -o -pl smart-platform/smart-platform-core -am test \
  -Dtest=JdbcConfidentialReleaseStoreOracleTest,JdbcSupplierAccessStoreOracleTest,ConfidentialReleaseWorkflowTest,SupplierAccessWorkflowTest \
  -Dsurefire.failIfNoSpecifiedTests=false -DfailIfNoTests=false \
  -Dsmart.client.008.oracle.test=true \
  -Dsmart.client.008.oracle.envFile="$PWD/../docker/client-integration/.env.client-local"
```

参数只传文件位置，密码由测试进程私读，勿将密码展开为命令参数。当前验收目标固定为127.0.0.1:15218、FREEPDB1、SMART_CLIENT_008及该worktree下的专用0600配置；新的测试目标须先核实后调整约束。目标表全部不存在才执行对应版本资源，部分存在、结构不符或目标不符时拒绝；只清理本轮合成ID。

最终61项通过，其中20项实际使用Oracle，41项为既有纯领域规则。未设置启用开关时，两个Oracle测试类各记录一次跳过，不能视为数据库通过；显式启用但缺少或指定错误配置时必须失败。此入口不会启动HTTP、DHR或设备服务。
