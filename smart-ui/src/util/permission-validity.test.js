import { describe, expect, it } from 'vitest'
import {
  buildPermissionDatePayload,
  getDefaultPermissionDateRange,
  isPermissionDateRangeValid
} from './permission-validity'

describe('permission validity', () => {
  it('defaults a manual permission window to today through 2030-12-31', () => {
    expect(getDefaultPermissionDateRange(new Date(2026, 8, 3))).toEqual(['2026-09-03', '2030-12-31'])
  })

  it('builds the API payload from a legal inclusive date range', () => {
    expect(buildPermissionDatePayload(['2026-09-03', '2026-09-05'])).toEqual({
      startTime: '2026-09-03',
      endTime: '2026-09-05'
    })
  })

  it('rejects an inverted date range before an API call can be made', () => {
    expect(isPermissionDateRangeValid(['2026-09-05', '2026-09-03'])).toBe(false)
  })
})
