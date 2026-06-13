import { describe, expect, it } from 'vitest'
import { repairDetailPhotos } from './repair-detail'

describe('repair detail helpers', () => {
  it('优先兼容旧详情返回 imgs，并支持当前 faultImgs mock', () => {
    expect(repairDetailPhotos({ imgs: ['old-1', '', 'old-3'], faultImgs: ['new-1'] })).toEqual([
      'old-1',
      'old-3',
    ])
    expect(repairDetailPhotos({ faultImgs: ['new-1', undefined, 'new-3'] })).toEqual(['new-1', 'new-3'])
    expect(repairDetailPhotos({ imgs: [null, ''], faultImgs: ['new-1'] })).toEqual(['new-1'])
    expect(repairDetailPhotos({})).toEqual([])
  })
})
