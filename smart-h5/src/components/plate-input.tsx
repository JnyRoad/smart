'use client'
import { Picker } from 'antd-mobile'
import { useState } from 'react'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'

const PROVINCES = [
  '京', '津', '冀', '晋', '蒙', '辽', '吉', '黑', '沪', '苏', '浙', '皖', '闽', '赣', '鲁', '豫',
  '鄂', '湘', '粤', '桂', '琼', '渝', '川', '贵', '云', '藏', '陕', '甘', '青', '宁', '新',
]

/** License-plate input: province abbreviation picker + alphanumeric body. */
export function PlateInput({
  value,
  onChange,
}: {
  /** Full plate, e.g. `豫A12345`. */
  value: string
  onChange: (plate: string) => void
}) {
  const [pickerVisible, setPickerVisible] = useState(false)
  const province = value.charAt(0) || '豫'
  const body = value.slice(1)

  return (
    <div className="flex items-center gap-2">
      <button
        type="button"
        onClick={() => setPickerVisible(true)}
        className="grid h-12 w-12 flex-none place-items-center rounded-xl border border-border-soft bg-surface text-base font-bold"
      >
        {province}
      </button>
      <Picker
        visible={pickerVisible}
        onClose={() => setPickerVisible(false)}
        columns={[PROVINCES.map((p) => ({ label: p, value: p }))]}
        value={[province]}
        onConfirm={(v) => {
          if (typeof v[0] === 'string') onChange(`${v[0]}${body}`)
        }}
      />
      <input
        placeholder="请输入车牌号"
        value={body}
        maxLength={7}
        onChange={(e) =>
          onChange(`${province}${e.target.value.toUpperCase().replace(/[^A-Z0-9]/g, '')}`)
        }
        className={SMS_INPUT_CLASS}
      />
    </div>
  )
}
