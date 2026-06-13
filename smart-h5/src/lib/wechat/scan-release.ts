export interface WechatSignature {
  timestamp: number | string
  nonceStr: string
  signature: string
}

interface CheckJsApiResult {
  checkResult?: { scanQRCode?: boolean }
}

interface ScanResult {
  resultStr?: string
}

interface WechatJsSdk {
  config: (options: {
    debug: boolean
    appId: string
    timestamp: number | string
    nonceStr: string
    signature: string
    jsApiList: string[]
  }) => void
  ready: (callback: () => void) => void
  error: (callback: (error: { errMsg?: string }) => void) => void
  checkJsApi: (options: {
    jsApiList: string[]
    success: (result: CheckJsApiResult) => void
    fail?: (error: { errMsg?: string }) => void
  }) => void
  scanQRCode: (options: {
    needResult: 1
    scanType: ('qrCode' | 'barCode')[]
    success: (result: ScanResult) => void
    fail?: (error: { errMsg?: string }) => void
  }) => void
}

declare global {
  interface Window {
    wx?: WechatJsSdk
  }
}

const WECHAT_SDK_SRC = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'
const JS_API_LIST = ['scanQRCode', 'checkJsApi']

let sdkLoadPromise: Promise<WechatJsSdk> | null = null

function currentWechatSignUrl(): string {
  return window.location.href
}

function loadWechatSdk(): Promise<WechatJsSdk> {
  if (window.wx) return Promise.resolve(window.wx)
  if (sdkLoadPromise) return sdkLoadPromise

  sdkLoadPromise = new Promise((resolve, reject) => {
    const existing = document.querySelector<HTMLScriptElement>(`script[src="${WECHAT_SDK_SRC}"]`)
    const script = existing ?? document.createElement('script')
    const rejectAndReset = (error: Error) => {
      sdkLoadPromise = null
      if (!existing) script.remove()
      reject(error)
    }
    if (!existing) {
      script.src = WECHAT_SDK_SRC
      script.async = true
    }
    script.onload = () => {
      if (window.wx) resolve(window.wx)
      else rejectAndReset(new Error('微信扫一扫不可用，请在微信内打开'))
    }
    script.onerror = () => rejectAndReset(new Error('微信配置失败'))
    if (!existing) document.head.appendChild(script)
  })

  return sdkLoadPromise
}

function waitForWechatReady(wx: WechatJsSdk, appId: string, signature: WechatSignature): Promise<void> {
  return new Promise((resolve, reject) => {
    wx.ready(resolve)
    wx.error((error) => reject(new Error(error.errMsg ? `微信配置错误：${error.errMsg}` : '微信配置失败')))
    wx.config({
      debug: false,
      appId,
      timestamp: signature.timestamp,
      nonceStr: signature.nonceStr,
      signature: signature.signature,
      jsApiList: JS_API_LIST,
    })
  })
}

function checkScanApi(wx: WechatJsSdk): Promise<void> {
  return new Promise((resolve, reject) => {
    wx.checkJsApi({
      jsApiList: ['scanQRCode'],
      success: (result) => {
        if (result.checkResult?.scanQRCode) resolve()
        else reject(new Error('当前微信版本不支持扫一扫'))
      },
      fail: (error) => reject(new Error(error.errMsg || '微信扫一扫检查失败')),
    })
  })
}

function scanQrCode(wx: WechatJsSdk): Promise<string> {
  return new Promise((resolve, reject) => {
    wx.scanQRCode({
      needResult: 1,
      scanType: ['qrCode', 'barCode'],
      success: (result) => {
        if (result.resultStr) resolve(result.resultStr)
        else reject(new Error('扫码结果为空'))
      },
      fail: (error) => reject(new Error(error.errMsg || '扫码失败')),
    })
  })
}

export function releaseRouteFromScanResult(resultStr: string): string {
  let parsed: { id?: string | number; type?: string | number }
  try {
    parsed = JSON.parse(resultStr) as { id?: string | number; type?: string | number }
  } catch {
    throw new Error('扫码结果格式不正确')
  }

  if (parsed.id === undefined || parsed.id === null || parsed.id === '') {
    throw new Error('扫码结果缺少放行单号')
  }

  const id = encodeURIComponent(String(parsed.id))
  const type = String(parsed.type ?? '')
  if (type === '6') return `/backlog/dorm-exit/detail?id=${id}&scan=1`
  if (type === '3-5') return `/backlog/release-work/detail?id=${id}&scan=1`
  return `/backlog/release-live/detail?id=${id}&sort=3&scan=1`
}

export async function scanReleaseCode(options: {
  appId: string
  getSignature: (url: string) => Promise<WechatSignature>
}): Promise<string> {
  const wx = await loadWechatSdk()
  const signature = await options.getSignature(currentWechatSignUrl())
  await waitForWechatReady(wx, options.appId, signature)
  await checkScanApi(wx)
  const resultStr = await scanQrCode(wx)
  return releaseRouteFromScanResult(resultStr)
}
