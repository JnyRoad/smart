'use client'
import { useQuery } from '@tanstack/react-query'
import { Picker, TextArea, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { ImageListUpload } from '@/components/image-list-upload'
import { PageShell } from '@/components/page-shell'
import { SegmentTabs } from '@/components/segment-tabs'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { addRepair, getRepairRangeEnum } from '@/features/dorm-services/api'
import {
  FALLBACK_RANGES,
  REPAIR_TYPES,
  buildingsForRange,
  normalizeRepairOptions,
  type RepairOption,
} from '@/features/dorm-services/repair-options'
import { getTenantConfig } from '@/lib/config/tenant'

function PickerField({
  label,
  valueText,
  placeholder,
  options,
  onPick,
}: {
  label: string
  valueText: string | undefined
  placeholder: string
  options: { label: string; value: string }[]
  onPick: (value: string) => void
}) {
  const [visible, setVisible] = useState(false)
  return (
    <div className="mt-3">
      <label className="mb-1.5 block text-[13px] font-semibold text-mid">
        {label} <span className="text-[#d83b36]">*</span>
      </label>
      <button type="button" onClick={() => setVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
        {valueText ?? <span className="text-weak">{placeholder}</span>}
      </button>
      <Picker
        visible={visible}
        onClose={() => setVisible(false)}
        columns={[options]}
        onConfirm={(v) => {
          if (typeof v[0] === 'string') onPick(v[0])
        }}
      />
    </div>
  )
}

/** Repair work-order submission. */
export default function DormRepairsPage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const config = getTenantConfig()
  const [range, setRange] = useState<RepairOption | undefined>()
  const [building, setBuilding] = useState<string | undefined>()
  const [repairType, setRepairType] = useState<RepairOption | undefined>()
  const [roomName, setRoomName] = useState('')
  const [faultDesc, setFaultDesc] = useState('')
  const [faultImgs, setFaultImgs] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)

  const rangeEnum = useQuery({ queryKey: ['repair-range-enum'], queryFn: getRepairRangeEnum, enabled: authorized })
  const apiRanges = normalizeRepairOptions(rangeEnum.data?.code === 0 ? rangeEnum.data.data : undefined)
  const ranges: RepairOption[] = apiRanges.length ? apiRanges : FALLBACK_RANGES

  function pickRange(codeText: string) {
    const next = ranges.find((r) => String(r.code) === codeText)
    if (!next) return
    setRange(next)
    // Legacy behavior: switching range defaults the building to the first option.
    setBuilding(buildingsForRange(next.code)[0])
  }

  async function handleSubmit() {
    if (submitting) return
    if (!range) return Toast.show('请选择维修区域')
    if (!building) return Toast.show('请选择所在楼栋')
    if (!repairType) return Toast.show('请选择维修类别')
    if (!roomName) return Toast.show('请输入所在房间')
    if (!faultDesc) return Toast.show('请输入故障描述')

    setSubmitting(true)
    try {
      const res = await addRepair({
        rangeType: range.code,
        repairType: repairType.code,
        dormitoryName: building,
        roomName,
        faultDesc,
        faultImgs,
        parkId: config.parkId,
      })
      if (res.code === 0) {
        // Legacy behavior: jump straight to the list, no success toast.
        router.push('/dorm-repairs/list')
      } else {
        Toast.show(res.message ?? res.msg ?? '提交失败')
      }
    } catch (error) {
      Toast.show(toUserMessage(error, '提交失败'))
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized) return null

  return (
    <PageShell title="宿舍报修">
      <SegmentTabs active="submit" submitHref="/dorm-repairs" listHref="/dorm-repairs/list" />

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <PickerField
          label="维修区域"
          valueText={range?.desc}
          placeholder="请选择维修区域"
          options={ranges.map((r) => ({ label: r.desc, value: String(r.code) }))}
          onPick={pickRange}
        />
        <PickerField
          label="所在楼栋"
          valueText={building}
          placeholder="请先选择维修区域"
          options={(range ? buildingsForRange(range.code) : []).map((b) => ({ label: b, value: b }))}
          onPick={setBuilding}
        />
        <PickerField
          label="维修类别"
          valueText={repairType?.desc}
          placeholder="请选择维修类别"
          options={REPAIR_TYPES.map((t) => ({ label: t.desc, value: String(t.code) }))}
          onPick={(v) => setRepairType(REPAIR_TYPES.find((t) => String(t.code) === v))}
        />

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="r-room">
            所在房间 <span className="text-[#d83b36]">*</span>
          </label>
          <input
            id="r-room"
            placeholder="请输入所在房间"
            value={roomName}
            onChange={(e) => setRoomName(e.target.value)}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="r-desc">
            故障描述 <span className="text-[#d83b36]">*</span>
          </label>
          <div className="rounded-xl border border-border-soft px-3 py-2">
            <TextArea
              id="r-desc"
              placeholder="请描述故障情况"
              value={faultDesc}
              onChange={setFaultDesc}
              rows={3}
            />
          </div>
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">上传物品照片</label>
          <ImageListUpload mode="base64" value={faultImgs} onChange={setFaultImgs} />
        </div>

        <button
          type="button"
          disabled={submitting}
          onClick={() => void handleSubmit()}
          className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
        >
          {submitting ? '提交中…' : '申请'}
        </button>
      </section>
    </PageShell>
  )
}
