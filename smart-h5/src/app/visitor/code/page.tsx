'use client'

import { useQuery } from '@tanstack/react-query'
import { ErrorBlock, SpinLoading } from 'antd-mobile'
import Image from 'next/image'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect } from 'react'
import { StatusIcon } from '@/components/app-icon'
import {
  clearQuerySession,
  fetchVisitorPassCode,
  getQuerySession,
  isAuthRejected,
} from '@/features/visitor/records-api'
import { visitorRecordAccessPath } from '@/features/visitor/pass-code-access'
import { useMounted } from '@/lib/use-mounted'

/**
 * 通行码页面只读取由 queryToken 保护的最小二维码数据。URL 中的 applyId 只是定位符，
 * 未完成短信验证或非本人的请求一律回到身份验证页，绝不请求遗留裸详情接口。
 */
function PassCodeInner() {
  const router = useRouter()
  const mounted = useMounted()
  const id = useSearchParams().get('id') ?? ''
  const recordPath = visitorRecordAccessPath(id)
  const hasSession = mounted && getQuerySession() !== null
  const passCode = useQuery({
    queryKey: ['visitor-pass-code', id],
    queryFn: () => fetchVisitorPassCode(id),
    enabled: hasSession && Boolean(id),
  })
  const rejected = passCode.error !== null
    ? isAuthRejected(passCode.error)
    : passCode.data !== undefined && isAuthRejected(passCode.data)

  useEffect(() => {
    if (mounted && (!hasSession || rejected) && recordPath) {
      if (rejected) clearQuerySession()
      router.replace(recordPath)
    }
  }, [mounted, hasSession, rejected, recordPath, router])

  if (!id || !recordPath) {
    return <ErrorBlock status="empty" description="缺少申请单信息" className="py-16" />
  }
  if (!mounted || !hasSession || passCode.isPending) {
    return <div className="flex justify-center py-24"><SpinLoading color="primary" /></div>
  }
  if (rejected) return null
  if (passCode.isError || !passCode.data || passCode.data.code !== 0) {
    return <ErrorBlock status="default" description={passCode.data?.message ?? '通行码暂时无法获取'} className="py-16" />
  }
  const info = passCode.data.data
  if (!info || !info.valid || !info.qrCode) {
    return (
      <div className="flex flex-col items-center gap-3 py-20">
        <div className="grid h-48 w-48 place-items-center rounded-xl bg-surface text-weak" aria-hidden>
          <StatusIcon name="close" className="h-14 w-14" />
        </div>
        <p className="text-[15px] font-bold text-[#d83b36]">二维码已失效</p>
      </div>
    )
  }
  return (
    <div className="flex flex-col gap-3 px-3 pb-8">
      <h1 className="pt-4 text-center text-lg font-bold">访客入园通行码</h1>
      <div className="flex flex-col items-center rounded-2xl bg-white p-6 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <Image src={`data:image/png;base64,${info.qrCode}`} alt="访客通行二维码" width={192} height={192} unoptimized className="h-48 w-48" />
        {info.smsCode && <p className="mt-3 text-xl font-bold tracking-widest">{info.smsCode}</p>}
        <span className="mt-2 rounded-full bg-accent-soft px-3 py-1 text-xs font-semibold text-accent-ink">首次扫码，打印有效</span>
        <p className="mt-3 text-center text-xs leading-5 text-mid">请在入园时向门岗出示本通行码。</p>
      </div>
    </div>
  )
}

export default function PassCodePage() {
  return <Suspense><PassCodeInner /></Suspense>
}
