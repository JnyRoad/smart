// @vitest-environment jsdom
import { act, cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { LockFaceCamera } from './lock-face-camera'

const visitorApiMock = vi.hoisted(() => ({
  checkFace: vi.fn(),
  faceCut: vi.fn(),
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

function firstCallOrder(callOrder: number[]): number {
  const order = callOrder[0]
  expect(order).toBeDefined()
  return order as number
}

beforeEach(() => {
  visitorApiMock.faceCut.mockResolvedValue({ code: 0, data: 'cut-face-base64' })
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
  visitorApiMock.faceCut.mockReset()
  visitorApiMock.checkFace.mockReset()
  getUserMedia.mockReset()
})

describe('LockFaceCamera', () => {
  it('shows a large portrait camera stage and keeps front-camera frames mirrored', async () => {
    render(React.createElement(LockFaceCamera, { onCaptured: vi.fn() }))

    const stage = screen.getByTestId('lock-face-camera-stage')
    expect(stage.className).toContain('w-full')
    expect(stage.className).toContain('max-w-[390px]')
    expect(stage.className).toContain('aspect-[3/4]')

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))

    const video = await screen.findByTestId('lock-face-camera-video')
    await waitFor(() => expect(video.className).toContain('scale-x-[-1]'))

    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))

    await waitFor(() => expect(translate).toHaveBeenCalledWith(320, 0))
    expect(scale).toHaveBeenCalledWith(-1, 1)
    expect(drawImage).toHaveBeenCalled()
    expect(firstCallOrder(translate.mock.invocationCallOrder)).toBeLessThan(
      firstCallOrder(scale.mock.invocationCallOrder),
    )
    expect(firstCallOrder(scale.mock.invocationCallOrder)).toBeLessThan(
      firstCallOrder(drawImage.mock.invocationCallOrder),
    )
  })

  it('uses front camera frames instead of a file picker for lock-code face verification', async () => {
    const onCaptured = vi.fn()

    const { container } = render(React.createElement(LockFaceCamera, { onCaptured }))

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

    await waitFor(() => expect(visitorApiMock.faceCut).toHaveBeenCalledWith('camera-raw-base64'))
    expect(drawImage).toHaveBeenCalled()
    expect(toDataURL).toHaveBeenCalledWith('image/jpeg', 0.9)
    expect(visitorApiMock.checkFace).toHaveBeenCalledWith('cut-face-base64')
    expect(onCaptured).toHaveBeenCalledWith('cut-face-base64')
    expect(stopTrack).toHaveBeenCalled()
  })

  it('stops a camera stream that resolves after the component unmounts', async () => {
    let resolveCamera!: (stream: { getTracks: () => Array<{ stop: () => void }> }) => void
    getUserMedia.mockReturnValue(
      new Promise((resolve) => {
        resolveCamera = resolve
      }),
    )

    const { unmount } = render(React.createElement(LockFaceCamera, { onCaptured: vi.fn() }))

    fireEvent.click(screen.getByTestId('lock-face-camera-start'))
    unmount()

    await act(async () => {
      resolveCamera({ getTracks: () => [{ stop: stopTrack }] })
      await Promise.resolve()
    })

    expect(stopTrack).toHaveBeenCalled()
  })
})
