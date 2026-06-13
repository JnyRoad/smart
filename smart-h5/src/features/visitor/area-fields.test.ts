import { describe, expect, it } from 'vitest'
import { buildVisitorAreaFields } from './area-fields'
import type { FactoryAreaConfig } from './api'

const FACTORIES: FactoryAreaConfig[] = [
  { factoryType: 'NEW01', areaFlag: 1, areas: [] },
  { factoryType: 'OLD01', areaFlag: 0, areas: [] },
]

describe('buildVisitorAreaFields（旧版 syncVisitorAreaFields 语义）', () => {
  it('新工厂（areaFlag=1）：custom 进 permitArea，areaType 取 list', () => {
    expect(
      buildVisitorAreaFields('NEW01', { NEW01: { list: ['A1', 'A2'], custom: '三楼会议室' } }, FACTORIES),
    ).toEqual({
      permitFactoryType: 'NEW01',
      permitArea: '三楼会议室',
      permitOldArea: '',
      areaType: ['A1', 'A2'],
    })
  })

  it('老工厂（areaFlag=0）：custom 进 permitOldArea', () => {
    expect(
      buildVisitorAreaFields('OLD01', { OLD01: { list: ['B1'], custom: '老车间' } }, FACTORIES),
    ).toEqual({
      permitFactoryType: 'OLD01',
      permitArea: '',
      permitOldArea: '老车间',
      areaType: ['B1'],
    })
  })

  it('无 custom 时两个 permit 字段都为空串', () => {
    expect(
      buildVisitorAreaFields('NEW01', { NEW01: { list: ['A1'], custom: '' } }, FACTORIES),
    ).toEqual({ permitFactoryType: 'NEW01', permitArea: '', permitOldArea: '', areaType: ['A1'] })
  })

  it('只取当前工厂，不聚合其他工厂的选择', () => {
    const result = buildVisitorAreaFields(
      'NEW01',
      { NEW01: { list: ['A1'], custom: '' }, OLD01: { list: ['B1', 'B2'], custom: '别处' } },
      FACTORIES,
    )
    expect(result.areaType).toEqual(['A1'])
    expect(result.permitOldArea).toBe('')
  })

  it('工厂缺失/未选时默认按新工厂、空区域', () => {
    expect(buildVisitorAreaFields(undefined, {}, FACTORIES)).toEqual({
      permitFactoryType: '',
      permitArea: '',
      permitOldArea: '',
      areaType: [],
    })
  })
})
