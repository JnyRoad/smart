'use client'
import { Toast } from 'antd-mobile'
import { useEffect, useId, useRef, useState } from 'react'

const SMS_COUNTDOWN_SECONDS = 120

export const SMS_INPUT_CLASS =
  'h-12 w-full min-w-0 flex-1 rounded-xl border border-border-soft bg-white px-3.5 ' +
  'text-ink placeholder:text-weak focus:outline-3 focus:outline-[rgba(236,108,0,0.22)]'

/**
 * Phone + SMS-code field pair with the legacy 120s resend countdown.
 * `onSend` performs the actual send request; validation of the phone format
 * happens here so every caller behaves identically.
 *
 * CONTRACT: `onSend` MUST reject when the send fails — including a 200 response
 * carrying a non-zero business code (e.g. rate-limit「提交过快」). Only a resolved
 * promise starts the countdown; a reject surfaces the error and keeps the button
 * clickable. Callers that merely `await` the request without checking the code
 * will falsely start the countdown while no SMS was sent.
 */
export function SmsCodeField({
  phone,
  code,
  onPhoneChange,
  onCodeChange,
  onSend,
  phonePlaceholder = '点击输入手机号',
  codePlaceholder = '点击输入验证码',
}: {
  phone: string
  code: string
  onPhoneChange: (v: string) => void
  onCodeChange: (v: string) => void
  onSend: (phone: string) => Promise<void>
  phonePlaceholder?: string
  codePlaceholder?: string
}) {
  const fieldId = useId()
  const [countdown, setCountdown] = useState(0)
  const [sending, setSending] = useState(false)
  const timerRef = useRef<ReturnType<typeof setInterval> | undefined>(undefined)

  useEffect(() => () => clearInterval(timerRef.current), [])

  async function handleSend() {
    if (!phone) {
      Toast.show('请输入手机号')
      return
    }
    if (!/^1\d{10}$/.test(phone)) {
      Toast.show('手机号格式不正确')
      return
    }
    setSending(true)
    try {
      await onSend(phone)
      Toast.show('发送成功')
      setCountdown(SMS_COUNTDOWN_SECONDS)
      timerRef.current = setInterval(() => {
        setCountdown((left) => {
          if (left <= 1) clearInterval(timerRef.current)
          return left - 1
        })
      }, 1000)
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '发送失败')
    } finally {
      setSending(false)
    }
  }

  return (
    <div>
      <div className="mt-3">
        <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor={`${fieldId}-phone`}>
          手机号
        </label>
        <input
          id={`${fieldId}-phone`}
          type="tel"
          inputMode="numeric"
          maxLength={11}
          placeholder={phonePlaceholder}
          value={phone}
          onChange={(e) => onPhoneChange(e.target.value)}
          className={SMS_INPUT_CLASS}
        />
      </div>
      <div className="mt-3">
        <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor={`${fieldId}-code`}>
          验证码
        </label>
        <div className="flex items-center gap-2.5">
          <input
            id={`${fieldId}-code`}
            type="text"
            inputMode="numeric"
            maxLength={6}
            placeholder={codePlaceholder}
            value={code}
            onChange={(e) => onCodeChange(e.target.value.slice(0, 6))}
            className={SMS_INPUT_CLASS}
          />
          <button
            type="button"
            disabled={sending || countdown > 0}
            onClick={() => void handleSend()}
            className="h-12 min-w-[108px] shrink-0 rounded-xl border border-[rgba(236,108,0,0.36)] bg-accent-soft px-3 text-sm font-semibold text-[#d95f00] disabled:border-border-soft disabled:bg-surface disabled:text-weak"
          >
            {sending ? '发送中…' : countdown > 0 ? `${countdown}s` : '获取验证码'}
          </button>
        </div>
      </div>
    </div>
  )
}
