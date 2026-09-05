# 验证与配置说明

## 交付基线与范围

基于 `origin/main` 的 `157d4ca1060873c480644714965093b81b514c2e`，分支 `fix/energy-projection-review`。开发起点为 `cb47dc74`，收尾时再次查询远端并快进纳入 #184；该访客功能与本修复无路径重叠。本地修复验收后已获用户授权提交 PR；本交付不包含合并、部署或生产配置修改。

## API 契约

- 应用入口：`GET /platform/open/energy/month/{parkId}`（平台服务内路径不带 `/platform`）。只传园区，不传起止日期；沿用既有服务器业务时区、月累计 DTO、数据质量及更新时间字段。
- 使用由认证服务签发的 `client_credentials` token，scope 为 `server`，客户端须绑定所需园区。空园区集合不表示全部园区。
- 原后台用户入口 `/platform/sd/statistics/month/{parkId}` 保持不变；普通用户 token 不能代替应用 token 调用新入口。
- 汇总只读已有投影结果，未齐数据仍按原质量口径返回；当日数值不是每次请求都现场抄表。

## Nacos 配置位置

以部署进程实际的 `NACOS_NAMESPACE`、`NACOS_GROUP` 和生效 profile 为准，不能把源码的 `dev` 默认值当成生产值。服务基础 Data ID 分别为 `smart-schedule.yml`、`smart-platform.yml`、`smart-upms-biz.yml`；如生产启用 profile 或覆盖 prefix，应编辑实际生效的配置项。

调度继续使用 `smart-schedule` 已有配置键，不需要为本修复创建新 OAuth 模块授权域：

```yaml
task:
  job:
    energy-projection-process-pending: true
    energy-projection-reconcile: true
    energy-projection-backfill: true
  energy:
    projection:
      pending-cron: "0 0/5 * * * ?"
      reconcile-cron: "0 15 2 * * ?"
smart:
  energy:
    zone-id: Asia/Shanghai
    projection:
      oauth:
        access-token-uri: ${ENERGY_OAUTH_TOKEN_URI}
        client-id: ${ENERGY_OAUTH_CLIENT_ID}
        client-secret: ${ENERGY_OAUTH_CLIENT_SECRET}
        scope: server
        energy-projection-run-scope: server
```

这些环境变量是示例占位，须由部署环境提供，不能原样当成凭据。`client-id` 对应后台客户端 App ID，`client-secret` 使用该客户端的原始密钥，不含数据库密码编码前缀；令牌地址取当前生产网关/认证服务真实地址。本轮没有读取或验证生产密钥及地址。合并到已有 YAML 的相应层级，不要再添加重复的 `task` / `smart` 顶层键。

平台现有批预算项为 `smart.energy.backfill.max-requests`，默认 1000（表计日扫描预算，不是整月可处理表数）。保持两个服务的 `smart.energy.zone-id` 一致。

## 自动恢复的范围

- 补齐以当前活跃表为候选，完成当月扫描后次日重新检查；保留的未完成月份会先续跑到月末，再追赶下一月。它不是任意历史月份的持续审计器。
- 扫描期间被逻辑删除的已入队表会产生 `METER_UNAVAILABLE`；物理缺失表会留下明确失败。此前已删除且没有待处理请求的历史事实不会由活跃表扫描重新发现，需单独核对，不应把“扫描完成”理解为历史数据已全面清理。
- Redis 断点必须持久化并由 platform 实例共享。断点丢失时从当前月重新扫描；更早月份的未完成进度需要人工核对。完成标记只表示扫描/入队结束，是否已计算完整仍看队列和数据质量。
- `reconcile` 仍按指定日期分页遍历全表，但只做持久入队。数据库全局故障会明确失败，需对原日期重试；不是一次请求已完成全园区计算。
- 既有日事实保留首次记录的园区归属快照；修改表计当前园区不会自动把历史用量迁移到新园区，本修复没有改变该归属口径。

## 上线后验收边界

以下生产操作本轮未执行，需后续授权后进行：

1. 用真实签发的应用 token 分别请求获授权和未授权园区，预期成功与拒绝；匿名、用户 token、新入口非法园区均拒绝。检查原后台有权用户仍可查询。
2. 对照已知表计日样本核验水、电累计。总表无规则但分表指定该总表时，分表必须标记 `INVALID`，不得重复累计。
3. 以超过单批的缺口观察多次短批的扫描位置推进；修改排除规则、祖先规则、倍率或历史读数后，检查旧日结果最终刷新。
4. 模拟单表投影失败及认证服务短暂不可用，确认错误可见、恢复后待办继续处理。检查 Redis 共享、持久化和备份策略。
5. 在 Oracle 验证新增查询的执行计划、索引利用、千表场景实际耗时及锁等待；本地 Mock 不代表性能通过。

## 本地验证证据

从仓库 `smart-module/` reactor 根目录执行（本机已具备依赖，因此使用离线模式）：

```bash
mvn -o -pl smart-platform/smart-platform-biz,smart-schedule -am \
  '-Dtest=*Energy*,OpenApiServerScopeMockMvcTest' \
  -Dsurefire.failIfNoSpecifiedTests=false package
```

2026-09-05 05:02:07 UTC，在最新基线 `157d4ca1` 上的最终能耗集成结果（含跨日质量重判及跨月完成标记修复）：退出码 0，21 个 reactor 项目 `BUILD SUCCESS`；API 1、core 7、platform-biz 67、schedule 20，合计 **95 项**，0 失败/错误/跳过。生成 `smart-platform-biz.jar` 和 `smart-schedule.jar`。测试范围为能耗链及 server 授权回归，不等同整个仓库全量测试或生产可用性验证。既有编译警告和故障注入测试预期日志仍存在，不宣称 warning-free。最终命令另加 `-Dsurefire.useFile=true -Dmaven.test.redirectTestOutputToFile=true` 将详细故障注入日志保留在 target 测试报告内。

基础依赖已从最新基线执行 `smart/` 下 `mvn -pl smart-upms/smart-upms-biz -am install -DskipTests` 成功；该命令跳过测试，仅用于刷新本地最新公共依赖。

从仓库 `smart/` reactor 根目录执行：

```bash
mvn -o -pl smart-common/smart-common-security,smart-upms/smart-upms-biz -am \
  '-Dtest=*Oauth*,OpenApi*' -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.test.redirectTestOutputToFile=true package
```

2026-09-05 04:56:41 UTC，在最新基线 `157d4ca1` 上的最终结果（包含失败首批公平重试修复）：退出码 0，11 个 reactor 项目 `BUILD SUCCESS`；common-security 20、UPMS 37，合计 **57 项**，0 失败/错误/跳过，生成 `smart-upms-biz.jar`。其中客户端服务目标类 34 项、真实 MyBatis 展开契约 2 项、控制器 1 项。加上最终能耗集成共 **152 项定向测试**，没有按重复运行次数累加。

UPMS 升级必须先完成 [新增撤销待办表及恢复配置](contracts/oauth-revocation-outbox.md)。四组任务独立评审及整体审查已完成；整体审查唯一的跨月完成标记问题已修复，限定复审通过，无未决修复发现。

提交 PR 前再次执行上述两组命令：2026-09-05 05:07:05 UTC 能耗 95 项、05:06:53 UTC 基础 57 项均通过，三个服务重新打包成功；此次复验未修改业务代码。
