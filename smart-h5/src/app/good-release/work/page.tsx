'use client'
import { Picker, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { ImageListUpload } from '@/components/image-list-upload'
import { PageShell } from '@/components/page-shell'
import { SegmentTabs } from '@/components/segment-tabs'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { createOfficeReleaseDraft, saveWorkRelease } from '@/features/good-release/api'
import {
  DDDD_OPTIONS,
  FXDD_OPTIONS,
  FXQC_OPTIONS,
  FXSX_OPTIONS,
  SFFC_OPTIONS,
  SQRJB_OPTIONS,
  WPFXLB_OPTIONS,
  isPersonRelease,
  type DictOption,
} from '@/features/good-release/dicts'
import { useWorkDraft, type WorkApplyMain } from '@/features/good-release/work-draft'
import { buildWorkSubmitBody } from '@/features/good-release/work-submit'
import { getTenantConfig } from '@/lib/config/tenant'
import { useMounted } from '@/lib/use-mounted'

function DictPickerField({
  label,
  options,
  value,
  onPick,
}: {
  label: string
  options: DictOption[]
  value: number | undefined
  onPick: (value: number) => void
}) {
  const [visible, setVisible] = useState(false)
  const current = options.find((o) => o.value === value)
  return (
    <div className="mt-3">
      <label className="mb-1.5 block text-[13px] font-semibold text-mid">
        {label} <span className="text-[#d83b36]">*</span>
      </label>
      <button type="button" onClick={() => setVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
        {current?.label ?? <span className="text-weak">请选择</span>}
      </button>
      <Picker
        visible={visible}
        onClose={() => setVisible(false)}
        columns={[options.map((o) => ({ label: o.label, value: String(o.value) }))]}
        onConfirm={(v) => {
          if (typeof v[0] === 'string') onPick(Number(v[0]))
        }}
      />
    </div>
  )
}

function TextField({
  label,
  placeholder,
  value,
  onChange,
}: {
  label: string
  placeholder: string
  value: string
  onChange: (v: string) => void
}) {
  return (
    <div className="mt-3">
      <label className="mb-1.5 block text-[13px] font-semibold text-mid">
        {label} <span className="text-[#d83b36]">*</span>
      </label>
      <input
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className={SMS_INPUT_CLASS}
      />
    </div>
  )
}

/** 办公区物品放行主表单。 */
export default function GoodReleaseWorkPage() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const config = getTenantConfig()
  const applyMain = useWorkDraft((s) => s.applyMain)
	const releaseId = useWorkDraft((s) => s.releaseId)
  const persons = useWorkDraft((s) => s.persons)
  const goods = useWorkDraft((s) => s.goods)
  const patchApplyMain = useWorkDraft((s) => s.patchApplyMain)
	const setReleaseId = useWorkDraft((s) => s.setReleaseId)
  const clearAll = useWorkDraft((s) => s.clearAll)
  const [submitting, setSubmitting] = useState(false)

  function patch(p: Partial<WorkApplyMain>) {
    patchApplyMain(p)
  }

  async function handleSubmit() {
    if (submitting) return
    // 逐项校验（文案对齐旧表单 requiredMessage）
    if (applyMain.fxqc === undefined) return Toast.show('请选择放行去处')
    if (applyMain.sffc === undefined) return Toast.show('请选择是否返厂')
    if (applyMain.fxdd === undefined) return Toast.show('请选择出发地点')
    if (!applyMain.fxddxq) return Toast.show('请输入出发备注')
    if (applyMain.dddd === undefined) return Toast.show('请选择到达地点')
    if (!applyMain.ddddxq) return Toast.show('请输入到达备注')
    if (applyMain.sqrjb === undefined) return Toast.show('请选择放行人级别')
    if (applyMain.fxsx === undefined) return Toast.show('请选择放行事项')
    if (applyMain.wpfxlb === undefined) return Toast.show('请选择物品放行类别')
    const personBranch = isPersonRelease(applyMain.fxsx)
    if (personBranch && persons.length === 0) return Toast.show('请添加放行人员')
    if (!personBranch && goods.length === 0) return Toast.show('请添加放行物品')
	if (!releaseId) return Toast.show('请先创建放行草稿')

    setSubmitting(true)
    try {
      const res = await saveWorkRelease(
		buildWorkSubmitBody({ applyMain, releaseId, parkId: config.parkId, persons, goods }),
      )
      if (res.code === 0 && res.data) {
        clearAll()
        router.push('/good-release/work/list')
      } else {
        Toast.show(res.message || res.msg || '提交失败')
      }
    } catch (error) {
      Toast.show(toUserMessage(error, '提交失败'))
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized || !mounted) return null

  const personBranch = isPersonRelease(applyMain.fxsx)

	async function enterReleaseDetails() {
		if (releaseId) {
			router.push(personBranch ? '/good-release/work/persons' : '/good-release/work/goods')
			return
		}
		try {
			const response = await createOfficeReleaseDraft({ parkId: config.parkId })
			const draftId = response.code === 0 ? response.data?.releaseId : undefined
			if (draftId === undefined || draftId === null) {
				Toast.show(response.message || response.msg || '创建草稿失败')
				return
			}
			setReleaseId(draftId)
			router.push(personBranch ? '/good-release/work/persons' : '/good-release/work/goods')
		} catch (error) {
			Toast.show(toUserMessage(error, '创建草稿失败'))
		}
	}

  return (
    <PageShell title="物品放行（办公区）">
      <SegmentTabs active="submit" submitHref="/good-release/work" listHref="/good-release/work/list" />

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <DictPickerField label="放行去处" options={FXQC_OPTIONS} value={applyMain.fxqc} onPick={(v) => patch({ fxqc: v })} />
        <DictPickerField label="是否返厂" options={SFFC_OPTIONS} value={applyMain.sffc} onPick={(v) => patch({ sffc: v })} />
        <DictPickerField label="出发地点" options={FXDD_OPTIONS} value={applyMain.fxdd} onPick={(v) => patch({ fxdd: v })} />
        <TextField label="出发备注" placeholder="请输入" value={applyMain.fxddxq ?? ''} onChange={(v) => patch({ fxddxq: v })} />
        <DictPickerField label="到达地点" options={DDDD_OPTIONS} value={applyMain.dddd} onPick={(v) => patch({ dddd: v })} />
        <TextField label="到达备注" placeholder="请输入" value={applyMain.ddddxq ?? ''} onChange={(v) => patch({ ddddxq: v })} />
        <DictPickerField label="放行人级别" options={SQRJB_OPTIONS} value={applyMain.sqrjb} onPick={(v) => patch({ sqrjb: v })} />
      </section>

      <section className="mt-3 rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <DictPickerField label="放行事项" options={FXSX_OPTIONS} value={applyMain.fxsx} onPick={(v) => patch({ fxsx: v })} />
        <DictPickerField label="物品放行类别" options={WPFXLB_OPTIONS} value={applyMain.wpfxlb} onPick={(v) => patch({ wpfxlb: v })} />
        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">附件上传</label>
          <ImageListUpload
            mode="base64"
            max={1}
            value={applyMain.fjsc ? [applyMain.fjsc] : []}
            onChange={(list) => patch({ fjsc: list[0] ?? '' })}
          />
        </div>

        {/* 条件入口：人员 / 物品 */}
        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            {personBranch ? '人员放行' : '物品放行'} <span className="text-[#d83b36]">*</span>
          </label>
          <button
            type="button"
			onClick={() => void enterReleaseDetails()}
            className={`${SMS_INPUT_CLASS} h-auto min-h-12 py-2 text-left`}
          >
            {personBranch ? (
              persons.length > 0 ? (
                <span className="flex flex-wrap gap-2">
                  {persons.map((person, index) => (
                    <span key={index} className="rounded-full bg-surface px-3 py-1 text-[13px]">
                      {person.name}
                    </span>
                  ))}
                </span>
              ) : (
                <span className="text-weak">请添加</span>
              )
            ) : goods.length > 0 ? (
              <span className="flex flex-wrap gap-2">
                {goods.map((good, index) => (
                  <span key={index} className="rounded-full bg-surface px-3 py-1 text-[13px]">
                    {good.wpbm}-{good.wpmc}
                    {good.wpsl}
                    {good.wpdw}
                  </span>
                ))}
              </span>
            ) : (
              <span className="text-weak">请添加</span>
            )}
          </button>
        </div>
      </section>

      <button
        type="button"
        disabled={submitting}
        onClick={() => void handleSubmit()}
        className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
      >
        {submitting ? '提交中…' : '申请'}
      </button>
    </PageShell>
  )
}
