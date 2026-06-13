'use client'
import { DatePicker, Picker, Popup } from 'antd-mobile'
import { useState } from 'react'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { FXSX_OPTIONS } from '@/features/good-release/dicts'
import type { ApprovalSearch } from './api'

function formatDateTime(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * 保安搜索弹层（旧 search-by-staff）：badge/name 恒有，时间区间与放行事项
 * 下拉按列表页配置开关。条件挂载（每次打开都是干净状态）。
 */
export function StaffFilterPopup({
  visible,
  onClose,
  onSearch,
  withTimeRange = false,
  withReleaseItem = false,
}: {
  visible: boolean
  onClose: () => void
  onSearch: (search: ApprovalSearch) => void
  withTimeRange?: boolean
  withReleaseItem?: boolean
}) {
  const [badge, setBadge] = useState('')
  const [name, setName] = useState('')
  const [releaseItem, setReleaseItem] = useState<number | undefined>()
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [releaseVisible, setReleaseVisible] = useState(false)
  const [startVisible, setStartVisible] = useState(false)
  const [endVisible, setEndVisible] = useState(false)

  return (
    <Popup visible={visible} onMaskClick={onClose} bodyStyle={{ borderRadius: '16px 16px 0 0' }}>
      <div className="p-4">
        <div className="flex items-center justify-between pb-3">
          <span className="text-[15px] font-bold">输入员工信息查询</span>
          <button
            type="button"
            onClick={() => {
              onSearch({
                ...(badge ? { badge } : {}),
                ...(name ? { name } : {}),
                ...(releaseItem !== undefined ? { releaseItem } : {}),
                ...(startTime ? { startTime } : {}),
                ...(endTime ? { endTime } : {}),
              })
              onClose()
            }}
            className="text-sm font-semibold text-brand"
          >
            确定
          </button>
        </div>
        <div className="flex flex-col gap-3">
          <input placeholder="工号" value={badge} onChange={(e) => setBadge(e.target.value)} className={SMS_INPUT_CLASS} />
          <input placeholder="携带人姓名" value={name} onChange={(e) => setName(e.target.value)} className={SMS_INPUT_CLASS} />
          {withReleaseItem && (
            <>
              <button type="button" onClick={() => setReleaseVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
                {FXSX_OPTIONS.find((o) => o.value === releaseItem)?.label ?? (
                  <span className="text-weak">放行事项</span>
                )}
              </button>
              <Picker
                visible={releaseVisible}
                onClose={() => setReleaseVisible(false)}
                columns={[FXSX_OPTIONS.map((o) => ({ label: o.label, value: String(o.value) }))]}
                onConfirm={(v) => {
                  if (typeof v[0] === 'string') setReleaseItem(Number(v[0]))
                }}
              />
            </>
          )}
          {withTimeRange && (
            <>
              <button type="button" onClick={() => setStartVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
                {startTime || <span className="text-weak">申请开始时间</span>}
              </button>
              <DatePicker
                visible={startVisible}
                onClose={() => setStartVisible(false)}
                precision="minute"
                onConfirm={(d) => setStartTime(formatDateTime(d))}
              />
              <button type="button" onClick={() => setEndVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
                {endTime || <span className="text-weak">申请结束时间</span>}
              </button>
              <DatePicker
                visible={endVisible}
                onClose={() => setEndVisible(false)}
                precision="minute"
                onConfirm={(d) => setEndTime(formatDateTime(d))}
              />
            </>
          )}
        </div>
      </div>
    </Popup>
  )
}
