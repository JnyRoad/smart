import { describe, expect, it, vi } from 'vitest'
import { visIentity } from './formRules'

describe('visIentity', () => {
  it('accepts a valid Chinese resident identity number', () => {
    const callback = vi.fn()
    const rule = visIentity()

    expect(() => rule.validator({}, '11010519491231002X', callback)).not.toThrow()

    expect(callback).toHaveBeenCalledTimes(1)
    expect(callback).toHaveBeenCalledWith()
  })

  it('reports an invalid Chinese resident identity number', () => {
    const callback = vi.fn()
    const rule = visIentity()

    expect(() => rule.validator({}, '110105194912310021', callback)).not.toThrow()

    expect(callback.mock.calls[0][0]).toBeInstanceOf(Error)
  })
})
