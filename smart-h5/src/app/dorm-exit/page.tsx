'use client'
import { useQuery } from '@tanstack/react-query'
import { DatePicker, Picker, TextArea, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { ImageListUpload } from '@/components/image-list-upload'
import { StatusIcon } from '@/components/app-icon'
import { PageShell } from '@/components/page-shell'
import { SegmentTabs } from '@/components/segment-tabs'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { getMyRooms, saveDormExit } from '@/features/dorm-services/api'
import { addRoomDeduped, roomOption, splitRoomValues, type RoomChoice } from '@/features/dorm-services/exit-rooms'
import { getTenantConfig } from '@/lib/config/tenant'

const QUIT_REASONS = [
  { code: 3, desc: '离职' },
  { code: 5, desc: '自离' },
  { code: 2, desc: '外宿' },
]

function formatDateTime(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** Dorm-exit application: pick owned rooms, reason, leave date, photos. */
export default function DormExitPage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const config = getTenantConfig()
  const [selectedRooms, setSelectedRooms] = useState<RoomChoice[]>([])
  const [reason, setReason] = useState<{ code: number; desc: string } | undefined>()
  const [leaveTime, setLeaveTime] = useState('')
  const [remark, setRemark] = useState('')
  const [imgs, setImgs] = useState<string[]>([])
  const [roomPickerVisible, setRoomPickerVisible] = useState(false)
  const [reasonVisible, setReasonVisible] = useState(false)
  const [timeVisible, setTimeVisible] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const badge = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeBadge : undefined
  const employeeName = baseInfo.data?.code === 0 ? baseInfo.data.data?.employeeName : undefined

  const myRooms = useQuery({
    queryKey: ['my-rooms', badge],
    queryFn: () => getMyRooms(badge as string),
    enabled: authorized && badge !== undefined,
  })
  // Double-data envelope (legacy gateway shape); rooms without ids are unusable.
  const roomChoices = (myRooms.data?.code === 0 ? (myRooms.data.data?.data ?? []) : [])
    .filter((room) => room.dormitoryId != null && room.roomId != null)
    .map(roomOption)

  useEffect(() => {
    if (myRooms.isSuccess && myRooms.data.code === 0 && roomChoices.length === 0) {
      Toast.show('没有查询到您的房间信息，不能进行退宿申请申请')
    } else if (myRooms.isSuccess && myRooms.data.code !== 0) {
      Toast.show(myRooms.data.message ?? '房间信息获取失败')
    } else if (myRooms.isError) {
      Toast.show('房间信息获取失败，请稍后重试')
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [myRooms.isSuccess, myRooms.isError])

  async function handleSubmit() {
    if (submitting) return
    if (selectedRooms.length === 0) return Toast.show('请选择退宿房间！')
    if (!reason) return Toast.show('请选择退宿原因')
    if (!leaveTime) return Toast.show('请选择预计离开日期')
    // badge/name are mandatory in the submit body — a record without them
    // would not show up in the badge-filtered list afterwards.
    if (!badge || !employeeName) return Toast.show('员工信息获取失败，请稍后重试')

    setSubmitting(true)
    try {
      const { dormitoryIds, roomIds } = splitRoomValues(selectedRooms.map((r) => r.value))
      const res = await saveDormExit({
        dormitoryIds,
        roomIds,
        quitReason: reason.code,
        applyLeaveTime: `${leaveTime}:00`,
        remark,
        imgs,
        parkId: config.parkId,
        badge,
        name: employeeName,
      })
      // Legacy success check: code === 0 AND truthy data.
      if (res.code === 0 && res.data) {
        Toast.show('申请成功')
        router.push('/dorm-exit/list')
      } else {
        Toast.show(res.message ?? res.msg ?? '提交失败')
      }
    } catch (error) {
      Toast.show(error instanceof Error ? error.message : '提交失败')
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized) return null

  return (
    <PageShell title="退宿申请">
      <SegmentTabs active="submit" submitHref="/dorm-exit" listHref="/dorm-exit/list" />

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <div>
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            房间信息 <span className="text-[#d83b36]">*</span>
          </label>
          <button
            type="button"
            onClick={() => {
              if (roomChoices.length === 0) {
                Toast.show('没有查询到您的房间信息，不能进行退宿申请申请')
                return
              }
              setRoomPickerVisible(true)
            }}
            className={`${SMS_INPUT_CLASS} text-left`}
          >
            <span className="text-weak">请选择退宿房间（可多选）</span>
          </button>
          <Picker
            visible={roomPickerVisible}
            onClose={() => setRoomPickerVisible(false)}
            columns={[roomChoices.map((r) => ({ label: r.label, value: r.value }))]}
            onConfirm={(v) => {
              const choice = roomChoices.find((r) => r.value === v[0])
              if (choice) setSelectedRooms((prev) => addRoomDeduped(prev, choice))
            }}
          />
          {selectedRooms.length > 0 && (
            <div className="mt-2 flex flex-col gap-1.5">
              {selectedRooms.map((room, index) => (
                <div
                  key={room.value}
                  data-testid="selected-room"
                  className="flex items-center justify-between rounded-xl bg-surface px-3 py-2 text-[13px]"
                >
                  <span>{room.label}</span>
                  <button
                    type="button"
                    aria-label={`删除${room.label}`}
                    onClick={() => setSelectedRooms((prev) => prev.filter((_, i) => i !== index))}
                    className="grid h-6 w-6 place-items-center text-[#d83b36]"
                  >
                    <StatusIcon name="close" className="h-4 w-4" />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            退宿原因 <span className="text-[#d83b36]">*</span>
          </label>
          <button type="button" onClick={() => setReasonVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {reason?.desc ?? <span className="text-weak">请选择退宿原因</span>}
          </button>
          <Picker
            visible={reasonVisible}
            onClose={() => setReasonVisible(false)}
            columns={[QUIT_REASONS.map((r) => ({ label: r.desc, value: String(r.code) }))]}
            onConfirm={(v) => setReason(QUIT_REASONS.find((r) => String(r.code) === v[0]))}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            预计离开日期 <span className="text-[#d83b36]">*</span>
          </label>
          <button type="button" onClick={() => setTimeVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {leaveTime || <span className="text-weak">请选择预计离开日期</span>}
          </button>
          <DatePicker
            visible={timeVisible}
            onClose={() => setTimeVisible(false)}
            precision="minute"
            onConfirm={(d) => setLeaveTime(formatDateTime(d))}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid" htmlFor="e-remark">
            备注
          </label>
          <div className="rounded-xl border border-border-soft px-3 py-2">
            <TextArea id="e-remark" placeholder="选填" value={remark} onChange={setRemark} rows={2} />
          </div>
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">上传物品照片</label>
          <ImageListUpload mode="photoId" value={imgs} onChange={setImgs} />
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
