export type ProcessTone = 'info' | 'success' | 'danger' | 'muted'

export interface ApproverItem {
  name?: string
  result?: number
  /** 服务端结果文案，优先于本地 result 码映射展示。 */
  resultDesc?: string
  opinion?: string
  time?: string
}

export interface ProcessNode {
  statusName?: string
  approvers?: ApproverItem[]
  /** 旧版真实载荷字段（recordNode===0 为提交节点）。 */
  recordNode?: number
  staffInfos?: StaffInfoItem[]
}

export interface StaffInfoItem {
  staffName?: string
  result?: number
  resultDesc?: string
  remark?: string
  recordDate?: string
  createDate?: string
}

export interface NormalizedProcess {
  submitter?: { name?: string; time?: string; desc?: string }
  nodes: ProcessNode[]
}

/**
 * 旧版网关时间线载荷是 {recordNode, statusName, staffInfos[]}，新代码内部
 * 形状是 {statusName, approvers[]}——两种都接受：staffInfos 映射为 approvers
 * （remark→opinion、recordDate||createDate→time），recordNode===0 提取为提交节点。
 */
export function normalizeProcess(raw: ProcessNode[] | undefined): NormalizedProcess {
  const nodes: ProcessNode[] = []
  let submitter: NormalizedProcess['submitter']
  for (const node of raw ?? []) {
    if (node.recordNode === 0 && node.staffInfos?.length) {
      const first = node.staffInfos[0]
      submitter = { name: first?.staffName, time: first?.createDate, desc: first?.resultDesc }
      continue
    }
    if (node.approvers) {
      nodes.push({ statusName: node.statusName, approvers: node.approvers })
      continue
    }
    nodes.push({
      statusName: node.statusName,
      approvers: (node.staffInfos ?? []).map((staff) => ({
        name: staff.staffName,
        result: staff.result,
        resultDesc: staff.resultDesc,
        opinion: staff.remark,
        time: staff.recordDate || staff.createDate,
      })),
    })
  }
  return { submitter, nodes }
}

/** Legacy result codes: 0 pending, 1 approved, 2 rejected, 3 closed, 4 waiting. */
const RESULT_LABELS: Record<number, { text: string; tone: ProcessTone }> = {
  0: { text: '待审批', tone: 'info' },
  1: { text: '通过', tone: 'success' },
  2: { text: '拒绝', tone: 'danger' },
  3: { text: '关闭', tone: 'danger' },
  4: { text: '等待', tone: 'muted' },
}

export function approvalResultLabel(result: number): { text: string; tone: ProcessTone } {
  return RESULT_LABELS[result] ?? { text: String(result), tone: 'muted' }
}
