import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const stubs = {
  ElButton: {
    template: '<button class="refresh-button" @click="$emit(\'click\')"><slot></slot></button>'
  },
  ElTable: {
    props: ['data', 'loading'],
    template: '<div class="table-stub"><slot></slot></div>'
  },
  ElTableColumn: {
    props: ['label'],
    template: '<div class="column-stub"><span>{{ label }}</span><slot :row="{ action: 2, actionDesc: \'删除卡片\' }"></slot></div>'
  }
}

const component = (await import('./TaskTable.vue')).default

function mountTaskTable(propsData = {}) {
  return shallowMount(component, {
    propsData: {
      loading: false,
      rows: [{ id: 1, badge: 'YD8800010', cardNo: '1024388812' }],
      formatAction: row => row.actionDesc || '-',
      ...propsData
    },
    directives: {
      loading(el, binding) {
        el.setAttribute('data-loading', String(binding.value))
      }
    },
    stubs
  })
}

describe('isc card fast add task table', () => {
  it('renders the task table columns and formatted action text', () => {
    const wrapper = mountTaskTable()

    expect(wrapper.text()).toContain('最近录卡结果 / 同步任务追踪')
    expect(wrapper.text()).toContain('园区')
    expect(wrapper.text()).toContain('工号')
    expect(wrapper.text()).toContain('卡 ID')
    expect(wrapper.text()).toContain('任务动作')
    expect(wrapper.text()).toContain('删除卡片')
  })

  it('passes rows and loading state to the Element table unchanged', () => {
    const rows = [{ id: 9, badge: 'YD8800099' }]
    const wrapper = mountTaskTable({
      loading: true,
      rows
    })
    const table = wrapper.findComponent(stubs.ElTable)

    expect(table.props('data')).toBe(rows)
    expect(wrapper.find('.table-stub').attributes('data-loading')).toBe('true')
  })

  it('emits refresh when the refresh button is clicked', async () => {
    const wrapper = mountTaskTable()

    await wrapper.find('.refresh-button').trigger('click')

    expect(wrapper.emitted('refresh')).toHaveLength(1)
  })

  it('requires an explicit formatter so the parent keeps task action rules', () => {
    const formatAction = vi.fn(() => '新增卡片')
    const wrapper = mountTaskTable({ formatAction })

    expect(wrapper.text()).toContain('新增卡片')
    expect(formatAction).toHaveBeenCalledWith({ action: 2, actionDesc: '删除卡片' })
  })
})
