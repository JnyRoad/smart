import { shallowMount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const fetchStaffList = vi.fn()
const fetchIscStaffCards = vi.fn()
const saveIscStaffCard = vi.fn()
const deleteIscStaffCard = vi.fn()
const fetchIscParkConfigs = vi.fn()
const fetchIscCardTaskList = vi.fn()
const staffStatusInit = vi.fn(status => (Number(status) === 0 ? '离职' : '在职'))
const mountedWrappers = []

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

vi.mock('@/filters/index', () => ({
  staffStatusInit
}))

const component = (await import('./index.vue')).default

const enabledPark = {
  parkId: 5000021,
  parkName: '裕同科技许昌园区',
  dispatcherParkId: 9001,
  dispatcherParkName: '许昌ISC',
  cardSyncEnabled: 1
}

const disabledPark = {
  parkId: 5000022,
  parkName: '未启用园区',
  dispatcherParkId: 9002,
  dispatcherParkName: '停用ISC',
  cardSyncEnabled: 0
}

const staff = {
  id: 1509092220633108482,
  badge: 'YD8800010',
  name: '王金鸽',
  status: 1,
  parkName: '裕同科技许昌园区',
  compName: '裕同科技',
  depName: '信息部'
}

const existingCard = {
  id: 7001,
  cardNo: '1024388800',
  dispatcherParkName: '许昌ISC',
  syncStatus: 1
}

function createResponse(data) {
  return { data: { data } }
}

function queueSummary(item) {
  return {
    badge: item.badge,
    cardNo: item.cardNo,
    parkId: item.parkId,
    dispatcherParkId: item.dispatcherParkId,
    status: item.status,
    message: item.message
  }
}

function expectedParkFields() {
  return {
    parkId: enabledPark.parkId,
    dispatcherParkId: enabledPark.dispatcherParkId
  }
}

function createPageWrapper() {
  const wrapper = shallowMount(component, {
    directives: {
      loading: {}
    },
    mocks: {
      $message: vi.fn(),
      $notify: vi.fn(),
      $router: { push: vi.fn() },
      $confirm: vi.fn()
    }
  })
  mountedWrappers.push(wrapper)
  return wrapper
}

async function flushPromises() {
  await Promise.resolve()
  await Promise.resolve()
}

describe('isc card fast add page flow', () => {
  afterEach(() => {
    mountedWrappers.splice(0).forEach(wrapper => wrapper.destroy())
  })

  beforeEach(() => {
    fetchStaffList.mockReset()
    fetchIscStaffCards.mockReset()
    saveIscStaffCard.mockReset()
    deleteIscStaffCard.mockReset()
    fetchIscParkConfigs.mockReset()
    fetchIscCardTaskList.mockReset()
    staffStatusInit.mockClear()
    fetchStaffList.mockResolvedValue(createResponse({ records: [] }))
    fetchIscStaffCards.mockResolvedValue(createResponse([]))
    saveIscStaffCard.mockResolvedValue(createResponse(true))
    deleteIscStaffCard.mockResolvedValue(createResponse(true))
    fetchIscParkConfigs.mockResolvedValue(createResponse({ records: [enabledPark, disabledPark] }))
    fetchIscCardTaskList.mockResolvedValue(createResponse({ records: [] }))
  })

  it('keeps the page flow from enabled park selection to card submit request stable', async () => {
    fetchStaffList.mockResolvedValue(createResponse({ records: [staff] }))
    fetchIscStaffCards.mockResolvedValue(createResponse([existingCard]))
    const wrapper = createPageWrapper()
    const expectedPark = expectedParkFields()
    await flushPromises()

    expect(wrapper.vm.iscParkOptions).toStrictEqual([enabledPark])

    wrapper.vm.searchForm.parkId = enabledPark.parkId
    wrapper.vm.searchForm.staffKeyword = staff.badge
    await wrapper.vm.searchStaff()
    await flushPromises()

    expect(fetchStaffList).toHaveBeenCalledWith({
      current: 1,
      size: 10,
      badges: staff.badge,
      parkId: enabledPark.parkId
    })
    expect(wrapper.vm.selectedStaff).toStrictEqual(staff)
    expect(fetchIscStaffCards).toHaveBeenCalledWith(staff.id)
    expect(wrapper.vm.staffCards).toStrictEqual([existingCard])

    wrapper.vm.cardNo = '1024388812'
    wrapper.vm.addCurrentCard()

    expect(wrapper.vm.queue).toHaveLength(1)
    expect(queueSummary(wrapper.vm.queue[0])).toStrictEqual({
      badge: staff.badge,
      cardNo: '1024388812',
      ...expectedPark,
      status: 'ready',
      message: ''
    })

    await wrapper.vm.submitQueue()
    await flushPromises()

    expect(saveIscStaffCard).toHaveBeenCalledWith({
      staffId: staff.id,
      parkId: enabledPark.parkId,
      cardNo: '1024388812'
    })
    expect(wrapper.vm.queue[0]).toMatchObject({
      status: 'success',
      message: '保存成功，ISC同步任务已创建'
    })
    expect(wrapper.vm.$notify).toHaveBeenCalledWith({
      title: '提交完成',
      message: '成功1条，失败0条',
      type: 'success'
    })
  })

  it('keeps batch paste staff lookup and park snapshot stable while resolving rows', async () => {
    let resolveStaffLookup
    fetchStaffList.mockReturnValue(new Promise(resolve => {
      resolveStaffLookup = resolve
    }))
    const wrapper = createPageWrapper()
    const expectedPark = expectedParkFields()
    await flushPromises()

    wrapper.vm.searchForm.parkId = enabledPark.parkId
    wrapper.vm.pasteText = '10288 1024388812\n10290 1024388845'

    const confirmPromise = wrapper.vm.confirmPaste()
    expect(wrapper.vm.pasteResolving).toBe(true)

    wrapper.vm.searchForm.parkId = ''
    resolveStaffLookup(createResponse({
      records: [
        { ...staff, id: 1, badge: '10288', name: '张三' },
        { ...staff, id: 2, badge: '10290', name: '李四' }
      ]
    }))
    await confirmPromise

    expect(fetchStaffList).toHaveBeenCalledWith({
      current: 1,
      size: 2,
      badges: '10288 10290'
    })
    expect(wrapper.vm.queue.map(queueSummary)).toStrictEqual([
      { badge: '10288', cardNo: '1024388812', ...expectedPark, status: 'ready', message: '' },
      { badge: '10290', cardNo: '1024388845', ...expectedPark, status: 'ready', message: '' }
    ])
    expect(wrapper.vm.pasteDialogVisible).toBe(false)
    expect(wrapper.vm.pasteResolving).toBe(false)
    expect(wrapper.vm.$message).toHaveBeenCalledWith({
      message: '批量数据已加入待提交队列',
      type: 'success'
    })
  })

  it('keeps batch paste missing staff rows as invalid rows for the selected park', async () => {
    fetchStaffList.mockResolvedValue(createResponse({
      records: [
        { ...staff, id: 1, badge: '10288', name: '张三' }
      ]
    }))
    const wrapper = createPageWrapper()
    const expectedPark = expectedParkFields()
    await flushPromises()

    wrapper.vm.searchForm.parkId = enabledPark.parkId
    wrapper.vm.pasteText = '10288 1024388812\n10299 1024388899'

    await wrapper.vm.confirmPaste()

    expect(wrapper.vm.queue.map(queueSummary)).toStrictEqual([
      { badge: '10288', cardNo: '1024388812', ...expectedPark, status: 'ready', message: '' },
      {
        badge: '10299',
        cardNo: '1024388899',
        ...expectedPark,
        status: 'invalid',
        message: '未找到该工号对应员工'
      }
    ])
  })

  it('keeps missing staff invalid rows on the paste park snapshot while resolving', async () => {
    let resolveStaffLookup
    fetchStaffList.mockReturnValue(new Promise(resolve => {
      resolveStaffLookup = resolve
    }))
    const wrapper = createPageWrapper()
    const expectedPark = expectedParkFields()
    await flushPromises()

    wrapper.vm.searchForm.parkId = enabledPark.parkId
    wrapper.vm.pasteText = '10299 1024388899'

    const confirmPromise = wrapper.vm.confirmPaste()
    wrapper.vm.searchForm.parkId = ''
    resolveStaffLookup(createResponse({ records: [] }))
    await confirmPromise

    expect(wrapper.vm.queue.map(queueSummary)).toStrictEqual([
      {
        badge: '10299',
        cardNo: '1024388899',
        ...expectedPark,
        status: 'invalid',
        message: '未找到该工号对应员工'
      }
    ])
  })
})
