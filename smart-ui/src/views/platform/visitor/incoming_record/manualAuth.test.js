import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  request: vi.fn(),
  message: vi.fn()
}))

vi.mock('@/router/axios', () => ({ default: mocks.request }))

const component = (await import('./manualAuth.vue')).default

const stubs = {
  ElDialog: {
    props: ['title', 'visible', 'width', 'closeOnClickModal'],
    template: '<section class="manual-auth-dialog-stub"><slot /><slot name="footer" /></section>'
  },
  ElInput: {
    props: ['value', 'readonly'],
    template: '<input :value="value" :readonly="readonly" />'
  },
  ElSelect: {
    props: ['value', 'disabled'],
    template: '<select :disabled="disabled"><slot /></select>'
  },
  ElOption: {
    props: ['label', 'value'],
    template: '<option :value="value">{{ label }}</option>'
  },
  ElCheckboxGroup: {
    props: ['value'],
    template: '<div><slot /></div>'
  },
  ElCheckbox: {
    props: ['label', 'disabled'],
    template: '<label><input type="checkbox" :disabled="disabled" /><slot /></label>'
  },
  ElButton: {
    props: ['disabled', 'loading'],
    template: '<button :disabled="disabled"><slot /></button>'
  }
}

function flushPromises () {
  return Promise.resolve().then(() => Promise.resolve()).then(() => Promise.resolve())
}

function mountManualAuth (propsData = { visible: false, record: null }) {
  return shallowMount(component, {
    propsData,
    stubs,
    mocks: {
      $message: mocks.message
    }
  })
}

const optionsResponse = {
  code: 0,
  data: {
    applyId: '101',
    startTime: '2026-09-05 08:00:00',
    endTime: '2026-09-06 18:00:00',
    fellows: [{ id: '201', name: '测试访客' }],
    vehicles: [{ id: '301', plate: '测试车牌' }],
    authorities: [
      { id: 401, authorityName: '园区公共权限', type: 1, areaType: 0 },
      { id: 402, authorityName: '园区涉密权限', type: 1, areaType: 1 },
      { id: 403, authorityName: '访客类型权限', type: 2, areaType: 0 },
      { id: 404, authorityName: '车辆公共权限', type: 3, areaType: 0 }
    ]
  }
}

describe('访客手动授权弹窗', () => {
  beforeEach(() => {
    mocks.request.mockReset()
    mocks.message.mockReset()
  })

  it('打开时查询申请选项，默认首个人员并展示只读有效期和车辆能力限制', async () => {
    mocks.request.mockResolvedValue({ data: optionsResponse })

    const wrapper = mountManualAuth({ visible: true, record: { id: '101' } })
    await flushPromises()

    expect(mocks.request).toHaveBeenCalledWith({
      url: '/platform/manage/admittance/apply/device/auth/options',
      method: 'get',
      params: { applyId: '101' }
    })
    expect(wrapper.vm.selectedTargetId).toBe('201')
    expect(wrapper.vm.filteredAuthList.map(item => item.id)).toEqual([401, 402])
    const readonlyInputs = wrapper.findAll('input[readonly]')
    expect(readonlyInputs.length).toBe(2)
    expect(readonlyInputs.at(0).element.value).toBe('2026-09-05 08:00:00')
    expect(readonlyInputs.at(1).element.value).toBe('2026-09-06 18:00:00')
    expect(wrapper.text()).toContain('仅支持人员ISC权限，车辆暂不支持')
    expect(wrapper.text()).toContain('保密考试校验暂未开通')
  })

  it('切换人员时清空上一人员的权限选择', () => {
    const wrapper = mountManualAuth()
    wrapper.setData({
      selectedAuth: [401],
      tempSelectedAuth: [402],
      tempRemovedAuth: [401]
    })

    wrapper.vm.handleTargetChange()

    expect(wrapper.vm.selectedAuth).toEqual([])
    expect(wrapper.vm.tempSelectedAuth).toEqual([])
    expect(wrapper.vm.tempRemovedAuth).toEqual([])
  })

  it('成功提交时只发送人员载荷，提示任务已提交并通知列表刷新', async () => {
    mocks.request.mockResolvedValue({ data: { code: 0, data: 'batch-1' } })
    const wrapper = mountManualAuth()
    await wrapper.setData({
      dialogVisible: true,
      options: optionsResponse.data,
      selectedTargetId: '201',
      selectedAuth: [401]
    })

    await wrapper.vm.submitAuth()

    expect(mocks.request).toHaveBeenCalledWith({
      url: '/platform/manage/admittance/apply/device/auth',
      method: 'post',
      data: { applyId: '101', fellowId: '201', authIds: [401] }
    })
    expect(mocks.message).toHaveBeenCalledWith({ message: '下发任务已提交', type: 'success' })
    expect(wrapper.emitted('submitted')).toEqual([['batch-1']])
    expect(wrapper.emitted('update:visible')).toEqual([[false]])
  })

  it('提交被后端拒绝时展示 msg、保留选择，并阻止并发请求', async () => {
    let resolveRequest
    mocks.request.mockReturnValue(new Promise(resolve => { resolveRequest = resolve }))
    const wrapper = mountManualAuth()
    await wrapper.setData({
      dialogVisible: true,
      options: optionsResponse.data,
      selectedTargetId: '201',
      selectedAuth: [401]
    })

    const firstSubmit = wrapper.vm.submitAuth()
    const secondSubmit = wrapper.vm.submitAuth()
    expect(mocks.request).toHaveBeenCalledTimes(1)
    expect(wrapper.vm.submitLoading).toBe(true)

    resolveRequest({ data: { code: 403, msg: '申请单已失效' } })
    await firstSubmit

    expect(mocks.message).toHaveBeenCalledWith({ message: '申请单已失效', type: 'error' })
    expect(wrapper.vm.selectedAuth).toEqual([401])
    expect(wrapper.vm.submitLoading).toBe(false)
    await secondSubmit
  })

  it('关闭后重新打开时忽略旧查询响应', async () => {
    let resolveFirst
    let resolveSecond
    mocks.request
      .mockReturnValueOnce(new Promise(resolve => { resolveFirst = resolve }))
      .mockReturnValueOnce(new Promise(resolve => { resolveSecond = resolve }))

    const wrapper = mountManualAuth({ visible: true, record: { id: '101' } })
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true })
    expect(mocks.request).toHaveBeenCalledTimes(2)

    resolveFirst({ data: { ...optionsResponse, data: { ...optionsResponse.data, applyId: 'old' } } })
    await flushPromises()
    expect(wrapper.vm.options.applyId).toBe('')

    resolveSecond({ data: { ...optionsResponse, data: { ...optionsResponse.data, applyId: 'new' } } })
    await flushPromises()
    expect(wrapper.vm.options.applyId).toBe('new')
  })

  it('打开期间切换申请单后查询失败时清空旧选项和人员选择', async () => {
    mocks.request
      .mockResolvedValueOnce({ data: optionsResponse })
      .mockResolvedValueOnce({ data: { code: 409, msg: '新申请单不可用' } })

    const wrapper = mountManualAuth({ visible: true, record: { id: '101' } })
    await flushPromises()
    expect(wrapper.vm.options.applyId).toBe('101')
    expect(wrapper.vm.selectedTargetId).toBe('201')

    await wrapper.setProps({ record: { id: '202' } })
    expect(wrapper.vm.options.applyId).toBe('')
    expect(wrapper.vm.selectedTargetId).toBeNull()
    await flushPromises()

    expect(wrapper.vm.options.applyId).toBe('')
    expect(wrapper.vm.fellows).toEqual([])
    expect(wrapper.vm.selectedTargetId).toBeNull()
    expect(mocks.message).toHaveBeenCalledWith({ message: '新申请单不可用', type: 'error' })
  })

  it('提交期间禁用取消，并忽略被关闭弹窗的旧提交响应', async () => {
    let resolveSubmit
    mocks.request.mockReturnValue(new Promise(resolve => { resolveSubmit = resolve }))
    const wrapper = mountManualAuth()
    await wrapper.setData({
      dialogVisible: true,
      options: optionsResponse.data,
      selectedTargetId: '201',
      selectedAuth: [401]
    })

    const submitPromise = wrapper.vm.submitAuth()
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.dialog-footer').findAll('button').at(0).attributes('disabled')).toBe('disabled')
    wrapper.vm.closeDialog()
    expect(wrapper.emitted('update:visible')).toBeFalsy()

    wrapper.vm.dialogVisible = false
    wrapper.vm.invalidateRequest()
    wrapper.vm.resetState()
    wrapper.vm.dialogVisible = true
    resolveSubmit({ data: { code: 0, data: 'old-batch' } })
    await submitPromise

    expect(wrapper.emitted('submitted')).toBeFalsy()
    expect(wrapper.vm.dialogVisible).toBe(true)
  })
})
