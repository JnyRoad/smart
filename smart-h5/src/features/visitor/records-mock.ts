import type { ApplyRecordDetail, ApprovalNode, RecordSummary } from './records-api'

/**
 * Explicit demo fixtures for the visitor-records list/detail pages (config flag
 * features.visitorRecordsMock). Covers every applyStatus, every dispatchStatus
 * and all detail demo states.
 *
 * Demo boundary: the mock covers verify -> list -> detail. The pass-code page
 * (/visitor/code) queries a different gateway endpoint outside this flag, so
 * following "查看入园通行码" in mock mode still uses the real pass-code API.
 */
export const MOCK_QUERY_TOKEN = 'mock-query-token'
export const MOCK_IDENTITY = { maskedName: '李明', maskedMobile: '137****1234' }

export function mockDelay(ms = 300): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

/** 模拟作废后端终态，保证本地演示可验证“作废后重新预约”的页面路径。 */
export function revokeMockApply(applyId: string): boolean {
	const summary = MOCK_LIST.find((item) => item.applyId === applyId)
	const detail = MOCK_DETAILS[applyId]
	if (!summary || !detail || summary.applyStatus === 'REVOKED') return false
	summary.applyStatus = 'REVOKED'
	delete summary.currentNode
	delete summary.dispatchStatus
	detail.detail.applyStatus = 'REVOKED'
	delete detail.detail.dispatchStatus
	detail.nodes = []
	return true
}

export const MOCK_LIST: RecordSummary[] = [
  {
    applyId: 'mock-pending',
    parkName: '裕同科技许昌园区',
    applyStatus: 'PENDING',
    receptionistName: '王强',
    startTime: '2026-06-12 09:30',
    endTime: '2026-06-12 18:00',
    fellowCount: 2,
    plates: ['豫A·D88E6'],
    currentNode: '部门负责人 张三 审批中',
    submitTime: '2026-06-10 15:08',
  },
  {
    applyId: 'mock-passed',
    parkName: '裕同科技许昌园区',
    applyStatus: 'PASSED',
    receptionistName: '刘洋',
    startTime: '2026-06-05 14:00',
    endTime: '2026-06-05 17:30',
    fellowCount: 0,
    plates: [],
    dispatchStatus: 'SUCCESS',
    submitTime: '2026-06-04 10:21',
  },
  {
    applyId: 'mock-passed-issuing',
    parkName: '裕同科技许昌园区',
    applyStatus: 'PASSED',
    receptionistName: '赵敏',
    startTime: '2026-06-11 13:30',
    endTime: '2026-06-11 17:00',
    fellowCount: 0,
    plates: ['豫A·K5203'],
    dispatchStatus: 'ISSUING',
    submitTime: '2026-06-10 09:02',
  },
  {
    applyId: 'mock-passed-failed',
    parkName: '裕同科技许昌园区',
    applyStatus: 'PASSED',
    receptionistName: '孙强',
    startTime: '2026-06-09 09:00',
    endTime: '2026-06-09 11:30',
    fellowCount: 1,
    plates: [],
    dispatchStatus: 'FAILED',
    submitTime: '2026-06-08 16:40',
  },
  {
    applyId: 'mock-rejected',
    parkName: '裕同科技许昌园区',
    applyStatus: 'REJECTED',
    receptionistName: '王强',
    startTime: '2026-06-08 10:00',
    endTime: '2026-06-08 12:00',
    fellowCount: 0,
    plates: [],
    submitTime: '2026-06-07 18:12',
  },
  {
    applyId: 'mock-expired',
    parkName: '裕同科技许昌园区',
    applyStatus: 'EXPIRED',
    receptionistName: '张三',
    startTime: '2026-04-17 09:00',
    endTime: '2026-04-17 18:00',
    fellowCount: 0,
    plates: [],
    submitTime: '2026-04-15 09:30',
  },
  {
    applyId: 'mock-revoked',
    parkName: '裕同科技许昌园区',
    applyStatus: 'REVOKED',
    receptionistName: '周敏',
    startTime: '2026-05-20 09:00',
    endTime: '2026-05-20 12:00',
    fellowCount: 0,
    plates: [],
    submitTime: '2026-05-18 14:00',
  },
]

const BASE_DETAIL: Omit<ApplyRecordDetail, 'applyId' | 'applyStatus' | 'dispatchStatus'> = {
  applyNo: 'VA20260610-0027',
  parkName: '裕同科技许昌园区',
  receptionistName: '王强',
  startTime: '2026-06-12 09:30',
  endTime: '2026-06-12 18:00',
  cause: '供应商打样确认，洽谈包装结构方案',
  visitorName: '李明',
  visitorPhone: '137****1234',
  fellows: [
    { name: '赵六', phone: '150****8821' },
    { name: '周燕', phone: '186****3307' },
  ],
  vehicles: [{ plate: '豫A·D88E6', type: '小型客车' }],
  areas: ['研发楼 A 座', '样品展示厅'],
  submitTime: '2026-06-10 15:08',
}

const DONE_NODE: ApprovalNode = {
  title: '被访人审批',
  state: 'done',
  approverName: '王强',
  time: '2026-06-10 16:55',
}

export const MOCK_DETAILS: Record<string, { detail: ApplyRecordDetail; nodes: ApprovalNode[] }> = {
  'mock-pending': {
    detail: { ...BASE_DETAIL, applyId: 'mock-pending', applyStatus: 'PENDING' },
    nodes: [
      DONE_NODE,
      { title: '部门负责人审批', state: 'current', approverName: '张三' },
    ],
  },
  'mock-passed': {
    detail: { ...BASE_DETAIL, applyId: 'mock-passed', applyStatus: 'PASSED', dispatchStatus: 'SUCCESS' },
    nodes: [
      DONE_NODE,
      { title: '部门负责人审批', state: 'done', approverName: '张三', time: '2026-06-11 09:20' },
    ],
  },
  'mock-passed-issuing': {
    detail: {
      ...BASE_DETAIL,
      applyId: 'mock-passed-issuing',
      applyStatus: 'PASSED',
      dispatchStatus: 'ISSUING',
    },
    nodes: [
      DONE_NODE,
      { title: '部门负责人审批', state: 'done', approverName: '张三', time: '2026-06-11 09:20' },
    ],
  },
  'mock-passed-failed': {
    detail: {
      ...BASE_DETAIL,
      applyId: 'mock-passed-failed',
      applyStatus: 'PASSED',
      dispatchStatus: 'FAILED',
    },
    nodes: [
      DONE_NODE,
      { title: '部门负责人审批', state: 'done', approverName: '张三', time: '2026-06-11 09:20' },
    ],
  },
  'mock-rejected': {
    detail: { ...BASE_DETAIL, applyId: 'mock-rejected', applyStatus: 'REJECTED' },
    nodes: [
      {
        title: '被访人审批',
        state: 'rejected',
        approverName: '王强',
        time: '2026-06-10 18:31',
        comment: '当日园区有接待安排，请改约下周',
      },
    ],
  },
  'mock-expired': {
    detail: { ...BASE_DETAIL, applyId: 'mock-expired', applyStatus: 'EXPIRED' },
    nodes: [
      { ...DONE_NODE, time: '2026-04-15 11:40' },
      { title: '部门负责人审批', state: 'done', approverName: '张三', time: '2026-04-16 08:55' },
    ],
  },
  'mock-revoked': {
    detail: { ...BASE_DETAIL, applyId: 'mock-revoked', applyStatus: 'REVOKED' },
    nodes: [DONE_NODE],
  },
}
