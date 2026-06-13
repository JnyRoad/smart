'use client'
import { SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useRef, useState } from 'react'
import { checkFace, faceCut } from '@/features/visitor/api'
import { extractPhotoId, photoViewUrl } from '@/lib/photo-id'

/** Strips the dataURL prefix; the gateway expects raw base64. */
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
 * Photo upload returning a gateway photo id.
 * mode 'face': face detection + crop (algorithm:/out/face/cut) before upload —
 * used for visitor/fellow portraits. mode 'plain': direct upload — used for
 * vehicle document photos. Both upload via app:/wechat/visit/checkFace.
 */
export interface FaceUploadResponse {
  code: number
  /** Gateway nests the photo id as `{ photoId }`; some callers get it directly. */
  data?: string | number | { photoId?: string | number } | null
  message?: string
  /** Server-processed photo, consumed by the lock-refresh page. */
  resultData?: { base64?: string }
}

export function FaceUpload({
  value,
  onChange,
  mode,
  label = '点击上传照片',
  onUploaded,
}: {
  value?: string
  onChange: (photoId: string) => void
  mode: 'face' | 'plain'
  label?: string
  /** Raw checkFace response for callers that need more than the photo id. */
  onUploaded?: (raw: FaceUploadResponse) => void
}) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [preview, setPreview] = useState<string | null>(null)

  async function handleFile(file: File) {
    if (uploading) return
    setUploading(true)
    try {
      const dataUrl = await readFileAsDataUrl(file)
      let base64 = toRawBase64(dataUrl)
      if (mode === 'face') {
        const cut = await faceCut(base64)
        // 真实响应 data 是裁剪后的图片 base64 字符串本身（不是 {imageData} 嵌套）。
        if (cut.code !== 0 || !cut.data) {
          // 网关成功消息是英文 success，失败兜底用中文提示。
          Toast.show(cut.message && cut.message !== 'success' ? cut.message : '人脸检测失败，请重新拍摄')
          return
        }
        base64 = cut.data
      }
      const uploaded = (await checkFace(base64)) as FaceUploadResponse
      if (uploaded.code === 0) {
        // The response has two consumables (data.photoId = uploaded photo id,
        // resultData = processed photo). Expose the raw response on any success
        // so callers that only need resultData are not blocked by a missing id.
        onUploaded?.(uploaded)
        const photoId = extractPhotoId(uploaded.data)
        if (photoId) {
          setPreview(dataUrl)
          onChange(photoId)
        } else if (!onUploaded) {
          // 网关成功消息是英文 success，不当错误文案展示。
          Toast.show('照片上传失败，请重试')
        }
      } else {
        Toast.show(uploaded.message ?? '照片上传失败')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '照片上传失败')
    } finally {
      setUploading(false)
    }
  }

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        className="hidden"
        data-testid="face-upload-input"
        onChange={(e) => {
          const file = e.target.files?.[0]
          if (file) void handleFile(file)
          e.target.value = ''
        }}
      />
      <button
        type="button"
        data-testid="face-upload-button"
        disabled={uploading}
        onClick={() => inputRef.current?.click()}
        className="relative flex h-24 w-24 items-center justify-center overflow-hidden rounded-xl border border-dashed border-light-gray bg-surface"
        style={{ borderColor: 'var(--light-gray)' }}
      >
        {uploading ? (
          <SpinLoading color="primary" />
        ) : preview ? (
          <Image src={preview} alt="已上传照片" fill unoptimized className="object-cover" />
        ) : value && !onUploaded ? (
          // value 是真实 photoId（访客场景）：返回本页本地预览已丢，用 photoId 回查网关图。
          // onUploaded 场景（门锁取码）value 只是哨兵、由父级自渲染预览，仍走文字标签。
          <Image src={photoViewUrl(value)} alt="已上传照片" fill unoptimized className="object-cover" />
        ) : value ? (
          <span className="px-2 text-xs text-mid">已上传，点击可重新上传</span>
        ) : (
          <span className="px-2 text-xs text-weak">{label}</span>
        )}
      </button>
    </div>
  )
}
