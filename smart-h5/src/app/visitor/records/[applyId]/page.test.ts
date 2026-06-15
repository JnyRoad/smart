// happy-dom 对 dangerouslySetInnerHTML 的 HTML 解析和样式属性保留不够接近浏览器；
// 这里用 jsdom 覆盖截图里的审批意见 HTML 渲染回归。
// @vitest-environment jsdom
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { act, cleanup, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import RecordDetailPage from './page'

const routerMock = vi.hoisted(() => ({
  back: vi.fn(),
  push: vi.fn(),
  replace: vi.fn(),
}))

const recordsMock = vi.hoisted(() => ({
  detail: {
    code: 0,
    data: {
      applyId: 'apply-1',
      applyNo: 'VA20260615-0001',
      parkName: '裕同科技许昌园区',
      applyStatus: 'PASSED',
      dispatchStatus: 'SUCCESS',
      receptionistName: '李世勋',
      startTime: '2026-06-16 18:31',
      endTime: '2026-06-17 21:31',
      cause: '送货',
      visitorName: '张三',
      visitorPhone: '137****1234',
      fellows: [],
      vehicles: [],
      areas: [],
      submitTime: '2026-06-13 18:34',
    },
  },
  progress: {
    code: 0,
    data: {
      nodes: [
        {
          title: '保密主管审核',
          state: 'done',
          statusText: '批准',
          approverName: '罗浩',
          time: '2026-06-15 15:56',
          comment: "<br><br><span style='font-size:11px;color:#666;'>来自微信公众号</span>",
        },
      ],
    },
  },
}))

vi.mock('next/navigation', () => ({
  useRouter: () => routerMock,
}))

vi.mock('antd-mobile', async () => {
  const ReactModule = await import('react')

  return {
    ErrorBlock: ({ title, description }: { title?: string; description?: string }) =>
      ReactModule.createElement('div', null, title ?? description),
    NavBar: ({ children }: { children: React.ReactNode }) => ReactModule.createElement('div', null, children),
    PullToRefresh: ({ children }: { children: React.ReactNode }) => ReactModule.createElement('div', null, children),
    SpinLoading: () => ReactModule.createElement('div', null, 'loading'),
  }
})

vi.mock('@/features/visitor/records-api', () => ({
  clearQuerySession: vi.fn(),
  fetchApplyDetail: vi.fn(() => Promise.resolve(recordsMock.detail)),
  fetchApprovalProgress: vi.fn(() => Promise.resolve(recordsMock.progress)),
  getQuerySession: () => ({ queryToken: 'query-token', maskedName: '张三', maskedMobile: '137****1234' }),
  isAuthRejected: () => false,
}))

beforeEach(() => {
  recordsMock.progress.data.nodes[0]!.comment =
    "<br><br><span style='font-size:11px;color:#666;'>来自微信公众号</span>"
})

async function renderRecordDetail() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })

  let rendered: ReturnType<typeof render> | undefined
  await act(async () => {
    rendered = render(
      React.createElement(
        QueryClientProvider,
        { client: queryClient },
        React.createElement(
          React.Suspense,
          { fallback: React.createElement('div', null, 'pending') },
          React.createElement(RecordDetailPage, { params: Promise.resolve({ applyId: 'apply-1' }) }),
        ),
      ),
    )
  })
  return rendered!
}

afterEach(() => {
  cleanup()
  routerMock.back.mockClear()
  routerMock.push.mockClear()
  routerMock.replace.mockClear()
})

describe('访客申请详情审批意见', () => {
  it('安全解析后端返回的 HTML 意见，不把标签源码显示给用户', async () => {
    await renderRecordDetail()

    expect(await screen.findByText('来自微信公众号')).toBeTruthy()

    expect(document.body.textContent).not.toContain('<br>')
    expect(document.body.textContent).not.toContain('<span')
    expect(document.body.querySelector('span[style*="font-size"]')?.textContent).toBe('来自微信公众号')
  })

  it('去掉审批意见开头的空白换行，不为公众号来源提示撑出空白行', async () => {
    await renderRecordDetail()

    const sourceHint = await screen.findByText('来自微信公众号')
    const commentBox = sourceHint.closest('div')

    expect(commentBox?.innerHTML).not.toMatch(/^<br\s*\/?><br\s*\/?>/i)
    expect(commentBox?.firstElementChild?.textContent).toBe('来自微信公众号')
  })

  it('压缩空白行时保留审批意见里的合法图片', async () => {
    recordsMock.progress.data.nodes[0]!.comment =
      '<br><img src="https://cdn.example/a.png" alt="审批附件"><p><img src="https://cdn.example/b.png" alt="补充附件"></p>'

    await renderRecordDetail()

    expect(await screen.findByAltText('审批附件')).toBeTruthy()
    expect(await screen.findByAltText('补充附件')).toBeTruthy()
  })

  it('压缩空白行时不破坏审批意见里的表格和列表结构', async () => {
    recordsMock.progress.data.nodes[0]!.comment =
      '<table><tbody><tr><td></td><td>说明</td></tr></tbody></table><ul><li></li><li>下一项</li></ul>'

    await renderRecordDetail()

    expect(await screen.findByText('说明')).toBeTruthy()
    expect(document.body.querySelectorAll('td')).toHaveLength(2)
    expect(document.body.querySelectorAll('li')).toHaveLength(2)
  })

  it('清洗审批意见里的危险 HTML', async () => {
    recordsMock.progress.data.nodes[0]!.comment =
      '<img src="x" onerror="alert(1)"><span onclick="evil()">安全内容</span>'

    await renderRecordDetail()

    expect(await screen.findByText('安全内容')).toBeTruthy()
    await waitFor(() => expect(document.body.innerHTML).not.toContain('onerror'))
    expect(document.body.innerHTML).not.toContain('onclick')
  })
})
