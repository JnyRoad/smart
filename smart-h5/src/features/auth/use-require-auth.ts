'use client'
import { useEffect, useSyncExternalStore } from 'react'
import { hasValidToken } from '@/lib/auth/token'
import { redirectToWechatOAuth } from '@/lib/wechat/oauth'

const noopSubscribe = () => () => {}

/**
 * Client-side route guard: without a valid token the user is sent through the
 * WeChat silent OAuth flow (employees already bound land back signed-in).
 * Returns whether the page may render; false during SSR and while redirecting.
 */
export function useRequireAuth(): boolean {
  const authorized = useSyncExternalStore(noopSubscribe, hasValidToken, () => false)

  useEffect(() => {
    // Read the storage directly: during hydration `authorized` still holds the
    // server snapshot (false) even when a valid token exists.
    if (!hasValidToken()) redirectToWechatOAuth()
  }, [])

  return authorized
}
