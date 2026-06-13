import { describe, expect, it } from 'vitest'
import { stripSpaces } from './text'

describe('stripSpaces', () => {
  it('去掉首尾与中间的半角空格', () => {
    expect(stripSpaces('  王 五  ')).toBe('王五')
    expect(stripSpaces('1101 0219 9001102030x')).toBe('110102199001102030x')
  })

  it('去掉全角空格、制表符、换行', () => {
    expect(stripSpaces('张　三')).toBe('张三')
    expect(stripSpaces('\t豫A\n12345 ')).toBe('豫A12345')
  })

  it('无空格原样返回', () => {
    expect(stripSpaces('测试')).toBe('测试')
    expect(stripSpaces('')).toBe('')
  })
})
