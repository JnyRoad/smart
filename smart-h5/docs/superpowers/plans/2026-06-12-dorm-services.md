# 宿舍域三件套实施计划（dorm-repairs / dorm-exit / check-in）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按已批准 + Codex 确认的 spec（`../specs/2026-06-12-dorm-services-design.md`）交付报修/退宿/宿舍申请共 9 页，三分支依次合并。

**Architecture:** 第 1 分支落公共设施（SegmentTabs / ApprovalTimeline / ImageListUpload / useListPager hook 并迁移既有两个列表页）；后续分支复用。全部登录态，parkId 取 getTenantConfig().parkId，badge/姓名取 baseinfo。

**Tech Stack:** Next.js 16 · antd-mobile 5 · TanStack Query（详情类）+ useListPager（分页列表）· vitest · Playwright

**通用约定：** 每 Task `pnpm check && pnpm test` + 对应 E2E；分支收尾全绿 → 子 agent 评审修复 → PR → 合并 → 下一分支从最新 main 拉。

---

## 分支 A：feat/dorm-repairs

### Task A1: useListPager hook（TDD，含竞态守卫）+ 迁移 help/water-elec

**Files:**
- Create: `src/lib/use-list-pager.ts`
- Test: `src/lib/use-list-pager.test.ts`（@testing-library/react 不引入——用 renderHook 替代：`pnpm add -D @testing-library/react`）
- Modify: `src/app/help/page.tsx`、`src/app/dorm/water-elec/page.tsx`（迁移，行为不变）

- [ ] **Step 1: `pnpm add -D @testing-library/react`**
- [ ] **Step 2: 失败测试**

```ts
import { act, renderHook, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { useListPager } from './use-list-pager'

interface Row { id: number }

function deferred<T>() {
  let resolve!: (v: T) => void
  const promise = new Promise<T>((r) => (resolve = r))
  return { promise, resolve }
}

describe('useListPager', () => {
  it('loadMore 翻页合并，filterKey 切换重置', async () => {
    const fetcher = vi.fn(async (page: number, key: string) => ({
      rows: [{ id: page * 10 + Number(key) }] as Row[],
      pages: 2,
    }))
    const { result, rerender } = renderHook(
      ({ k }) => useListPager<Row, string>({ filterKey: k, fetchPage: (p) => fetcher(p, k) }),
      { initialProps: { k: '1' } },
    )
    await act(() => result.current.loadMore())
    expect(result.current.rows).toEqual([{ id: 11 }])
    await act(() => result.current.loadMore())
    expect(result.current.rows).toEqual([{ id: 11 }, { id: 21 }])
    expect(result.current.hasMore).toBe(false)

    rerender({ k: '2' })
    expect(result.current.rows).toEqual([])      // filterKey 变化即重置
    expect(result.current.loadedOnce).toBe(false)
  })

  it('过期响应不污染列表（filterKey 切换后丢弃旧 in-flight）', async () => {
    const slow = deferred<{ rows: Row[]; pages: number }>()
    const calls: string[] = []
    const { result, rerender } = renderHook(
      ({ k }) =>
        useListPager<Row, string>({
          filterKey: k,
          fetchPage: () => {
            calls.push(k)
            return k === 'old' ? slow.promise : Promise.resolve({ rows: [{ id: 99 }], pages: 1 })
          },
        }),
      { initialProps: { k: 'old' } },
    )
    const oldLoad = act(() => result.current.loadMore())   // 旧 key 在途
    rerender({ k: 'new' })
    await act(() => result.current.loadMore())              // 新 key 完成
    slow.resolve({ rows: [{ id: 1 }], pages: 5 })           // 旧响应迟到
    await oldLoad
    await waitFor(() => expect(result.current.rows).toEqual([{ id: 99 }]))
    expect(result.current.pages).toBe(1)                    // 旧 pages 不覆盖
  })

  it('refresh 重置到第一页', async () => {
    const fetcher = vi.fn(async (page: number) => ({ rows: [{ id: page }], pages: 3 }))
    const { result } = renderHook(() =>
      useListPager<Row, string>({ filterKey: '', fetchPage: fetcher }),
    )
    await act(() => result.current.loadMore())
    await act(() => result.current.loadMore())
    await act(() => result.current.refresh())
    expect(result.current.rows).toEqual([{ id: 1 }])
  })
})
```

- [ ] **Step 3: FAIL** → **Step 4: 实现**

```ts
import { useRef, useState } from 'react'

export interface PageResult<Row> { rows: Row[]; pages: number }

/**
 * Paginated-list state machine shared by all list pages (InfiniteScroll +
 * PullToRefresh). filterKey changes reset the list and invalidate in-flight
 * responses (the month-switch race guard generalized).
 */
export function useListPager<Row, Key>({
  filterKey,
  fetchPage,
}: {
  filterKey: Key
  fetchPage: (page: number) => Promise<PageResult<Row>>
}) {
  const [rows, setRows] = useState<Row[]>([])
  const [loadedOnce, setLoadedOnce] = useState(false)
  const [paging, setPaging] = useState({ current: 0, pages: 1 })
  const keyRef = useRef(filterKey)
  const lastKeyRef = useRef(filterKey)

  // Render-time reset on filter change (no effect needed; render is pure w.r.t. state setters guarded by comparison)
  if (lastKeyRef.current !== filterKey) {
    lastKeyRef.current = filterKey
    keyRef.current = filterKey
    setRows([])
    setLoadedOnce(false)
    setPaging({ current: 0, pages: 1 })
  }

  async function runLoad(target: number, reset: boolean) {
    const issuedKey = filterKey
    const res = await fetchPage(target)
    if (issuedKey !== keyRef.current) return  // stale response, drop
    setPaging({ current: target, pages: res.pages })
    setRows((prev) => (reset ? res.rows : [...prev, ...res.rows]))
    setLoadedOnce(true)
  }

  return {
    rows,
    loadedOnce,
    pages: paging.pages,
    hasMore: paging.current < paging.pages,
    loadMore: () => runLoad(loadedOnce ? paging.current + 1 : 1, !loadedOnce),
    refresh: () => runLoad(1, true),
  }
}
```

注意：render 期 setState 自身组件是 React 允许的（"adjusting state during render"官方模式），若 React Compiler lint 拒绝则改用 key 重挂或 useEffect+token，以 lint 通过为准。

- [ ] **Step 5: PASS** → **Step 6: 迁移 help/page.tsx 与 water-elec/page.tsx 到 useListPager**（fetchPage 包装各自 API：res.code!==0 时 Toast + throw；返回 `{rows, pages}`；water-elec 的 filterKey=month，删除原 monthTokenRef/loadPage/switchMonth 样板，switchMonth 只 setMonth）。跑 `pnpm exec playwright test e2e/help-dorm-lock.spec.ts` 既有 10 条全绿（迁移回归保护）。
- [ ] **Step 7: Commit** `refactor(lists): add useListPager hook and migrate help/water-elec`

### Task A2: 公共组件 SegmentTabs / ApprovalTimeline / ImageListUpload + result 映射纯函数（TDD）

**Files:**
- Create: `src/components/segment-tabs.tsx`、`src/components/approval-timeline.tsx`、`src/components/image-list-upload.tsx`、`src/features/dorm-services/process.ts`
- Test: `src/features/dorm-services/process.test.ts`

- [ ] **Step 1: 失败测试（result 映射）**

```ts
import { describe, expect, it } from 'vitest'
import { approvalResultLabel } from './process'

describe('approvalResultLabel', () => {
  it('0-4 映射文案与色调', () => {
    expect(approvalResultLabel(0)).toEqual({ text: '待审批', tone: 'info' })
    expect(approvalResultLabel(1)).toEqual({ text: '通过', tone: 'success' })
    expect(approvalResultLabel(2)).toEqual({ text: '拒绝', tone: 'danger' })
    expect(approvalResultLabel(3)).toEqual({ text: '关闭', tone: 'danger' })
    expect(approvalResultLabel(4)).toEqual({ text: '等待', tone: 'muted' })
  })
  it('未知值回退原值灰调', () => {
    expect(approvalResultLabel(9)).toEqual({ text: '9', tone: 'muted' })
  })
})
```

- [ ] **Step 2: FAIL → 实现 process.ts**

```ts
export type ProcessTone = 'info' | 'success' | 'danger' | 'muted'
export interface ApproverItem { name?: string; result?: number; opinion?: string; time?: string }
export interface ProcessNode { statusName?: string; approvers?: ApproverItem[] }

const RESULT_LABELS: Record<number, { text: string; tone: ProcessTone }> = {
  0: { text: '待审批', tone: 'info' },
  1: { text: '通过', tone: 'success' },
  2: { text: '拒绝', tone: 'danger' },
  3: { text: '关闭', tone: 'danger' },
  4: { text: '等待', tone: 'muted' },
}
export function approvalResultLabel(result: number): { text: string; tone: ProcessTone } {
  return RESULT_LABELS[result] ?? { text: String(result), tone: 'muted' }
}
```

- [ ] **Step 3: SegmentTabs**

```tsx
'use client'
import { useRouter } from 'next/navigation'
// 「发起提交｜查看数据」双段切换条；修正旧版退宿页跳错 bug：路由由调用方显式传入
export function SegmentTabs({ active, submitHref, listHref }: { active: 'submit' | 'list'; submitHref: string; listHref: string }) {
  const router = useRouter()
  const seg = (key: 'submit' | 'list', label: string, href: string) => (
    <button type="button" role="tab" aria-selected={active === key}
      onClick={() => active !== key && router.push(href)}
      className={`min-h-11 flex-1 rounded-[10px] text-sm font-bold ${active === key ? 'bg-white text-accent-ink shadow-[0_6px_18px_rgba(89,87,87,0.08),inset_0_0_0_1px_rgba(236,108,0,0.24)]' : 'text-mid'}`}>
      {label}
    </button>
  )
  return (
    <div role="tablist" className="mb-3 grid grid-cols-2 gap-1 rounded-[14px] border border-border-soft bg-surface p-1">
      {seg('submit', '发起提交', submitHref)}
      {seg('list', '查看数据', listHref)}
    </div>
  )
}
```

- [ ] **Step 4: ApprovalTimeline**（props `{ submitter?: {name?, time?}, nodes: ProcessNode[] }`；节点 0 提交节点「{name} - 提交申请」+时间；每审批节点 statusName + 审批人行 `{name} - {approvalResultLabel(result).text}`（按 tone 着色）+ 可选「意见: {opinion}」+ 时间；竖线圆点样式同 records 详情时间线）。
- [ ] **Step 5: ImageListUpload**

```tsx
'use client'
// props: { mode: 'photoId' | 'base64'; value: string[]; onChange(list: string[]): void; max?: number (默认 3) }
// photoId 模式：FileReader→base64→checkFace（plain）→ push photoId；失败 toast message
// base64 模式：FileReader→raw base64 → push（无网络）
// UI：已传图缩略（dataURL 本地预览）网格 + 删除角标 + 添加格子（达 max 隐藏）；点击缩略 ImageViewer 预览
// 上传中 SpinLoading 占位；并发守卫（上传中禁触发）
// data-testid: image-list-input / image-list-item
```

- [ ] **Step 6: check + 单测 PASS → Commit** `feat(dorm-services): add segment tabs, approval timeline and multi-image upload`

### Task A3: dorm-services API 层 + 报修联动表（TDD）

**Files:**
- Create: `src/features/dorm-services/api.ts`、`src/features/dorm-services/repair-options.ts`
- Test: `src/features/dorm-services/repair-options.test.ts`

- [ ] **Step 1: 失败测试（联动表）**

```ts
import { describe, expect, it } from 'vitest'
import { FALLBACK_RANGES, REPAIR_TYPES, buildingsForRange } from './repair-options'

describe('repair options', () => {
  it('区域→楼栋联动表', () => {
    expect(buildingsForRange(1)).toEqual(['老工厂1号宿舍', '老工厂2号宿舍', '老工厂3号宿舍', '新工厂宿舍楼'])
    expect(buildingsForRange(2)).toEqual(['餐厅三楼', '北门岗', '东门岗', '辅房'])
    expect(buildingsForRange(3)).toEqual(['一楼', '二楼', '三楼'])
    expect(buildingsForRange(4)).toEqual(['园区周边'])
    expect(buildingsForRange(99)).toEqual([])
  })
  it('兜底区域 4 项与维修类别 14 项', () => {
    expect(FALLBACK_RANGES).toHaveLength(4)
    expect(REPAIR_TYPES).toHaveLength(14)
    expect(REPAIR_TYPES[0]).toEqual({ code: 1, desc: '灯' })
    expect(REPAIR_TYPES[13]).toEqual({ code: 14, desc: '地漏' })
  })
})
```

- [ ] **Step 2: FAIL → 实现 repair-options.ts**（FALLBACK_RANGES 宿舍1/办公室2/车间3/园区周边4；REPAIR_TYPES 灯1 插座2 水龙头3 水管4 门窗5 锁6 空调7 其他8 床9 柜子10 玻璃11 洗手台12 桌椅13 地漏14；BUILDINGS Record<number,string[]> + buildingsForRange）。
- [ ] **Step 3: api.ts**（信封 `{code,data?,message?,msg?}`，全部 bearer）：

```ts
// 报修
getRepairRangeEnum(): GET app:/dormitory/repair/enum/range → Envelope<EnumItem[]>
addRepair(data): POST platform:/dormitory/repair/add  // {rangeType,repairType,dormitoryName,roomName,faultDesc,faultImgs,parkId}
getRepairRecords(p:{current,size}): GET platform:/dormitory/repair/query/record → Envelope<{records?,pages?}>
getRepairDetail(id): GET platform:/dormitory/repair/query/detail/${id}
// 退宿
getMyRooms(badge): GET app:/appdormitory/roomList/${badge} → Envelope<{ data?: {dormitoryId,roomId,dormitoryName,roomName}[] }>  // 双层 data
saveDormExit(data): POST platform:/dor/quit/apply
getDormExitPage(p:{current,size,badge}): GET platform:/dor/quit/page
getDormExitDetail(id): GET platform:/dor/quit/detail/${id}
// check-in
queryDormitories(): POST platform:/dormitory/queryDormitory {parkId, isAccount:true}
getRoomTypes(dormitoryId): GET platform:/dormitory/type/by/park-and-dormitory {parkId,dormitoryId}
getStaffIdentity(badge): GET platform:/staff/define/badge {badge}
submitCheckIn(data): POST platform:/dormitory/room/autoallot
getFloorTree(p): GET platform:/park/tree/condition
searchRooms(p): GET platform:/dormitory/room/search/condition
getBedDetail(roomId): POST platform:/dormitory/room/bedDetail/${roomId}
getCheckInRecords(badge): GET platform:/dormitory/staff/roomList/${badge}
```

- [ ] **Step 4: check 过 → Commit** `feat(dorm-services): add api layer and repair option tables`

### Task A4: 报修 3 页 + E2E

**Files:**
- Create: `src/app/dorm-repairs/page.tsx`、`src/app/dorm-repairs/list/page.tsx`、`src/app/dorm-repairs/detail/page.tsx`（query id，沿用旧路由形态）
- Test: `e2e/dorm-services.spec.ts`（新建）

- [ ] **Step 1: 发起页**：SegmentTabs(submit, '/dorm-repairs', '/dorm-repairs/list')；区域 picker（枚举接口失败→FALLBACK_RANGES）→ 楼栋 picker（buildingsForRange 联动，切区域默认首项）→ 类别 picker（REPAIR_TYPES）→ 房间 input → 故障描述 textarea → ImageListUpload(base64)；提交 addRepair 成功 `router.push('/dorm-repairs/list')`（无 toast），失败 toast。
- [ ] **Step 2: 列表页**：SegmentTabs(list)；useListPager（filterKey 固定 ''，fetchPage 包 getRepairRecords）；卡片标题「{姓名}提交的园区报修」+ statusDesc（status===3 绿 / 4、6 红 / 其余灰）+ 字段行；点卡片 → `/dorm-repairs/detail?id=`；空态；PullToRefresh+InfiniteScroll。
- [ ] **Step 3: 详情页**：TanStack Query getRepairDetail；状态卡 + 信息卡（位置 `楼栋#房间`）+ ApprovalTimeline + 维修结果区（repairReplyList 非空，replyStatusDesc==='维修成功' 绿否则红）。
- [ ] **Step 4: E2E**（e2e/dorm-services.spec.ts）：
  - 提交链：区域切换→楼栋默认首项断言 → 填表 + base64 两图 → 断言 add 提交体（faultImgs 为 base64 数组、rangeType/repairType 数值）→ 跳列表。
  - 列表：状态配色断言（toHaveCount + class 检查可简化为文本可见）→ 详情：维修结果区双态（成功绿/无法维修红文本）。
  - 死链回归：home 服务宫格「园区报修」入口 → /dorm-repairs 真实页（补 home mock）。
- [ ] **Step 5: 全绿 Commit** `feat(dorm-repairs): add repair submit/list/detail pages`

### Task A5: 分支 A 收尾

- [ ] README「已实现」段补：visitor-records、help/dorm/lock、本批报修（上批欠账一并）
- [ ] `pnpm check && pnpm test && pnpm e2e && pnpm build` 全绿 → 子 agent 评审 → 修复 → 复评 → PR → 合并

## 分支 B：feat/dorm-exit（从最新 main 拉）

### Task B1: 退宿房间选择纯函数（TDD）+ 发起页

**Files:**
- Create: `src/features/dorm-services/exit-rooms.ts`、`src/app/dorm-exit/page.tsx`
- Test: `src/features/dorm-services/exit-rooms.test.ts`

- [ ] **Step 1: 失败测试**

```ts
import { describe, expect, it } from 'vitest'
import { roomOption, splitRoomValues, addRoomDeduped } from './exit-rooms'

describe('exit rooms', () => {
  it('roomOption 组装 value/label（旧版格式）', () => {
    expect(roomOption({ dormitoryId: 'D1', roomId: 'R2', dormitoryName: '新工厂宿舍楼', roomName: '302' }))
      .toEqual({ value: 'D1/R2', label: '新工厂宿舍楼/302号房' })
  })
  it('splitRoomValues 拆 dormitoryIds/roomIds', () => {
    expect(splitRoomValues(['D1/R2', 'D3/R4'])).toEqual({ dormitoryIds: ['D1', 'D3'], roomIds: ['R2', 'R4'] })
  })
  it('addRoomDeduped 去重', () => {
    expect(addRoomDeduped([{ value: 'D1/R2', label: 'x' }], { value: 'D1/R2', label: 'x' })).toHaveLength(1)
    expect(addRoomDeduped([{ value: 'D1/R2', label: 'x' }], { value: 'D1/R3', label: 'y' })).toHaveLength(2)
  })
})
```

- [ ] **Step 2: FAIL → 实现** → **Step 3: 发起页**：SegmentTabs；房间 picker（getMyRooms 双层 data → roomOption；空列表 toast 原文案）+ 已选可删列表 + addRoomDeduped；退宿原因 picker 离职(3)/自离(5)/外宿(2)；DatePicker 日期；备注；ImageListUpload(photoId)；提交校验「请选择退宿房间！」→ saveDormExit（splitRoomValues + applyLeaveTime+':00' + parkId/badge/name），**成功判定 `code===0 && data`** toast「申请成功」→ `/dorm-exit/list`。
- [ ] **Step 4: 单测 + check 过 → Commit** `feat(dorm-exit): add exit application page with room selection`

### Task B2: 列表 + 详情（三态）+ E2E

**Files:**
- Create: `src/app/dorm-exit/list/page.tsx`、`src/app/dorm-exit/detail/page.tsx`
- Test: `e2e/dorm-services.spec.ts` 追加

- [ ] **Step 1: 列表**：useListPager + getDormExitPage（badge 参数）；卡片「{姓名}提交的退宿申请」status 2 绿/3 红；dorDetailStr 多行；→ `/dorm-exit/detail?id=`。
- [ ] **Step 2: 详情**：getDormExitDetail；status 分态：2=放行二维码（`data:image/jpeg;base64,` img）+「在门卫处出示放行码」/ 4=「已出厂」占位+「已同意出厂」/ 5=「已拒绝出厂」/ 其余无码区；信息卡（facePic=`/platform/image/view/${faceId}`，错误回退默认占位 div；照片≤3 ImageViewer 预览）；ApprovalTimeline。
- [ ] **Step 3: E2E**：发起链（房间多选去重/删除断言 → 提交体断言 dormitoryIds/roomIds/applyLeaveTime 秒/imgs photoId 数组）；详情 2/4/5 三态渲染断言；home 宫格「退宿申请」死链回归（Codex 提出的缺口）。
- [ ] **Step 4: 全绿 Commit** `feat(dorm-exit): add exit list and detail with pass-code states`

### Task B3: 分支 B 收尾（同 A5 流程）

## 分支 C：feat/check-in（从最新 main 拉）

### Task C1: 床位过滤纯函数（TDD）+ 选房草稿

**Files:**
- Create: `src/features/dorm-services/check-in-rules.ts`
- Test: `src/features/dorm-services/check-in-rules.test.ts`

- [ ] **Step 1: 失败测试**

```ts
import { describe, expect, it } from 'vitest'
import { availableBeds, loadRoomDraft, saveRoomDraft, clearRoomDraft } from './check-in-rules'

describe('availableBeds', () => {
  it('剔除已占用（staffBadge 非空或 delFlag=1）', () => {
    expect(
      availableBeds([
        { bedId: 'b1', bedNumber: 1, staffBadge: null, delFlag: 0 },
        { bedId: 'b2', bedNumber: 2, staffBadge: 'YT1', delFlag: 0 },
        { bedId: 'b3', bedNumber: 3, staffBadge: null, delFlag: 1 },
      ]),
    ).toEqual([{ bedId: 'b1', bedNumber: 1, staffBadge: null, delFlag: 0 }])
  })
})

describe('room draft (sessionStorage)', () => {
  it('存取与清除', () => {
    sessionStorage.clear()
    expect(loadRoomDraft()).toBeNull()
    saveRoomDraft({ floorId: 'f', roomId: 'r', roomName: '302', bedId: 'b', bedNumber: 2 })
    expect(loadRoomDraft()?.roomName).toBe('302')
    clearRoomDraft()
    expect(loadRoomDraft()).toBeNull()
  })
})
```

- [ ] **Step 2: FAIL → 实现**（draft key `check-in-room`，JSON 信封 try/catch）→ **Step 3: Commit** `feat(check-in): add bed filtering and room draft helpers`

### Task C2: 三页实现 + E2E

**Files:**
- Create: `src/app/check-in/page.tsx`、`src/app/check-in/select-room/page.tsx`、`src/app/check-in/detail/page.tsx`
- Test: `e2e/check-in.spec.ts`（新建）

- [ ] **Step 1: /check-in**：SegmentTabs；并行 queryDormitories + getStaffIdentity（失败：隐藏申请按钮 + toast「获取用户信息失败！」）；分配方式按钮组（自选默认/系统分配）；楼栋 picker → 房型 picker（getRoomTypes 联动，**切楼栋清空房型**）；自选模式显示房间/床位入口（校验楼栋房型已选后 `router.push('/check-in/select-room?dormitoryId=&roomType=')`，loadRoomDraft 回显）；提交 submitCheckIn（实名字段 + dormitoryId/roomType/badge/parkId + 自选 floorId/roomId/bedId；自选未选房 toast「请选择房间号」）成功 clearRoomDraft → `/check-in/detail`。
- [ ] **Step 2: /check-in/select-room**：query 读 dormitoryId/roomType；getFloorTree（无数据 toast「无房间信息！」回 /check-in）；左楼层列（默认第 1 层）/右房间格「{roomName}房 {男|女} {freeBedNum}/{totalBedNum}」（roomSex 0 男 1 女）；点房间 freeBedNum>0 → getBedDetail → availableBeds（剔空 toast「无可选用的床位！」）→ 床位 picker「X床」→ saveRoomDraft → `router.back()`。
- [ ] **Step 3: /check-in/detail**：SegmentTabs(list)；getCheckInRecords 卡片（楼栋/房间/床位 + 指纹/动态码状态：code 1/3 成功图标 0/2/4 失败；dynamicCode===3「已录入」否则 dynamicDesc）+ 卡底 fingerprintCode≠0 且 getLockPwd 有值 → decryptFromHex 展示；PullToRefresh；空态。
- [ ] **Step 4: E2E**（e2e/check-in.spec.ts）：实名失败隐藏按钮；楼栋切换清空房型断言；选房页满房拦截 + 床位过滤 + 草稿回填（返回 /check-in 显示 302/2号床）+ 刷新后草稿仍在（sessionStorage 场景，Codex 要求）；提交体断言（自选含 floorId/roomId/bedId + 实名字段）；detail 动态码解密（测试 key 真密文复用 legacyCipherHex 辅助——抽到 e2e/helpers.ts 共用）；home 宫格「宿舍申请」死链回归。
- [ ] **Step 5: 全绿 Commit** `feat(check-in): add dorm application, room selection and status pages`

### Task C3: 分支 C 收尾（同 A5 流程）+ 批次汇报

- [ ] 更新记忆批次表 → 汇报批 2 完成 → **直接进入批 3（物品放行域）brainstorming**（按旅途授权不停车）

---

## Self-Review 记录

- Spec 覆盖：§1 组件 ↔ A1/A2；§2 报修 ↔ A3/A4；§3 退宿 ↔ B1/B2；§4 check-in ↔ C1/C2；§5 测试逐条对应（含 Codex 四点：useListPager 竞态单测 A1、退宿入口死链 B2、parkId 模式 A3、草稿刷新场景 C2）；§6 防御决策内嵌各 Task。
- 类型一致性：EnumItem 沿用 visitor/api 模式重新本地定义；ProcessNode/ApproverItem 定义于 A2 被 A4/B2 引用；PageResult/useListPager 签名 A1 定义全批引用；roomOption/splitRoomValues B1 定义 B1 内用。
- 占位符扫描：通过（页面 Task 为行为契约 + 关键交互规则，复用已验证组件模式；ImageListUpload 以注释契约给出，实现见 A2）。
