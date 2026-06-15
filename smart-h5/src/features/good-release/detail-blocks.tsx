'use client'
import Image from 'next/image'
import type { ReactNode } from 'react'
import { StatusIcon } from '@/components/app-icon'
import type { QrPanelState } from './release-status'

export function InfoRow({ label, value }: { label: string; value?: ReactNode }) {
  return (
    <div className="flex justify-between gap-3 border-b border-border-soft py-2.5 text-[13px] last:border-b-0">
      <span className="flex-none text-mid">{label}</span>
      <span className="min-w-0 text-right">{value ?? '无'}</span>
    </div>
  )
}

/** 图片字段以 base64/URL 直出；缺 data: 前缀时补齐以便 <img> 渲染。 */
export function toImageSrc(value: string): string {
  if (value.startsWith('data:') || value.startsWith('http') || value.startsWith('/')) return value
  return `data:image/jpg;base64,${value}`
}

/** 放行码三态卡（expired / left / qr；none 时不渲染）。 */
export function QrPanel({
  state,
  qrCodePic,
  tip = '在门卫处出示放行码',
}: {
  state: QrPanelState
  qrCodePic?: string
  tip?: string
}) {
  if (state === 'none') return null
  return (
    <div className="flex flex-col items-center rounded-2xl bg-white p-6 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
      {state === 'expired' && (
        <>
          <StatusIcon name="expired" className="h-12 w-12 text-[#d83b36]" />
          <p className="mt-2 text-[15px] font-bold text-[#d83b36]">放行码已过期</p>
        </>
      )}
      {state === 'left' && (
        <>
          <StatusIcon name="success" className="h-12 w-12 text-[#16a673]" />
          <p className="mt-2 text-[15px] font-bold">已出厂</p>
        </>
      )}
      {state === 'qr' && qrCodePic && (
        <>
          <div className="relative h-48 w-48">
            <Image
              src={`data:image/jpg;base64,${qrCodePic}`}
              alt="放行二维码"
              fill
              unoptimized
              className="object-contain"
            />
          </div>
          <p className="mt-2 text-sm text-mid">【温馨提示】{tip}</p>
        </>
      )}
    </div>
  )
}

/** status 4/5 的出厂放行信息卡。 */
export function ReleaseInfoCard({
  statusName,
  securityStaff,
  departureTime,
  remark,
}: {
  statusName?: string
  securityStaff?: string
  departureTime?: string
  remark?: string
}) {
  return (
    <div className="rounded-2xl bg-white px-4 py-2 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
      <p className="border-b border-border-soft py-3 text-[15px] font-bold">放行信息</p>
      <InfoRow label="状态" value={statusName} />
      <InfoRow label="放行人员" value={securityStaff} />
      <InfoRow label="离场时间" value={departureTime} />
      <InfoRow label="备注" value={remark} />
    </div>
  )
}
