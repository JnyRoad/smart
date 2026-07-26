'use client'
import { useQuery } from '@tanstack/react-query'
import { DatePicker, Picker, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { FaceUpload } from '@/components/face-upload'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { VisitorSteps } from '@/components/visitor-steps'
import { checkApplyEqual, getCauseEnum, type FactoryAreaConfig } from '@/features/visitor/api'
import { buildVisitorAreaFields } from '@/features/visitor/area-fields'
import { validateVisitorName } from '@/features/visitor/visitor-name'
import { stripSpaces } from '@/lib/text'
import { loadAreaOptions } from '@/features/visitor/area-options'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { validateIdCard } from '@/features/visitor/id-card'
import { getTenantConfig } from '@/lib/config/tenant'
import { useMounted } from '@/lib/use-mounted'
import { useEffect } from 'react'

const MAX_VISIT_RANGE_DAYS = 365

function formatDateTime(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** Parses `YYYY-MM-DD HH:mm`; non-ISO Date parsing is implementation-defined. */
function parseDateTime(value: string): Date | null {
  const d = new Date(value.replace(' ', 'T'))
  return Number.isNaN(d.getTime()) ? null : d
}

function FieldLabel({ text, required = true }: { text: string; required?: boolean }) {
  return (
    <label className="mb-1.5 block text-[13px] font-semibold text-mid">
      {text} {required && <span className="text-[#d83b36]">*</span>}
    </label>
  )
}

/** Visitor flow step 2: the visitor's own info, areas, times and fellows entry. */
export default function VisitorInfoPage() {
  const router = useRouter()
  const config = getTenantConfig()
  const mounted = useMounted()
  const { host, visitor, areasByFactory, fellows, patchVisitor, setFactoryAreas, replaceAreas } =
    useVisitorFlow()
  const [submitting, setSubmitting] = useState(false)
  const [causeVisible, setCauseVisible] = useState(false)
  const [startVisible, setStartVisible] = useState(false)
  const [endVisible, setEndVisible] = useState(false)

  // Draft-integrity guard: step 2 needs the receptionist from step 1.
  useEffect(() => {
    if (mounted && !useVisitorFlow.getState().host.receptionistBadge) {
      router.replace('/visitor')
    }
  }, [mounted, router])

  const visitorDraft = host.visitorDraftToken && host.visitorDraftId
    ? { draftToken: host.visitorDraftToken, draftId: host.visitorDraftId }
    : null
  const causeEnum = useQuery({
    queryKey: ['visitor', 'cause-enum', visitorDraft?.draftId],
    queryFn: () => getCauseEnum(visitorDraft!),
    enabled: visitorDraft !== null,
  })
  const areaOptions = useQuery({
    queryKey: ['visitor', 'area-options', config.parkId, visitorDraft?.draftId],
    queryFn: () => loadAreaOptions(config.parkId, visitorDraft!),
    enabled: visitorDraft !== null,
  })

  const factories: FactoryAreaConfig[] = areaOptions.data ?? []
  const currentFactory = factories.find((f) => f.factoryType === visitor.permitFactoryType)
  const inlineLimit = currentFactory?.inlineAreaLimit ?? 4
  // 旧版语义（visitorInfo.vue:248-252）：区域数不超过 inlineLimit 时全部内联展示；
  // 超出时优先展示「常用区域」(isCommon)，若没有任何常用区域（真机 isCommon 普遍为
  // false）再回退展示全部，并截断到 inlineLimit。否则内联区域会整列消失。
  const allAreas = currentFactory?.areas ?? []
  const preferredAreas = allAreas.filter((a) => a.isCommon === 1)
  const commonAreas =
    allAreas.length <= inlineLimit
      ? allAreas
      : (preferredAreas.length > 0 ? preferredAreas : allAreas).slice(0, inlineLimit)
  const selectedAreas = visitor.permitFactoryType
    ? (areasByFactory[visitor.permitFactoryType]?.list ?? [])
    : []
  const totalSelectedCount = Object.values(areasByFactory).reduce((n, a) => n + a.list.length, 0)

  function selectFactory(factoryType: string | undefined) {
    if (!factoryType || factoryType === visitor.permitFactoryType) return
    // Legacy behavior: switching factory clears selections made for other factories.
    patchVisitor({ permitFactoryType: factoryType })
    const kept = areasByFactory[factoryType]
    replaceAreas(kept ? { [factoryType]: kept } : {})
  }

  function toggleInlineArea(code: string) {
    if (!visitor.permitFactoryType) return
    const current = areasByFactory[visitor.permitFactoryType] ?? { list: [], custom: '' }
    const list = current.list.includes(code)
      ? current.list.filter((c) => c !== code)
      : [...current.list, code]
    setFactoryAreas(visitor.permitFactoryType, { ...current, list })
  }

  async function handleNext() {
    if (!visitorDraft) return Toast.show('访客操作授权已失效，请重新进入申请流程')
    // 姓名去全部空格（含中间），单位仅去首尾；写回草稿保持显示一致。
    const visitorName = stripSpaces(visitor.visitorName)
    const company = visitor.company.trim()
    if (visitorName !== visitor.visitorName || company !== visitor.company) {
      patchVisitor({ visitorName, company })
    }
    if (!visitorName) return Toast.show('请输入访客姓名')
    const nameCheck = validateVisitorName(visitorName)
    if (!nameCheck.ok) return Toast.show(nameCheck.message)
    if (!visitor.visitorPhotoId) return Toast.show('请上传访客照片')
    if (!visitor.certNo) return Toast.show('请输入证件号码')
    if (!company) return Toast.show('请输入来访单位')
    if (!visitor.cause) return Toast.show('请选择来访事由')
    if (!visitor.permitFactoryType || totalSelectedCount === 0) {
      return Toast.show('授权区域不能为空！')
    }
    if (!visitor.startTime) return Toast.show('请选择来访时间')
    if (!visitor.endTime) return Toast.show('请选择离开时间')
    const start = parseDateTime(visitor.startTime)
    const end = parseDateTime(visitor.endTime)
    if (!start || !end) return Toast.show('时间格式不正确，请重新选择')
    if (end <= start) {
      // Legacy behavior: an invalid end time is cleared so it must be re-picked.
      patchVisitor({ endTime: '' })
      return Toast.show('离开时间应大于来访时间!')
    }
    if (end.getTime() - start.getTime() > MAX_VISIT_RANGE_DAYS * 24 * 3600 * 1000) {
      return Toast.show('申请时间最多不允许超过365天!')
    }
    const certNo = stripSpaces(visitor.certNo).toUpperCase()
    if (certNo !== visitor.certNo) patchVisitor({ certNo })
    const idCheck = validateIdCard(certNo)
    if (!idCheck.ok) return Toast.show(idCheck.message)

    setSubmitting(true)
    try {
      // 请求体按旧版 validateRepeat（visitorInfo.vue:304-339）逐字段组装：顶层实名+
      // 区域字段，访客与随行合并进 fellowList（主访客 isMain=1，随行 isMain=0）。
      const area = buildVisitorAreaFields(visitor.permitFactoryType, areasByFactory, factories)
      const res = await checkApplyEqual({
        receptionistBadge: host.receptionistBadge,
        visitorName,
        visitorPhotoId: visitor.visitorPhotoId,
        startTime: `${visitor.startTime}:00`,
        endTime: `${visitor.endTime}:00`,
        company,
        permitArea: area.permitArea,
        permitOldArea: area.permitOldArea,
        permitFactoryType: area.permitFactoryType,
        areaType: area.areaType,
        cause: visitor.cause?.code ?? '',
        thing: 4,
        fellowList: [
          {
            certNo,
            fellowName: visitorName,
            fellowPhotoId: visitor.visitorPhotoId,
            isMain: 1,
            nativePlace: '',
          },
          ...fellows.map((f) => ({
            certNo: stripSpaces(f.certNo).toUpperCase(),
            fellowName: stripSpaces(f.fellowName),
            fellowPhotoId: f.fellowPhotoId,
            isMain: 0,
            nativePlace: '',
          })),
        ],
      }, visitorDraft)
      // 旧版成功判定：code===0 且 data 为真值。
      if (res.code === 0 && res.data) {
        router.push('/visitor/tel')
      } else {
        Toast.show(res.message ?? '验证失败')
      }
    } catch (error) {
      Toast.show(toUserMessage(error, '验证失败'))
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

      <VisitorSteps current={2} />

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <h2 className="text-[15px] font-bold">访客信息</h2>

        <div className="mt-3">
          <FieldLabel text="访客姓名" />
          <input
            placeholder="请输入访客姓名"
            value={visitor.visitorName}
            onChange={(e) => patchVisitor({ visitorName: e.target.value })}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="访客照片" />
          <FaceUpload
            mode="face"
            value={visitor.visitorPhotoId}
            onChange={(photoId) => patchVisitor({ visitorPhotoId: photoId })}
            label="点击拍摄/上传人脸照片"
			visitorFaceDraft={host.visitorDraftToken && host.visitorDraftId
			  ? { draftToken: host.visitorDraftToken, draftId: host.visitorDraftId }
			  : undefined}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="证件号码" />
          <input
            placeholder="请输入身份证号码"
            value={visitor.certNo}
            onChange={(e) => patchVisitor({ certNo: e.target.value })}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="来访单位" />
          <input
            placeholder="请输入来访单位"
            value={visitor.company}
            onChange={(e) => patchVisitor({ company: e.target.value })}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="来访事由" />
          <button type="button" onClick={() => setCauseVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {visitor.cause?.desc ?? <span className="text-weak">请选择来访事由</span>}
          </button>
          <Picker
            visible={causeVisible}
            onClose={() => setCauseVisible(false)}
            columns={[(causeEnum.data?.data ?? []).map((c) => ({ label: c.desc, value: String(c.code) }))]}
            onConfirm={(v) => {
              const item = (causeEnum.data?.data ?? []).find((c) => String(c.code) === v[0])
              if (item) patchVisitor({ cause: item })
            }}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="区域类型" />
          {areaOptions.isError ? (
            <p className="text-sm text-[#d83b36]">授权区域配置不可用，请联系管理员</p>
          ) : (
            <div className="grid grid-cols-2 gap-2">
              {factories.map((f) => {
                const active = f.factoryType === visitor.permitFactoryType
                const count = f.factoryType ? (areasByFactory[f.factoryType]?.list.length ?? 0) : 0
                return (
                  <button
                    key={f.factoryType}
                    type="button"
                    onClick={() => selectFactory(f.factoryType)}
                    className={`rounded-xl border p-3 text-left text-sm font-semibold ${
                      active ? 'border-brand bg-accent-soft text-accent-ink' : 'border-border-soft bg-surface text-mid'
                    }`}
                  >
                    {f.factoryName}
                    <span className="mt-1 block text-xs font-normal text-weak">已选 {count}</span>
                  </button>
                )
              })}
            </div>
          )}
        </div>

        {visitor.permitFactoryType && currentFactory && (
          <div className="mt-3">
            <FieldLabel text="授权区域" />
            <div className="flex flex-wrap gap-2">
              {commonAreas.map((a) => {
                const on = selectedAreas.includes(a.code)
                return (
                  <button
                    key={a.code}
                    type="button"
                    onClick={() => toggleInlineArea(a.code)}
                    className={`min-h-9 rounded-full border px-3.5 text-[13px] ${
                      on ? 'border-brand bg-accent-soft font-semibold text-accent-ink' : 'border-border-soft bg-white text-mid'
                    }`}
                  >
                    {a.name}
                  </button>
                )
              })}
              <button
                type="button"
                onClick={() =>
                  router.push(
                    `/visitor/area?type=${currentFactory.areaFlag ?? ''}&factoryType=${encodeURIComponent(currentFactory.factoryType ?? '')}&parkId=${config.parkId}`,
                  )
                }
                className="min-h-9 rounded-full border border-dashed border-brand px-3.5 text-[13px] text-brand"
              >
                更多区域（已选 {selectedAreas.length}）
              </button>
            </div>
          </div>
        )}

        <div className="mt-3">
          <FieldLabel text="来访时间" />
          <button type="button" onClick={() => setStartVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {visitor.startTime || <span className="text-weak">请选择来访时间</span>}
          </button>
          <DatePicker
            visible={startVisible}
            onClose={() => setStartVisible(false)}
            precision="minute"
            onConfirm={(d) => {
              // Legacy behavior: changing the start time clears the end time.
              patchVisitor({ startTime: formatDateTime(d), endTime: '' })
            }}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="离开时间" />
          <button type="button" onClick={() => setEndVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {visitor.endTime || <span className="text-weak">请选择离开时间</span>}
          </button>
          <DatePicker
            visible={endVisible}
            onClose={() => setEndVisible(false)}
            precision="minute"
            onConfirm={(d) => patchVisitor({ endTime: formatDateTime(d) })}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="随行人员" required={false} />
          <button
            type="button"
            onClick={() => router.push('/visitor/persons')}
            className={`${SMS_INPUT_CLASS} flex items-center justify-between text-left`}
          >
            <span className={fellows.length > 0 ? '' : 'text-weak'}>
              {fellows.length > 0 ? `${fellows.length}位` : '可添加随行人员'}
            </span>
            <span aria-hidden className="text-weak">
              ›
            </span>
          </button>
        </div>

        <button
          type="button"
          disabled={submitting}
          onClick={() => void handleNext()}
          className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
        >
          {submitting ? '正在验证' : '下一步'}
        </button>
      </section>
    </div>
  )
}
