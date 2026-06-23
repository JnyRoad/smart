// src/router/avue-router.contract.test.js
import { describe, it, expect, vi } from 'vitest'

// PlatformRouter 是"菜单之后追加"的静态自定义路由；用哨兵 mock 它，
// 以便断言它在动态菜单路由之后被 addRoutes（顺序是行为关键点）。
vi.mock('./platform/', () => ({
  default: [{ path: '/__platform_sentinel__', name: 'platform-sentinel' }]
}))

const AvueRouter = (await import('./avue-router')).default

// 构造一个可记录 addRoutes 调用的假 router 和带 website 配置的假 store
function makeFakeRouter () {
  const calls = []
  return { addRoutes: (routes) => calls.push(routes), $addRoutesCalls: calls }
}
function makeFakeStore () {
  return {
    getters: { website: { menu: { props: {} } }, tag: {} },
    commit: vi.fn()
  }
}

describe('avue-router 动态装配契约（拆分前钉死）', () => {
  it('一级菜单：path 去 /index、redirect 指向 /index、keepAlive 取反、生成 index 子路由', () => {
    const router = makeFakeRouter()
    AvueRouter.install(router, makeFakeStore())

    const menu = [
      { label: '设备', path: '/platform/device/index', icon: 'i1', keepAlive: 0, component: 'views/platform/device/gate', children: [] }
    ]
    router.$avueRouter.formatRoutes(menu, true)

    // 第 1 次 addRoutes = 动态菜单路由
    const dynamic = router.$addRoutesCalls[0]
    expect(dynamic).toHaveLength(1)
    expect(dynamic[0].path).toBe('/platform/device')
    expect(dynamic[0].redirect).toBe('/platform/device/index')
    expect(dynamic[0].name).toBe('设备')
    expect(dynamic[0].meta).toEqual({ keepAlive: true }) // Number(0)===0 → true
    expect(dynamic[0].children).toHaveLength(1)
    expect(dynamic[0].children[0].path).toBe('index')
  })

  it('PlatformRouter 必须在动态菜单路由之后追加（顺序契约）', () => {
    const router = makeFakeRouter()
    AvueRouter.install(router, makeFakeStore())
    router.$avueRouter.formatRoutes(
      [{ label: 'A', path: '/platform/a/index', icon: '', keepAlive: 1, component: 'views/platform/a/index', children: [] }],
      true
    )

    expect(router.$addRoutesCalls).toHaveLength(2)
    // 第 2 次 addRoutes 必须是 PlatformRouter 哨兵
    expect(router.$addRoutesCalls[1][0].path).toBe('/__platform_sentinel__')
  })

  it('keepAlive 非 0 时为 false', () => {
    const router = makeFakeRouter()
    AvueRouter.install(router, makeFakeStore())
    router.$avueRouter.formatRoutes(
      [{ label: 'B', path: '/platform/b/index', icon: '', keepAlive: 1, component: 'views/platform/b/index', children: [] }],
      true
    )
    expect(router.$addRoutesCalls[0][0].meta).toEqual({ keepAlive: false })
  })
})
