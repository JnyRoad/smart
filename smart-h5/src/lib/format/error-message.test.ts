import { describe, expect, it } from 'vitest'
import { ApiError } from '@/lib/api/http'
import { toUserMessage } from './error-message'

describe('toUserMessage (只把后端业务提示透出给用户)', () => {
  it('ApiError 携带的是后端业务提示，原样返回', () => {
    expect(toUserMessage(new ApiError('该访客已登记'), '提交失败')).toBe('该访客已登记')
  })

  it('普通 Error 可能是技术异常，统一返回兜底文案', () => {
    expect(toUserMessage(new Error('Cannot read properties of undefined'), '提交失败')).toBe(
      '提交失败',
    )
  })

  it('TypeError 等内置异常同样不外泄，返回兜底文案', () => {
    expect(toUserMessage(new TypeError('x is not a function'), '网络错误')).toBe('网络错误')
  })

  it('字符串异常返回兜底文案', () => {
    expect(toUserMessage('boom', '提交失败')).toBe('提交失败')
  })

  it('null / undefined 返回兜底文案', () => {
    expect(toUserMessage(null, '提交失败')).toBe('提交失败')
    expect(toUserMessage(undefined, '提交失败')).toBe('提交失败')
  })
})
