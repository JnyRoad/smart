// @vitest-environment jsdom
import { QueryClient, useQuery, useQueryClient } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import React, { useEffect } from 'react'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { clearSession, getAccessToken, saveSession } from '@/lib/auth/token'
import { saveQuerySession } from '@/features/visitor/records-api'
import { QueryProvider } from './query-provider'

function QueryClientCapture({ onReady }: { onReady: (client: QueryClient) => void }) {
  const client = useQueryClient()

  useEffect(() => {
    onReady(client)
  }, [client, onReady])

  return null
}

function renderQueryProvider(): QueryClient {
  let client: QueryClient | undefined

  render(
    React.createElement(
      QueryProvider,
      null,
      React.createElement(QueryClientCapture, { onReady: (readyClient) => { client = readyClient } }),
    ),
  )

  if (!client) throw new Error('QueryClient 未初始化')
  return client
}

function cacheDormitoryDataForCurrentUser(client: QueryClient) {
  client.setQueryData(['my-lock-pwd'], 'A 的门锁动态码')
  client.setQueryData(['my-check-in-records'], [{ roomName: 'A 的房间' }])
}

function LockCodeProbe() {
  const lockCode = useQuery({
    queryKey: ['my-lock-pwd'],
    queryFn: () => Promise.resolve(getAccessToken() === 'employee-B-token' ? 'B 的门锁动态码' : 'A 的门锁动态码'),
  })

  return React.createElement('output', { 'data-testid': 'lock-code-probe' }, lockCode.data ?? '加载中')
}

beforeEach(() => {
  localStorage.clear()
})

afterEach(() => {
  cleanup()
  clearSession()
})

describe('会话切换时的 Query 缓存隔离', () => {
  it('A 切换到 B 后不保留 A 的入住和门锁缓存', () => {
    saveSession({ accessToken: 'employee-A-token' })
    const client = renderQueryProvider()
    cacheDormitoryDataForCurrentUser(client)

    saveSession({ accessToken: 'employee-B-token' })

    expect(client.getQueryData(['my-lock-pwd'])).toBeUndefined()
    expect(client.getQueryData(['my-check-in-records'])).toBeUndefined()
  })

  it('登出后不保留当前员工的入住和门锁缓存', () => {
    saveSession({ accessToken: 'employee-A-token' })
    const client = renderQueryProvider()
    cacheDormitoryDataForCurrentUser(client)

    clearSession()

    expect(client.getQueryData(['my-lock-pwd'])).toBeUndefined()
    expect(client.getQueryData(['my-check-in-records'])).toBeUndefined()
  })

  it('其他标签页将 token 从 A 改为 B 后不保留 A 的入住和门锁缓存', () => {
    saveSession({ accessToken: 'employee-A-token' })
    const client = renderQueryProvider()
    cacheDormitoryDataForCurrentUser(client)

    localStorage.setItem(
      'xc-access_token',
      JSON.stringify({ dataType: 'string', content: 'employee-B-token', datetime: 0 }),
    )
    window.dispatchEvent(new StorageEvent('storage', { key: 'xc-access_token', storageArea: localStorage }))

    expect(client.getQueryData(['my-lock-pwd'])).toBeUndefined()
    expect(client.getQueryData(['my-check-in-records'])).toBeUndefined()
  })

  it('其他标签页清空 localStorage 后不保留当前员工的入住和门锁缓存', () => {
    saveSession({ accessToken: 'employee-A-token' })
    const client = renderQueryProvider()
    cacheDormitoryDataForCurrentUser(client)

    localStorage.clear()
    window.dispatchEvent(new StorageEvent('storage', { key: null, storageArea: localStorage }))

    expect(client.getQueryData(['my-lock-pwd'])).toBeUndefined()
    expect(client.getQueryData(['my-check-in-records'])).toBeUndefined()
  })

  it('同一 token 重复写入时保留当前员工的缓存', () => {
    saveSession({ accessToken: 'employee-A-token' })
    const client = renderQueryProvider()
    cacheDormitoryDataForCurrentUser(client)

    saveSession({ accessToken: 'employee-A-token' })

    expect(client.getQueryData(['my-lock-pwd'])).toBe('A 的门锁动态码')
    expect(client.getQueryData(['my-check-in-records'])).toEqual([{ roomName: 'A 的房间' }])
  })

  it('本标签切换访客查询凭证后不保留上一位访客的通行码缓存', () => {
    saveQuerySession({ queryToken: 'visitor-A-token', maskedName: '访客甲', maskedMobile: '137****0001' })
    const client = renderQueryProvider()
    client.setQueryData(['visitor-pass-code', 'apply-1'], '访客甲的通行码')

    saveQuerySession({ queryToken: 'visitor-B-token', maskedName: '访客乙', maskedMobile: '137****0002' })

    expect(client.getQueryData(['visitor-pass-code', 'apply-1'])).toBeUndefined()
  })

  it('其他标签切换访客查询凭证后不保留上一位访客的通行码缓存', () => {
    saveQuerySession({ queryToken: 'visitor-A-token', maskedName: '访客甲', maskedMobile: '137****0001' })
    const client = renderQueryProvider()
    client.setQueryData(['visitor-pass-code', 'apply-1'], '访客甲的通行码')

    localStorage.setItem(
      'visitor-query-session',
      JSON.stringify({
        queryToken: 'visitor-B-token',
        maskedName: '访客乙',
        maskedMobile: '137****0002',
        savedAt: Date.now(),
      }),
    )
    window.dispatchEvent(new StorageEvent('storage', { key: 'visitor-query-session', storageArea: localStorage }))

    expect(client.getQueryData(['visitor-pass-code', 'apply-1'])).toBeUndefined()
  })

  it('已挂载的门锁查询在切换到 B 后重新读取 B 的数据', async () => {
    saveSession({ accessToken: 'employee-A-token' })
    render(React.createElement(QueryProvider, null, React.createElement(LockCodeProbe)))
    expect(await screen.findByText('A 的门锁动态码')).toBeTruthy()

    saveSession({ accessToken: 'employee-B-token' })

    await waitFor(() => expect(screen.getByTestId('lock-code-probe').textContent).toBe('B 的门锁动态码'))
  })
})
