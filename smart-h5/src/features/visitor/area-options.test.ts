import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as api from './api'
import { loadAreaOptions, pruneSelectedAreas } from './area-options'

vi.mock('./api')
const mocked = vi.mocked(api)

beforeEach(() => {
  localStorage.clear()
  vi.resetAllMocks()
})

// Real area-options response: data is an object; factoryType is the backend
// value ("15"/"16"); areas use areaCode (number) / areaName.
const RAW = {
  parkId: 5000021,
  inlineAreaLimit: 8,
  factories: [
    {
      factoryType: '16',
      factoryName: '老工厂',
      areaFlag: 0,
      sort: 2,
      areas: [{ areaCode: 7, areaName: 'A栋', isCommon: false, sort: 1 }],
    },
    {
      factoryType: '15',
      factoryName: '新工厂',
      areaFlag: 1,
      sort: 1,
      areas: [
        { areaCode: 3, areaName: '外围', isCommon: false, sort: 4 },
        { areaCode: 6, areaName: '其他区域', isCommon: false, sort: 5 },
      ],
    },
  ],
}

describe('loadAreaOptions', () => {
  it('解析真实结构：保留后端 factoryType、areaCode 转字符串、按 sort 排序、塞全局 inlineAreaLimit', async () => {
    mocked.getAreaOptions.mockResolvedValue({ code: 0, data: RAW })
    const result = await loadAreaOptions(5000021)
    // sort：新工厂(sort1) 在前、老工厂(sort2) 在后
    expect(result.map((f) => f.factoryType)).toEqual(['15', '16'])
    expect(result[0]?.factoryName).toBe('新工厂')
    expect(result[0]?.inlineAreaLimit).toBe(8)
    expect(result[0]?.areas).toEqual([
      { code: '3', name: '外围', isCommon: 0 },
      { code: '6', name: '其他区域', isCommon: 0 },
    ])
    // 缓存写入解析后的结构
    expect(localStorage.getItem('visitor-area-options-5000021')).toContain('"factoryType":"15"')
  })

  it('接口失败：回退到缓存（解析后的结构）', async () => {
    localStorage.setItem(
      'visitor-area-options-5000021',
      JSON.stringify([{ factoryType: '15', factoryName: '新工厂', areaFlag: 1, areas: [{ code: '3', name: '外围' }] }]),
    )
    mocked.getAreaOptions.mockRejectedValue(new Error('boom'))
    const result = await loadAreaOptions(5000021)
    expect(result[0]?.factoryType).toBe('15')
  })

  it('接口失败且无缓存：返回空配置（不编造工厂）', async () => {
    mocked.getAreaOptions.mockRejectedValue(new Error('boom'))
    const result = await loadAreaOptions(5000021)
    expect(result).toEqual([])
  })

  it('接口返回空 factories：返回空配置', async () => {
    mocked.getAreaOptions.mockResolvedValue({ code: 0, data: { parkId: 5000021, factories: [] } })
    const result = await loadAreaOptions(5000021)
    expect(result).toEqual([])
  })

  it('工厂缺 factoryType 的条目被过滤掉', async () => {
    mocked.getAreaOptions.mockResolvedValue({
      code: 0,
      data: { factories: [{ factoryName: '无类型', areaFlag: 1, areas: [{ areaCode: 1, areaName: 'X' }] }] },
    })
    const result = await loadAreaOptions(5000021)
    expect(result).toEqual([])
  })
})

describe('pruneSelectedAreas', () => {
  const CONFIG = [
    {
      factoryType: '15',
      factoryName: '新工厂',
      areaFlag: 1,
      areas: [
        { code: '3', name: '外围' },
        { code: '6', name: '其他区域' },
      ],
    },
  ]

  it('剪除配置中已不存在的区域码与厂区', () => {
    const pruned = pruneSelectedAreas(
      {
        '15': { list: ['3', 'DEAD'], custom: 'x' },
        GONE: { list: ['9'], custom: '' },
      },
      CONFIG,
    )
    expect(pruned).toEqual({ '15': { list: ['3'], custom: 'x' } })
  })

  it('全部失效时返回空对象', () => {
    expect(pruneSelectedAreas({ GONE: { list: ['9'], custom: '' } }, CONFIG)).toEqual({})
  })
})
