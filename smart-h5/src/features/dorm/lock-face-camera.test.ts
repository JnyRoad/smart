// @vitest-environment jsdom
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { LockFaceCamera } from './lock-face-camera'

const visitorApiMock = vi.hoisted(() => ({
  checkFace: vi.fn(),
  cropEmployeeFace: vi.fn(),
}))

vi.mock('@/features/visitor/api', () => visitorApiMock)

vi.mock('next/image', async () => {
  const ReactModule = await import('react')

  return {
    default: ({ alt, ...props }: { alt: string }) => ReactModule.createElement('img', { alt, ...props }),
  }
})

vi.mock('antd-mobile', async () => {
  const ReactModule = await import('react')

  return {
    SpinLoading: () => ReactModule.createElement('span', null, 'loading'),
    Toast: { show: vi.fn() },
  }
})

const stopTrack = vi.fn()
const getUserMedia = vi.fn()
const drawImage = vi.fn()
const translate = vi.fn()
const scale = vi.fn()
const toDataURL = vi.fn(() => 'data:image/jpeg;base64,camera-raw-base64')

beforeEach(() => {
  visitorApiMock.cropEmployeeFace.mockResolvedValue({ code: 0, data: 'cut-face-base64' })
  visitorApiMock.checkFace.mockResolvedValue({ code: 0, data: { photoId: 'photo-1' } })
  stopTrack.mockClear()
  getUserMedia.mockResolvedValue({ getTracks: () => [{ stop: stopTrack }] })
  drawImage.mockClear()
  translate.mockClear()
  scale.mockClear()
  toDataURL.mockClear()

  Object.defineProperty(navigator, 'mediaDevices', {
    configurable: true,
    value: { getUserMedia },
  })
  Object.defineProperty(HTMLVideoElement.prototype, 'play', {
    configurable: true,
    value: vi.fn(() => Promise.resolve()),
  })
  Object.defineProperty(HTMLVideoElement.prototype, 'videoWidth', {
    configurable: true,
    get: () => 320,
  })
  Object.defineProperty(HTMLVideoElement.prototype, 'videoHeight', {
    configurable: true,
    get: () => 240,
  })
  Object.defineProperty(HTMLCanvasElement.prototype, 'getContext', {
    configurable: true,
    value: () => ({ drawImage, scale, translate }),
  })
  Object.defineProperty(HTMLCanvasElement.prototype, 'toDataURL', {
    configurable: true,
    value: toDataURL,
  })
})

afterEach(() => {
  cleanup()
  visitorApiMock.cropEmployeeFace.mockReset()
  visitorApiMock.checkFace.mockReset()
  getUserMedia.mockReset()
})

describe('LockFaceCamera', () => {
  it('renders the Open Design scan template, mirrors only preview, and uploads the original frame', async () => {
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    const stage = screen.getByTestId('lock-face-camera-stage')
    expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('initial')
    expect(stage.getAttribute('aria-label')).toBe('人脸核验区域')
    expect(screen.getByText('请先打开前置摄像头')).toBeTruthy()
    expect(screen.getByTestId('lock-face-camera-actions')).toBeTruthy()

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))

    const video = await screen.findByTestId('lock-face-camera-video')
    await waitFor(() => expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('scanning'))
    expect(video.dataset.mirrorPreview).toBe('true')
    expect(screen.getByTestId('lock-face-camera-mask')).toBeTruthy()
    expect(screen.getByTestId('lock-face-camera-focus-oval')).toBeTruthy()
    expect(screen.getByText('镜像预览')).toBeTruthy()

    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))

    await waitFor(() => expect(visitorApiMock.cropEmployeeFace).toHaveBeenCalledWith('camera-raw-base64'))
    expect(translate).not.toHaveBeenCalled()
    expect(scale).not.toHaveBeenCalled()
    expect(drawImage).toHaveBeenCalled()
  })

  it('uses front camera frames instead of a file picker for lock-code face verification', async () => {
    const onCaptured = vi.fn()

    const { container } = render(React.createElement(LockFaceCamera, { onCaptured, onGenerate: vi.fn() }))

    expect(screen.queryByTestId('face-upload-input')).toBeNull()
    expect(container.querySelector('input[type="file"]')).toBeNull()

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))

    await waitFor(() =>
      expect(getUserMedia).toHaveBeenCalledWith({
        audio: false,
        video: { facingMode: 'user' },
      }),
    )

    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))

    await waitFor(() => expect(visitorApiMock.cropEmployeeFace).toHaveBeenCalledWith('camera-raw-base64'))
    expect(drawImage).toHaveBeenCalled()
    expect(toDataURL).toHaveBeenCalledWith('image/jpeg', 0.9)
    expect(visitorApiMock.checkFace).toHaveBeenCalledWith('cut-face-base64')
    expect(onCaptured).toHaveBeenCalledWith('cut-face-base64')
    expect(stopTrack).toHaveBeenCalled()
  })

  it('keeps the generate action inside the verified face template state', async () => {
    const onGenerate = vi.fn()
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))
    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))

    expect(await screen.findByText('人脸识别通过')).toBeTruthy()
    expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('success')

    fireEvent.click(screen.getByTestId('lock-face-camera-generate'))

    expect(onGenerate).toHaveBeenCalledTimes(1)
  })

  it('shows an in-stage failure state that can return to scanning', async () => {
    visitorApiMock.cropEmployeeFace.mockResolvedValue({ code: 1, message: '未检测到人脸' })
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))
    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))

    expect(await screen.findByText('未检测到人脸')).toBeTruthy()
    expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('failure')

    fireEvent.click(screen.getByTestId('lock-face-camera-retry'))

    await waitFor(() => expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('scanning'))
  })

  it('reuses the active stream when opening the camera again from failure state', async () => {
    visitorApiMock.cropEmployeeFace.mockResolvedValue({ code: 1, message: '未检测到人脸' })
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))
    await waitFor(() => expect(getUserMedia).toHaveBeenCalledTimes(1))
    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))
    expect(await screen.findByText('未检测到人脸')).toBeTruthy()

    fireEvent.click(screen.getByTestId('lock-face-camera-open-from-failure'))

    await waitFor(() => expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('scanning'))
    expect(getUserMedia).toHaveBeenCalledTimes(1)
    expect(stopTrack).not.toHaveBeenCalled()
  })

  it('requests a fresh camera stream when the retained stream has ended', async () => {
    const retainedTrack = { readyState: 'live', stop: stopTrack }
    visitorApiMock.cropEmployeeFace.mockResolvedValue({ code: 1, message: '未检测到人脸' })
    getUserMedia.mockResolvedValueOnce({ active: true, getTracks: () => [retainedTrack] })
    getUserMedia.mockResolvedValueOnce({ active: true, getTracks: () => [{ readyState: 'live', stop: vi.fn() }] })
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))
    await waitFor(() => expect(getUserMedia).toHaveBeenCalledTimes(1))
    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))
    expect(await screen.findByText('未检测到人脸')).toBeTruthy()

    retainedTrack.readyState = 'ended'
    fireEvent.click(screen.getByTestId('lock-face-camera-open-from-failure'))

    await waitFor(() => expect(getUserMedia).toHaveBeenCalledTimes(2))
    await waitFor(() => expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('scanning'))
    expect(stopTrack).toHaveBeenCalledTimes(1)
  })

  it('requests a fresh camera stream from the primary retry action when the retained stream has ended', async () => {
    const retainedTrack = { readyState: 'live', stop: stopTrack }
    visitorApiMock.cropEmployeeFace.mockResolvedValue({ code: 1, message: '未检测到人脸' })
    getUserMedia.mockResolvedValueOnce({ active: true, getTracks: () => [retainedTrack] })
    getUserMedia.mockResolvedValueOnce({ active: true, getTracks: () => [{ readyState: 'live', stop: vi.fn() }] })
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))
    await waitFor(() => expect(getUserMedia).toHaveBeenCalledTimes(1))
    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))
    expect(await screen.findByText('未检测到人脸')).toBeTruthy()

    retainedTrack.readyState = 'ended'
    fireEvent.click(screen.getByTestId('lock-face-camera-retry'))

    await waitFor(() => expect(getUserMedia).toHaveBeenCalledTimes(2))
    await waitFor(() => expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('scanning'))
    expect(stopTrack).toHaveBeenCalledTimes(1)
  })

  it('names the camera starting action for screen readers', async () => {
    getUserMedia.mockReturnValue(new Promise(() => {}))
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))

    expect(screen.getByRole('button', { name: '正在打开摄像头' })).toBeTruthy()
  })

  it('hides inactive state panels from assistive technology', async () => {
    const { container } = render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    expect(container.querySelector('[data-state="initial"]')?.getAttribute('aria-hidden')).toBe('false')
    expect(container.querySelector('[data-state="scanning"]')?.getAttribute('aria-hidden')).toBe('true')

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))

    await waitFor(() => expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('scanning'))
    expect(container.querySelector('[data-state="initial"]')?.getAttribute('aria-hidden')).toBe('true')
    expect(container.querySelector('[data-state="scanning"]')?.getAttribute('aria-hidden')).toBe('false')
  })

  it('names the checking action for screen readers', async () => {
    let resolveCut!: (value: { code: number; message: string }) => void
    visitorApiMock.cropEmployeeFace.mockReturnValue(
      new Promise((resolve) => {
        resolveCut = resolve
      }),
    )
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))
    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))

    await waitFor(() => expect(screen.getByTestId('lock-face-camera-root').dataset.currentState).toBe('checking'))
    expect((screen.getByRole('button', { name: '正在核验人脸' }) as HTMLButtonElement).disabled).toBe(true)

    await act(async () => {
      resolveCut({ code: 1, message: '未检测到人脸' })
      await Promise.resolve()
    })
  })

  it('stops a camera stream that resolves after the component unmounts', async () => {
    let resolveCamera!: (stream: { getTracks: () => Array<{ stop: () => void }> }) => void
    getUserMedia.mockReturnValue(
      new Promise((resolve) => {
        resolveCamera = resolve
      }),
    )

    const { unmount } = render(React.createElement(LockFaceCamera, { onCaptured: vi.fn(), onGenerate: vi.fn() }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))
    unmount()

    await act(async () => {
      resolveCamera({ getTracks: () => [{ stop: stopTrack }] })
      await Promise.resolve()
    })

    expect(stopTrack).toHaveBeenCalled()
  })
})
