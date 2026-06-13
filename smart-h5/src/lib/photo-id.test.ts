import { describe, expect, it } from 'vitest'
import { extractPhotoId, photoViewUrl } from './photo-id'

describe('extractPhotoId（checkFace 响应 data.photoId 取值）', () => {
  it('嵌套对象取 photoId（旧版真实形状）', () => {
    expect(extractPhotoId({ photoId: 'p123' })).toBe('p123')
    expect(extractPhotoId({ photoId: 456 })).toBe('456')
  })

  it('直接字符串/数字原样', () => {
    expect(extractPhotoId('p9')).toBe('p9')
    expect(extractPhotoId(9)).toBe('9')
  })

  it('空/缺失/对象无 photoId 返回空串', () => {
    expect(extractPhotoId(undefined)).toBe('')
    expect(extractPhotoId(null)).toBe('')
    expect(extractPhotoId('')).toBe('')
    expect(extractPhotoId({})).toBe('')
    expect(extractPhotoId({ photoId: null })).toBe('')
  })
})

describe('photoViewUrl', () => {
  it('拼网关图片地址', () => {
    expect(photoViewUrl('p1')).toBe('/platform/image/view/p1')
  })
})
