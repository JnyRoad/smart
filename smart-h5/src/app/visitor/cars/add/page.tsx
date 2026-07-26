'use client'
import { useQuery } from '@tanstack/react-query'
import { Picker, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { FaceUpload } from '@/components/face-upload'
import { PlateInput } from '@/components/plate-input'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { getVehicleCertEnum } from '@/features/visitor/api'
import { useVisitorFlow, type VisitorCar } from '@/features/visitor/flow-store'
import { validateVisitorName } from '@/features/visitor/visitor-name'
import { stripSpaces } from '@/lib/text'
import { PageShell } from '@/components/page-shell'
import { useMounted } from '@/lib/use-mounted'

/** Legacy default: 证件类型 身份证复印件 (code=2). */
const DEFAULT_CERT_TYPE = { code: 2, desc: '身份证复印件' }

/** Add or edit one vehicle (edit mode via `?index=`). */
function CarFormInner() {
  const router = useRouter()
  const mounted = useMounted()
  const indexParam = useSearchParams().get('index')
  const { addCar, updateCar, host } = useVisitorFlow()
  const [certTypeVisible, setCertTypeVisible] = useState(false)

  // Edit mode only with a valid in-range index (see persons/add for rationale).
  const [editIndex] = useState<number | null>(() => {
    if (indexParam === null) return null
    const i = Number(indexParam)
    return Number.isInteger(i) && i >= 0 && i < useVisitorFlow.getState().cars.length ? i : null
  })
  const [form, setForm] = useState<VisitorCar>(() => {
    const existing = editIndex === null ? undefined : useVisitorFlow.getState().cars[editIndex]
    if (existing) return existing
    return {
      plate: '',
      // Legacy default: driver name prefilled with the visitor's name.
      name: useVisitorFlow.getState().visitor.visitorName,
      certType: DEFAULT_CERT_TYPE,
      certImg: '',
    }
  })
  const isEdit = editIndex !== null

  const visitorDraft = host.visitorDraftToken && host.visitorDraftId
    ? { draftToken: host.visitorDraftToken, draftId: host.visitorDraftId }
    : null
  const certEnum = useQuery({
    queryKey: ['visitor', 'vehicle-cert-enum', visitorDraft?.draftId],
    queryFn: () => getVehicleCertEnum(visitorDraft!),
    enabled: visitorDraft !== null,
  })

  function handleSubmit() {
    // 先去首尾空格再校验，避免误带空格卡在格式校验。
    const plate = stripSpaces(form.plate)
    const driverName = stripSpaces(form.name)
    // Province char + issuing letter (no I/O) + 5-6 alphanumerics covers
    // standard and new-energy plates (legacy had no validation at all).
    if (!/^[一-龥][A-HJ-NP-Z][A-Z0-9]{5,6}$/.test(plate)) {
      return Toast.show('请输入正确的车牌号')
    }
    if (!driverName) return Toast.show('请输入司机姓名')
    const nameCheck = validateVisitorName(driverName)
    if (!nameCheck.ok) return Toast.show(nameCheck.message)
    if (!form.certImg) return Toast.show('请上传证件照片')

    const cleaned = { ...form, name: driverName, plate }
    if (isEdit) {
      updateCar(editIndex, cleaned)
    } else {
      addCar(cleaned)
    }
    router.replace('/visitor/cars')
  }

  if (!mounted) return null

  return (
    <PageShell title={isEdit ? '修改车辆' : '添加车辆'}>
      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <div>
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            车牌号 <span className="text-[#d83b36]">*</span>
          </label>
          <PlateInput value={form.plate} onChange={(plate) => setForm((f) => ({ ...f, plate }))} />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="c-name">
            司机姓名 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="c-name"
            placeholder="请输入司机姓名"
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            证件类型 <span className="text-[#d83b36]">*</span>
          </label>
          <button
            type="button"
            onClick={() => setCertTypeVisible(true)}
            className={`${SMS_INPUT_CLASS} text-left`}
          >
            {form.certType.desc}
          </button>
          <Picker
            visible={certTypeVisible}
            onClose={() => setCertTypeVisible(false)}
            columns={[
              (certEnum.data?.data ?? [DEFAULT_CERT_TYPE]).map((c) => ({
                label: c.desc,
                value: String(c.code),
              })),
            ]}
            onConfirm={(v) => {
              const item = (certEnum.data?.data ?? [DEFAULT_CERT_TYPE]).find(
                (c) => String(c.code) === v[0],
              )
              if (item) setForm((f) => ({ ...f, certType: item }))
            }}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            证件照片 <span className="text-[#d83b36]">*</span>
          </label>
          <FaceUpload
            mode="plain"
            value={form.certImg}
            onChange={(certImg) => setForm((f) => ({ ...f, certImg }))}
            label="请上传证件照片"
				visitorFaceDraft={
					host.visitorDraftToken && host.visitorDraftId
						? { draftToken: host.visitorDraftToken, draftId: host.visitorDraftId }
						: undefined
				}
          />
        </div>

        <button
          type="button"
          onClick={handleSubmit}
          className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
        >
          {isEdit ? '确认修改车辆' : '确认添加车辆'}
        </button>
      </section>
    </PageShell>
  )
}

export default function CarFormPage() {
  return (
    <Suspense>
      <CarFormInner />
    </Suspense>
  )
}
