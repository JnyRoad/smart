import { describe, it, expect } from 'vitest'
import { dateFormat2 } from '@/util/date'
import { registerRowClassName, formatCheckInDate, toExportRows } from './bed-rules'

describe('registerRowClassName', () => {
  it('登记状态(status=-1)的行返回高亮类名 isRegister', () => {
    expect(registerRowClassName({ status: -1 })).toBe('isRegister')
  })

  it('非登记状态不加类名（返回 undefined，保持原行为）', () => {
    expect(registerRowClassName({ status: 0 })).toBeUndefined()
    expect(registerRowClassName({ status: 1 })).toBeUndefined()
  })
})

describe('formatCheckInDate', () => {
  it('空值不显示（沿用 validatenull 判定）', () => {
    expect(formatCheckInDate('')).toBeUndefined()
    expect(formatCheckInDate(null)).toBeUndefined()
    expect(formatCheckInDate(undefined)).toBeUndefined()
  })

  it('非空值委托给 dateFormat2 格式化（行为与原 dateFormat 一致）', () => {
    const val = '2024-03-05 12:00:00'
    expect(formatCheckInDate(val)).toBe(dateFormat2(new Date(val)))
  })
})

describe('toExportRows', () => {
  it('按列字段顺序把对象数组映射成二维数组（原 formatJson）', () => {
    const rows = [
      { name: '张三', badge: 'A1' },
      { name: '李四', badge: 'B2' }
    ]
    expect(toExportRows(['badge', 'name'], rows)).toEqual([
      ['A1', '张三'],
      ['B2', '李四']
    ])
  })

  it('空数据返回空数组', () => {
    expect(toExportRows(['name'], [])).toEqual([])
  })
})
