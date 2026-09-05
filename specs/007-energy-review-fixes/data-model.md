# 数据模型

- 原能耗日事实、范围规则、园区日结果保持原含义；规则缺父级产生 INVALID。
- 应用读取只复用月累计 DTO，不增加事实字段或实时扫描。
- 投影队列继续保存表计日状态、请求计数、重试和租约；恢复进度使用 Redis `smart:energy:backfill:cursor:v1`，无 TTL。固定序列为 `month|date|through|source|afterId|scanned|accepted|failed|completedOn`，不写真实凭据。
- 每项先检查/入队，再用 Lua 比较旧完整序列后更新断点；冲突明确失败，不覆盖别的批次的新进度。Redis 丢失会从当月重新扫描，已存在的活跃队列不重复覆盖。持久化、备份、各 platform 实例共享同一 Redis 是运维要求。
- 完成当月扫描的当天不重复扫，次日重新检查月内事实；跨月先完成旧月到月末，再进入下一月。扫描完成仅表示本轮检查/入队结束，不表示队列已消费完或所有质量都 READY。
- 客户端撤销待办使用 UPMS 客户端所在数据库的新 outbox 表；客户端变更和待办在同一事务写入。只保存 clientId、随机版本与必要恢复信息，不保存 secret/token；成功按版本移除，失败保留重试。

先建 `SYS_OAUTH_CLIENT_REVOKE_TASK`（`TASK_ID`、`CLIENT_ID`、`CREATE_TIME`、`NEXT_RETRY_AT`），再升级 UPMS。按 NEXT_RETRY_AT 到期顺序选取有限批次；失败只推迟该任务，未尝试任务不会永远排在持续失败首批之后。本轮仅交付 [DDL 与升级契约](contracts/oauth-revocation-outbox.md)，不执行真实数据库操作，不修改已上线的历史迁移。
