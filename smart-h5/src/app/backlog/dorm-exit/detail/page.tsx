'use client'
import { useQuery } from '@tanstack/react-query'
import { ImageViewer, SpinLoading, TextArea, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { FaceAvatar } from '@/components/face-avatar'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { updateExitStatus } from '@/features/backlog/api'
import { exitProcessNodes } from '@/features/backlog/exit-process'
import { getDormExitDetail, getDormExitScanDetail } from '@/features/dorm-services/api'
import { confirmIrreversible } from '@/lib/confirm-irreversible'

function InfoRow({ label, value }: { label: string; value?: React.ReactNode }) {
  if (!value) return null
  return (
    <div className="flex justify-between gap-4 border-b border-border-soft py-2.5 last:border-b-0">
      <span className="flex-none text-[13px] text-mid">{label}</span>
      <span className="min-w-0 text-right text-[13px] font-medium break-all">{value}</span>
    </div>
  )
}

function DormExitApprovalDetailInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const id = params.get('id') ?? ''
  const readOnly = params.get('tab') === 'done'
  const fromScan = params.get('scan') === '1'
  const [remark, setRemark] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined

  const detail = useQuery({
    queryKey: ['exit-detail', fromScan ? 'scan' : 'normal', id],
    queryFn: () => (fromScan ? getDormExitScanDetail(id) : getDormExitDetail(id)),
    enabled: authorized && id !== '',
  })
  const info = detail.data?.code === 0 ? detail.data.data : undefined
  const failed = id === '' || detail.isError || (detail.isSuccess && (detail.data.code !== 0 || !detail.data.data))

  const warnedRef = useRef(false)
  useEffect(() => {
    if (failed && !warnedRef.current) {
      warnedRef.current = true
      Toast.show(detail.data?.message || '网络错误')
      router.replace('/home')
    }
  }, [failed, detail.data?.message, router])

  // 审批模式 = isApprove === true 且非只读 Tab；isApprove 缺失/false 默认只读（旧版查看模式）。
  const approveMode = !readOnly && (fromScan || info?.isApprove === true)
  const status = info?.status
  const photos = (info?.imgs ?? []).filter((img): img is string => !!img).slice(0, 3)

  async function submit(nextStatus: 2 | 3 | 4 | 5) {
    if (submitting) return
    if (!badge) return Toast.show('获取用户信息失败！')
    if (!info?.id) return Toast.show('获取退宿单据失败')
    // 退宿审批通过/拒绝不可撤销，执行前二次确认（3/5 为拒绝，2/4 为通过）。
    const isReject = nextStatus === 3 || nextStatus === 5
    const message = isReject ? '确定拒绝该退宿申请？拒绝后不可撤销' : '确定通过该退宿申请？通过后不可撤销'
    if (!(await confirmIrreversible(message))) return
    setSubmitting(true)
    try {
      const res = await updateExitStatus({ id: info.id, status: nextStatus, approveBadge: badge, remark })
      if (res.code === 0 && res.data) {
        router.replace(fromScan ? '/home' : '/backlog/dorm-exit?tab=done')
      } else {
        Toast.show(res.message || res.msg || '网络错误')
      }
    } catch (error) {
      Toast.show(toUserMessage(error, '网络错误'))
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized) return null

  return (
    <PageShell title="退宿审批">
      {detail.isPending || failed || !info ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col gap-3 pb-24">
          {/* 申请人卡 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <div className="flex items-center gap-3 border-b border-border-soft py-3">
              <FaceAvatar faceId={info.faceId} />
              <span className="text-[15px] font-bold">{info.name}</span>
            </div>
            <InfoRow
              label="房间信息"
              value={info.dorDetailStr?.map((line, i) => (
                <span key={i} className="block">
                  {line}
                </span>
              ))}
            />
            <InfoRow label="退宿原因" value={info.quitReasonDesc} />
            <InfoRow label="预计离开时间" value={info.applyLeaveTime} />
            <InfoRow label="备注" value={info.remark} />
            {photos.length > 0 && (
              <div className="flex gap-2.5 py-2.5">
                {photos.map((img, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => ImageViewer.Multi.show({ images: photos, defaultIndex: index })}
                    className="relative h-[72px] w-[72px] overflow-hidden rounded-xl"
                  >
                    <Image src={img} alt="照片" fill unoptimized className="object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          <ApprovalTimeline
            submitter={info.name ? { name: info.name, time: info.createTime } : undefined}
            nodes={exitProcessNodes(info)}
          />

          {approveMode && (
            <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
              <p className="mb-2 text-[13px] font-semibold text-mid">描述(非必填)</p>
              <div className="rounded-xl border border-border-soft px-3 py-2">
                <TextArea placeholder="请输入内容" value={remark} onChange={setRemark} rows={2} maxLength={50} />
              </div>
            </div>
          )}

          {approveMode && status === 1 && (
            <div className="fixed inset-x-0 bottom-0 flex gap-3 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submit(3)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] border border-[#d83b36] text-base font-semibold text-[#d83b36] disabled:opacity-60"
              >
                拒绝
              </button>
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submit(2)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
              >
                通过
              </button>
            </div>
          )}
          {approveMode && status === 2 && (
            <div className="fixed inset-x-0 bottom-0 flex gap-3 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submit(5)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] border border-[#d83b36] text-base font-semibold text-[#d83b36] disabled:opacity-60"
              >
                拒绝
              </button>
              <button
                type="button"
                disabled={submitting}
                onClick={() => void submit(4)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
              >
                保安通过
              </button>
            </div>
          )}
        </div>
      )}
    </PageShell>
  )
}

export default function DormExitApprovalDetailPage() {
  return (
    <Suspense>
      <DormExitApprovalDetailInner />
    </Suspense>
  )
}
