'use client'
import { ErrorBlock } from 'antd-mobile'
import { useEffect } from 'react'

// Next.js App Router 段级错误边界：捕获子树客户端渲染异常并展示降级 UI。
// 约定见 https://nextjs.org/docs/app/api-reference/file-conventions/error
export default function AppError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  useEffect(() => {
    // 仅上报到控制台便于排查，不把原始 message 展示给用户（可能含敏感信息）。
    console.error(error)
  }, [error])

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center px-6 py-8">
      <ErrorBlock status="default" title="页面加载失败" description="出了点小问题，请稍后重试" />
      <button
        type="button"
        onClick={reset}
        className="mx-auto mt-4 flex h-11 w-40 items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white"
      >
        重试
      </button>
    </div>
  )
}
