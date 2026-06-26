import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const validateForm = vi.fn(callback => callback(true))
const resetFields = vi.fn()

async function mountDialog(propsData = {}) {
  const form = {
    isDormitoryRoom: 0,
    isCount: 1,
    roomType: 20,
    roomSex: 1,
    sdTemplateId: 30
  }
  const rules = {
    isDormitoryRoom: [{ required: true, message: '请选择是否参与分配', trigger: 'change' }]
  }
  const isDormitoryArr = [
    { label: '是', value: 0 },
    { label: '否', value: 1 }
  ]
  const isCountArr = [
    { label: '是', value: 1 },
    { label: '否', value: 0 }
  ]
  const parkDormTypeList = [
    { id: 20, typeName: '四人间' }
  ]
  const sdTempList = [
    { id: 30, templateName: '水电模板' }
  ]

  const wrapper = shallowMount((await import('./RoomBatchEditDialog.vue')).default, {
    propsData: {
      title: '批量设置房间类型',
      visible: true,
      form,
      rules,
      isHandelSD: false,
      isDormitoryArr,
      isCountArr,
      parkDormTypeList,
      sdTempList,
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
      ElSelect: {
        name: 'ElSelect',
        props: ['value', 'placeholder'],
        template: '<div><slot /></div>'
      },
      ElOption: {
        props: ['label', 'value'],
        template: '<span>{{ label }}</span>'
      },
      ElButton: {
        name: 'ElButton',
        props: ['loading', 'plain', 'type'],
        template: '<button @click="$emit(\'click\')"><slot /></button>'
      },
      RoomGenderSelect: {
        name: 'RoomGenderSelect',
        props: ['value'],
        template: '<div class="room-gender-select-stub"></div>'
      }
    }
  })

  return { wrapper, form, rules, isDormitoryArr, isCountArr, parkDormTypeList, sdTempList }
}

describe('RoomBatchEditDialog', () => {
  it('保留普通批量编辑弹窗标题、visible、表单绑定和字段', async () => {
    const { wrapper, form, rules, isDormitoryArr, isCountArr, parkDormTypeList } = await mountDialog()

    const dialog = wrapper.findComponent({ name: 'ElDialog' })
    const elForm = wrapper.findComponent({ name: 'ElForm' })
    const selects = wrapper.findAllComponents({ name: 'ElSelect' })
    const genderSelect = wrapper.findComponent({ name: 'RoomGenderSelect' })

    expect(dialog.props()).toMatchObject({
      title: '批量设置房间类型',
      visible: true,
      width: '600px'
    })
    expect(elForm.props()).toMatchObject({
      rules,
      model: form,
      labelWidth: '120px'
    })
    expect(wrapper.text()).toContain('是否参与分配')
    expect(wrapper.text()).toContain('是否参与计算')
    expect(wrapper.text()).toContain('宿舍分类')
    expect(wrapper.text()).toContain('房间属性')
    expect(wrapper.text()).toContain(isDormitoryArr[0].label)
    expect(wrapper.text()).toContain(isCountArr[0].label)
    expect(wrapper.text()).toContain(parkDormTypeList[0].typeName)
    expect(wrapper.text()).not.toContain('水电分摊模板')
    expect(selects.at(0).props('value')).toBe(0)
    expect(selects.at(1).props('value')).toBe(1)
    expect(selects.at(2).props('value')).toBe(20)
    expect(genderSelect.props('value')).toBe(1)
  })

  it('保留水电模板批量编辑字段', async () => {
    const { wrapper, form, rules, sdTempList } = await mountDialog({
      title: '批量设置房间水电模板',
      isHandelSD: true
    })

    const dialog = wrapper.findComponent({ name: 'ElDialog' })
    const elForm = wrapper.findComponent({ name: 'ElForm' })
    const selects = wrapper.findAllComponents({ name: 'ElSelect' })

    expect(dialog.props('title')).toBe('批量设置房间水电模板')
    expect(elForm.props()).toMatchObject({
      rules,
      model: form,
      labelWidth: '120px'
    })
    expect(wrapper.text()).toContain('水电分摊模板')
    expect(wrapper.text()).toContain(sdTempList[0].templateName)
    expect(wrapper.text()).not.toContain('是否参与分配')
    expect(selects).toHaveLength(1)
    expect(selects.at(0).props('value')).toBe(30)
  })

  it('保留普通模式和水电模板模式的字段更新事件', async () => {
    const { wrapper } = await mountDialog()
    const selects = wrapper.findAllComponents({ name: 'ElSelect' })
    const genderSelect = wrapper.findComponent({ name: 'RoomGenderSelect' })

    selects.at(0).vm.$emit('input', 1)
    selects.at(1).vm.$emit('input', 0)
    selects.at(2).vm.$emit('input', 21)
    genderSelect.vm.$emit('input', 2)

    expect(wrapper.emitted('update-form-field')).toStrictEqual([
      [{ field: 'isDormitoryRoom', value: 1 }],
      [{ field: 'isCount', value: 0 }],
      [{ field: 'roomType', value: 21 }],
      [{ field: 'roomSex', value: 2 }]
    ])

    const sdDialog = await mountDialog({ isHandelSD: true })
    sdDialog.wrapper.findComponent({ name: 'ElSelect' }).vm.$emit('input', 31)

    expect(sdDialog.wrapper.emitted('update-form-field')).toStrictEqual([
      [{ field: 'sdTemplateId', value: 31 }]
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

  it('向父页暴露 validate 和 resetFields，保持原 $refs.batchEditForm 调用契约', async () => {
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
