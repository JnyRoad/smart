import { describe, expect, it } from 'vitest'
import { normalizeCateInfos } from './water-elec-rules'

describe('normalizeCateInfos（旧版前端规则：过滤热水、冷水改名水）', () => {
  it('过滤热水、冷水→水，其余原样', () => {
    expect(
      normalizeCateInfos([
        { cateName: '热水', fee: 5 },
        { cateName: '冷水', fee: 10 },
        { cateName: '电', fee: 20 },
      ]),
    ).toEqual([
      { cateName: '水', fee: 10 },
      { cateName: '电', fee: 20 },
    ])
  })

  it('空数组与缺字段容错', () => {
    expect(normalizeCateInfos([])).toEqual([])
    expect(normalizeCateInfos(undefined)).toEqual([])
    expect(normalizeCateInfos([{ fee: 3 }])).toEqual([{ fee: 3 }])
  })
})
