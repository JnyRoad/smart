'use client'
import { SpinLoading } from 'antd-mobile'
import { useEffect } from 'react'
import { redirectToWechatOAuth } from '@/lib/wechat/oauth'

/** Trampoline page: immediately starts the WeChat silent OAuth flow. */
export default function WechatLoginPage() {
  useEffect(() => {
    redirectToWechatOAuth()
  }, [])

  return (
    <div className="flex min-h-dvh flex-col items-center justify-center gap-4">
      <SpinLoading color="primary" />
      <p className="text-sm text-mid">正在跳转微信授权…</p>
    </div>
  )
}
