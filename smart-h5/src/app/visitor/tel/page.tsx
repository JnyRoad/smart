'use client'
import { Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { SmsCodeField } from '@/components/sms-code-field'
import { VisitorSteps } from '@/components/visitor-steps'
import {
  checkBlackVisitor,
  saveVisitorApply,
  sendVisitorSms,
  verifyVisitorSms,
} from '@/features/visitor/api'
import { buildVisitorAreaFields } from '@/features/visitor/area-fields'
import { loadAreaOptions, pruneSelectedAreas } from '@/features/visitor/area-options'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { getTenantConfig } from '@/lib/config/tenant'
import { stripSpaces } from '@/lib/text'
import { useMounted } from '@/lib/use-mounted'

/** Legacy fixed values: personType 3 = 普通来访, thing 4 = 无携带物品. */
const PERSON_TYPE_ORDINARY = 3
const THING_NONE = 4

/** Visitor flow step 3: SMS verification and the final application submit. */
export default function VisitorTelPage() {
  const router = useRouter()
  const config = getTenantConfig()
  const mounted = useMounted()
  const flow = useVisitorFlow()
  const [smsCode, setSmsCode] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // Draft-integrity guard: direct entry (deep link / back after submit) with an
  // incomplete draft would otherwise submit a garbage payload. Captured once at
  // entry — the post-submit reset() must not re-trigger it mid-navigation.
  const [draftCompleteAtEntry] = useState(() => {
    const { host, visitor } = useVisitorFlow.getState()
    return Boolean(host.receptionistBadge && visitor.visitorName && visitor.startTime)
  })
  useEffect(() => {
    if (mounted && !draftCompleteAtEntry) {
      const { host } = useVisitorFlow.getState()
      router.replace(host.receptionistBadge ? '/visitor/info' : '/visitor')
    }
  }, [mounted, draftCompleteAtEntry, router])

  async function handleSubmit() {
    const { phone } = flow
    if (!phone) return Toast.show('请输入手机号')
    if (!/^1\d{10}$/.test(phone)) return Toast.show('手机号格式不正确')
    if (!smsCode) return Toast.show('请输入验证码')

    setSubmitting(true)
    try {
      const verify = await verifyVisitorSms(phone, smsCode)
      if (verify.code !== 0) {
        Toast.show(verify.message ?? '验证码校验失败')
        return
      }

      // Re-check the area config and prune selections that expired meanwhile.
      const options = await loadAreaOptions(config.parkId)
      const pruned = pruneSelectedAreas(flow.areasByFactory, options)
      const prunedCount = Object.values(pruned).reduce((n, a) => n + a.list.length, 0)
      if (prunedCount === 0) {
        flow.replaceAreas(pruned)
        Toast.show('请选择授权区域')
        router.push('/visitor/info')
        return
      }
      flow.replaceAreas(pruned)
      // Keep permitFactoryType consistent if the current factory got pruned away.
      let permitFactoryType = flow.visitor.permitFactoryType
      if (permitFactoryType && !pruned[permitFactoryType]) {
        permitFactoryType = Object.keys(pruned)[0]
        flow.patchVisitor({ permitFactoryType })
      }

      // 黑名单校验用的姓名/证件号必须与下方入库 save 同口径（去空格、证件号大写），
      // 否则会出现「按清洗后入库、却按原始值查黑名单」的漏判缝隙。
      const black = await checkBlackVisitor({
        visitorName: stripSpaces(flow.visitor.visitorName),
        certNo: stripSpaces(flow.visitor.certNo).toUpperCase(),
        parkId: config.parkId,
      })
      // Security check must fail closed: a service error blocks the submit.
      if (black.code !== 0) {
        Toast.show(black.message ?? '黑名单校验失败，请稍后重试')
        return
      }
      if (black.data === false) {
        Toast.show('抱歉，你已被加入访客黑名单，不能进行入厂申请!')
        return
      }

      // 区域字段按旧版 save 语义（tel.vue:174-201，与已修好的 equal/check 同口径）：
      // permitArea/permitOldArea 是当前工厂的自定义文本（非 code 拼接），
      // areaType 是当前工厂勾选的区域 code 列表，remark 旧版恒为空串。
      const area = buildVisitorAreaFields(permitFactoryType, pruned, options)

      const res = await saveVisitorApply({
        parkId: config.parkId,
        unionId: flow.host.unionId ?? '',
        openId: flow.host.openId ?? '',
        receptionistBadge: flow.host.receptionistBadge,
        receptionistName: flow.host.receptionistName,
        receptionistPhone: flow.host.receptionistPhone,
        visitorName: stripSpaces(flow.visitor.visitorName),
        visitorPhotoId: flow.visitor.visitorPhotoId,
        visitorPhone: phone,
        startTime: `${flow.visitor.startTime}:00`,
        endTime: `${flow.visitor.endTime}:00`,
        company: flow.visitor.company.trim(),
        cause: flow.visitor.cause?.code ?? '',
        personType: PERSON_TYPE_ORDINARY,
        thing: THING_NONE,
        remark: '',
        permitFactoryType: area.permitFactoryType,
        permitArea: area.permitArea,
        permitOldArea: area.permitOldArea,
        // save 接口要求数字数组：旧版 tel.vue 提交前对每个 code parseInt，真机已
        // 证实传字符串会报「授权区域不正确」。区域 code 本质是数字字符串（旧版
        // normalizeAreaCode=String(code)），Number 安全。equal/check 反而接受字符
        // 串数组（旧版 validateRepeat 原样传、已通过），故只在此处转数字。
        areaType: area.areaType.map(Number),
        fellowList: [
          {
            isMain: 1,
            fellowName: stripSpaces(flow.visitor.visitorName),
            fellowPhotoId: flow.visitor.visitorPhotoId,
            certNo: stripSpaces(flow.visitor.certNo).toUpperCase(),
          },
          ...flow.fellows.map((f) => ({
            isMain: 0,
            fellowName: stripSpaces(f.fellowName),
            fellowPhotoId: f.fellowPhotoId,
            certNo: stripSpaces(f.certNo).toUpperCase(),
          })),
        ],
        vehicleList: flow.cars.map((c) => ({
          plate: stripSpaces(c.plate),
          name: stripSpaces(c.name),
          certType: c.certType.code,
          certImg: c.certImg,
        })),
      })
      if (res.code === 0) {
        Toast.show('申请成功')
        flow.reset()
        router.push('/visitor/result')
      } else {
        Toast.show(res.message ?? '提交失败')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '提交失败')
    } finally {
      setSubmitting(false)
    }
  }

  if (!mounted) return null

  return (
    <div className="min-h-dvh px-3 pt-[max(14px,env(safe-area-inset-top))] pb-[max(24px,env(safe-area-inset-bottom))]">
      <header className="mb-3 flex min-h-[42px] items-center px-1.5 text-base font-bold">
        裕同<em className="text-brand not-italic">智慧园区</em> · 入厂申请
      </header>

      <VisitorSteps current={3} />

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <h2 className="text-[15px] font-bold">手机验证</h2>
        <p className="mt-1 text-[13px] text-mid">请验证访客本人手机号，验证通过后提交申请</p>

        <SmsCodeField
          phone={flow.phone}
          code={smsCode}
          onPhoneChange={flow.setPhone}
          onCodeChange={setSmsCode}
          onSend={async (mobile) => {
            const res = await sendVisitorSms(mobile)
            if (res.code !== 0) throw new Error(res.message ?? '验证码发送失败')
          }}
        />

        <button
          type="button"
          disabled={submitting}
          onClick={() => void handleSubmit()}
          className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
        >
          {submitting ? '正在提交' : '下一步'}
        </button>
      </section>
    </div>
  )
}
