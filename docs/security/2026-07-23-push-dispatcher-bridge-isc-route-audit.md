# Push、Dispatcher、Bridge、ISC 路由复核与发布门禁

## 范围和证据边界

本次仅核查本地源码及 `docker/nacos/config/dev` 模板。`yuto_prod/dev` 的实际 Data ID、OAuth client、网关暴露和历史调用流量均为 **UNVERIFIED**；本文不是生产已生效的声明。

## 已修复的共性风险

- Push 的 `/notice`、`/transmission` 已收紧为 Platform 专属纯服务令牌，要求 `from=Y`；`security.inner.mode=ENFORCE` 且 client_id 空值默认拒绝。
- 未发现 `/push/pushAll` 的源码调用方，路径保留为明确 403 拒绝，不能由任意 `server` scope token 触发广播。
- Platform 推送调用日志不再记录 device token、标题、正文或 payload；ISC 事件原文、人员编号、工号、图片地址不再写入诊断日志。
- Dispatcher、Bridge、ISC Bridge 的出站 Feign 均使用 `INTERNAL_SERVICE_AUTH_REQUIRED`；本地模板已配置独立 `security.inner.service-token`，缺失环境变量时 fail-closed。

## 14 条路由审计矩阵

| 服务 | 路由 | 本地入站控制 | 已知源码调用 | 当前处置 |
| --- | --- | --- | --- | --- |
| Push | `/push/notice` | `@Inner`、`server` scope、Platform client、`from=Y` | Platform Android 单播 | 本地通过；生产待注入 client |
| Push | `/push/transmission` | 同上 | Platform iOS 单播 | 本地通过；生产待注入 client |
| Push | `/push/pushAll` | 显式拒绝 | 未发现 | 关闭，待专属调用方审批 |
| Dispatcher | `/dispatcher/dispatch` | `@Inner`、`server` scope | Platform/动态 Bridge | 生产精确 client 待核验 |
| Dispatcher | `/dispatcher/handle` | `@Inner`、`server` scope | Bridge、ISC Bridge | 生产精确 client 待核验 |
| Dispatcher | `/dispatcher/image` | `@Inner`、`server` scope | Bridge 调图 | 图像响应按敏感数据处理 |
| Dispatcher | `/dispatcher/thumbnail` | `@Inner`、`server` scope | Bridge 缩略图 | 图像响应按敏感数据处理 |
| Bridge | `/bridge/dispatch` | `@Inner`、`server` scope | Dispatcher 动态 Feign | 生产精确 client 待核验 |
| Bridge | `/bridge/image` | `@Inner`、`server` scope | Dispatcher | 图像响应按敏感数据处理 |
| Bridge | `/bridge/thumbnail` | `@Inner`、`server` scope | Dispatcher | 图像响应按敏感数据处理 |
| ISC Bridge | `/bridge/dispatch` | `@Inner`、`server` scope | Dispatcher 动态 Feign | 生产精确 client 待核验 |
| ISC Bridge | `/bridge/handle` | `@Inner`、`server` scope | 无内部 Feign；旧厂商回调 | 默认不订阅，禁止公网启用 |
| ISC Bridge | `/bridge/image` | `@Inner`、`server` scope | Dispatcher | 图像响应按敏感数据处理 |
| ISC Bridge | `/bridge/thumbnail` | `@Inner`、`server` scope | Dispatcher | 图像响应按敏感数据处理 |

## 生产发布硬门禁

1. 在目标 Nacos Data ID 注入独立、最小权限的 `SMART_PUSH_PLATFORM_CLIENT_ID`、Dispatcher、Bridge、ISC Bridge 服务令牌三元组；不得使用通用 OAuth fallback。
2. 对每条仍为通用 `@OpenApi("server")` 的 Dispatcher/Bridge 路由，先由访问日志确定真实 client_id，再增加路由级 allowlist；未完成前不得宣称已精确收口。
3. 核对实际 `ignore-urls=[]`、Push `inner.mode=ENFORCE`，并确认服务端口仅通过受控网关/内网 ACL 可达；生产公网暴露状态为 UNVERIFIED。
4. 只在预发/沙箱设备执行合法单播；匿名、用户 token、错误 scope、错误 client、错误 `from` 必须为 401/403。禁止对 `pushAll` 做真实群发探针。
5. ISC 厂商回调仅在完成签名、时间窗、nonce/replay 防护并经安全评审后，才可将 `event-subscribe-enabled` 改为 true；当前必须保持 false。
