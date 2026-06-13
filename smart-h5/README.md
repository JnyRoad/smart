# smart-h5

智慧园区 H5 应用，基于 Next.js App Router、React、TypeScript、antd-mobile 和 Tailwind CSS 构建，面向微信内嵌 H5 场景。

当前已覆盖认证、首页、公告、我的、访客、帮助中心、宿舍、门锁和宿舍报修等阶段一功能。后续模块继续在本目录内按现有 `src/features/` 结构扩展。

## 命令

```bash
pnpm install
pnpm dev          # http://localhost:3000（API 经 rewrites 代理到现网网关）
pnpm check        # tsc --noEmit + eslint
pnpm test         # vitest 单测（API 兼容层 / token 存储 / URL 白名单）
pnpm e2e          # Playwright（网络层 mock，dev server 端口 3100）
pnpm build        # 生产构建（standalone）
```

环境变量 `API_PROXY_TARGET` 可覆盖开发代理目标。

## 目录约定

```
docs/             评估报告、架构设计、原型规格与 mockup、实施计划
src/app/          App Router 路由（页面壳）
src/features/     业务模块（api + 流程逻辑）
src/lib/          API 兼容层 / 认证存储 / 租户配置 / 微信 OAuth
src/components/   跨模块通用组件
e2e/              Playwright 用例（网络层 mock）
public/config.js  运行时租户配置（window.__SMART_CONFIG__，部署期渲染）
```

## 关键文档

- 架构设计：docs/superpowers/specs/2026-06-11-nextjs-rewrite-architecture.md
- 阶段一设计：docs/superpowers/specs/2026-06-12-stage1-auth-home-mine-design.md
- 全量页面清单（69 页）：docs/superpowers/specs/2026-06-11-page-inventory.md
- 原型规格与 mockup：docs/prototype/

## 已实现（阶段一）

- **认证**：微信 snsapi_base 静默授权 → `POST auth:/wx/public/token`（Basic）换 token；
  已绑定员工无感进 `/home`；未绑定 → `/login/badge` 工号 + 身份证后六位绑定
  （`POST app:/wechat/xc/banging/badge`）→ 重新授权完成登录；401 invalid_token 自动重走 OAuth。
  Token 存 localStorage `xc-` 信封格式。
- **主页** `/home`：园区/天气/员工条、最新公告条、审批宫格（3 项待办角标）、
  后端动态园区服务宫格；离职用户强制登出。
- **公告** `/home/bbs`、`/home/bbs/[id]`：外链/富文本（DOMPurify + 图片预览）/PDF 三种形态。
- **我的** `/mine`、`/mine/detail`：个人信息、园区横幅、菜单、微信解绑。
- **访客（标准/许昌流程，12 页）**：三步申请流（被访人 → 访客信息 → 短信验证提交，
  访客侧微信 OAuth 换 openId、人脸照片上传、厂区/区域选择、随行人员、车辆、
  黑名单校验、区域复核剪除）+ 货车独立预约 + 通行二维码页（delFlag 三态）。
  跨页草稿存 Zustand persist（`visitor-flow`），OAuth 整页跳转后存活；
  合肥变体以 `flows.visitor==='hefei'` 配置分支预留，未实现。
- **访客申请记录（2 页，新增功能）**：`/visitor/records`（短信验证 + 状态筛选列表）、
  `/visitor/records/[applyId]`（审批进度时间线 + 六态详情）；queryToken 鉴权防 IDOR；
  后端接口未实现，`features.visitorRecordsMock` 开关（默认开）返回演示数据，契约见
  specs/2026-06-12-visitor-records-design.md §2。
- **帮助中心（2 页）**：`/help` 分页列表 + `/help/[id]` 富文本详情。
- **我的宿舍 / 门锁（4 页）**：`/dorm` 聚合页、`/dorm/water-elec` 水电账单（月份筛选
  + 热水过滤/冷水改名规则）、`/dorm/lock` 门锁动态码（hex 密文前端 AES 解密、修改、
  未入住回跳）、`/dorm/get-code` 人脸刷新动态码。
- **宿舍报修（3 页）**：`/dorm-repairs` 工单提交（区域→楼栋联动、base64 多图）、
  列表（状态配色）、详情（审批时间线 + 维修结果区）。

## 关键实现说明

- `src/lib/api/http.ts`：统一 API 请求约定（模块前缀、bearer/basic/none、
  POST 分页提升、实体反转义、任意 HTTP 状态码解析业务信封、AbortController 去重）。
- `src/lib/react19-compat.ts`：antd-mobile v5 命令式 API 的 React 19 适配（官方 unstableSetRender 方案）。
- 多租户：`public/config.js` 注入 `window.__SMART_CONFIG__`，部署期按租户渲染。

## 待办

- 访客模块（重点，许昌/合肥双流程配置化）
- 敏感字段 AES 加解密
- 微信 JSSDK 扫码（真机 Spike：JS 安全域名/授权回调域名配置）
- 短信登录、PDF 的 pdfjs 渲染、审批/宿舍等业务模块
