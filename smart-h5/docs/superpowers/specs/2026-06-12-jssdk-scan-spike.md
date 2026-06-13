# Spike：微信 JS-SDK 扫一扫（审批侧扫码核验）

> 结论先行：**本批不落地任何扫码代码**。旧版所有扫一扫入口均被注释停用（无行为基线），且 JS-SDK 无法在本地/Playwright 环境验真。本文沉淀旧版事实、技术路径与风险清单，供后续真机阶段实施。

## 1. 旧版事实（净室提取）

- 入口（全部被注释）：backLog 生活区审批列表、退宿审批列表、返厂确认列表的「扫一扫」按钮。
- 流程：
  1. `GET app:/wechat/sign?url={当前页面完整 URL}` → 返回 `{appId, timestamp, nonceStr, signature}`
  2. `wx.config({ jsApiList: ['scanQRCode'], ...签名 })` → `wx.ready`
  3. `wx.scanQRCode({ needResult: 1 })` → 得二维码文本（放行单/退宿单 id）
  4. 分流：物品放行码 → `GET platform:/articlesrelease/detail/{id}`，按 `articlesType`（3 生活区 / 4 办公区）跳对应审批详情并带 `isScan=1`；退宿码 → `GET platform:/dor/quit/list/check/{id}`（扫码专用详情）跳退宿审批详情。
  5. 扫码进入的详情审批成功后回 `/home`（普通进入回列表 `?tab=done`）。

## 2. 新栈技术路径（未来实施建议，不在本批）

- 依赖：`weixin-js-sdk` npm 包（或官方 CDN `jweixin-1.6.0.js` 动态注入）。SSR 环境必须懒加载（`'use client'` + dynamic import），wx 对象仅存在于微信 WebView。
- 封装形态：`src/lib/wechat-scan.ts` 暴露 `scanQrCode(): Promise<string>`，内部完成 sign→config→ready→scanQRCode 链；以 `features.wechatScan`（config.js 注入，默认 false）控制审批列表是否渲染扫码按钮。
- 签名 URL：iOS 微信取**首次进入页面的 URL**，SPA 路由切换后需用 `location.href.split('#')[0]` 重新签名；Next.js App Router 下建议每次扫码前现签（不缓存签名）。
- 详情页需恢复 `isScan` 分支：审批成功跳 `/home`、加载失败跳 `/home`（spec §4/§5 已为普通进入实现，扫码差异点仅成功跳转目标）。

## 3. 风险清单（真机阶段必须逐项核销）

| # | 风险 | 验证方式 |
|---|---|---|
| 1 | JS 安全域名未配置 → wx.config 报 invalid url domain | 公众号后台配置新前端域名后真机验证 |
| 2 | `/wechat/sign` 返回签名与新域名 URL 不匹配（后端按旧域名缓存 jsapi_ticket 场景） | 抓包比对 signature 入参 url |
| 3 | iOS 首址签名问题（SPA 切路由后 config 失效） | iOS 真机：进入列表→切 Tab→扫码 |
| 4 | 二维码内容格式假设（纯 id vs 带前缀 URL）无后端文档佐证 | 用真实放行码扫一次，记录原文 |
| 5 | 扫码进入免登录态：未绑定微信的保安扫码会被引导到绑定流程，isScan 上下文是否丢失 | 真机新用户路径 |
| 6 | `dor/quit/list/check/{id}` 响应形状未在前端代码中完整体现 | 真实网关抓包补类型 |

## 4. 验收口径

后续实施分支的完成定义：真机微信内完成「保安扫生活区放行码 → 审批详情(isScan) → 确认放行 → 回 /home」全链路一次，并把 §3 表格逐项标记结果。
