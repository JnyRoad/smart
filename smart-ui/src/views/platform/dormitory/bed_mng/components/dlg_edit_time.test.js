import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi, beforeEach } from 'vitest'

// mock 床位管理 API：只关心 updateDormitoryStaff 被如何调用，默认返回成功
const updateDormitoryStaff = vi.fn(() => Promise.resolve({ data: {} }))
vi.mock('@/api/platform/dormitory/bed_mng', () => ({
  updateDormitoryStaff: (...args) => updateDormitoryStaff(...args)
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
  ElDatePicker: {
    props: ['value'],
    template: '<input class="date-stub" :value="value" @input="$emit(\'input\', $event.target.value)" />'
  },
  ElButton: {
    props: ['loading'],
    template: '<button class="button-stub" @click="$emit(\'click\')"><slot></slot></button>'
  }
})

const component = (await import('./dlg_edit_time.vue')).default

function mountDialog(propsData = {}, valid = true) {
  return shallowMount(component, {
    propsData: { visible: false, row: { id: 'x1', createTime: '2024-03-05' }, ...propsData },
    stubs: stubs(valid)
  })
}

describe('bed_mng 修改入住时间弹窗', () => {
  beforeEach(() => {
    updateDormitoryStaff.mockClear()
    updateDormitoryStaff.mockResolvedValue({ data: {} })
  })

  it('visible 变 true 时用父级 row 初始化表单（等价原 editTime 赋值）', async () => {
    const wrapper = mountDialog()
    await wrapper.setProps({ visible: true })
    expect(wrapper.vm.form).toEqual({ id: 'x1', createTime: '2024-03-05' })
    expect(wrapper.vm.setFormVisible).toBe(true)
  })

  it('关闭（setFormVisible→false）时向父级 emit dlgdo(false)', async () => {
    const wrapper = mountDialog({ visible: true })
    await wrapper.setProps({ visible: true })
    wrapper.vm.setFormVisible = false
    await wrapper.vm.$nextTick()
    expect(wrapper.emitted('dlgdo')[0]).toStrictEqual([false])
  })

  it('校验通过点确定：用当前表单调 updateDormitoryStaff，成功后关闭并 emit refresh', async () => {
    const wrapper = mountDialog({ visible: true }, true)
    await wrapper.setProps({ visible: true })
    wrapper.vm.form = { id: 'x1', createTime: '2024-09-09' }

    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()

    expect(updateDormitoryStaff).toHaveBeenCalledWith({ id: 'x1', createTime: '2024-09-09' })
    expect(wrapper.emitted('refresh')).toHaveLength(1)
    expect(wrapper.vm.setFormVisible).toBe(false)
    expect(wrapper.vm.loading).toBe(false)
  })

  it('校验不通过点确定：不调用 API、不 emit refresh', async () => {
    const wrapper = mountDialog({ visible: true }, false)
    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()
    expect(updateDormitoryStaff).not.toHaveBeenCalled()
    expect(wrapper.emitted('refresh')).toBeUndefined()
  })

  it('API 失败时复位 loading、不 emit refresh', async () => {
    updateDormitoryStaff.mockRejectedValueOnce(new Error('boom'))
    const wrapper = mountDialog({ visible: true }, true)
    await wrapper.findAll('.button-stub').at(1).trigger('click')
    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.loading).toBe(false)
    expect(wrapper.emitted('refresh')).toBeUndefined()
  })
})
