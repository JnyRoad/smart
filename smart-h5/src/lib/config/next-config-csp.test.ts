import { describe, expect, it } from 'vitest'

// 直接 import 根目录的 next.config 默认导出，验证其下发的安全响应头。
// 放在 src 下是因为 vitest.config 的 include 只匹配 src/**/*.test.ts。
import nextConfig from '../../../next.config'

/**
 * 取出挂在 source '/:path*' 上的全部响应头，按 key 收成 map，方便断言。
 */
async function headersForAllPaths(): Promise<Record<string, string>> {
  const entries = await nextConfig.headers!()
  const matched = entries.find((entry) => entry.source === '/:path*')
  if (!matched) throw new Error("未找到 source '/:path*' 的 headers 配置")
  return Object.fromEntries(matched.headers.map((h) => [h.key, String(h.value)]))
}

describe('next.config CSP headers', () => {
  it('下发强制 Content-Security-Policy，仅含三条零误报指令', async () => {
    const headers = await headersForAllPaths()
    const enforced = headers['Content-Security-Policy']

    expect(enforced).toBeDefined()
    expect(enforced).toContain("frame-ancestors 'self'")
    expect(enforced).toContain("base-uri 'self'")
    expect(enforced).toContain("form-action 'self'")
    // 强制头绝不能带 script-src / style-src，否则会拦截 Next 注水脚本与内联样式
    expect(enforced).not.toContain('script-src')
    expect(enforced).not.toContain('style-src')
  })

  it('保留原有的 Content-Security-Policy-Report-Only 头', async () => {
    const headers = await headersForAllPaths()
    expect(headers['Content-Security-Policy-Report-Only']).toBeDefined()
  })
})
