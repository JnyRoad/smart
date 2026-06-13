'use client'
import { useEffect } from 'react'
import '@/lib/react19-compat'
import { setInvalidTokenHandler } from '@/lib/api/http'
import { markTokenInvalid } from '@/lib/auth/token'
import { redirectToWechatOAuth } from '@/lib/wechat/oauth'

/**
 * Installs the legacy 401 invalid_token behavior once per app load:
 * mark the stored token invalid and restart the WeChat silent OAuth flow.
 */
export function AuthBootstrap() {
  useEffect(() => {
    setInvalidTokenHandler(() => {
      markTokenInvalid()
      redirectToWechatOAuth()
    })
  }, [])
  return null
}
