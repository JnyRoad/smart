# 阶段一开发设计：认证 + 主页 + 我的（真实实现）

日期：2026-06-12
前置：原型已评审通过（docs/prototype/）；架构设计 [2026-06-11-nextjs-rewrite-architecture.md](./2026-06-11-nextjs-rewrite-architecture.md)。

## 1. 范围

本阶段交付真实代码（非 mock 原型），共 4 个独立功能分支，逐个完成、逐个合并：

| 分支 | 内容 |
|---|---|
| `feat/app-scaffold` | Next.js 工程骨架、租户配置层、API 兼容层（含单测）、认证 token 存储 |
| `feat/auth-login` | 登录模块 4 页：账号登录 / 微信授权跳板 / OAuth 回调换 token / 工号绑定 |
| `feat/home` | 主页 + 公告列表 + 公告详情 |
| `feat/mine` | 个人中心 + 个人信息 |

访客模块为下一阶段重点，不在本文档内。

## 2. 认证流程（以原 Vue 项目为准，净室复刻行为）

事实来源：旧仓库只读分析（login_wechat.vue / code_wechat.vue / logon_badge.vue / services/login.js）。

1. **入口**：受保护页面无 token → 跳 `/login/wechat` → 立即重定向微信 `snsapi_base` 静默授权，redirect_uri 指向 `/login/wechat/callback`。
2. **回调换 token**：`POST auth:/wx/public/token`（`Authorization: Basic c21hcnQ6c21hcnQ=`，body `{ code, type: 'F' }`）。
   - 返回 `access_token` → 存储并跳 `/home`（**已绑定微信的员工无感直接进主页**）。
   - 返回 `code===1` 且 data 为「账号未绑定工号，请先绑定」/「员工状态异常」→ 重新发起 OAuth，redirect 到 `/login/badge`。
   - 其他失败 → toast `message`。
3. **工号绑定**（外包/新员工首次使用）：输入 **工号 + 身份证后六位**，`POST app:/wechat/xc/banging/badge`，body `{ parkId, code, badge, lastCertNum }`。成功 → 重新 OAuth 走第 2 步完成登录；失败 → toast，2s 后重新 OAuth 回本页（刷新 code）。
   - ⚠️ 旅途口述为「工号+密码」；原项目实际是「工号+身份证后六位」，按原项目实现（用户已要求一切以原项目流程为准）。
4. **Token**：localStorage `xc-access_token` / `xc-refresh_token` / `xc-expires_in`（与旧前端同 key，灰度期共享登录态）。请求默认 `Bearer {token}`。
5. **401 处理**：响应 `code===401 && msg==='invalid_token'` → 写 `invalid_token` 标记 → 重新跳微信 OAuth。
6. `/login` 账号登录页（短信/密码 Tab）按原型保留 UI；旧版该功能未接线（密码 submit 为空函数），新版接 `POST /auth/oauth/token`（密码）与发送验证码接口，**不可用时快速失败 toast**，不再把手机号写成 token（旧版开发期残留行为，不复刻）。

## 3. API 兼容层（本阶段实现的子集）

按架构 §3 约定实现 `lib/api/`，本阶段需要的能力：

- `auth: 'bearer' | 'basic' | 'none'`，默认 bearer；Basic 用于 OAuth 换码。
- module 前缀拼接：`request({ module: 'app', url: '/employee/baseinfo' })` → `/app/employee/baseinfo`。
- POST body 含 `size`+`current` → 提升为 URL query（与旧版一致，body 保留）。
- 响应 HTML 实体反转义（`&#40; &#41; &#39; &lt; &gt;`）。
- 401 invalid_token → 清 token 标记 + 跳 OAuth（可注入，便于测试）。
- AbortController 同名请求取消。
- **AES 加解密本阶段不做**（仅访客等模块需要，YAGNI，下一阶段实现并做密文比对单测）。

开发期后端：`next.config.ts` rewrites 把 `/auth /app /platform /admin /algorithm /workbench /visitor /file` 代理到 `https://xuchang.szyuto.com`（等价旧版 proxy.js）。

## 4. 本阶段接口清单

| 接口 | method | auth | 用途 |
|---|---|---|---|
| `auth:/wx/public/token` | POST | basic | code 换 token |
| `app:/wechat/xc/banging/badge` | POST | none | 工号绑定 |
| `auth:/auth/oauth/token` | POST | none | 密码登录 |
| `app:/sms/send/getCode/{mobile}` | GET | bearer | 发送验证码 |
| `app:/employee/fullinfo` | GET | bearer | 用户全量信息（首页员工条） |
| `app:/employee/baseinfo` | GET | bearer | 基本信息（离职判断/我的） |
| `app:/service/module/list` | GET | bearer | 园区服务宫格 |
| `app:/home/bbs/list` | GET | bearer | 公告 |
| `app:/home/bbs/detail/{id}` | GET | bearer | 公告详情 |
| `app:/common/weather` | GET | bearer | 天气 |
| `app:/approve/list/new/page` | GET | bearer | 物品放行待审批角标 |
| `app:/approve/list/repairs/list` | GET | bearer | 报修待审批角标 |
| `app:/dor/quit/list/approval` | POST | bearer | 退宿待审批角标 |
| `app:/wechat/xc/unbind` | POST | bearer | 微信解绑 |

响应结构以真实抓包/灰度对照为最终准绳；开发期用 zod 宽松校验 + 快速失败。

## 5. 测试策略

- **单测（vitest）**：API 兼容层全部约定（auth 三态、module 前缀、分页提升、实体反转义、401 跳转回调）；token 存储读写。
- **E2E（Playwright，page.route 网络层 mock）**：
  - 回调换 token 成功 → 进主页；未绑定 → 进绑定页；绑定成功 → 重新 OAuth（断言跳转 URL）。
  - 主页渲染（公告/宫格/角标）、公告列表→详情。
  - 我的页渲染、解绑确认流。
- 微信 OAuth 真机链路无法本地验证，跳转 URL 以断言字符串方式锁定契约。

## 6. 分支与合并

每个功能在独立 `feat/*` 分支开发，`pnpm check + test + e2e` 全绿后合入集成分支（本会话分支），最终由旅途决定合入 main。提交遵循 Conventional Commits（英文）。
