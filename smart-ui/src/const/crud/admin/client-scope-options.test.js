import { describe, expect, it } from 'vitest'
import * as clientCrud from './client'

describe('客户端 capability scope 表单目录', () => {
  it('使用运行时后端目录创建多选授权域，而不是固化前端白名单', () => {
    expect(clientCrud.createTableOption).toBeTypeOf('function')
    const scopeOptions = [{ value: 'server', label: '通用服务', deprecated: false }]
    const tableOption = clientCrud.createTableOption(scopeOptions)
    const scopeColumn = tableOption.column.find(column => column.prop === 'scope')

    expect(scopeColumn.multiple).toBe(true)
    expect(scopeColumn.dicData).toEqual(scopeOptions)
  })

  it('重新打开本地已保存的行时，将数组 scope 规范为表单可用值', () => {
    expect(clientCrud.normalizeScopeFormValue).toBeTypeOf('function')

    expect(clientCrud.normalizeScopeFormValue([
      ' server ',
      '',
      ' internal:energy:projection:run '
    ])).toEqual([
      'server',
      'internal:energy:projection:run'
    ])
  })

  it('编辑存量客户端时将已废弃的细分 scope 设为不可选但保留显示', () => {
    expect(clientCrud.mergeEditableScopeOptions).toBeTypeOf('function')
    const options = clientCrud.mergeEditableScopeOptions([
      { value: 'server', label: '通用服务', deprecated: false },
      { value: 'open:admittance:photo:read', label: '入厂申请照片-读取', deprecated: true },
      { value: 'internal:energy:projection:run', label: '能耗投影-运行', deprecated: true }
    ], ['server', 'internal:energy:projection:run', 'legacy:retained'])

    expect(options).toEqual([
      { value: 'server', label: '通用服务', deprecated: false, disabled: false },
      { value: 'internal:energy:projection:run', label: '能耗投影-运行', deprecated: true, disabled: true },
      {
        value: 'legacy:retained',
        label: '历史授权域（仅保留）：legacy:retained',
        deprecated: true,
        disabled: true
      }
    ])
  })
})
