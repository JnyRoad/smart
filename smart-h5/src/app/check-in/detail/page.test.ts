// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import React from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import CheckInDetailPage from './page'

const dormApiMock = vi.hoisted(() => ({ getLockPwd: vi.fn() }))
const dormServicesApiMock = vi.hoisted(() => ({ getCheckInRecords: vi.fn() }))

vi.mock('antd-mobile', async () => {
  const ReactModule = await import('react')

  return {
    ErrorBlock: ({ description }: { description?: string }) => ReactModule.createElement('div', null, description),
    PullToRefresh: ({ children }: { children: React.ReactNode }) => ReactModule.createElement(ReactModule.Fragment, null, children),
    SpinLoading: () => ReactModule.createElement('div', null, '加载中'),
    Toast: { show: vi.fn() },
  }
})

vi.mock('@/components/app-icon', () => ({
  StatusIcon: () => React.createElement('span'),
}))

vi.mock('@/components/page-shell', () => ({
  PageShell: ({ children }: { children: React.ReactNode }) => React.createElement('main', null, children),
}))

vi.mock('@/components/segment-tabs', () => ({
  SegmentTabs: () => React.createElement('div'),
}))

vi.mock('@/features/auth/use-require-auth', () => ({
  useRequireAuth: () => true,
}))

vi.mock('@/features/dorm/api', () => ({
  getLockPwd: dormApiMock.getLockPwd,
}))

vi.mock('@/features/dorm-services/api', () => ({
  getCheckInRecords: dormServicesApiMock.getCheckInRecords,
}))

vi.mock('@/lib/crypto/aes', () => ({
  decryptFromHex: () => '864321',
}))

function renderPage() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })

  return render(
    React.createElement(
      QueryClientProvider,
      { client: queryClient },
      React.createElement(CheckInDetailPage),
    ),
  )
}

beforeEach(() => {
  dormServicesApiMock.getCheckInRecords.mockResolvedValue({
    code: 0,
    data: [{ dormitoryName: '一号宿舍', roomName: '201', bedNumber: 1 }],
  })
  dormApiMock.getLockPwd.mockResolvedValue({ code: 0, data: 'cipher-from-self-api' })
})

afterEach(() => {
  cleanup()
  dormApiMock.getLockPwd.mockReset()
  dormServicesApiMock.getCheckInRecords.mockReset()
})

describe('入住详情门锁动态码', () => {
  it('房间列表最小化时仍展示本人动态码', async () => {
    renderPage()

    expect((await screen.findByTestId('checkin-lock-code')).textContent).toBe('864321')
  })
})
