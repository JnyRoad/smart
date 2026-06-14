import { describe, expect, it } from 'vitest'
import { resolveLockRefreshFacePic } from './lock-refresh'

describe('resolveLockRefreshFacePic', () => {
  it('falls back to the uploaded base64 when checkFace only returns a photoId', () => {
    expect(resolveLockRefreshFacePic({ code: 0, data: { photoId: 'p-1' } }, 'cut-face-base64')).toBe(
      'cut-face-base64',
    )
  })

  it('uses the uploaded base64 even when a gateway variant also provides resultData.base64', () => {
    expect(
      resolveLockRefreshFacePic({ code: 0, data: { photoId: 'p-1' }, resultData: { base64: 'server-face-base64' } }, 'cut-face-base64'),
    ).toBe('cut-face-base64')
  })
})
