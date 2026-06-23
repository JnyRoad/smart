import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi, beforeEach } from 'vitest'

// mock 床位管理 API：只关心 editCheckInDormitory 的入参与返回分支
const editCheckInDormitory = vi.fn(() => Promise.resolve({ data: { data: true } }))
vi.mock('@/api/platform/dormitory/bed_mng', () => ({
  editCheckInDormitory: (...args) => editCheckInDormitory(...args)
}))

// ElForm stub：暴露 validate(cb) 与 resetFields()，由各用例控制校验结果
function formStub(valid, resetFields) {
  return {
    methods: {
      validate(cb) {
        cb(valid)
      },
      resetFields() {
        resetFields()
      }
    },
    template: '<form><slot></slot></form>'
  }
}

const stubs = (valid, resetFields) => ({
  ElDialog: {
    props: ['title', 'visible'],
    template: '<div class="dialog-stub"><slot></slot><slot name="footer"></slot></div>'
  },
  ElForm: formStub(valid, resetFields),
  ElFormItem: { template: '<div><slot></slot></div>' },
  ElInput: {
    props: ['value'],
    template: '<input class="input-stub" :value="value" @input="$emit(\'input\', $event.target.value)" />'
  },
  ElRadio: { template: '<label><slot></slot></label>' },
  ElButton: {
    props: ['loading'],
    template: '<button class="button-stub" @click="$emit(\'click\')"><slot></slot></button>'
  }
})

const component = (await import('./dlg_edit_check_in.vue')).default

function mountDialog(propsData = {}, { valid = true } = {}) {
  const notify = vi.fn()
  const resetFields = vi.fn()
  const wrapper = shallowMount(component, {
    propsData: {
      visible: false,
      row: { id: 'c1', bedId: 'b1', staffBadge: 'A9', staffName: '王五', jobName: '组长', staffSex: 1 },
      ...propsData
    },
    stubs: stubs(valid, resetFields),
    mocks: { $notify: notify }
  })
  return { wrapper, notify, resetFields }
}

describe('bed_mng 编辑非员工入住弹窗', () => {
  beforeEach(() => {
    editCheckInDormitory.mockClear()
    editCheckInDormitory.mockResolvedValue({ data: { data: true } })
  })

  it('visible 变 true 时用父级（已映射好的）row 初始化表单', async () => {
    const { wrapper } = mountDialog()
    await wrapper.setProps({ visible: true })
    expect(wrapper.vm.form).toEqual({
      id: 'c1', bedId: 'b1', staffBadge: 'A9', staffName: '王五', jobName: '组长', staffSex: 1
    })
    expect(wrapper.vm.setFormVisible).toBe(true)
  })

  it('取消：resetFields 后关闭并 emit dlgdo(false)（沿用原 resetEditCheckInForm）', async () => {
    const { wrapper, resetFields } = mountDialog({ visible: true })
    await wrapper.setProps({ visible: true })
    await wrapper.findAll('.button-stub').at(0).trigger('click')
    await wrapper.vm.$nextTick()
    expect(resetFields).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('dlgdo')[0]).toStrictEqual([false])
  })

  it('校验通过且返回 data 为真：调接口、提示成功、关闭并 emit refresh', async () => {
    const { wrapper, notify } = mountDialog({ visible: true }, { valid: true })
    await wrapper.setProps({ visible: true })
    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()

    expect(editCheckInDormitory).toHaveBeenCalledWith(wrapper.vm.form)
    expect(notify).toHaveBeenCalledWith(expect.objectContaining({ type: 'success', message: '入住成功' }))
    expect(wrapper.emitted('refresh')).toHaveLength(1)
    expect(wrapper.vm.setFormVisible).toBe(false)
    expect(wrapper.vm.loading).toBe(false)
  })

  it('校验通过但返回 data 为假：提示失败、不关闭、不 emit refresh', async () => {
    editCheckInDormitory.mockResolvedValueOnce({ data: { data: null, message: '床位已占用' } })
    const { wrapper, notify } = mountDialog({ visible: true }, { valid: true })
    await wrapper.setProps({ visible: true })
    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()

    expect(notify).toHaveBeenCalledWith(expect.objectContaining({ type: 'error', message: '床位已占用' }))
    expect(wrapper.emitted('refresh')).toBeUndefined()
    expect(wrapper.vm.setFormVisible).toBe(true)
    expect(wrapper.vm.loading).toBe(false)
  })

  it('校验不通过：不调接口、不 emit refresh', async () => {
    const { wrapper } = mountDialog({ visible: true }, { valid: false })
    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()
    expect(editCheckInDormitory).not.toHaveBeenCalled()
    expect(wrapper.emitted('refresh')).toBeUndefined()
  })

  it('接口失败：复位 loading、不 emit refresh', async () => {
    editCheckInDormitory.mockRejectedValueOnce(new Error('boom'))
    const { wrapper } = mountDialog({ visible: true }, { valid: true })
    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.loading).toBe(false)
    expect(wrapper.emitted('refresh')).toBeUndefined()
  })
})
