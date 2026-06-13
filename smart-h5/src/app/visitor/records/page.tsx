'use client'
import { ErrorBlock, PullToRefresh, SpinLoading, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { SmsCodeField } from '@/components/sms-code-field'
import {
  applyStatusBadge,
  dispatchStatusText,
  formatVisitRange,
  type ApplyStatus,
  type Tone,
} from '@/features/visitor/record-status'
import {
  clearQuerySession,
  fetchMyApplies,
  getQuerySession,
  isAuthRejected,
  saveQuerySession,
  sendRecordSms,
  type RecordSummary,
} from '@/features/visitor/records-api'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { useMounted } from '@/lib/use-mounted'

const FILTERS: { label: string; status?: ApplyStatus }[] = [
  { label: '全部' },
  { label: '审批中', status: 'PENDING' },
  { label: '已通过', status: 'PASSED' },
  { label: '已拒绝', status: 'REJECTED' },
  { label: '已过期', status: 'EXPIRED' },
]

const TONE_CLASSES: Record<Tone, string> = {
  success: 'bg-[rgba(22,166,115,0.12)] text-[#16a673]',
  warning: 'bg-[rgba(201,132,22,0.12)] text-[#c98416]',
  danger: 'bg-[rgba(216,59,54,0.12)] text-[#d83b36]',
  muted: 'bg-surface text-weak',
}

function RecordCard({ record, onClick }: { record: RecordSummary; onClick: () => void }) {
  const badge = applyStatusBadge(record.applyStatus)
  const fellowText = record.fellowCount > 0 ? `随行 ${record.fellowCount} 人` : '无随行'
  const plateText = record.plates.length > 0 ? record.plates.join('、') : '无车辆'
  const dispatch = record.dispatchStatus ? dispatchStatusText(record.dispatchStatus) : null

  return (
    <button
      type="button"
      onClick={onClick}
      className="w-full rounded-2xl bg-white p-4 text-left shadow-[0_16px_40px_rgba(89,87,87,0.10)] active:bg-surface"
    >
      <div className="flex items-center justify-between gap-2">
        <span className="text-[15px] font-bold">{record.parkName}</span>
        <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${TONE_CLASSES[badge.tone]}`}>
          {badge.text}
        </span>
      </div>
      <div className="mt-2 flex flex-col gap-1 text-[13px]">
        <div className="flex gap-3">
          <span className="flex-none text-mid">被访人</span>
          <span>{record.receptionistName}</span>
        </div>
        <div className="flex gap-3">
          <span className="flex-none text-mid">来访时间</span>
          <span>{formatVisitRange(record.startTime, record.endTime)}</span>
        </div>
        <div className="flex gap-3">
          <span className="flex-none text-mid">随行/车辆</span>
          <span>
            {fellowText} · {plateText}
          </span>
        </div>
      </div>
      <div className="mt-2.5 flex items-center justify-between border-t border-border-soft pt-2.5 text-xs">
        <span className="min-w-0 truncate text-mid">
          {record.applyStatus === 'PENDING' && record.currentNode && (
            <>
              当前节点：<em className="font-semibold text-accent-ink not-italic">{record.currentNode}</em>
            </>
          )}
          {record.applyStatus === 'PASSED' && dispatch && (
            <>
              通行权限：
              <span className={`font-semibold ${TONE_CLASSES[dispatch.tone]} rounded px-1`}>{dispatch.text}</span>
            </>
          )}
        </span>
        <span className="flex-none font-semibold text-brand">
          {record.applyStatus === 'PENDING' ? '查看进度 ›' : '查看详情 ›'}
        </span>
      </div>
    </button>
  )
}

function RecordsInner() {
  const router = useRouter()
  const redirectParam = useSearchParams().get('redirect')
  // applyId only — a free-form redirect target could steer the post-verify
  // navigation to an arbitrary same-origin path.
  const redirect = redirectParam && /^[\w-]+$/.test(redirectParam) ? redirectParam : null
  const mounted = useMounted()
  const [phase, setPhase] = useState<'boot' | 'verify' | 'list' | 'error'>('boot')
  const [phone, setPhone] = useState('')
  const [smsCode, setSmsCode] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [records, setRecords] = useState<RecordSummary[]>([])
  const [identity, setIdentity] = useState({ maskedName: '', maskedMobile: '' })
  const [filter, setFilter] = useState(0)
  const bootedRef = useRef(false)

  async function loadWithExistingCredential() {
    // Existing token first; otherwise try openId silent verification.
    const openId = useVisitorFlow.getState().host.openId
    const hasSession = getQuerySession() !== null
    if (!hasSession && !openId) {
      setPhase('verify')
      return
    }
    try {
      const res = await fetchMyApplies(hasSession ? null : { openId })
      if (res.code === 0 && res.data) {
        applyResult(res.data)
      } else if (isAuthRejected(res)) {
        clearQuerySession()
        setPhase('verify')
      } else if (hasSession) {
        // Transient failure with a valid token: don't burn the verification.
        setPhase('error')
      } else {
        setPhase('verify')
      }
    } catch (error) {
      if (isAuthRejected(error)) {
        clearQuerySession()
        setPhase('verify')
      } else if (getQuerySession() !== null) {
        setPhase('error')
      } else {
        setPhase('verify')
      }
    }
  }

  function applyResult(data: {
    queryToken: string
    maskedName: string
    maskedMobile: string
    records: RecordSummary[]
  }) {
    saveQuerySession({
      queryToken: data.queryToken,
      maskedName: data.maskedName,
      maskedMobile: data.maskedMobile,
    })
    setIdentity({ maskedName: data.maskedName, maskedMobile: data.maskedMobile })
    setRecords(data.records)
    setPhase('list')
    if (redirect) {
      router.replace(`/visitor/records/${redirect}`)
    }
  }

  useEffect(() => {
    if (!mounted || bootedRef.current) return
    bootedRef.current = true
    void loadWithExistingCredential()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mounted])

  async function handleVerify() {
    if (!phone) return Toast.show('请输入手机号')
    if (!/^1\d{10}$/.test(phone)) return Toast.show('手机号格式不正确')
    if (!smsCode) return Toast.show('请输入验证码')
    setSubmitting(true)
    try {
      const res = await fetchMyApplies({ mobile: phone, smsCode })
      if (res.code === 0 && res.data) {
        applyResult(res.data)
      } else {
        Toast.show(res.message ?? '验证失败')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '验证失败')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRefresh() {
    try {
      const res = await fetchMyApplies(null)
      if (res.code === 0 && res.data) {
        applyResult(res.data)
      } else if (isAuthRejected(res)) {
        clearQuerySession()
        setPhase('verify')
      } else {
        Toast.show(res.message ?? '刷新失败')
      }
    } catch (error) {
      if (isAuthRejected(error)) {
        clearQuerySession()
        setPhase('verify')
      } else {
        Toast.show('刷新失败')
      }
    }
  }

  if (!mounted) return null

  const activeFilter = FILTERS[filter]
  const visible = activeFilter?.status
    ? records.filter((r) => r.applyStatus === activeFilter.status)
    : records

  return (
    <PageShell title="我的申请记录" onBack={() => router.push('/visitor')}>
      {phase === 'boot' && (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      )}

      {phase === 'error' && (
        <div className="py-8">
          <ErrorBlock status="default" title="加载失败" description="网络异常，请稍后重试" />
          <button
            type="button"
            onClick={() => {
              setPhase('boot')
              void loadWithExistingCredential()
            }}
            className="mx-auto mt-4 flex h-11 w-40 items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white"
          >
            重试
          </button>
        </div>
      )}

      {phase === 'verify' && (
        <div>
          <div className="px-0.5 pt-2 pb-1">
            <h2 className="text-[22px] leading-snug font-bold">验证身份后查看记录</h2>
            <p className="mt-2 text-[13px] leading-relaxed text-mid">
              为保护访客隐私，请使用申请时填写的手机号完成短信验证。
            </p>
          </div>
          <div className="mt-3 rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <SmsCodeField
              phone={phone}
              code={smsCode}
              onPhoneChange={setPhone}
              onCodeChange={setSmsCode}
              onSend={async (mobile) => {
                const res = await sendRecordSms(mobile)
                if (res.code !== 0) throw new Error(res.message ?? '发送失败')
              }}
            />
            <button
              type="button"
              disabled={submitting}
              onClick={() => void handleVerify()}
              className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
            >
              {submitting ? '验证中…' : '查看申请记录'}
            </button>
          </div>
          <p className="mt-3.5 px-1 text-xs leading-relaxed text-weak">
            验证一次后，本次浏览期间无需重复验证；记录中的姓名与手机号将脱敏展示。
          </p>
        </div>
      )}

      {phase === 'list' && (
        <PullToRefresh onRefresh={handleRefresh}>
          <div className="mb-3 flex items-center justify-between gap-2.5 rounded-xl bg-accent-soft px-3.5 py-2.5 text-xs text-accent-ink">
            <span>
              当前查询：
              <strong className="font-bold">
                {identity.maskedName} {identity.maskedMobile}
              </strong>
            </span>
            <button
              type="button"
              onClick={() => {
                clearQuerySession()
                setRecords([])
                setPhase('verify')
              }}
              className="flex-none font-semibold underline"
            >
              换个手机号
            </button>
          </div>

          <div className="mb-3 flex gap-2 overflow-x-auto" role="tablist" aria-label="按状态筛选">
            {FILTERS.map((f, index) => (
              <button
                key={f.label}
                type="button"
                role="tab"
                aria-selected={filter === index}
                onClick={() => setFilter(index)}
                className={`min-h-8 flex-none rounded-full border px-3.5 text-[13px] ${
                  filter === index
                    ? 'border-brand bg-accent-soft font-semibold text-accent-ink'
                    : 'border-border-soft bg-white text-mid'
                }`}
              >
                {f.label}
              </button>
            ))}
          </div>

          {visible.length === 0 ? (
            <div className="py-8" role="status" aria-label="暂无申请记录">
              <ErrorBlock status="empty" title="暂无申请记录" description="" />
              <button
                type="button"
                onClick={() => router.push('/visitor')}
                className="mx-auto mt-4 flex h-11 w-40 items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white"
              >
                去预约
              </button>
            </div>
          ) : (
            <div className="flex flex-col gap-3 pb-6">
              {visible.map((record) => (
                <RecordCard
                  key={record.applyId}
                  record={record}
                  onClick={() => router.push(`/visitor/records/${record.applyId}`)}
                />
              ))}
            </div>
          )}
        </PullToRefresh>
      )}
    </PageShell>
  )
}

export default function RecordsPage() {
  return (
    <Suspense>
      <RecordsInner />
    </Suspense>
  )
}
