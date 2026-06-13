'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, ImageViewer, SpinLoading } from 'antd-mobile'
import Image from 'next/image'
import { useSearchParams } from 'next/navigation'
import { Suspense } from 'react'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { FaceAvatar } from '@/components/face-avatar'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getDormExitDetail } from '@/features/dorm-services/api'

function InfoRow({ label, value }: { label: string; value?: React.ReactNode }) {
  if (!value) return null
  return (
    <div className="flex justify-between gap-4 border-b border-border-soft py-2.5 last:border-b-0">
      <span className="flex-none text-[13px] text-mid">{label}</span>
      <span className="min-w-0 text-right text-[13px] font-medium break-all">{value}</span>
    </div>
  )
}

function DormExitDetailInner() {
  const authorized = useRequireAuth()
  const id = useSearchParams().get('id')

  const detail = useQuery({
    queryKey: ['dorm-exit-detail', id],
    queryFn: () => getDormExitDetail(id as string),
    enabled: authorized && !!id,
  })

  if (!authorized) return null
  const info = detail.data?.code === 0 ? detail.data.data : undefined

  return (
    <PageShell title="退宿申请详情">
      {!id || (detail.isSuccess && !info) ? (
        <ErrorBlock
          status="default"
          description={detail.data?.message ?? '申请不存在或暂时无法查询'}
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
          {/* 放行码 / 出厂状态区（按 status） */}
          {info?.status === 2 && !info.qrCode && (
            <div className="rounded-2xl bg-white p-5 text-center text-sm text-mid shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
              审批已通过，放行码生成中，请稍后刷新
            </div>
          )}
          {info?.status === 2 && info.qrCode && (
            <div className="flex flex-col items-center rounded-2xl bg-white p-6 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
              <Image
                src={`data:image/jpeg;base64,${info.qrCode}`}
                alt="退宿放行二维码"
                width={192}
                height={192}
                unoptimized
                className="h-48 w-48"
              />
              <p className="mt-3 text-sm text-mid">在门卫处出示放行码</p>
            </div>
          )}
          {info?.status === 4 && (
            <div className="flex flex-col items-center rounded-2xl bg-white p-6 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
              <span aria-hidden className="text-5xl">
                ✅
              </span>
              <p className="mt-2 text-[15px] font-bold">已出厂</p>
              <p className="mt-1 text-sm text-mid">已同意出厂</p>
            </div>
          )}
          {info?.status === 5 && (
            <div className="flex flex-col items-center rounded-2xl bg-white p-6 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
              <span aria-hidden className="text-5xl">
                🚫
              </span>
              <p className="mt-2 text-[15px] font-bold text-[#d83b36]">已拒绝出厂</p>
            </div>
          )}

          {/* 信息卡 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <div className="flex items-center gap-3 border-b border-border-soft py-3">
              <FaceAvatar faceId={info?.faceId} />
              <span className="text-[15px] font-bold">{info?.name}</span>
            </div>
            <InfoRow
              label="房间信息"
              value={
                info?.dorDetailStr?.length
                  ? info.dorDetailStr.map((line, i) => (
                      <span key={i} className="block">
                        {line}
                      </span>
                    ))
                  : undefined
              }
            />
            <InfoRow label="退宿原因" value={info?.quitReasonDesc} />
            <InfoRow label="预计离开时间" value={info?.applyLeaveTime} />
            <InfoRow label="备注" value={info?.remark} />
            {(info?.imgs?.length ?? 0) > 0 && (
              <div className="flex gap-2.5 py-2.5">
                {info?.imgs?.slice(0, 3).map((img, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => ImageViewer.Multi.show({ images: info.imgs ?? [], defaultIndex: index })}
                    className="relative h-[72px] w-[72px] overflow-hidden rounded-xl"
                  >
                    <Image src={img} alt="" fill unoptimized className="object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          <ApprovalTimeline
            submitter={info?.name ? { name: info.name, time: info.createTime } : undefined}
            nodes={info?.approvalProcess ?? []}
          />
        </div>
      )}
    </PageShell>
  )
}

export default function DormExitDetailPage() {
  return (
    <Suspense>
      <DormExitDetailInner />
    </Suspense>
  )
}
