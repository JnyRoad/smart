'use client'
import { Dialog, ImageViewer, SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRef, useState } from 'react'
// checkFace is the gateway's generic photo-upload endpoint; it lives under the
// visitor module for historical (legacy path) reasons only.
import { checkFace } from '@/features/visitor/api'
import { extractPhotoId } from '@/lib/photo-id'

function toRawBase64(dataUrl: string): string {
  const comma = dataUrl.indexOf(',')
  return comma >= 0 ? dataUrl.slice(comma + 1) : dataUrl
}

function readFileAsDataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(new Error('图片读取失败'))
    reader.readAsDataURL(file)
  })
}

/**
 * Multi-image upload (legacy upload-image / upload-image-base64 port).
 * mode 'photoId': each photo goes through checkFace and yields a photo id
 * (dorm-exit). mode 'base64': local read only, raw base64 goes straight into
 * the submit body (dorm-repairs).
 */
export function ImageListUpload({
  mode,
  value,
  onChange,
  max = 3,
  confirmRemove = false,
}: {
  mode: 'photoId' | 'base64'
  value: string[]
  onChange: (list: string[]) => void
  max?: number
  /** Ask "是否移除此图片" before deleting (legacy good-release behavior). */
  confirmRemove?: boolean
}) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  // previews mirror `value` index-by-index and are only consistent when value
  // changes exclusively through this component's onChange. External partial
  // edits require a remount (key prop); a full external reset is handled below.
  const [previews, setPreviews] = useState<string[]>([])
  if (value.length === 0 && previews.length > 0) {
    setPreviews([])
  }

  async function handleFile(file: File) {
    if (uploading) return
    setUploading(true)
    try {
      const dataUrl = await readFileAsDataUrl(file)
      const base64 = toRawBase64(dataUrl)
      if (mode === 'base64') {
        onChange([...value, base64])
        setPreviews((prev) => [...prev, dataUrl])
      } else {
        const uploaded = await checkFace(base64)
        // 网关把照片 id 嵌在 data.photoId（旧版 res.data.photoId），直接读 data 会取空。
        const photoId = uploaded.code === 0 ? extractPhotoId(uploaded.data) : ''
        if (photoId) {
          onChange([...value, photoId])
          setPreviews((prev) => [...prev, dataUrl])
        } else {
          Toast.show(uploaded.code === 0 ? '照片上传失败，请重试' : (uploaded.message ?? '照片上传失败'))
        }
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '照片上传失败')
    } finally {
      setUploading(false)
    }
  }

  function removeAt(index: number) {
    onChange(value.filter((_, i) => i !== index))
    setPreviews((prev) => prev.filter((_, i) => i !== index))
  }

  async function requestRemove(index: number) {
    if (confirmRemove) {
      const ok = await Dialog.confirm({ content: '是否移除此图片' })
      if (!ok) return
    }
    removeAt(index)
  }

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        data-testid="image-list-input"
        onChange={(e) => {
          const file = e.target.files?.[0]
          if (file) void handleFile(file)
          e.target.value = ''
        }}
      />
      <div className="flex flex-wrap gap-2.5">
        {previews.map((preview, index) => (
          <div key={index} className="relative" data-testid="image-list-item">
            <button
              type="button"
              onClick={() => ImageViewer.Multi.show({ images: previews, defaultIndex: index })}
              className="relative block h-20 w-20 overflow-hidden rounded-xl"
            >
              <Image src={preview} alt="" fill unoptimized className="object-cover" />
            </button>
            <button
              type="button"
              aria-label="删除图片"
              onClick={() => void requestRemove(index)}
              className="absolute -top-1.5 -right-1.5 grid h-5 w-5 place-items-center rounded-full bg-[#d83b36] text-xs text-white"
            >
              ✕
            </button>
          </div>
        ))}
        {value.length < max && (
          <button
            type="button"
            disabled={uploading}
            onClick={() => inputRef.current?.click()}
            className="grid h-20 w-20 place-items-center rounded-xl border border-dashed border-light-gray bg-surface text-2xl text-weak"
          >
            {uploading ? <SpinLoading color="primary" style={{ '--size': '20px' }} /> : '+'}
          </button>
        )}
      </div>
    </div>
  )
}
