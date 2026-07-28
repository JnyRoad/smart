import { describe, expect, it, vi } from 'vitest'

vi.mock('@/router/axios', () => ({ default: vi.fn() }))

const api = await import('./staff_info')

describe('api/platform/basic/staff_info 员工详情安全契约', () => {
  it('不再导出按工号读取完整员工资料的请求', () => {
    expect(api.getStaffByBadge).toBeUndefined()
  })
})
