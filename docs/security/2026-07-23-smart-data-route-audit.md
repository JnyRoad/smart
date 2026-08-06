# SmartData 174 路由人工审计证据（2026-07-23）

## 结论与边界

- 证据基线：`2026-07-22-service-route-inventory.md` 中 `smart-data` 的 135 条控制器路由，以及同一轮库存合计 174 条服务路由；库存逐行的路径、控制器文件和静态注解是本表的逐路由索引。
- 本文不连接生产 Nacos、不请求真实员工数据。生产网关、Nacos `yuto_prod/dev` 生效配置、OAuth client/scope 与旧 URL 流量均为 **UNVERIFIED**，必须按本文探针清单上线核验。
- `REVIEW_REQUIRED` 是库存生成器的保守静态状态：它不会把 `@Inner`、`@OpenApi("server")`、Feign `FROM_IN` 和 service-token 当成已在生产生效的事实；因此不等于“仍可外网访问”。

## SmartData 逐控制器/路由证据表

| 路由/控制器组（完整逐路径索引） | 数据与风险 | 源码调用/授权结论 | 处置与生产探针 |
| --- | --- | --- | --- |
| 全部 EHR view 控制器：`ehrview/*Controller`（库存中 `internal` 的 92 条） | 工号、姓名、组织、考勤、请假、补卡、补贴等人员资料 | `@Inner + @OpenApi(server)`；Feign 明确 `FROM` 和 `INTERNAL_SERVICE_AUTH`；真实 App/Platform 调用迁服务令牌 | 普通用户/错误 client/错误 scope 必须 403；受管 server client 2xx；跨工号入口由上游本人/园区校验 |
| `YutoDhrPsndoController`：`/empdhr/ys/internal/page`、`internal/properties`、`internal/badge/{userId}` | 分页含证件、手机号、邮箱；工号/用户 ID 可枚举 | 仅 Smart App 服务端调用，已迁内部路径和服务令牌 | `3e868c46`；旧路径 404/403，登录和个人资料流程成功 |
| temporary：`Ocompany`、`Ebg*` 五条 save | 组织、教育、家属、回避关系、履历 | 仅 Platform 同步；内部 token | `baed266c`；验证入职同步、无 token 拒绝 |
| temporary：`EPhoto`、`EstaffRegister`、`EleaveJjitem` 三类写入 | 人脸、完整身份/联系方式/银行档案、离职交接与金额 | 仅 Platform 同步；内部 token | `0e956fd5`；验证人脸、入职、离职流程；旧 URL 拒绝 |
| `EmailManagerController` 四路、`SmsManageController` 十三路 | 任意邮件附件、手机号、短信正文/验证码 | 仅 App/Platform 服务端；Feign 默认门面固定服务令牌 | `20648348`；邮件/短信业务入口仍在上游做频控/本人校验；Data 直达拒绝 |
| `SmsManageController` attendance/wage/articlesrelease/msg | GET+body 可被代理/缓存误处理 | 全部改精确内部 POST | `61824bd0`；旧 GET 返回 404/405，现有业务 POST 成功 |
| `OaDataManageController` staff info | 上游返回曾含 PASSWORD 等过度字段 | 仅物品放行调用；响应最小化为 id/name，原始响应不记日志 | `6c344f61`；验证放行流程，日志无密码/原始响应 |
| `OaDataManageController` 其余 `/oarmanage/send/**` | 创建、撤销 OA 人事/审批流程 | 已由内部 token 路由保护；上游 Platform 负责本人、园区、状态机 | 对每类流程验证错误身份/园区拒绝、正确服务调用成功 |
| `VcallCarController` `/vcallcar/internal/page` | 车牌、司机姓名/手机号、供应商 | 无源码外部消费者；仅内部 server token | `55a323b2`；旧 `/page` 拒绝，确认无生产遗留调用 |
| 剩余 `external-authenticated`：`eapprais/info`、`getBlackInfo`、`evw*/*info` 等库存逐行所列端点 | 多为历史只读兼容端点；部分当前返回空或无源码调用 | 未因“有 token”视为安全；需逐一确认仍有外部产品语义，否则迁内部或删除 | 发布前抓取网关访问日志；任意 badge/分页探针不得泄露他人数据 |

## 135 条 `REVIEW_REQUIRED` 的解释

1. 生成器只观察本地源码和 Nacos 模板，不能证明生产网关没有旧路由、覆盖配置或直连服务。
2. 内部路由仍要验证 service client id、`server` scope、`FROM_IN`、`OpenApiInterceptor` 和实际 Feign token 链路同时生效。
3. 外部认证路由仍要验证业务层本人/园区/角色约束；认证本身不是数据最小化授权。

## 发布前统一探针

1. 无 token、用户 token、错误 client、错误 scope、缺 service header 分别访问每个 `internal` 路由，均应拒绝。
2. 正确受管 server client 仅验证需要的 Platform/App 流程，记录 2xx、403、404、Feign 失败率。
3. 在网关访问日志中搜索旧路径和工号查询；流量归零后保留拒绝规则并移除遗留调用。
4. 响应和日志抽样：身份证、手机号、邮箱、密码、驾驶员电话、人脸/照片 URL 均不得出现在未授权响应或日志。
