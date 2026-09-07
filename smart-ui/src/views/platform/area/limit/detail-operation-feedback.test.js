import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createLocalVue, shallowMount } from '@vue/test-utils'
import VueRouter from 'vue-router'

const api = vi.hoisted(() => ({
  getDetailPage: vi.fn(),
  delObj: vi.fn(),
  batchDelPersonWithReceipt: vi.fn(),
  clearPersonWithReceipt: vi.fn(),
  personIntakeCapability: vi.fn(),
  batchDel: vi.fn(),
  clearAll: vi.fn(),
  fetchOperationBatchPage: vi.fn(),
  getOperationBatchDetail: vi.fn(),
  fetchOperationTargetPage: vi.fn()
}))

const intakeStorage = vi.hoisted(() => ({ current: null }))
vi.mock('./employee-intake-request', async importOriginal => {
  const actual = await importOriginal()
  const slots = { transact: (user, work) => {
    const state = intakeStorage.current
    const result = state.tail.then(() => {
      const next = work(state.rows.has(user) ? JSON.parse(JSON.stringify(state.rows.get(user))) : undefined)
      state.rows.set(user, JSON.parse(JSON.stringify(next.record)))
      return next.value
    })
    state.tail = result.catch(() => undefined)
    return result
  } }
  return { ...actual,
    submitEmployeeIntake: options => actual.submitEmployeeIntake({ ...options, slots }),
    pendingEmployeeIntake: actorId => actual.pendingEmployeeIntake(actorId, window.localStorage, slots)
  }
})

vi.mock('@/api/platform/area/limit', () => api)
vi.mock('@/filters/index', () => ({ staffStatusInit: value => value }))
vi.mock('./doPaste', () => ({ default: { name: 'DoPasteDialog', render: createElement => createElement('div') } }))
vi.mock('./doPasteBadge', () => ({ default: { name: 'DoPasteDialogs', render: createElement => createElement('div') } }))

const PersonDetail = (await import('./personDetail.vue')).default
const VehicleDetail = (await import('./vechileDetail.vue')).default

const stubs = {
  ElScrollbar: { template: '<div><slot /></div>' },
  ElButton: {
    props: ['disabled', 'loading', 'type', 'plain'],
    template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
  },
  ElForm: { template: '<form><slot /></form>', methods: { resetFields: vi.fn() } },
  ElFormItem: { template: '<label><slot /></label>' },
  ElInput: { props: ['value', 'operationKey'], template: '<input :value="value" />' },
  ElTag: { template: '<span><slot /></span>' },
  AvueCrud: { template: '<div></div>' },
  DoPasteDialog: { template: '<div></div>' },
  DoPasteDialogs: { template: '<div></div>' },
  AuthOperationProgress: {
    name: 'AuthOperationProgress',
    props: ['value', 'operationKey'],
    template: '<div class="auth-operation-progress-stub">{{ value }}</div>'
  }
}

function flushPromises () {
  return Promise.resolve().then(() => Promise.resolve()).then(() => Promise.resolve())
}

function mountPage (component) {
  const notify = vi.fn()
  notify.error = vi.fn()
  const msgbox = vi.fn(() => Promise.resolve())
  const wrapper = shallowMount(component, {
    stubs,
    mocks: {
      $route: {
        params: { id: 'authority-001', type: '1' },
        query: { name: '测试权限组', backPageTag: 'limit' }
      },
      $router: { push: vi.fn() },
      $store: { getters: { permissions: [], userInfo: { id: 7 } } },
      $notify: notify,
      $msgbox: msgbox
    }
  })
  return { wrapper, notify, msgbox }
}

async function mountRoutedPage (component, options = {}) {
  const localVue = createLocalVue()
  localVue.use(VueRouter)
  const router = new VueRouter({
    mode: 'abstract',
    routes: [{ path: '/limit/:id/:type', component }]
  })
  await router.push({
    path: '/limit/authority-001/1',
    query: { name: '旧权限组', backPageTag: 'limit' }
  })
  const notify = vi.fn()
  notify.error = vi.fn()
  const msgbox = options.msgbox || vi.fn(() => Promise.resolve())
  const wrapper = shallowMount(component, {
    localVue,
    router,
    stubs,
    mocks: {
      $store: { getters: { permissions: [], userInfo: { id: 7 } } },
      $notify: notify,
      $msgbox: msgbox
    }
  })
  await flushPromises()
  return { wrapper, notify, msgbox, router }
}

function pageResponse (records = [], total = records.length) {
  return { data: { data: { records, total } } }
}

describe.each([
  ['人员权限明细', PersonDetail],
  ['车辆权限明细', VehicleDetail]
])('%s', (pageName, component) => {
  beforeEach(() => {
    Object.values(api).forEach(mock => mock.mockReset())
    intakeStorage.current = { rows: new Map(), tail: Promise.resolve() }
    const saved = new Map()
    Object.defineProperty(window, 'localStorage', { configurable: true, value: { getItem: key => saved.get(key) || null, setItem: (key, value) => saved.set(key, value), removeItem: key => saved.delete(key) } })
    api.personIntakeCapability.mockResolvedValue({ data: { data: { intakeVersion: 1, reliableIntakeEnabled: false } } })
    api.getDetailPage.mockResolvedValue({ data: { data: { records: [], total: 0 } } })
    api.batchDel.mockResolvedValue({ data: { data: true } })
    api.clearAll.mockResolvedValue({ data: { data: true } })
    api.batchDelPersonWithReceipt.mockImplementation((...args) => api.batchDel(...args))
    api.clearPersonWithReceipt.mockImplementation((...args) => api.clearAll(...args))
  })

  if (component === PersonDetail) it('人员网络响应丢失后显式重试沿原键并绕过能力查询', async () => {
    const { wrapper } = mountPage(component)
    await wrapper.setData({ deleteForm: { authId: '9', type: '1', delIds: ['5'] } })
    api.personIntakeCapability.mockResolvedValue({ data: { data: { intakeVersion: 1, reliableIntakeEnabled: true } } })
    api.batchDelPersonWithReceipt.mockRejectedValueOnce(new Error('响应丢失'))
    await wrapper.vm.handleDelBatch()
    const key = api.batchDelPersonWithReceipt.mock.calls[0][1]
    expect(key).toMatch(/^[a-f0-9]{32}$/)
    api.personIntakeCapability.mockRejectedValue(new Error('组已删除'))
    api.batchDelPersonWithReceipt.mockImplementation((data, savedKey) => Promise.resolve({ data: { data: { requestKey: savedKey, mode: 'RELIABLE', submitted: true, operationKey: 'original-operation' } } }))
    await wrapper.vm.retryPendingIntake()
    expect(api.personIntakeCapability).toHaveBeenCalledTimes(1)
    expect(api.batchDelPersonWithReceipt.mock.calls[1][1]).toBe(key)
    expect(wrapper.vm.acceptedOperationKey).toBe('original-operation')
    wrapper.destroy()
  })

  if (component === PersonDetail) it('提供“权限任务”入口并打开同一个可复用进度组件', async () => {
    const { wrapper } = mountPage(component)
    await flushPromises()
    const button = wrapper.findAll('button').wrappers.find(item => item.text() === '权限任务')

    expect(button).toBeTruthy()
    await button.trigger('click')

    expect(wrapper.vm.operationProgressVisible).toBe(true)
    expect(wrapper.findComponent({ name: 'AuthOperationProgress' }).props('value')).toBe(true)
    wrapper.destroy()
  })

  if (component === VehicleDetail) it('旧车辆删除接口没有回执时不展示无法定位的权限任务面板', async () => {
    const { wrapper } = mountPage(component)
    await flushPromises()

    expect(wrapper.findAll('button').wrappers.find(item => item.text() === '权限任务')).toBeUndefined()
    expect(wrapper.findComponent({ name: 'AuthOperationProgress' }).exists()).toBe(false)
    expect(wrapper.vm.operationProgressVisible).toBeUndefined()
    wrapper.destroy()
  })

  it('批量删除成功只反馈请求已受理和设备结果待确认', async () => {
    const { wrapper, notify } = mountPage(component)
    await wrapper.setData({ deleteForm: { authId: 'authority-001', type: '1', delIds: ['relation-001'] } })

    await wrapper.vm.handleDelBatch()
    await flushPromises()

    expect(notify).toHaveBeenCalledWith(expect.objectContaining({
      title: '删除请求已提交',
      message: expect.stringContaining('设备结果仍待确认'),
      type: 'info'
    }))
    expect(JSON.stringify(notify.mock.calls)).not.toContain('删除成功')
    wrapper.destroy()
  })

  it('清空成功只反馈请求已受理，不伪造可定位批次', async () => {
    const { wrapper, notify } = mountPage(component)

    await wrapper.vm.handleClear()
    await flushPromises()

    expect(notify).toHaveBeenCalledWith(expect.objectContaining({
      title: '删除请求已提交',
      message: expect.stringContaining('旧链路暂不能自动定位批次'),
      type: 'info'
    }))
    expect(JSON.stringify(notify.mock.calls)).not.toContain('batchId')
    wrapper.destroy()
  })

  it('删除请求在途时阻止重复提交', async () => {
    let resolveDelete
    api.batchDel.mockReturnValue(new Promise(resolve => { resolveDelete = resolve }))
    const { wrapper } = mountPage(component)
    await wrapper.setData({ deleteForm: { authId: 'authority-001', type: '1', delIds: ['relation-001'] } })

    const first = wrapper.vm.handleDelBatch()
    const second = wrapper.vm.handleDelBatch()
    await flushPromises()
    expect(api.batchDel).toHaveBeenCalledTimes(1)

    resolveDelete({ data: { data: true } })
    await Promise.all([first, second])
    wrapper.destroy()
  })

  it('复用组件切换路由后查询、批量删除和清空都使用新权限组', async () => {
    const { wrapper, router } = await mountRoutedPage(component)
    api.getDetailPage.mockClear()
    await wrapper.setData({ deleteForm: { authId: 'authority-001', type: '1', delIds: ['old-relation'] } })

    await router.push({
      path: '/limit/authority-002/2',
      query: { name: '新权限组', backPageTag: 'limit' }
    })
    await flushPromises()

    expect(wrapper.vm.page).toEqual(expect.objectContaining({ authId: 'authority-002', type: '2', currentPage: 1 }))
    expect(wrapper.vm.deleteForm).toEqual(expect.objectContaining({ authId: 'authority-002', type: '2', delIds: [] }))
    if (pageName === '人员权限明细') expect(wrapper.vm.authorityName).toBe('新权限组')
    expect(api.getDetailPage).toHaveBeenCalledWith(expect.objectContaining({
      authId: 'authority-002',
      type: '2'
    }))

    await wrapper.setData({ deleteForm: { authId: 'authority-002', type: '2', delIds: ['new-relation'] } })
    await wrapper.vm.handleDelBatch()
    expect(api.batchDel).toHaveBeenCalledWith({
      authId: 'authority-002',
      type: pageName === '人员权限明细' ? '1' : '2',
      delIds: ['new-relation']
    })

    await wrapper.vm.handleClear()
    expect(api.clearAll).toHaveBeenCalledWith('authority-002')
    wrapper.destroy()
  })

  it('旧路由列表响应迟到时不能覆盖新权限组列表', async () => {
    let resolveOldRequest
    let oldRequestCount = 0
    api.getDetailPage.mockImplementation(params => {
      if (params.authId === 'authority-001' && oldRequestCount++ === 0) {
        return new Promise(resolve => { resolveOldRequest = resolve })
      }
      if (params.authId === 'authority-002') return Promise.resolve(pageResponse([{ id: 'new-row' }], 1))
      return Promise.resolve(pageResponse([{ id: 'wrong-route-row' }], 1))
    })
    const { wrapper, router } = await mountRoutedPage(component)

    await router.push('/limit/authority-002/2')
    await flushPromises()
    expect(wrapper.vm.tableData).toEqual([{ id: 'new-row' }])

    resolveOldRequest(pageResponse([{ id: 'old-row' }], 1))
    await flushPromises()
    expect(wrapper.vm.tableData).toEqual([{ id: 'new-row' }])
    wrapper.destroy()
  })

  it.each([
    ['批量删除', 'handleDelBatch', 'batchDel'],
    ['清空', 'handleClear', 'clearAll']
  ])('%s 确认期间切换路由会取消旧上下文提交', async (label, method, apiMethod) => {
    let confirmDialog
    const msgbox = vi.fn(() => new Promise(resolve => { confirmDialog = resolve }))
    const { wrapper, notify, router } = await mountRoutedPage(component, { msgbox })
    await wrapper.setData({ deleteForm: { authId: 'authority-001', type: '1', delIds: ['old-relation'] } })

    const operation = wrapper.vm[method]()
    await flushPromises()
    await router.push('/limit/authority-002/2')
    await flushPromises()
    confirmDialog()
    await operation

    expect(api[apiMethod]).not.toHaveBeenCalled()
    expect(notify).toHaveBeenCalledWith(expect.objectContaining({
      title: '权限组已切换',
      type: 'warning'
    }))
    wrapper.destroy()
  })

  it('删除已受理后列表刷新失败会保持锁并给出独立提示', async () => {
    let rejectRefresh
    const { wrapper, notify } = await mountRoutedPage(component)
    api.getDetailPage.mockImplementationOnce(() => new Promise((resolve, reject) => { rejectRefresh = reject }))
    await wrapper.setData({ deleteForm: { authId: 'authority-001', type: '1', delIds: ['relation-001'] } })

    const operation = wrapper.vm.handleDelBatch()
    await flushPromises()
    expect(wrapper.vm.batchDeleting).toBe(true)
    const duplicate = wrapper.vm.handleDelBatch()
    expect(api.batchDel).toHaveBeenCalledTimes(1)

    await vi.waitFor(() => expect(rejectRefresh).toBeTypeOf('function'))
    rejectRefresh(new Error('刷新超时'))
    await Promise.all([operation, duplicate])
    await flushPromises()

    expect(notify).toHaveBeenCalledWith(expect.objectContaining({
      title: '请求已受理，列表刷新失败',
      message: expect.stringContaining('请手动刷新列表后再操作'),
      type: 'warning'
    }))
    expect(notify.error).not.toHaveBeenCalled()
    expect(wrapper.vm.deleteForm.delIds).toEqual([])
    expect(wrapper.vm.batchDeleting).toBe(false)
    wrapper.destroy()
  })

  it.each([
    ['批量删除', 'success', 'handleDelBatch', 'batchDel'],
    ['批量删除', 'false', 'handleDelBatch', 'batchDel'],
    ['批量删除', 'error', 'handleDelBatch', 'batchDel'],
    ['清空', 'success', 'handleClear', 'clearAll'],
    ['清空', 'false', 'handleClear', 'clearAll'],
    ['清空', 'error', 'handleClear', 'clearAll']
  ])('%s 旧请求迟到的 %s 结果归属于原权限组且不修改新选择', async (label, outcome, method, apiMethod) => {
    let resolveWrite
    let rejectWrite
    api[apiMethod].mockImplementationOnce(() => new Promise((resolve, reject) => {
      resolveWrite = resolve
      rejectWrite = reject
    }))
    const { wrapper, notify, router } = await mountRoutedPage(component)
    await wrapper.setData({ deleteForm: { authId: 'authority-001', type: '1', delIds: ['old-relation'] } })

    const operation = wrapper.vm[method]()
    await flushPromises()
    expect(api[apiMethod]).toHaveBeenCalledTimes(1)
    if (apiMethod === 'batchDel') {
      expect(api.batchDel).toHaveBeenCalledWith({
        authId: 'authority-001',
        type: '1',
        delIds: ['old-relation']
      })
    } else {
      expect(api.clearAll).toHaveBeenCalledWith('authority-001')
    }

    await router.push({ path: '/limit/authority-002/2', query: { name: '新权限组' } })
    await flushPromises()
    await wrapper.setData({ deleteForm: { authId: 'authority-002', type: '2', delIds: ['new-relation'] } })
    const listCallsBeforeResponse = api.getDetailPage.mock.calls.length

    if (outcome === 'success') resolveWrite({ data: { data: true } })
    else if (outcome === 'false') resolveWrite({ data: { data: false } })
    else rejectWrite(new Error('旧请求网络异常'))
    await operation
    await flushPromises()

    expect(wrapper.vm.deleteForm.delIds).toEqual(['new-relation'])
    expect(api.getDetailPage).toHaveBeenCalledTimes(listCallsBeforeResponse)
    const resultCalls = outcome === 'success' ? notify.mock.calls : notify.error.mock.calls
    expect(JSON.stringify(resultCalls)).toContain('旧权限组')
    expect(JSON.stringify(resultCalls)).toContain('authority-001')
    expect(JSON.stringify(resultCalls)).not.toContain('新权限组')
    wrapper.destroy()
  })
})


describe('人员操作回执定位', () => {
  beforeEach(() => {
    Object.values(api).forEach(mock => mock.mockReset())
    intakeStorage.current = { rows: new Map(), tail: Promise.resolve() }
    const saved = new Map()
    Object.defineProperty(window, 'localStorage', { configurable: true, value: { getItem: key => saved.get(key) || null, setItem: (key, value) => saved.set(key, value), removeItem: key => saved.delete(key) } })
    api.personIntakeCapability.mockResolvedValue({ data: { data: { intakeVersion: 1, reliableIntakeEnabled: false } } })
    api.getDetailPage.mockResolvedValue({ data: { data: { records: [], total: 0 } } })
    api.batchDelPersonWithReceipt.mockResolvedValue({ data: { data: { mode: 'RELIABLE', submitted: true, operationKey: 'exact-operation' } } })
    api.clearPersonWithReceipt.mockResolvedValue({ data: { data: { mode: 'NO_CHANGE', submitted: false, operationKey: null } } })
  })
  it('可靠受理打开面板且保留真实操作键', async () => {
    const { wrapper, notify } = mountPage(PersonDetail)
    await wrapper.setData({ deleteForm: { authId: 'authority-001', type: '2', delIds: ['relation-001'] } })
    await wrapper.vm.handleDelBatch()
    expect(wrapper.findComponent({ name: 'AuthOperationProgress' }).props()).toEqual(expect.objectContaining({ value: true, operationKey: 'exact-operation' }))
    expect(JSON.stringify(notify.mock.calls)).toContain('设备结果仍待确认')
    expect(api.batchDel).not.toHaveBeenCalled()
    wrapper.destroy()
  })
  it.each([
    [{ mode: 'NO_CHANGE', submitted: false, operationKey: null }, '没有产生新批次'],
    [{ mode: 'LEGACY', submitted: true, operationKey: null }, '旧链路'],
    [true, '旧链路']
  ])('非可靠回执不自动定位', async (receipt, text) => {
    api.clearPersonWithReceipt.mockResolvedValue({ data: { data: receipt } })
    const { wrapper, notify } = mountPage(PersonDetail)
    await wrapper.vm.handleClear()
    expect(wrapper.vm.operationProgressVisible).toBe(false)
    expect(JSON.stringify(notify.mock.calls)).toContain(text)
    wrapper.destroy()
  })
  it.each([
    { mode: 'RELIABLE', submitted: true, operationKey: null },
    { mode: 'RELIABLE', submitted: true, operationKey: 'NO_CHANGE' },
    { mode: 'UNKNOWN', submitted: true, operationKey: 'wrong' },
    { mode: 'LEGACY', submitted: false, operationKey: null }
  ])('异常回执不能误报已受理', async receipt => {
    api.clearPersonWithReceipt.mockResolvedValue({ data: { data: receipt } })
    const { wrapper, notify } = mountPage(PersonDetail)
    await wrapper.vm.handleClear()
    expect(wrapper.vm.operationProgressVisible).toBe(false)
    expect(notify.error).toHaveBeenCalled()
    expect(notify).not.toHaveBeenCalledWith(expect.objectContaining({ title: '删除请求已提交' }))
    wrapper.destroy()
  })
  it('空操作后列表加载失败不能误报请求已受理', async () => {
    const { wrapper, notify } = mountPage(PersonDetail)
    await flushPromises()
    api.getDetailPage.mockRejectedValue(new Error('list failed'))
    await wrapper.vm.handleClear()
    expect(JSON.stringify(notify.mock.calls)).not.toContain('请求已受理')
    expect(JSON.stringify(notify.mock.calls)).toContain('列表刷新失败')
    wrapper.destroy()
  })
  it('旧流程或无变化回执不能继续显示上次操作作为本次结果' , async () => {
    const { wrapper } = mountPage(PersonDetail)
    await wrapper.setData({ acceptedOperationKey: 'previous-operation', operationProgressVisible: true })
    await wrapper.vm.handleClear()
    expect(wrapper.vm.operationProgressVisible).toBe(false)
    expect(wrapper.findComponent({ name: 'AuthOperationProgress' }).props('operationKey')).toBe('')
    wrapper.destroy()
  })
  it('网络中断提示结果未确认且不回退旧写接口' , async () => {
    api.clearPersonWithReceipt.mockRejectedValue(new Error('Network Error'))
    const { wrapper, notify } = mountPage(PersonDetail)
    await wrapper.vm.handleClear()
    expect(JSON.stringify(notify.error.mock.calls)).toContain('提交结果未确认')
    expect(api.clearPersonWithReceipt).toHaveBeenCalledTimes(1)
    expect(api.clearAll).not.toHaveBeenCalled()
    wrapper.destroy()
  })
  it('删除与清空互斥，即使直接调用方法也不能并发写入', async () => {
    const { wrapper } = mountPage(PersonDetail)
    await wrapper.setData({ batchDeleting: true })
    await wrapper.vm.handleClear()
    expect(api.clearPersonWithReceipt).not.toHaveBeenCalled()
    wrapper.destroy()
  })
  it('旧组可靠回执迟到不能打开新组面板', async () => {
    let resolveReceipt
    api.clearPersonWithReceipt.mockReturnValue(new Promise(resolve => { resolveReceipt = resolve }))
    const { wrapper, router } = await mountRoutedPage(PersonDetail)
    const pending = wrapper.vm.handleClear()
    await flushPromises()
    await router.push('/limit/authority-002/2')
    resolveReceipt({ data: { data: { mode: 'RELIABLE', submitted: true, operationKey: 'old-operation' } } })
    await pending
    expect(wrapper.vm.operationProgressVisible).toBe(false)
    expect(wrapper.findComponent({ name: 'AuthOperationProgress' }).props('operationKey')).not.toBe('old-operation')
    wrapper.destroy()
  })
})
