import { describe, it, expect } from 'vitest'

// 钉死 business 域各 crud 表的【非 column 顶层配置】，确保抽 _base.js 重构前后逐键一致。
// 用 toEqual（深度相等、忽略键顺序）而非快照：spread 会改变键的书写顺序，但只要键集合与值不变即等价；
// column 本次不改、不纳入比对（靠 git diff 保证整块不动）。
const omitColumn = (opt) => {
  const rest = { ...opt }
  delete rest.column
  return rest
}

// business 域 9 个文件改写前的真实顶层配置：8 个完全一致，isc_park_config 额外多一个 menuWidth:320。
// 这是 business 专属风格（border:true / stripe:false），与 admin 的 baseTableOption 相反，故不能共用。
const EXPECTED_BASE = {
  border: true,
  index: true,
  indexLabel: '序号',
  stripe: false,
  menuAlign: 'center',
  align: 'center',
  editBtn: false,
  delBtn: false,
  addBtn: false
}

// 顶层配置与 EXPECTED_BASE 完全一致的 8 个文件。
const sameAsBase = {
  attendance_config: () => import('@/const/crud/platform/business/attendance_config'),
  badge_config: () => import('@/const/crud/platform/business/badge_config'),
  dormitory_config: () => import('@/const/crud/platform/business/dormitory_config'),
  release: () => import('@/const/crud/platform/business/release'),
  repair: () => import('@/const/crud/platform/business/repair'),
  salary_config: () => import('@/const/crud/platform/business/salary_config'),
  security_area: () => import('@/const/crud/platform/business/security_area'),
  visitor_config: () => import('@/const/crud/platform/business/visitor_config')
}

describe('business crud tableOption 顶层配置契约（抽 _base 前后必须逐键一致）', () => {
  for (const [name, load] of Object.entries(sameAsBase)) {
    it(name, async () => {
      const mod = await load()
      expect(omitColumn(mod.tableOption)).toEqual(EXPECTED_BASE)
    })
  }

  it('isc_park_config（base + 特有 menuWidth:320）', async () => {
    const mod = await import('@/const/crud/platform/business/isc_park_config')
    expect(omitColumn(mod.tableOption)).toEqual({ ...EXPECTED_BASE, menuWidth: 320 })
  })
})
