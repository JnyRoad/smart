import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'
import { normalizeCommonAreaConfig } from './common-area-config.js'

const here = path.dirname(fileURLToPath(import.meta.url))

describe('common-area-config module format', () => {
  it('uses ES module exports (CommonJS breaks the production chunk)', () => {
    const source = fs.readFileSync(path.join(here, 'common-area-config.js'), 'utf8')
    expect(/\bmodule\.exports\b|\bexports\./.test(source)).toBe(false)
  })
})

describe('normalizeCommonAreaConfig', () => {
  const standardAreas = {
    newAreas: [
      { code: 'N1', desc: '新厂一区' },
      { code: 'N2', desc: '新厂二区' }
    ],
    oldAreas: [{ code: 'O1', desc: '老厂一区' }]
  }

  it('builds the default config from legacy newAreas/oldAreas', () => {
    expect(normalizeCommonAreaConfig(null, standardAreas)).toStrictEqual({
      inlineAreaLimit: 4,
      factories: [
        {
          factoryType: '15',
          factoryName: '新工厂',
          areaFlag: 1,
          sort: 1,
          areas: [
            { areaCode: 'N1', areaName: '新厂一区', isCommon: false, sort: 1 },
            { areaCode: 'N2', areaName: '新厂二区', isCommon: false, sort: 2 }
          ]
        },
        {
          factoryType: '16',
          factoryName: '老工厂',
          areaFlag: 0,
          sort: 2,
          areas: [{ areaCode: 'O1', areaName: '老厂一区', isCommon: false, sort: 1 }]
        }
      ]
    })
  })

  it('merges a stored config: keeps flags/sorts, refreshes names, drops stale areas', () => {
    const configuredValue = JSON.stringify({
      inlineAreaLimit: 6,
      factories: [
        {
          areaFlag: 1,
          areas: [
            { areaCode: 'N1', areaName: '旧名称', isCommon: 1, sort: 9 },
            { areaCode: 'STALE', areaName: '已删除区域', isCommon: 1, sort: 1 }
          ]
        },
        {
          areaFlag: 0,
          areas: [{ areaCode: 'O1', areaName: '旧老厂一区', isCommon: true, sort: '3' }]
        }
      ]
    })

    const merged = normalizeCommonAreaConfig(configuredValue, standardAreas)

    expect(merged.inlineAreaLimit).toBe(6)
    expect(merged.factories[0].areas).toStrictEqual([
      { areaCode: 'N1', areaName: '新厂一区', isCommon: true, sort: 9 },
      { areaCode: 'N2', areaName: '新厂二区', isCommon: false, sort: 2 }
    ])
    expect(merged.factories[1].areas).toStrictEqual([{ areaCode: 'O1', areaName: '老厂一区', isCommon: true, sort: 3 }])
  })

  it('builds the default config from dynamic factories', () => {
    const dynamicStandardAreas = {
      factories: [
        {
          factoryType: '88',
          factoryName: '技术园区',
          areaFlag: 3,
          sort: 3,
          areas: [{ areaCode: 'R1', areaName: '研发楼' }]
        }
      ]
    }

    expect(normalizeCommonAreaConfig(null, dynamicStandardAreas)).toStrictEqual({
      inlineAreaLimit: 4,
      factories: [
        {
          factoryType: '88',
          factoryName: '技术园区',
          areaFlag: 3,
          sort: 3,
          areas: [{ areaCode: 'R1', areaName: '研发楼', isCommon: false, sort: 1 }]
        }
      ]
    })
  })

  it('matches factories by factoryType when areaFlag values collide', () => {
    const sameFlagStandardAreas = {
      factories: [
        {
          factoryType: '15',
          factoryName: '新工厂',
          areaFlag: 1,
          sort: 1,
          areas: [{ areaCode: 'N1', areaName: '新厂一区' }]
        },
        {
          factoryType: '88',
          factoryName: '技术园区',
          areaFlag: 1,
          sort: 2,
          areas: [{ areaCode: 'R1', areaName: '研发楼' }]
        }
      ]
    }
    const sameFlagConfiguredValue = JSON.stringify({
      factories: [
        {
          factoryType: '88',
          areaFlag: 1,
          areas: [{ areaCode: 'R1', isCommon: true, sort: 8 }]
        },
        {
          factoryType: '15',
          areaFlag: 1,
          areas: [{ areaCode: 'N1', isCommon: true, sort: 3 }]
        }
      ]
    })

    const sameFlagConfig = normalizeCommonAreaConfig(sameFlagConfiguredValue, sameFlagStandardAreas)

    expect(sameFlagConfig.factories[0].areas).toStrictEqual([{ areaCode: 'N1', areaName: '新厂一区', isCommon: true, sort: 3 }])
    expect(sameFlagConfig.factories[1].areas).toStrictEqual([{ areaCode: 'R1', areaName: '研发楼', isCommon: true, sort: 8 }])
  })

  it('drops factories without a factoryType', () => {
    const config = normalizeCommonAreaConfig(null, {
      factories: [
        {
          factoryType: null,
          factoryName: '无效厂区',
          areas: [{ areaCode: 'X1', areaName: '无效区域' }]
        },
        {
          factoryType: '88',
          factoryName: '技术园区',
          areas: [{ areaCode: 'R1', areaName: '研发楼' }]
        }
      ]
    })

    expect(config.factories.length).toBe(1)
    expect(config.factories[0].factoryType).toBe('88')
  })
})
