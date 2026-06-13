import { describe, expect, it } from 'vitest'
import { FALLBACK_RANGES, REPAIR_TYPES, buildingsForRange, normalizeRepairOptions } from './repair-options'

describe('repair options', () => {
  it('区域→楼栋联动表', () => {
    expect(buildingsForRange(1)).toEqual(['老工厂1号宿舍', '老工厂2号宿舍', '老工厂3号宿舍', '新工厂宿舍楼'])
    expect(buildingsForRange(2)).toEqual(['餐厅三楼', '北门岗', '东门岗', '辅房'])
    expect(buildingsForRange(3)).toEqual(['一楼', '二楼', '三楼'])
    expect(buildingsForRange(4)).toEqual(['园区周边'])
    expect(buildingsForRange(99)).toEqual([])
  })

  it('兜底区域 4 项与维修类别 14 项', () => {
    expect(FALLBACK_RANGES).toHaveLength(4)
    expect(FALLBACK_RANGES[0]).toEqual({ code: 1, desc: '宿舍' })
    expect(REPAIR_TYPES).toHaveLength(14)
    expect(REPAIR_TYPES[0]).toEqual({ code: 1, desc: '灯' })
    expect(REPAIR_TYPES[13]).toEqual({ code: 14, desc: '地漏' })
  })

  it('兼容接口 code/desc 与旧表单 label/value 形态，并过滤坏值', () => {
    expect(
      normalizeRepairOptions([
        { code: '1', desc: '宿舍' },
        { value: 2, label: '办公室' },
        { code: null, desc: 'NULL' },
        { value: 'x', label: '坏数据' },
        { code: '', desc: '空 code' },
        { value: '   ', label: '空白 value' },
        { code: 0, desc: '零值' },
      ]),
    ).toEqual([
      { code: 1, desc: '宿舍' },
      { code: 2, desc: '办公室' },
    ])
  })
})
