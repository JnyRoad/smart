'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { use, useEffect, useMemo, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { approvalNodeStatusText, formatVisitRange } from '@/features/visitor/record-status'
import {
  clearQuerySession,
  fetchApplyDetail,
  fetchApprovalProgress,
  getQuerySession,
  isAuthRejected,
  revokeApply,
  type ApplyRecordDetail,
  type ApprovalNode,
} from '@/features/visitor/records-api'
import { sanitizeRichText } from '@/lib/sanitize'
import { confirmIrreversible } from '@/lib/confirm-irreversible'
import { useMounted } from '@/lib/use-mounted'

function heroFor(detail: ApplyRecordDetail, nodes: ApprovalNode[]) {
  const { applyStatus, dispatchStatus } = detail
  if (applyStatus === 'PENDING') {
    const current = nodes.find((n) => n.state === 'current')
    const done = nodes.filter((n) => n.state === 'done').length
    return {
      tone: 'warning' as const,
      main: '审批中',
      sub: current
        ? `当前停留在 ${current.title} ${current.approverName ?? ''} 处，共 ${nodes.length} 个流程节点，已完成 ${done} 个`
        : `共 ${nodes.length} 个流程节点，已完成 ${done} 个`,
    }
  }
  if (applyStatus === 'PASSED') {
    // Missing dispatchStatus is treated as still issuing, never as success.
    const dispatch = dispatchStatus ?? 'ISSUING'
    if (dispatch === 'FAILED') {
      return {
        tone: 'danger' as const,
        main: '审批已通过 · 权限下发失败',
        sub: `门岗未能收到通行权限，请联系被访人 ${detail.receptionistName} 处理，或至门岗人工登记`,
      }
    }
    if (dispatch === 'ISSUING') {
      return { tone: 'warning' as const, main: '审批已通过 · 权限下发中', sub: '通行权限正在下发至门岗设备，请稍后刷新' }
    }
    return { tone: 'success' as const, main: '审批已通过 · 权限已下发', sub: '可凭通行码入园，请提前准备' }
  }
  if (applyStatus === 'REJECTED') {
    const rejected = nodes.find((n) => n.state === 'rejected')
    return {
      tone: 'danger' as const,
      main: '审批未通过',
      sub: rejected ? `${rejected.title} ${rejected.approverName ?? ''} 已拒绝本次申请，可修改后重新预约` : '可修改后重新预约',
    }
  }
  if (applyStatus === 'EXPIRED') {
    return { tone: 'muted' as const, main: '申请已过期', sub: '来访时间已过，如需入园请重新预约' }
  }
  return { tone: 'muted' as const, main: '申请已作废', sub: '本次申请已作废，如需入园请重新预约' }
}

const HERO_TONES = {
  success: 'bg-[rgba(22,166,115,0.10)] text-[#16a673]',
  warning: 'bg-accent-soft text-accent-ink',
  danger: 'bg-[rgba(216,59,54,0.08)] text-[#d83b36]',
  muted: 'bg-surface text-mid',
}

const NODE_DOTS = {
  done: 'bg-[#16a673]',
  current: 'bg-brand',
  wait: 'bg-light-gray',
  rejected: 'bg-[#d83b36]',
}

const APPROVAL_COMMENT_VISIBLE_SELECTORS = 'img, table, ul, ol'
const APPROVAL_COMMENT_STRUCTURE_SELECTORS = 'table, thead, tbody, tfoot, tr, td, th, ul, ol, li'

function isWhitespaceTextNode(node: ChildNode): boolean {
  return node.nodeType === Node.TEXT_NODE && (node.textContent ?? '').trim() === ''
}

function isBreakNode(node: ChildNode): boolean {
  return node instanceof HTMLBRElement
}

function isEmptyElementNode(node: ChildNode): boolean {
  if (!(node instanceof HTMLElement)) return false
  if (node instanceof HTMLBRElement) return true

  const hasText = (node.textContent ?? '').trim().length > 0
  const hasMedia = node.matches(APPROVAL_COMMENT_VISIBLE_SELECTORS) || node.querySelector(APPROVAL_COMMENT_VISIBLE_SELECTORS) !== null
  return !hasText && !hasMedia
}

function isBlankLineNode(node: ChildNode): boolean {
  return isWhitespaceTextNode(node) || isBreakNode(node) || isEmptyElementNode(node)
}

function trimBoundaryBlankLines(parent: ParentNode) {
  while (parent.firstChild && isBlankLineNode(parent.firstChild)) {
    parent.firstChild.remove()
  }
  while (parent.lastChild && isBlankLineNode(parent.lastChild)) {
    parent.lastChild.remove()
  }
}

function compactApprovalCommentChildren(parent: ParentNode) {
  if (parent instanceof HTMLElement && parent.matches(APPROVAL_COMMENT_STRUCTURE_SELECTORS)) return

  Array.from(parent.childNodes).forEach((child) => {
    if (child instanceof HTMLElement) compactApprovalCommentChildren(child)
  })

  trimBoundaryBlankLines(parent)

  let previousBreak = false
  Array.from(parent.childNodes).forEach((child) => {
    if (isWhitespaceTextNode(child)) return
    if (isBreakNode(child)) {
      if (previousBreak) {
        child.remove()
        return
      }
      previousBreak = true
      return
    }
    previousBreak = false
  })

  trimBoundaryBlankLines(parent)
}

function compactApprovalCommentHtml(html: string): string {
  const template = document.createElement('template')
  template.innerHTML = html
  compactApprovalCommentChildren(template.content)
  return template.innerHTML
}

function InfoRow({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex justify-between gap-4 border-b border-border-soft py-2.5 last:border-b-0">
      <span className="flex-none text-[13px] text-mid">{label}</span>
      <span className="text-right text-[13px] font-medium break-all">{children}</span>
    </div>
  )
}

function ApprovalComment({ html }: { html: string }) {
  const mounted = useMounted()
  // 审批意见可能来自 OA/公众号富文本，必须先过统一白名单再渲染，避免把脚本事件带进 H5。
  const sanitized = useMemo(() => (mounted ? compactApprovalCommentHtml(sanitizeRichText(html)) : ''), [html, mounted])
  if (!sanitized) return null

  return (
    <div
      className="mt-1 rounded-lg bg-surface px-2.5 py-1.5 text-xs leading-relaxed break-words text-mid [&_p]:m-0 [&_span]:break-words"
      dangerouslySetInnerHTML={{ __html: sanitized }}
    />
  )
}

function RecordDetailInner({ applyId }: { applyId: string }) {
  const router = useRouter()
  const mounted = useMounted()
  const hasSession = mounted && getQuerySession() !== null

  // Deep links arrive without a session: verify identity first, then come back.
  useEffect(() => {
    if (mounted && !hasSession) {
      router.replace(`/visitor/records?redirect=${encodeURIComponent(applyId)}`)
    }
  }, [mounted, hasSession, router, applyId])

  const detail = useQuery({
    queryKey: ['visitor-record', applyId],
    queryFn: () => fetchApplyDetail(applyId),
    enabled: hasSession,
  })
  const progress = useQuery({
    queryKey: ['visitor-record-progress', applyId],
    queryFn: () => fetchApprovalProgress(applyId),
    enabled: hasSession,
  })

  // Expired/invalid token: clear and re-verify. Covers both shapes — the
  // gateway envelope ({code: 401|403} returned as data) and thrown ApiError.
  const tokenRejected = [detail.error, progress.error, detail.data, progress.data].some(
    (value) => value !== undefined && value !== null && isAuthRejected(value),
  )
  // A 403 can also mean "this applyId belongs to someone else" (anti-IDOR).
  // Re-verifying once is fine; a second rejection for the same applyId must
  // not loop — render an access-denied state instead.
  const deniedKey = `visitor-record-denied-${applyId}`
  const deniedBefore = mounted && sessionStorage.getItem(deniedKey) !== null
  const accessDenied = tokenRejected && deniedBefore
  useEffect(() => {
    if (tokenRejected && !deniedBefore) {
      sessionStorage.setItem(deniedKey, '1')
      clearQuerySession()
      router.replace(`/visitor/records?redirect=${encodeURIComponent(applyId)}`)
    }
  }, [tokenRejected, deniedBefore, deniedKey, router, applyId])
  useEffect(() => {
    if (detail.data?.code === 0) sessionStorage.removeItem(deniedKey)
  }, [detail.data, deniedKey])

  const reset = useVisitorFlow((s) => s.reset)
  const [isVoiding, setIsVoiding] = useState(false)

  if (accessDenied) {
    return (
      <PageShell title="申请详情" onBack={() => router.push('/visitor/records')}>
        <ErrorBlock
          status="default"
          title="无权查看该申请"
          description="该申请不属于当前验证的身份，请确认链接或更换手机号验证"
          className="py-16"
        />
      </PageShell>
    )
  }

  if (!mounted || !hasSession) return null

  if (detail.isPending || progress.isPending) {
    return (
      <PageShell title="申请详情">
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      </PageShell>
    )
  }

  const info = detail.data?.code === 0 ? detail.data.data : undefined
  const nodes = progress.data?.code === 0 ? (progress.data.data?.nodes ?? []) : []
  if (!info) {
    return (
      <PageShell title="申请详情">
        <ErrorBlock
          status="default"
          description={detail.data?.message ?? '申请单不存在或暂时无法查询'}
          className="py-16"
        />
      </PageShell>
    )
  }

  const hero = heroFor(info, nodes)
  const currentApplyId = info.applyId
  const showPassCode = info.applyStatus === 'PASSED' && info.dispatchStatus !== 'FAILED'
  const showRebook = info.applyStatus === 'REJECTED' || info.applyStatus === 'EXPIRED' || info.applyStatus === 'REVOKED'
  const showVoid = info.applyStatus !== 'REVOKED'

  function rebook() {
    // 详情载荷仍是展示形状（手机号脱敏、区域名/事由文本已格式化），
    // 不能可靠回填为可提交草稿，重新预约从空草稿开始。
    reset()
    router.push('/visitor')
  }

  async function refreshBoth() {
    await Promise.all([detail.refetch(), progress.refetch()])
  }

  async function voidApply() {
    const confirmed = await confirmIrreversible(
      '确认作废该申请？作废后单据立即失效，已下发的通行权限将进入回收流程，且不可恢复。',
    )
    if (!confirmed) return
    setIsVoiding(true)
    try {
      const result = await revokeApply(currentApplyId)
      if (result.code !== 0 || result.data !== true) {
        throw new Error(result.message ?? '作废申请失败')
      }
      await refreshBoth()
      Toast.show({ icon: 'success', content: '申请已作废' })
    } catch (error) {
      Toast.show({ content: error instanceof Error ? error.message : '作废申请失败' })
    } finally {
      setIsVoiding(false)
    }
  }

  return (
    <PageShell title="申请详情" onBack={() => router.push('/visitor/records')}>
      <PullToRefresh onRefresh={refreshBoth}>
        <div className="flex flex-col gap-3 pb-6">
          {/* 状态 hero */}
          <div className={`rounded-2xl p-4 ${HERO_TONES[hero.tone]}`}>
            <p className="text-[17px] font-bold">{hero.main}</p>
            <p className="mt-1 text-[13px] leading-relaxed opacity-90">{hero.sub}</p>
            {showPassCode && (
              <button
                type="button"
                onClick={() => router.push(`/visitor/code?id=${encodeURIComponent(info.applyId)}`)}
                className={`mt-3 flex h-11 w-full items-center justify-center rounded-[14px] text-[15px] font-semibold ${
                  info.dispatchStatus === 'SUCCESS'
                    ? 'bg-brand text-white active:bg-[#d95f00]'
                    : 'border-[1.5px] border-brand bg-white text-brand'
                }`}
              >
                查看入园通行码
              </button>
            )}
            {showRebook && (
              <button
                type="button"
                onClick={rebook}
                className="mt-3 flex h-11 w-full items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white active:bg-[#d95f00]"
              >
                {info.applyStatus === 'REJECTED' ? '修改信息重新预约' : '再次预约'}
              </button>
            )}
            {showVoid && (
              <button
                type="button"
                onClick={voidApply}
                disabled={isVoiding}
                className="mt-3 flex h-11 w-full items-center justify-center rounded-[14px] border-[1.5px] border-[#d83b36] bg-white text-[15px] font-semibold text-[#d83b36] disabled:opacity-50"
              >
                {isVoiding ? '作废中…' : '作废申请'}
              </button>
            )}
          </div>

          {/* 审批进度时间线 */}
          <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <h2 className="mb-3 text-[15px] font-bold">审批进度</h2>
            <div className="flex flex-col">
              {nodes.map((node, index) => (
                <div key={`${node.title}-${index}`} className="relative flex gap-3 pb-4 last:pb-0">
                  {index < nodes.length - 1 && (
                    <span aria-hidden className="absolute top-4 left-[5px] h-full w-px bg-border-soft" />
                  )}
                  <span aria-hidden className={`relative mt-1.5 h-[11px] w-[11px] flex-none rounded-full ${NODE_DOTS[node.state]}`} />
                  <div className="min-w-0">
                    <p className="text-sm font-semibold">{node.title}</p>
                    <p className="mt-0.5 text-xs text-mid">
                      {node.state === 'current' && (
                        <>
                          <span className="font-semibold text-accent-ink">{node.approverName}</span>{' '}
                          {approvalNodeStatusText(node)}
                        </>
                      )}
                      {node.state === 'done' && (
                        <>
                          <span className="font-semibold">{node.approverName}</span> {approvalNodeStatusText(node)}
                          {node.time && ` · ${node.time}`}
                        </>
                      )}
                      {node.state === 'rejected' && (
                        <>
                          <span className="font-semibold text-[#d83b36]">{node.approverName}</span>{' '}
                          {approvalNodeStatusText(node)}
                          {node.time && ` · ${node.time}`}
                        </>
                      )}
                      {node.state === 'wait' && approvalNodeStatusText(node)}
                    </p>
                    {node.comment && <ApprovalComment html={node.comment} />}
                  </div>
                </div>
              ))}
              {nodes.length === 0 && <p className="py-2 text-sm text-weak">暂无审批节点信息</p>}
            </div>
          </div>

          {/* 申请信息 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <h2 className="pt-2 pb-1 text-[15px] font-bold">申请信息</h2>
            <InfoRow label="申请单号">{info.applyNo}</InfoRow>
            <InfoRow label="来访园区">{info.parkName}</InfoRow>
            <InfoRow label="被访人">{info.receptionistName}</InfoRow>
            <InfoRow label="来访时间">{formatVisitRange(info.startTime, info.endTime)}</InfoRow>
            <InfoRow label="来访事由">{info.cause}</InfoRow>
            <InfoRow label="访客">
              {info.visitorName} {info.visitorPhone}
            </InfoRow>
            {info.fellows.length > 0 && (
              <InfoRow label="随行人员">
                {info.fellows.map((f) => `${f.name} ${f.phone}`).join('，')}
              </InfoRow>
            )}
            {info.vehicles.length > 0 && (
              <InfoRow label="来访车辆">
                {info.vehicles.map((v) => `${v.plate}${v.type ? ` ${v.type}` : ''}`).join('，')}
              </InfoRow>
            )}
            {info.areas.length > 0 && <InfoRow label="授权区域">{info.areas.join('，')}</InfoRow>}
            <InfoRow label="提交时间">{info.submitTime}</InfoRow>
          </div>

          <p className="px-2 text-center text-xs leading-relaxed text-weak">
            进度有更新时将通过短信 / 公众号通知您，也可下拉刷新查看最新进度
          </p>
        </div>
      </PullToRefresh>
    </PageShell>
  )
}

export default function RecordDetailPage({ params }: { params: Promise<{ applyId: string }> }) {
  const { applyId } = use(params)
  return <RecordDetailInner applyId={applyId} />
}
