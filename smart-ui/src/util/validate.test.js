import { describe, expect, it } from 'vitest'
import { isEmail, isMobile, isURL } from '@/util/validate'

describe('isMobile', () => {
  it('accepts 11-digit numbers starting with 1', () => {
    expect(isMobile('13800138000')).toBe(true)
    expect(isMobile(13800138000)).toBe(true)
  })

  it('rejects wrong length or prefix', () => {
    expect(isMobile('23800138000')).toBe(false)
    expect(isMobile('1380013800')).toBe(false)
    expect(isMobile('')).toBe(false)
  })
})

describe('isEmail', () => {
  it('accepts a normal address', () => {
    expect(isEmail('user-1@example.com')).toBe(true)
  })

  it('rejects an address without @', () => {
    expect(isEmail('user.example.com')).toBe(false)
  })
})

describe('isURL', () => {
  it('accepts http and https', () => {
    expect(isURL('http://10.13.21.6/YUTO')).toBe(true)
    expect(isURL('https://example.com')).toBe(true)
  })

  it('rejects other protocols', () => {
    expect(isURL('ftp://example.com')).toBe(false)
  })
})
