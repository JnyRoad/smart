# 访客申请记录功能设计（records + record-detail）

日期：2026-06-12
状态：设计已经旅途批准（含详情接口鉴权修订）；本文档为落档 spec，待旅途审阅。
来源：原型新增功能（无旧版对照），mockup 见 docs/prototype/mockups/visitor/records.html、record-detail.html（已评审）。
背景决策（旅途拍板）：前端按真实 API 契约接入，同时保留显式本地演示用的配置式 mock 开关；合肥变体放到最后，本功能不含合肥分支。

## 1. 范围与路由

| 路由 | 内容 |
|---|---|
| `/visitor/records` | 我的申请记录：身份校验态（手机号+验证码，复用 SmsCodeField）→ 记录列表态（身份条 + 状态筛选 chips + 记录卡列表）/ 空态 |
| `/visitor/records/[applyId]` | 申请详情：状态 hero（含行动按钮）+ 审批进度时间线（动态节点数组）+ 申请信息卡 |

**三个入口全部打通**（原型要求）：
1. `/visitor` 首页表单下方常驻入口「已提交过申请？查看申请记录与审批进度」→ `/visitor/records`。
2. `/visitor/result` 增加「查看审批进度」按钮 → `/visitor/records`。
3. 短信/公众号深链直达详情：链接只带 `applyId`；无有效 queryToken 时详情页跳 `/visitor/records?redirect={applyId}`，验证身份后回跳。

## 2. 接口契约（前端先行自定 = 给后端的接口需求；platform 模块）

### 2.1 鉴权模型（防 IDOR，安全修订）

详情类接口**不允许**仅凭 applyId 免认证查询（否则可遍历 applyId 拉取访客隐私）。统一 token 流：

- `listMyApply` 验证通过后签发 **`queryToken`**（建议 30 分钟短时效），服务端绑定该手机号/openId 名下的申请单集合。
- `applyDetail` / `approvalProgress` 必须携带请求头 `X-Visitor-Query-Token`；服务端校验 token 有效 **且 applyId 属于绑定集合**，否则 403。
- openId 免验路径同样经 `listMyApply({openId})` 换 token，统一鉴权路径。
- 姓名/手机号/车牌的**脱敏由服务端完成**，前端不接触全量明文。

### 2.2 接口清单

| 接口 | Method | 入参 | 返回 |
|---|---|---|---|
| `/sms/send/getCode/{mobile}` | GET app | path mobile | `{code, message}` |
| `/admittance/apply/app/listMyApply` | POST | `{mobile, smsCode}` 或 `{openId}`，或持有效 token 时空体 + token 头（重进/下拉刷新复用） | `{code, data: {queryToken, maskedName, maskedMobile, records: RecordSummary[]}}` |
| `/admittance/apply/app/applyDetail` | GET | `?applyId=` + token 头 | `{code, data: ApplyRecordDetail}` |
| `/admittance/apply/app/approvalProgress` | GET | `?applyId=` + token 头 | `{code, data: {nodes: ApprovalNode[]}}` |

### 2.3 数据结构

```ts
type ApplyStatus = 'PENDING' | 'PASSED' | 'REJECTED' | 'EXPIRED' | 'REVOKED'
type DispatchStatus = 'SUCCESS' | 'ISSUING' | 'FAILED'   // 聚合逻辑后端做：任一设备成功→SUCCESS（业务已确认）；
                                                          // 全部失败→FAILED（业务已确认）；其余混合态→ISSUING（推断值，原型注明待业务确认）

interface RecordSummary {
  applyId: string
  parkName: string
  applyStatus: ApplyStatus
  receptionistName: string          // 脱敏
  startTime: string                 // 'YYYY-MM-DD HH:mm'
  endTime: string
  fellowCount: number
  plates: string[]                  // 车牌（可空数组）
  currentNode?: string              // PENDING 时：'部门负责人 张伟 审批中'
  dispatchStatus?: DispatchStatus   // PASSED 时返回
  submitTime: string
}

interface ApplyRecordDetail {
  applyId: string
  applyNo: string                   // 申请单号 VA20260610-0027
  parkName: string
  applyStatus: ApplyStatus
  dispatchStatus?: DispatchStatus
  receptionistName: string          // 脱敏
  startTime: string
  endTime: string
  cause: string
  visitorName: string               // 脱敏
  visitorPhone: string              // 脱敏
  fellows: { name: string; phone: string }[]   // 均脱敏
  vehicles: { plate: string; type?: string }[]
  areas: string[]                   // 授权区域名称
  submitTime: string
}

interface ApprovalNode {
  title: string                                  // '被访人审批' / '部门负责人审批'…（级数动态，不写死）
  state: 'done' | 'current' | 'wait' | 'rejected'
  approverName?: string                          // 仅脱敏姓名；部门/职务/等待时长/手机号不下发
  time?: string
  comment?: string                               // 审批意见（拒绝原因）
}
```

记录排序：提交时间倒序（服务端）。列表筛选 chips：全部/审批中/已通过/已拒绝/已过期（前端按 applyStatus 过滤；REVOKED 渲染灰色「已撤销」徽章，仅在「全部」出现）。

## 3. mock 开关（真实业务默认）

- `public/config.js` 与 `TenantConfig` 提供 `features: { visitorRecordsMock: boolean }`，默认 `false`，避免真实业务误走演示数据。
- `features/visitor/records-api.ts` 的列表/详情接口判断开关：开 → 返回 `records-mock.ts` fixture（约 300ms 延迟模拟加载）；关 → 真实请求。
- 短信验证码发送是业务副作用，复用访客申请现有 `app:/sms/send/getCode/{mobile}` 接口，不受 mock 开关短路。
- mock fixture 覆盖：列表含全部 5 种 applyStatus + 3 种 dispatchStatus 的记录；详情按 applyId 区分 6 个演示态（审批中 / 通过·下发成功 / 通过·下发中 / 通过·下发失败 / 已拒绝 / 已过期）。

## 4. 身份校验与 token 管理（前端）

- 进入 `/visitor/records`：sessionStorage 有未过期 queryToken → 直接 `listMyApply`（mock 下同构）；否则若 flow store 有 openId → 试 `listMyApply({openId})` 免验；都不行 → 验证态。
- `queryToken` + 脱敏身份存 **sessionStorage**（关页失效，不落 localStorage）；records-api 统一注入 `X-Visitor-Query-Token` 头；收到 401/403 → 清 token 回验证态。
- 列表态顶部身份条：「当前查询：李** 137****1234」+「换个手机号」（清 token 回验证态）。
- 详情深链：无 token → `/visitor/records?redirect={applyId}`，验证通过后 `router.replace('/visitor/records/' + applyId)`。

## 5. 详情页行为（按原型注释逐条）

- 审批节点按数组渲染，不写死级数；时间线状态样式 done（绿勾）/ current（橙点 + 「等待其审批中」）/ wait（灰）/ rejected（红叉 + 拒绝意见）。
- 状态 hero 文案与行动按钮：
  | 状态 | hero | 按钮 |
  |---|---|---|
  | PENDING | 审批中 · 当前停留在 X 处（共 N 节点已过 M） | 无（只读，无撤销/催办） |
  | PASSED + SUCCESS | 审批已通过 · 权限已下发 | 「查看入园通行码」（主）→ `/visitor/code?id=` |
  | PASSED + ISSUING | 审批已通过 · 权限下发中，请稍后刷新 | 「查看入园通行码」（次） |
  | PASSED + FAILED | 审批已通过 · 权限下发失败，请联系被访人或至门岗人工登记 | 无通行码按钮 |
  | REJECTED | 审批未通过（含拒绝人） | 「修改信息重新预约」→ `/visitor`（预填） |
  | EXPIRED | 已过期 | 「再次预约」→ `/visitor`（预填） |
- **重新预约**：detail 返回均为服务端脱敏值（姓名/手机号）或展示形态（区域名称、事由文案），无法可靠预填结构化草稿字段——预填脱敏值反而会产生脏数据。故「重新预约」清空草稿后跳 `/visitor`，用户重新填写（实现修订，2026-06-12 评审定论）。
- 刷新提示文案：「进度有更新时将通过短信/公众号通知您，也可下拉刷新查看最新进度」；列表与详情支持下拉刷新（antd-mobile PullToRefresh）。

## 6. 测试策略

- 单测：applyStatus/dispatchStatus → 徽章/文案映射纯函数；mock 开关分支（开→fixture，关→fetch）。
- E2E（**关 mock**、Playwright 网络层拦截，路径与真实后端同构）：
  1. 验证态 → 发码 → listMyApply 断言请求体 → 列表渲染 + 筛选 chips 过滤。
  2. 列表 → 详情：断言 applyDetail/approvalProgress 请求带 `X-Visitor-Query-Token` 头。
  3. 详情四态渲染（PENDING/PASSED+SUCCESS/REJECTED/EXPIRED）+ 通行码跳转 + 重新预约预填断言。
  4. 深链无 token → 重定向验证 → 回跳详情。
  5. 403 → 清 token 回验证态。
- mock 开关「开」的路径用 1 条冒烟 E2E 验证（显式开开关 → 短信发送仍请求真实接口 → 列表 fixture 可见）。

## 7. 分支与门禁

单分支 `feat/visitor-records`：check/test/e2e/build 全绿 → 独立子 agent 评审修复至无明确问题 → PR → 合并。入口改造（/visitor、/visitor/result 两处）随本分支。

## 8. 明确不做（YAGNI）

- 合肥变体（旅途明确放最后）。
- 撤销申请/催办（原型明确只读）。
- 下发失败的重试按钮（聚合状态只读，处理方式为线下联系）。
- queryToken 刷新机制（30 分钟过期后重新验证即可）。
