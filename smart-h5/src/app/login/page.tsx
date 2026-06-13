'use client'
import { Toast } from 'antd-mobile'
import { useState } from 'react'
import { SMS_INPUT_CLASS, SmsCodeField } from '@/components/sms-code-field'
import { sendSmsCode } from '@/features/auth/api'

type LoginTab = 'sms' | 'pwd'

/**
 * Account login page (SMS / password tabs). The production entrance is the
 * WeChat OAuth flow; this page mirrors the legacy standalone login.
 * Legacy note: the old SMS submit never called a real login API (it wrote the
 * phone number as the token) — that dev remnant is not replicated.
 */
export default function LoginPage() {
  const [tab, setTab] = useState<LoginTab>('sms')
  const [phone, setPhone] = useState('')
  const [smsCode, setSmsCode] = useState('')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [agreed, setAgreed] = useState(true)

  function requireAgreement(): boolean {
    if (!agreed) {
      Toast.show('请先阅读并同意用户协议')
      return false
    }
    return true
  }

  function handleSmsLogin() {
    if (!requireAgreement()) return
    if (!phone || !smsCode) {
      Toast.show('请输入手机号和验证码')
      return
    }
    // The gateway has no SMS login endpoint yet (legacy left it unimplemented).
    Toast.show('短信登录暂未开通，请使用微信授权登录')
  }

  function handlePasswordLogin() {
    if (!requireAgreement()) return
    if (!username || !password) {
      Toast.show('请输入用户名和密码')
      return
    }
    // The legacy password login was never wired to a working endpoint (dead
    // code); surface that honestly instead of calling a guessed contract.
    Toast.show('密码登录暂未开通，请使用微信授权登录')
  }

  const inputClass = SMS_INPUT_CLASS

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
        <div
          className="mb-4 grid grid-cols-2 gap-1 rounded-[14px] border border-border-soft bg-surface p-1"
          role="tablist"
          aria-label="登录方式"
        >
          {(
            [
              ['sms', '短信登录'],
              ['pwd', '密码登录'],
            ] as const
          ).map(([key, label]) => (
            <button
              key={key}
              type="button"
              role="tab"
              aria-selected={tab === key}
              onClick={() => setTab(key)}
              className={`min-h-11 rounded-[10px] text-sm font-bold ${
                tab === key
                  ? 'bg-white text-accent-ink shadow-[0_6px_18px_rgba(89,87,87,0.08),inset_0_0_0_1px_rgba(236,108,0,0.24)]'
                  : 'text-mid'
              }`}
            >
              {label}
            </button>
          ))}
        </div>

        {tab === 'sms' ? (
          <SmsCodeField
            phone={phone}
            code={smsCode}
            onPhoneChange={setPhone}
            onCodeChange={setSmsCode}
            onSend={async (mobile) => {
              const res = await sendSmsCode(mobile)
              if (res.code !== 0) throw new Error(res.message ?? '验证码发送失败')
            }}
          />
        ) : (
          <div>
            <div className="mt-3">
              <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="username">
                用户名
              </label>
              <input
                id="username"
                type="text"
                placeholder="点击输入用户名"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className={inputClass}
              />
            </div>
            <div className="mt-3">
              <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="password">
                密码
              </label>
              <input
                id="password"
                type="password"
                placeholder="点击输入密码"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className={inputClass}
              />
            </div>
          </div>
        )}

        <label className="mx-0.5 mt-4 flex items-start gap-2 text-xs leading-relaxed text-mid">
          <input
            type="checkbox"
            checked={agreed}
            onChange={(e) => setAgreed(e.target.checked)}
            className="mt-px h-4.5 w-4.5 shrink-0 accent-brand"
          />
          <span>
            我已阅读并同意
            <span className="font-semibold text-[#d95f00]">《裕慧家园用户协议及保密承诺》</span>
          </span>
        </label>

        <button
          type="button"
          onClick={tab === 'sms' ? handleSmsLogin : handlePasswordLogin}
          className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
        >
          登录
        </button>
      </section>
    </div>
  )
}
