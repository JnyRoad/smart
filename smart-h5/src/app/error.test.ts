// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import React from 'react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import AppError from './error'

afterEach(cleanup)

function renderAppError(reset = vi.fn()) {
  const error = new Error('数据库连接失败的敏感细节')
  render(React.createElement(AppError, { error, reset }))
  return { error, reset }
}

describe('段级错误边界', () => {
  it('展示友好的中文降级文案', () => {
    renderAppError()

    expect(screen.getByText('页面加载失败')).toBeTruthy()
  })

  it('不把 error.message 原样暴露给用户', () => {
    renderAppError()

    // 原始报错可能含敏感信息，界面上不应出现
    expect(screen.queryByText('数据库连接失败的敏感细节')).toBeNull()
  })

  it('点击重试调用 reset', () => {
    const { reset } = renderAppError()

    fireEvent.click(screen.getByRole('button', { name: '重试' }))

    expect(reset).toHaveBeenCalledTimes(1)
  })
})
