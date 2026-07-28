'use client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { subscribeToSessionChanges } from '@/lib/auth/token'
import { subscribeToQuerySessionChanges } from '@/features/visitor/records-api'

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const [cacheEpoch, setCacheEpoch] = useState(0)
  const [client] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { retry: 1, refetchOnWindowFocus: false, staleTime: 30_000 },
        },
      }),
  )

  useEffect(() => {
    // 两类会话代际都不含 token；清缓存并重挂载观察者，避免固定 queryKey 保留旧身份结果。
    const resetIdentityCache = () => {
      client.clear()
      setCacheEpoch((currentEpoch) => currentEpoch + 1)
    }
    const unsubscribeSession = subscribeToSessionChanges(resetIdentityCache)
    const unsubscribeQuerySession = subscribeToQuerySessionChanges(resetIdentityCache)
    return () => {
      unsubscribeSession()
      unsubscribeQuerySession()
    }
  }, [client])

  return <QueryClientProvider client={client} key={cacheEpoch}>{children}</QueryClientProvider>
}
