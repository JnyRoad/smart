import { describe, expect, it } from 'vitest'
import { returnColor, formatJson, placeTypeDesc, meterTagFields } from './electric-manage-rules'

// 电表管理纯展示/格式化规则的行为表征测试。
// 这些断言锁定从 index.vue 抽离前的真实行为（含“非在线/离线返回 undefined”“宽松 == 区域映射”
// 等历史语义），抽离后必须逐字保持，不在重构里顺手“修正”。

describe('returnColor', () => {
  it('在线 → deviceStatus2', () => {
    expect(returnColor('在线')).toBe('deviceStatus2')
  })

  it('离线 → deviceStatus1', () => {
    expect(returnColor('离线')).toBe('deviceStatus1')
  })

  it('其它状态（未连接 / 未关联 / 空 / 未知）→ undefined（保留原 switch 无 default 行为）', () => {
    expect(returnColor('未连接')).toBeUndefined()
    expect(returnColor('未关联')).toBeUndefined()
    expect(returnColor('')).toBeUndefined()
    expect(returnColor(undefined)).toBeUndefined()
    expect(returnColor('在线 ')).toBeUndefined()
  })
})

describe('formatJson', () => {
  it('按 filterVal 顺序从每行取值', () => {
    expect(
      formatJson(['name', 'address'], [
        { name: 'm1', address: 'a1' },
        { name: 'm2', address: 'a2' }
      ])
    ).toEqual([
      ['m1', 'a1'],
      ['m2', 'a2']
    ])
  })

  it('缺失字段得 undefined（与原 v[j] 实现一致）', () => {
    expect(formatJson(['name', 'missing'], [{ name: 'm1' }])).toEqual([['m1', undefined]])
  })

  it('空数据 → 空数组', () => {
    expect(formatJson(['name'], [])).toEqual([])
  })
})

describe('placeTypeDesc', () => {
  it('placeType == 0 → 宿舍（含字符串 "0"，保留宽松 == 语义）', () => {
    expect(placeTypeDesc(0)).toBe('宿舍')
    expect(placeTypeDesc('0')).toBe('宿舍')
  })

  it('其它值 → 厂区', () => {
    expect(placeTypeDesc(1)).toBe('厂区')
    expect(placeTypeDesc('1')).toBe('厂区')
  })
})

describe('meterTagFields', () => {
  it('tagList 为 null → 空 tagName 与空 tagIds（保留原默认）', () => {
    expect(meterTagFields(null)).toEqual({ tagName: '', tagIds: [] })
  })

  it('tagList 为空数组 → 空 tagName 与空 tagIds', () => {
    expect(meterTagFields([])).toEqual({ tagName: '', tagIds: [] })
  })

  it('聚合多个标签：tagName 逗号拼接、tagIds 收集 id（顺序保持）', () => {
    expect(
      meterTagFields([
        { id: 1, tagName: '一楼' },
        { id: 2, tagName: '高压' }
      ])
    ).toEqual({ tagName: '一楼,高压', tagIds: [1, 2] })
  })

  it('单标签：tagName 无逗号', () => {
    expect(meterTagFields([{ id: 9, tagName: '总表' }])).toEqual({ tagName: '总表', tagIds: [9] })
  })
})
