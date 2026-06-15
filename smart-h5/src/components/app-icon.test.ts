import { renderToStaticMarkup } from 'react-dom/server'
import { createElement } from 'react'
import { describe, expect, test } from 'vitest'
import { AppTileIcon, StatusIcon } from './app-icon'

describe('app icons', () => {
  test('业务入口图标渲染为主页同款 SVG 图标块', () => {
    const html = renderToStaticMarkup(createElement(AppTileIcon, { name: 'lock' }))

    expect(html).toContain('data-app-tile-icon="lock"')
    expect(html).toContain('flex-none')
    expect(html).toContain('<svg')
    expect(html).not.toMatch(/[🔐💧🏠❓💬💡📍]/u)
  })

  test('状态图标渲染为统一 SVG，不回退到 emoji', () => {
    const html = renderToStaticMarkup(createElement(StatusIcon, { name: 'denied' }))

    expect(html).toContain('data-status-icon="denied"')
    expect(html).toContain('<svg')
    expect(html).not.toMatch(/[✅❌🚫⌛⏳✕]/u)
  })
})
