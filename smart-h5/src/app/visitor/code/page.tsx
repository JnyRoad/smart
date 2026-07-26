'use client'

import { ErrorBlock, SpinLoading } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect } from 'react'
import { visitorRecordAccessPath } from '@/features/visitor/pass-code-access'

/**
 * 历史短信通行码链接只含申请 id，不能再直接换取预约详情或二维码。进入后统一要求
 * 手机短信校验，避免顺序 id 被枚举造成访客、接待人和通行码泄露。
 */
function PassCodeInner() {
  const router = useRouter()
  const id = useSearchParams().get('id') ?? ''
  const recordPath = visitorRecordAccessPath(id)

  useEffect(() => {
    if (recordPath) {
      router.replace(recordPath)
    }
  }, [recordPath, router])

  if (!recordPath) {
    return <ErrorBlock status="empty" description="缺少申请单信息" className="py-16" />
  }
  return (
    <div className="flex justify-center py-24">
      <SpinLoading color="primary" />
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
