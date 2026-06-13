# 访客申请记录功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按已批准 spec（`../specs/2026-06-12-visitor-records-design.md`）交付访客申请记录 2 页 + 3 个入口，单分支 `feat/visitor-records`。

**Architecture:** 真实 API + 配置式 mock 开关（`features.visitorRecordsMock` 默认关，仅显式本地演示列表/详情）；短信发送始终真实请求；queryToken 鉴权流（sessionStorage，401/403 回验证态）；状态映射纯函数 + TDD；页面复用既有 SmsCodeField/PageShell/useMounted 模式。

**Tech Stack:** Next.js 16 App Router · antd-mobile 5 · TanStack Query · vitest · Playwright

**通用约定：** 每 Task 完成跑 `pnpm check && pnpm test`，页面 Task 加对应 E2E；E2E 默认**关 mock**（页面 `addInitScript` 覆写 `window.__SMART_CONFIG__.features.visitorRecordsMock=false`）走网络拦截。

---

### Task 1: 状态映射纯函数（TDD）

**Files:**
- Create: `src/features/visitor/record-status.ts`
- Test: `src/features/visitor/record-status.test.ts`

- [ ] **Step 1: 失败测试**

```ts
import { describe, expect, it } from 'vitest'
import { applyStatusBadge, dispatchStatusText } from './record-status'

describe('applyStatusBadge', () => {
  it('五种申请状态映射徽章文案与色调', () => {
    expect(applyStatusBadge('PENDING')).toEqual({ text: '审批中', tone: 'warning' })
    expect(applyStatusBadge('PASSED')).toEqual({ text: '已通过', tone: 'success' })
    expect(applyStatusBadge('REJECTED')).toEqual({ text: '已拒绝', tone: 'danger' })
    expect(applyStatusBadge('EXPIRED')).toEqual({ text: '已过期', tone: 'muted' })
    expect(applyStatusBadge('REVOKED')).toEqual({ text: '已撤销', tone: 'muted' })
  })
  it('未知状态回退原值灰调（快速暴露契约漂移）', () => {
    expect(applyStatusBadge('WHATEVER' as never)).toEqual({ text: 'WHATEVER', tone: 'muted' })
  })
})

describe('dispatchStatusText', () => {
  it('三种下发状态文案', () => {
    expect(dispatchStatusText('SUCCESS')).toEqual({ text: '已下发成功', tone: 'success' })
    expect(dispatchStatusText('ISSUING')).toEqual({ text: '正在下发', tone: 'warning' })
    expect(dispatchStatusText('FAILED')).toEqual({ text: '下发失败', tone: 'danger' })
  })
})
```

- [ ] **Step 2: 跑 `pnpm test -- record-status` 确认 FAIL（模块不存在）**
- [ ] **Step 3: 实现**

```ts
export type ApplyStatus = 'PENDING' | 'PASSED' | 'REJECTED' | 'EXPIRED' | 'REVOKED'
export type DispatchStatus = 'SUCCESS' | 'ISSUING' | 'FAILED'
export type Tone = 'success' | 'warning' | 'danger' | 'muted'

const APPLY_BADGES: Record<ApplyStatus, { text: string; tone: Tone }> = {
  PENDING: { text: '审批中', tone: 'warning' },
  PASSED: { text: '已通过', tone: 'success' },
  REJECTED: { text: '已拒绝', tone: 'danger' },
  EXPIRED: { text: '已过期', tone: 'muted' },
  REVOKED: { text: '已撤销', tone: 'muted' },
}

export function applyStatusBadge(status: ApplyStatus): { text: string; tone: Tone } {
  return APPLY_BADGES[status] ?? { text: String(status), tone: 'muted' }
}

const DISPATCH_TEXTS: Record<DispatchStatus, { text: string; tone: Tone }> = {
  SUCCESS: { text: '已下发成功', tone: 'success' },
  ISSUING: { text: '正在下发', tone: 'warning' },
  FAILED: { text: '下发失败', tone: 'danger' },
}

export function dispatchStatusText(status: DispatchStatus): { text: string; tone: Tone } {
  return DISPATCH_TEXTS[status] ?? { text: String(status), tone: 'muted' }
}
```

- [ ] **Step 4: PASS** → **Step 5: Commit** `feat(visitor): add record status mapping`

### Task 2: records API 层 + mock 开关（TDD）+ 配置扩展

**Files:**
- Create: `src/features/visitor/records-api.ts`、`src/features/visitor/records-mock.ts`
- Modify: `src/lib/config/tenant.ts`（加 `features`）、`public/config.js`
- Test: `src/features/visitor/records-api.test.ts`

- [ ] **Step 1: tenant.ts 扩展**：`TenantConfig` 加 `features: { visitorRecordsMock: boolean }`，DEFAULTS 中 `features: { visitorRecordsMock: false }`，`getTenantConfig` 合并 `features: { ...DEFAULTS.features, ...runtime.features }`；config.js 同步加字段。
- [ ] **Step 2: 失败测试**（vi.stubGlobal window config + vi.stubGlobal fetch）

```ts
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { fetchApplyDetail, fetchMyApplies, getQuerySession, saveQuerySession, clearQuerySession } from './records-api'

const fetchMock = vi.fn()
function setMockFlag(on: boolean) {
  window.__SMART_CONFIG__ = { features: { visitorRecordsMock: on } }
}

beforeEach(() => {
  sessionStorage.clear()
  fetchMock.mockReset()
  vi.stubGlobal('fetch', fetchMock)
})
afterEach(() => vi.unstubAllGlobals())

describe('mock 开关', () => {
  it('开关开：不发请求，返回 fixture 列表与 token', async () => {
    setMockFlag(true)
    const res = await fetchMyApplies({ mobile: '13700001234', smsCode: '123456' })
    expect(fetchMock).not.toHaveBeenCalled()
    expect(res.data?.queryToken).toBeTruthy()
    expect(res.data?.records.length).toBeGreaterThanOrEqual(5)
  })
  it('开关关：走真实请求并带 token 头', async () => {
    setMockFlag(false)
    saveQuerySession({ queryToken: 'tok-q', maskedName: '李**', maskedMobile: '137****1234' })
    fetchMock.mockResolvedValue(new Response(JSON.stringify({ code: 0, data: {} }), { status: 200 }))
    await fetchApplyDetail('a-1')
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit]
    expect(url).toContain('/platform/admittance/apply/app/applyDetail?applyId=a-1')
    expect((init.headers as Record<string, string>)['X-Visitor-Query-Token']).toBe('tok-q')
  })
})

describe('query session', () => {
  it('sessionStorage 存取与清除', () => {
    saveQuerySession({ queryToken: 't', maskedName: '李**', maskedMobile: '137****1234' })
    expect(getQuerySession()?.queryToken).toBe('t')
    clearQuerySession()
    expect(getQuerySession()).toBeNull()
  })
})
```

- [ ] **Step 3: FAIL** → **Step 4: 实现**

`records-mock.ts`：fixture 数据（列表 6 条覆盖 5 状态+3 下发态；`MOCK_DETAILS: Record<string, {detail, nodes}>` 6 个演示态；`mockDelay()` 300ms）。
`records-api.ts` 核心：

```ts
import { request } from '@/lib/api/http'
import { getTenantConfig } from '@/lib/config/tenant'
import type { ApplyStatus, DispatchStatus } from './record-status'
import { MOCK_LIST, MOCK_DETAILS, MOCK_NODES, mockDelay } from './records-mock'

export interface RecordSummary { applyId: string; parkName: string; applyStatus: ApplyStatus; receptionistName: string; startTime: string; endTime: string; fellowCount: number; plates: string[]; currentNode?: string; dispatchStatus?: DispatchStatus; submitTime: string }
export interface ApplyRecordDetail { applyId: string; applyNo: string; parkName: string; applyStatus: ApplyStatus; dispatchStatus?: DispatchStatus; receptionistName: string; startTime: string; endTime: string; cause: string; visitorName: string; visitorPhone: string; fellows: { name: string; phone: string }[]; vehicles: { plate: string; type?: string }[]; areas: string[]; submitTime: string }
export interface ApprovalNode { title: string; state: 'done' | 'current' | 'wait' | 'rejected'; approverName?: string; time?: string; comment?: string }
export interface QuerySession { queryToken: string; maskedName: string; maskedMobile: string }
interface Envelope<T> { code: number; data?: T; message?: string }

const SESSION_KEY = 'visitor-query-session'
export function saveQuerySession(s: QuerySession): void { sessionStorage.setItem(SESSION_KEY, JSON.stringify(s)) }
export function getQuerySession(): QuerySession | null { const raw = sessionStorage.getItem(SESSION_KEY); if (!raw) return null; try { return JSON.parse(raw) as QuerySession } catch { return null } }
export function clearQuerySession(): void { sessionStorage.removeItem(SESSION_KEY) }

const useMock = () => getTenantConfig().features.visitorRecordsMock
const tokenHeaders = () => { const t = getQuerySession()?.queryToken; return t ? { 'X-Visitor-Query-Token': t } : undefined }
// http.ts 的 RequestOptions 需要支持自定义 headers（见 Step 4b）

export function sendRecordSms(mobile: string): Promise<Envelope<unknown>>           // 复用访客申请短信 GET，不受 mock 短路
export function fetchMyApplies(input: { mobile?: string; smsCode?: string; openId?: string } | null): Promise<Envelope<{ queryToken: string; maskedName: string; maskedMobile: string; records: RecordSummary[] }>>
  // mock: 任意入参成功，token 'mock-query-token'，mobile 脱敏由 fixture 给；input null = 用既有 token 头刷新
export function fetchApplyDetail(applyId: string): Promise<Envelope<ApplyRecordDetail>>      // GET + token 头
export function fetchApprovalProgress(applyId: string): Promise<Envelope<{ nodes: ApprovalNode[] }>>  // GET + token 头
```

- [ ] **Step 4b: http.ts 加 `headers?: Record<string,string>` 选项**（合并进请求头，调用方传入的优先级低于内置 Authorization；加一条单测：自定义头透传）。
- [ ] **Step 5: PASS（全部单测）** → **Step 6: Commit** `feat(visitor): add records api with mock switch`

### Task 3: 记录列表页 `/visitor/records`

**Files:**
- Create: `src/app/visitor/records/page.tsx`
- Test: `e2e/visitor-records.spec.ts`（新建）

- [ ] **Step 1: 实现页面**。行为契约（spec §1/§4）：
  - 状态机：`mounted` 后——有 session → `fetchMyApplies(null)`（token 刷新形态）；无 session 且 flow store 有 openId → `fetchMyApplies({openId})`；否则验证态。任何 401/403/`code!==0` → `clearQuerySession()` 落验证态（toast message）。
  - 验证态：hero「验证身份后查看记录」+ 副文案 + SmsCodeField（onSend=sendRecordSms）+「查看申请记录」主按钮 → `fetchMyApplies({mobile, smsCode})` → 成功 `saveQuerySession` + 渲染列表；`?redirect=` 存在 → `router.replace('/visitor/records/'+redirect)`。
  - 列表态：身份条（`当前查询：{maskedName} {maskedMobile}` +「换个手机号」清 session 回验证态）；筛选 chips 全部/审批中/已通过/已拒绝/已过期（前端过滤 applyStatus，REVOKED 仅「全部」可见）；记录卡（园区名 + 状态徽章 / 被访人 / 来访时间 `start ~ end(HH:mm)` / 随行/车辆 `随行 N 人 · 车牌或无车辆` / PENDING 显示 `当前节点：{currentNode}`、PASSED 显示 `通行权限：{dispatchStatusText}` / 右下「查看进度/详情」）→ 点击 `router.push('/visitor/records/'+applyId)`；PullToRefresh 重拉列表。
  - 空态：ErrorBlock「暂无申请记录」+「去预约」按钮 → `/visitor`。
  - 顶部 PageShell「我的申请记录」，返回 → `/visitor`。
- [ ] **Step 2: E2E（关 mock）**：验证流（断言 sendRecordSms 复用访客申请短信 GET、listMyApply 请求体）→ 列表渲染（状态徽章/下发态文案）→ chips 过滤数量变化 → 「换个手机号」回验证态；403 响应 → 回验证态。mock 开冒烟：显式开 mock → sendRecordSms 仍真实请求 → 列表 fixture 可见。
- [ ] **Step 3: 全绿 Commit** `feat(visitor): add my-applies records page`

### Task 4: 详情页 `/visitor/records/[applyId]`

**Files:**
- Create: `src/app/visitor/records/[applyId]/page.tsx`
- Test: `e2e/visitor-records.spec.ts` 追加

- [ ] **Step 1: 实现**。行为契约（spec §5）：
  - 无 session → `router.replace('/visitor/records?redirect='+applyId)`。
  - 并行 `fetchApplyDetail` + `fetchApprovalProgress`（TanStack Query，token 头由 api 层注入）；401/403 → 清 session 同上跳转。
  - 状态 hero（applyStatus×dispatchStatus 六态文案与按钮，按 spec §5 表）；时间线（nodes 数组渲染 done绿/current橙+「等待其审批中」/wait灰/rejected红+comment）；申请信息卡（applyNo/parkName/被访人/时间/事由/访客/随行 chips/车辆 chips/区域 chips/提交时间，空值隐藏行）；刷新提示文案 + PullToRefresh（refetch 两查询）。
  - 「查看入园通行码」→ `router.push('/visitor/code?id='+applyId)`；「修改信息重新预约/再次预约」→ 预填 flow store（patchHost receptionistName、patchVisitor company/cause 可得字段）→ `router.push('/visitor')`。
- [ ] **Step 2: E2E**：详情四态渲染断言（hero 文案/按钮显隐/时间线节点状态/拒绝意见）；断言两接口请求带 `X-Visitor-Query-Token` 头；通行码跳转；重新预约预填断言（localStorage visitor-flow 含被访人姓名）；深链无 session → redirect 验证 → 回跳详情。
- [ ] **Step 3: 全绿 Commit** `feat(visitor): add apply record detail page`

### Task 5: 三入口接线

**Files:**
- Modify: `src/app/visitor/page.tsx`（表单卡片下方加入口行）、`src/app/visitor/result/page.tsx`（加「查看审批进度」按钮）

- [ ] **Step 1: /visitor 入口**：主按钮下方加文字链接行「已提交过申请？查看申请记录与审批进度 ›」→ `/visitor/records`（无 code 也可达？入口在表单卡内，页面本身有 OAuth gate——保持现状：入口随表单渲染）。
- [ ] **Step 2: /visitor/result**：在「再预约一次」上方加主按钮「查看审批进度」→ `/visitor/records`。
- [ ] **Step 3: E2E 追加**：result 页按钮跳转；visitor 入口链接跳转（复用既有 entry mocks）。
- [ ] **Step 4: 全绿 Commit** `feat(visitor): wire records entries on entry and result pages`

### Task 6: 分支收尾

- [ ] `pnpm check && pnpm test && pnpm e2e && pnpm build` 全绿
- [ ] 子 agent 只读评审本分支 diff（重点：token 流安全语义、mock/真实路径同构性、状态机分支、E2E 强度）→ 修复 → 复评至无明确问题
- [ ] PR `feat/visitor-records → main`（英文，Risks 注明后端契约为前端先行自定 + mock 默认关，仅显式本地演示开启）→ 合并
- [ ] 更新 README 已实现段 + 记忆文件 → 向旅途汇报

---

## Self-Review 记录

- Spec 覆盖：§1 路由/入口 ↔ Task 3/4/5；§2 契约+鉴权 ↔ Task 2（含 http headers 扩展）；§3 mock 开关 ↔ Task 2；§4 token 管理 ↔ Task 2/3/4；§5 详情行为 ↔ Task 4；§6 测试 ↔ 各 Task + Task 1 纯函数；§7 门禁 ↔ Task 6。无缺口。
- 类型一致性：ApplyStatus/DispatchStatus 定义于 Task 1，Task 2 引用；QuerySession/RecordSummary/ApplyRecordDetail/ApprovalNode 定义于 Task 2，页面引用。
- 占位符扫描：通过（页面 Task 为行为契约 + 关键状态机描述，复用已验证的既有组件模式）。
