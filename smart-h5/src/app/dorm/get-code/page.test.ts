// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import GetCodePage from './page'

const routerMock = vi.hoisted(() => ({
  back: vi.fn(),
  push: vi.fn(),
  replace: vi.fn(),
}))

const visitorApiMock = vi.hoisted(() => ({
  checkFace: vi.fn(),
  cropEmployeeFace: vi.fn(),
}))

const dormApiMock = vi.hoisted(() => ({
  refreshLockPwd: vi.fn(),
}))

vi.mock('next/navigation', () => ({
  useRouter: () => routerMock,
}))

vi.mock('next/image', async () => {
  const ReactModule = await import('react')

  return {
    default: ({ alt, ...props }: { alt: string }) => ReactModule.createElement('img', { alt, ...props }),
  }
})

vi.mock('antd-mobile', async () => {
  const ReactModule = await import('react')

  return {
    Dialog: { alert: vi.fn(() => Promise.resolve()) },
    ErrorBlock: ({ title, description }: { title?: string; description?: string }) =>
      ReactModule.createElement('div', null, title ?? description),
    NavBar: ({ children }: { children: React.ReactNode }) => ReactModule.createElement('div', null, children),
    SpinLoading: () => ReactModule.createElement('div', null, 'loading'),
    Toast: { show: vi.fn() },
  }
})

vi.mock('@/features/auth/use-require-auth', () => ({
  useRequireAuth: () => true,
}))

vi.mock('@/features/visitor/api', () => ({
  checkFace: visitorApiMock.checkFace,
  cropEmployeeFace: visitorApiMock.cropEmployeeFace,
}))

vi.mock('@/features/dorm/api', () => ({
  refreshLockPwd: dormApiMock.refreshLockPwd,
}))

const stopTrack = vi.fn()
const getUserMedia = vi.fn()
const drawImage = vi.fn()
const translate = vi.fn()
const scale = vi.fn()
const toDataURL = vi.fn(() => 'data:image/jpeg;base64,camera-raw-base64')

function renderGetCodePage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })

  return render(
    React.createElement(
      QueryClientProvider,
      { client: queryClient },
      React.createElement(GetCodePage),
    ),
  )
}

beforeEach(() => {
  visitorApiMock.cropEmployeeFace.mockResolvedValue({ code: 0, data: 'cut-face-base64' })
  visitorApiMock.checkFace.mockResolvedValue({ code: 0, data: { photoId: 'photo-1' } })
  dormApiMock.refreshLockPwd.mockResolvedValue({ code: 0 })
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
  routerMock.back.mockClear()
  routerMock.push.mockClear()
  routerMock.replace.mockClear()
  visitorApiMock.cropEmployeeFace.mockReset()
  visitorApiMock.checkFace.mockReset()
  dormApiMock.refreshLockPwd.mockReset()
  getUserMedia.mockReset()
})

describe('门锁动态码刷新人脸采集', () => {
  it('renders the Open Design lock-code face scan page and hides gallery upload', async () => {
    const { container } = renderGetCodePage()

    expect(await screen.findByTestId('lock-face-page')).toBeTruthy()
    expect(screen.getByRole('heading', { name: '刷新动态码' })).toBeTruthy()
    expect(await screen.findByText('当前员工')).toBeTruthy()
    expect(screen.queryByText(/工号/)).toBeNull()
    expect(screen.getByText('动态码刷新')).toBeTruthy()
    expect(await screen.findByTestId('lock-face-camera-start')).toBeTruthy()
    expect(screen.getByTestId('lock-face-camera-stage')).toBeTruthy()
    expect(screen.getByTestId('lock-face-camera-actions')).toBeTruthy()

    expect(screen.queryByTestId('face-upload-input')).toBeNull()
    expect(container.querySelector('input[type="file"]')).toBeNull()
  })

  it('generates the lock code with the captured face only', async () => {
    renderGetCodePage()

    fireEvent.click(await screen.findByTestId('lock-face-camera-start'))
    fireEvent.click(await screen.findByTestId('lock-face-camera-capture'))

    expect(await screen.findByText('人脸识别通过')).toBeTruthy()

    fireEvent.click(screen.getByTestId('lock-face-camera-generate'))

    await waitFor(() =>
      expect(dormApiMock.refreshLockPwd).toHaveBeenCalledWith({
        facePic: 'cut-face-base64',
      }),
    )
    await waitFor(() => expect(routerMock.replace).toHaveBeenCalledWith('/dorm/lock'))
  })
})
