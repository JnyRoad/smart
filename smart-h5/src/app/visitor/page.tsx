'use client'
import { Dialog, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { VisitorSteps } from '@/components/visitor-steps'
import { admittanceNoticeHtml, getAdmittanceNotice, getVisitorOpenId, searchReceptionist } from '@/features/visitor/api'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { getTenantConfig } from '@/lib/config/tenant'
import { sanitizeRichText } from '@/lib/sanitize'
import { stripSpaces } from '@/lib/text'
import { useMounted } from '@/lib/use-mounted'
import { redirectToWechatOAuth } from '@/lib/wechat/oauth'

/**
 * Visitor flow step 1: receptionist (被访人) info. Entering without a WeChat
 * code starts the silent OAuth round (visitor-side: the code is exchanged for
 * an openId, not a login token).
 */
function VisitorEntryInner() {
  const router = useRouter()
  const code = useSearchParams().get('code')
  const config = getTenantConfig()
  const mounted = useMounted()
  const { host, patchHost } = useVisitorFlow()
  // Form fields bind straight to the persisted draft (refill = store hydration).
  const name = host.receptionistName ?? ''
  const phone = host.receptionistPhone ?? ''
  const [submitting, setSubmitting] = useState(false)
  const startedRef = useRef(false)

  useEffect(() => {
    if (startedRef.current) return
    startedRef.current = true

    if (!code) {
      redirectToWechatOAuth('/visitor')
      return
    }

    void (async () => {
      try {
        const notice = await getAdmittanceNotice(config.parkId)
        const html = notice.code === 0 ? admittanceNoticeHtml(notice.data) : ''
        if (html) {
          void Dialog.alert({
            title: '温馨提示',
            content: (
              <div
                className="max-h-[50dvh] overflow-y-auto text-sm leading-6"
                dangerouslySetInnerHTML={{ __html: sanitizeRichText(html) }}
              />
            ),
            confirmText: '知道了',
          })
        }
      } catch {
        // Notice failures must not block the form.
      }
    })()

    void (async () => {
      try {
        const res = await getVisitorOpenId(code)
        if (res.code === 0 && res.data) {
          patchHost({ openId: res.data.openId, unionId: res.data.unionId })
        }
      } catch (error) {
        // Missing openId surfaces later at submit; do not block the form.
        Toast.show(toUserMessage(error, '微信授权信息获取失败'))
      }
    })()
  }, [code, config.parkId, patchHost])

  async function handleNext() {
    // 提交前去首尾空格。
    const receptionistName = stripSpaces(name)
    const receptionistPhone = stripSpaces(phone)
    if (!receptionistName) {
      Toast.show('请输入被访人姓名')
      return
    }
    if (!receptionistPhone) {
      Toast.show('请输入被访人手机号')
      return
    }
    if (!/^1\d{10}$/.test(receptionistPhone)) {
      Toast.show('手机号格式不正确')
      return
    }
    setSubmitting(true)
    try {
      const res = await searchReceptionist({
        parkId: config.parkId,
        receptionistName,
        receptionistPhone,
      })
      if (res.code === 0 && res.data) {
        patchHost({
          receptionistBadge: res.data.receptionistBadge,
          receptionistName: res.data.receptionistName ?? receptionistName,
          receptionistPhone: res.data.receptionistPhone ?? receptionistPhone,
        })
        router.push('/visitor/info')
        return
      } else {
        Toast.show(res.message ?? '查询被访人失败')
      }
    } catch (error) {
      Toast.show(toUserMessage(error, '查询被访人失败'))
    } finally {
      setSubmitting(false)
    }
  }

  // Avoid form flash before the OAuth redirect kicks in / during hydration.
  if (!code || !mounted) return null

  return (
    <div className="min-h-dvh bg-[linear-gradient(180deg,rgba(255,241,227,0.72)_0,rgba(255,255,255,0)_220px)] px-3 pt-[max(14px,env(safe-area-inset-top))] pb-[max(24px,env(safe-area-inset-bottom))]">
      <header className="mb-3 flex min-h-[42px] items-center px-1.5 text-base font-bold">
        裕同<em className="text-brand not-italic">智慧园区</em> · 入厂申请
      </header>

      <VisitorSteps current={1} />

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <h2 className="text-[15px] font-bold">被访者信息</h2>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">所选园区</label>
          <input value={config.parkName} readOnly className={`${SMS_INPUT_CLASS} bg-surface`} />
        </div>
        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="r-name">
            姓名 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="r-name"
            placeholder="请输入被访人姓名"
            value={name}
            onChange={(e) => patchHost({ receptionistName: e.target.value })}
            className={SMS_INPUT_CLASS}
          />
        </div>
        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="r-phone">
            手机号 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="r-phone"
            type="tel"
            inputMode="numeric"
            maxLength={11}
            placeholder="请输入被访人手机号"
            value={phone}
            onChange={(e) => patchHost({ receptionistPhone: e.target.value })}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <button
          type="button"
          disabled={submitting}
          onClick={() => void handleNext()}
          className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
        >
          {submitting ? '查询中…' : '下一步'}
        </button>

        <button
          type="button"
          onClick={() => router.push('/visitor/records')}
          className="mt-3.5 flex w-full items-center justify-center gap-1 text-[13px] text-mid"
        >
          已提交过申请？<span className="font-semibold text-brand">查看申请记录与审批进度 ›</span>
        </button>
      </section>
    </div>
  )
}

export default function VisitorEntryPage() {
  return (
    <Suspense>
      <VisitorEntryInner />
    </Suspense>
  )
}
