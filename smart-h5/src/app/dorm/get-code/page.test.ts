// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import React from 'react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import GetCodePage from './page'

const routerMock = vi.hoisted(() => ({
  back: vi.fn(),
  push: vi.fn(),
  replace: vi.fn(),
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

vi.mock('@/features/employee/api', () => ({
  getEmployeeBaseInfo: vi.fn(() => Promise.resolve({ code: 0, data: { employeeBadge: 'E001' } })),
}))

vi.mock('@/features/visitor/api', () => ({
  checkFace: vi.fn(),
  faceCut: vi.fn(),
}))

vi.mock('@/features/dorm/api', () => ({
  refreshLockPwd: vi.fn(),
}))

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

afterEach(() => {
  cleanup()
  routerMock.back.mockClear()
  routerMock.push.mockClear()
  routerMock.replace.mockClear()
})

describe('门锁动态码刷新人脸采集', () => {
  it('只能使用摄像头采集，不能渲染普通相册上传入口', async () => {
    const { container } = renderGetCodePage()

    expect(await screen.findByTestId('lock-face-camera-start')).toBeTruthy()

    expect(screen.queryByTestId('face-upload-input')).toBeNull()
    expect(container.querySelector('input[type="file"]')).toBeNull()
  })
})
