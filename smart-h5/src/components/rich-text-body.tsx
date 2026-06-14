'use client'
import { ImageViewer } from 'antd-mobile'
import { useMemo } from 'react'
import { sanitizeRichText } from '@/lib/sanitize'

/** Sanitized rich-text renderer with a fullscreen viewer for embedded images. */
export function RichTextBody({ html }: { html: string }) {
  // 统一走 sanitize 白名单，避免与访客须知两处配置漂移。
  const sanitized = useMemo(() => sanitizeRichText(html), [html])

  function handleClick(event: React.MouseEvent<HTMLDivElement>) {
    const target = event.target as HTMLElement
    if (target.tagName !== 'IMG') return
    const container = event.currentTarget
    const images = Array.from(container.querySelectorAll('img')).map((img) => img.src)
    const index = images.indexOf((target as HTMLImageElement).src)
    ImageViewer.Multi.show({ images, defaultIndex: Math.max(index, 0) })
  }

  return (
    <div
      className="prose-img:max-w-full text-sm leading-6 break-words [&_img]:h-auto [&_img]:max-w-full"
      onClick={handleClick}
      dangerouslySetInnerHTML={{ __html: sanitized }}
    />
  )
}
