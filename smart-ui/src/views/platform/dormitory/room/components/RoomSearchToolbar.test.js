import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const stubs = {
  ElButton: {
    props: ['icon', 'loading', 'plain', 'type'],
    template: '<button class="button-stub" :data-icon="icon || \'\'" :data-loading="String(!!loading)" @click="$emit(\'click\')"><slot /></button>'
  },
  ElForm: {
    template: '<form class="form-stub"><slot /></form>',
    methods: {
      resetFields: vi.fn()
    }
  },
  ElFormItem: {
    props: ['label'],
    template: '<div class="form-item-stub"><span>{{ label }}</span><slot /></div>'
  },
  ElSelect: {
    props: ['value', 'placeholder'],
    template: '<div class="select-stub" :data-placeholder="placeholder || \'\'" :data-value="String(value)"><slot /></div>'
  },
  ElOption: {
    props: ['label', 'value'],
    template: '<span class="option-stub" :data-value="String(value)">{{ label }}</span>'
  }
}

const component = (await import('./RoomSearchToolbar.vue')).default
const resetFieldsMock = stubs.ElForm.methods.resetFields

function mountToolbar(propsData = {}) {
  return shallowMount(component, {
    propsData: {
      searchForm: {
        isDormitoryRoom: undefined,
        isCount: undefined,
        roomType: undefined,
        roomSex: undefined
      },
      allDormTypeList: [
        { id: 1, typeName: '员工宿舍' },
        { id: 2, typeName: '干部宿舍' }
      ],
      hasData: true,
      exportLoading: false,
      ...propsData
    },
    stubs
  })
}

function buttonByText(wrapper, text) {
  return wrapper.findAll('.button-stub').filter(button => button.text().includes(text)).at(0)
}

function hasButtonText(wrapper, text) {
  return wrapper.findAll('.button-stub').filter(button => button.text().includes(text)).length > 0
}

describe('room search toolbar', () => {
  it('renders the existing title, search fields, and data actions when rooms exist', () => {
    const wrapper = mountToolbar({
      exportLoading: true
    })

    expect(wrapper.text()).toContain('房间')
    expect(wrapper.text()).toContain('是否参与分配')
    expect(wrapper.text()).toContain('是否参与计算')
    expect(wrapper.text()).toContain('宿舍分类')
    expect(wrapper.text()).toContain('房间属性')
    expect(wrapper.text()).toContain('员工宿舍')
    expect(wrapper.text()).toContain('干部宿舍')
    expect(buttonByText(wrapper, '搜索').attributes('data-icon')).toBe('el-icon-search')
    expect(buttonByText(wrapper, '清空').attributes('data-icon')).toBe('el-icon-delete')
    expect(buttonByText(wrapper, '导出表格').attributes('data-icon')).toBe('icon-yutong-download')
    expect(buttonByText(wrapper, '导出表格').attributes('data-loading')).toBe('true')
    expect(buttonByText(wrapper, '批量设置房间类型').attributes('data-icon')).toBe('el-icon-edit')
    expect(buttonByText(wrapper, '批量设置房间水电模板').attributes('data-icon')).toBe('el-icon-edit')
  })

  it('hides export and batch actions when the parent has no room data', () => {
    const wrapper = mountToolbar({
      hasData: false
    })

    expect(buttonByText(wrapper, '搜索').exists()).toBe(true)
    expect(buttonByText(wrapper, '清空').exists()).toBe(true)
    expect(hasButtonText(wrapper, '导出表格')).toBe(false)
    expect(hasButtonText(wrapper, '批量设置房间类型')).toBe(false)
    expect(hasButtonText(wrapper, '批量设置房间水电模板')).toBe(false)
  })

  it('emits toolbar actions without touching parent state directly', async () => {
    const wrapper = mountToolbar()

    await buttonByText(wrapper, '搜索').trigger('click')
    await buttonByText(wrapper, '清空').trigger('click')
    await buttonByText(wrapper, '导出表格').trigger('click')
    await buttonByText(wrapper, '批量设置房间类型').trigger('click')
    await buttonByText(wrapper, '批量设置房间水电模板').trigger('click')

    expect(wrapper.emitted('search')).toHaveLength(1)
    expect(wrapper.emitted('reset')).toHaveLength(1)
    expect(wrapper.emitted('export')).toHaveLength(1)
    expect(wrapper.emitted('batch-edit')).toHaveLength(1)
    expect(wrapper.emitted('sd-batch-edit')).toHaveLength(1)
  })

  it('emits field updates and exposes resetFields for the parent wrapper', () => {
    resetFieldsMock.mockClear()
    const wrapper = mountToolbar()

    wrapper.vm.updateSearchField('roomSex', '2')
    wrapper.vm.resetFields()

    expect(wrapper.emitted('update-search-field')).toStrictEqual([[{ field: 'roomSex', value: '2' }]])
    expect(resetFieldsMock).toHaveBeenCalledTimes(1)
  })
})
