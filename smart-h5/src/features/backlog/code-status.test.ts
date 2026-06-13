import { describe, expect, it } from 'vitest'
import { codePanelState } from './code-status'

describe('放行条 /code 五态（旧 code.vue：expire 最高优先，其余按 status）', () => {
  it('expire 压过任何 status', () => {
    expect(codePanelState({ expire: true, status: 2 })).toBe('expired')
    expect(codePanelState({ expire: true, status: 4 })).toBe('expired')
  })

  it('status 1-5 分别映射', () => {
    expect(codePanelState({ expire: false, status: 1 })).toBe('reviewing')
    expect(codePanelState({ expire: false, status: 2 })).toBe('qr')
    expect(codePanelState({ expire: false, status: 3 })).toBe('rejected')
    expect(codePanelState({ expire: false, status: 4 })).toBe('left')
    expect(codePanelState({ expire: false, status: 5 })).toBe('denied')
  })

  it('未知 status 不显示', () => {
    expect(codePanelState({ expire: false, status: 9 })).toBe('none')
    expect(codePanelState({})).toBe('none')
  })
})
