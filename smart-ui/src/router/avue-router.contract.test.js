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

  // 多级菜单（children 非空）走 formatRoutes 的递归分支（first=false），是拆路由最易错的一段，必须钉死。
  it('多级菜单：父级 redirect 为空、递归生成子路由且子级 path 不做 /index 剥离', () => {
    const router = makeFakeRouter()
    AvueRouter.install(router, makeFakeStore())

    const menu = [
      {
        label: '父级',
        path: '/platform/parent',
        icon: '',
        keepAlive: 1,
        component: 'views/platform/parent/index',
        children: [
          { label: '子级', path: '/platform/parent/child', icon: '', keepAlive: 1, component: 'views/platform/parent/child/index', children: [] }
        ]
      }
    ]
    router.$avueRouter.formatRoutes(menu, true)

    const parent = router.$addRoutesCalls[0][0]
    expect(parent.path).toBe('/platform/parent')
    expect(parent.redirect).toBe('') // isChild 分支：父级不生成 redirect
    expect(typeof parent.component).toBe('function') // first=true → loadPage 懒加载函数

    // 递归（first=false）生成的子路由
    expect(parent.children).toHaveLength(1)
    const child = parent.children[0]
    expect(child.path).toBe('/platform/parent/child') // first=false：path 原样，不剥 /index
    expect(child.name).toBe('子级')
    expect(typeof child.component).toBe('function') // 叶子节点 → loadMenuComponent 懒加载函数
    expect(child.children).toHaveLength(0)
  })
})
