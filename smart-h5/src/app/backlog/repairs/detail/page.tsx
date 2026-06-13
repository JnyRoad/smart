'use client'
import { useQuery } from '@tanstack/react-query'
import { ImageViewer, SpinLoading, TextArea, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { replyRepair, updateRepairStatus } from '@/features/backlog/api'
import { getRepairDetail } from '@/features/dorm-services/api'
import { repairDetailPhotos } from '@/features/dorm-services/repair-detail'
import { RepairReplyList } from '@/features/dorm-services/repair-reply-list'
import { toImageSrc } from '@/features/good-release/detail-blocks'

function InfoRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null
  return (
    <div className="flex justify-between gap-4 border-b border-border-soft py-2.5 last:border-b-0">
      <span className="flex-none text-[13px] text-mid">{label}</span>
      <span className="min-w-0 text-right text-[13px] font-medium break-all">{value}</span>
    </div>
  )
}

function RepairApprovalDetailInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const id = params.get('id') ?? ''
  const readOnly = params.get('tab') === 'done'
  const [remark, setRemark] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined

  const detail = useQuery({
    queryKey: ['repair-detail', id],
    queryFn: () => getRepairDetail(id),
    enabled: authorized && id !== '',
  })
  const info = detail.data?.code === 0 ? detail.data.data : undefined
  const failed = id === '' || detail.isError || (detail.isSuccess && (detail.data.code !== 0 || !detail.data.data))

  // 旧版报修审批详情失败只 toast 留页（区别于 exit/release 的回 /home）。
  const warnedRef = useRef(false)
  useEffect(() => {
    if (failed && !warnedRef.current) {
      warnedRef.current = true
      Toast.show(detail.data?.message || '网络错误')
    }
  }, [failed, detail.data?.message])

  const photos = repairDetailPhotos(info)

  async function acceptOrDecline(nextStatus: 1 | 2) {
    if (submitting) return
    if (!badge) return Toast.show('获取用户信息失败！')
    setSubmitting(true)
    try {
      const res = await updateRepairStatus({ approveBadge: badge, id, status: nextStatus, remark })
      if (res.code === 0 && res.data) {
        // 接单/不接单后回「待我审批的」（旧 curTabIndex=0）。
        router.replace('/backlog/repairs')
      } else {
        Toast.show(res.message || res.msg || '网络错误')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '网络错误')
    } finally {
      setSubmitting(false)
    }
  }

  async function reply(nextStatus: 3 | 4) {
    if (submitting) return
    if (!badge) return Toast.show('获取用户信息失败！')
    setSubmitting(true)
    try {
      const res = await replyRepair({ approveBadge: badge, id, result: remark, status: nextStatus })
      if (res.code === 0 && res.data) {
        router.replace('/backlog/repairs?tab=done')
      } else {
        Toast.show(res.message || res.msg || '网络错误')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '网络错误')
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized) return null

  const status = info?.status

  return (
    <PageShell title="报修审批">
      {failed ? (
        <p className="py-16 text-center text-sm text-[#d83b36]">{detail.data?.message || '网络错误'}</p>
      ) : detail.isPending || !info ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col gap-3 pb-24">
          {/* 状态卡 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <InfoRow label="状态" value={info.statusDesc} />
            <InfoRow label="报修时间" value={info.createTime} />
          </div>

          {/* 报修信息卡 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <InfoRow label="姓名" value={info.name} />
            <InfoRow label="工号" value={info.staffBadge} />
            <InfoRow label="BU" value={info.compName} />
            <InfoRow label="部门" value={info.depName} />
            <InfoRow label="维修区域" value={info.rangeTypeDesc} />
            <InfoRow label="维修类别" value={info.repairTypeDesc} />
            <InfoRow
              label="维修位置"
              value={info.dormitoryName ? `${info.dormitoryName}#${info.roomName ?? ''}` : undefined}
            />
            <InfoRow label="所在园区" value={info.parkName} />
            <InfoRow label="故障描述" value={info.faultDesc} />
            {photos.length > 0 && (
              <div className="flex gap-2.5 py-2.5">
                {photos.map((img, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() =>
                      ImageViewer.Multi.show({ images: photos.map(toImageSrc), defaultIndex: index })
                    }
                    className="relative h-[72px] w-[72px] overflow-hidden rounded-xl"
                  >
                    <Image src={toImageSrc(img)} alt="物品照片" fill unoptimized className="object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          <ApprovalTimeline
            submitter={info.name ? { name: info.name, time: info.createTime } : undefined}
            nodes={info.approvalProcess ?? []}
          />

          {!readOnly && (
            <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
              <p className="mb-2 text-[13px] font-semibold text-mid">描述(非必填)</p>
              <div className="rounded-xl border border-border-soft px-3 py-2">
                <TextArea placeholder="请输入内容" value={remark} onChange={setRemark} rows={2} maxLength={100} />
              </div>
            </div>
          )}

          {readOnly && <RepairReplyList replies={info.repairReplyList} />}

          {!readOnly && status === 0 && (
            <div className="fixed inset-x-0 bottom-0 flex gap-3 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
              <button
                type="button"
                disabled={submitting}
                onClick={() => void acceptOrDecline(2)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] border border-[#d83b36] text-base font-semibold text-[#d83b36] disabled:opacity-60"
              >
                不接单
              </button>
              <button
                type="button"
                disabled={submitting}
                onClick={() => void acceptOrDecline(1)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
              >
                接单
              </button>
            </div>
          )}
          {!readOnly && status === 1 && (
            <div className="fixed inset-x-0 bottom-0 flex gap-3 bg-white p-3 shadow-[0_-6px_18px_rgba(89,87,87,0.08)]">
              <button
                type="button"
                disabled={submitting}
                onClick={() => void reply(4)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] border border-[#d83b36] text-base font-semibold text-[#d83b36] disabled:opacity-60"
              >
                无法维修
              </button>
              <button
                type="button"
                disabled={submitting}
                onClick={() => void reply(3)}
                className="flex h-12 flex-1 items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
              >
                已安排维修
              </button>
            </div>
          )}
        </div>
      )}
    </PageShell>
  )
}

export default function RepairApprovalDetailPage() {
  return (
    <Suspense>
      <RepairApprovalDetailInner />
    </Suspense>
  )
}
