import { describe, expect, it, vi } from 'vitest'
import {
  buildRecentTaskQuery,
  canApplyStaffCardResult,
  emptyStaffCardState,
  resolveBadgeSearchResult,
  resolveNameSearchResult,
  runBadgeStaffSearch,
  runNameStaffSearch
} from './staff-flow'

const enabledPark = {
  parkId: 5000021,
  parkName: '裕同科技许昌园区'
}

const staff = {
  id: 1509092220633108482,
  badge: 'YD8800010',
  name: '王金鸽',
  status: 1
}

describe('isc card fast add staff flow', () => {
  it('keeps recent task query scoped by selected park and selected staff badge', () => {
    expect(buildRecentTaskQuery({
      searchForm: { parkId: enabledPark.parkId },
      selectedStaff: staff
    })).toStrictEqual({
      parkId: enabledPark.parkId,
      badge: staff.badge
    })

    expect(buildRecentTaskQuery({
      searchForm: { parkId: '' },
      selectedStaff: null
    })).toStrictEqual({
      parkId: '',
      badge: null
    })
  })

  it('keeps name search auto-select limited to a single exact match', () => {
    expect(resolveNameSearchResult([], '王金鸽')).toStrictEqual({
      staffCandidates: [],
      selectedStaff: null,
      staffCards: [],
      staffToSelect: null,
      message: { message: '未找到匹配员工', type: 'warning' }
    })

    expect(resolveNameSearchResult([
      { ...staff, id: 1, name: '王金鸽' },
      { ...staff, id: 2, name: '王金鸽' }
    ], '王金鸽')).toStrictEqual({
      staffCandidates: [
        { ...staff, id: 1, name: '王金鸽' },
        { ...staff, id: 2, name: '王金鸽' }
      ],
      selectedStaff: null,
      staffCards: [],
      staffToSelect: null,
      message: { message: '找到2名候选员工，请手动选择', type: 'warning' }
    })

    const exactResult = resolveNameSearchResult([
      { ...staff, id: 1, name: '王金鸽' },
      { ...staff, id: 2, name: '王金' }
    ], '王金鸽')

    expect(exactResult).toStrictEqual({
      staffCandidates: [
        { ...staff, id: 1, name: '王金鸽' },
        { ...staff, id: 2, name: '王金' }
      ],
      selectedStaff: null,
      staffCards: [],
      staffToSelect: { ...staff, id: 1, name: '王金鸽' },
      message: null
    })
  })

  it('keeps badge search result handling for no match, one match, and ambiguous matches', () => {
    expect(resolveBadgeSearchResult([])).toStrictEqual({
      staffCandidates: [],
      selectedStaff: null,
      staffCards: [],
      staffToSelect: null,
      message: { message: '未找到该工号对应员工', type: 'warning' }
    })

    expect(resolveBadgeSearchResult([staff])).toStrictEqual({
      staffCandidates: [staff],
      selectedStaff: null,
      staffCards: [],
      staffToSelect: staff,
      message: null
    })

    expect(resolveBadgeSearchResult([
      { ...staff, id: 1 },
      { ...staff, id: 2 }
    ])).toStrictEqual({
      staffCandidates: [
        { ...staff, id: 1 },
        { ...staff, id: 2 }
      ],
      selectedStaff: null,
      staffCards: [],
      staffToSelect: null,
      message: { message: '找到2名候选员工，请手动选择', type: 'warning' }
    })
  })

  it('keeps alphanumeric badge fallback on selected-park name search', async () => {
    const requestStaffByBadge = vi.fn().mockResolvedValue([])
    const nameMatchedStaff = { ...staff, name: 'ALICE' }
    const requestStaffByName = vi.fn().mockResolvedValue([nameMatchedStaff])

    const result = await runBadgeStaffSearch({
      badge: 'ALICE',
      selectedPark: enabledPark,
      fallbackToName: true,
      requestStaffByBadge,
      requestStaffByName
    })

    expect(requestStaffByBadge).toHaveBeenCalledWith('ALICE', enabledPark)
    expect(requestStaffByName).toHaveBeenCalledWith('ALICE', enabledPark)
    expect(result).toStrictEqual({
      staffCandidates: [nameMatchedStaff],
      selectedStaff: null,
      staffCards: [],
      staffToSelect: nameMatchedStaff,
      message: null
    })
  })

  it('keeps badge fallback reading the current park after the badge miss', async () => {
    const changedPark = {
      parkId: 5000022,
      parkName: '裕同科技九江园区'
    }
    const requestStaffByBadge = vi.fn().mockResolvedValue([])
    const requestStaffByName = vi.fn().mockResolvedValue([])

    await runBadgeStaffSearch({
      badge: 'ALICE',
      selectedPark: enabledPark,
      fallbackToName: true,
      readFallbackPark: () => changedPark,
      requestStaffByBadge,
      requestStaffByName
    })

    expect(requestStaffByBadge).toHaveBeenCalledWith('ALICE', enabledPark)
    expect(requestStaffByName).toHaveBeenCalledWith('ALICE', changedPark)
  })

  it('keeps direct name search request and result resolution together', async () => {
    const requestStaffByName = vi.fn().mockResolvedValue([staff])

    await expect(runNameStaffSearch({
      keyword: '王金鸽',
      selectedPark: enabledPark,
      requestStaffByName
    })).resolves.toStrictEqual({
      staffCandidates: [staff],
      selectedStaff: null,
      staffCards: [],
      staffToSelect: staff,
      message: null
    })

    expect(requestStaffByName).toHaveBeenCalledWith('王金鸽', enabledPark)
  })

  it('keeps stale staff card responses from updating the visible card list', () => {
    expect(canApplyStaffCardResult(staff, staff.id)).toBe(true)
    expect(canApplyStaffCardResult(null, staff.id)).toBe(false)
    expect(canApplyStaffCardResult({ ...staff, id: 999 }, staff.id)).toBe(false)
    expect(emptyStaffCardState()).toStrictEqual({
      staffCards: [],
      staffCardLoading: false
    })
  })
})
