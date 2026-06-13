import { describe, expect, it } from 'vitest'
import { extractToken } from './dev-token'

describe('extractToken（开发调试登录的粘贴容错）', () => {
  it('原始 token 原样返回（含首尾空白裁剪）', () => {
    expect(extractToken('  abc-token-123  ')).toBe('abc-token-123')
  })

  it('xc 信封 JSON 取 content', () => {
    expect(
      extractToken('{"dataType":"string","content":"tok-x","datetime":1718160000000}'),
    ).toBe('tok-x')
  })

  it('整段文本中嵌着信封也能提取', () => {
    expect(
      extractToken('xc-access_token\t{"dataType":"string","content":"tok-y","datetime":1}'),
    ).toBe('tok-y')
  })

  it('空输入返回 null', () => {
    expect(extractToken('')).toBeNull()
    expect(extractToken('   ')).toBeNull()
  })
})
