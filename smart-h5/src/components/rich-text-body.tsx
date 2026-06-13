'use client'
import { ImageViewer } from 'antd-mobile'
import DOMPurify from 'dompurify'
import { useMemo } from 'react'

/** Sanitized rich-text renderer with a fullscreen viewer for embedded images. */
export function RichTextBody({ html }: { html: string }) {
  const sanitized = useMemo(() => DOMPurify.sanitize(html), [html])

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
