import { describe, expect, it } from 'vitest'
import { roomCountOptions, roomDormitoryOptions } from './room-rules'

describe('room select options', () => {
  it('保留是否参与分配和是否参与计算下拉选项的旧顺序和值', () => {
    expect(roomDormitoryOptions).toStrictEqual([
      { label: '是', value: 0 },
      { label: '否', value: 1 }
    ])
    expect(roomCountOptions).toStrictEqual([
      { label: '是', value: 1 },
      { label: '否', value: 0 }
    ])
  })
})
