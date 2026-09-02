import { describe, expect, it } from 'vitest'
import * as clientCrud from './client'

describe('客户端 capability scope 表单目录', () => {
  it('使用运行时后端目录创建多选授权域，而不是固化前端白名单', () => {
    expect(clientCrud.createTableOption).toBeTypeOf('function')
    const scopeOptions = [{ value: 'internal:energy:projection:run', label: '能耗投影-运行' }]
    const tableOption = clientCrud.createTableOption(scopeOptions)
    const scopeColumn = tableOption.column.find(column => column.prop === 'scope')

    expect(scopeColumn.multiple).toBe(true)
    expect(scopeColumn.dicData).toEqual(scopeOptions)
  })
})
