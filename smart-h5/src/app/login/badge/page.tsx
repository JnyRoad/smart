'use client'
import { Toast } from 'antd-mobile'
import { useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { bindEmployeeBadge } from '@/features/auth/api'
import { BADGE_BINDING_PATH, redirectToWechatOAuth } from '@/lib/wechat/oauth'

/**
 * Badge-binding page, entered via WeChat OAuth with a fresh `code`.
 * Binds the WeChat openId to an employee badge (badge + last 6 ID digits);
 * on success a new OAuth round completes the login.
 */
function BadgeBindingInner() {
  const code = useSearchParams().get('code')
  const [badge, setBadge] = useState('')
  const [lastCertNum, setLastCertNum] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const retryTimerRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined)

  useEffect(() => () => clearTimeout(retryTimerRef.current), [])

  async function handleBind() {
    if (!badge || !lastCertNum) {
      Toast.show('请填写员工号与身份证后六位')
      return
    }
    if (!code) {
      Toast.show('缺少微信授权信息，请重新进入')
      return
    }
    setSubmitting(true)
    try {
      const res = await bindEmployeeBadge({ code, badge, lastCertNum })
      if (res.code === 0) {
        Toast.show('绑定成功，正在登录…')
        redirectToWechatOAuth()
      } else {
        Toast.show(res.message ?? '绑定失败')
        // Legacy behavior: the OAuth code is single-use, so after a failure
        // restart OAuth back to this page to obtain a fresh code.
        retryTimerRef.current = setTimeout(() => redirectToWechatOAuth(BADGE_BINDING_PATH), 2000)
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '绑定失败')
      setSubmitting(false)
    }
  }

  const inputClass =
    'h-12 w-full rounded-xl border border-border-soft bg-white px-3.5 text-ink ' +
    'placeholder:text-weak focus:outline-3 focus:outline-[rgba(236,108,0,0.22)]'

  return (
    <div className="min-h-dvh bg-[linear-gradient(180deg,rgba(255,241,227,0.72)_0,rgba(255,255,255,0)_220px)] px-4.5 pt-[max(14px,env(safe-area-inset-top))] pb-[max(24px,env(safe-area-inset-bottom))]">
      <header className="flex min-h-[46px] items-center text-base font-bold">
        裕同<em className="text-brand not-italic">智慧园区</em>
      </header>

      <section className="px-0.5 pt-6 pb-5">
        <span className="mb-1.5 block text-[15px] text-mid">欢迎来到</span>
        <h1 className="text-3xl leading-tight font-bold text-brand">裕慧家园</h1>
      </section>

      <section className="rounded-2xl border border-border-soft bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <h2 className="text-lg font-bold">绑定员工号</h2>
        <p className="mt-1 text-[13px] leading-relaxed text-mid">
          首次微信登录需将微信与员工工号绑定，绑定成功后自动完成登录
        </p>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="badge">
            员工号 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="badge"
            type="text"
            placeholder="输入员工号"
            value={badge}
            onChange={(e) => setBadge(e.target.value)}
            className={inputClass}
          />
        </div>
        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="lastCertNum">
            身份证后六位 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="lastCertNum"
            type="text"
            maxLength={6}
            placeholder="输入身份证后六位"
            value={lastCertNum}
            onChange={(e) => setLastCertNum(e.target.value)}
            className={inputClass}
          />
        </div>

        <button
          type="button"
          disabled={submitting}
          onClick={handleBind}
          className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
        >
          {submitting ? '绑定中…' : '绑定'}
        </button>
      </section>
    </div>
  )
}

export default function BadgeBindingPage() {
  return (
    <Suspense>
      <BadgeBindingInner />
    </Suspense>
  )
}
