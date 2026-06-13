export interface TenantConfig {
  tenant: string
  parkId: number
  parkName: string
  parkAddress: string
  weatherCity: string
  wxAppId: string
  flows: { visitor: 'standard' | 'hefei' }
  /** AES key for legacy-compatible field encryption; injected at deploy time, never committed. */
  securityEncodeKey?: string
  features: {
    /** Explicit local/demo fallback for visitor records list/detail APIs. */
    visitorRecordsMock: boolean
  }
}

const DEFAULTS: TenantConfig = {
  tenant: 'xuchang',
  parkId: 5000021,
  parkName: '裕同科技许昌园区',
  parkAddress: '许昌数字经济产业园',
  weatherCity: '许昌',
  wxAppId: 'wx5c0d26056102d41e',
  flows: { visitor: 'standard' },
  features: { visitorRecordsMock: false },
}

declare global {
  interface Window {
    __SMART_CONFIG__?: Partial<TenantConfig>
  }
}

export function getTenantConfig(): TenantConfig {
  if (typeof window === 'undefined') return DEFAULTS
  const runtime = window.__SMART_CONFIG__
  if (!runtime) return DEFAULTS
  return {
    ...DEFAULTS,
    ...runtime,
    flows: { ...DEFAULTS.flows, ...runtime.flows },
    features: { ...DEFAULTS.features, ...runtime.features },
  }
}
