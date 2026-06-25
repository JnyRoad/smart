import { beforeEach, describe, expect, it, vi } from 'vitest'

const fetchStaffList = vi.fn()

vi.mock('@/api/platform/basic/staff_info', () => ({
  fetchList: fetchStaffList
}))

vi.mock('@/api/platform/basic/staff_info_detail', () => ({
  fetchIscStaffCards: vi.fn(),
  saveIscStaffCard: vi.fn(),
  deleteIscStaffCard: vi.fn(),
  fetchIscParkConfigs: vi.fn()
}))

vi.mock('@/api/platform/records/isc_card_task', () => ({
  fetchList: vi.fn()
}))

vi.mock('@/filters/index', () => ({
  staffStatusInit: vi.fn()
}))

const component = (await import('./index.vue')).default

function createSearchContext(staffKeyword) {
  return {
    selectedPark: { parkId: 5000021, parkName: '裕同科技许昌园区' },
    searchForm: { staffKeyword },
    staffLoading: false,
    searchExactStaffByBadge: vi.fn(),
    $message: vi.fn()
  }
}

describe('isc card fast add staff search', () => {
  beforeEach(() => {
    fetchStaffList.mockReset()
    fetchStaffList.mockResolvedValue({ data: { data: { records: [] } } })
  })

  it('treats alphanumeric badge as employee badge search', () => {
    const context = createSearchContext('YD8800010')

    component.methods.searchStaff.call(context)

    expect(context.searchExactStaffByBadge).toHaveBeenCalledWith('YD8800010', true)
    expect(fetchStaffList).not.toHaveBeenCalled()
  })

  it('keeps numeric badge search on exact employee badge path', () => {
    const context = createSearchContext('1217999')

    component.methods.searchStaff.call(context)

    expect(context.searchExactStaffByBadge).toHaveBeenCalledWith('1217999', true)
    expect(fetchStaffList).not.toHaveBeenCalled()
  })

  it('searches exact badge within the selected ISC park', async () => {
    const staff = {
      id: 1509092220633108482,
      badge: 'YD8800010',
      name: '王金鸽',
      status: 1
    }
    fetchStaffList.mockResolvedValue({ data: { data: { records: [staff] } } })
    const context = {
      selectedPark: { parkId: 5000021, parkName: '裕同科技许昌园区' },
      staffCandidates: [],
      selectedStaff: null,
      staffCards: [],
      staffLoading: true,
      applyStaffSearchResult: component.methods.applyStaffSearchResult,
      selectStaff: vi.fn(),
      $message: vi.fn()
    }

    await component.methods.searchExactStaffByBadge.call(context, 'YD8800010')

    expect(fetchStaffList).toHaveBeenCalledWith({
      current: 1,
      size: 10,
      badges: 'YD8800010',
      parkId: 5000021
    })
    expect(context.staffCandidates).toStrictEqual([staff])
    expect(context.selectStaff).toHaveBeenCalledWith(staff)
    expect(context.staffLoading).toBe(false)
  })

  it('falls back to selected-park name search when alphanumeric keyword is not a badge', async () => {
    const staff = {
      id: 1001,
      badge: '1001',
      name: 'ALICE',
      status: 1
    }
    fetchStaffList
      .mockResolvedValueOnce({ data: { data: { records: [] } } })
      .mockResolvedValueOnce({ data: { data: { records: [staff] } } })
    const context = {
      selectedPark: { parkId: 5000021, parkName: '裕同科技许昌园区' },
      searchForm: { staffKeyword: 'ALICE' },
      staffCandidates: [],
      selectedStaff: null,
      staffCards: [],
      staffLoading: false,
      searchExactStaffByBadge: component.methods.searchExactStaffByBadge,
      searchStaffByName: component.methods.searchStaffByName,
      applyStaffSearchResult: component.methods.applyStaffSearchResult,
      selectStaff: vi.fn(),
      $message: vi.fn()
    }

    await component.methods.searchStaff.call(context)

    expect(fetchStaffList).toHaveBeenNthCalledWith(1, {
      current: 1,
      size: 10,
      badges: 'ALICE',
      parkId: 5000021
    })
    expect(fetchStaffList).toHaveBeenNthCalledWith(2, {
      current: 1,
      size: 10,
      name: 'ALICE',
      parkId: 5000021
    })
    expect(context.selectStaff).toHaveBeenCalledWith(staff)
  })
})
