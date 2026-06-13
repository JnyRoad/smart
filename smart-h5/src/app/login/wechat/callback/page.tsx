'use client'
import { SpinLoading } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { exchangeWechatCode } from '@/features/auth/api'
import { hasValidToken, saveSession } from '@/lib/auth/token'
import { BADGE_BINDING_PATH, redirectToWechatOAuth } from '@/lib/wechat/oauth'

/** Gateway replies that require the badge-binding step before login can finish. */
const BINDING_REQUIRED_MESSAGES = ['账号未绑定工号，请先绑定', '员工状态异常']

function CallbackInner() {
  const router = useRouter()
  const code = useSearchParams().get('code')
  const startedRef = useRef(false)
  const [exchangeError, setExchangeError] = useState<string | null>(null)
  // Derived at render time: arriving without ?code= is a terminal error state.
  const error = exchangeError ?? (code ? null : '缺少微信授权参数')

  useEffect(() => {
    // React 18 StrictMode double-invokes effects; the code exchange must run once.
    if (startedRef.current) return
    startedRef.current = true

    if (!code) return

    if (hasValidToken()) {
      router.replace('/home')
      return
    }

    void (async () => {
      try {
        const res = await exchangeWechatCode(code)
        if (res.access_token) {
          saveSession({
            accessToken: res.access_token,
            refreshToken: res.refresh_token,
            expiresIn: res.expires_in,
          })
          router.replace('/home')
        } else if (res.code === 1 && BINDING_REQUIRED_MESSAGES.includes(res.data ?? '')) {
          // Unbound WeChat account: restart OAuth so the binding page gets a fresh code.
          redirectToWechatOAuth(BADGE_BINDING_PATH)
        } else {
          setExchangeError(res.message ?? '登录失败')
        }
      } catch (err) {
        setExchangeError(err instanceof Error ? err.message : '登录失败')
      }
    })()
  }, [code, router])

  if (error) {
    return (
      <div className="flex min-h-dvh flex-col items-center justify-center gap-4 px-8">
        <p className="text-center text-sm text-mid">{error}</p>
        <button
          type="button"
          onClick={() => redirectToWechatOAuth()}
          className="flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
        >
          重新授权
        </button>
      </div>
    )
  }

  return (
    <div className="flex min-h-dvh flex-col items-center justify-center gap-4">
      <SpinLoading color="primary" />
      <p className="text-sm text-mid">微信授权登录中…</p>
    </div>
  )
}

export default function WechatCallbackPage() {
  return (
    <Suspense>
      <CallbackInner />
    </Suspense>
  )
}
