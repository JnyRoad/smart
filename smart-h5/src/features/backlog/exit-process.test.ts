import { describe, expect, it } from 'vitest'
import { exitProcessNodes } from './exit-process'

describe('退宿审批时间线归一化', () => {
  it('processRecord 优先（审批侧字段名）', () => {
    expect(
      exitProcessNodes({
        processRecord: [{ statusName: '主管审批' }],
        approvalProcess: [{ statusName: '不应取到' }],
      }),
    ).toEqual([{ statusName: '主管审批' }])
  })

  it('缺 processRecord 回退 approvalProcess', () => {
    expect(exitProcessNodes({ approvalProcess: [{ statusName: '宿管审批' }] })).toEqual([
      { statusName: '宿管审批' },
    ])
  })

  it('两者皆缺返回空数组', () => {
    expect(exitProcessNodes({})).toEqual([])
  })
})
