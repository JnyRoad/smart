import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const sampleRow = {
  status: 'ready',
  badge: 'YD8800010',
  name: '王金鸽',
  parkName: '裕同科技许昌园区',
  dispatcherParkName: '许昌ISC',
  cardNo: '1024388812',
  message: ''
}
let currentSlotRow = sampleRow

const stubs = {
  ElButton: {
    props: ['disabled', 'loading'],
    template: '<button class="button-stub" :disabled="disabled" @click="$emit(\'click\')"><slot></slot></button>'
  },
  ElTag: {
    props: ['type'],
    template: '<span class="tag-stub"><slot></slot></span>'
  },
  ElTable: {
    props: ['data', 'rowClassName'],
    template: '<div class="table-stub"><slot></slot></div>'
  },
  ElTableColumn: {
    props: ['label'],
    template: '<div class="column-stub"><span>{{ label }}</span><slot :row="row" :$index="0"></slot></div>',
    data() {
      return {
        row: currentSlotRow
      }
    }
  }
}

const component = (await import('./QueueTable.vue')).default

function mountQueueTable(propsData = {}) {
  currentSlotRow = propsData.rows && propsData.rows[0] ? propsData.rows[0] : sampleRow
  return shallowMount(component, {
    propsData: {
      rows: [sampleRow],
      readyCount: 1,
      invalidCount: 0,
      submitting: false,
      canSubmit: true,
      formatStatusText: status => (status === 'ready' ? '待提交' : status),
      formatStatusType: status => (status === 'ready' ? 'primary' : status),
      rowClassName: () => '',
      ...propsData
    },
    stubs
  })
}

describe('isc card fast add queue table', () => {
  it('renders queue summary, columns, card number, and status text', () => {
    const wrapper = mountQueueTable()

    expect(wrapper.text()).toContain('待提交队列')
    expect(wrapper.text()).toContain('共1条，可提交1条，异常0条')
    expect(wrapper.text()).toContain('状态')
    expect(wrapper.text()).toContain('工号')
    expect(wrapper.text()).toContain('卡号 / 结果')
    expect(wrapper.text()).toContain('1024388812')
    expect(wrapper.text()).toContain('待提交')
  })

  it('passes rows and row class callback to the Element table unchanged', () => {
    const rows = [{ ...sampleRow, cardNo: '1024388899' }]
    const rowClassName = vi.fn()
    const wrapper = mountQueueTable({
      rows,
      rowClassName
    })
    const table = wrapper.findComponent(stubs.ElTable)

    expect(table.props('data')).toBe(rows)
    expect(table.props('rowClassName')).toBe(rowClassName)
  })

  it('emits row and footer actions without mutating queue state directly', async () => {
    const wrapper = mountQueueTable()
    const buttons = wrapper.findAll('.button-stub')

    await buttons.at(0).trigger('click')
    await buttons.at(1).trigger('click')
    await buttons.at(2).trigger('click')
    await buttons.at(3).trigger('click')

    expect(wrapper.emitted('remove-row')[0]).toStrictEqual([0])
    expect(wrapper.emitted('remove-finished')).toHaveLength(1)
    expect(wrapper.emitted('clear')).toHaveLength(1)
    expect(wrapper.emitted('submit')).toHaveLength(1)
  })

  it('locks row removal and footer actions while submitting', () => {
    const wrapper = mountQueueTable({
      submitting: true,
      canSubmit: false
    })
    const buttons = wrapper.findAll('.button-stub')

    expect(buttons.at(0).attributes('disabled')).toBe('disabled')
    expect(buttons.at(1).attributes('disabled')).toBe('disabled')
    expect(buttons.at(2).attributes('disabled')).toBe('disabled')
    expect(buttons.at(3).attributes('disabled')).toBe('disabled')
  })

  it('keeps saving rows non-removable even when the queue is not submitting', () => {
    const wrapper = mountQueueTable({
      rows: [{ ...sampleRow, status: 'saving' }]
    })

    expect(wrapper.findAll('.button-stub').at(0).attributes('disabled')).toBe('disabled')
  })
})
