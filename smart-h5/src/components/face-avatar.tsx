'use client'
import Image from 'next/image'
import { useState } from 'react'

const FACE_IMAGE_BASE = '/platform/image/view'

/** 人脸照片头像（faceId 走网关图片服务，加载失败/缺省占位）。 */
export function FaceAvatar({ faceId }: { faceId?: string }) {
  const [failed, setFailed] = useState(false)
  if (!faceId || failed) {
    return (
      <span
        className="grid h-[72px] w-[72px] flex-none place-items-center rounded-xl bg-accent-soft text-2xl"
        aria-hidden
      >
        👤
      </span>
    )
  }
  return (
    <span className="relative block h-[72px] w-[72px] flex-none overflow-hidden rounded-xl">
      <Image
        src={`${FACE_IMAGE_BASE}/${faceId}`}
        alt="人脸照片"
        fill
        unoptimized
        className="object-cover"
        onError={() => setFailed(true)}
      />
    </span>
  )
}
