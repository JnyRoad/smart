'use client'
import { SpinLoading, Toast } from 'antd-mobile'
import { useEffect, useRef, useState } from 'react'
import { checkFace, cropEmployeeFace } from '@/features/visitor/api'
import { resolveLockRefreshFacePic } from './lock-refresh'
import styles from './lock-face-camera.module.css'

interface LockFaceUploadResponse {
  code: number
  data?: unknown
  message?: string
  resultData?: { base64?: string | null }
}

type LockFaceState = 'initial' | 'scanning' | 'checking' | 'success' | 'failure'

interface LockFaceCameraProps {
  onCaptured: (facePic: string) => void
  onGenerate: () => void
  generating?: boolean
  generateDisabled?: boolean
}

function toRawBase64(dataUrl: string): string {
  const comma = dataUrl.indexOf(',')
  return comma >= 0 ? dataUrl.slice(comma + 1) : dataUrl
}

function stopStream(stream: MediaStream | null) {
  stream?.getTracks().forEach((track) => track.stop())
}

function isStreamLive(stream: MediaStream): boolean {
  if (stream.active === false) return false
  return stream.getTracks().some((track) => track.readyState !== 'ended')
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

function stateClassName(current: LockFaceState, target: LockFaceState): string {
  const stateViewClass = styles.stateView ?? ''
  if (current !== target) return stateViewClass
  return `${stateViewClass} ${styles.stateViewActive ?? ''}`.trim()
}

async function attachCameraStream(video: HTMLVideoElement | null, stream: MediaStream) {
  if (!video) {
    throw new Error('摄像头启动失败，请重试')
  }

  if (video.srcObject !== stream) {
    video.srcObject = stream
  }
  await video.play()
}

export function LockFaceCamera({
  onCaptured,
  onGenerate,
  generating = false,
  generateDisabled = false,
}: LockFaceCameraProps) {
  const mountedRef = useRef(true)
  const videoRef = useRef<HTMLVideoElement>(null)
  const streamRef = useRef<MediaStream | null>(null)
  const [faceState, setFaceState] = useState<LockFaceState>('initial')
  const [cameraReady, setCameraReady] = useState(false)
  const [starting, setStarting] = useState(false)
  const [checking, setChecking] = useState(false)
  const [preview, setPreview] = useState('')
  const [failureCopy, setFailureCopy] = useState({
    title: '人脸不在取景框内',
    detail: '请将面部完整置于取景框内，保持正对屏幕后重新拍照。',
  })

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
      const activeStream = streamRef.current
      if (activeStream) {
        if (!isStreamLive(activeStream)) {
          stopStream(activeStream)
          streamRef.current = null
        } else {
          setPreview('')
          onCaptured('')
          await attachCameraStream(videoRef.current, activeStream)
          if (!mountedRef.current) return
          setCameraReady(true)
          setFaceState('scanning')
          return
        }
      }

      nextStream = await requestFrontCamera()
      if (!mountedRef.current) {
        stopStream(nextStream)
        return
      }

      stopStream(streamRef.current)
      streamRef.current = nextStream
      setPreview('')
      onCaptured('')

      await attachCameraStream(videoRef.current, nextStream)
      if (!mountedRef.current) {
        stopStream(nextStream)
        return
      }
      setCameraReady(true)
      setFaceState('scanning')
    } catch (error) {
      stopStream(nextStream)
      if (nextStream) {
        streamRef.current = null
      }
      if (mountedRef.current) {
        setCameraReady(false)
        const message = error instanceof Error ? error.message : '摄像头启动失败'
        setFailureCopy({
          title: '摄像头启动失败',
          detail: message,
        })
        setFaceState('failure')
        Toast.show(message)
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

    // 只镜像屏幕预览；上传给后端的人脸帧保持相机原始方向，延续旧识别链路契约。
    context.drawImage(video, 0, 0, width, height)
    const photoDataUrl = canvas.toDataURL('image/jpeg', 0.9)
    const rawBase64 = toRawBase64(photoDataUrl)
    if (!rawBase64) {
      Toast.show('拍照失败，请重试')
      return
    }

    setChecking(true)
    setFaceState('checking')
    try {
      const cut = await cropEmployeeFace(rawBase64)
      if (!mountedRef.current) return
      if (cut.code !== 0 || !cut.data) {
        const message = cut.message && cut.message !== 'success' ? cut.message : '人脸检测失败，请重新拍摄'
        setFailureCopy({
          title: message,
          detail: '请将面部完整置于取景框内，保持正对屏幕后重新拍照。',
        })
        setFaceState('failure')
        return
      }

      const uploaded = (await checkFace(cut.data)) as LockFaceUploadResponse
      if (!mountedRef.current) return
      if (uploaded.code !== 0) {
        setFailureCopy({
          title: uploaded.message ?? '照片上传失败',
          detail: '请重新拍照核验，仍失败时联系宿舍管理员处理。',
        })
        setFaceState('failure')
        return
      }

      const facePic = resolveLockRefreshFacePic(uploaded, cut.data)
      if (!facePic) {
        setFailureCopy({
          title: '人脸比对结果异常',
          detail: '本次核验没有返回可用于刷新动态码的人脸信息，请重新拍照。',
        })
        setFaceState('failure')
        return
      }

      setPreview(photoDataUrl)
      onCaptured(facePic)
      stopStream(streamRef.current)
      streamRef.current = null
      setCameraReady(false)
      setFaceState('success')
    } catch (error) {
      if (mountedRef.current) {
        setFailureCopy({
          title: '核验失败',
          detail: error instanceof Error ? error.message : '照片上传失败',
        })
        setFaceState('failure')
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

  function handleGenerate() {
    if (generating || generateDisabled) return
    onGenerate()
  }

  return (
    <div className={styles.root} data-current-state={faceState} data-testid="lock-face-camera-root">
      <main className={styles.scanArea} aria-live="polite">
        <section className={styles.cameraStage} data-testid="lock-face-camera-stage" aria-label="人脸核验区域">
          <section
            className={stateClassName(faceState, 'initial')}
            data-state="initial"
            aria-hidden={faceState !== 'initial'}
            aria-label="摄像头未开启"
          >
            <div className={styles.emptyCamera}>
              <div className={styles.cameraSymbol} aria-hidden="true" />
              <div className={styles.emptyCopy}>
                <h2>请先打开前置摄像头</h2>
                <p>用于刷新宿舍门锁动态码的人脸核验。打开后请保持手机竖直，面部正对屏幕。</p>
              </div>
            </div>
          </section>

          <section
            className={stateClassName(faceState, 'scanning')}
            data-state="scanning"
            aria-hidden={faceState !== 'scanning'}
            aria-label="摄像头扫描中"
          >
            <video
              ref={videoRef}
              data-testid="lock-face-camera-video"
              data-mirror-preview="true"
              className={cameraReady && !preview ? styles.cameraVideo : styles.hiddenVideo}
              playsInline
              muted
            />
            <div className={styles.stageMask} data-testid="lock-face-camera-mask" aria-hidden="true" />
            <div className={styles.focusOval} data-testid="lock-face-camera-focus-oval" aria-hidden="true">
              <div className={styles.scanLine} />
            </div>
            <div className={styles.mirrorChip}>镜像预览</div>
            <div className={styles.scanHint}>
              <p className={styles.scanStatus}>请将面部置于取景框内</p>
              <p className={styles.scanDetail}>保持正对屏幕，取景框稳定后点击拍照核验。</p>
            </div>
          </section>

          <section
            className={stateClassName(faceState, 'checking')}
            data-state="checking"
            aria-hidden={faceState !== 'checking'}
            aria-label="正在核验"
          >
            <div className={styles.checkingLayer}>
              <div>
                <div className={styles.progressRing} aria-hidden="true" />
                <div className={styles.resultCopy}>
                  <h2>正在核验人脸</h2>
                  <p>请保持当前姿势，系统正在比对本人人脸信息。</p>
                </div>
              </div>
            </div>
          </section>

          <section
            className={stateClassName(faceState, 'success')}
            data-state="success"
            aria-hidden={faceState !== 'success'}
            aria-label="核验通过"
          >
            <div className={styles.resultLayer}>
              <div className={`${styles.resultMark} ${styles.resultMarkSuccess}`} aria-hidden="true" />
              <div className={styles.resultCopy}>
                <h2>人脸识别通过</h2>
                <p>已确认本人操作，可以继续生成本次宿舍门锁动态码。</p>
              </div>
            </div>
          </section>

          <section
            className={stateClassName(faceState, 'failure')}
            data-state="failure"
            aria-hidden={faceState !== 'failure'}
            aria-label="核验失败"
          >
            <div className={styles.resultLayer}>
              <div className={`${styles.resultMark} ${styles.resultMarkFailure}`} aria-hidden="true" />
              <div className={styles.resultCopy}>
                <h2>{failureCopy.title}</h2>
                <p>{failureCopy.detail}</p>
              </div>
            </div>
          </section>
        </section>
      </main>

      <footer className={styles.actions} data-testid="lock-face-camera-actions" aria-label="操作区">
        <div className={faceState === 'initial' ? `${styles.actionSet} ${styles.actionSetActive}` : styles.actionSet}>
          <button
            className={`${styles.btn} ${styles.btnPrimary}`}
            type="button"
            data-testid="lock-face-camera-start"
            aria-label={starting ? '正在打开摄像头' : '打开摄像头'}
            disabled={starting || checking}
            onClick={() => void startCamera()}
          >
            {starting ? <SpinLoading color="white" /> : '打开摄像头'}
          </button>
          <p className={styles.assistText}>请在光线充足的位置完成核验。</p>
        </div>

        <div className={faceState === 'scanning' ? `${styles.actionSet} ${styles.actionSetActive}` : styles.actionSet}>
          <button
            className={`${styles.btn} ${styles.btnPrimary}`}
            type="button"
            data-testid="lock-face-camera-capture"
            disabled={checking}
            onClick={() => void captureFace()}
          >
            拍照核验
          </button>
          <p className={styles.assistText}>请保持正脸在框内。</p>
        </div>

        <div className={faceState === 'checking' ? `${styles.actionSet} ${styles.actionSetActive}` : styles.actionSet}>
          <button className={styles.btn} type="button" disabled aria-label="正在核验人脸">
            <span className={styles.checkingButtonContent}>
              <SpinLoading />
              <span>正在核验人脸</span>
            </span>
          </button>
          <p className={styles.assistText}>核验过程中请不要移开手机。</p>
        </div>

        <div className={faceState === 'success' ? `${styles.actionSet} ${styles.actionSetActive}` : styles.actionSet}>
          <button
            className={`${styles.btn} ${styles.btnPrimary}`}
            type="button"
            data-testid="lock-face-camera-generate"
            disabled={generating || generateDisabled}
            onClick={handleGenerate}
          >
            {generating ? '生成中...' : '生成动态码'}
          </button>
          <button
            className={`${styles.btn} ${styles.btnGhost}`}
            type="button"
            data-testid="lock-face-camera-retake"
            onClick={() => void retake()}
          >
            重新拍照
          </button>
        </div>

        <div className={faceState === 'failure' ? `${styles.actionSet} ${styles.actionSetActive}` : styles.actionSet}>
          <button
            className={`${styles.btn} ${styles.btnPrimary}`}
            type="button"
            data-testid="lock-face-camera-retry"
            onClick={() => void retake()}
          >
            重新拍照
          </button>
          <button
            className={`${styles.btn} ${styles.btnGhost}`}
            type="button"
            data-testid="lock-face-camera-open-from-failure"
            onClick={() => void startCamera()}
          >
            打开摄像头
          </button>
        </div>
      </footer>
    </div>
  )
}
