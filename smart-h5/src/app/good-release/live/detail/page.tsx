'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, ImageViewer, SpinLoading } from 'antd-mobile'
import Image from 'next/image'
import { useSearchParams } from 'next/navigation'
import { Suspense } from 'react'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getReleaseDetail } from '@/features/good-release/api'
import { InfoRow, QrPanel, ReleaseInfoCard, toImageSrc } from '@/features/good-release/detail-blocks'
import { qrPanelState, showReleaseInfo } from '@/features/good-release/release-status'

function GoodReleaseLiveDetailInner() {
  const authorized = useRequireAuth()
  const params = useSearchParams()
  const id = params.get('id') ?? ''

  const detail = useQuery({
    queryKey: ['good-release-detail', id],
    queryFn: () => getReleaseDetail(id),
    enabled: authorized && id !== '',
  })
  const info = detail.data?.code === 0 ? detail.data.data : undefined
  const failed = id === '' || detail.isError || (detail.isSuccess && detail.data.code !== 0)
  const photos = [info?.oneImg, info?.twoImg, info?.threeImg].filter((img): img is string => !!img)

  if (!authorized) return null

  return (
    <PageShell title="放行详情">
      {/* failed first: a disabled query (missing id) stays isPending forever */}
      {failed || (detail.isSuccess && !info) ? (
        <ErrorBlock
          status="default"
          title="加载失败"
          description={detail.data?.message ?? detail.data?.msg ?? '网络错误'}
        />
      ) : detail.isPending || !info ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          <QrPanel state={qrPanelState(info)} qrCodePic={info.qrCodePic} />

          {/* 申请信息 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <div className="flex items-center justify-between border-b border-border-soft py-3">
              <span className="text-lg font-bold">{info.carrier}</span>
              <div className="relative h-[72px] w-[72px] overflow-hidden rounded-xl bg-surface">
                {info.facePic ? (
                  <Image src={toImageSrc(info.facePic)} alt="人脸照片" fill unoptimized className="object-cover" />
                ) : (
                  <span className="grid h-full w-full place-items-center text-3xl" aria-hidden>
                    👤
                  </span>
                )}
              </div>
            </div>
            <InfoRow label="物品类型" value={info.articlesTypeName} />
            <InfoRow label="物品名称" value={info.articlesDesc} />
            <InfoRow label="房间信息" value={`${info.dormitoryName ?? ''}${info.roomName ?? ''}` || undefined} />
            <InfoRow label="离厂时间" value={info.plannedDepartureTime} />
            <InfoRow label="车牌号" value={info.licensePlate || undefined} />
            <InfoRow label="备注信息" value={info.remarks || undefined} />
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
            submitter={info.name ? { name: info.name } : undefined}
            nodes={info.approvalProcess ?? []}
          />

          {showReleaseInfo(info.status) && (
            <ReleaseInfoCard
              statusName={info.statusName}
              securityStaff={info.securityStaff}
              departureTime={info.departureTime}
              remark={info.remark}
            />
          )}
        </div>
      )}
    </PageShell>
  )
}

export default function GoodReleaseLiveDetailPage() {
  return (
    <Suspense>
      <GoodReleaseLiveDetailInner />
    </Suspense>
  )
}
