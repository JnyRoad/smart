import { describe, expect, it, vi } from 'vitest'

vi.mock('@/page/index/', () => ({ default: { name: 'LayoutMock' } }))
vi.mock('@/views/platform/business/security_area/auth_delete_log/index', () => ({
  default: { name: 'SecurityAuthDeleteLogMock' }
}))

const { businessSecurityAreaEditRoute } = await import('@/router/platform/business')

describe('保密区自动删权报表本地路由', () => {
  it('导出父路由、子路径及懒加载组件均指向报表入口', async () => {
    const childRoute = businessSecurityAreaEditRoute.children.find(child => child.path === 'auth_delete_log/index')

    expect(businessSecurityAreaEditRoute.path).toBe('/platform/business/security_area')
    expect(childRoute).toBeDefined()
    expect(childRoute.component).toBeTypeOf('function')

    const loadedComponent = await childRoute.component()
    expect(loadedComponent.default).toEqual({ name: 'SecurityAuthDeleteLogMock' })
  })
})
