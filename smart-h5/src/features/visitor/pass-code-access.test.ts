import { describe, expect, it } from 'vitest'
import { visitorRecordAccessPath } from './pass-code-access'

describe('visitor pass-code access', () => {
  it('routes a legacy application id into the SMS-query-token protected record flow', () => {
    expect(visitorRecordAccessPath('1001')).toBe('/visitor/records?redirect=1001')
  })

  it('does not create a redirect target for an empty application id', () => {
    expect(visitorRecordAccessPath('')).toBeNull()
  })
})
