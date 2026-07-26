import { getAreaOptions, type AreaOptionsResponse, type FactoryAreaConfig, type VisitorFaceDraft } from './api'
import type { AreaSelection } from './flow-store'

const cacheKey = (parkId: number) => `visitor-area-options-${parkId}`

export function clearAreaOptionsCache(parkId: number): void {
  localStorage.removeItem(cacheKey(parkId))
}

/**
 * Normalize the raw area-options response (data is an object holding the real
 * factories with their backend factoryType "15"/"16"). The backend validates
 * the submitted permitFactoryType against this list, so the factoryType MUST be
 * the real value — never a frontend-invented placeholder. Area codes are kept as
 * strings internally (the save call converts to numbers); the global
 * inlineAreaLimit is copied onto each factory for the info page.
 */
function normalizeAreaOptions(data?: AreaOptionsResponse): FactoryAreaConfig[] {
  const factories = data?.factories ?? []
  const limit = Number(data?.inlineAreaLimit) > 0 ? Number(data?.inlineAreaLimit) : 4
  return [...factories]
    .sort((left, right) => (Number(left.sort) || 0) - (Number(right.sort) || 0))
    .map((factory) => {
      const areaFlag = Number(factory.areaFlag)
      const areas = [...(factory.areas ?? [])]
        // 丢弃无 code / 无 name 的脏条目（空白胶囊），并按 sort 排（对齐旧版 normalizeFactory）。
        .filter((area) => area.areaCode != null && (area.areaName ?? '') !== '')
        .sort((left, right) => (Number(left.sort) || 0) - (Number(right.sort) || 0))
        .map((area) => ({ code: String(area.areaCode), name: String(area.areaName), isCommon: area.isCommon ? 1 : 0 }))
      return {
        factoryType: factory.factoryType != null ? String(factory.factoryType) : '',
        factoryName: factory.factoryName ?? '',
        areaFlag: Number.isNaN(areaFlag) ? undefined : areaFlag,
        inlineAreaLimit: limit,
        areas,
      }
    })
    .filter((factory) => factory.factoryType !== '')
}

/**
 * Area-config loading chain (mirrors legacy getAreaOptionsData):
 * area-options API -> localStorage cache. There is NO factory/type-enum
 * fallback: that endpoint returns area enums without a real factoryType, so any
 * assembled factory would be rejected by the backend. When nothing is available
 * we return an empty list and the caller shows 「配置不可用」.
 */
export async function loadAreaOptions(parkId: number, visitorDraft?: VisitorFaceDraft): Promise<FactoryAreaConfig[]> {
  try {
    if (!visitorDraft) {
      throw new Error('访客操作授权已失效，请重新进入申请流程')
    }
    const res = await getAreaOptions(parkId, visitorDraft)
    if (res.code === 0 && res.data) {
      const parsed = normalizeAreaOptions(res.data)
      if (parsed.length > 0) {
        localStorage.setItem(cacheKey(parkId), JSON.stringify(parsed))
        return parsed
      }
    }
  } catch {
    // fall through to cache
  }

  const cached = localStorage.getItem(cacheKey(parkId))
  if (cached !== null) {
    try {
      const arr = JSON.parse(cached) as FactoryAreaConfig[]
      if (Array.isArray(arr) && arr.length > 0) return arr
    } catch {
      localStorage.removeItem(cacheKey(parkId))
    }
  }

  return []
}

/** Pre-submit re-check: drop factories and area codes no longer present in the config. */
export function pruneSelectedAreas(
  selected: Record<string, AreaSelection>,
  options: FactoryAreaConfig[],
): Record<string, AreaSelection> {
  const validCodes: Record<string, Set<string>> = {}
  for (const factory of options) {
    if (factory.factoryType) {
      validCodes[factory.factoryType] = new Set((factory.areas ?? []).map((a) => a.code))
    }
  }
  const result: Record<string, AreaSelection> = {}
  for (const [factoryType, selection] of Object.entries(selected)) {
    const codes = validCodes[factoryType]
    if (!codes) continue
    const list = selection.list.filter((code) => codes.has(code))
    if (list.length > 0) result[factoryType] = { ...selection, list }
  }
  return result
}
