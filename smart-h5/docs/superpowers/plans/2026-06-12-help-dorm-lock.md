# help + dorm + lock 批次实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按已批准 spec（`../specs/2026-06-12-help-dorm-lock-design.md`）交付 help 2 页 + dorm 2 页 + lock 2 页与 AES 加密层，单分支 `feat/help-dorm-lock`。

**Architecture:** AES 层（crypto-js）以旧 encryption.js 为 oracle 做密文比对单测；6 个页面全部登录态（useRequireAuth）+ badge 来自 baseinfo；复用既有 PageShell/RichTextBody 模式/FaceUpload（扩展 onUploaded 回调）/TanStack Query。

**Tech Stack:** Next.js 16 · antd-mobile 5 · crypto-js · vitest · Playwright

**通用约定：** 每 Task 完成跑 `pnpm check && pnpm test`，页面 Task 加对应 E2E；E2E 网络层 mock；门锁 E2E 用测试 key 加密的真密文 + config.js 拦截注入 securityEncodeKey。

---

### Task 1: AES 加密层（TDD 硬门禁）

**Files:**
- Create: `src/lib/crypto/aes.ts`
- Test: `src/lib/crypto/aes.test.ts`
- Modify: `src/lib/config/tenant.ts`（TenantConfig 加 `securityEncodeKey?: string`）

- [ ] **Step 1: `pnpm add crypto-js && pnpm add -D @types/crypto-js`**
- [ ] **Step 2: 失败测试**（oracle = 旧 encryption.js 逐字复刻）

```ts
import CryptoJS from 'crypto-js'
import { beforeEach, describe, expect, it } from 'vitest'
import { decryptFromHex, encryptFields } from './aes'

const TEST_KEY = 'abcdef0123456789' // 16 字节测试密钥，不是生产 key

beforeEach(() => {
  window.__SMART_CONFIG__ = { securityEncodeKey: TEST_KEY }
})

/** Oracle：旧 encryption.js 默认导出的加密逻辑逐字复刻 */
function legacyEncrypt(plain: string, key: string): string {
  const parsedKey = CryptoJS.enc.Latin1.parse(key)
  return CryptoJS.AES.encrypt(plain, parsedKey, {
    iv: parsedKey,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.ZeroPadding,
  }).toString()
}

/** Oracle 的逆向：构造旧 decryption() 期望的 hex 密文（ECB/Pkcs7） */
function legacyEncryptForDecrypt(plain: string, key: string): string {
  const parsedKey = CryptoJS.enc.Utf8.parse(key)
  const encrypted = CryptoJS.AES.encrypt(plain, parsedKey, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  })
  return encrypted.ciphertext.toString(CryptoJS.enc.Hex)
}

const SAMPLES = ['123456', '888000', '动态码中文', 'a-long-sample-text-with-多字节-🔒', '1']

describe('encryptFields vs legacy oracle', () => {
  it('同明文同 key 密文完全一致（CBC/ZeroPadding/IV=key）', () => {
    for (const plain of SAMPLES) {
      const result = encryptFields({ value: plain }, ['value'])
      expect(result.value).toBe(legacyEncrypt(plain, TEST_KEY))
    }
  })
  it('Base64 模式与旧实现一致', () => {
    const result = encryptFields({ v: 'hello' }, ['v'], 'Base64')
    expect(result.v).toBe(window.btoa('hello'))
  })
  it('不修改原对象与未列字段', () => {
    const src = { a: '1', b: '2' }
    const out = encryptFields(src, ['a'])
    expect(src.a).toBe('1')
    expect(out.b).toBe('2')
  })
})

describe('decryptFromHex vs legacy oracle', () => {
  it('oracle 构造的 hex 密文能还原明文（ECB/Pkcs7/Utf8-key）', () => {
    for (const plain of SAMPLES) {
      expect(decryptFromHex(legacyEncryptForDecrypt(plain, TEST_KEY))).toBe(plain)
    }
  })
  it('非法密文返回空串（防御，不抛错）', () => {
    expect(decryptFromHex('zzzz-not-hex')).toBe('')
  })
})

describe('key 缺失', () => {
  it('无 runtime key 且无 env key 时抛错（快速失败）', () => {
    window.__SMART_CONFIG__ = {}
    expect(() => decryptFromHex('00')).toThrow()
  })
})
```

- [ ] **Step 3: 跑 `pnpm test -- aes` 确认 FAIL** → **Step 4: 实现**

```ts
import CryptoJS from 'crypto-js'
import { getTenantConfig } from '@/lib/config/tenant'

/**
 * Legacy-compatible AES helpers (cleanroom port of the old encryption.js).
 * The two directions are deliberately asymmetric, matching the legacy app:
 * - encrypt: AES-CBC, ZeroPadding, key parsed as Latin1, IV = key, -> base64
 * - decrypt: AES-ECB, Pkcs7, key parsed as Utf8, input is HEX ciphertext
 * Compatibility is enforced by oracle tests against the legacy algorithm.
 */
function getKey(): string {
  const key = getTenantConfig().securityEncodeKey ?? process.env.NEXT_PUBLIC_SECURITY_ENCODE_KEY
  if (!key) throw new Error('securityEncodeKey is not configured')
  return key
}

export function encryptFields<T extends Record<string, unknown>>(
  data: T,
  fields: (keyof T)[],
  type?: 'Base64',
): T {
  const result = { ...data }
  if (type === 'Base64') {
    for (const field of fields) {
      result[field] = window.btoa(String(result[field])) as T[keyof T]
    }
    return result
  }
  const parsedKey = CryptoJS.enc.Latin1.parse(getKey())
  for (const field of fields) {
    result[field] = CryptoJS.AES.encrypt(String(result[field]), parsedKey, {
      iv: parsedKey,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.ZeroPadding,
    }).toString() as T[keyof T]
  }
  return result
}

export function decryptFromHex(hexStr: string): string {
  const key = CryptoJS.enc.Utf8.parse(getKey())
  try {
    return CryptoJS.AES.decrypt(
      CryptoJS.enc.Base64.stringify(CryptoJS.enc.Hex.parse(hexStr)),
      key,
      { mode: CryptoJS.mode.ECB, padding: CryptoJS.pad.Pkcs7 },
    ).toString(CryptoJS.enc.Utf8)
  } catch {
    return ''
  }
}
```

tenant.ts：`TenantConfig` 加可选 `securityEncodeKey?: string`（不进 DEFAULTS——key 不进仓库；config.js 留注释占位说明部署注入）。

- [ ] **Step 5: PASS** → **Step 6: Commit** `feat(crypto): add legacy-compatible AES layer with oracle tests`

### Task 2: dorm/help API 层 + 水电展示规则纯函数（TDD）

**Files:**
- Create: `src/features/dorm/api.ts`、`src/features/dorm/water-elec-rules.ts`、`src/features/help/api.ts`
- Test: `src/features/dorm/water-elec-rules.test.ts`

- [ ] **Step 1: 失败测试（水电规则）**

```ts
import { describe, expect, it } from 'vitest'
import { normalizeCateInfos } from './water-elec-rules'

describe('normalizeCateInfos（旧版前端规则：过滤热水、冷水改名水）', () => {
  it('过滤热水、冷水→水，其余原样', () => {
    expect(
      normalizeCateInfos([
        { cateName: '热水', fee: 5 },
        { cateName: '冷水', fee: 10 },
        { cateName: '电', fee: 20 },
      ]),
    ).toEqual([
      { cateName: '水', fee: 10 },
      { cateName: '电', fee: 20 },
    ])
  })
  it('空数组与缺字段容错', () => {
    expect(normalizeCateInfos([])).toEqual([])
    expect(normalizeCateInfos(undefined)).toEqual([])
  })
})
```

- [ ] **Step 2: FAIL → 实现**

```ts
// water-elec-rules.ts
export interface CateInfo { cateName?: string; fee?: number }
/** Legacy display rule: drop 热水, rename 冷水 -> 水. */
export function normalizeCateInfos(cateInfos: CateInfo[] | undefined): CateInfo[] {
  return (cateInfos ?? [])
    .filter((c) => c.cateName !== '热水')
    .map((c) => (c.cateName === '冷水' ? { ...c, cateName: '水' } : c))
}
```

- [ ] **Step 3: API 层**（信封 `{code,data?,message?/msg?}`，bearer 默认）

```ts
// features/dorm/api.ts
export interface StatementRecord { staffName?: string; staffBadge?: string; statementDate?: string; meterMonth?: string; cateInfos?: CateInfo[]; totalFee?: number }
export const getWaterElecRecords = (p: { current: number; size: number; statementMonth: string }) =>
  request<Envelope<{ records?: StatementRecord[]; pages?: number }>>({ module: 'platform', url: '/dormitory/staff/statementdetail/record', params: p })
export const getLockPwd = (badge: string) =>
  request<Envelope<string> & { msg?: string }>({ module: 'platform', url: '/dormitory/staff/get/pwd', params: { badge } })
export const updateLockPwd = (data: { badge: string; newPwd: string }) =>
  request<{ code: number; msg?: string }>({ module: 'platform', url: '/dormitory/staff/update/lock/pwd', method: 'POST', data })
export const refreshLockPwd = (data: { badge: string; facePic: string }) =>
  request<{ code: number; msg?: string }>({ module: 'platform', url: '/dormitory/staff/update/pwd', method: 'POST', data })

// features/help/api.ts
export interface HelpQuestion { questionId?: number | string; questionTitle?: string }
export const getHelpQuestions = (p: { current: number; size: number }) =>
  request<Envelope<{ records?: HelpQuestion[]; pages?: number }>>({ module: 'app', url: '/guide/help/question/list', params: p })
export const getHelpAnswer = (id: string) =>
  request<Envelope<{ answerContent?: string; questionTitle?: string }>>({ module: 'app', url: `/guide/help/question/answer/${id}` })
```

- [ ] **Step 4: 全部单测过 + check 过** → **Step 5: Commit** `feat(dorm): add dorm/help api layer and water-elec display rules`

### Task 3: help 两页 + RichTextBody 抽取复用

**Files:**
- Create: `src/components/rich-text-body.tsx`（从公告详情抽取）、`src/app/help/page.tsx`、`src/app/help/[id]/page.tsx`
- Modify: `src/app/home/bbs/[id]/page.tsx`（改用公共 RichTextBody，行为不变）
- Test: `e2e/help-dorm-lock.spec.ts`（新建）

- [ ] **Step 1: 抽取 RichTextBody** 到 `src/components/rich-text-body.tsx`（DOMPurify + ImageViewer 多图，代码原样平移），bbs 详情页改 import；跑既有 home e2e 保持绿。
- [ ] **Step 2: /help 列表页**：PageShell「帮助中心」+ 橙色横幅（标题+插画位）；`useQuery` 拉 `getHelpQuestions({current,size:10})`，上拉加载合并 records（current<pages），PullToRefresh 重置并 toast「更新成功」；列表项标题省略+箭头 → `/help/{questionId}`；空态 ErrorBlock；失败 toast。
- [ ] **Step 3: /help/[id] 详情页**：PageShell「问题详情」+ RichTextBody(answerContent)；加载/失败态。
- [ ] **Step 4: E2E**：列表渲染→点击进详情富文本可见；空态；mine 页「帮助中心」菜单点击落到 /help（死链闭合回归）。
- [ ] **Step 5: 全绿 Commit** `feat(help): add help center list and detail pages`

### Task 4: dorm 聚合页 + 水电明细页

**Files:**
- Create: `src/app/dorm/page.tsx`、`src/app/dorm/water-elec/page.tsx`
- Test: `e2e/help-dorm-lock.spec.ts` 追加

- [ ] **Step 1: /dorm**：PageShell「我的宿舍」+ 两行入口卡（图标+标题+副标题+箭头）→ /dorm/lock、/dorm/water-elec。
- [ ] **Step 2: /dorm/water-elec**：筛选条（「查询全部」链接 + 月份按钮拉起 DatePicker precision='month'，默认当月、max 当月，选全部后显示「未选择」）；账单卡（姓名-工号 + statementDate / 抄表月份 / `normalizeCateInfos` 后逐项「房间{cateName}费 ¥fee」 / 总计 ¥totalFee）；上拉分页 + PullToRefresh；空态；失败 toast。
- [ ] **Step 3: E2E**：默认当月参数断言（statementMonth=YYYY-MM 当月值）→ 查询全部（statementMonth=''）→ 卡片断言「房间水费」可见且「热水」不可见；空态；mine 页「我的宿舍」菜单落到 /dorm（死链闭合回归）。
- [ ] **Step 4: 全绿 Commit** `feat(dorm): add dorm hub and water-elec statement pages`

### Task 5: 门锁两页（解密展示 + 人脸刷新）

**Files:**
- Create: `src/app/dorm/lock/page.tsx`、`src/app/dorm/get-code/page.tsx`
- Modify: `src/components/face-upload.tsx`（加可选 `onUploaded?: (raw: { code: number; data?: unknown; resultData?: { base64?: string } }) => void`）
- Test: `e2e/help-dorm-lock.spec.ts` 追加

- [ ] **Step 1: /dorm/lock**：
  - 拉 baseinfo 取 badge → `getLockPwd(badge)`；`data` 为空 → Dialog.alert「您暂未入住智能宿舍，请联系宿管入住！」→ 确认 `router.replace('/dorm')`。
  - 有密文：`decryptFromHex(data)` 展示大号码；解密结果空串 → 显示 `******` + toast「动态码解析失败」。
  - 「修改动态码」→ antd-mobile Dialog 含受控输入框：校验 `/^[0-9]{6}$/`（toast「请输入6位数字动态码」）、与当前明文相同（toast「请输入跟当前动态码不一样的新的动态码」）→ `updateLockPwd({badge,newPwd})`；`code!==0` → alert「错误」+msg；成功关弹窗重拉。
  - 「刷新动态码（人脸）」次按钮 → `/dorm/get-code`。
- [ ] **Step 2: face-upload 扩展**：上传成功后（photoId 有效分支内）调用 `onUploaded?.(rawResponse)`；既有调用方不受影响（可选 prop）。
- [ ] **Step 3: /dorm/get-code**：文案「刷新动态码 / 需完成人脸识别」+ FaceUpload(mode='face', onUploaded 捕获 `resultData.base64` 存 state)；比对成功显示「人脸对比成功」+「生成动态码」主按钮 → `refreshLockPwd({badge, facePic: base64})`；成功 alert「刷新动态码成功！」→ 跳 `/dorm/lock`；失败 alert「错误」+msg。**注意**：checkFace 的 mock 响应需含 `resultData: { base64 }`（访客模块消费 data，本页消费 resultData.base64，双字段同回）。
- [ ] **Step 4: E2E**：
  - 注入测试 key（config.js 拦截加 `securityEncodeKey:'abcdef0123456789'`）；mock getLockPwd 返回「用旧算法 ECB/Pkcs7 加密 '123456' 的 hex」（测试文件内用 crypto-js 现场生成）→ 断言页面显示 123456。
  - 修改三连：填 12345 → toast 非 6 位；填 123456 → toast 与当前相同；填 654321 → 断言提交体 {badge,newPwd:'654321'} → 成功重拉。
  - 未入住：getLockPwd data 空 → alert 文案 → 确认回 /dorm。
  - get-code：上传（mock checkFace 带 resultData.base64）→「人脸对比成功」→ 生成 → 断言提交体 facePic → alert → 回 /dorm/lock。
- [ ] **Step 5: 全绿 Commit** `feat(lock): add door-lock code pages with AES decryption`

### Task 6: 收尾

- [ ] `pnpm check && pnpm test && pnpm e2e && pnpm build` 全绿
- [ ] 子 agent 只读评审本分支 diff（重点：AES 与 oracle 等价性、key 处理、门锁状态机、face-upload 扩展兼容性、E2E 强度）→ 修复 → 复评至无明确问题
- [ ] PR `feat/help-dorm-lock → main`（英文 Summary/Changes/Testing/Risks）→ 合并
- [ ] 更新 README 已实现段 + 记忆 → 向旅途汇报验收，**停下等指令**（不自动进第 2 批）

---

## Self-Review 记录

- Spec 覆盖：§1 六页 ↔ Task 3/4/5；§2 AES ↔ Task 1；§3 接口 ↔ Task 2；§4 测试 ↔ 各 Task E2E + Task 1/2 单测（含 mine 死链闭合回归）；§5 门禁 ↔ Task 6；§6 防御决策嵌入 Task 5（解密失败 ******+toast）。无缺口。
- 类型一致性：CateInfo/StatementRecord/HelpQuestion 定义于 Task 2 并被 Task 3/4 引用；decryptFromHex/encryptFields 定义于 Task 1 被 Task 5 引用；onUploaded 签名在 Task 5 内一致。
- 占位符扫描：通过。
