import { describe, expect, it } from 'vitest'
import { validateVisitorName, VISITOR_NAME_MESSAGE } from './visitor-name'

describe('validateVisitorName（汉字/英文/数字/下划线，1-30）', () => {
  it('合法：汉字、英文、数字、下划线及其组合', () => {
    for (const ok of ['王五', 'John', 'abc_123', '张三Wang_9', 'a', 'x'.repeat(30), '王'.repeat(30)]) {
      expect(validateVisitorName(ok).ok).toBe(true)
    }
  })

  it('边界：基本块汉字放行（一/龥），扩展区生僻字拦截（龦/㐀/𠀀）', () => {
    expect(validateVisitorName('一龥').ok).toBe(true)
    for (const ext of ['龦', '㐀', '𠀀']) {
      expect(validateVisitorName(ext).ok).toBe(false)
    }
  })

  it('非法：空、超 30、含空格或特殊字符', () => {
    for (const bad of ['', ' ', '张 三', '王五!', 'a-b', '李四@', 'x'.repeat(31), '😀']) {
      const r = validateVisitorName(bad)
      expect(r.ok).toBe(false)
      expect(r.message).toBe(VISITOR_NAME_MESSAGE)
    }
  })
})
