// src/util/util.contract.test.js
import { describe, it, expect, vi } from 'vitest'

// util.js 顶层 `import request from '@/router/axios'` 是有副作用的导入；
// 用 vi.hoisted 记录该副作用是否在 util 求值时被触发，作为"副作用契约"的断言依据。
const hoisted = vi.hoisted(() => ({ axiosLoaded: false }))
vi.mock('@/router/axios', () => {
  hoisted.axiosLoaded = true
  return { default: {} }
})

// util.js 当前的全部命名导出（拆分时若新增/删除导出，必须显式更新本清单）
const EXPECTED_EXPORTS = [
  'serialize', 'getObjType', 'deepClone', 'diff', 'toggleGrayMode', 'setTheme',
  'encryption', 'fullscreenToggel', 'listenfullscreen', 'fullscreenEnable',
  'reqFullScreen', 'exitFullScreen', 'findParent', 'loadStyle', 'isObjectValueEqual',
  'findByvalue', 'findArray', 'randomLenNum', 'openWindow', 'handleImg', 'isArrayFn',
  'dateFormat', 'getDateMonth', 'getDatePreMonth', 'getDatePreDay', 'formatNumber',
  'getProportion', 'floatNumMinus', 'getLabel'
].sort()

describe('util.js 模块契约（拆分前钉死）', () => {
  it('求值时触发 @/router/axios 的副作用导入', async () => {
    await import('./util')
    expect(hoisted.axiosLoaded).toBe(true)
  })

  it('没有 default export（禁止 import x from "@/util/util"）', async () => {
    const mod = await import('./util')
    expect(mod.default).toBeUndefined()
  })

  it('命名导出清单与基线完全一致', async () => {
    const mod = await import('./util')
    const actual = Object.keys(mod).filter((key) => key !== 'default').sort()
    expect(actual).toEqual(EXPECTED_EXPORTS)
  })
})
