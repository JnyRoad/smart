'use client'
import { Result } from 'antd-mobile'
import Link from 'next/link'

/** Truck booking success page (system-approved, unlike the standard flow). */
export default function TruckResultPage() {
  return (
    <div className="flex min-h-dvh flex-col items-center justify-center px-6">
      <Result
        status="success"
        title="已发送成功，等待系统审批"
        description="审批通过后，我们会以短信或公众号的方式通知您，请注意查看"
      />
      <Link href="/visitor/truck" className="mt-4 text-[15px] font-semibold text-brand">
        再预约一次
      </Link>
    </div>
  )
}
