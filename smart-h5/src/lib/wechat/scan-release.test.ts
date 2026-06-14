import { afterEach, describe, expect, test, vi } from 'vitest'
import { releaseRouteFromScanResult, scanReleaseCode } from './scan-release'

const WECHAT_SDK_SRC = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'

afterEach(() => {
  document.querySelectorAll(`script[src="${WECHAT_SDK_SRC}"]`).forEach((script) => script.remove())
  window.wx = undefined
  vi.restoreAllMocks()
})

describe('releaseRouteFromScanResult', () => {
  test('routes dorm-exit scan codes to the scan detail page', () => {
    expect(releaseRouteFromScanResult(JSON.stringify({ id: 'SMS123', type: '6' }))).toBe(
      '/backlog/dorm-exit/detail?id=SMS123&scan=1',
    )
  })

  test('routes office release scans to the work approval page', () => {
    expect(releaseRouteFromScanResult(JSON.stringify({ id: '1451429288591634434', type: '3-5' }))).toBe(
      '/backlog/release-work/detail?id=1451429288591634434&scan=1',
    )
  })

  test('routes other release scans to security live approval', () => {
    expect(releaseRouteFromScanResult(JSON.stringify({ id: '9', type: '1' }))).toBe(
      '/backlog/release-live/detail?id=9&sort=3&scan=1',
    )
  })

  test('rejects malformed scan results', () => {
    expect(() => releaseRouteFromScanResult('not-json')).toThrow('扫码结果格式不正确')
    expect(() => releaseRouteFromScanResult(JSON.stringify({ type: '6' }))).toThrow('扫码结果缺少放行单号')
  })

  test('allows retrying after the WeChat SDK script fails to load', async () => {
    let existingScriptRemoved = false
    const existingScript = document.createElement('script')
    existingScript.setAttribute('src', WECHAT_SDK_SRC)
    vi.spyOn(existingScript, 'remove').mockImplementation(() => {
      existingScriptRemoved = true
    })
    vi.spyOn(document, 'querySelector').mockImplementation((selector) => {
      if (selector === `script[src="${WECHAT_SDK_SRC}"]` && !existingScriptRemoved) return existingScript
      return null
    })
    const appendedScripts: HTMLScriptElement[] = []
    vi.spyOn(document.head, 'appendChild').mockImplementation((node) => {
      appendedScripts.push(node as HTMLScriptElement)
      return node
    })

    const failedScan = scanReleaseCode({
      appId: 'wx-test',
      getSignature: () => Promise.resolve({ timestamp: 1, nonceStr: 'nonce', signature: 'sig' }),
    })
    existingScript.dispatchEvent(new Event('error'))
    await expect(failedScan).rejects.toThrow('微信配置失败')
    expect(existingScriptRemoved).toBe(true)

    const successfulScan = scanReleaseCode({
      appId: 'wx-test',
      getSignature: () => Promise.resolve({ timestamp: 1, nonceStr: 'nonce', signature: 'sig' }),
    })
    const retryScript = appendedScripts.at(-1)
    expect(retryScript).toBeDefined()
    expect(retryScript).not.toBe(existingScript)

    window.wx = {
      config: vi.fn(),
      ready: vi.fn((callback: () => void) => callback()),
      error: vi.fn(),
      checkJsApi: vi.fn(({ success }) => success({ checkResult: { scanQRCode: true } })),
      scanQRCode: vi.fn(({ success }) => success({ resultStr: JSON.stringify({ id: '9', type: '1' }) })),
    }
    retryScript?.dispatchEvent(new Event('load'))

    await expect(successfulScan).resolves.toBe('/backlog/release-live/detail?id=9&sort=3&scan=1')
  })
})
