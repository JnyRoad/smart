import { describe, expect, it } from 'vitest'
import { buildIdCardFellow, validateIdCard } from './id-card'

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

// 99开头的结构合成号码只用于校验位与请求契约测试。
describe('buildIdCardFellow', () => {
  it('为主访客和陪同人员明确提交身份证类型0并保留逐人资料', () => {
    expect(buildIdCardFellow({ fellowName: '合成 甲', fellowPhotoId: 'photo-a', certNo: ' 990000200001010012 ' }, 1))
      .toEqual({ isMain: 1, fellowName: '合成甲', fellowPhotoId: 'photo-a', certNo: '990000200001010012', certType: 0 })
    expect(buildIdCardFellow({ fellowName: '合成乙', fellowPhotoId: 'photo-b', certNo: '990000200001010012' }, 0))
      .toEqual({ isMain: 0, fellowName: '合成乙', fellowPhotoId: 'photo-b', certNo: '990000200001010012', certType: 0 })
  })
  it('不把未识别证件或校验位错误号码默认成身份证', () => {
    for (const certNo of ['synthetic-passport', '990000200001010011']) {
      expect(() => buildIdCardFellow({ fellowName: '合成甲', fellowPhotoId: '', certNo }, 1)).toThrow()
    }
  })
})
