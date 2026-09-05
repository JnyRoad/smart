import { shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const serviceMocks = vi.hoisted(() => ({
  getCauseEnum: vi.fn(),
  getList: vi.fn(),
  reSend: vi.fn(),
  revoke: vi.fn()
}))

const publicMocks = vi.hoisted(() => ({
  getCompTree: vi.fn()
}))

const visitorRecordMocks = vi.hoisted(() => ({
  delAuth: vi.fn(),
  reSend: vi.fn()
}))

vi.mock('@/api/platform/visitor/visitor_record', () => ({
  delAuth: visitorRecordMocks.delAuth,
  reSend: visitorRecordMocks.reSend
}))

vi.mock('@/api/platform/_publicService', () => ({
  getCompTree: publicMocks.getCompTree
}))

vi.mock('@/util/util', () => ({
  isArrayFn: Array.isArray
}))

vi.mock('./_service', () => ({
  xcIncomingRecordApi: serviceMocks
}))

vi.mock('./manualAuth', () => ({ default: { name: 'ManualAuthStub' } }), { virtual: true })

const component = (await import('./index.vue')).default

const stubs = {
  ManualAuthDialog: { template: '<div class="manual-auth-dialog-stub" />' },
  ElScrollbar: { template: '<div><slot /></div>' },
  'tce-Search-bar': { template: '<div><slot /></div>' },
  ElForm: { template: '<form><slot /></form>' },
  ElFormItem: { props: ['label'], template: '<div><span>{{ label }}</span><slot /></div>' },
  ElInput: { template: '<input />' },
  ElSelect: { template: '<select><slot /></select>' },
  ElOption: { props: ['label'], template: '<option>{{ label }}</option>' },
  ElCascader: { template: '<select />' },
  ElDatePicker: { template: '<input />' },
  ElTooltip: { template: '<div><slot /></div>' },
  ElButton: {
    props: ['disabled', 'loading'],
    template: '<button :disabled="disabled"><slot /></button>'
  },
  ElRadio: {
    props: ['label', 'value'],
    template: '<label class="record-radio"><input type="radio" :checked="value === label" @change="$emit(\'input\', label); $emit(\'change\', label)" /><slot /></label>'
  },
  ElPagination: { template: '<div />' }
}

function flushPromises () {
  return Promise.resolve().then(() => Promise.resolve()).then(() => Promise.resolve())
}

function mountIncomingRecord (permissions = { platform_visitor_incoming_auth: true }, router = { push: vi.fn() }) {
  vi.stubGlobal('WebSocket', class {
    close () {}
  })
  return shallowMount(component, {
    stubs,
    methods: {
      errorImgPeaple: () => '',
      errorImgCar: () => ''
    },
    filters: {
      visitorStatusClassFormat: value => value,
      visitorStatusFormat: value => value
    },
    computed: {
      permissions: () => permissions
    },
    mocks: {
      $route: { query: {} },
      $router: router,
      $message: { error: vi.fn() }
    }
  })
}

describe('入厂申请记录手动授权入口', () => {
  beforeEach(() => {
    serviceMocks.getCauseEnum.mockResolvedValue({ data: { data: [] } })
    serviceMocks.getList.mockResolvedValue({ data: { data: { records: [], total: 0 } } })
    publicMocks.getCompTree.mockResolvedValue({ data: { data: [] } })
  })

  it('选择卡片只保留一个申请，并允许工具条识别当前申请', () => {
    expect(typeof component.methods.selectIncomingRecord).toBe('function')

    const context = {
      selectedRecord: null,
      selectedRecordId: null
    }
    const firstRecord = { id: 101, visitorName: '访客甲' }
    const secondRecord = { id: 102, visitorName: '访客乙' }

    component.methods.selectIncomingRecord.call(context, firstRecord)
    expect(context.selectedRecord).toBe(firstRecord)
    expect(context.selectedRecordId).toBe(101)

    component.methods.selectIncomingRecord.call(context, secondRecord)
    expect(context.selectedRecord).toBe(secondRecord)
    expect(context.selectedRecordId).toBe(102)
  })

  it('搜索和翻页前清空当前申请选择，避免把旧选择带到新结果', () => {
    expect(typeof component.methods.clearIncomingRecordSelection).toBe('function')

    const clearIncomingRecordSelection = vi.fn()
    const getList = vi.fn()
    const context = {
      clearIncomingRecordSelection,
      getList,
      page: { currentPage: 3, pageSize: 8 },
      searchForm: { visitorName: '访客甲' }
    }

    component.methods.searchSubmit.call(context, context.searchForm)
    component.methods.handleSizeChange.call(context, 16)
    component.methods.handleCurrentChange.call(context, 2)

    expect(clearIncomingRecordSelection).toHaveBeenCalledTimes(3)
    expect(getList).toHaveBeenCalledTimes(3)
    expect(context.page.currentPage).toBe(2)
    expect(context.page.pageSize).toBe(16)
  })

  it('直接刷新列表也清空当前申请选择', async () => {
    const wrapper = mountIncomingRecord()
    await flushPromises()
    await wrapper.setData({
      selectedRecord: { id: 101, visitorName: '访客甲' },
      selectedRecordId: 101
    })

    wrapper.vm.getList(wrapper.vm.page, wrapper.vm.searchForm)

    expect(wrapper.vm.selectedRecord).toBeNull()
    expect(wrapper.vm.selectedRecordId).toBeNull()
  })

  it('单选控件点击只选择申请，不冒泡打开详情；无权限时工具条不渲染入口', async () => {
    const router = { push: vi.fn() }
    const wrapper = mountIncomingRecord({}, router)
    await flushPromises()
    await wrapper.setData({ visitor: [{ id: 101, visitorName: '访客甲', cause: 1 }] })

    expect(wrapper.text()).not.toContain('通关权限')
    await wrapper.find('.record-radio input').trigger('change')

    expect(wrapper.vm.selectedRecord).toEqual({ id: 101, visitorName: '访客甲', cause: 1 })
    expect(wrapper.vm.selectedRecordId).toBe(101)
    expect(wrapper.find('.visitor-card').classes()).toContain('is-selected')
    expect(router.push).not.toHaveBeenCalled()
  })

  it('授予入口权限时无选择按钮不可用，选中申请后可用', async () => {
    const wrapper = mountIncomingRecord()
    await flushPromises()
    await wrapper.setData({ visitor: [{ id: 101, visitorName: '访客甲', cause: 1 }] })

    const getAuthButton = () => wrapper.findAll('button').wrappers.find(button => button.text() === '通关权限')
    expect(getAuthButton().attributes('disabled')).toBe('disabled')

    await wrapper.find('.record-radio input').trigger('change')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.selectedRecordId).toBe(101)
    expect(wrapper.find('.visitor-card').classes()).toContain('is-selected')
    expect(getAuthButton().attributes('disabled')).toBeUndefined()
  })
})
