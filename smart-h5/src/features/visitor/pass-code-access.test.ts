import { describe, expect, it } from 'vitest'
import { visitorPassCodePath, visitorRecordAccessPath } from './pass-code-access'

describe('visitor pass-code access', () => {
  it('keeps the pass-code route separate from records while its API requires a query token', () => {
    expect(visitorPassCodePath('1001')).toBe('/visitor/code?id=1001')
  })

  it('does not create a redirect target for an empty application id', () => {
    expect(visitorPassCodePath('')).toBeNull()
  })

  it('routes a tokenless pass-code request to the SMS verification flow', () => {
    expect(visitorRecordAccessPath('1001')).toBe('/visitor/records?redirect=1001')
  })
})
