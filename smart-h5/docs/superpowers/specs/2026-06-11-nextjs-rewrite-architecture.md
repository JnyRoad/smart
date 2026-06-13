# smart-h5 商用化重写 — Next.js 架构设计

日期：2026-06-11
前置文档：[2026-06-11-nextjs-migration-assessment.md](./2026-06-11-nextjs-migration-assessment.md)
状态：待用户评审

## 0. 决策背景与范围

- **目标**：净室重写一套可商用的 H5 前端，替换供应商的 Vue 2 代码。不复制供应商任何源码；业务流程、字段、API 契约作为事实参考。
- **范围**：仅前端。后端（smart-gateway 及其后服务）暂时复用，新前端必须与现有接口 **100% 兼容**（静态枚举共 87 条、实际在用 73 条，见 [2026-06-11-api-coverage.md](./2026-06-11-api-coverage.md)）；后端替换是后续独立项目。
- **流程约定**：架构设计（本文档）→ 原型 → 代码开发。本阶段不写产品源码。
- **仓库布局**：本项目是独立 git 仓库（`~/source/YUTO/yuto-smart/smart-h5`），与旧版供应商仓库（`~/source/YUTO/yuto-smart/smart-h5`）完全隔离，不存在误合并风险。旧仓库只读参考：核对业务流程/字段/API 契约用，禁止复制源码。新版商用代码在本仓库 `yuto-park-h5/` 目录。
- **开发模式**：AI 主导开发，公司内部无 React/Vue 3 技术储备 → 质量护栏必须由工具链强制，不依赖人工 review。

## 1. 技术选型

| 层 | 选型 | 理由 |
|----|------|------|
| 框架 | Next.js 15+（App Router）+ React 19 | AI 语料最丰富；商用产品后续可扩展官网/SaaS 管理端；App Router 是长期方向 |
| 语言 | TypeScript（strict 模式） | AI 开发无人工 review，类型系统是第一道护栏 |
| UI 组件 | antd-mobile 5 + Tailwind CSS | antd-mobile 覆盖 cube-ui 的全部交互形态（表单/弹层/选择器/下拉刷新）；Tailwind 做布局与定制 |
| 服务端状态 | TanStack Query 5 | 替代手写 loading/防重复提交/缓存；接口数据全部走它 |
| 客户端状态 | Zustand | 仅存认证态 + 园区配置 + 用户信息，体量小，替代 Vuex |
| HTTP | 基于 fetch 的自研轻封装（~200 行） | 必须精确复刻现有 8 条隐性约定（见 §3），现成库反而碍事 |
| 表单 | react-hook-form + zod | 替代 @tce/tce-form 体系；zod schema 同时用于运行时校验和类型推导 |
| 图表 | echarts（按需引入） | 与现状一致 |
| PDF | pdfjs-dist | 与现状一致 |
| E2E | Playwright | 微信链路之外的全部主流程回归 |
| 部署 | Next.js `output: 'standalone'` + Docker，置于现有 nginx 之后 | nginx 继续反代 `/auth|/app|/platform|...` 到 smart-gateway，新增一条反代到 Next 容器；后端完全无感知 |

**明确不用的东西**：不用 SSR 渲染业务页面（微信内嵌场景无收益，全部页面 `'use client'` 为主）；不用 Redux；不做 i18n（当前无此需求，YAGNI）。Next.js 在本项目当前阶段的价值是工程体系 + React 生态 + 未来扩展性，不是服务端渲染。

## 2. 总体架构

```
微信浏览器
   │
   ▼
nginx（现有，新增 location / → next-app:3000）
   ├── /auth /app /platform /admin /algorithm /file /workbench /visitor ──► smart-gateway:9990（不动）
   └── /  ──► Next.js standalone 容器（新）
                ├── app/(routes)/…        # 页面（CSR 为主）
                ├── lib/api/              # API 兼容层（§3）
                ├── lib/wechat/           # 微信集成层（§4）
                ├── lib/config/           # 多租户运行时配置（§5）
                └── features/<module>/    # 业务模块（§6）
```

接口仍走浏览器 → nginx → gateway 的相对路径直连（与现状相同），**Next 服务只承担静态资源与页面渲染**，不做 API 转发，避免引入新故障点。

## 3. API 兼容层（lib/api/）— 本设计的生命线

复刻现有 `http.js` + `services/index.js` 的全部隐性约定，逐条对应：

| # | 现有约定 | 新实现 |
|---|---------|--------|
| 1 | `Authorization: Bearer {token}`，OAuth 换码用 `Basic {token}`，部分接口免认证 | 请求选项 `auth: 'bearer' \| 'basic' \| 'none'`，默认 bearer |
| 2 | POST 且 body 含 `size`+`current` → 自动提升为 URL query 并从 body 移除 | 拦截器等价复刻（注意：现实现 body 中仍保留 size/current，需抓包确认后对齐，原型期验证） |
| 3 | 敏感字段 AES-256-CBC（ZeroPadding，IV=key）加密；解密 AES-ECB（Pkcs7） | `lib/api/crypto.ts` 用 crypto-js 复刻同算法同模式；**先与现有实现做密文比对测试**（同明文同密钥产出必须一致） |
| 4 | 密钥来源：`window.__SMART_CONFIG__.securityEncodeKey` → 环境变量 | 保留同名运行时注入（§5），fallback `NEXT_PUBLIC_SECURITY_ENCODE_KEY` |
| 5 | 响应 HTML 实体反转义（`&#40;` `&#41;` `&#39;` `&lt;` `&gt;`） | 响应拦截器等价复刻 |
| 6 | `code===401 && msg==='invalid_token'` → 写 invalid_token 标记 → 跳微信 OAuth | 同逻辑；OAuth URL 由配置层生成（appid/回调路径不再硬编码） |
| 7 | 空响应打 `$is_null` 标记、挂 `$original` | 不照搬魔法字段；改为类型化返回 `{ data, isEmpty, raw }`，在兼容层内消化 |
| 8 | `requestTask` 同名请求取消防重复 | 用 AbortController 实现；列表页/提交按钮默认启用 |
| 9 | 8 个 module 前缀（platform/app/auth/admin/algorithm/workbench/visitor/file） | `apiClient(module).get/post(...)`，路径拼接规则与现状一致 |
| 10 | Base64 图片走 JSON body、文件走 FormData | 上传工具函数两种模式各一 |

每个业务域一个类型化 service 文件（`features/<module>/api.ts`），用 zod 定义响应 schema——**全部在用接口（实测 73 条）的请求/响应类型在原型期通过真实抓包逐个固化**，这是兼容性的唯一可靠来源（供应商代码只能当线索，不能当规范）。

## 4. 认证与微信集成层（lib/wechat/）

- **OAuth 流**：进入受保护路由 → 无 token → 跳微信 `snsapi_base` 静默授权 → 回调路由 `/login/wechat/callback?code=` → `POST /auth/wx/public/token`（Basic）→ 存 token → 回原页面。回跳路径用 `state` 参数携带（现实现写死回 login，可改进但保持兼容）。
- **Token 存储**：沿用 localStorage 同名 key（`SMART_WEB_APP_TOKEN` 等），保证新旧前端灰度并行期间可以共享登录态。
- **JSSDK**：`useWxJsSdk()` hook —— 取当前 URL → `GET /app/wechat/sign?url=` → `wx.config` → 暴露 `scanQRCode()` 等 promise 化方法。iOS 微信对 SPA 路由变化后的签名 URL 有著名兼容性坑（须用首次进入的 landing URL 签名），hook 内统一处理。
- **refresh_token**：现有代码存而不用。新版本先保持同样行为（过期即重走静默授权，用户无感），不自作主张加刷新逻辑——避免与后端未知行为打架。
- **Spike 0（开发前置）**：用一个 10 行的最小 Next 页面在真机微信内调通 OAuth + JSSDK 签名 + 扫码，这是全项目唯一必须真机验证的环节，原型阶段就要完成。

## 5. 多租户/园区配置（lib/config/）

现状用 js-conditional-compile-loader 编译出 5 个构建变体（h5/jiantao/junya/shunluo/face）+ 许昌/合肥双流程页面。商用产品不能给每个客户编译一个包，改为**单构建 + 运行时配置**：

- `public/config.js` 注入 `window.__SMART_CONFIG__ = { parkId, wxAppId, securityEncodeKey, tenant, features: {...}, flows: { visitor: 'standard' | 'hefei' } }`，由部署时挂载/生成（Docker env → entrypoint 渲染模板）。
- 代码内用 `useTenantConfig()` 读取；页面级差异（如访客合肥流程）用同一路由 + 配置驱动的流程分支组件，不再复制整套页面。
- 这是相对现状的**架构改进点**，也是商用化（一套部署服务多客户）的必要条件。

## 6. 目录结构与模块划分

```
src/
├── app/                      # Next App Router 路由壳（薄）
│   ├── (auth)/login/…
│   ├── visitor/…  check-in/…  dorm/…  dorm-exit/…  dorm-repairs/…
│   ├── good-release/(live|work)/…  return-factory/…  backlog/…
│   ├── home/…  mine/…  help/…  lock/…
│   └── layout.tsx            # 全局 Provider（Query/Config/Auth）
├── features/<module>/        # 业务模块：api.ts + hooks + 组件 + 流程逻辑
├── components/               # 跨模块通用组件（替代 @tce 与现有 32 个公共组件）
├── lib/
│   ├── api/                  # §3 兼容层
│   ├── wechat/               # §4
│   ├── config/               # §5
│   └── crypto/  format/      # 加密、日期/金额格式化（替代 filters）
└── e2e/                      # Playwright
```

路由路径**保持与旧版 hash 路由语义一致的 history 路由**（`/#/visitor/index` → `/visitor`）。注意：旧版是 hash 路由，微信公众号后台的 JS 安全域名/授权回调配置需要核对是否兼容 history 路由回调地址（原型期确认，是已知风险项）。

## 7. 质量护栏（AI 开发的强制门禁）

1. TypeScript strict + `noUncheckedIndexedAccess`；CI 上 `tsc --noEmit` 必须过。
2. ESLint + Prettier；CI 强制。
3. 接口契约：zod schema 校验真实后端响应，开发期 schema 不符直接抛错（快速失败），避免 AI 凭想象写字段名。
4. Playwright E2E：每完成一个模块，至少覆盖其主路径（登录态 mock + API 录制回放）；微信专属能力（扫码）在 E2E 中以注入 stub 验证调用契约，真机手工验证扫码本身。
5. 加密兼容性单测：新旧实现同明文同密钥密文一致（这是和后端解密兼容的硬指标）。
6. 旧系统作为参照系：灰度期同一后端跑新旧两套前端，业务数据互相可见即为兼容性最强证据。

## 8. 原型阶段方案（设计批准后的下一步）

**原型 = 用 Next.js 本身搭建的可点击高保真原型**，而非一次性扔掉的 Figma/纯 HTML：

- 搭好 §6 的工程骨架 + 全部路由壳 + antd-mobile 主题定制（对照现有视觉）。
- 所有页面用静态 mock 数据渲染，完整走通页面间导航流（访客申请 16 页流程、离宿扫码流程等）。
- 不接真实后端、不写业务逻辑——原型评审通过后，mock 逐模块替换为真实 API 即进入开发阶段，原型投入零浪费。
- **原型范围建议**：第一轮只做「登录 + 首页 + 访客模块」垂直切片（覆盖最复杂业务 + 微信 OAuth/扫码/双流程全部技术风险），评审通过后再铺开其余模块。
- Spike 0（§4）与原型并行完成。

## 9. 实施路线图（原型批准后）

| 阶段 | 内容 | 出口标准 |
|------|------|---------|
| 0 | 微信 Spike + 工程骨架 + API 兼容层 + 加密兼容单测 | 真机扫码通；密文比对一致 |
| 1 | 登录 + 首页 + 访客模块（真实 API） | 真实业务跑通访客全流程，E2E 绿 |
| 2 | 离宿 + 物品放行（生活/办公）+ 历史查询 | 扫码链路全部真机验证 |
| 3 | 宿舍签到/水电/报修 + 返厂 + 门禁 + 个人中心/帮助 | 全模块 E2E 绿 |
| 4 | 多租户配置化收尾 + 灰度并行 + 切流 | 新旧并行 ≥2 周无差异工单 |

## 10. 关键风险与对策

| 风险 | 对策 |
|------|------|
| 接口"文档"只有供应商代码可参考，可能与实际行为有出入 | 一切以真实抓包为准；zod 校验快速失败暴露差异 |
| 微信 JSSDK 在 history 路由 + iOS 下的签名坑 | Spike 0 前置真机验证；landing URL 签名策略 |
| 公众号后台回调域名/安全域名配置变更需求 | 原型期核对，必要时保留兼容回调路径 |
| AES ZeroPadding 等非标加密细节复刻出错 | 密文比对单测作为硬门禁 |
| AI 生成代码无人工 review | §7 全套工具链门禁 + 旧系统对照灰度 |
| 灰度期新旧并行的登录态互通 | 沿用相同 localStorage key 与 token 流 |
