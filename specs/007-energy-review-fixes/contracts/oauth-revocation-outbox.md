# OAuth 撤销待办升级契约

本文件仅交付升级要求，未执行生产 SQL。适用于从本次基线 `157d4ca1` 升级本修复；不是对当前生产版本的重新盘点。

## 必需前置操作

在 **UPMS 的 SYS_OAUTH_CLIENT_DETAILS 所在数据库/schema** 建表，再启动新版 `smart-upms-biz`。不要建到只供 platform 使用的其他 schema，也不要将 SQL 填入 Nacos。

DBA 执行前核验现有对象、CLIENT_ID 字段实际长度、表空间、权限及 Oracle 版本；如同名对象已存在，应比对结构，不要删除重建或忽略报错。

```sql
CREATE TABLE SYS_OAUTH_CLIENT_REVOKE_TASK (
    TASK_ID     VARCHAR2(36 CHAR)  NOT NULL,
    CLIENT_ID   VARCHAR2(256 CHAR) NOT NULL,
    CREATE_TIME TIMESTAMP(6) DEFAULT SYSTIMESTAMP NOT NULL,
    NEXT_RETRY_AT TIMESTAMP(6) DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_SYS_OAUTH_CLIENT_REVOKE PRIMARY KEY (TASK_ID)
);

CREATE INDEX IDX_OAUTH_REVOKE_PENDING
    ON SYS_OAUTH_CLIENT_REVOKE_TASK (NEXT_RETRY_AT, CREATE_TIME, TASK_ID);

CREATE INDEX IDX_OAUTH_REVOKE_CLIENT
    ON SYS_OAUTH_CLIENT_REVOKE_TASK (CLIENT_ID, CREATE_TIME, TASK_ID);
```

所有 Oracle 对象名均不超过 30 字符。DDL 已与实体/Mapper 进行源码核对；尚未在真实 Oracle 执行，索引效率及执行计划仍需现场核验。

## 行为与配置

- 每次 scope 实际变化或删除客户端，变更与撤销待办在服务自有 `REQUIRES_NEW` 事务内原子提交。外层事务不能撤回该已提交单元，当前调用方不得假定可以。
- 提交后先清共享客户端缓存，再请求 auth 撤销旧 token；只有成功才按该任务 ID 删除待办。同客户端的新任务不会被旧补偿删除。
- 认证服务不可用时，接口会报错，但数据库变更可能已经提交。不要将报错理解为授权完全未改变；后台恢复，或重新提交相同 scope，可继续处理待办。
- 不承诺依赖服务故障时旧令牌立即失效；待办提供恢复后的补偿。Auth 和 UPMS 必须沿用共享的客户端缓存配置。
- `resetSecret` 沿用原有显式清缓存和同步吊销语义，未扩展为 outbox。
- 待办只保存任务 UUID、客户端 ID、创建时间、下次重试时间；无 token、secret 或 TTL。新任务立即到期，失败后按既有重试间隔推迟；后台按到期时间排序，避免失败首批阻塞全部后续待办。

默认每轮结束 60 秒后运行下一轮、每批最多 50 项。通常无需新增 Nacos 配置；需覆盖时在实际生效的 UPMS Data ID 中合并：

```yaml
security:
  oauth:
    client-revocation-retry-delay-ms: 60000
    client-revocation-recovery-batch-size: 50
```

合并到已有 `security.oauth` 层级，不要覆盖其他安全配置。修改后按部署流程重启 UPMS 并核验实际周期，不依赖定时注解热刷新；间隔和批量均应为正数。

## 升级和回退顺序

1. 备份当前应用包及对应配置；确认客户端库和缓存连接归属。
2. DBA 核验并创建上述 outbox 表和索引。
3. 升级 `smart-upms-biz`，检查 Mapper 加载、定时恢复和客户端授权故障恢复。
4. 升级 `smart-platform-biz`，再升级 `smart-schedule`；按 [quickstart](../quickstart.md) 核验开关、令牌和能耗读取。能耗本次没有新增 DDL，但此前能耗投影表仍是必要前提。
5. 验收正常与异常路径；用下列只读查询观察待办是否恢复，不手动删除记录来伪造成功。

```sql
SELECT COUNT(*) AS PENDING_COUNT,
       MIN(CREATE_TIME) AS OLDEST_PENDING_AT
FROM SYS_OAUTH_CLIENT_REVOKE_TASK;
```

回退应用时保留待办表及数据。旧版 UPMS 不会消费这些待办，须人工确认未完成授权撤销的处置方式；未经审计和独立授权不删表、不清空任务。
