// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import BadgeBindingPage from './page'

const authApiMock = vi.hoisted(() => ({
  bindEmployeeBadge: vi.fn(),
}))

const wechatOauthMock = vi.hoisted(() => ({
  redirectToWechatOAuth: vi.fn(),
}))

vi.mock('next/navigation', () => ({
  useSearchParams: () => new URLSearchParams('code=code-bind'),
}))

vi.mock('antd-mobile', () => ({
  Toast: { show: vi.fn() },
}))

vi.mock('@/features/auth/api', () => ({
  bindEmployeeBadge: authApiMock.bindEmployeeBadge,
}))

vi.mock('@/lib/wechat/oauth', () => ({
  BADGE_BINDING_PATH: '/login/badge',
  redirectToWechatOAuth: wechatOauthMock.redirectToWechatOAuth,
}))

function renderBadgeBindingPage() {
  return render(React.createElement(BadgeBindingPage))
}

beforeEach(() => {
  authApiMock.bindEmployeeBadge.mockResolvedValue({ code: 0, message: '绑定成功' })
})

afterEach(() => {
  cleanup()
  authApiMock.bindEmployeeBadge.mockReset()
  wechatOauthMock.redirectToWechatOAuth.mockReset()
})

describe('绑定员工号页面', () => {
  it('员工号输入框使用文本键盘以支持字母工号', () => {
    renderBadgeBindingPage()

    expect(screen.getByPlaceholderText('输入员工号').getAttribute('inputmode')).toBeNull()
  })

  it('提交时保留字母工号原值', async () => {
    renderBadgeBindingPage()

    fireEvent.change(screen.getByPlaceholderText('输入员工号'), { target: { value: 'YT100200' } })
    fireEvent.change(screen.getByPlaceholderText('输入身份证后六位'), { target: { value: '123456' } })
    fireEvent.click(screen.getByRole('button', { name: '绑定' }))

    await waitFor(() =>
      expect(authApiMock.bindEmployeeBadge).toHaveBeenCalledWith({
        code: 'code-bind',
        badge: 'YT100200',
        lastCertNum: '123456',
      }),
    )
  })
})
