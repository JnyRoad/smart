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
  it('自带 html 与 body 包裹（验证组件输出而非 jsdom 宿主）', () => {
    renderGlobalError()

    // jsdom 初始文档本就有 html/body，断言它们恒真、证明不了组件。
    // 组件给 <html> 设了 lang="zh-CN"，宿主默认没有；据此证明组件确实渲染了自己的 html，
    // 再用 body 内的「重试」按钮证明 body 被渲染。
    expect(document.documentElement.getAttribute('lang')).toBe('zh-CN')
    expect(document.querySelector('body button')).toBeTruthy()
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
