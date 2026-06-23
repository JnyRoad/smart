import { shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const sampleStaff = {
  id: 1001,
  badge: 'YD8800010',
  name: '王金鸽',
  status: 1,
  parkName: '裕同科技许昌园区',
  jobName: '工程师',
  createTime: '2024-01-08',
  compName: '裕同科技',
  depName: '智慧园区'
}

const sampleCandidate = {
  id: 1002,
  badge: 'YD8800011',
  name: '李明',
  depName: '安防部'
}

const sampleCard = {
  id: 2001,
  cardNo: '1024388812',
  dispatcherParkName: '许昌ISC',
  syncStatus: 1,
  lastSyncRemark: '同步成功'
}

let currentCandidateRow = null
let currentCardRow = null

const stubs = {
  ElButton: {
    props: ['disabled', 'loading'],
    template: '<button class="button-stub" :disabled="disabled" :data-loading="String(!!loading)" @click="$emit(\'click\')"><slot></slot></button>'
  },
  ElCol: {
    template: '<div class="col-stub"><slot></slot></div>'
  },
  ElRow: {
    template: '<div class="row-stub"><slot></slot></div>'
  },
  ElTable: {
    props: ['data', 'loading'],
    template: '<div class="table-stub"><slot></slot></div>'
  },
  ElTableColumn: {
    props: ['label', 'prop', 'width'],
    template: '<div class="column-stub"><span>{{ label }}</span><span v-if="prop && rowForColumn">{{ rowForColumn[prop] }}</span><slot v-if="rowForColumn" :row="rowForColumn" :$index="0"></slot></div>',
    computed: {
      rowForColumn() {
        if (this.label === '操作' && String(this.width) === '70') {
          return currentCandidateRow
        }
        if (this.label === '操作' && String(this.width) === '80') {
          return currentCardRow
        }
        if (this.label === '同步状态' || this.prop === 'cardNo' || this.prop === 'dispatcherParkName') {
          return currentCardRow
        }
        if (this.prop === 'badge' || this.prop === 'name' || this.prop === 'depName') {
          return currentCandidateRow
        }
        return null
      }
    }
  },
  ElTag: {
    props: ['type'],
    template: '<span class="tag-stub"><slot></slot></span>'
  }
}

const component = (await import('./StaffPanel.vue')).default

function mountStaffPanel(propsData = {}) {
  currentCandidateRow = propsData.staffCandidates && propsData.staffCandidates[0] ? propsData.staffCandidates[0] : null
  currentCardRow = propsData.staffCards && propsData.staffCards[0] ? propsData.staffCards[0] : null
  return shallowMount(component, {
    propsData: {
      selectedStaff: null,
      staffCandidates: [],
      staffCards: [],
      staffCardLoading: false,
      staffCardDeleting: null,
      staffAvatarText: '员',
      staffStatusText: status => (Number(status) === 1 ? '在职' : '离职'),
      cardSyncStatusText: row => (row.syncStatus === 1 ? '已同步' : '未同步'),
      cardSyncStatusType: syncStatus => (syncStatus === 1 ? 'success' : 'warning'),
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

function tableStubs(wrapper) {
  return wrapper.findAllComponents(stubs.ElTable)
}

describe('isc card fast add staff panel', () => {
  it('renders the empty current-entry state and keeps the card table visible', () => {
    const wrapper = mountStaffPanel()

    expect(wrapper.text()).toContain('当前录入')
    expect(wrapper.text()).toContain('先输入工号或姓名定位员工')
    expect(wrapper.text()).toContain('已有ISC卡片')
    expect(wrapper.text()).toContain('卡号')
    expect(wrapper.text()).toContain('同步状态')
  })

  it('renders selected staff details and emits the detail action with the same staff row', async () => {
    const wrapper = mountStaffPanel({
      selectedStaff: sampleStaff,
      staffAvatarText: '王'
    })

    expect(wrapper.text()).toContain('王')
    expect(wrapper.text()).toContain('王金鸽')
    expect(wrapper.text()).toContain('YD8800010')
    expect(wrapper.text()).toContain('裕同科技 / 智慧园区')
    expect(wrapper.text()).toContain('裕同科技许昌园区')
    expect(wrapper.text()).toContain('工程师')
    expect(wrapper.text()).toContain('2024-01-08')
    expect(wrapper.text()).toContain('在职')

    await wrapper.findAll('.button-stub').filter(button => button.text() === '查看人员详情').at(0).trigger('click')

    expect(wrapper.emitted('open-detail')[0]).toStrictEqual([sampleStaff])
  })

  it('passes candidate rows into the candidate table and emits select-staff from that row', async () => {
    const staffCandidates = [sampleCandidate]
    const wrapper = mountStaffPanel({
      staffCandidates
    })
    const tables = tableStubs(wrapper)

    expect(tables).toHaveLength(2)
    expect(tables.at(0).props('data')).toBe(staffCandidates)
    expect(wrapper.text()).toContain('匹配人员')
    expect(wrapper.text()).toContain('YD8800011')
    expect(wrapper.text()).toContain('李明')
    expect(wrapper.text()).toContain('安防部')

    await wrapper.findAll('.button-stub').filter(button => button.text() === '选择').at(0).trigger('click')

    expect(wrapper.emitted('select-staff')[0]).toStrictEqual([sampleCandidate])
  })

  it('passes card rows into the card table, renders card fields, and emits remove-card for that row', async () => {
    const staffCards = [sampleCard]
    const cardSyncStatusText = vi.fn(() => '已同步')
    const cardSyncStatusType = vi.fn(() => 'success')
    const wrapper = mountStaffPanel({
      staffCards,
      cardSyncStatusText,
      cardSyncStatusType
    })
    const tables = tableStubs(wrapper)

    expect(tables).toHaveLength(1)
    expect(tables.at(0).props('data')).toBe(staffCards)
    expect(wrapper.text()).toContain('已有ISC卡片')
    expect(wrapper.text()).toContain('1024388812')
    expect(wrapper.text()).toContain('许昌ISC')
    expect(wrapper.text()).toContain('已同步')
    expect(cardSyncStatusText).toHaveBeenCalledWith(sampleCard)
    expect(cardSyncStatusType).toHaveBeenCalledWith(sampleCard.syncStatus)

    await wrapper.findAll('.button-stub').filter(button => button.text() === '删除').at(0).trigger('click')

    expect(wrapper.emitted('remove-card')[0]).toStrictEqual([sampleCard])
  })

  it('keeps the existing delete loading and cross-row lock rules', () => {
    const deletingCurrent = mountStaffPanel({
      staffCards: [sampleCard],
      staffCardDeleting: sampleCard.id
    })
    const deletingAnother = mountStaffPanel({
      staffCards: [sampleCard],
      staffCardDeleting: 9999
    })

    expect(deletingCurrent.findAll('.button-stub').filter(button => button.text() === '删除').at(0).attributes('data-loading')).toBe('true')
    expect(deletingAnother.findAll('.button-stub').filter(button => button.text() === '删除').at(0).attributes('disabled')).toBe('disabled')
  })
})
