// @vitest-environment jsdom
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import React from 'react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import GlobalError from './global-error'

afterEach(cleanup)

function renderGlobalError(reset = vi.fn()) {
  const error = new Error('根布局崩溃的敏感细节')
  // global-error 自带 <html><body>，挂到 document 上避免 jsdom 校验报错
  render(React.createElement(GlobalError, { error, reset }), {
    container: document.documentElement,
    baseElement: document.documentElement,
  })
  return { error, reset }
}

describe('根级错误边界', () => {
  it('自带 html 与 body 包裹', () => {
    renderGlobalError()

    expect(document.querySelector('html body')).toBeTruthy()
  })

  it('展示友好的中文降级文案，且不暴露原始 message', () => {
    renderGlobalError()

    expect(screen.getByText('页面加载失败')).toBeTruthy()
    expect(screen.queryByText('根布局崩溃的敏感细节')).toBeNull()
  })

  it('点击重试调用 reset', () => {
    const { reset } = renderGlobalError()

    fireEvent.click(screen.getByRole('button', { name: '重试' }))

    expect(reset).toHaveBeenCalledTimes(1)
  })
})
