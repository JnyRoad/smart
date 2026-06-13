import { describe, expect, it } from 'vitest'
import {
  DDDD_OPTIONS,
  FXDD_OPTIONS,
  FXQC_OPTIONS,
  FXSX_OPTIONS,
  SFFC_OPTIONS,
  SQRJB_OPTIONS,
  WPFXLB_OPTIONS,
  YSFS_OPTIONS,
  isPersonRelease,
  ysfsLabel,
} from './dicts'

describe('放行字典（旧 const.js 事实）', () => {
  it('人员放行分支判定：fxsx 0 或 7', () => {
    expect(isPersonRelease(0)).toBe(true)
    expect(isPersonRelease(7)).toBe(true)
    expect(isPersonRelease(1)).toBe(false)
    expect(isPersonRelease(undefined)).toBe(false)
  })

  it('字典条目数与关键取值', () => {
    expect(FXQC_OPTIONS).toEqual([
      { value: 0, label: '厂内' },
      { value: 1, label: '厂外' },
    ])
    expect(SFFC_OPTIONS).toEqual([
      { value: 0, label: '是' },
      { value: 1, label: '否' },
    ])
    expect(FXDD_OPTIONS).toHaveLength(12)
    expect(DDDD_OPTIONS).toBe(FXDD_OPTIONS) // 出发/到达共用一组
    expect(FXDD_OPTIONS.find((o) => o.value === 11)?.label).toBe('市场开发部/生活区')
    expect(SQRJB_OPTIONS).toHaveLength(5)
    expect(FXSX_OPTIONS).toHaveLength(9)
    expect(FXSX_OPTIONS.find((o) => o.value === 7)?.label).toBe('人员放行(仅限出差使用)')
    expect(FXSX_OPTIONS.find((o) => o.value === 10)?.label).toBe('废品出售')
    expect(WPFXLB_OPTIONS).toHaveLength(3)
    expect(YSFS_OPTIONS.find((o) => o.value === 2)?.label).toBe('叉车')
  })

  it('运输方式取文案，未知值为空串', () => {
    expect(ysfsLabel(0)).toBe('人工')
    expect(ysfsLabel(3)).toBe('三轮')
    expect(ysfsLabel(99)).toBe('')
    expect(ysfsLabel(undefined)).toBe('')
  })
})
