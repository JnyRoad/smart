import { describe, it, expect } from 'vitest'
import { diffAgainstBaseline } from './check-lint-baseline.mjs'

describe('diffAgainstBaseline', () => {
  it('文件 warning 数未增 → 无违规', () => {
    const baseline = { 'src/a.js': 3 }
    const current = { 'src/a.js': 3 }
    expect(diffAgainstBaseline(baseline, current)).toEqual([])
  })

  it('文件 warning 数下降 → 无违规（允许变好）', () => {
    expect(diffAgainstBaseline({ 'src/a.js': 3 }, { 'src/a.js': 1 })).toEqual([])
  })

  it('文件 warning 数上升 → 报违规', () => {
    const v = diffAgainstBaseline({ 'src/a.js': 3 }, { 'src/a.js': 5 })
    expect(v).toEqual([{ file: 'src/a.js', baseline: 3, current: 5 }])
  })

  it('新文件带 warning → 报违规（基线视为 0）', () => {
    const v = diffAgainstBaseline({}, { 'src/new.js': 2 })
    expect(v).toEqual([{ file: 'src/new.js', baseline: 0, current: 2 }])
  })
})
