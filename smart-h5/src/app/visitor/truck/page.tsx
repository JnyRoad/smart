'use client'
import { useQuery } from '@tanstack/react-query'
import { DatePicker, Picker, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { PlateInput } from '@/components/plate-input'
import { SMS_INPUT_CLASS, SmsCodeField } from '@/components/sms-code-field'
import {
  getTruckCauseEnum,
  saveTruckApply,
  sendVisitorSms,
  verifyVisitorSms,
} from '@/features/visitor/api'
import { validateVisitorName } from '@/features/visitor/visitor-name'
import { stripSpaces } from '@/lib/text'
import { useMounted } from '@/lib/use-mounted'

function formatDateTime(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * Standalone truck-driver booking: a single form + SMS verification, outside
 * the multi-step visitor flow (state is page-local like the legacy page).
 */
export default function TruckBookingPage() {
  const router = useRouter()
  const mounted = useMounted()
  const [plate, setPlate] = useState('')
  const [cause, setCause] = useState<{ code: string | number; desc: string } | undefined>()
  const [name, setName] = useState('')
  const [company, setCompany] = useState('')
  const [startTime, setStartTime] = useState('')
  const [remark, setRemark] = useState('')
  const [phone, setPhone] = useState('')
  const [smsCode, setSmsCode] = useState('')
  const [causeVisible, setCauseVisible] = useState(false)
  const [timeVisible, setTimeVisible] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const causeEnum = useQuery({ queryKey: ['visitor', 'truck-cause-enum'], queryFn: getTruckCauseEnum })

  async function handleSubmit() {
    // 提交前去首尾空格（含车牌，避免误带空格卡在格式校验）。
    const visitorName = stripSpaces(name)
    const departure = company.trim()
    const plateNo = stripSpaces(plate)
    if (!phone) return Toast.show('请输入手机号')
    if (!/^1\d{10}$/.test(phone)) return Toast.show('手机号格式不正确')
    if (!smsCode) return Toast.show('请输入验证码')
    if (!/^[一-龥][A-HJ-NP-Z][A-Z0-9]{5,6}$/.test(plateNo)) return Toast.show('请输入正确的车牌号')
    if (!cause) return Toast.show('请选择来访事由')
    if (!visitorName) return Toast.show('请输入访客姓名')
    const nameCheck = validateVisitorName(visitorName)
    if (!nameCheck.ok) return Toast.show(nameCheck.message)
    if (!departure) return Toast.show('请输入出发地')
    if (!startTime) return Toast.show('请选择预约时间')

    setSubmitting(true)
    try {
      const verify = await verifyVisitorSms(phone, smsCode)
      if (verify.code !== 0) {
        Toast.show(verify.message ?? '验证码校验失败')
        return
      }
      const res = await saveTruckApply({
        visitorName,
        visitorPhone: phone,
        remark: remark.trim(),
        cause: cause.code,
        startTime: `${startTime}:00`,
        company: departure,
        vehicleList: [{ name: visitorName, plate: plateNo }],
      })
      if (res.code === 0) {
        Toast.show('申请成功')
        router.push('/visitor/truck/result')
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
        裕同<em className="text-brand not-italic">智慧园区</em> · 货车预约
      </header>

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <h2 className="text-[15px] font-bold">货车预约信息</h2>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            车牌号 <span className="text-[#d83b36]">*</span>
          </label>
          <PlateInput value={plate} onChange={setPlate} />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            来访事由 <span className="text-[#d83b36]">*</span>
          </label>
          <button type="button" onClick={() => setCauseVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {cause?.desc ?? <span className="text-weak">请选择来访事由</span>}
          </button>
          <Picker
            visible={causeVisible}
            onClose={() => setCauseVisible(false)}
            columns={[(causeEnum.data?.data ?? []).map((c) => ({ label: c.desc, value: String(c.code) }))]}
            onConfirm={(v) => {
              const item = (causeEnum.data?.data ?? []).find((c) => String(c.code) === v[0])
              if (item) setCause(item)
            }}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="t-name">
            访客姓名 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="t-name"
            placeholder="请输入访客姓名"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="t-company">
            出发地 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="t-company"
            placeholder="请输入出发地"
            value={company}
            onChange={(e) => setCompany(e.target.value)}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            预约时间 <span className="text-[#d83b36]">*</span>
          </label>
          <button type="button" onClick={() => setTimeVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {startTime || <span className="text-weak">请选择预约时间</span>}
          </button>
          <DatePicker
            visible={timeVisible}
            onClose={() => setTimeVisible(false)}
            precision="minute"
            onConfirm={(d) => setStartTime(formatDateTime(d))}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="t-remark">
            备注
          </label>
          <input
            id="t-remark"
            placeholder="请填写内托/原材/成品/其他"
            value={remark}
            onChange={(e) => setRemark(e.target.value)}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <h2 className="mt-5 text-[15px] font-bold">手机验证</h2>
        <SmsCodeField
          phone={phone}
          code={smsCode}
          onPhoneChange={setPhone}
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
          {submitting ? '正在提交...' : '提交申请'}
        </button>
      </section>
    </div>
  )
}
