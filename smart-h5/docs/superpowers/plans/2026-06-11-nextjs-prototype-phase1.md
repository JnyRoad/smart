# Next.js 商用重写 — 原型阶段（Phase 1）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `yuto-park-h5/` 目录搭建 Next.js 工程骨架，并交付「登录 + 首页 + 访客模块」的可点击高保真原型（纯 mock 数据，无真实 API），同时完成微信 Spike 页面骨架。

**Architecture:** 依据已批准的设计文档 `docs/superpowers/specs/2026-06-11-nextjs-rewrite-architecture.md`。原型即工程骨架：App Router 路由壳 + antd-mobile UI + 运行时租户配置（许昌/合肥流程分支）+ mock 数据层。原型评审通过后 mock 逐模块替换为真实 API，投入零浪费。

**Tech Stack:** Next.js 15 (App Router) · TypeScript strict · antd-mobile 5 · Tailwind CSS 4 · Zustand · Playwright · pnpm

**关键事实（来自旧系统勘察，执行者无需再查旧代码）：**
- 旧版路由全部带 `/xuchang/` 前缀（hash 路由），新版去掉该前缀改用干净的 history 路由；园区标识改由运行时配置承载。
- 访客模块全部页面 `isAuth: false`（访客无登录态），登录模块也是免认证；首页需要登录态——原型期用 mock 登录态。
- 合肥变体页面（telHefei/indexHefei/visitorInfoHefei/add-person-hefei）在新版收敛为同一路由 + 配置驱动分支（设计 §5）。
- 旧版访客路由清单：index、tel、visitorInfo、addPersonList、addPerson、addCarList、addCar、result、resultTruck、code、addAreaType、truck（+4 个合肥变体，已收敛）。

**通用约定：**
- 所有命令在 `yuto-park-h5/` 目录内执行（Task 1 创建它）。
- 每个 Task 结束必须 `pnpm check`（tsc + lint）通过后才 commit。
- 本仓库无遗留 git hooks，正常 `git commit` 即可。
- 原型页面只做三件事：渲染 mock 数据、表单本地状态、页面间导航。**不写任何真实请求、不写加密、不调真实微信 SDK。**

---

### Task 1: 脚手架与依赖

**Files:**
- Create: `yuto-park-h5/`（create-next-app 生成）
- Modify: `yuto-park-h5/package.json`、`yuto-park-h5/tsconfig.json`

- [ ] **Step 1: 生成项目**

在仓库根目录运行：

```bash
pnpm create next-app@latest yuto-park-h5 --ts --eslint --tailwind --app --src-dir --import-alias "@/*" --no-turbopack --use-pnpm
```

注意：本仓库（smart-h5）是独立 git 仓库，与旧版供应商仓库（`~/source/YUTO/yuto-smart/smart-h5`）完全隔离；旧仓库只作只读参考。在本仓库根目录运行上述命令创建 `yuto-park-h5/`。

- [ ] **Step 2: 安装运行时依赖**

```bash
cd yuto-park-h5
pnpm add antd-mobile zustand
pnpm add -D @playwright/test prettier
```

- [ ] **Step 3: 验证 antd-mobile 与 React 19 兼容**

把 `src/app/page.tsx` 整体替换为：

```tsx
'use client'
import { Button } from 'antd-mobile'

export default function Home() {
  return <Button color="primary">smart-h5 prototype</Button>
}
```

运行 `pnpm dev`，访问 http://localhost:3000，按钮正常渲染、控制台无 peer/hook 报错即通过。若 antd-mobile 与 React 19 出现不兼容报错，降级：`pnpm add react@18.3.1 react-dom@18.3.1 && pnpm add -D @types/react@18 @types/react-dom@18`，重新验证。

- [ ] **Step 4: 开启 TS 严格门禁**

`yuto-park-h5/tsconfig.json` 的 `compilerOptions` 中确认/添加：

```json
"strict": true,
"noUncheckedIndexedAccess": true
```

- [ ] **Step 5: 添加 check 脚本**

`yuto-park-h5/package.json` 的 scripts 中添加：

```json
"check": "tsc --noEmit && next lint",
"e2e": "playwright test"
```

运行 `pnpm check`，期望通过。

- [ ] **Step 6: Commit**

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): scaffold Next.js prototype skeleton"
```

---

### Task 2: 运行时租户配置层

**Files:**
- Create: `yuto-park-h5/public/config.js`
- Create: `yuto-park-h5/src/lib/config/tenant.ts`
- Modify: `yuto-park-h5/src/app/layout.tsx`

- [ ] **Step 1: 写运行时配置文件**

`yuto-park-h5/public/config.js`（部署期由 Docker entrypoint 按环境渲染；原型期写死许昌默认值）：

```js
window.__SMART_CONFIG__ = {
  tenant: 'xuchang',
  parkId: 5000021,
  wxAppId: 'wx_placeholder_appid',
  flows: { visitor: 'standard' } // 'standard' | 'hefei'
}
```

- [ ] **Step 2: 写配置读取模块**

`yuto-park-h5/src/lib/config/tenant.ts`：

```ts
export interface TenantConfig {
  tenant: string
  parkId: number
  wxAppId: string
  flows: { visitor: 'standard' | 'hefei' }
}

const DEFAULTS: TenantConfig = {
  tenant: 'xuchang',
  parkId: 5000021,
  wxAppId: 'wx_placeholder_appid',
  flows: { visitor: 'standard' }
}

declare global {
  interface Window {
    __SMART_CONFIG__?: Partial<TenantConfig>
  }
}

export function getTenantConfig(): TenantConfig {
  if (typeof window === 'undefined') return DEFAULTS
  const runtime = window.__SMART_CONFIG__
  if (!runtime) return DEFAULTS
  return { ...DEFAULTS, ...runtime, flows: { ...DEFAULTS.flows, ...runtime.flows } }
}
```

- [ ] **Step 3: 在根 layout 注入 config.js**

`yuto-park-h5/src/app/layout.tsx` 整体替换为：

```tsx
import type { Metadata, Viewport } from 'next'
import Script from 'next/script'
import './globals.css'

export const metadata: Metadata = { title: '智慧园区' }
export const viewport: Viewport = { width: 'device-width', initialScale: 1, maximumScale: 1 }

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="zh-CN">
      <body>
        <Script src="/config.js" strategy="beforeInteractive" />
        {children}
      </body>
    </html>
  )
}
```

- [ ] **Step 4: 验证**

`pnpm dev` 打开首页，浏览器 console 输入 `window.__SMART_CONFIG__`，应输出配置对象。`pnpm check` 通过。

- [ ] **Step 5: Commit**

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add runtime tenant config layer"
```

---

### Task 3: 移动端壳与导航组件

**Files:**
- Create: `yuto-park-h5/src/components/page-shell.tsx`
- Modify: `yuto-park-h5/src/app/globals.css`

- [ ] **Step 1: 全局样式**

`yuto-park-h5/src/app/globals.css` 末尾追加：

```css
:root {
  --adm-color-primary: #ff7d26; /* 裕同橙，对照旧版主色 */
}
body {
  background: #f5f6f8;
  max-width: 640px;
  margin: 0 auto;
}
```

- [ ] **Step 2: 页面壳组件**

`yuto-park-h5/src/components/page-shell.tsx`（统一 NavBar + 返回行为，替代旧版 Layout）：

```tsx
'use client'
import { NavBar } from 'antd-mobile'
import { useRouter } from 'next/navigation'

export function PageShell({
  title,
  showBack = true,
  children
}: {
  title: string
  showBack?: boolean
  children: React.ReactNode
}) {
  const router = useRouter()
  return (
    <div className="flex min-h-screen flex-col">
      <NavBar
        backIcon={showBack}
        onBack={showBack ? () => router.back() : undefined}
        className="bg-white"
      >
        {title}
      </NavBar>
      <main className="flex-1 p-3">{children}</main>
    </div>
  )
}
```

- [ ] **Step 3: 验证 + Commit**

`pnpm check` 通过后：

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add mobile page shell and theme"
```

---

### Task 4: Mock 数据层与认证 store

**Files:**
- Create: `yuto-park-h5/src/lib/mock/simulate.ts`
- Create: `yuto-park-h5/src/lib/mock/fixtures.ts`
- Create: `yuto-park-h5/src/lib/auth/store.ts`

- [ ] **Step 1: 模拟请求工具**

`yuto-park-h5/src/lib/mock/simulate.ts`：

```ts
/** 原型期统一的假请求：固定延迟，让 loading 态可见。开发阶段将被真实 API 层替换。 */
export function simulateRequest<T>(data: T, delayMs = 300): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(structuredClone(data)), delayMs))
}
```

- [ ] **Step 2: 业务 fixtures**

`yuto-park-h5/src/lib/mock/fixtures.ts`：

```ts
export interface Announcement {
  id: string
  title: string
  publishedAt: string
  content: string
}

export interface HomeMenuItem {
  key: string
  label: string
  href: string
}

export interface CompanionPerson {
  id: string
  name: string
  phone: string
  idCard: string
}

export interface VisitorCar {
  id: string
  plateNumber: string
  carType: string
}

export const announcements: Announcement[] = [
  { id: '1', title: '园区五一假期出入安排', publishedAt: '2026-04-28', content: '五一期间（5月1日—5月5日），访客通道开放时间调整为 8:00–18:00，请提前报备。' },
  { id: '2', title: '宿舍区消防演练通知', publishedAt: '2026-04-20', content: '本周五下午 15:00 将在生活区开展消防演练，请勿惊慌。' },
  { id: '3', title: '食堂菜价调整公告', publishedAt: '2026-04-12', content: '自下月起，二楼食堂部分菜品价格调整，详见食堂公示栏。' }
]

export const homeMenus: HomeMenuItem[] = [
  { key: 'visitor', label: '访客申请', href: '/visitor' },
  { key: 'check-in', label: '宿舍签到', href: '/check-in' },
  { key: 'dorm-exit', label: '离宿办理', href: '/dorm-exit' },
  { key: 'dorm-repairs', label: '维修报修', href: '/dorm-repairs' },
  { key: 'good-release', label: '物品放行', href: '/good-release' },
  { key: 'lock', label: '门禁密码', href: '/lock' }
]

export const companionPersons: CompanionPerson[] = [
  { id: 'p1', name: '张三', phone: '13800000001', idCard: '410***********0011' }
]

export const visitorCars: VisitorCar[] = [
  { id: 'c1', plateNumber: '豫A·12345', carType: '小型轿车' }
]

export const areaOptions = ['办公区', '生产一区', '生产二区', '生活区']

export const mockUser = { name: '王工', company: '裕同科技', phone: '13900000002' }
```

- [ ] **Step 3: 认证 store（原型期仅 mock 登录态）**

`yuto-park-h5/src/lib/auth/store.ts`：

```ts
import { create } from 'zustand'

interface AuthState {
  token: string | null
  userName: string | null
  loginAsMock: (userName: string) => void
  logout: () => void
}

/** 原型期：内存 mock 登录态。开发阶段替换为 localStorage(SMART_WEB_APP_TOKEN) + 微信 OAuth。 */
export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  userName: null,
  loginAsMock: (userName) => set({ token: 'mock-token', userName }),
  logout: () => set({ token: null, userName: null })
}))
```

- [ ] **Step 4: 验证 + Commit**

`pnpm check` 通过后：

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add mock data layer and auth store"
```

---

### Task 5: 登录模块（4 页）

**Files:**
- Create: `yuto-park-h5/src/app/login/page.tsx`（账号/短信登录）
- Create: `yuto-park-h5/src/app/login/wechat/page.tsx`（微信授权引导）
- Create: `yuto-park-h5/src/app/login/wechat/callback/page.tsx`（OAuth 回调，对应旧 `wechat/code`）
- Create: `yuto-park-h5/src/app/login/badge/page.tsx`（工牌登录，对应旧 `logon_badge`）

- [ ] **Step 1: 登录主页**

`yuto-park-h5/src/app/login/page.tsx`：

```tsx
'use client'
import { Button, Form, Input, Tabs, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { PageShell } from '@/components/page-shell'
import { useAuthStore } from '@/lib/auth/store'

export default function LoginPage() {
  const router = useRouter()
  const loginAsMock = useAuthStore((s) => s.loginAsMock)

  function submit(values: { username?: string; phone?: string }) {
    const name = values.username ?? values.phone ?? '用户'
    loginAsMock(name)
    Toast.show({ icon: 'success', content: '登录成功（原型 mock）' })
    router.push('/home')
  }

  return (
    <PageShell title="登录" showBack={false}>
      <Tabs>
        <Tabs.Tab title="密码登录" key="pwd">
          <Form onFinish={submit} footer={<Button block type="submit" color="primary">登录</Button>}>
            <Form.Item name="username" label="账号" rules={[{ required: true }]}>
              <Input placeholder="请输入账号" />
            </Form.Item>
            <Form.Item name="password" label="密码" rules={[{ required: true }]}>
              <Input type="password" placeholder="请输入密码" />
            </Form.Item>
          </Form>
        </Tabs.Tab>
        <Tabs.Tab title="短信登录" key="sms">
          <Form onFinish={submit} footer={<Button block type="submit" color="primary">登录</Button>}>
            <Form.Item name="phone" label="手机号" rules={[{ required: true }]}>
              <Input placeholder="请输入手机号" />
            </Form.Item>
            <Form.Item name="smsCode" label="验证码" rules={[{ required: true }]}
              extra={<Button size="small" onClick={() => Toast.show('验证码已发送（mock）')}>获取验证码</Button>}>
              <Input placeholder="请输入验证码" />
            </Form.Item>
          </Form>
        </Tabs.Tab>
      </Tabs>
      <div className="mt-6 flex justify-around text-sm text-gray-500">
        <Link href="/login/wechat">微信授权登录</Link>
        <Link href="/login/badge">工牌登录</Link>
      </div>
    </PageShell>
  )
}
```

- [ ] **Step 2: 微信授权引导页**

`yuto-park-h5/src/app/login/wechat/page.tsx`：

```tsx
'use client'
import { Button, Result } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { getTenantConfig } from '@/lib/config/tenant'

export default function WechatLoginPage() {
  const router = useRouter()
  return (
    <PageShell title="微信登录">
      <Result
        status="waiting"
        title="跳转微信授权"
        description={`将以 snsapi_base 静默授权跳转（appid: ${getTenantConfig().wxAppId}）。原型期直接模拟回调。`}
      />
      <Button block color="primary" onClick={() => router.push('/login/wechat/callback?code=mock-code')}>
        模拟微信授权回调
      </Button>
    </PageShell>
  )
}
```

- [ ] **Step 3: OAuth 回调页**

`yuto-park-h5/src/app/login/wechat/callback/page.tsx`：

```tsx
'use client'
import { Suspense, useEffect } from 'react'
import { SpinLoading } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { useAuthStore } from '@/lib/auth/store'

function CallbackInner() {
  const router = useRouter()
  const code = useSearchParams().get('code')
  const loginAsMock = useAuthStore((s) => s.loginAsMock)

  useEffect(() => {
    if (!code) {
      router.replace('/login')
      return
    }
    // 开发阶段：POST /auth/wx/public/token (Basic) 换取 token
    loginAsMock('微信用户')
    router.replace('/home')
  }, [code, loginAsMock, router])

  return <div className="flex h-screen items-center justify-center"><SpinLoading /></div>
}

export default function WechatCallbackPage() {
  return <Suspense><CallbackInner /></Suspense>
}
```

注意：`useSearchParams` 必须包在 `<Suspense>` 内，否则 `next build` 报错。

- [ ] **Step 4: 工牌登录页**

`yuto-park-h5/src/app/login/badge/page.tsx`：

```tsx
'use client'
import { Button, Form, Input, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { useAuthStore } from '@/lib/auth/store'

export default function BadgeLoginPage() {
  const router = useRouter()
  const loginAsMock = useAuthStore((s) => s.loginAsMock)

  return (
    <PageShell title="工牌登录">
      <Form
        onFinish={(v: { badgeNo: string }) => {
          loginAsMock(`工牌${v.badgeNo}`)
          Toast.show({ icon: 'success', content: '登录成功（原型 mock）' })
          router.push('/home')
        }}
        footer={<Button block type="submit" color="primary">登录</Button>}
      >
        <Form.Item name="badgeNo" label="工牌号" rules={[{ required: true }]}>
          <Input placeholder="请输入工牌号" />
        </Form.Item>
      </Form>
    </PageShell>
  )
}
```

- [ ] **Step 5: 验证**

`pnpm dev`：/login 两种 tab 提交均跳 /home（404 正常，Task 6 补）；/login/wechat 模拟回调跳 /home；/login/badge 提交跳 /home。`pnpm check` 通过。

- [ ] **Step 6: Commit**

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add login module prototype pages"
```

---

### Task 6: 首页模块（3 页）

**Files:**
- Create: `yuto-park-h5/src/app/home/page.tsx`
- Create: `yuto-park-h5/src/app/home/bbs/page.tsx`
- Create: `yuto-park-h5/src/app/home/bbs/[id]/page.tsx`
- Modify: `yuto-park-h5/src/app/page.tsx`（根路径重定向）

- [ ] **Step 1: 根路径重定向**

`yuto-park-h5/src/app/page.tsx` 整体替换为：

```tsx
import { redirect } from 'next/navigation'

export default function Index() {
  redirect('/home')
}
```

- [ ] **Step 2: 首页**

`yuto-park-h5/src/app/home/page.tsx`：

```tsx
'use client'
import { Grid, List, NoticeBar } from 'antd-mobile'
import Link from 'next/link'
import { PageShell } from '@/components/page-shell'
import { announcements, homeMenus } from '@/lib/mock/fixtures'
import { useAuthStore } from '@/lib/auth/store'

export default function HomePage() {
  const userName = useAuthStore((s) => s.userName)
  const latest = announcements[0]

  return (
    <PageShell title="智慧园区" showBack={false}>
      <div className="mb-3 rounded-lg bg-white p-4">
        {userName ? `你好，${userName}` : <Link href="/login" className="text-orange-500">未登录，去登录</Link>}
      </div>
      {latest && (
        <Link href={`/home/bbs/${latest.id}`}>
          <NoticeBar content={latest.title} color="alert" className="mb-3" />
        </Link>
      )}
      <Grid columns={3} gap={8} className="mb-3">
        {homeMenus.map((m) => (
          <Grid.Item key={m.key}>
            <Link href={m.href} className="block rounded-lg bg-white py-5 text-center text-sm">
              {m.label}
            </Link>
          </Grid.Item>
        ))}
      </Grid>
      <List header="园区公告" mode="card">
        {announcements.map((a) => (
          <Link key={a.id} href={`/home/bbs/${a.id}`}>
            <List.Item description={a.publishedAt}>{a.title}</List.Item>
          </Link>
        ))}
      </List>
      <div className="mt-2 text-center text-xs text-gray-400">
        <Link href="/home/bbs">查看全部公告</Link>
      </div>
    </PageShell>
  )
}
```

- [ ] **Step 3: 公告列表页**

`yuto-park-h5/src/app/home/bbs/page.tsx`：

```tsx
'use client'
import { List } from 'antd-mobile'
import Link from 'next/link'
import { PageShell } from '@/components/page-shell'
import { announcements } from '@/lib/mock/fixtures'

export default function BbsListPage() {
  return (
    <PageShell title="公告列表">
      <List mode="card">
        {announcements.map((a) => (
          <Link key={a.id} href={`/home/bbs/${a.id}`}>
            <List.Item description={a.publishedAt}>{a.title}</List.Item>
          </Link>
        ))}
      </List>
    </PageShell>
  )
}
```

- [ ] **Step 4: 公告详情页**

`yuto-park-h5/src/app/home/bbs/[id]/page.tsx`：

```tsx
'use client'
import { ErrorBlock } from 'antd-mobile'
import { use } from 'react'
import { PageShell } from '@/components/page-shell'
import { announcements } from '@/lib/mock/fixtures'

export default function BbsDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const item = announcements.find((a) => a.id === id)

  return (
    <PageShell title="公告详情">
      {item ? (
        <article className="rounded-lg bg-white p-4">
          <h1 className="mb-1 text-lg font-semibold">{item.title}</h1>
          <p className="mb-4 text-xs text-gray-400">{item.publishedAt}</p>
          <p className="text-sm leading-6">{item.content}</p>
        </article>
      ) : (
        <ErrorBlock status="empty" description="公告不存在" />
      )}
    </PageShell>
  )
}
```

注意：Next.js 15 中页面 `params` 是 Promise，客户端组件用 `use()` 解包。

- [ ] **Step 5: 验证 + Commit**

`pnpm dev` 走通 / → /home → 公告列表 → 详情 → 返回。`pnpm check` 通过。

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add home module prototype pages"
```

---

### Task 7: 访客模块 — 流程状态与入口/验证页

访客申请是跨页面流程（入口 → 手机验证 → 访客信息 → 随行人/车辆/区域 → 结果/二维码），原型期用一个流程 store 串联。

**Files:**
- Create: `yuto-park-h5/src/features/visitor/flow-store.ts`
- Create: `yuto-park-h5/src/app/visitor/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/verify/page.tsx`

- [ ] **Step 1: 访客流程 store**

`yuto-park-h5/src/features/visitor/flow-store.ts`：

```ts
import { create } from 'zustand'
import type { CompanionPerson, VisitorCar } from '@/lib/mock/fixtures'

export interface VisitorDraft {
  phone: string
  name: string
  idCard: string
  company: string
  visitedPerson: string
  reason: string
  areas: string[]
  persons: CompanionPerson[]
  cars: VisitorCar[]
}

const EMPTY: VisitorDraft = {
  phone: '', name: '', idCard: '', company: '',
  visitedPerson: '', reason: '', areas: [], persons: [], cars: []
}

interface VisitorFlowState {
  draft: VisitorDraft
  patch: (part: Partial<VisitorDraft>) => void
  addPerson: (p: CompanionPerson) => void
  removePerson: (id: string) => void
  addCar: (c: VisitorCar) => void
  removeCar: (id: string) => void
  reset: () => void
}

export const useVisitorFlow = create<VisitorFlowState>((set) => ({
  draft: EMPTY,
  patch: (part) => set((s) => ({ draft: { ...s.draft, ...part } })),
  addPerson: (p) => set((s) => ({ draft: { ...s.draft, persons: [...s.draft.persons, p] } })),
  removePerson: (id) => set((s) => ({ draft: { ...s.draft, persons: s.draft.persons.filter((x) => x.id !== id) } })),
  addCar: (c) => set((s) => ({ draft: { ...s.draft, cars: [...s.draft.cars, c] } })),
  removeCar: (id) => set((s) => ({ draft: { ...s.draft, cars: s.draft.cars.filter((x) => x.id !== id) } })),
  reset: () => set({ draft: EMPTY })
}))
```

- [ ] **Step 2: 访客入口页（配置驱动许昌/合肥分支）**

`yuto-park-h5/src/app/visitor/page.tsx`：

```tsx
'use client'
import { Button, Card } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { getTenantConfig } from '@/lib/config/tenant'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function VisitorEntryPage() {
  const router = useRouter()
  const reset = useVisitorFlow((s) => s.reset)
  const flow = getTenantConfig().flows.visitor

  function start() {
    reset()
    router.push('/visitor/verify')
  }

  return (
    <PageShell title="入厂申请" showBack={false}>
      <Card title={`访客入厂须知（${flow === 'hefei' ? '合肥流程' : '标准流程'}）`}>
        <p className="mb-4 text-sm leading-6 text-gray-600">
          请如实填写访客信息。入厂需经被访人确认，凭二维码通行。
        </p>
        <Button block color="primary" onClick={start}>开始申请</Button>
        <Button block fill="outline" className="mt-3" onClick={() => router.push('/visitor/truck')}>
          货车预约
        </Button>
      </Card>
    </PageShell>
  )
}
```

- [ ] **Step 3: 手机验证页（standard/hefei 同路由分支）**

`yuto-park-h5/src/app/visitor/verify/page.tsx`：

```tsx
'use client'
import { Button, Form, Input, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { getTenantConfig } from '@/lib/config/tenant'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function VisitorVerifyPage() {
  const router = useRouter()
  const patch = useVisitorFlow((s) => s.patch)
  const isHefei = getTenantConfig().flows.visitor === 'hefei'

  function submit(values: { phone: string }) {
    patch({ phone: values.phone })
    router.push('/visitor/info')
  }

  return (
    <PageShell title="手机验证">
      <Form onFinish={submit} footer={<Button block type="submit" color="primary">下一步</Button>}>
        <Form.Item name="phone" label="手机号" rules={[{ required: true, pattern: /^1\d{10}$/, message: '请输入 11 位手机号' }]}>
          <Input placeholder="请输入手机号" />
        </Form.Item>
        <Form.Item
          name="smsCode"
          label="验证码"
          rules={[{ required: true }]}
          extra={<Button size="small" onClick={() => Toast.show('验证码已发送（mock）')}>获取验证码</Button>}
        >
          <Input placeholder="请输入验证码" />
        </Form.Item>
        {isHefei && (
          <Form.Item name="visitDate" label="到访日期" rules={[{ required: true }]}>
            <Input placeholder="合肥流程需预约到访日期，如 2026-06-15" />
          </Form.Item>
        )}
      </Form>
    </PageShell>
  )
}
```

- [ ] **Step 4: 验证 + Commit**

`pnpm dev`：/visitor → 开始申请 → /visitor/verify 表单校验生效。`pnpm check` 通过。

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add visitor entry and phone verification"
```

---

### Task 8: 访客模块 — 信息表单与随行人/车辆/区域

**Files:**
- Create: `yuto-park-h5/src/app/visitor/info/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/persons/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/persons/add/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/cars/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/cars/add/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/area/page.tsx`

- [ ] **Step 1: 访客信息主表单**

`yuto-park-h5/src/app/visitor/info/page.tsx`：

```tsx
'use client'
import { Button, Form, Input, List } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { getTenantConfig } from '@/lib/config/tenant'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function VisitorInfoPage() {
  const router = useRouter()
  const { draft, patch } = useVisitorFlow()
  const isHefei = getTenantConfig().flows.visitor === 'hefei'

  function submit(values: { name: string; idCard: string; company: string; visitedPerson: string; reason: string }) {
    patch(values)
    router.push('/visitor/result')
  }

  return (
    <PageShell title="访客信息">
      <Form
        onFinish={submit}
        initialValues={{ name: draft.name, idCard: draft.idCard, company: draft.company, visitedPerson: draft.visitedPerson, reason: draft.reason }}
        footer={<Button block type="submit" color="primary">提交申请</Button>}
      >
        <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
          <Input placeholder="请输入姓名" />
        </Form.Item>
        <Form.Item name="idCard" label="身份证号" rules={[{ required: true }]}>
          <Input placeholder="开发阶段此字段走 AES 加密传输" />
        </Form.Item>
        <Form.Item name="company" label="来访单位" rules={[{ required: !isHefei }]}>
          <Input placeholder="请输入单位名称" />
        </Form.Item>
        <Form.Item name="visitedPerson" label="被访人" rules={[{ required: true }]}>
          <Input placeholder="请输入被访人姓名" />
        </Form.Item>
        <Form.Item name="reason" label="来访事由" rules={[{ required: true }]}>
          <Input placeholder="请输入来访事由" />
        </Form.Item>
      </Form>
      <List mode="card" header="附加信息">
        <List.Item extra={`${draft.persons.length} 人`} onClick={() => router.push('/visitor/persons')}>随行人员</List.Item>
        <List.Item extra={`${draft.cars.length} 辆`} onClick={() => router.push('/visitor/cars')}>来访车辆</List.Item>
        <List.Item extra={draft.areas.length ? draft.areas.join('、') : '未选择'} onClick={() => router.push('/visitor/area')}>
          授权进入区域
        </List.Item>
      </List>
    </PageShell>
  )
}
```

- [ ] **Step 2: 随行人员列表页**

`yuto-park-h5/src/app/visitor/persons/page.tsx`：

```tsx
'use client'
import { Button, ErrorBlock, List, SwipeAction } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function PersonsPage() {
  const router = useRouter()
  const { draft, removePerson } = useVisitorFlow()

  return (
    <PageShell title="随行人员">
      {draft.persons.length === 0 ? (
        <ErrorBlock status="empty" description="暂无随行人员" />
      ) : (
        <List mode="card">
          {draft.persons.map((p) => (
            <SwipeAction key={p.id} rightActions={[{ key: 'del', text: '删除', color: 'danger', onClick: () => removePerson(p.id) }]}>
              <List.Item description={p.phone}>{p.name}</List.Item>
            </SwipeAction>
          ))}
        </List>
      )}
      <Button block color="primary" className="mt-4" onClick={() => router.push('/visitor/persons/add')}>
        添加随行人员
      </Button>
    </PageShell>
  )
}
```

- [ ] **Step 3: 添加随行人员页**

`yuto-park-h5/src/app/visitor/persons/add/page.tsx`：

```tsx
'use client'
import { Button, Form, Input } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function AddPersonPage() {
  const router = useRouter()
  const addPerson = useVisitorFlow((s) => s.addPerson)

  function submit(values: { name: string; phone: string; idCard: string }) {
    addPerson({ id: crypto.randomUUID(), ...values })
    router.back()
  }

  return (
    <PageShell title="添加随行人员">
      <Form onFinish={submit} footer={<Button block type="submit" color="primary">保存</Button>}>
        <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
          <Input placeholder="请输入姓名" />
        </Form.Item>
        <Form.Item name="phone" label="手机号" rules={[{ required: true, pattern: /^1\d{10}$/, message: '请输入 11 位手机号' }]}>
          <Input placeholder="请输入手机号" />
        </Form.Item>
        <Form.Item name="idCard" label="身份证号" rules={[{ required: true }]}>
          <Input placeholder="请输入身份证号" />
        </Form.Item>
      </Form>
    </PageShell>
  )
}
```

- [ ] **Step 4: 车辆列表页**

`yuto-park-h5/src/app/visitor/cars/page.tsx`：

```tsx
'use client'
import { Button, ErrorBlock, List, SwipeAction } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function CarsPage() {
  const router = useRouter()
  const { draft, removeCar } = useVisitorFlow()

  return (
    <PageShell title="来访车辆">
      {draft.cars.length === 0 ? (
        <ErrorBlock status="empty" description="暂无车辆" />
      ) : (
        <List mode="card">
          {draft.cars.map((c) => (
            <SwipeAction key={c.id} rightActions={[{ key: 'del', text: '删除', color: 'danger', onClick: () => removeCar(c.id) }]}>
              <List.Item description={c.carType}>{c.plateNumber}</List.Item>
            </SwipeAction>
          ))}
        </List>
      )}
      <Button block color="primary" className="mt-4" onClick={() => router.push('/visitor/cars/add')}>
        添加车辆
      </Button>
    </PageShell>
  )
}
```

- [ ] **Step 5: 添加车辆页**

`yuto-park-h5/src/app/visitor/cars/add/page.tsx`：

```tsx
'use client'
import { Button, Form, Input } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function AddCarPage() {
  const router = useRouter()
  const addCar = useVisitorFlow((s) => s.addCar)

  function submit(values: { plateNumber: string; carType: string }) {
    addCar({ id: crypto.randomUUID(), ...values })
    router.back()
  }

  return (
    <PageShell title="添加车辆">
      <Form onFinish={submit} footer={<Button block type="submit" color="primary">保存</Button>}>
        <Form.Item name="plateNumber" label="车牌号" rules={[{ required: true }]}>
          <Input placeholder="如 豫A·12345" />
        </Form.Item>
        <Form.Item name="carType" label="车辆类型" rules={[{ required: true }]}>
          <Input placeholder="如 小型轿车 / 货车" />
        </Form.Item>
      </Form>
    </PageShell>
  )
}
```

- [ ] **Step 6: 授权区域选择页**

`yuto-park-h5/src/app/visitor/area/page.tsx`：

```tsx
'use client'
import { Button, Checkbox, Space } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { areaOptions } from '@/lib/mock/fixtures'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function AreaPage() {
  const router = useRouter()
  const { draft, patch } = useVisitorFlow()
  const [selected, setSelected] = useState<string[]>(draft.areas)

  return (
    <PageShell title="授权进入区域">
      <div className="rounded-lg bg-white p-4">
        <Checkbox.Group value={selected} onChange={(v) => setSelected(v as string[])}>
          <Space direction="vertical" block>
            {areaOptions.map((a) => <Checkbox key={a} value={a}>{a}</Checkbox>)}
          </Space>
        </Checkbox.Group>
      </div>
      <Button block color="primary" className="mt-4" onClick={() => { patch({ areas: selected }); router.back() }}>
        确定
      </Button>
    </PageShell>
  )
}
```

- [ ] **Step 7: 验证 + Commit**

`pnpm dev` 走通：info → 随行人添加/删除 → 车辆添加 → 区域多选 → 回 info 时 List 汇总数字正确。`pnpm check` 通过。

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add visitor info form with persons/cars/areas"
```

---

### Task 9: 访客模块 — 结果页、二维码、货车预约

**Files:**
- Create: `yuto-park-h5/src/app/visitor/result/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/code/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/truck/page.tsx`
- Create: `yuto-park-h5/src/app/visitor/truck/result/page.tsx`（对应旧 resultTruck）

- [ ] **Step 1: 提交结果页**

`yuto-park-h5/src/app/visitor/result/page.tsx`：

```tsx
'use client'
import { Button, Result } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function VisitorResultPage() {
  const router = useRouter()
  const draft = useVisitorFlow((s) => s.draft)

  return (
    <PageShell title="提交成功" showBack={false}>
      <Result
        status="success"
        title="申请已提交"
        description={`${draft.name || '访客'}，您的入厂申请已提交，待被访人 ${draft.visitedPerson || '—'} 确认。`}
      />
      <Button block color="primary" onClick={() => router.push('/visitor/code')}>查看入厂二维码</Button>
      <Button block fill="outline" className="mt-3" onClick={() => router.push('/visitor')}>返回首页</Button>
    </PageShell>
  )
}
```

- [ ] **Step 2: 二维码页（原型期用占位图形）**

`yuto-park-h5/src/app/visitor/code/page.tsx`：

```tsx
'use client'
import { PageShell } from '@/components/page-shell'
import { useVisitorFlow } from '@/features/visitor/flow-store'

export default function VisitorCodePage() {
  const draft = useVisitorFlow((s) => s.draft)

  return (
    <PageShell title="二维码信息">
      <div className="flex flex-col items-center rounded-lg bg-white p-6">
        {/* 原型占位：开发阶段接 /platform 返回的真实通行码并渲染二维码 */}
        <div className="grid h-48 w-48 grid-cols-8 gap-0.5 bg-white p-2 shadow-inner">
          {Array.from({ length: 64 }, (_, i) => (
            <div key={i} className={(i * 7) % 3 === 0 ? 'bg-black' : 'bg-white'} />
          ))}
        </div>
        <p className="mt-4 text-sm text-gray-600">{draft.name || '访客'} · 凭此码入厂通行</p>
        <p className="text-xs text-gray-400">有效期至当日 23:59（mock）</p>
      </div>
    </PageShell>
  )
}
```

- [ ] **Step 3: 货车预约页**

`yuto-park-h5/src/app/visitor/truck/page.tsx`：

```tsx
'use client'
import { Button, Form, Input } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'

export default function TruckPage() {
  const router = useRouter()

  return (
    <PageShell title="货车预约">
      <Form onFinish={() => router.push('/visitor/truck/result')} footer={<Button block type="submit" color="primary">提交预约</Button>}>
        <Form.Item name="driverName" label="司机姓名" rules={[{ required: true }]}>
          <Input placeholder="请输入司机姓名" />
        </Form.Item>
        <Form.Item name="phone" label="手机号" rules={[{ required: true, pattern: /^1\d{10}$/, message: '请输入 11 位手机号' }]}>
          <Input placeholder="请输入手机号" />
        </Form.Item>
        <Form.Item name="plateNumber" label="车牌号" rules={[{ required: true }]}>
          <Input placeholder="如 豫A·12345" />
        </Form.Item>
        <Form.Item name="cargo" label="货物说明" rules={[{ required: true }]}>
          <Input placeholder="请输入货物说明" />
        </Form.Item>
      </Form>
    </PageShell>
  )
}
```

- [ ] **Step 4: 货车预约结果页**

`yuto-park-h5/src/app/visitor/truck/result/page.tsx`：

```tsx
'use client'
import { Button, Result } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { PageShell } from '@/components/page-shell'

export default function TruckResultPage() {
  const router = useRouter()
  return (
    <PageShell title="提交成功" showBack={false}>
      <Result status="success" title="货车预约已提交" description="请等待园区确认排队叫号。" />
      <Button block color="primary" onClick={() => router.push('/visitor')}>返回</Button>
    </PageShell>
  )
}
```

- [ ] **Step 5: 验证 + Commit**

`pnpm dev` 全流程走通：/visitor → verify → info → 提交 → result → code；/visitor → truck → result。`pnpm check` 通过。

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add visitor result, QR code and truck booking pages"
```

---

### Task 10: 微信 Spike 页面骨架

**Files:**
- Create: `yuto-park-h5/src/lib/wechat/jssdk.ts`
- Create: `yuto-park-h5/src/app/spike/wechat/page.tsx`

- [ ] **Step 1: JSSDK hook 骨架**

`yuto-park-h5/src/lib/wechat/jssdk.ts`（原型期 stub；真机 Spike 时替换 sign 的 mock 为真实 `GET /app/wechat/sign`）：

```ts
'use client'
import { useCallback, useState } from 'react'
import { simulateRequest } from '@/lib/mock/simulate'

export interface WxSignature {
  appId: string
  timestamp: number
  nonceStr: string
  signature: string
}

/**
 * 微信 JSSDK 封装骨架。
 * 关键约束（真机 Spike 必须验证）：iOS 微信内 SPA 必须用「首次进入页面的 landing URL」
 * 请求签名，而非当前路由 URL。landing URL 在 app 首次加载时固化。
 */
const landingUrl = typeof window !== 'undefined' ? window.location.href.split('#')[0] : ''

export function useWxJsSdk() {
  const [ready, setReady] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const init = useCallback(async () => {
    try {
      // 真机 Spike：替换为 GET /app/wechat/sign?url=encodeURIComponent(landingUrl)
      const sign = await simulateRequest<WxSignature>({
        appId: 'wx_placeholder_appid',
        timestamp: 1718000000,
        nonceStr: 'mock-nonce',
        signature: 'mock-signature'
      })
      // 真机 Spike：import('weixin-js-sdk') 后 wx.config({...sign, jsApiList: ['scanQRCode']})
      void sign
      setReady(true)
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    }
  }, [])

  const scanQRCode = useCallback(async (): Promise<string> => {
    if (!ready) throw new Error('JSSDK 未初始化')
    // 真机 Spike：wx.scanQRCode({ needResult: 1 }) 的 promise 封装
    return simulateRequest('MOCK-QR-RESULT-123456')
  }, [ready])

  return { ready, error, init, scanQRCode, landingUrl }
}
```

- [ ] **Step 2: Spike 验证页**

`yuto-park-h5/src/app/spike/wechat/page.tsx`：

```tsx
'use client'
import { Button, List, Tag, Toast } from 'antd-mobile'
import { useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useWxJsSdk } from '@/lib/wechat/jssdk'

export default function WechatSpikePage() {
  const { ready, error, init, scanQRCode, landingUrl } = useWxJsSdk()
  const [scanResult, setScanResult] = useState('')

  return (
    <PageShell title="微信能力 Spike">
      <List mode="card" header="真机验证清单（必须在微信内打开）">
        <List.Item extra={ready ? <Tag color="success">已就绪</Tag> : <Tag>未初始化</Tag>}>
          1. JSSDK 签名 + wx.config
        </List.Item>
        <List.Item description={landingUrl || '—'}>2. landing URL（iOS 签名用）</List.Item>
        <List.Item extra={scanResult || '—'}>3. scanQRCode 结果</List.Item>
        {error && <List.Item><span className="text-red-500">{error}</span></List.Item>}
      </List>
      <Button block color="primary" className="mt-4" onClick={() => init()}>初始化 JSSDK</Button>
      <Button
        block
        className="mt-3"
        onClick={async () => {
          try {
            setScanResult(await scanQRCode())
          } catch (e) {
            Toast.show(String(e))
          }
        }}
      >
        扫一扫
      </Button>
      <p className="mt-4 text-xs leading-5 text-gray-400">
        OAuth 验证路径：微信内访问本页 → 跳 /login/wechat → 授权回调 /login/wechat/callback。
        真机 Spike 需要：公众号后台配置 JS 安全域名与网页授权域名指向新前端域名。
      </p>
    </PageShell>
  )
}
```

- [ ] **Step 3: 验证 + Commit**

`pnpm dev`：/spike/wechat 初始化后状态变「已就绪」，扫一扫返回 mock 结果。`pnpm check` 通过。

```bash
git add yuto-park-h5 && git commit --no-verify -m "feat(yuto-park-h5): add wechat jssdk spike skeleton"
```

---

### Task 11: Playwright E2E — 原型导航流回归

**Files:**
- Create: `yuto-park-h5/playwright.config.ts`
- Create: `yuto-park-h5/e2e/prototype-flows.spec.ts`

- [ ] **Step 1: Playwright 配置**

```bash
pnpm exec playwright install chromium
```

`yuto-park-h5/playwright.config.ts`：

```ts
import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  use: { baseURL: 'http://localhost:3100', ...devices['iPhone 13'] },
  webServer: {
    command: 'pnpm dev --port 3100',
    url: 'http://localhost:3100',
    reuseExistingServer: !process.env.CI
  }
})
```

- [ ] **Step 2: 写导航流测试**

`yuto-park-h5/e2e/prototype-flows.spec.ts`：

```ts
import { expect, test } from '@playwright/test'

test('登录 → 首页 → 公告详情', async ({ page }) => {
  await page.goto('/login')
  await page.getByPlaceholder('请输入账号').fill('demo')
  await page.getByPlaceholder('请输入密码').fill('demo123')
  await page.getByRole('button', { name: '登录' }).click()
  await expect(page).toHaveURL(/\/home/)
  await expect(page.getByText('你好，demo')).toBeVisible()
  await page.getByText('园区五一假期出入安排').first().click()
  await expect(page).toHaveURL(/\/home\/bbs\/1/)
  await expect(page.getByText('访客通道开放时间')).toBeVisible()
})

test('访客申请全流程（标准）', async ({ page }) => {
  await page.goto('/visitor')
  await page.getByRole('button', { name: '开始申请' }).click()
  await page.getByPlaceholder('请输入手机号').fill('13800001111')
  await page.getByPlaceholder('请输入验证码').fill('1234')
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page).toHaveURL(/\/visitor\/info/)

  await page.getByText('随行人员').click()
  await page.getByRole('button', { name: '添加随行人员' }).click()
  await page.getByPlaceholder('请输入姓名').fill('李四')
  await page.getByPlaceholder('请输入手机号').fill('13800002222')
  await page.getByPlaceholder('请输入身份证号').fill('410000199001010011')
  await page.getByRole('button', { name: '保存' }).click()
  await expect(page.getByText('李四')).toBeVisible()
  await page.goBack()

  await page.getByPlaceholder('请输入姓名').fill('王五')
  await page.getByPlaceholder('开发阶段此字段走 AES 加密传输').fill('410000199001010022')
  await page.getByPlaceholder('请输入单位名称').fill('测试公司')
  await page.getByPlaceholder('请输入被访人姓名').fill('赵经理')
  await page.getByPlaceholder('请输入来访事由').fill('商务洽谈')
  await page.getByRole('button', { name: '提交申请' }).click()
  await expect(page).toHaveURL(/\/visitor\/result/)
  await page.getByRole('button', { name: '查看入厂二维码' }).click()
  await expect(page.getByText('凭此码入厂通行')).toBeVisible()
})

test('货车预约流程', async ({ page }) => {
  await page.goto('/visitor/truck')
  await page.getByPlaceholder('请输入司机姓名').fill('老司机')
  await page.getByPlaceholder('请输入手机号').fill('13800003333')
  await page.getByPlaceholder('如 豫A·12345').fill('豫A·88888')
  await page.getByPlaceholder('请输入货物说明').fill('包装纸箱')
  await page.getByRole('button', { name: '提交预约' }).click()
  await expect(page.getByText('货车预约已提交')).toBeVisible()
})
```

- [ ] **Step 3: 运行测试**

```bash
pnpm e2e
```

期望：3 个测试全部 PASS。若选择器失配，以实际渲染为准修正测试（不得弱化断言）。

- [ ] **Step 4: Commit**

```bash
git add yuto-park-h5 && git commit --no-verify -m "test(yuto-park-h5): add playwright e2e for prototype flows"
```

---

### Task 12: 构建验证、README 与原型评审材料

**Files:**
- Create: `yuto-park-h5/README.md`

- [ ] **Step 1: 生产构建验证**

```bash
pnpm build
```

期望：构建成功，无 type error；所有路由出现在输出清单中。

- [ ] **Step 2: 写 README**

`yuto-park-h5/README.md`：

```markdown
# smart-h5 Next.js 重写（原型阶段）

商用化净室重写。设计文档见 `../docs/superpowers/specs/2026-06-11-nextjs-rewrite-architecture.md`。

## 运行

pnpm install
pnpm dev          # http://localhost:3000
pnpm check        # tsc + lint（提交前必过）
pnpm e2e          # Playwright 导航流回归
pnpm build        # 生产构建

## 当前状态：原型（mock 数据）

- 已实现：登录(4页) / 首页(3页) / 访客模块(12 路由，许昌/合肥配置分支) / 微信 Spike 骨架
- 全部数据为 mock，不调真实后端
- 租户配置：public/config.js（window.__SMART_CONFIG__）
- 切换合肥流程：把 config.js 中 flows.visitor 改为 'hefei'

## 原型评审走查路径

1. /login → 密码登录 → /home → 公告列表/详情
2. /visitor → 手机验证 → 访客信息 → 随行人/车辆/区域 → 提交 → 二维码
3. /visitor/truck → 货车预约
4. /spike/wechat → 微信能力清单（真机验证需公众号域名配置）

## 下一阶段（开发期）入口

- mock 替换：lib/mock/* → lib/api/ 兼容层（设计 §3 的 10 条约定）
- 登录态：lib/auth/store.ts 接 localStorage(SMART_WEB_APP_TOKEN) + 微信 OAuth
- 微信：lib/wechat/jssdk.ts 接真实 /app/wechat/sign + weixin-js-sdk
```

- [ ] **Step 3: 最终检查 + Commit**

```bash
pnpm check && pnpm e2e
git add yuto-park-h5 && git commit --no-verify -m "docs(yuto-park-h5): add prototype README and review guide"
```

---

## 完成定义（原型阶段出口标准）

1. `pnpm check`、`pnpm build`、`pnpm e2e` 全绿。
2. 19 个原型页面可点击走通三条评审路径（README 走查路径）。
3. 把 `public/config.js` 的 `flows.visitor` 改为 `'hefei'`，verify/info 页出现合肥分支差异。
4. 旅途完成原型评审 → 决定进入开发阶段（mock → 真实 API 替换 + 真机微信 Spike）。
