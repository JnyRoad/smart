import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const validateForm = vi.fn(callback => callback(true))
const resetFields = vi.fn()

async function mountDialog(propsData = {}) {
  const form = { parkId: 10, dormitoryName: 'A栋' }
  const rules = {
    dormitoryName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }]
  }

  const wrapper = shallowMount((await import('./RoomDormitoryDialog.vue')).default, {
    propsData: {
      title: '新增楼栋',
      visible: true,
      form,
      rules,
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
        props: ['rules', 'model', 'labelWidth', 'labelPosition'],
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
        props: ['value', 'clearable'],
        template: '<input :value="value" @input="$emit(\'input\', $event.target.value)" />'
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

describe('RoomDormitoryDialog', () => {
  it('保留楼栋弹窗标题、visible、表单绑定和字段', async () => {
    const { wrapper, form, rules } = await mountDialog()

    const dialog = wrapper.findComponent({ name: 'ElDialog' })
    const elForm = wrapper.findComponent({ name: 'ElForm' })

    expect(dialog.props()).toMatchObject({
      title: '新增楼栋',
      visible: true,
      width: '550px'
    })
    expect(elForm.props()).toMatchObject({
      rules,
      model: form,
      labelWidth: '100px',
      labelPosition: 'left'
    })
    expect(wrapper.text()).toContain('宿舍楼名称')
    expect(wrapper.text()).toContain('取 消')
    expect(wrapper.text()).toContain('确 定')

    const input = wrapper.find('input')
    input.element.value = 'B栋'
    await input.trigger('input')
    expect(wrapper.emitted('update-form-field')[0]).toStrictEqual([
      { field: 'dormitoryName', value: 'B栋' }
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

  it('向父页暴露 validate 和 resetFields，保持原 $refs.dormForm 调用契约', async () => {
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
