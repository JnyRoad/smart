import { beforeEach, describe, expect, it, vi } from 'vitest'

const fetchPage = vi.fn()
const exportLogs = vi.fn()
const fetchTasks = vi.fn()

vi.mock('@/api/platform/securityAuthDeleteLog', () => ({
  fetchPage,
  exportLogs,
  fetchTasks
}))

const component = (await import('./index.vue')).default

function createContext(overrides = {}) {
  return Object.assign({}, component.methods, {
    searchForm: {
      dateRange: [],
      parkId: '',
      staffBadge: '',
      staffName: '',
      department: '',
      authName: '',
      result: ''
    },
    page: { currentPage: 1, pageSize: 20, total: 0 },
    tableData: [],
    tableLoading: false,
    exportLoading: false,
    detailVisible: false,
    detailLoading: false,
    detailError: '',
    detailTasks: [],
    detailRow: null,
    listRequestId: 0,
    detailRequestId: 0,
    $message: vi.fn(),
    downloadBlob: vi.fn(),
    ...overrides
  })
}

describe('保密区自动删权记录报表', () => {
  beforeEach(() => {
    fetchPage.mockReset()
    exportLogs.mockReset()
    fetchTasks.mockReset()
  })

  it('组合筛选会转换为分页请求，并从服务端响应更新结果', async () => {
    fetchPage.mockResolvedValue({
      data: {
        code: 0,
        data: { records: [{ id: '1' }], total: 1 }
      }
    })
    const context = createContext({
      searchForm: {
        dateRange: ['2026-09-01 00:00:00', '2026-09-05 23:59:59'],
        parkId: 1001,
        staffBadge: 'A001',
        staffName: '张三',
        department: '研发部',
        authName: '保密区权限',
        result: 'FAILED'
      },
      page: { currentPage: 2, pageSize: 50, total: 0 }
    })

    await component.methods.getList.call(context)

    expect(fetchPage).toHaveBeenCalledWith({
      current: 2,
      size: 50,
      parkId: 1001,
      startTime: '2026-09-01 00:00:00',
      endTime: '2026-09-05 23:59:59',
      staffBadge: 'A001',
      staffName: '张三',
      department: '研发部',
      authName: '保密区权限',
      result: 'FAILED'
    })
    expect(context.tableData).toStrictEqual([{ id: '1' }])
    expect(context.page.total).toBe(1)
    expect(context.tableLoading).toBe(false)
  })

  it('搜索和重置都回到第一页，重置会清空组合筛选', async () => {
    fetchPage.mockResolvedValue({ data: { code: 0, data: { records: [], total: 0 } } })
    const context = createContext({
      searchForm: {
        dateRange: ['2026-09-01 00:00:00', '2026-09-05 23:59:59'],
        parkId: 1001,
        staffBadge: 'A001',
        staffName: '张三',
        department: '研发部',
        authName: '保密区权限',
        result: 'FAILED'
      },
      page: { currentPage: 4, pageSize: 20, total: 10 }
    })

    await component.methods.handleSearch.call(context)
    expect(context.page.currentPage).toBe(1)

    await component.methods.resetSearch.call(context)
    expect(context.searchForm).toStrictEqual({
      dateRange: [],
      parkId: '',
      staffBadge: '',
      staffName: '',
      department: '',
      authName: '',
      result: ''
    })
    expect(context.page.currentPage).toBe(1)
    expect(fetchPage).toHaveBeenCalledTimes(2)
  })

  it('旧列表响应不能覆盖筛选变化后的新结果', async () => {
    let resolveOld
    let resolveNew
    fetchPage
      .mockReturnValueOnce(new Promise(resolve => { resolveOld = resolve }))
      .mockReturnValueOnce(new Promise(resolve => { resolveNew = resolve }))
    const context = createContext()

    const oldRequest = component.methods.getList.call(context)
    context.searchForm.staffName = '新筛选'
    const newRequest = component.methods.getList.call(context)

    resolveNew({ data: { code: 0, data: { records: [{ id: 'new' }], total: 1 } } })
    await newRequest
    resolveOld({ data: { code: 0, data: { records: [{ id: 'old' }], total: 1 } } })
    await oldRequest

    expect(context.tableData).toStrictEqual([{ id: 'new' }])
  })

  it('导出使用当前筛选并在请求失败时清理加载态且提示错误', async () => {
    exportLogs.mockRejectedValue(new Error('导出失败'))
    const context = createContext({
      searchForm: { dateRange: [], parkId: 1001, staffBadge: '', staffName: '', department: '', authName: '', result: 'UNKNOWN' }
    })

    await component.methods.exportReport.call(context)

    expect(exportLogs).toHaveBeenCalledWith({ parkId: 1001, result: 'UNKNOWN' })
    expect(context.exportLoading).toBe(false)
    expect(context.$message).toHaveBeenCalledWith({ message: '导出失败', type: 'error' })
  })

  it('导出接口返回业务失败时不把错误响应下载成成功文件', async () => {
    exportLogs.mockResolvedValue({ data: { code: 1, msg: '记录超过 10000 条，请缩小范围' } })
    const context = createContext()

    await component.methods.exportReport.call(context)

    expect(context.downloadBlob).not.toHaveBeenCalled()
    expect(context.$message).toHaveBeenCalledWith({ message: '记录超过 10000 条，请缩小范围', type: 'error' })
  })

  it('任务详情失败会保留明确错误状态，不把失败显示为空成功', async () => {
    fetchTasks.mockRejectedValue(new Error('任务接口失败'))
    const context = createContext()

    await component.methods.openTaskDetail.call(context, { id: 'log-1' })

    expect(fetchTasks).toHaveBeenCalledWith('log-1')
    expect(context.detailVisible).toBe(true)
    expect(context.detailLoading).toBe(false)
    expect(context.detailTasks).toStrictEqual([])
    expect(context.detailError).toBe('任务明细加载失败，请稍后重试')
  })

  it('晚到的旧任务详情不能串到当前记录', async () => {
    let resolveOld
    let resolveNew
    fetchTasks
      .mockReturnValueOnce(new Promise(resolve => { resolveOld = resolve }))
      .mockReturnValueOnce(new Promise(resolve => { resolveNew = resolve }))
    const context = createContext()

    const oldRequest = component.methods.openTaskDetail.call(context, { id: 'old' })
    const newRequest = component.methods.openTaskDetail.call(context, { id: 'new' })
    resolveNew({ data: { code: 0, data: [{ taskId: 'new-task' }] } })
    await newRequest
    resolveOld({ data: { code: 0, data: [{ taskId: 'old-task' }] } })
    await oldRequest

    expect(context.detailRow.id).toBe('new')
    expect(context.detailTasks).toStrictEqual([{ taskId: 'new-task' }])
  })

  it('按统一状态口径区分离线处理中、过期失败、成功和未知', () => {
    expect(component.methods.taskStatusLabel(6)).toBe('执行中')
    expect(component.methods.taskStatusLabel(5)).toBe('失败')
    expect(component.methods.taskStatusLabel(1)).toBe('成功（任务记录）')
    expect(component.methods.taskStatusLabel(null)).toBe('任务状态未知')
    expect(component.methods.displayValue(null)).toBe('-')
  })
})
