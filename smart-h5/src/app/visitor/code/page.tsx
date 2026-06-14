'use client'
import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useSearchParams } from 'next/navigation'
import { Suspense, useEffect } from 'react'
import { getApplyDetail, getFactoryTypeEnum, type ApplyDetail } from '@/features/visitor/api'
import { buildPassCodeAreaDisplay } from '@/features/visitor/pass-code-areas'
import { getTenantConfig } from '@/lib/config/tenant'

function InfoRow({ label, value }: { label: string; value?: string }) {
  if (!value) return null
  return (
    <div className="flex justify-between gap-4 border-b border-border-soft py-2.5 last:border-b-0">
      <span className="flex-none text-[13px] text-mid">{label}</span>
      <span className="text-right text-[13px] font-medium break-all">{value}</span>
    </div>
  )
}

function PassCodeInner() {
  const id = useSearchParams().get('id')
  const config = getTenantConfig()

  const detail = useQuery({
    queryKey: ['visitor', 'apply-detail', id],
    queryFn: () => getApplyDetail(id as string),
    enabled: id !== null,
  })
  const newEnum = useQuery({
    queryKey: ['factory-enum', 1],
    queryFn: () => getFactoryTypeEnum(1),
    enabled: id !== null,
  })
  const oldEnum = useQuery({
    queryKey: ['factory-enum', 0],
    queryFn: () => getFactoryTypeEnum(0),
    enabled: id !== null,
  })

  useEffect(() => {
    if (detail.isError) Toast.show(detail.error.message || '网络错误')
  }, [detail.isError, detail.error])

  if (!id) {
    return <ErrorBlock status="empty" description="缺少申请单信息" className="py-16" />
  }
  if (detail.isPending) {
    return (
      <div className="flex justify-center py-24">
        <SpinLoading color="primary" />
      </div>
    )
  }
  if (detail.isError) {
    return <ErrorBlock status="default" description="网络错误，请稍后重试" className="py-16" />
  }
  // Business failure: surface the gateway message instead of a generic state.
  if (detail.data && detail.data.code !== 0) {
    return (
      <ErrorBlock
        status="default"
        description={detail.data.message ?? '申请单查询失败'}
        className="py-16"
      />
    )
  }
  const info: ApplyDetail | undefined = detail.data?.data
  if (!info) {
    return <ErrorBlock status="default" description="申请单不存在" className="py-16" />
  }

  const delFlag = info.delFlag ?? 0
  // delFlag=0 without a QR payload is a data anomaly, not a valid pass.
  const qrValid = delFlag === 0 && Boolean(info.qrCode)
  const { newAreas, oldAreas } = buildPassCodeAreaDisplay(
    info,
    newEnum.data?.data ?? [],
    oldEnum.data?.data ?? [],
  )

  return (
    <div className="flex flex-col gap-3 px-3 pb-8">
      <h1 className="pt-4 text-center text-lg font-bold">
        {info.parkName ?? config.parkName}欢迎您
      </h1>

      {/* 二维码卡片 */}
      <div className="flex flex-col items-center rounded-2xl bg-white p-6 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        {qrValid && info.qrCode ? (
          <>
            <Image
              src={`data:image/png;base64,${info.qrCode}`}
              alt="访客通行二维码"
              width={192}
              height={192}
              unoptimized
              className="h-48 w-48"
            />
            {info.smsCode && (
              <p className="mt-3 text-xl font-bold tracking-widest">{info.smsCode}</p>
            )}
            <span className="mt-2 rounded-full bg-accent-soft px-3 py-1 text-xs font-semibold text-accent-ink">
              首次扫码，打印有效
            </span>
            <p className="mt-3 text-center text-xs leading-5 text-mid">
              请截图保存该页面，入园接受检查时可出此凭证，以便核实
            </p>
          </>
        ) : (
          <>
            <div className="grid h-48 w-48 place-items-center rounded-xl bg-surface text-5xl text-weak" aria-hidden>
              ✕
            </div>
            <p className="mt-3 text-[15px] font-bold text-[#d83b36]">二维码已失效</p>
          </>
        )}
      </div>

      {/* 预约信息卡（delFlag 0/1 显示） */}
      {delFlag !== 2 && (
        <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
          <InfoRow label="园区" value={info.parkName} />
          <InfoRow label="来访事由" value={info.causeDesc} />
          <InfoRow
            label="被访人"
            value={
              info.receptionistName &&
              `${info.receptionistName} ${info.receptionistPhone ?? ''}（被访人）`
            }
          />
          <InfoRow
            label="访客"
            value={info.visitorName && `${info.visitorName} ${info.visitorPhone ?? ''}（访客）`}
          />
          <InfoRow label="区域类型" value={info.permitFactoryTypeDesc} />
          <InfoRow label="新工厂" value={newAreas} />
          <InfoRow label="老工厂" value={oldAreas} />
          <InfoRow label="预约到访时间" value={info.startTime} />
          <InfoRow label="预约离开时间" value={info.endTime} />
        </div>
      )}

      {/* 使用指引（仅有效态） */}
      {qrValid && (
        <div className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
          <h2 className="mb-3 text-[15px] font-bold">使用指引</h2>
          <div className="grid grid-cols-3 gap-2 text-center">
            {(
              [
                ['01', '填写预约码'],
                ['02', '打印凭条'],
                ['03', '粘贴至胸前'],
              ] as const
            ).map(([step, label]) => (
              <div key={step} className="rounded-xl bg-surface p-3">
                <span className="text-lg font-bold text-brand">{step}</span>
                <p className="mt-1 text-xs text-mid">{label}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      <p className="mt-2 text-center text-xs text-weak">裕同智慧园区</p>
    </div>
  )
}

export default function PassCodePage() {
  return (
    <Suspense>
      <PassCodeInner />
    </Suspense>
  )
}
