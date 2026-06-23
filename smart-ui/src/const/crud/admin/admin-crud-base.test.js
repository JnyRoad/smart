import { describe, it, expect, vi } from 'vitest'

// 部分 crud 文件经 @/api 间接 import @/router/axios，stub 掉以隔离网络/路由依赖。
vi.mock('@/router/axios', () => ({ default: () => Promise.resolve({ data: {} }) }))

// 钉死各 admin crud 表的【非 column 顶层配置】，确保抽 _base.js 重构前后完全一致。
// column 部分本次不改、原样保留，故不纳入比对。
const omitColumn = (opt) => {
  const rest = { ...opt }
  delete rest.column
  return rest
}

const crudFiles = {
  client: () => import('@/const/crud/admin/client'),
  dict: () => import('@/const/crud/admin/dict'),
  log: () => import('@/const/crud/admin/log'),
  role: () => import('@/const/crud/admin/role'),
  'sys-social-details': () => import('@/const/crud/admin/sys-social-details'),
  token: () => import('@/const/crud/admin/token'),
  user: () => import('@/const/crud/admin/user')
}

describe('admin crud tableOption 顶层配置契约（抽 _base 前后必须一致）', () => {
  for (const [name, load] of Object.entries(crudFiles)) {
    it(name, async () => {
      const mod = await load()
      expect(omitColumn(mod.tableOption)).toMatchSnapshot()
    })
  }
})
