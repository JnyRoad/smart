import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'

const api = vi.hoisted(() => ({
  fetchOperationBatchPage: vi.fn(),
  getOperationBatchDetail: vi.fn(),
  fetchOperationTargetPage: vi.fn()
}))

vi.mock('@/api/platform/area/limit', () => api)

const AuthOperationProgress = (await import('./AuthOperationProgress.vue')).default

const stubs = {
  ElDialog: {
    name: 'ElDialog',
    props: ['visible', 'title'],
    template: '<section v-show="visible" class="dialog-stub"><h2>{{ title }}</h2><slot /></section>'
  },
  ElInput: {
    name: 'ElInput',
    props: ['value', 'placeholder', 'clearable', 'size'],
    template: '<input :value="value" :placeholder="placeholder" @input="$emit(\'input\', $event.target.value)" />'
  },
  ElSelect: {
    name: 'ElSelect',
    props: ['value', 'placeholder', 'clearable', 'size'],
    template: '<select :value="value" @change="$emit(\'input\', $event.target.value)"><slot /></select>'
  },
  ElOption: {
    name: 'ElOption',
    props: ['label', 'value'],
    template: '<option :value="value">{{ label }}</option>'
  },
  ElButton: {
    name: 'ElButton',
    props: ['disabled', 'loading', 'type', 'plain', 'size'],
    template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
  },
  ElTable: {
    name: 'ElTable',
    props: ['data', 'loading'],
    template: '<div class="table-stub"><slot /></div>'
  },
  ElTableColumn: {
    name: 'ElTableColumn',
    props: ['label', 'prop', 'width', 'minWidth'],
    template: '<span class="column-stub">{{ label }}</span>'
  },
  ElPagination: {
    name: 'ElPagination',
    props: ['currentPage', 'pageSize', 'pageSizes', 'total'],
    template: '<div class="pagination-stub">{{ total }}</div>'
  },
  ElProgress: {
    name: 'ElProgress',
    props: ['percentage', 'status'],
    template: '<div class="progress-stub">{{ percentage }}%</div>'
  }
}

function flushPromises () {
  return Promise.resolve().then(() => Promise.resolve()).then(() => Promise.resolve())
}

function pageResponse (records, total = records.length) {
  return Promise.resolve({
    data: {
      data: {
        records,
        total,
        current: 1,
        size: 20,
        pages: Math.ceil(total / 20)
      }
    }
  })
}

function detailResponse (batchId, status = 'EXECUTING', progress = {}) {
  return Promise.resolve({
    data: {
      data: {
        batchId,
        parkId: '26',
        action: 'DELETE',
        sourceType: 'STAFF_AUTHORITY',
        sourceId: 'operation-key-001',
        status,
        expectedCount: 1,
        expandedCount: 1,
        acceptedAt: '2026-09-05T08:30:00Z',
        progress: {
          batchId,
          batchStatus: status,
          expectedCount: 1,
          expandedCount: 1,
          totalTargetCount: 1,
          preparingCount: 0,
          queuedCount: 0,
          executingCount: 0,
          waitingConfirmCount: 0,
          verifyingCount: 0,
          confirmedCount: 0,
          convergedCount: 0,
          failedCount: 0,
          unfinishedCount: 1,
          ...progress
        }
      }
    }
  })
}

function targetPageResponse (records = [], total = records.length) {
  return Promise.resolve({ data: { data: { records, total, current: 1, size: 20 } } })
}

function mountProgress (value = true) {
  return mount(AuthOperationProgress, {
    propsData: { value },
    stubs,
    directives: {
      loading: () => {}
    }
  })
}

describe('权限任务进度面板', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    api.fetchOperationBatchPage.mockReset()
    api.getOperationBatchDetail.mockReset()
    api.fetchOperationTargetPage.mockReset()
    api.fetchOperationBatchPage.mockImplementation(() => pageResponse([]))
    api.getOperationBatchDetail.mockImplementation(batchId => detailResponse(batchId))
    api.fetchOperationTargetPage.mockImplementation(() => targetPageResponse())
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('关闭面板收到回执并打开时只查询一次精确操作', async () => {
    const wrapper = mount(AuthOperationProgress, { propsData: { value: false }, stubs })
    await wrapper.setProps({ value: true, operationKey: 'just-accepted' })
    await flushPromises()
    expect(api.fetchOperationBatchPage).toHaveBeenCalledTimes(1)
    expect(api.fetchOperationBatchPage).toHaveBeenCalledWith({ current: 1, size: 20, sourceId: 'just-accepted' })
    wrapper.destroy()
  })

  it('收到操作键后精确分页，并清除上一次批次及筛选', async () => {
    const wrapper = mount(AuthOperationProgress, { propsData: { value: true, operationKey: 'operation-one' }, stubs })
    await flushPromises()
    expect(api.fetchOperationBatchPage).toHaveBeenLastCalledWith(expect.objectContaining({ sourceId: 'operation-one', current: 1, size: 20 }))
    await wrapper.setData({ selectedBatchId: '9223372036854775806', selectedBatch: { status: 'CONVERGED' }, targets: [{ targetId: 'old' }], batchFilters: { sourceId: 'manual', status: 'FAILED' } })
    await wrapper.setProps({ operationKey: 'operation-two' })
    await flushPromises()
    expect(api.fetchOperationBatchPage).toHaveBeenLastCalledWith({ sourceId: 'operation-two', current: 1, size: 20 })
    expect(wrapper.vm.selectedBatchId).toBe('')
    expect(wrapper.vm.targets).toEqual([])
    wrapper.destroy()
  })

  it('旧操作分页响应迟到不能污染新操作，无结果也不回退全部', async () => {
    let resolveOld
    api.fetchOperationBatchPage.mockImplementation(params => params.sourceId === 'old' ? new Promise(resolve => { resolveOld = resolve }) : pageResponse([]))
    const wrapper = mount(AuthOperationProgress, { propsData: { value: true, operationKey: 'old' }, stubs })
    await wrapper.setProps({ operationKey: 'new' })
    await flushPromises()
    expect(wrapper.vm.batches).toEqual([])
    resolveOld({ data: { data: { records: [{ batchId: '9223372036854775806' }], total: 1 } } })
    await flushPromises()
    expect(wrapper.vm.batches).toEqual([])
    expect(api.fetchOperationBatchPage.mock.calls.every(([params]) => ['old', 'new'].includes(params.sourceId))).toBe(true)
    wrapper.destroy()
  })

  it('万条批次结果只读取当前服务端分页，不逐条请求详情或目标', async () => {
    api.fetchOperationBatchPage.mockImplementation(() => pageResponse([
      {
        batchId: '9007199254740993',
        parkId: '26',
        action: 'DELETE',
        status: 'EXECUTING',
        expectedCount: 10000,
        expandedCount: 20,
        acceptedAt: '2026-09-05T08:30:00Z'
      }
    ], 10000))

    const wrapper = mountProgress()
    await flushPromises()

    expect(wrapper.vm.batchPage.total).toBe(10000)
    expect(wrapper.vm.batches).toHaveLength(1)
    expect(api.fetchOperationBatchPage).toHaveBeenCalledTimes(1)
    expect(api.fetchOperationBatchPage).toHaveBeenCalledWith({ current: 1, size: 20 })
    expect(api.getOperationBatchDetail).not.toHaveBeenCalled()
    expect(api.fetchOperationTargetPage).not.toHaveBeenCalled()
    wrapper.destroy()
  })

  it('超过安全整数的批次 ID 在详情和目标请求中保持字符串原值', async () => {
    const wrapper = mountProgress(false)
    await wrapper.setProps({ value: true })
    await flushPromises()

    await wrapper.vm.selectBatch({ batchId: '9007199254740993', status: 'EXECUTING' })
    await flushPromises()

    expect(wrapper.vm.selectedBatchId).toBe('9007199254740993')
    expect(api.getOperationBatchDetail).toHaveBeenCalledWith('9007199254740993')
    expect(api.fetchOperationTargetPage).toHaveBeenCalledWith({
      batchId: '9007199254740993',
      current: 1,
      size: 20
    })
    wrapper.destroy()
  })

  it('快速切换批次时旧详情响应不能覆盖新选择', async () => {
    let resolveFirst
    let resolveSecond
    api.getOperationBatchDetail.mockImplementation(batchId => new Promise(resolve => {
      if (batchId === 'batch-old') resolveFirst = resolve
      if (batchId === 'batch-new') resolveSecond = resolve
    }))

    const wrapper = mountProgress()
    await flushPromises()
    const oldRequest = wrapper.vm.selectBatch({ batchId: 'batch-old', status: 'EXECUTING' })
    const newRequest = wrapper.vm.selectBatch({ batchId: 'batch-new', status: 'VERIFYING' })

    resolveSecond((await detailResponse('batch-new', 'VERIFYING')))
    await flushPromises()
    resolveFirst((await detailResponse('batch-old', 'EXECUTING')))
    await Promise.all([oldRequest, newRequest])
    await flushPromises()

    expect(wrapper.vm.selectedBatchId).toBe('batch-new')
    expect(wrapper.vm.selectedBatch.batchId).toBe('batch-new')
    expect(wrapper.vm.selectedBatch.status).toBe('VERIFYING')
    wrapper.destroy()
  })

  it('关闭面板后停止活跃批次轮询', async () => {
    const wrapper = mountProgress()
    await flushPromises()
    await wrapper.vm.selectBatch({ batchId: 'batch-active', status: 'EXECUTING' })
    await flushPromises()

    const firstDetailCalls = api.getOperationBatchDetail.mock.calls.length
    const firstTargetCalls = api.fetchOperationTargetPage.mock.calls.length
    await vi.advanceTimersByTimeAsync(4000)
    await flushPromises()

    expect(api.getOperationBatchDetail.mock.calls.length).toBeGreaterThan(firstDetailCalls)
    expect(api.fetchOperationTargetPage.mock.calls.length).toBeGreaterThan(firstTargetCalls)
    const detailCallsAfterPoll = api.getOperationBatchDetail.mock.calls.length
    const targetCallsAfterPoll = api.fetchOperationTargetPage.mock.calls.length
    await wrapper.setProps({ value: false })
    await vi.advanceTimersByTimeAsync(5000)

    expect(api.getOperationBatchDetail).toHaveBeenCalledTimes(detailCallsAfterPoll)
    expect(api.fetchOperationTargetPage).toHaveBeenCalledTimes(targetCallsAfterPoll)
    wrapper.destroy()
  })

  it.each([
    ['WAITING_CONFIRM', '待设备确认'],
    ['CONFIRMED', '设备已确认，待本地收敛']
  ])('%s 不显示为成功或完成', async (status, label) => {
    api.getOperationBatchDetail.mockImplementation(batchId => detailResponse(batchId, status, {
      expectedCount: 1,
      totalTargetCount: 1,
      confirmedCount: status === 'CONFIRMED' ? 1 : 0,
      waitingConfirmCount: status === 'WAITING_CONFIRM' ? 1 : 0,
      convergedCount: 0,
      unfinishedCount: 1
    }))

    const wrapper = mountProgress()
    await flushPromises()
    await wrapper.vm.selectBatch({ batchId: `batch-${status}`, status })
    await flushPromises()

    const statusNode = wrapper.find('.auth-operation-progress__current-status')
    expect(statusNode.text()).toContain(label)
    expect(statusNode.classes()).not.toContain('is-success')
    expect(wrapper.find('.auth-operation-progress__percent').text()).toBe('0%')
    wrapper.destroy()
  })

  it('零目标待核验批次显示 0% 且保留未完成状态', async () => {
    api.getOperationBatchDetail.mockImplementation(async batchId => {
      const response = await detailResponse(batchId, 'VERIFYING', {
        expectedCount: 0,
        expandedCount: 0,
        totalTargetCount: 0,
        verifyingCount: 0,
        convergedCount: 0,
        unfinishedCount: 0
      })
      response.data.data.failureReason = '缺少设备依据'
      return response
    })

    const wrapper = mountProgress()
    await flushPromises()
    await wrapper.vm.selectBatch({ batchId: 'batch-zero', status: 'VERIFYING' })
    await flushPromises()

    expect(wrapper.find('.auth-operation-progress__percent').text()).toBe('0%')
    expect(wrapper.find('.auth-operation-progress__current-status').classes()).toContain('is-warning')
    expect(wrapper.text()).toContain('待核验')
    expect(wrapper.find('.auth-operation-progress__batch-reason').text()).toContain('缺少设备依据')
    wrapper.destroy()
  })

  it('批次列表刷新失败时保留旧页并显示局部重试', async () => {
    api.fetchOperationBatchPage.mockImplementationOnce(() => pageResponse([
      { batchId: 'batch-existing', parkId: '26', action: 'DELETE', status: 'FAILED' }
    ], 1))
    const wrapper = mountProgress()
    await flushPromises()

    api.fetchOperationBatchPage.mockRejectedValueOnce(new Error('查询暂时不可用'))
    await wrapper.vm.loadBatchPage()
    await flushPromises()

    expect(wrapper.vm.batches.map(item => item.batchId)).toEqual(['batch-existing'])
    expect(wrapper.find('[role="alert"]').text()).toContain('查询暂时不可用')
    expect(wrapper.find('.auth-operation-progress__batch-retry').exists()).toBe(true)
    wrapper.destroy()
  })
})
