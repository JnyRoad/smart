// Runtime tenant config. In production this file is rendered per deployment
// (Docker entrypoint); the values below are the Xuchang-park defaults.
window.__SMART_CONFIG__ = {
  tenant: 'xuchang',
  parkId: 5000021,
  parkName: '裕同科技许昌园区',
  parkAddress: '许昌数字经济产业园',
  weatherCity: '许昌',
  wxAppId: 'wx5c0d26056102d41e',
  flows: { visitor: 'standard' },
  // visitorRecordsMock only affects visitor record list/detail fixtures.
  // Keep false for real business traffic; enable explicitly for local demos.
  features: { visitorRecordsMock: false },
}
