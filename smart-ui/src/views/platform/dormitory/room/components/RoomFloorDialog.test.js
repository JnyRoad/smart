import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const validateForm = vi.fn(callback => callback(true))
const resetFields = vi.fn()

async function mountDialog(propsData = {}) {
  const form = {
    floorName: '1F',
    roomNum: 10,
    startNum: 1,
    floorNum: 3
  }
  const rules = {
    startNum: [{ required: true, message: '请输入起始编号', trigger: 'blur' }],
    floorNum: [{ required: true, message: '请输入楼层数量', trigger: 'blur' }]
  }

  const wrapper = shallowMount((await import('./RoomFloorDialog.vue')).default, {
    propsData: {
      title: '新增楼层',
      visible: true,
      form,
      rules,
      editFloor: false,
      hasStartNum: false,
      loading: false,
      ...propsData
    },
    stubs: {
      ElDialog: {
        name: 'ElDialog',
        props: ['title', 'visible', 'width'],
        template: '<section><h2>{{ title }}</h2><slot /><slot name="footer" /></section>'
      },
      ElForm: {
        name: 'ElForm',
        props: ['rules', 'model', 'labelWidth'],
        methods: {
          validate: validateForm,
          resetFields
        },
        template: '<form><slot /></form>'
      },
      ElFormItem: {
        props: ['label', 'prop'],
        template: '<label><span>{{ label }}</span><slot /></label>'
      },
      ElInput: {
        name: 'ElInput',
        props: ['value', 'clearable', 'disabled'],
        template: '<input :value="value" :disabled="disabled" @input="$emit(\'input\', $event.target.value)" />'
      },
      ElButton: {
        name: 'ElButton',
        props: ['loading', 'plain', 'type'],
        template: '<button @click="$emit(\'click\')"><slot /></button>'
      }
    }
  })

  return { wrapper, form, rules }
}

describe('RoomFloorDialog', () => {
  it('保留新增楼层弹窗标题、visible、表单绑定和字段', async () => {
    const { wrapper, form, rules } = await mountDialog({ hasStartNum: true })

    const dialog = wrapper.findComponent({ name: 'ElDialog' })
    const elForm = wrapper.findComponent({ name: 'ElForm' })
    const inputs = wrapper.findAllComponents({ name: 'ElInput' })

    expect(dialog.props()).toMatchObject({
      title: '新增楼层',
      visible: true,
      width: '550px'
    })
    expect(elForm.props()).toMatchObject({
      rules,
      model: form,
      labelWidth: '80px'
    })
    expect(wrapper.text()).toContain('起始编号')
    expect(wrapper.text()).toContain('楼层数量')
    expect(wrapper.text()).toContain('取 消')
    expect(wrapper.text()).toContain('确 定')
    expect(inputs.at(0).props()).toMatchObject({
      value: 1,
      clearable: '',
      disabled: true
    })
    expect(inputs.at(1).props()).toMatchObject({
      value: 3,
      clearable: ''
    })
  })

  it('保留编辑楼层字段和禁用的楼层编号', async () => {
    const { wrapper } = await mountDialog({
      title: '编辑楼层',
      editFloor: true
    })

    const inputs = wrapper.findAllComponents({ name: 'ElInput' })

    expect(wrapper.text()).toContain('楼层编号')
    expect(wrapper.text()).toContain('房间数量')
    expect(inputs.at(0).props()).toMatchObject({
      value: '1F',
      disabled: ''
    })
    expect(inputs.at(1).props()).toMatchObject({
      value: 10,
      clearable: ''
    })
  })

  it('保留楼层表单字段更新事件', async () => {
    const { wrapper } = await mountDialog()
    const inputs = wrapper.findAll('input')

    inputs.at(0).element.value = '2'
    await inputs.at(0).trigger('input')
    inputs.at(1).element.value = '4'
    await inputs.at(1).trigger('input')

    expect(wrapper.emitted('update-form-field')).toStrictEqual([
      [{ field: 'startNum', value: '2' }],
      [{ field: 'floorNum', value: '4' }]
    ])
  })

  it('保留关闭和提交事件，并透传原 loading 状态', async () => {
    const { wrapper } = await mountDialog({ loading: true })
    const buttons = wrapper.findAll('button')

    await buttons.at(0).trigger('click')
    await buttons.at(1).trigger('click')

    expect(wrapper.findAllComponents({ name: 'ElButton' }).at(1).props('loading')).toBe(true)
    expect(wrapper.emitted('close')).toHaveLength(1)
    expect(wrapper.emitted('submit')).toHaveLength(1)
  })

  it('向父页暴露 validate 和 resetFields，保持原 $refs.floorForm 调用契约', async () => {
    const { wrapper } = await mountDialog()
    const callback = vi.fn()

    validateForm.mockClear()
    resetFields.mockClear()

    wrapper.vm.validate(callback)
    wrapper.vm.resetFields()

    expect(validateForm).toHaveBeenCalledWith(callback)
    expect(callback).toHaveBeenCalledWith(true)
    expect(resetFields).toHaveBeenCalledTimes(1)
  })
})
