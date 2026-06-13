import { describe, expect, it } from 'vitest'
import { formatReturnFactorySearchDateTime } from './return-factory-search'

describe('formatReturnFactorySearchDateTime', () => {
  it('formats return-factory search times with seconds for backend parsing', () => {
    expect(formatReturnFactorySearchDateTime(new Date(2026, 0, 2, 3, 4, 0))).toBe(
      '2026-01-02 03:04:00',
    )
  })
})
