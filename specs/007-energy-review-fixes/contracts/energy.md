# 接口契约

## 应用月累计
GET /platform/open/energy/month/{parkId}，不接受起止日期。
只支持 server 的应用令牌，目标园区必须在 app_park_ids 中；拒绝时不访问汇总查询服务。
响应复用现有 Result<ParkUtilityUsageMonthToDateRespDTO>，保留水电分项、单位、质量与计算时间。
原 /platform/sd/statistics/month/{parkId} 保持后台用户权限和园区校验。

## 投影维护
既有内部路径及 server 授权不放宽。单批受控，未完成可续跑。失败不伪造整批成功，持久化重试须幂等。

- `reconcile/{businessDate}` 的成功含义为指定日的活跃表已持久化入队（或已有活跃请求），不是同步重算完成；返回结构仍为 `Result<Boolean>`。
- `backfill-month-to-date` 按持久断点做一个有界检查批次。仅扫描/入队成功不代表月数据齐全，须同时看队列及返回质量。
- `process-pending` 每批最多 200 条；近两日和历史各保留 100 条名额，空名额可互借。单表计算失败写入原重试状态后向调用方报告批次失败，已完成项不回滚。
- 补齐进度通过结构化日志 `能耗补齐进度` 输出月份、日期、来源、位置、扫描/入队/失败数、完成日；内部 URL 及安全请求头不变。
