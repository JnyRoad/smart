'use client'
import {
  approvalResultLabel,
  normalizeProcess,
  type ProcessNode,
  type ProcessTone,
} from '@/features/dorm-services/process'

const TONE_TEXT: Record<ProcessTone, string> = {
  info: 'text-[#2376d9]',
  success: 'text-[#16a673]',
  danger: 'text-[#d83b36]',
  muted: 'text-weak',
}

const TONE_DOT: Record<ProcessTone, string> = {
  info: 'bg-[#2376d9]',
  success: 'bg-[#16a673]',
  danger: 'bg-[#d83b36]',
  muted: 'bg-light-gray',
}

function nodeTone(node: ProcessNode): ProcessTone {
  const results = (node.approvers ?? []).map((a) => a.result ?? 4)
  if (results.some((r) => r === 2 || r === 3)) return 'danger'
  if (results.length > 0 && results.every((r) => r === 1)) return 'success'
  // Pending approvers OR a partially-approved countersign node = in progress.
  if (results.some((r) => r === 0) || results.some((r) => r === 1)) return 'info'
  return 'muted'
}

/**
 * Approval-process timeline (legacy components/process port): node 0 is the
 * submission, each approval node lists its approvers with result and opinion.
 */
export function ApprovalTimeline({
  submitter,
  nodes: rawNodes,
}: {
  submitter?: { name?: string; time?: string }
  nodes: ProcessNode[]
}) {
  const items: { title: React.ReactNode; meta: React.ReactNode; tone: ProcessTone }[] = []

  const normalized = normalizeProcess(rawNodes)
  // 真实载荷自带提交节点（recordNode===0）时优先，避免与合成提交人重复。
  const submitNode = normalized.submitter ?? (submitter?.name ? { ...submitter, desc: undefined } : undefined)
  const nodes = normalized.nodes

  if (submitNode?.name) {
    items.push({
      // 旧版提交行显示服务端 resultDesc，缺省才用「提交申请」。
      title: `${submitNode.name} - ${submitNode.desc || '提交申请'}`,
      meta: submitNode.time,
      tone: 'success',
    })
  }
  for (const node of nodes) {
    items.push({
      title: (
        <span>
          <span className="block font-semibold">{node.statusName}</span>
          {(node.approvers ?? []).map((approver, index) => {
            const label = approvalResultLabel(approver.result ?? 4)
            return (
              <span key={`${approver.name}-${index}`} className="mt-0.5 block text-[13px]">
                {approver.name} -{' '}
                <span className={TONE_TEXT[label.tone]}>{approver.resultDesc || label.text}</span>
                {approver.opinion && <span className="block text-xs text-mid">意见: {approver.opinion}</span>}
                {approver.time && <span className="block text-xs text-weak">{approver.time}</span>}
              </span>
            )
          })}
        </span>
      ),
      meta: null,
      tone: nodeTone(node),
    })
  }

  return (
    <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
      <h2 className="mb-3 text-[15px] font-bold">审批流程</h2>
      <div className="flex flex-col">
        {items.map((item, index) => (
          <div key={index} className="relative flex gap-3 pb-4 last:pb-0">
            {index < items.length - 1 && (
              <span aria-hidden className="absolute top-4 left-[5px] h-full w-px bg-border-soft" />
            )}
            <span aria-hidden className={`relative mt-1.5 h-[11px] w-[11px] flex-none rounded-full ${TONE_DOT[item.tone]}`} />
            <div className="min-w-0 text-sm">
              {item.title}
              {item.meta && <p className="mt-0.5 text-xs text-weak">{item.meta}</p>}
            </div>
          </div>
        ))}
        {items.length === 0 && <p className="py-2 text-sm text-weak">暂无审批流程信息</p>}
      </div>
    </div>
  )
}
