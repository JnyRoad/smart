import { describe, expect, it } from 'vitest'
import { validateIdCard } from './id-card'

describe('validateIdCard', () => {
  it('接受合法 18 位身份证（含 X 校验位）', () => {
    expect(validateIdCard('11010519491231002X').ok).toBe(true)
    expect(validateIdCard('110105199001011005').ok).toBe(true)
  })

  it('拒绝格式错误', () => {
    expect(validateIdCard('123')).toEqual({ ok: false, message: '证件号码格式不正确' })
    expect(validateIdCard('')).toEqual({ ok: false, message: '证件号码格式不正确' })
    expect(validateIdCard('11010519491231002a').ok).toBe(false)
  })

  it('拒绝校验位错误并给出原因', () => {
    expect(validateIdCard('110105194912310021')).toEqual({
      ok: false,
      message: '证件号码校验位不正确',
    })
  })

  it('接受小写 x 校验位', () => {
    expect(validateIdCard('11010519491231002x').ok).toBe(true)
  })
})
