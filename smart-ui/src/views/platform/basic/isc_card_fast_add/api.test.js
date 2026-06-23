import { beforeEach, describe, expect, it, vi } from 'vitest'

const fetchStaffList = vi.fn()
const fetchIscStaffCards = vi.fn()
const saveIscStaffCard = vi.fn()
const deleteIscStaffCard = vi.fn()
const fetchIscParkConfigs = vi.fn()
const fetchIscCardTaskList = vi.fn()

vi.mock('@/api/platform/basic/staff_info', () => ({
  fetchList: fetchStaffList
}))

vi.mock('@/api/platform/basic/staff_info_detail', () => ({
  fetchIscStaffCards,
  saveIscStaffCard,
  deleteIscStaffCard,
  fetchIscParkConfigs
}))

vi.mock('@/api/platform/records/isc_card_task', () => ({
  fetchList: fetchIscCardTaskList
}))

const api = await import('./api')

function createPageResponse(records) {
  return { data: { data: { records } } }
}

function createPayloadResponse(payload) {
  return { data: { data: payload } }
}

describe('isc card fast add api service', () => {
  beforeEach(() => {
    fetchStaffList.mockReset()
    fetchIscStaffCards.mockReset()
    saveIscStaffCard.mockReset()
    deleteIscStaffCard.mockReset()
    fetchIscParkConfigs.mockReset()
    fetchIscCardTaskList.mockReset()
  })

  it('keeps ISC park and task query signatures stable', async () => {
    fetchIscParkConfigs.mockResolvedValue(createPageResponse([{ parkId: 1 }]))
    fetchIscCardTaskList.mockResolvedValue(createPageResponse([{ id: 2 }]))

    await expect(api.fetchIscParkRecords()).resolves.toStrictEqual([{ parkId: 1 }])
    await expect(api.fetchRecentCardTaskRecords({
      parkId: 5000021,
      badge: 'YD8800010'
    })).resolves.toStrictEqual([{ id: 2 }])

    expect(fetchIscParkConfigs).toHaveBeenCalledWith({
      current: 1,
      size: 1000
    })
    expect(fetchIscCardTaskList).toHaveBeenCalledWith({
      current: 1,
      size: 8,
      parkId: 5000021,
      badge: 'YD8800010'
    })
  })

  it('keeps staff search request signatures stable', async () => {
    const park = { parkId: 5000021 }
    fetchStaffList
      .mockResolvedValueOnce(createPageResponse([{ badge: 'YD8800010' }]))
      .mockResolvedValueOnce(createPageResponse([{ name: '王金鸽' }]))

    await api.searchStaffByBadge('YD8800010', park)
    await api.searchStaffByName('王金鸽', park)

    expect(fetchStaffList).toHaveBeenNthCalledWith(1, {
      current: 1,
      size: 10,
      badges: 'YD8800010',
      parkId: 5000021
    })
    expect(fetchStaffList).toHaveBeenNthCalledWith(2, {
      current: 1,
      size: 10,
      name: '王金鸽',
      parkId: 5000021
    })
  })

  it('keeps batch badge lookup deduplicated and whitespace joined', async () => {
    fetchStaffList.mockResolvedValue(createPageResponse([
      { id: 1, badge: '10288' },
      { id: 2, badge: '10290' }
    ]))

    const staffMap = await api.fetchStaffMapByBadges(['10288', ' 10288 ', '', '10290'])

    expect(fetchStaffList).toHaveBeenCalledWith({
      current: 1,
      size: 2,
      badges: '10288 10290'
    })
    expect(staffMap).toStrictEqual({
      10288: { id: 1, badge: '10288' },
      10290: { id: 2, badge: '10290' }
    })
  })

  it('skips batch staff lookup when there is no badge', async () => {
    await expect(api.fetchStaffMapByBadges(['', '  '])).resolves.toStrictEqual({})

    expect(fetchStaffList).not.toHaveBeenCalled()
  })

  it('keeps card list parsing and card mutation calls delegated unchanged', async () => {
    const cards = [{ id: 7001, cardNo: '1024388812' }]
    const payload = { staffId: 1, parkId: 2, cardNo: '1024388812' }
    fetchIscStaffCards.mockResolvedValue(createPayloadResponse(cards))

    await expect(api.fetchStaffCardRecords(1)).resolves.toStrictEqual(cards)
    api.saveStaffCard(payload)
    api.deleteStaffCard(7001)

    expect(fetchIscStaffCards).toHaveBeenCalledWith(1)
    expect(saveIscStaffCard).toHaveBeenCalledWith(payload)
    expect(deleteIscStaffCard).toHaveBeenCalledWith(7001)
  })
})
