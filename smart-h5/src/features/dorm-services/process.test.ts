import { describe, expect, it } from 'vitest'
import { approvalResultLabel, normalizeProcess } from './process'

describe('approvalResultLabel', () => {
  it('0-4 映射文案与色调', () => {
    expect(approvalResultLabel(0)).toEqual({ text: '待审批', tone: 'info' })
    expect(approvalResultLabel(1)).toEqual({ text: '通过', tone: 'success' })
    expect(approvalResultLabel(2)).toEqual({ text: '拒绝', tone: 'danger' })
    expect(approvalResultLabel(3)).toEqual({ text: '关闭', tone: 'danger' })
    expect(approvalResultLabel(4)).toEqual({ text: '等待', tone: 'muted' })
  })

  it('未知值回退原值灰调', () => {
    expect(approvalResultLabel(9)).toEqual({ text: '9', tone: 'muted' })
  })
})

describe('normalizeProcess（旧网关 staffInfos 载荷 → 内部 approvers 形状）', () => {
  it('recordNode=0 提取为提交节点，其余 staffInfos 映射 approvers', () => {
    const result = normalizeProcess([
      {
        recordNode: 0,
        staffInfos: [{ staffName: '王建国', resultDesc: '提交申请', createDate: '2026-06-12 09:00' }],
      },
      {
        statusName: '室友审批',
        staffInfos: [
          { staffName: '张**', result: 1, remark: '同意', recordDate: '2026-06-12 10:00' },
          { staffName: '李**', result: 0, createDate: '2026-06-12 09:30' },
        ],
      },
    ])
    expect(result.submitter).toEqual({ name: '王建国', time: '2026-06-12 09:00', desc: '提交申请' })
    expect(result.nodes).toEqual([
      {
        statusName: '室友审批',
        approvers: [
          { name: '张**', result: 1, resultDesc: undefined, opinion: '同意', time: '2026-06-12 10:00' },
          { name: '李**', result: 0, resultDesc: undefined, opinion: undefined, time: '2026-06-12 09:30' },
        ],
      },
    ])
  })

  it('已是 approvers 形状的节点原样通过（mock/既有调用兼容）', () => {
    const result = normalizeProcess([
      { statusName: '宿管审批', approvers: [{ name: '张**', result: 1, time: 't' }] },
    ])
    expect(result.submitter).toBeUndefined()
    expect(result.nodes[0]?.approvers?.[0]?.name).toBe('张**')
  })

  it('空入参容错', () => {
    expect(normalizeProcess(undefined)).toEqual({ submitter: undefined, nodes: [] })
  })
})
