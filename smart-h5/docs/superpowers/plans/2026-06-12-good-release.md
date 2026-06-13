# 物品放行域实施计划（good-release-live / return-factory / good-release-work）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按 Codex 确认的 spec（`../specs/2026-06-12-good-release-design.md`）交付物品放行域 16 页，三分支依次合并（live 3 页 → return-factory 2 页 → work 11 页）。

**Architecture:** 分支 1 落域公共层（`src/features/good-release/`：api / dicts / release-status / room-option）；分支 2 复用详情接口与时间线；分支 3 加 work 草稿 store 与只读链快照。全部登录态；parkId=getTenantConfig().parkId；badge/姓名取 baseinfo。

**Tech Stack:** Next.js 16 · antd-mobile 5 · TanStack Query（详情）+ useListPager（分页列表）· Zustand persist（work 草稿）· vitest · Playwright

**通用约定：** 每 Task `pnpm check && pnpm test` + 对应 E2E；分支收尾全绿（check/test/e2e/build）→ 子 agent 评审 → 修复 → 复评 → PR → 合并 → 下一分支从最新 origin/main 拉。**每页 E2E mock 形状先 grep 旧 Vue 源码核对**（旧仓库 `~/source/YUTO/yuto-smart/smart-h5/src/views-mobile/pages/{good-release-live,good-release-work,return-factory}/`）。

---

## 分支 A：feat/good-release-live（3 页 + 域公共层）

### Task A1: 域公共层（TDD）

**Files:**
- Create: `src/features/good-release/api.ts`、`dicts.ts`、`release-status.ts`、`room-option.ts`
- Test: `dicts.test.ts`、`release-status.test.ts`、`room-option.test.ts`

- [ ] **Step 1: 失败测试（三个纯模块）**

```ts
// dicts.test.ts 关键断言
expect(isPersonRelease(0)).toBe(true)
expect(isPersonRelease(7)).toBe(true)
expect(isPersonRelease(1)).toBe(false)
expect(YSFS_OPTIONS.find(o => o.value === 2)?.label).toBe('叉车')
expect(FXSX_OPTIONS).toHaveLength(9)
expect(FXDD_OPTIONS).toHaveLength(12)

// release-status.test.ts（spec §3 规则）
expect(qrPanelState({ expire: true, status: 2 })).toBe('expired')
expect(qrPanelState({ expire: false, status: 4 })).toBe('left')      // status=4 即使 expire 也算已出厂？旧码：expire&&status<4 才过期 → status4 恒 left
expect(qrPanelState({ expire: true, status: 4 })).toBe('left')
expect(qrPanelState({ expire: false, status: 2, qrCodePic: 'x' })).toBe('qr')
expect(qrPanelState({ expire: false, status: 1 })).toBe('none')
expect(listStatusTone(2)).toBe('success'); expect(listStatusTone(3)).toBe('danger'); expect(listStatusTone(1)).toBe('muted')
expect(showReleaseInfo(4)).toBe(true); expect(showReleaseInfo(5)).toBe(true); expect(showReleaseInfo(2)).toBe(false)
expect(showBackConfirm({ status: 4, backTime: null })).toBe(true)
expect(showBackConfirm({ status: 4, backTime: '2026-01-01' })).toBe(false)
expect(showBackConfirm({ status: 2, backTime: null })).toBe(false)

// room-option.test.ts（spec §3/§4：value 四段、label 四段；床位 id 字段是 id）
const opt = liveRoomOption({ dormitoryId: 'D1', floorId: 'F2', roomId: 'R3', id: 'B4', dormitoryName: '新工厂宿舍楼', floorName: '2', roomName: '302', bedNumber: 4 })
expect(opt.value).toBe('D1/F2/R3/B4')
expect(opt.label).toBe('新工厂宿舍楼/2层/302号房/4床')
expect(splitLiveRoomValue('D1/F2/R3/B4')).toEqual({ dormitoryId: 'D1', floorId: 'F2', roomId: 'R3', bedId: 'B4' })
```

- [ ] **Step 2: 实现**（dicts 字典数组 `{value, label}` + isPersonRelease；release-status 四个纯函数；room-option 两个纯函数；api.ts 按 spec §3 全部端点 + 类型：LiveRoom、ReleaseListItem、ReleaseDetail（含 applyMain、personDetailList、thingDetailList、approvalProcess、qrCodePic、expire、status、backTime、securityStaff、departureTime）、BackListItem 等——字段名先 grep 旧 list/detail vue 核对）
- [ ] **Step 3: 测试全绿 → commit `feat(good-release): add domain api, dicts and status rules`**

### Task A2: ImageListUpload confirmRemove + live 三页

**Files:**
- Modify: `src/components/image-list-upload.tsx`（加 `confirmRemove?: boolean`，true 时删除先 Dialog.confirm「是否移除此图片」）
- Create: `src/app/good-release/live/page.tsx`、`live/list/page.tsx`、`live/detail/page.tsx`

- [ ] **Step 1: 发起页**（spec §4：getLiveRooms 双层 data；空/失败 toast 拦截文案逐字「没有查询到您的房间信息，不能进行物品放行（生活区）申请」；表单 8 项；照片必填空图拦截；提交体平铺 + oneImg/twoImg/threeImg 空串补位；成功 code===0&&data 跳 list）
- [ ] **Step 2: 列表页**（useListPager + /articlesrelease/page badge+type=3+size=10；卡片字段与配色 spec §4）
- [ ] **Step 3: 详情页**（qrPanelState 三态区 + 申请信息 + 照片 ImageViewer 预览 + ApprovalTimeline + 放行信息区 status 4/5）
- [ ] **Step 4: `pnpm check && pnpm test` 全绿 → commit `feat(good-release): add living-area release pages`**

### Task A3: E2E + 收尾

**Files:**
- Create: `e2e/good-release-live.spec.ts`
- Modify: `src/features/home/module-routes.ts` 已有映射，补死链回归

- [ ] **Step 1: E2E**（spec §7 live 段：房间空拦截 / 空图拦截 / 提交体断言（含房间四 id 拆分与三 img）/ 列表配色 class / 详情三态 + 放行信息区 / home 宫格 `/releaseGoods` 死链回归；mock 形状对照旧 index/list/detail.vue）
- [ ] **Step 2: 全绿（含 build）→ 子 agent 评审 → 修复 → 复评 → PR → 合并**

---

## 分支 B：feat/return-factory（2 页，从最新 main 拉）

### Task B1: 列表 + 详情

**Files:**
- Create: `src/app/return-factory/page.tsx`、`detail/page.tsx`
- Modify: `src/features/good-release/api.ts`（若 A 未含 back 接口则补）

- [ ] **Step 1: 列表页**（双 Tab approvalStatus 0/1 各自 useListPager；`?tab=confirmed` 直达；卡片字段 spec §5；底部「搜索」按钮列表非空显示 → 弹层（工号/携带人姓名/开始/结束时间）确定重查当前 Tab）
- [ ] **Step 2: 详情页**（共用 /articlesrelease/detail/{id}；头部 + applyMain 字段 + 条件标签云：isPersonRelease → 写 sessionStorage 快照 `good-release-detail-items` 后跳 work 只读链 URL；ApprovalTimeline；「确认返厂」按钮 showBackConfirm → POST back/confirm → 成功 `router.replace('/return-factory?tab=confirmed')`）
- [ ] **Step 3: check+test 全绿 → commit**

### Task B2: E2E + 收尾

- [ ] **Step 1: `e2e/return-factory.spec.ts`**（spec §7：Tab 切换与直达 / 搜索重查请求参数断言 / 确认返厂按钮条件两态 + 确认后跳转 / 标签云点击仅断言快照写入+URL（work 未合并）/ home 宫格 `/returnFactory` 死链回归）
- [ ] **Step 2: 全绿 → 评审 → PR → 合并**

---

## 分支 C：feat/good-release-work（11 页，从最新 main 拉）

### Task C1: work 草稿 store + 提交体组装（TDD）

**Files:**
- Create: `src/features/good-release/work-draft.ts`（Zustand persist key `goods-work-draft`）、`work-submit.ts`
- Test: `work-draft.test.ts`、`work-submit.test.ts`

- [ ] **Step 1: 失败测试**

```ts
// work-submit.test.ts（spec §6 提交体，对照旧 index.vue:213-272）
const body = buildWorkSubmitBody({
  applyMain: { fxqc: 0, sffc: 0, fxdd: 1, fxddxq: 'x', dddd: 2, ddddxq: 'y', sqrjb: 1, fxsx: 0, wpfxlb: 0, fjsc: '' },
  badge: 'YT1', parkId: 5000021,
  persons: [{ gh: 'YT2', xm: 9, name: '李四', lcsy: '出差', lcDate: '2026-06-20 09:30' }],
  goods: [{ wpbm: 'A1', wpmc: '电脑', wpdw: '台', wpsl: '1', jsdw: '裕同', fxrq: '2026-06-20 00:00', bz: '', ysfs: 1, xm: 8, name: '王五', cph: '' }],
})
expect(body.applyMain.fxsx).toBe(0)
expect(body.status).toBe(1)
expect(body.personList).toEqual([{ gh: 'YT2', xm: 9, name: '李四', lcsy: '出差', lcrq: '2026-06-20', lcsj: '09:30' }])
expect(body.thingList).toEqual([])           // 人员分支时物品为空数组
const body2 = buildWorkSubmitBody({ ...同上, applyMain: { ...fxsx: 1 } })
expect(body2.personList).toEqual([])
expect(body2.thingList[0]).toMatchObject({ wpbm: 'A1', fxrq: '2026-06-20', ysfs: 1 })

// work-draft.test.ts：set 表单字段 / addPerson / updatePerson(i) / removePerson(i) / 同 goods / clearAll 后全空
```

- [ ] **Step 2: 实现 → 全绿 → commit**

### Task C2: 主表单 + 列表 + 详情

**Files:** `src/app/good-release/work/page.tsx`、`work/list/page.tsx`、`work/detail/page.tsx`

- [ ] **Step 1: 主表单**（spec §6：5 picker + 2 文本 + fxsx/wpfxlb + fjsc ImageListUpload max=1；条件入口行人员/物品标签；校验逐项 toast 文案摘旧码；提交 buildWorkSubmitBody → office/save → 成功 clearAll 跳 list）
- [ ] **Step 2: 列表**（office/page badge+type=5；卡片 compName/releaseItemDesc/createTime/backStatus + oaNode 标签）
- [ ] **Step 3: 详情**（同 live 骨架 + 条件标签云写快照跳只读链）
- [ ] **Step 4: check+test → commit**

### Task C3: 申请编辑链 4 页 + staff-search-popup

**Files:** `src/features/good-release/staff-search-popup.tsx`、`src/app/good-release/work/persons/page.tsx`、`persons/edit/page.tsx`、`goods/page.tsx`、`goods/edit/page.tsx`

- [ ] **Step 1: staff-search-popup**（弹层：工号输入 + 确定 → GET oa/staff/info/{badge} → onPicked({name, id})；失败 toast message）
- [ ] **Step 2: persons 列表页**（store 数据；「已添加放行人员（N人）」卡片 + 编辑/删除；空态文案；底部「新增人员」「确 定」）
- [ ] **Step 3: persons/edit**（`?index=` 编辑回填；工号/姓名经弹窗回填 gh/name/xm=id；lcsy 必填、lcDate 日期+时间必填；确认写 store 回列表）
- [ ] **Step 4: goods 列表 + goods/edit**（同构，字段 spec §6；ysfs picker；cph PlateInput）
- [ ] **Step 5: check+test → commit**

### Task C4: 详情只读链 4 页

**Files:** `src/app/good-release/work/detail/persons/page.tsx`、`detail/persons/item/page.tsx`、`detail/goods/page.tsx`、`detail/goods/item/page.tsx`、`src/features/good-release/detail-snapshot.ts`（读写快照 + 容错）

- [ ] **Step 1: detail-snapshot.ts + 单测**（load 返回 null 当缺失/损坏；按 i 取单条越界返回 null）
- [ ] **Step 2: 4 页**（字段 spec §6；快照缺失/越界/损坏 → `router.replace('/good-release/work')`；ysfsDesc 字典映射）
- [ ] **Step 3: check+test → commit**

### Task C5: E2E + 收尾

- [ ] **Step 1: `e2e/good-release-work.spec.ts`**（spec §7 work 段全部 + 补 return-factory 标签云到只读链的完整落地断言 + home 宫格 `/articlesrelease` 死链回归）
- [ ] **Step 2: 全绿（含 build）→ 评审 → 修复 → 复评 → PR → 合并 → 更新记忆批次表 → 汇报批 3**

---

## Self-Review 记录

- spec 覆盖：§3→A1、§4→A2/A3、§5→B1/B2、§6→C1-C4、§7 测试逐条映射到各 Task E2E、§8 防御项内嵌于对应 Task。
- 占位扫描：无 TBD；「文案摘旧码」「字段先 grep 旧 vue」为既定净室流程动作，非占位。
- 类型一致：`buildWorkSubmitBody` 入参 persons/goods 字段与 C3 编辑页写入字段一致（lcDate 在编辑页存整串、提交时拆分；xm 存 id）。
