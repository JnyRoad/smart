'use client'
import { Result } from 'antd-mobile'
import Link from 'next/link'
import { useRouter } from 'next/navigation'

/** Standard visitor application success page. */
export default function VisitorResultPage() {
  const router = useRouter()
  return (
    <div className="flex min-h-dvh flex-col items-center justify-center px-6">
      <Result
        status="success"
        title="已发送成功，等待被访对象审批"
        description="审批通过后，我们会以短信或公众号的方式通知您，请注意查看"
      />
      <button
        type="button"
        onClick={() => router.push('/visitor/records')}
        className="mt-4 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
      >
        查看审批进度
      </button>
      <Link href="/visitor" className="mt-4 text-[15px] font-semibold text-brand">
        再预约一次
      </Link>
    </div>
  )
}
