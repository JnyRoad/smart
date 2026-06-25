import { describe, it, expect } from 'vitest'
import {
  buildPlatformRouterFingerprintFromSource,
  diffFingerprint
} from './check-platform-router-fingerprint.mjs'

const sampleRoutes = `
import Layout from '@/page/index/'
import detailLayout from '@/views/platform/detail/index'

export default [
  {
    path: '/platform/a',
    component: Layout,
    redirect: '/platform/a/index',
    hidden: true,
    children: [{
      path: 'index',
      name: 'AIndex',
      meta: { keepAlive: true, title: 'A' },
      component: () => import('@/views/platform/a/index')
    }]
  },
  {
    path: '/platform/device',
    component: Layout,
    children: []
  },
  {
    path: '/platform/device',
    component: Layout,
    children: [{
      path: 'detail/:id',
      component: detailLayout
    }]
  }
]
`

describe('buildPlatformRouterFingerprintFromSource', () => {
  it('保留顶层路由顺序，并记录重复 path 的出现位置', () => {
    const fingerprint = buildPlatformRouterFingerprintFromSource(sampleRoutes)

    expect(fingerprint.topLevelRouteCount).toBe(3)
    expect(fingerprint.routes.map((route) => route.path)).toEqual([
      '/platform/a',
      '/platform/device',
      '/platform/device'
    ])
    expect(fingerprint.duplicateTopLevelPaths).toEqual({
      '/platform/device': [1, 2]
    })
  })

  it('递归保留 children 顺序、meta 静态字段和 component 目标', () => {
    const fingerprint = buildPlatformRouterFingerprintFromSource(sampleRoutes)

    expect(fingerprint.routes[0].children).toEqual({
      type: 'array',
      routes: [
        expect.objectContaining({
          path: 'index',
          name: 'AIndex',
          meta: { keepAlive: true, title: 'A' },
          component: {
            type: 'lazyImport',
            source: '@/views/platform/a/index'
          }
        })
      ]
    })
    expect(fingerprint.routes[2].children.routes[0].component).toEqual({
      type: 'identifier',
      name: 'detailLayout',
      importSource: '@/views/platform/detail/index'
    })
    expect(fingerprint.routes[0].extra).toEqual({
      hidden: true
    })
  })

  it('支持相对导入的路由数组展开，保证后续按域拆分后仍比较真实 route 顺序', () => {
    const source = `
import Layout from '@/page/index/'
import deviceRoutes from './device'
import dormitoryRoutes from './dormitory/index'

export default [
  ...deviceRoutes,
  ...dormitoryRoutes
]
`
    const modules = {
      'src/router/platform/device.js': `
import Layout from '@/page/index/'
export default [{
  path: '/platform/device',
  component: Layout,
  children: [{ path: 'index', component: () => import('@/views/platform/device/index') }]
}]
`,
      'src/router/platform/dormitory/index.js': `
export default [{
  path: '/platform/dormitory',
  component: () => import('@/views/platform/dormitory/index')
}]
`
    }

    const fingerprint = buildPlatformRouterFingerprintFromSource(source, 'src/router/platform/index.js', {
      readModule: (relativePath) => modules[relativePath] || null
    })

    expect(fingerprint.routes.map((route) => route.path)).toEqual([
      '/platform/device',
      '/platform/dormitory'
    ])
    expect(fingerprint.routes[0].children.routes[0].component).toEqual({
      type: 'lazyImport',
      source: '@/views/platform/device/index'
    })
  })
})

describe('diffFingerprint', () => {
  it('字段或顺序变化时输出可定位差异', () => {
    const baseline = {
      version: 1,
      routes: [
        {
          path: '/platform/a',
          children: { type: 'array', routes: [{ path: 'index' }] }
        }
      ]
    }
    const current = {
      version: 1,
      routes: [
        {
          path: '/platform/a',
          children: { type: 'array', routes: [{ path: 'detail' }] }
        }
      ]
    }

    expect(diffFingerprint(baseline, current)).toEqual([
      'routes[0].children.routes[0].path expected "index" but got "detail"'
    ])
  })

  it('顶层路由换序时输出每个位置的 path 差异', () => {
    expect(diffFingerprint(
      { routes: [{ path: '/platform/a' }, { path: '/platform/b' }] },
      { routes: [{ path: '/platform/b' }, { path: '/platform/a' }] }
    )).toEqual([
      'routes[0].path expected "/platform/a" but got "/platform/b"',
      'routes[1].path expected "/platform/b" but got "/platform/a"'
    ])
  })

  it('顶层路由新增或删除时输出数组长度差异', () => {
    expect(diffFingerprint(
      { routes: [{ path: '/platform/a' }] },
      { routes: [{ path: '/platform/a' }, { path: '/platform/b' }] }
    )).toContain('routes length expected 1 but got 2')
  })

  it('redirect、name、meta、component 变化时都输出差异', () => {
    expect(diffFingerprint(
      {
        routes: [{
          path: '/platform/a',
          redirect: '/platform/a/index',
          name: 'A',
          meta: { keepAlive: true },
          component: { type: 'lazyImport', source: '@/views/a' }
        }]
      },
      {
        routes: [{
          path: '/platform/a',
          redirect: '/platform/a/detail',
          name: 'ADetail',
          meta: { keepAlive: false },
          component: { type: 'lazyImport', source: '@/views/a-detail' }
        }]
      }
    )).toEqual([
      'routes[0].component.source expected "@/views/a" but got "@/views/a-detail"',
      'routes[0].meta.keepAlive expected true but got false',
      'routes[0].name expected "A" but got "ADetail"',
      'routes[0].redirect expected "/platform/a/index" but got "/platform/a/detail"'
    ])
  })

  it('重复顶层 path 出现或消失时输出差异', () => {
    expect(diffFingerprint(
      { duplicateTopLevelPaths: {} },
      { duplicateTopLevelPaths: { '/platform/device': [1, 2] } }
    )).toEqual([
      'duplicateTopLevelPaths./platform/device expected undefined but got [1,2]'
    ])
  })

  it('额外静态字段变化时输出差异', () => {
    expect(diffFingerprint(
      { routes: [{ path: '/platform/a', extra: { hidden: true } }] },
      { routes: [{ path: '/platform/a', extra: { hidden: false } }] }
    )).toEqual([
      'routes[0].extra.hidden expected true but got false'
    ])
  })
})
