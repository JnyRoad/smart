'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, ImageViewer, SpinLoading } from 'antd-mobile'
import Image from 'next/image'
import { useSearchParams } from 'next/navigation'
import { Suspense } from 'react'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getRepairDetail } from '@/features/dorm-services/api'
import { RepairReplyList } from '@/features/dorm-services/repair-reply-list'
import { toImageSrc } from '@/features/good-release/detail-blocks'

function InfoRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null
  return (
    <div className="flex justify-between gap-4 border-b border-border-soft py-2.5 last:border-b-0">
      <span className="flex-none text-[13px] text-mid">{label}</span>
      <span className="text-right text-[13px] font-medium break-all">{value}</span>
    </div>
  )
}

function RepairDetailInner() {
  const authorized = useRequireAuth()
  const id = useSearchParams().get('id')

  const detail = useQuery({
    queryKey: ['repair-detail', id],
    queryFn: () => getRepairDetail(id as string),
    enabled: authorized && id !== null,
  })

  if (!authorized) return null

  const info = detail.data?.code === 0 ? detail.data.data : undefined

  return (
    <PageShell title="报修详情">
      {!id || (detail.isSuccess && !info) ? (
        <ErrorBlock
          status="default"
          description={detail.data?.message ?? '工单不存在或暂时无法查询'}
          className="py-16"
        />
      ) : detail.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : detail.isError ? (
        <ErrorBlock status="default" description="网络错误，请稍后重试" className="py-16" />
      ) : (
        <div className="flex flex-col gap-3">
          {/* 状态卡 */}
          <div className="flex items-center justify-between rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <span className="text-[15px] font-bold">{info?.statusDesc}</span>
            <span className="text-xs text-mid">{info?.createTime}</span>
          </div>

          {/* 报修信息卡 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <h2 className="pt-2 pb-1 text-[15px] font-bold">报修信息</h2>
            <InfoRow label="姓名" value={info?.name} />
            <InfoRow label="工号" value={info?.staffBadge} />
            <InfoRow label="BU" value={info?.compName} />
            <InfoRow label="部门" value={info?.depName} />
            <InfoRow label="维修区域" value={info?.rangeTypeDesc} />
            <InfoRow label="维修类别" value={info?.repairTypeDesc} />
            <InfoRow
              label="维修位置"
              value={info?.dormitoryName && `${info.dormitoryName}#${info.roomName ?? ''}`}
            />
            <InfoRow label="所在园区" value={info?.parkName} />
            <InfoRow label="故障描述" value={info?.faultDesc} />
            {(info?.faultImgs?.length ?? 0) > 0 && (
              <div className="flex gap-2.5 py-2.5">
                {info?.faultImgs?.slice(0, 3).map((img, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() =>
                      ImageViewer.Multi.show({ images: (info.faultImgs ?? []).map(toImageSrc), defaultIndex: index })
                    }
                    className="relative h-[72px] w-[72px] overflow-hidden rounded-xl"
                  >
                    <Image src={toImageSrc(img)} alt="" fill unoptimized className="object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          <ApprovalTimeline
            submitter={info?.name ? { name: info.name, time: info.createTime } : undefined}
            nodes={info?.approvalProcess ?? []}
          />

          {/* 维修结果区 */}
          <RepairReplyList replies={info?.repairReplyList} />
        </div>
      )}
    </PageShell>
  )
}

export default function RepairDetailPage() {
  return (
    <Suspense>
      <RepairDetailInner />
    </Suspense>
  )
}
