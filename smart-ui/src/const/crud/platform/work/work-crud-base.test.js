import { describe, it, expect } from 'vitest'
import { baseTableOption } from './_base'

// 钉死 work 域各 crud 表的【非 column 顶层配置】，确保抽 _base.js 重构前后逐键一致。
// column 本次不改、不纳入比对，避免把列配置、formatter、slot 等业务细节混进结构去重 PR。
const omitColumn = (opt) => {
  const rest = { ...opt }
  delete rest.column
  return rest
}

// work 域 8 个文件当前真实顶层配置完全一致；这里只抽这些纯静态配置键。
const EXPECTED_WORK_BASE = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menuAlign: 'center',
  menuWidth: 150,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  selection: false,
  props: {
    label: 'label',
    value: 'value'
  }
}

const crudFiles = {
  ask_leave: () => import('@/const/crud/platform/work/ask_leave'),
  attendance: () => import('@/const/crud/platform/work/attendance'),
  break_off: () => import('@/const/crud/platform/work/break_off'),
  checkedOut: () => import('@/const/crud/platform/work/checkedOut'),
  leaveApplication: () => import('@/const/crud/platform/work/leaveApplication'),
  out_dormitory: () => import('@/const/crud/platform/work/out_dormitory'),
  over_time: () => import('@/const/crud/platform/work/over_time'),
  to_staff: () => import('@/const/crud/platform/work/to_staff')
}

describe('work crud tableOption 顶层配置契约（抽 _base 前后必须逐键一致）', () => {
  it('_base 导出的公共配置与当前 work 域真实基线一致', () => {
    expect(baseTableOption).toEqual(EXPECTED_WORK_BASE)
  })

  for (const [name, load] of Object.entries(crudFiles)) {
    it(name, async () => {
      const mod = await load()
      expect(omitColumn(mod.tableOption)).toEqual(EXPECTED_WORK_BASE)
    })
  }
})
