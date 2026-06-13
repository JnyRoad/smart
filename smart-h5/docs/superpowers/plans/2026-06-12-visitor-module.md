# 访客模块（标准/许昌流程）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按已批准 spec（`../specs/2026-06-12-visitor-module-design.md`）交付访客标准流程 12 页，3 个分支依次合并。

**Architecture:** 免登录访客流程；Zustand+persist 单 store 承载跨页草稿（OAuth 整页跳转后存活）；接口全部 `auth:'none'` 走现有 `lib/api/http.ts` 兼容层；许昌参数取租户配置，合肥分支只留口子。

**Tech Stack:** Next.js 16 App Router · antd-mobile 5 · Zustand persist · vitest · Playwright（网络层 mock）

**通用约定：**
- 仓库根即应用根；每个 Task 完成跑 `pnpm check && pnpm test`，页面类 Task 加 `pnpm e2e`。
- 每个分支收尾：全绿 → 子 agent 评审修复 → PR → 合并（自动删分支）。
- E2E 统一用 `page.route` mock 网关；微信 OAuth 用 `open.weixin.qq.com` stub（已有模式见 `e2e/auth.spec.ts`）。
- 视觉对齐 `docs/prototype/mockups/visitor/*.html`（设计令牌见 DESIGN-GUIDE.md，工程里已有同款 Tailwind 用法）。

---

## 分支 1：feat/visitor-flow（主流程 4 页 + 基础设施）

### Task 1.1: 身份证校验纯函数（TDD）

**Files:**
- Create: `src/features/visitor/id-card.ts`
- Test: `src/features/visitor/id-card.test.ts`

- [ ] **Step 1: 写失败测试**

```ts
import { describe, expect, it } from 'vitest'
import { validateIdCard } from './id-card'

describe('validateIdCard', () => {
  it('接受合法 18 位身份证（含 X 校验位）', () => {
    expect(validateIdCard('11010519491231002X').ok).toBe(true)
    expect(validateIdCard('110105194912310026').ok).toBe(false) // 校验位错
  })
  it('拒绝格式错误', () => {
    expect(validateIdCard('123')).toEqual({ ok: false, message: '证件号码格式不正确' })
    expect(validateIdCard('11010519491231002a').ok).toBe(false)
  })
  it('拒绝校验位错误并给出原因', () => {
    const r = validateIdCard('110105194912310021')
    expect(r).toEqual({ ok: false, message: '证件号码校验位不正确' })
  })
})
```

- [ ] **Step 2: 跑测试确认 FAIL**（模块不存在）：`pnpm test -- id-card`
- [ ] **Step 3: 最小实现**

```ts
const WEIGHTS = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
const CHECK_CODES = '10X98765432'

export type IdCardResult = { ok: true } | { ok: false; message: string }

/** 18 位身份证校验：格式（17 数字 + 数字/X）+ ISO 7064 校验位。 */
export function validateIdCard(certNo: string): IdCardResult {
  if (!/^\d{17}[\dXx]$/.test(certNo)) return { ok: false, message: '证件号码格式不正确' }
  const sum = WEIGHTS.reduce((acc, w, i) => acc + w * Number(certNo[i]), 0)
  const expected = CHECK_CODES[sum % 11]
  if (certNo[17]?.toUpperCase() !== expected) {
    return { ok: false, message: '证件号码校验位不正确' }
  }
  return { ok: true }
}
```

- [ ] **Step 4: 跑测试确认 PASS**
- [ ] **Step 5: Commit** `feat(visitor): add id-card validation`

### Task 1.2: 流程 store（TDD）

**Files:**
- Create: `src/features/visitor/flow-store.ts`
- Test: `src/features/visitor/flow-store.test.ts`

- [ ] **Step 1: 失败测试**（happy-dom 自带 localStorage）

```ts
import { beforeEach, describe, expect, it } from 'vitest'
import { useVisitorFlow } from './flow-store'

beforeEach(() => {
  localStorage.clear()
  useVisitorFlow.getState().reset()
})

describe('visitor flow store', () => {
  it('patch 与增删改', () => {
    const s = useVisitorFlow.getState()
    s.patchHost({ receptionistName: '赵经理' })
    s.patchVisitor({ visitorName: '王五' })
    s.addFellow({ fellowName: '李四', fellowPhotoId: 'p1', certNo: '11010519491231002X' })
    s.updateFellow(0, { fellowName: '李四四', fellowPhotoId: 'p1', certNo: '11010519491231002X' })
    s.addCar({ plate: '豫A12345', name: '王五', certType: { code: 2, desc: '身份证复印件' }, certImg: 'img1' })
    s.removeCar(0)
    s.setFactoryAreas('NEW01', { list: ['A1'], custom: '三楼会议室' })
    const cur = useVisitorFlow.getState()
    expect(cur.host.receptionistName).toBe('赵经理')
    expect(cur.fellows[0]?.fellowName).toBe('李四四')
    expect(cur.cars).toHaveLength(0)
    expect(cur.areasByFactory['NEW01']?.list).toEqual(['A1'])
  })
  it('persist 到 localStorage（key=visitor-flow），reset 清空', () => {
    useVisitorFlow.getState().patchVisitor({ visitorName: '王五' })
    expect(localStorage.getItem('visitor-flow')).toContain('王五')
    useVisitorFlow.getState().reset()
    expect(useVisitorFlow.getState().visitor.visitorName).toBe('')
  })
})
```

- [ ] **Step 2: FAIL** → **Step 3: 实现**

```ts
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface HostInfo {
  openId?: string
  unionId?: string
  receptionistBadge?: string
  receptionistName?: string
  receptionistPhone?: string
}
export interface CauseOption { code: string | number; desc: string }
export interface VisitorBase {
  visitorName: string
  visitorPhotoId: string
  certNo: string
  company: string
  cause?: CauseOption
  startTime: string
  endTime: string
  permitFactoryType?: string
}
export interface AreaSelection { list: string[]; custom: string }
export interface FellowPerson { fellowName: string; fellowPhotoId: string; certNo: string }
export interface VisitorCar {
  plate: string
  name: string
  certType: CauseOption
  certImg: string
}

const EMPTY_VISITOR: VisitorBase = {
  visitorName: '', visitorPhotoId: '', certNo: '', company: '', startTime: '', endTime: '',
}

interface VisitorFlowState {
  host: HostInfo
  visitor: VisitorBase
  areasByFactory: Record<string, AreaSelection>
  fellows: FellowPerson[]
  cars: VisitorCar[]
  phone: string
  patchHost: (p: Partial<HostInfo>) => void
  patchVisitor: (p: Partial<VisitorBase>) => void
  setFactoryAreas: (factoryType: string, sel: AreaSelection) => void
  replaceAreas: (areas: Record<string, AreaSelection>) => void
  addFellow: (f: FellowPerson) => void
  updateFellow: (i: number, f: FellowPerson) => void
  removeFellow: (i: number) => void
  addCar: (c: VisitorCar) => void
  updateCar: (i: number, c: VisitorCar) => void
  removeCar: (i: number) => void
  setPhone: (p: string) => void
  reset: () => void
}

/** 跨页访客草稿。必须持久化：/visitor 入口的微信 OAuth 是整页跳转。 */
export const useVisitorFlow = create<VisitorFlowState>()(
  persist(
    (set) => ({
      host: {}, visitor: EMPTY_VISITOR, areasByFactory: {}, fellows: [], cars: [], phone: '',
      patchHost: (p) => set((s) => ({ host: { ...s.host, ...p } })),
      patchVisitor: (p) => set((s) => ({ visitor: { ...s.visitor, ...p } })),
      setFactoryAreas: (t, sel) => set((s) => ({ areasByFactory: { ...s.areasByFactory, [t]: sel } })),
      replaceAreas: (areas) => set({ areasByFactory: areas }),
      addFellow: (f) => set((s) => ({ fellows: [...s.fellows, f] })),
      updateFellow: (i, f) => set((s) => ({ fellows: s.fellows.map((x, n) => (n === i ? f : x)) })),
      removeFellow: (i) => set((s) => ({ fellows: s.fellows.filter((_, n) => n !== i) })),
      addCar: (c) => set((s) => ({ cars: [...s.cars, c] })),
      updateCar: (i, c) => set((s) => ({ cars: s.cars.map((x, n) => (n === i ? c : x)) })),
      removeCar: (i) => set((s) => ({ cars: s.cars.filter((_, n) => n !== i) })),
      setPhone: (phone) => set({ phone }),
      reset: () => set({ host: {}, visitor: EMPTY_VISITOR, areasByFactory: {}, fellows: [], cars: [], phone: '' }),
    }),
    { name: 'visitor-flow' },
  ),
)
```

- [ ] **Step 4: PASS** → **Step 5: Commit** `feat(visitor): add persisted flow store`

### Task 1.3: 访客 API 层

**Files:**
- Create: `src/features/visitor/api.ts`

- [ ] **Step 1: 按 spec §3 实现全部接口函数**（全部 `auth:'none'`；信封 `{code, data?, message?}`；类型字段对照规格可选宽松）。核心签名：

```ts
import { request } from '@/lib/api/http'

interface Envelope<T> { code: number; data?: T; message?: string }
export interface EnumItem { code: string | number; desc: string }

export const getAdmittanceNotice = (parkId: number) =>
  request<Envelope<{ isNeedNotice?: number; noticeContent?: string }>>({
    module: 'platform', url: '/common/config/admittance/notice', params: { parkId }, auth: 'none' })

export const getVisitorOpenId = (code: string) =>
  request<Envelope<{ openId?: string; unionId?: string }>>({
    module: 'platform', url: '/admittance/apply/get/openId', params: { code }, auth: 'none' })

export const searchReceptionist = (p: { parkId: number; receptionistName: string; receptionistPhone: string }) =>
  request<Envelope<{ receptionistBadge?: string; receptionistName?: string; receptionistPhone?: string }>>({
    module: 'platform', url: '/admittance/apply/app/searchReceptionist', method: 'POST', data: p, auth: 'none' })

export const getPersonCertEnum = () => request<Envelope<EnumItem[]>>({ module: 'platform', url: '/admittance/apply/enum/person/cert', auth: 'none' })
export const getCauseEnum = () => request<Envelope<EnumItem[]>>({ module: 'platform', url: '/admittance/apply/enum/cause', auth: 'none' })
export const getVehicleCertEnum = () => request<Envelope<EnumItem[]>>({ module: 'platform', url: '/admittance/apply/enum/vehicle/cert', auth: 'none' })
export const getTruckCauseEnum = () => request<Envelope<EnumItem[]>>({ module: 'platform', url: '/admittance/apply/enum/car/cause', auth: 'none' })
export const getAreaTypeList = () => request<Envelope<EnumItem[]>>({ module: 'platform', url: '/admittance/area/type/list', params: { type: 2 }, auth: 'none' })

export interface FactoryAreaConfig { factoryType?: string; factoryName?: string; areaFlag?: number; inlineAreaLimit?: number; areas?: { code: string; name: string; isCommon?: number }[] }
export const getAreaOptions = (parkId: number) =>
  request<Envelope<FactoryAreaConfig[]>>({ module: 'platform', url: '/admittance/apply/app/area-options', params: { parkId }, auth: 'none' })
export const getFactoryTypeEnum = (flag: 0 | 1) =>
  request<Envelope<EnumItem[]>>({ module: 'platform', url: '/admittance/apply/enum/factory/type', params: { flag }, auth: 'none' })

export const checkApplyEqual = (data: Record<string, unknown>) =>
  request<Envelope<unknown>>({ module: 'platform', url: '/admittance/apply/equal/check', method: 'POST', data, auth: 'none' })

export const sendVisitorSms = (mobile: string) => request<Envelope<unknown>>({ module: 'app', url: `/sms/send/getCode/${mobile}`, auth: 'none' })
export const verifyVisitorSms = (mobile: string, smsCode: string) =>
  request<Envelope<unknown>>({ module: 'app', url: '/sms/verify', params: { mobile, smsCode }, auth: 'none' })

export const checkBlackVisitor = (data: { visitorName: string; certNo: string; parkId: number }) =>
  request<Envelope<boolean>>({ module: 'app', url: '/wechat/visit/checkBlackVisitor', method: 'POST', data, auth: 'none' })

export const saveVisitorApply = (data: Record<string, unknown>) =>
  request<Envelope<unknown>>({ module: 'platform', url: '/admittance/apply/save/apply', method: 'POST', data, auth: 'none' })
export const saveTruckApply = (data: Record<string, unknown>) =>
  request<Envelope<unknown>>({ module: 'platform', url: '/admittance/apply/save/car/apply', method: 'POST', data, auth: 'none' })

export const getApplyDetail = (id: string) =>
  request<Envelope<Record<string, unknown>>>({ module: 'platform', url: `/admittance/apply/search/Detail/${id}`, auth: 'none' })

export const faceCut = (imageData: string) =>
  request<Envelope<{ imageData?: string }>>({ module: 'algorithm', url: '/out/face/cut', method: 'POST', data: { imageData }, auth: 'none' })
export const checkFace = (visitorPhoto: string) =>
  request<Envelope<string | number>>({ module: 'app', url: '/wechat/visit/checkFace', method: 'POST', data: { visitorPhoto }, auth: 'none' })
```

- [ ] **Step 2: `pnpm check` 通过** → **Step 3: Commit** `feat(visitor): add visitor api service layer`

### Task 1.4: 区域配置加载链 + 剪除（TDD）

**Files:**
- Create: `src/features/visitor/area-options.ts`
- Test: `src/features/visitor/area-options.test.ts`

- [ ] **Step 1: 失败测试**（vi.mock `./api`）

```ts
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as api from './api'
import { loadAreaOptions, pruneSelectedAreas } from './area-options'

vi.mock('./api')
const mocked = vi.mocked(api)

beforeEach(() => { localStorage.clear(); vi.resetAllMocks() })

const CONFIG = [
  { factoryType: 'NEW01', factoryName: '新工厂', areaFlag: 1, areas: [{ code: 'A1', name: '办公区' }, { code: 'A2', name: '生产区' }] },
]

describe('loadAreaOptions', () => {
  it('新接口成功：返回配置并写缓存', async () => {
    mocked.getAreaOptions.mockResolvedValue({ code: 0, data: CONFIG })
    const r = await loadAreaOptions(5000021)
    expect(r[0]?.factoryName).toBe('新工厂')
    expect(localStorage.getItem('visitor-area-options-5000021')).toContain('NEW01')
  })
  it('新接口失败：先用缓存', async () => {
    localStorage.setItem('visitor-area-options-5000021', JSON.stringify(CONFIG))
    mocked.getAreaOptions.mockRejectedValue(new Error('boom'))
    const r = await loadAreaOptions(5000021)
    expect(r[0]?.factoryType).toBe('NEW01')
  })
  it('新接口失败且无缓存：回退旧枚举组装新/老工厂', async () => {
    mocked.getAreaOptions.mockRejectedValue(new Error('boom'))
    mocked.getFactoryTypeEnum.mockImplementation(async (flag) => ({
      code: 0, data: [{ code: `F${flag}`, desc: flag === 1 ? '新区域' : '老区域' }],
    }))
    const r = await loadAreaOptions(5000021)
    expect(r).toHaveLength(2)
    expect(r.find((f) => f.areaFlag === 1)?.areas?.[0]?.code).toBe('F1')
  })
})

describe('pruneSelectedAreas', () => {
  it('剪除配置中已不存在的区域码', () => {
    const pruned = pruneSelectedAreas(
      { NEW01: { list: ['A1', 'DEAD'], custom: 'x' }, GONE: { list: ['Z1'], custom: '' } },
      CONFIG,
    )
    expect(pruned).toEqual({ NEW01: { list: ['A1'], custom: 'x' } })
  })
})
```

- [ ] **Step 2: FAIL** → **Step 3: 实现**

```ts
import { getAreaOptions, getFactoryTypeEnum, type FactoryAreaConfig } from './api'
import type { AreaSelection } from './flow-store'

const cacheKey = (parkId: number) => `visitor-area-options-${parkId}`

/** 区域配置加载链：新接口 → localStorage 缓存 → 旧枚举回退（组装新/老工厂）。 */
export async function loadAreaOptions(parkId: number): Promise<FactoryAreaConfig[]> {
  try {
    const res = await getAreaOptions(parkId)
    if (res.code === 0 && Array.isArray(res.data) && res.data.length > 0) {
      localStorage.setItem(cacheKey(parkId), JSON.stringify(res.data))
      return res.data
    }
  } catch {
    // fall through to cache / legacy fallback
  }
  const cached = localStorage.getItem(cacheKey(parkId))
  if (cached) {
    try { return JSON.parse(cached) as FactoryAreaConfig[] } catch { localStorage.removeItem(cacheKey(parkId)) }
  }
  const [newRes, oldRes] = await Promise.all([getFactoryTypeEnum(1), getFactoryTypeEnum(0)])
  const toAreas = (items?: { code: string | number; desc: string }[]) =>
    (items ?? []).map((i) => ({ code: String(i.code), name: i.desc }))
  return [
    { factoryType: 'LEGACY_NEW', factoryName: '新工厂', areaFlag: 1, areas: toAreas(newRes.data) },
    { factoryType: 'LEGACY_OLD', factoryName: '老工厂', areaFlag: 0, areas: toAreas(oldRes.data) },
  ]
}

/** 提交前复核：剪除配置中已失效的厂区与区域码。 */
export function pruneSelectedAreas(
  selected: Record<string, AreaSelection>,
  options: FactoryAreaConfig[],
): Record<string, AreaSelection> {
  const valid: Record<string, Set<string>> = {}
  for (const f of options) {
    if (f.factoryType) valid[f.factoryType] = new Set((f.areas ?? []).map((a) => a.code))
  }
  const result: Record<string, AreaSelection> = {}
  for (const [factoryType, sel] of Object.entries(selected)) {
    const codes = valid[factoryType]
    if (!codes) continue
    const list = sel.list.filter((c) => codes.has(c))
    if (list.length > 0) result[factoryType] = { ...sel, list }
  }
  return result
}
```

- [ ] **Step 4: PASS** → **Step 5: Commit** `feat(visitor): add area options loading chain and pruning`

### Task 1.5: 公共组件（步骤条 / 人脸上传 / 短信验证字段）

**Files:**
- Create: `src/components/visitor-steps.tsx`、`src/components/face-upload.tsx`、`src/components/sms-code-field.tsx`
- Modify: `src/app/login/page.tsx`（短信倒计时逻辑抽到 sms-code-field 后复用，行为不变）

- [ ] **Step 1: visitor-steps**：`<VisitorSteps current={1|2|3} />`，三步「被访信息/访客信息/提交信息」，antd-mobile `Steps` 或自绘横向圆点+连线（对齐 mockup）。
- [ ] **Step 2: face-upload**：

```tsx
'use client'
// Props: { value?: string; onChange(photoId: string): void; mode: 'face' | 'plain'; label?: string }
// 实现要点：
// 1. <input type="file" accept="image/*" capture="user"> 隐藏，点击占位卡触发。
// 2. FileReader 读 base64（去掉 dataURL 前缀）。
// 3. mode==='face'：先 faceCut(base64)（失败 toast「人脸检测失败，请重新拍摄」终止），
//    用返回的裁剪图（res.data.imageData ?? 原图）调 checkFace；
//    mode==='plain'（车辆证件照）：直接 checkFace。
// 4. checkFace 返回 res.data 作为 photoId → onChange(String(id))；失败 toast message。
// 5. 上传中 SpinLoading 蒙层；已有 value 时显示缩略图（dataURL 本地预览）+ 可重新上传。
```

- [ ] **Step 3: sms-code-field**：受控组件 `{ phone, code, onPhoneChange, onCodeChange, onSend: () => Promise<void> }`，内部管理 120s 倒计时与发送 loading（逻辑从 `login/page.tsx` 平移）；改造登录页复用它，登录页 e2e 必须保持全绿（占位文案不变）。
- [ ] **Step 4: `pnpm check && pnpm e2e -- auth` 全绿** → **Step 5: Commit** `feat(visitor): add shared visitor components`

### Task 1.6: 页面 `/visitor`（被访人）

**Files:**
- Create: `src/app/visitor/page.tsx`
- Test: `e2e/visitor-flow.spec.ts`（新建，首条用例）

- [ ] **Step 1: 实现页面**。行为契约：
  - 挂载时：URL 无 `code` → `redirectToWechatOAuth('/visitor')`；有 `code` → 并行调 `getAdmittanceNotice(parkId)`（`isNeedNotice===1` 时 `Dialog.alert` 展示富文本 + 知道了）与 `getVisitorOpenId(code)`（成功 `patchHost({openId, unionId})`，失败 toast 不阻塞）。
  - 表单：只读园区名（config.parkName）、被访人姓名、手机号（11 位校验）；store 回填。
  - 下一步：校验 → `searchReceptionist({parkId, ...})` → code===0 存 host → `router.push('/visitor/info')`；失败 toast message。
  - 布局：VisitorSteps(1) + 卡片表单 + 底部主按钮（mockup visitor/index.html）。
- [ ] **Step 2: E2E**：stub OAuth；`/visitor?code=mock` → mock notice(isNeedNotice=1) 断言弹窗 → 填表 → mock searchReceptionist 断言请求体（parkId/姓名/手机号）→ 跳 `/visitor/info`；另一条：无 code 时跳 OAuth 且 redirect_uri 含 `/visitor`。
- [ ] **Step 3: 全绿后 Commit** `feat(visitor): add receptionist step page`

### Task 1.7: 页面 `/visitor/info`（访客信息）

**Files:**
- Create: `src/app/visitor/info/page.tsx`
- Test: `e2e/visitor-flow.spec.ts` 追加

- [ ] **Step 1: 实现**。行为契约（spec §1 第 2 行）：
  - 挂载并行拉：cause 枚举、person/cert 枚举、area/type/list、`loadAreaOptions(parkId)`；store 回填全部字段。
  - 字段：姓名 / FaceUpload(mode=face) / 证件号 / 来访单位 / 来访事由 Picker / 厂区单选卡片（factoryName + 已选 N；切厂区清其他厂区选择 `replaceAreas({[t]: 当前]})`）/ 区域 chips（前 `inlineAreaLimit ?? 4` 个 isCommon 区域内联多选 + 「更多区域(已选 N)」→ `/visitor/area?type={areaFlag}&factoryType=&parkId=`）/ 来访时间、离开时间（antd-mobile DatePicker，分钟精度）/ 随行人员入口行（显示 `N位`）→ `/visitor/persons`。
  - 下一步校验顺序：必填 → 区域非空（toast「授权区域不能为空！」）→ 离开>来访（toast「离开时间应大于来访时间!」）→ 跨度≤365 天 → `validateIdCard` → `checkApplyEqual({receptionistBadge, visitorList…}) `（按钮「正在验证」禁用）→ 通过 `router.push('/visitor/tel')`。
  - 所有输入 onChange 即写 store（persist 自动落盘）。
- [ ] **Step 2: E2E**：mock 枚举/区域/equal-check + mock 人脸两接口（faceCut→checkFace 返回 photoId）；走通填写→下一步→断言 equal/check 请求体与跳转；校验分支断言（区域空、时间倒挂）。
- [ ] **Step 3: 全绿后 Commit** `feat(visitor): add visitor info step page`

### Task 1.8: 页面 `/visitor/tel` + `/visitor/result`

**Files:**
- Create: `src/app/visitor/tel/page.tsx`、`src/app/visitor/result/page.tsx`
- Test: `e2e/visitor-flow.spec.ts` 追加

- [ ] **Step 1: tel 实现**。提交链（spec §1）：
  1. SmsCodeField（onSend=sendVisitorSms）；本地校验手机/验证码。
  2. `verifyVisitorSms` 失败 toast。
  3. `loadAreaOptions` 复核 → `pruneSelectedAreas`；剪空 → toast「请选择授权区域」`router.push('/visitor/info')`；有剪除则 `replaceAreas(pruned)`。
  4. `checkBlackVisitor`（data===false → toast「抱歉，你已被加入访客黑名单，不能进行入厂申请!」终止）。
  5. 组装 `saveVisitorApply` 请求体：host 三字段 + unionId + visitor 全字段 + `startTime/endTime + ':00'` + `permitArea/permitOldArea`（按 areaFlag 拼区域码逗号串）+ `areaType/permitFactoryType` + `personType:3, thing:4, remark:''` + `fellowList`（主访客 `isMain:1` 在首位 + 随行 `isMain:0`）+ `vehicleList`（cars 映射 certImg/certType.code/name/plate）。
  6. 成功 toast「申请成功」→ `reset()` → `/visitor/result`；失败 toast，按钮恢复（「正在提交」态）。
- [ ] **Step 2: result 实现**：成功插画（CheckCircle）+「已发送成功，等待被访对象审批」副文案 + 文字按钮「再预约一次」→ `/visitor`。
- [ ] **Step 3: E2E 主链全程**：从 `/visitor?code=` 一路到 result；**断言 save/apply 请求体**（isMain 结构、`:00` 秒、区域字段、vehicleList 为空数组）；黑名单拦截用例；区域剪空回跳用例。
- [ ] **Step 4: 全绿后 Commit** `feat(visitor): add sms verification submit and result pages`

### Task 1.9: 分支收尾

- [ ] `pnpm check && pnpm test && pnpm e2e && pnpm build` 全绿
- [ ] 子 agent 只读评审本分支 diff（重点：提交体组装正确性、store 竞态、OAuth 回跳、E2E 断言强度）→ 修复 → 复评至无明确问题
- [ ] PR `feat/visitor-flow → main`（英文 title/body：Summary/Changes/Testing/Risks）→ 合并

---

## 分支 2：feat/visitor-extras（区域 + 随行 + 车辆 5 页）

> 从最新 main 拉 `feat/visitor-extras`。

### Task 2.1: 页面 `/visitor/area`

**Files:**
- Create: `src/app/visitor/area/page.tsx`
- Test: `e2e/visitor-extras.spec.ts`（新建）

- [ ] **Step 1: 实现**（spec visitor-b §3）：query `type/factoryType/parkId`（parkId 缺省取 config）；`loadAreaOptions` 按 factoryType（或 areaFlag）取本厂区区域；匹配不到 → 清缓存 + toast「授权区域配置不可用，请联系管理员」+ 空列表。搜索框实时不区分大小写过滤（无匹配显示「暂无匹配区域」）；全选/取消全选切换；「已选 n/总数」；详细位置输入；确定 → `setFactoryAreas(factoryType, {list, custom})` → `router.back()`；重置清勾选与详细位置；进入回显 store 已选。
- [ ] **Step 2: E2E**：mock area-options → 搜索过滤 → 全选 → 确定 → 回 info 断言「已选 N」更新；配置不可用 toast 用例。
- [ ] **Step 3: Commit** `feat(visitor): add area selection page`

### Task 2.2: 随行人员两页

**Files:**
- Create: `src/app/visitor/persons/page.tsx`、`src/app/visitor/persons/add/page.tsx`
- Test: `e2e/visitor-extras.spec.ts` 追加

- [ ] **Step 1: 列表页**：标题「已添加随行人员（N人）」；卡片（照片缩略/姓名/证件号/编辑/删除）；删除直接移除（无确认，复刻旧行为）；空态文案；底部「新增随行人员」（次）→ `/visitor/persons/add`、「确 定」（主）→ `router.back()`。编辑 → `/visitor/persons/add?index=i`。
- [ ] **Step 2: 添加/编辑页**：姓名 + FaceUpload(mode=face) + 证件号（`validateIdCard`）；`index` query 存在时回填、按钮文案「确认修改随行人员」否则「确认添加随行人员」；提交 add/updateFellow → `router.back()`。
- [ ] **Step 3: E2E**：增→列表显示→编辑→删除→空态；身份证非法 toast。
- [ ] **Step 4: Commit** `feat(visitor): add fellow person pages`

### Task 2.3: 车辆两页 + 车牌输入组件

**Files:**
- Create: `src/components/plate-input.tsx`、`src/app/visitor/cars/page.tsx`、`src/app/visitor/cars/add/page.tsx`
- Test: `src/components/plate-input` 不单测（纯 UI）；`e2e/visitor-extras.spec.ts` 追加

- [ ] **Step 1: plate-input**：省份简称 Picker（京津冀晋蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新）+ 字母数字输入，value 形如 `豫A12345`。
- [ ] **Step 2: 列表页**：同随行人员模式（「已添加车辆（N辆）」/ 车牌标题 + 司机姓名行）。
- [ ] **Step 3: 添加/编辑页**：车牌（PlateInput）/ 司机姓名（默认 `visitor.visitorName`）/ 证件类型（`getVehicleCertEnum`，默认 code=2 身份证复印件）/ 证件照片 FaceUpload(mode=plain)；提交 add/updateCar → `/visitor/cars`。
- [ ] **Step 4: E2E**：添加车辆全字段 → 列表 → 删除；证件照走 checkFace mock。
- [ ] **Step 5: Commit** `feat(visitor): add vehicle pages with plate input`
- [ ] **Step 6: 主链回归**：分支 1 的 E2E 主链加随行人员/车辆数据重跑，断言 save/apply 的 fellowList/vehicleList 完整。

### Task 2.4: 分支收尾（同 Task 1.9：全绿 → 评审修复 → PR → 合并）

---

## 分支 3：feat/visitor-truck-code（货车 + 二维码 3 页）

> 从最新 main 拉 `feat/visitor-truck-code`。

### Task 3.1: 货车预约 + 结果页

**Files:**
- Create: `src/app/visitor/truck/page.tsx`、`src/app/visitor/truck/result/page.tsx`
- Test: `e2e/visitor-truck-code.spec.ts`（新建）

- [ ] **Step 1: truck 实现**（spec visitor-b §4）：分组「货车预约信息」（PlateInput / 事由 `getTruckCauseEnum` / 姓名 / 出发地 / 预约时间 DatePicker / 备注 placeholder「请填写内托/原材/成品/其他」）+ 分组「手机验证」（SmsCodeField）。提交：本地校验 → `verifyVisitorSms` → `saveTruckApply({visitorName, visitorPhone, remark, cause, startTime+':00', company, vehicleList:[{name, plate}]})` → toast「申请成功」→ `/visitor/truck/result`；防重复提交。本页独立状态（不写 flow-store，旧版即如此）。
- [ ] **Step 2: result 页**：「已发送成功，等待系统审批」+ 再预约一次 → `/visitor/truck`。
- [ ] **Step 3: E2E**：填表 → 提交 → 断言 save/car/apply 请求体（vehicleList 单车、时间秒）→ result。
- [ ] **Step 4: Commit** `feat(visitor): add truck booking pages`

### Task 3.2: 二维码页 `/visitor/code`

**Files:**
- Create: `src/app/visitor/code/page.tsx`
- Test: `e2e/visitor-truck-code.spec.ts` 追加

- [ ] **Step 1: 实现**（spec visitor-b §7）：query `id` → `getApplyDetail(id)` + `getFactoryTypeEnum(0/1)`（code→名称映射）。三态：
  - `delFlag=0`：标语「{parkName}欢迎您」、二维码图（`data.qrCode` base64 `<img src={'data:image/png;base64,'+...}>`）、预约码 `smsCode`、橙标「首次扫码，打印有效」、截图提示、预约信息卡（园区/事由标签/被访人/访客/区域类型/新老工厂区域+permitArea/起止时间）、三步使用指引、底部 logo。
  - `delFlag=1`：失效占位 + 红字「二维码已失效」+ 预约信息卡。
  - `delFlag=2`：仅失效占位 + 红字。
  - 无 id 或接口失败：toast + ErrorBlock。
- [ ] **Step 2: E2E**：三态各一条（mock detail 返回不同 delFlag），断言关键元素显隐。
- [ ] **Step 3: Commit** `feat(visitor): add pass QR code page`

### Task 3.3: 模块收尾

- [ ] `pnpm check && pnpm test && pnpm e2e && pnpm build` 全绿
- [ ] 子 agent 评审本分支 → 修复 → 复评
- [ ] PR → 合并
- [ ] 更新 README「已实现」段 + 记忆文件（访客模块完成状态）
- [ ] 向旅途汇报：交付清单、与旧版差异点、真机验证清单（访客 OAuth 域名、人脸上传摄像头链路）

---

## Self-Review 记录

- Spec 覆盖：§1 12 页 ↔ Task 1.6-1.8 / 2.1-2.3 / 3.1-3.2；§2 store ↔ 1.2；§3 API ↔ 1.3；§4 组件 ↔ 1.5/2.3；§5 错误处理嵌入各页行为契约；§6 分支门禁 ↔ 1.9/2.4/3.3；§7 测试 ↔ 各 Task E2E + 1.1/1.2/1.4 单测。无遗漏。
- 类型一致性：`FactoryAreaConfig`/`AreaSelection`/`FellowPerson`/`VisitorCar` 在 1.2/1.3/1.4 定义并被页面 Task 引用，命名已对齐。
- 占位符扫描：页面 Task 以「行为契约」形式给出完整字段/校验/跳转规则与关键代码骨架；E2E 断言点明确。
