// src/router/avue-router.fallback.test.js
// 菜单组件动态加载失败的快速失败行为：
// 生产事故背景——sys_menu 里存在指向 smart-ui 不存在组件（views/app/*，属老前端 smart-app-ui）
// 的菜单，点击后动态 import reject、导航静默中止、NProgress 无限转圈。
// 本测试锁定：import 失败必须 resolve 成 404 错误页组件并上报，而不是无限悬挂。
import { describe, it, expect, vi, beforeEach } from 'vitest'

// PlatformRouter 与契约测试同样用哨兵 mock，避免拉起真实业务路由依赖
vi.mock('./platform/', () => ({
  default: [{ path: '/__platform_sentinel__', name: 'platform-sentinel' }]
}))

// error-reporter 会引入 store 依赖链，单测中用假实现隔离并记录调用
vi.mock('../error-reporter', () => ({
  reportCaughtError: vi.fn()
}))

const { reportCaughtError } = await import('../error-reporter')
const { loadMenuComponent } = await import('./avue-router')

beforeEach(() => {
  vi.clearAllMocks()
})

describe('loadMenuComponent 加载失败兜底', () => {
  it('组件不存在时 resolve 为 404 错误页组件，而不是 reject 悬挂', async () => {
    // 复现生产数据：sys_menu 300301 的 component 指向 smart-ui 里不存在的路径
    const loader = loadMenuComponent('views/app/app_set/set/index')
    const fallbackModule = await loader()
    const errorPage404 = await import('../components/error-page/404.vue')
    expect(fallbackModule.default).toBe(errorPage404.default)
  })

  it('组件不存在时通过 error-reporter 上报，信息包含组件路径', async () => {
    await loadMenuComponent('views/app/app_set/set/index')()
    expect(reportCaughtError).toHaveBeenCalledTimes(1)
    const [error, info] = reportCaughtError.mock.calls[0]
    expect(error).toBeTruthy()
    expect(info).toContain('views/app/app_set/set/index')
  })

  it('组件存在时正常返回该组件，不触发上报', async () => {
    // 注意：Vite 的变量动态 import 只解析一层深度（"one level deep"），深层路径在
    // vitest 里必然 reject；生产 webpack context 是递归的，深层路径正常。
    // 因此“存在的组件”用例只能用 src 顶层的 App.vue（无依赖、最轻量）。
    const loader = loadMenuComponent('App')
    const module_ = await loader()
    const appModule = await import('../App.vue')
    expect(module_.default).toBe(appModule.default)
    expect(reportCaughtError).not.toHaveBeenCalled()
  })
})
