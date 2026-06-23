import type { NextConfig } from 'next'

/**
 * API proxy to the existing gateway. Development keeps the legacy default
 * remote target; standalone Docker production sets API_PROXY_TARGET at build
 * time so same-origin frontend calls still reach smart-gateway. Production
 * deployments without API_PROXY_TARGET can keep using an external nginx route.
 */
// NOTE: the gateway's 'visitor' module prefix is intentionally NOT proxied:
// it collides with this app's /visitor/* pages (afterFiles rewrites run
// before dynamic routes, so /visitor/records/[id] would be swallowed), and
// the new frontend calls no gateway API under /visitor — its visitor APIs
// live under /platform, /app and /algorithm. Production nginx must likewise
// route /visitor/* to this app, not to smart-gateway.
const GATEWAY_MODULES = ['auth', 'app', 'platform', 'admin', 'algorithm', 'workbench', 'file']

/**
 * 内容安全策略（CSP）—— 仅报告模式（Content-Security-Policy-Report-Only）
 *
 * 目的：为后续真正落地强制 CSP 做准备，先用「只观察、不拦截」模式收集线上
 * 真实加载来源，确认白名单完整后再单独评估是否切换为强制 CSP。本头不会
 * 阻断任何脚本 / 样式 / 接口 / 图片，对现有页面与微信 SDK 零影响。
 *
 * 当前已知的合法来源：
 *   - 'self'：本应用同源资源（页面、JS chunk、被 next.config rewrites 同源代理
 *     到网关的接口都视为 self）。
 *   - https://res.wx.qq.com：微信 JS-SDK 脚本（jweixin-1.6.0.js）来源。
 *   - https://open.weixin.qq.com：微信网页授权（snsapi_base）跳转目标。
 *   - https://xuchang.szyuto.com：网关默认远端（开发期 rewrites 直连、生产期
 *     若未走同源 nginx 时的直连兜底）。
 *   - 'unsafe-inline' / 'unsafe-eval'：Next.js 注水脚本、antd-mobile 的
 *     CSS-in-JS 内联样式，以及开发期 Turbopack 需要内联与 eval；先在报告模式
 *     放行以避免误报淹没真实违规，后续强制 CSP 时再用 nonce / hash 收紧。
 *
 * 说明：因为是 Report-Only，浏览器只会把违规上报到控制台 / report-uri，绝不
 * 阻止资源加载；故此处白名单即使有遗漏也不会影响线上功能，只会多出几条违规
 * 报告，正好用于补全白名单。
 */
/**
 * 内容安全策略（CSP）—— 强制模式（Content-Security-Policy）
 *
 * 为什么强制头只放这三条：frame-ancestors / base-uri / form-action 不约束脚本、
 * 样式、接口、图片的加载来源，因此对 Next 注水脚本、antd-mobile 内联样式、被
 * rewrites 同源代理的网关接口、人脸采集 data/blob 图片等统统零影响，可以立即
 * 强制生效，从而拿到：点击劫持防护（frame-ancestors）、<base> 劫持防护
 * （base-uri）、表单越权提交防护（form-action）。
 *
 * 为什么不把 script-src / style-src 放进强制头：它们一旦强制就会拦截 Next 的
 * 内联注水脚本和 antd-mobile 的 CSS-in-JS 内联样式造成白屏，必须等 nonce / hash
 * 改造完成后才能进强制头；在此之前继续由下方的 Report-Only 头观察。
 */
const CSP_ENFORCED = [
  "frame-ancestors 'self'",
  "base-uri 'self'",
  "form-action 'self'",
].join('; ')

const CSP_REPORT_ONLY = [
  "default-src 'self'",
  // 脚本：本应用 + 微信 JS-SDK；内联与 eval 暂时放行（见上注释）
  "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://res.wx.qq.com",
  // 样式：antd-mobile 等运行时注入内联样式，需放行 inline
  "style-src 'self' 'unsafe-inline'",
  // 图片：本域 + data/blob（人脸采集、二维码等本地生成）
  "img-src 'self' data: blob:",
  "font-src 'self' data:",
  // 接口：同源（含被代理的网关）+ 网关直连兜底
  "connect-src 'self' https://xuchang.szyuto.com",
  // 微信网页授权跳转目标允许出现在 frame/跳转链路中
  "frame-src 'self' https://open.weixin.qq.com",
  // 收紧基础风险面：禁止 <base> 劫持、限制表单提交目标为同源
  "base-uri 'self'",
  "form-action 'self'",
  // 禁止本站被任意站点内嵌（点击劫持防护）
  "frame-ancestors 'self'",
].join('; ')

const nextConfig: NextConfig = {
  output: 'standalone',
  // Pin the workspace root: this repo can be checked out as a nested git
  // worktree whose parent directory also holds a pnpm lockfile; root
  // auto-detection would then watch the entire parent tree (extreme CPU).
  turbopack: { root: __dirname },
  async rewrites() {
    const configuredTarget = process.env.API_PROXY_TARGET
    if (process.env.NODE_ENV !== 'development' && !configuredTarget) return []
    const target = configuredTarget ?? 'https://xuchang.szyuto.com'
    return GATEWAY_MODULES.map((module) => ({
      source: `/${module}/:path*`,
      destination: `${target}/${module}/:path*`,
    }))
  },
  // 给所有页面同时挂上：强制 CSP（仅三条零误报指令，立即拦截点击劫持 / base
  // 劫持 / 表单越权）+ 仅报告模式 CSP（继续观察 script/style 来源，不拦截）。
  async headers() {
    return [
      {
        source: '/:path*',
        headers: [
          { key: 'Content-Security-Policy', value: CSP_ENFORCED },
          { key: 'Content-Security-Policy-Report-Only', value: CSP_REPORT_ONLY },
        ],
      },
    ]
  },
}

export default nextConfig
