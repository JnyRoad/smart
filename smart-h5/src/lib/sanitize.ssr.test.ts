// @vitest-environment node
import { describe, expect, it } from 'vitest'

/**
 * Next production build 会在 Node/SSR 阶段加载 client module 依赖。
 * sanitizer 不能在模块顶层假设已经存在浏览器 DOM。
 */
describe('sanitizeRichText SSR compatibility', () => {
  it('can be imported without a browser window', async () => {
    await expect(import('./sanitize')).resolves.toHaveProperty('sanitizeRichText')
  })

  it('does not emit raw HTML when no browser DOM is available', async () => {
    const { sanitizeRichText } = await import('./sanitize')

    expect(sanitizeRichText('<img src="x" onerror="alert(1)">')).toBe('')
  })
})
