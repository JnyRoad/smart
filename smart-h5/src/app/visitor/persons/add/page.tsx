'use client'
import { Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useState } from 'react'
import { FaceUpload } from '@/components/face-upload'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useVisitorFlow } from '@/features/visitor/flow-store'
import { validateIdCard } from '@/features/visitor/id-card'
import { validateVisitorName } from '@/features/visitor/visitor-name'
import { PageShell } from '@/components/page-shell'
import { useMounted } from '@/lib/use-mounted'

/** Add or edit one fellow person (edit mode via `?index=`). */
function FellowFormInner() {
  const router = useRouter()
  const mounted = useMounted()
  const indexParam = useSearchParams().get('index')
  const { addFellow, updateFellow } = useVisitorFlow()

  // Edit mode only with a valid in-range index; anything else is treated as
  // a new entry (an out-of-range update would silently no-op and lose input).
  const [editIndex] = useState<number | null>(() => {
    if (indexParam === null) return null
    const i = Number(indexParam)
    return Number.isInteger(i) && i >= 0 && i < useVisitorFlow.getState().fellows.length ? i : null
  })
  const [form, setForm] = useState(() => {
    const existing = editIndex === null ? undefined : useVisitorFlow.getState().fellows[editIndex]
    return existing ?? { fellowName: '', fellowPhotoId: '', certNo: '' }
  })
  const isEdit = editIndex !== null

  function handleSubmit() {
    if (!form.fellowName) return Toast.show('请输入访客姓名')
    const nameCheck = validateVisitorName(form.fellowName)
    if (!nameCheck.ok) return Toast.show(nameCheck.message)
    if (!form.fellowPhotoId) return Toast.show('请上传访客照片')
    const certNo = form.certNo.trim().toUpperCase()
    const idCheck = validateIdCard(certNo)
    if (!idCheck.ok) return Toast.show(idCheck.message)

    const person = { ...form, certNo }
    if (isEdit) {
      updateFellow(editIndex, person)
    } else {
      addFellow(person)
    }
    router.replace('/visitor/persons')
  }

  if (!mounted) return null

  return (
    <PageShell title={isEdit ? '修改随行人员' : '添加随行人员'}>
      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <div>
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="f-name">
            访客姓名 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="f-name"
            placeholder="请输入"
            value={form.fellowName}
            onChange={(e) => setForm({ ...form, fellowName: e.target.value })}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            访客照片 <span className="text-[#d83b36]">*</span>
          </label>
          <FaceUpload
            mode="face"
            value={form.fellowPhotoId}
            onChange={(photoId) => setForm((f) => ({ ...f, fellowPhotoId: photoId }))}
            label="点击拍摄/上传人脸照片"
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="f-cert">
            证件号码 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="f-cert"
            placeholder="请输入身份证号码"
            value={form.certNo}
            onChange={(e) => setForm({ ...form, certNo: e.target.value })}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <button
          type="button"
          onClick={handleSubmit}
          className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00]"
        >
          {isEdit ? '确认修改随行人员' : '确认添加随行人员'}
        </button>
      </section>
    </PageShell>
  )
}

export default function FellowFormPage() {
  return (
    <Suspense>
      <FellowFormInner />
    </Suspense>
  )
}
