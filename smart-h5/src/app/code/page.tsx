'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, ImageViewer, SpinLoading } from 'antd-mobile'
import Image from 'next/image'
import { useSearchParams } from 'next/navigation'
import { Suspense } from 'react'
import { ApprovalTimeline } from '@/components/approval-timeline'
import { StatusIcon, type StatusIconName } from '@/components/app-icon'
import { PageShell } from '@/components/page-shell'
import { codePanelState, type CodePanelState } from '@/features/backlog/code-status'
import { getReleaseDetailForCode } from '@/features/backlog/api'
import { InfoRow, toImageSrc } from '@/features/good-release/detail-blocks'

const PANEL_COPY: Record<Exclude<CodePanelState, 'qr' | 'none'>, { iconName: StatusIconName; iconClass: string; text: string; danger?: boolean }> = {
  expired: { iconName: 'expired', iconClass: 'text-[#d83b36]', text: '放行码已过期', danger: true },
  reviewing: { iconName: 'pending', iconClass: 'text-mid', text: '放行码仍在审批中，请稍后' },
  rejected: { iconName: 'denied', iconClass: 'text-[#d83b36]', text: '审核失败', danger: true },
  left: { iconName: 'success', iconClass: 'text-[#16a673]', text: '已出厂' },
  denied: { iconName: 'denied', iconClass: 'text-[#d83b36]', text: '拒绝放行', danger: true },
}

/** 免登录放行条（门岗扫码/核验入口；query id）。 */
function CodePageInner() {
  const params = useSearchParams()
  const id = params.get('id') ?? ''

  const detail = useQuery({
    queryKey: ['code-release-detail', id],
    queryFn: () => getReleaseDetailForCode(id),
    enabled: id !== '',
  })
  const info = detail.data?.code === 0 ? detail.data.data : undefined
  const failed = id === '' || detail.isError || (detail.isSuccess && (detail.data.code !== 0 || !detail.data.data))
  const state = info ? codePanelState(info) : 'none'
  const photos = [info?.oneImg, info?.twoImg, info?.threeImg].filter((img): img is string => !!img)
  const dormLocation =
    info?.dormitoryName != null
      ? `${info.dormitoryName}-${info.floorName ?? ''}层-${info.roomName ?? ''}号房-${info.bedName ?? ''}床`
      : undefined

  return (
    <PageShell title="放行条">
      {failed ? (
        <ErrorBlock
          status="default"
          title="加载失败"
          description={detail.data?.message || detail.data?.msg || '网络错误'}
        />
      ) : detail.isPending || !info ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {/* 码区五态 */}
          <div className="flex flex-col items-center rounded-2xl bg-white p-6 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            {state === 'qr' ? (
              <>
                <div className="relative h-48 w-48">
                  <Image
                    src={`data:image/jpg;base64,${info.qrCodePic ?? ''}`}
                    alt="放行二维码"
                    fill
                    unoptimized
                    className="object-contain"
                  />
                </div>
                <p className="mt-2 text-sm text-mid">扫描放行码以识别备案物进行放行</p>
              </>
            ) : state !== 'none' ? (
              <>
                <StatusIcon
                  name={PANEL_COPY[state].iconName}
                  className={`h-12 w-12 ${PANEL_COPY[state].iconClass}`}
                />
                <p className={`mt-2 text-[15px] font-bold ${PANEL_COPY[state].danger ? 'text-[#d83b36]' : ''}`}>
                  {PANEL_COPY[state].text}
                </p>
              </>
            ) : null}
          </div>

          {/* 携带人 */}
          <div className="flex items-center justify-between rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <span className="text-lg font-bold">{info.carrier}</span>
            {info.facePic && (
              <div className="relative h-[72px] w-[72px] overflow-hidden rounded-xl">
                <Image src={toImageSrc(info.facePic)} alt="人脸照片" fill unoptimized className="object-cover" />
              </div>
            )}
          </div>

          {/* 单据信息 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <InfoRow label="园区" value={info.parkName} />
            <InfoRow
              label="物品类型"
              value={
                info.articlesTypeName ? (
                  <span className="rounded-full bg-accent-soft px-2.5 py-0.5 text-xs font-semibold text-accent-ink">
                    {info.articlesTypeName}
                  </span>
                ) : undefined
              }
            />
            {dormLocation && <InfoRow label="宿舍位置" value={dormLocation} />}
            <InfoRow label="预计离厂日期" value={info.plannedDepartureTime} />
            <InfoRow label="物品描述" value={info.remarks || undefined} />
            {photos.length > 0 && (
              <div className="flex gap-2.5 py-2.5">
                {photos.map((img, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => ImageViewer.Multi.show({ images: photos.map(toImageSrc), defaultIndex: index })}
                    className="relative h-[72px] w-[72px] overflow-hidden rounded-xl"
                  >
                    <Image src={toImageSrc(img)} alt="物品照片" fill unoptimized className="object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* 状态卡 */}
          <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
            <InfoRow label="状态" value={info.statusName || '-'} />
            {info.status === 5 && <InfoRow label="拒绝原因" value={info.remark} />}
            {(info.status === 4 || info.status === 5) && (
              <InfoRow label="放行人员" value={info.securityStaff || '-'} />
            )}
            {info.status === 4 && <InfoRow label="离厂时间" value={info.departureTime || '-'} />}
          </div>

          {/* 流转记录 */}
          {(info.approvalProcess?.length ?? 0) > 0 && (
            <div className="relative">
              {info.phone && (
                <a
                  href={`tel:${info.phone}`}
                  className="absolute top-3 right-4 z-10 text-[13px] font-semibold text-brand"
                >
                  电话联系
                </a>
              )}
              <ApprovalTimeline
                submitter={info.name ? { name: info.name } : undefined}
                nodes={info.approvalProcess ?? []}
              />
            </div>
          )}
        </div>
      )}
    </PageShell>
  )
}

export default function CodePage() {
  return (
    <Suspense>
      <CodePageInner />
    </Suspense>
  )
}
