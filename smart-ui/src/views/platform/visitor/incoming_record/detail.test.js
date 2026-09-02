import { describe, expect, it, vi } from 'vitest'

vi.mock('@/api/platform/visitor/visitor_record_detail', () => ({ addBlackObj: vi.fn() }))
vi.mock('@/api/platform/visitor/visitor_record', () => ({ delAuth: vi.fn() }))
vi.mock('@/api/platform/_publicService', () => ({ getCompTree: vi.fn() }))
vi.mock('@/util/util', () => ({ isArrayFn: Array.isArray }))
vi.mock('./_service', () => ({
  xcIncomingRecordApi: {
    getAreaType: vi.fn(),
    getDetail: vi.fn()
  }
}))

const component = (await import('./detail.vue')).default
const truckComponent = (await import('./truck.vue')).default

describe('入厂申请详情返回', () => {
  it('返回列表时替换详情历史并原样携带查询条件和分页', () => {
    const replace = vi.fn()
    const queryPage = { currentPage: 2, pageSize: 8 }
    const queryForm = { visitorName: '王小雨', status: '2' }

    component.methods.goBack.call({
      $router: { replace },
      $route: { query: { queryPage, queryForm } }
    })

    expect(replace).toHaveBeenCalledWith({
      path: '/platform/visitor/incoming_record',
      query: { queryPage, queryForm }
    })
  })

  it('从货车记录进入时返回货车列表并保留查询条件和分页', () => {
    const replace = vi.fn()
    const queryPage = { currentPage: 3, pageSize: 16 }
    const queryForm = { visitorName: '司机甲', vehiclePlate: '豫A12345' }

    component.methods.goBack.call({
      $router: { replace },
      $route: { query: { queryPage, queryForm, recordType: 'truck' } }
    })

    expect(replace).toHaveBeenCalledWith({
      path: '/platform/visitor/incoming_record/truck',
      query: { queryPage, queryForm, recordType: 'truck' }
    })
  })

  it('货车列表进入详情时标记返回目标并保留查询条件和分页', () => {
    const push = vi.fn()
    const page = { currentPage: 3, pageSize: 16 }
    const searchForm = { visitorName: '司机甲', vehiclePlate: '豫A12345' }

    truckComponent.methods.handleDetail.call({
      $router: { push },
      page,
      searchForm
    }, { id: 88 })

    expect(push).toHaveBeenCalledWith({
      path: '/platform/visitor/incoming_record/detail/88',
      query: { queryPage: page, queryForm: searchForm, recordType: 'truck' }
    })
  })
})
