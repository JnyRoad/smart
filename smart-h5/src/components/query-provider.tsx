'use client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { subscribeToSessionChanges } from '@/lib/auth/token'

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const [sessionGeneration, setSessionGeneration] = useState(0)
  const [client] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { retry: 1, refetchOnWindowFocus: false, staleTime: 30_000 },
        },
      }),
  )

  useEffect(() => {
    // 会话代际变化不含 token；清缓存并重挂载观察者，避免固定 queryKey 保留旧身份结果。
    return subscribeToSessionChanges((generation) => {
      client.clear()
      setSessionGeneration(generation)
    })
  }, [client])

  return <QueryClientProvider client={client} key={sessionGeneration}>{children}</QueryClientProvider>
}
