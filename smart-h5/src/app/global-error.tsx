'use client'
import { useEffect } from 'react'

// Next.js App Router 根级错误边界：捕获根布局自身的异常。
// 它会替换根 layout，因此必须自带 <html>/<body>；此时全局样式可能未生效，故用内联样式兜底。
// 约定见 https://nextjs.org/docs/app/api-reference/file-conventions/error#global-error
export default function GlobalError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  useEffect(() => {
    // 仅上报到控制台便于排查，不把原始 message 展示给用户（可能含敏感信息）。
    console.error(error)
  }, [error])

  return (
    <html lang="zh-CN">
      <body
        style={{
          margin: 0,
          minHeight: '100vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 16,
          padding: '32px 24px',
          fontFamily: 'system-ui, sans-serif',
          color: '#1f1f1f',
        }}
      >
        <h1 style={{ fontSize: 18, fontWeight: 700, margin: 0 }}>页面加载失败</h1>
        <p style={{ fontSize: 14, color: '#8a8a8a', margin: 0 }}>出了点小问题，请稍后重试</p>
        <button
          type="button"
          onClick={reset}
          style={{
            height: 44,
            width: 160,
            border: 'none',
            borderRadius: 14,
            background: '#ec6c00',
            color: '#fff',
            fontSize: 15,
            fontWeight: 600,
          }}
        >
          重试
        </button>
      </body>
    </html>
  )
}
