'use client'
import { SpinLoading, Toast } from 'antd-mobile'
import Image from 'next/image'
import { useEffect, useRef, useState } from 'react'
import { checkFace, faceCut } from '@/features/visitor/api'
import { resolveLockRefreshFacePic } from './lock-refresh'

interface LockFaceUploadResponse {
  code: number
  data?: unknown
  message?: string
  resultData?: { base64?: string | null }
}

function toRawBase64(dataUrl: string): string {
  const comma = dataUrl.indexOf(',')
  return comma >= 0 ? dataUrl.slice(comma + 1) : dataUrl
}

function stopStream(stream: MediaStream | null) {
  stream?.getTracks().forEach((track) => track.stop())
}

async function requestFrontCamera(): Promise<MediaStream> {
  const getUserMedia = navigator.mediaDevices?.getUserMedia
  if (!getUserMedia) {
    throw new Error('当前浏览器不支持直接拍照，请在微信内打开并允许摄像头权限')
  }

  return getUserMedia.call(navigator.mediaDevices, {
    audio: false,
    video: { facingMode: 'user' },
  })
}

export function LockFaceCamera({ onCaptured }: { onCaptured: (facePic: string) => void }) {
  const mountedRef = useRef(true)
  const videoRef = useRef<HTMLVideoElement>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const [cameraReady, setCameraReady] = useState(false)
  const [starting, setStarting] = useState(false)
  const [checking, setChecking] = useState(false)
  const [preview, setPreview] = useState('')

  useEffect(
    () => {
      mountedRef.current = true
      return () => {
        mountedRef.current = false
        stopStream(streamRef.current)
        streamRef.current = null
      }
    },
    [],
  )

  async function startCamera() {
    if (starting) return
    setStarting(true)
    let nextStream: MediaStream | null = null
    try {
      nextStream = await requestFrontCamera()
      if (!mountedRef.current) {
        stopStream(nextStream)
        return
      }

      stopStream(streamRef.current)
      streamRef.current = nextStream
      setPreview('')

      const video = videoRef.current
      if (!video) {
        throw new Error('摄像头启动失败，请重试')
      }

      video.srcObject = nextStream
      await video.play()
      if (!mountedRef.current) {
        stopStream(nextStream)
        return
      }
      setCameraReady(true)
    } catch (error) {
      stopStream(nextStream)
      streamRef.current = null
      if (mountedRef.current) {
        setCameraReady(false)
        Toast.show(error instanceof Error ? error.message : '摄像头启动失败')
      }
    } finally {
      if (mountedRef.current) {
        setStarting(false)
      }
    }
  }

  async function captureFace() {
    if (checking) return
    const video = videoRef.current
    if (!video || !streamRef.current) {
      Toast.show('请先打开摄像头')
      return
    }

    const width = video.videoWidth || 640
    const height = video.videoHeight || 480
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const context = canvas.getContext('2d')
    if (!context) {
      Toast.show('拍照失败，请重试')
      return
    }

    context.drawImage(video, 0, 0, width, height)
    const photoDataUrl = canvas.toDataURL('image/jpeg', 0.9)
    const rawBase64 = toRawBase64(photoDataUrl)
    if (!rawBase64) {
      Toast.show('拍照失败，请重试')
      return
    }

    setChecking(true)
    try {
      const cut = await faceCut(rawBase64)
      if (!mountedRef.current) return
      if (cut.code !== 0 || !cut.data) {
        Toast.show(cut.message && cut.message !== 'success' ? cut.message : '人脸检测失败，请重新拍摄')
        return
      }

      const uploaded = (await checkFace(cut.data)) as LockFaceUploadResponse
      if (!mountedRef.current) return
      if (uploaded.code !== 0) {
        Toast.show(uploaded.message ?? '照片上传失败')
        return
      }

      const facePic = resolveLockRefreshFacePic(uploaded, cut.data)
      if (!facePic) {
        Toast.show('人脸比对结果异常，请重试')
        return
      }

      setPreview(photoDataUrl)
      onCaptured(facePic)
      stopStream(streamRef.current)
      streamRef.current = null
      setCameraReady(false)
    } catch (error) {
      if (mountedRef.current) {
        Toast.show(error instanceof Error ? error.message : '照片上传失败')
      }
    } finally {
      if (mountedRef.current) {
        setChecking(false)
      }
    }
  }

  async function retake() {
    onCaptured('')
    setPreview('')
    await startCamera()
  }

  return (
    <div className="flex flex-col items-center gap-3">
      <div
        className="relative grid h-48 w-48 place-items-center overflow-hidden rounded-xl border border-dashed border-light-gray bg-surface"
        style={{ borderColor: 'var(--light-gray)' }}
      >
        <video
          ref={videoRef}
          data-testid="lock-face-camera-video"
          className={cameraReady && !preview ? 'h-full w-full object-cover' : 'hidden'}
          playsInline
          muted
        />
        {preview ? (
          <Image
            src={preview}
            alt="已拍摄人脸照片"
            data-testid="lock-face-camera-preview"
            fill
            unoptimized
            className="object-cover"
          />
        ) : cameraReady ? null : (
          <span className="px-4 text-center text-xs text-weak">请打开摄像头拍摄正面人脸</span>
        )}
      </div>

      {cameraReady ? (
        <button
          type="button"
          data-testid="lock-face-camera-capture"
          disabled={checking}
          onClick={() => void captureFace()}
          className="flex h-11 w-40 items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white disabled:opacity-60"
        >
          {checking ? <SpinLoading color="white" /> : '拍照识别'}
        </button>
      ) : preview ? (
        <button
          type="button"
          data-testid="lock-face-camera-retake"
          onClick={() => void retake()}
          className="flex h-11 w-40 items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white"
        >
          重新拍照
        </button>
      ) : (
        <button
          type="button"
          data-testid="lock-face-camera-start"
          disabled={starting || checking}
          onClick={() => void startCamera()}
          className="flex h-11 w-40 items-center justify-center rounded-[14px] bg-brand text-[15px] font-semibold text-white disabled:opacity-60"
        >
          {starting ? <SpinLoading color="white" /> : '打开摄像头'}
        </button>
      )}
    </div>
  )
}
