// 验证运行时资源只加载一次，失败或超时可恢复，不依赖真实网络。
import { afterEach, beforeEach, expect, it, vi } from 'vitest'

let loadPdfmeRuntime
const runtime = { version: '6.1.12', mountDesigner: () => undefined }

/** 捕获异步结果，确保断言提前失败也不会产生未处理拒绝。 */
function capture(promise) {
  return promise.then(value => ({ value }), error => ({ error }))
}

beforeEach(async () => {
  vi.resetModules()
  delete window.SmartPdfme
  document.querySelectorAll('script[data-pdfme-runtime]').forEach(script => script.remove())
  loadPdfmeRuntime = (await import('./pdfme-runtime')).loadPdfmeRuntime
})

afterEach(() => {
  vi.useRealTimers()
  delete window.SmartPdfme
  document.querySelectorAll('script[data-pdfme-runtime]').forEach(script => script.remove())
})

it('多个宿主共享一次资源加载，返回校验过的运行时', async () => {
  const first = capture(loadPdfmeRuntime())
  const second = capture(loadPdfmeRuntime())
  const scripts = document.querySelectorAll('script[data-pdfme-runtime]')
  expect(scripts.length).toBe(1)
  expect(new URL(scripts[0].src).origin).toBe(window.location.origin)
  window.SmartPdfme = runtime
  scripts[0].dispatchEvent(new Event('load'))
  expect((await first).value).toBe(runtime)
  expect((await second).value).toBe(runtime)
})

it('失败清理脚本并允许下一次重新加载', async () => {
  const first = capture(loadPdfmeRuntime())
  const script = document.querySelector('script[data-pdfme-runtime]')
  expect(script).not.toBeNull()
  script.dispatchEvent(new Event('error'))
  expect((await first).error.message).toContain('加载失败')
  expect(document.querySelector('script[data-pdfme-runtime]')).toBeNull()
  const second = capture(loadPdfmeRuntime())
  window.SmartPdfme = runtime
  document.querySelector('script[data-pdfme-runtime]').dispatchEvent(new Event('load'))
  expect((await second).value).toBe(runtime)
})

it('超时拒绝等待并释放资源', async () => {
  vi.useFakeTimers()
  const result = capture(loadPdfmeRuntime())
  await vi.advanceTimersByTimeAsync(15000)
  expect((await result).error.message).toContain('超时')
  expect(document.querySelector('script[data-pdfme-runtime]')).toBeNull()
})

it('资源加载完成但版本错误时拒绝初始化', async () => {
  const result = capture(loadPdfmeRuntime())
  window.SmartPdfme = { ...runtime, version: '0.0.0' }
  const script = document.querySelector('script[data-pdfme-runtime]')
  expect(script).not.toBeNull()
  script.dispatchEvent(new Event('load'))
  expect((await result).error.message).toContain('版本')
})
