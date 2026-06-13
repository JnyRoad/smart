import { describe, expect, it } from 'vitest'
import { isHttpUrl } from './url'

describe('isHttpUrl (scheme whitelist for backend-provided URLs)', () => {
  it('accepts http and https URLs', () => {
    expect(isHttpUrl('https://example.com/doc.pdf')).toBe(true)
    expect(isHttpUrl('http://example.com')).toBe(true)
  })

  it('accepts relative paths (resolved against the app origin)', () => {
    expect(isHttpUrl('/file/preview/123.pdf')).toBe(true)
  })

  it('rejects javascript: and other dangerous schemes', () => {
    expect(isHttpUrl('javascript:alert(1)')).toBe(false)
    expect(isHttpUrl('data:text/html,<script>alert(1)</script>')).toBe(false)
    expect(isHttpUrl('vbscript:msgbox')).toBe(false)
  })

  it('rejects empty and missing values', () => {
    expect(isHttpUrl('')).toBe(false)
    expect(isHttpUrl(null)).toBe(false)
    expect(isHttpUrl(undefined)).toBe(false)
  })
})
