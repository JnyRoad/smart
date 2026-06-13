import { describe, expect, it, vi } from 'vitest'

// util.js imports '@/router/axios' at module level, which drags in the router,
// vuex store and element-ui. Stub it out - these tests only cover pure functions.
vi.mock('@/router/axios', () => ({ default: {} }))

const { deepClone, dateFormat, floatNumMinus, getProportion } = await import('./util')

describe('deepClone', () => {
  it('clones nested objects and arrays without sharing references', () => {
    const source = { name: '张三', tags: ['a', 'b'], child: { age: 3 } }
    const clone = deepClone(source)

    expect(clone).toStrictEqual(source)
    expect(clone).not.toBe(source)
    expect(clone.tags).not.toBe(source.tags)
    expect(clone.child).not.toBe(source.child)

    clone.child.age = 99
    clone.tags.push('c')
    expect(source.child.age).toBe(3)
    expect(source.tags).toStrictEqual(['a', 'b'])
  })

  it('returns primitives, null and undefined as-is', () => {
    expect(deepClone(null)).toBeNull()
    expect(deepClone(undefined)).toBeUndefined()
    expect(deepClone(0)).toBe(0)
    expect(deepClone(-1.5)).toBe(-1.5)
    expect(deepClone('')).toBe('')
    expect(deepClone(false)).toBe(false)
  })

  it('returns Date instances by reference, not cloned (known limitation)', () => {
    const date = new Date(2026, 0, 1)
    const source = { createdAt: date }
    const clone = deepClone(source)
    expect(clone.createdAt).toBe(date)
  })

  it('clones an empty object and an empty array', () => {
    expect(deepClone({})).toStrictEqual({})
    expect(deepClone([])).toStrictEqual([])
  })
})

describe('dateFormat', () => {
  const date = new Date(2026, 0, 5, 9, 8, 7, 45)

  it('formats with the default yyyy-MM-dd pattern', () => {
    expect(dateFormat(date)).toBe('2026-01-05')
  })

  it('zero-pads multi-letter date and time tokens', () => {
    expect(dateFormat(date, 'yyyy-MM-dd hh:mm:ss')).toBe('2026-01-05 09:08:07')
  })

  it('leaves single-letter tokens unpadded', () => {
    expect(dateFormat(date, 'yy-M-d h:m:s')).toBe('26-1-5 9:8:7')
  })

  it('supports quarter and millisecond tokens', () => {
    expect(dateFormat(date, 'yyyy-q')).toBe('2026-1')
    expect(dateFormat(new Date(2026, 11, 31), 'q')).toBe('4')
    expect(dateFormat(date, 'S')).toBe('45')
  })

  it('throws on non-Date input instead of returning garbage', () => {
    expect(() => dateFormat(null)).toThrow(TypeError)
    expect(() => dateFormat('2026-01-05')).toThrow(TypeError)
  })
})

describe('floatNumMinus', () => {
  it('fixes binary float drift and returns a string', () => {
    // plain JS: 0.3 - 0.1 === 0.19999999999999998
    expect(floatNumMinus(0.3, 0.1)).toBe('0.2')
    expect(floatNumMinus(1.5, 1.2)).toBe('0.3')
  })

  it('keeps the longer operand precision in the result', () => {
    expect(floatNumMinus(1.25, 0.1)).toBe('1.15')
    expect(floatNumMinus(2, 0.05)).toBe('1.95')
  })

  it('handles integers, zero and negative results', () => {
    expect(floatNumMinus(5, 3)).toBe('2')
    expect(floatNumMinus(0, 0)).toBe('0')
    expect(floatNumMinus(0.1, 0.3)).toBe('-0.2')
    expect(floatNumMinus(-0.1, -0.2)).toBe('0.1')
  })

  it('treats null as 0 instead of throwing (known behavior)', () => {
    expect(floatNumMinus(null, 1)).toBe('-1')
    expect(floatNumMinus(1, null)).toBe('1')
  })
})

describe('getProportion', () => {
  it('returns a percentage rounded to two decimals', () => {
    expect(getProportion(50, 200)).toBe(25)
    expect(getProportion(1, 3)).toBe(33.33)
    expect(getProportion(2, 3)).toBe(66.67)
  })

  it('returns 0 for a zero numerator', () => {
    expect(getProportion(0, 100)).toBe(0)
  })

  it('falls back to sum=1 when sum is 0 or negative (known behavior)', () => {
    expect(getProportion(5, 0)).toBe(500)
    expect(getProportion(5, -10)).toBe(500)
  })

  it('keeps the sign of a negative numerator', () => {
    expect(getProportion(-5, 10)).toBe(-50)
  })
})
