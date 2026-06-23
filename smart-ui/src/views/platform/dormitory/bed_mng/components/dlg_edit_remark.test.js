import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi, beforeEach } from 'vitest'

// mock 床位管理 API：只关心 updateSimpleRemark 被如何调用，默认成功
const updateSimpleRemark = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/api/platform/dormitory/bed_mng', () => ({
  updateSimpleRemark: (...args) => updateSimpleRemark(...args)
}))

// ElForm stub 暴露 validate(cb)，由各用例控制校验通过与否
function formStub(valid) {
  return {
    methods: {
      validate(cb) {
        cb(valid)
      }
    },
    template: '<form><slot></slot></form>'
  }
}

const stubs = (valid = true) => ({
  ElDialog: {
    props: ['title', 'visible', 'closeOnClickModal'],
    template: '<div class="dialog-stub"><slot></slot><slot name="footer"></slot></div>'
  },
  ElForm: formStub(valid),
  ElFormItem: { template: '<div><slot></slot></div>' },
  ElInput: {
    props: ['value'],
    template: '<input class="input-stub" :value="value" @input="$emit(\'input\', $event.target.value)" />'
  },
  ElButton: {
    props: ['loading'],
    template: '<button class="button-stub" @click="$emit(\'click\')"><slot></slot></button>'
  }
})

const component = (await import('./dlg_edit_remark.vue')).default

function mountDialog(propsData = {}, valid = true) {
  return shallowMount(component, {
    propsData: { visible: false, row: { id: 'r1', simpleRemark: '旧备注' }, ...propsData },
    stubs: stubs(valid)
  })
}

describe('bed_mng 修改备注弹窗', () => {
  beforeEach(() => {
    updateSimpleRemark.mockClear()
    updateSimpleRemark.mockResolvedValue({ data: {} })
  })

  it('visible 变 true 时用父级 row 初始化表单', async () => {
    const wrapper = mountDialog()
    await wrapper.setProps({ visible: true })
    expect(wrapper.vm.form).toEqual({ id: 'r1', simpleRemark: '旧备注' })
    expect(wrapper.vm.setFormVisible).toBe(true)
  })

  it('关闭时向父级 emit dlgdo(false)', async () => {
    const wrapper = mountDialog({ visible: true })
    await wrapper.setProps({ visible: true })
    wrapper.vm.setFormVisible = false
    await wrapper.vm.$nextTick()
    expect(wrapper.emitted('dlgdo')[0]).toStrictEqual([false])
  })

  it('校验通过点确定：把 simpleRemark 映射成 remark 调接口，成功后关闭并 emit refresh', async () => {
    const wrapper = mountDialog({ visible: true }, true)
    await wrapper.setProps({ visible: true })
    wrapper.vm.form = { id: 'r1', simpleRemark: '新备注' }

    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()

    // 关键：字段名映射 simpleRemark -> remark（沿用原 handleEditSimpleRemark）
    expect(updateSimpleRemark).toHaveBeenCalledWith({ id: 'r1', remark: '新备注' })
    expect(wrapper.emitted('refresh')).toHaveLength(1)
    expect(wrapper.vm.setFormVisible).toBe(false)
    expect(wrapper.vm.loading).toBe(false)
  })

  it('校验不通过点确定：不调接口、不 emit refresh', async () => {
    const wrapper = mountDialog({ visible: true }, false)
    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()
    expect(updateSimpleRemark).not.toHaveBeenCalled()
    expect(wrapper.emitted('refresh')).toBeUndefined()
  })

  it('接口失败时复位 loading、不 emit refresh', async () => {
    updateSimpleRemark.mockRejectedValueOnce(new Error('boom'))
    const wrapper = mountDialog({ visible: true }, true)
    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.loading).toBe(false)
    expect(wrapper.emitted('refresh')).toBeUndefined()
  })
})
