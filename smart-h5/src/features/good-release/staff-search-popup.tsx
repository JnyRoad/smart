'use client'
import { Popup, SpinLoading, Toast } from 'antd-mobile'
import { useState } from 'react'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { getOaStaffInfo } from './api'

/**
 * 「输入员工工号查询」弹窗（旧 search-by-staff）：确定后查 OA 员工信息，
 * 成功回填姓名与人员 id。
 */
export function StaffSearchPopup({
  visible,
  onClose,
  onPicked,
}: {
  visible: boolean
  onClose: () => void
  onPicked: (staff: { gh: string; name: string; id: string | number }) => void
}) {
  const [badge, setBadge] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleConfirm() {
    if (loading) return
    if (!badge) return Toast.show('请输入工号')
    setLoading(true)
    try {
      const res = await getOaStaffInfo(badge)
      if (res.code === 0 && res.data) {
        onPicked({ gh: badge, name: res.data.name ?? '', id: res.data.id ?? '' })
        onClose()
      } else {
        Toast.show(res.message || res.msg || '查询失败')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '查询失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Popup visible={visible} onMaskClick={onClose} bodyStyle={{ borderRadius: '16px 16px 0 0' }}>
      <div className="p-4">
        <div className="flex items-center justify-between pb-3">
          <span className="text-[15px] font-bold">输入员工工号查询</span>
          <button
            type="button"
            onClick={() => void handleConfirm()}
            className="flex items-center gap-1 text-sm font-semibold text-brand"
          >
            {loading && <SpinLoading color="primary" style={{ '--size': '14px' }} />}
            确定
          </button>
        </div>
        <input
          placeholder="请输入"
          value={badge}
          onChange={(e) => setBadge(e.target.value)}
          className={SMS_INPUT_CLASS}
        />
      </div>
    </Popup>
  )
}
