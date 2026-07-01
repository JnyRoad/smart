import { describe, it, expect } from 'vitest'
import { STEPS, runSteps, formatSummary } from './gate.mjs'

describe('STEPS', () => {
  it('包含电表管理 UI 守卫，防止后续拆分 electric_manage 时漏跑接线契约', () => {
    expect(STEPS).toContainEqual({
      name: 'electric-manage-ui',
      cmd: ['node', 'scripts/check-electric-manage-ui.js']
    })
  })
})

describe('runSteps', () => {
  it('全部步骤通过 → allPassed 为真，逐步记录结果', () => {
    const steps = [
      { name: 'a', cmd: ['echo', 'a'] },
      { name: 'b', cmd: ['echo', 'b'] }
    ]
    // 假 exec：永远成功，耗时固定，便于断言
    const exec = () => ({ ok: true, ms: 5 })
    const { results, allPassed } = runSteps(steps, exec)
    expect(allPassed).toBe(true)
    expect(results).toEqual([
      { name: 'a', ok: true, ms: 5 },
      { name: 'b', ok: true, ms: 5 }
    ])
  })

  it('某步失败 → allPassed 为假，但其余步骤仍全部执行（聚合而非快速失败）', () => {
    const steps = [
      { name: 'a', cmd: ['x'] },
      { name: 'b', cmd: ['x'] },
      { name: 'c', cmd: ['x'] }
    ]
    const ran = []
    // 假 exec：只让 b 失败，记录实际执行顺序
    const exec = (cmd, name) => {
      ran.push(name)
      return { ok: name !== 'b', ms: 1 }
    }
    const { results, allPassed } = runSteps(steps, exec)
    expect(allPassed).toBe(false)
    // 关键：b 失败后 c 仍然跑了，开发者一次看清所有问题
    expect(ran).toEqual(['a', 'b', 'c'])
    expect(results.map((r) => r.ok)).toEqual([true, false, true])
  })
})

describe('formatSummary', () => {
  it('通过项标 ✓、失败项标 ✗，并带步骤名', () => {
    const text = formatSummary([
      { name: 'unit', ok: true, ms: 1200 },
      { name: 'lint-baseline', ok: false, ms: 800 }
    ])
    expect(text).toContain('✓')
    expect(text).toContain('✗')
    expect(text).toContain('unit')
    expect(text).toContain('lint-baseline')
    // 有失败项时列出失败步骤名
    expect(text).toContain('门禁未通过')
  })

  it('全部通过 → 给出"门禁通过"结论', () => {
    const text = formatSummary([
      { name: 'unit', ok: true, ms: 100 },
      { name: 'bundle', ok: true, ms: 50 }
    ])
    expect(text).toContain('门禁通过')
    expect(text).not.toContain('✗')
  })
})
