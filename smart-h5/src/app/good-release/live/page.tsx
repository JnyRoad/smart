'use client'
import { useQuery } from '@tanstack/react-query'
import { DatePicker, Picker, TextArea, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { ImageListUpload } from '@/components/image-list-upload'
import { PageShell } from '@/components/page-shell'
import { PlateInput } from '@/components/plate-input'
import { SegmentTabs } from '@/components/segment-tabs'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getEmployeeBaseInfo } from '@/features/employee/api'
import { getLiveRooms, saveLiveRelease } from '@/features/good-release/api'
import { liveRoomOption, splitLiveRoomValue } from '@/features/good-release/room-option'
import { getTenantConfig } from '@/lib/config/tenant'

const NO_ROOM_TIP = '没有查询到您的房间信息，不能进行物品放行（生活区）申请'

function formatDateTime(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function FieldLabel({ text, required = false }: { text: string; required?: boolean }) {
  return (
    <label className="mb-1.5 block text-[13px] font-semibold text-mid">
      {text} {required && <span className="text-[#d83b36]">*</span>}
    </label>
  )
}

/** 生活区物品放行申请（携带宿舍生活物品离厂）。 */
export default function GoodReleaseLivePage() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const config = getTenantConfig()
  const [articlesDesc, setArticlesDesc] = useState('')
  const [roomValue, setRoomValue] = useState<string | undefined>()
  const [roomVisible, setRoomVisible] = useState(false)
  const [departTime, setDepartTime] = useState('')
  const [timeVisible, setTimeVisible] = useState(false)
  const [plate, setPlate] = useState('')
  const [remarks, setRemarks] = useState('')
  const [imgs, setImgs] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)

  const baseInfo = useQuery({
    queryKey: ['employee', 'baseinfo'],
    queryFn: getEmployeeBaseInfo,
    enabled: authorized,
  })
  const me = baseInfo.data?.code === 0 ? baseInfo.data.data : undefined

	const roomsQuery = useQuery({
		queryKey: ['good-release-live-rooms'],
		queryFn: getLiveRooms,
		enabled: authorized,
	})
	const rooms = roomsQuery.data?.code === 0 ? (roomsQuery.data.data ?? []) : []
  const roomOptions = rooms.map(liveRoomOption)
  // Legacy behavior: empty/failed room lookup blocks the whole application;
  // envelope errors surface the backend message instead of the generic tip.
  const envelopeError = roomsQuery.isSuccess && roomsQuery.data.code !== 0
  const noRooms = roomsQuery.isError || envelopeError || (roomsQuery.isSuccess && rooms.length === 0)
  const warnedRef = useRef(false)
  useEffect(() => {
    if (noRooms && !warnedRef.current) {
      warnedRef.current = true
      Toast.show(envelopeError ? (roomsQuery.data?.message ?? NO_ROOM_TIP) : NO_ROOM_TIP)
    }
  }, [noRooms, envelopeError, roomsQuery.data?.message])

  async function handleSubmit() {
    if (submitting) return
    if (noRooms || rooms.length === 0) return Toast.show(NO_ROOM_TIP)
    if (!articlesDesc) return Toast.show('请输入物品名称')
    if (!roomValue) return Toast.show('请选择房间信息')
    if (!departTime) return Toast.show('请选择预计离厂日期')
    if (imgs.length === 0) return Toast.show('请上传物品照片')
    if (!me?.employeeBadge || !me.employeeName) return Toast.show('获取用户信息失败！')

    setSubmitting(true)
    try {
      const res = await saveLiveRelease({
        articlesDesc,
        articlesType: 3,
        badge: me.employeeBadge,
        parkId: config.parkId,
        ...splitLiveRoomValue(roomValue),
        carrier: me.employeeName,
        // 省份字符默认存在：未输入车牌主体时按未填处理，避免提交单个省字。
        licensePlate: plate.length <= 1 ? '' : plate,
        name: me.employeeName,
        plannedDepartureTime: departTime,
        remarks,
        status: 1,
        oneImg: imgs[0] ?? '',
        twoImg: imgs[1] ?? '',
        threeImg: imgs[2] ?? '',
      })
      if (res.code === 0 && res.data) {
        router.push('/good-release/live/list')
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

  const roomLabel = roomOptions.find((o) => o.value === roomValue)?.label

  return (
    <PageShell title="物品放行（生活区）">
      <SegmentTabs active="submit" submitHref="/good-release/live" listHref="/good-release/live/list" />

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <div className="mt-3">
          <FieldLabel text="物品类型" required />
          <div className={`${SMS_INPUT_CLASS} flex items-center text-left`}>宿舍生活物品</div>
        </div>

        <div className="mt-3">
          <FieldLabel text="物品名称" required />
          <input
            placeholder="请输入物品名称"
            value={articlesDesc}
            onChange={(e) => setArticlesDesc(e.target.value)}
            className={SMS_INPUT_CLASS}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="携带人名称" required />
          <div className={`${SMS_INPUT_CLASS} flex items-center bg-surface text-left`}>
            {me?.employeeName ?? ''}
          </div>
        </div>

        <div className="mt-3">
          <FieldLabel text="房间信息" required />
          <button type="button" onClick={() => setRoomVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {roomLabel ?? <span className="text-weak">请选择房间</span>}
          </button>
          <Picker
            visible={roomVisible}
            onClose={() => setRoomVisible(false)}
            columns={[roomOptions]}
            onConfirm={(v) => {
              if (typeof v[0] === 'string') setRoomValue(v[0])
            }}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="预计离厂日期" required />
          <button type="button" onClick={() => setTimeVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {departTime || <span className="text-weak">请选择预计离厂日期</span>}
          </button>
          <DatePicker
            visible={timeVisible}
            onClose={() => setTimeVisible(false)}
            precision="minute"
            onConfirm={(d) => setDepartTime(formatDateTime(d))}
          />
        </div>

        <div className="mt-3">
          <FieldLabel text="车牌号" />
          <PlateInput value={plate} onChange={setPlate} />
        </div>

        <div className="mt-3">
          <FieldLabel text="备注" />
          <div className="rounded-xl border border-border-soft px-3 py-2">
            <TextArea placeholder="选填" value={remarks} onChange={setRemarks} rows={2} />
          </div>
        </div>

        <div className="mt-3">
          <FieldLabel text="上传物品照片" required />
          <ImageListUpload mode="base64" value={imgs} onChange={setImgs} confirmRemove />
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
