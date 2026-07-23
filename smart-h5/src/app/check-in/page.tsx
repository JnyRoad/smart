'use client'
import { useQuery } from '@tanstack/react-query'
import { Picker, Toast } from 'antd-mobile'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { toUserMessage } from '@/lib/format/error-message'
import { PageShell } from '@/components/page-shell'
import { SegmentTabs } from '@/components/segment-tabs'
import { SMS_INPUT_CLASS } from '@/components/sms-code-field'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import {
  getMyCheckInProfile,
  getRoomTypes,
  queryDormitories,
  submitCheckIn,
  type Dormitory,
  type RoomTypeItem,
} from '@/features/dorm-services/api'
import {
  clearFormDraft,
  clearRoomDraft,
  checkInSelectionToSubmit,
  loadFormDraft,
  loadRoomDraft,
  saveFormDraft,
} from '@/features/dorm-services/check-in-rules'
import { getTenantConfig } from '@/lib/config/tenant'
import { useMounted } from '@/lib/use-mounted'

type AllotMode = 'manual' | 'auto'

/** Dorm check-in application: building/room-type cascade + optional manual bed pick. */
export default function CheckInPage() {
  const authorized = useRequireAuth()
  const mounted = useMounted()
  const router = useRouter()
  const config = getTenantConfig()
  const [mode, setMode] = useState<AllotMode>('manual')
  // Selecting a room navigates away and unmounts this page, so the
  // building/room-type choice is restored from the session form draft.
  const [dormitory, setDormitory] = useState<Dormitory | undefined>(() => {
    if (typeof window === 'undefined') return undefined
    const saved = loadFormDraft()
    return saved ? { id: saved.dormitoryId, dormitoryName: saved.dormitoryName } : undefined
  })
  const [roomType, setRoomType] = useState<RoomTypeItem | undefined>(() => {
    if (typeof window === 'undefined') return undefined
    const saved = loadFormDraft()
    return saved ? { id: saved.roomTypeCode, typeName: saved.roomTypeDesc } : undefined
  })
  const [dormVisible, setDormVisible] = useState(false)
  const [typeVisible, setTypeVisible] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const dorms = useQuery({
    queryKey: ['check-in-dorms'],
    queryFn: () => queryDormitories(config.parkId),
    enabled: authorized,
  })
  const identity = useQuery({
    queryKey: ['my-check-in-profile'],
    queryFn: getMyCheckInProfile,
    enabled: authorized,
  })
  const identityFailed =
    identity.isError || (identity.isSuccess && (identity.data.code !== 0 || !identity.data.data))
  const dormsFailed = dorms.isError || (dorms.isSuccess && dorms.data.code !== 0)
  const roomTypes = useQuery({
    queryKey: ['check-in-room-types', dormitory?.id],
    queryFn: () => getRoomTypes(config.parkId, dormitory?.id as string | number),
    enabled: authorized && dormitory?.id !== undefined,
  })

  // Room/bed selection carried back from the select-room page.
  const draft = mounted ? loadRoomDraft() : null

  function pickDormitory(value: string) {
    const next = (dorms.data?.data ?? []).find((d) => String(d.id) === value)
    if (!next || next.id === dormitory?.id) return
    setDormitory(next)
    // Legacy behavior: switching the building clears the room type (and any picked room).
    setRoomType(undefined)
    clearRoomDraft()
    clearFormDraft()
  }

  function pickRoomType(value: string) {
    const next = (roomTypes.data?.data ?? []).find((t) => String(t.id) === value)
    if (!next || next.id === roomType?.id) return
    setRoomType(next)
    // A picked room belongs to the previous room type (legacy left it stale — a bug).
    clearRoomDraft()
    // Keep the persisted form draft in sync so a refresh doesn't revert the choice.
    if (dormitory) {
      saveFormDraft({
        dormitoryId: dormitory.id as string | number,
        dormitoryName: dormitory.dormitoryName ?? '',
        roomTypeCode: next.id as string | number,
        roomTypeDesc: next.typeName ?? '',
      })
    }
  }

  function goSelectRoom() {
    if (!dormitory) return Toast.show('请选择楼栋信息')
    if (!roomType) return Toast.show('请选择房间类型')
    saveFormDraft({
      dormitoryId: dormitory.id as string | number,
      dormitoryName: dormitory.dormitoryName ?? '',
      roomTypeCode: roomType.id as string | number,
      roomTypeDesc: roomType.typeName ?? '',
    })
    router.push(
      `/check-in/select-room?dormitoryId=${encodeURIComponent(String(dormitory.id))}&roomType=${encodeURIComponent(String(roomType.id))}`,
    )
  }

  async function handleSubmit() {
    if (submitting) return
    if (!dormitory) return Toast.show('请选择楼栋信息')
    if (!roomType) return Toast.show('请选择房间类型')
    if (mode === 'manual' && !draft) return Toast.show('请选择房间号')
    const profile = identity.data?.data
    if (!profile?.profileComplete) return Toast.show('当前员工资料不完整，无法申请入住')

    setSubmitting(true)
    try {
      const res = await submitCheckIn(checkInSelectionToSubmit({
        dormitoryId: dormitory.id,
        roomType: roomType.id,
        parkId: config.parkId,
        ...(mode === 'manual' && draft
          ? { floorId: draft.floorId, roomId: draft.roomId, bedId: draft.bedId }
          : {}),
      }))
      if (res.code === 0) {
        clearRoomDraft()
        clearFormDraft()
        router.push('/check-in/detail')
      } else {
        Toast.show(res.message ?? res.msg ?? '提交失败')
      }
    } catch (error) {
      Toast.show(toUserMessage(error, '提交失败'))
    } finally {
      setSubmitting(false)
    }
  }

  if (!authorized || !mounted) return null

  const dormOptions = (dorms.data?.code === 0 ? (dorms.data.data ?? []) : []).map((d) => ({
    label: d.dormitoryName ?? '',
    value: String(d.id),
  }))
  const typeOptions = (roomTypes.data?.code === 0 ? (roomTypes.data.data ?? []) : []).map((t) => ({
    label: t.typeName ?? '',
    value: String(t.id),
  }))

  return (
    <PageShell title="宿舍申请">
      <SegmentTabs active="submit" submitHref="/check-in" listHref="/check-in/detail" />

      <section className="rounded-2xl bg-white p-4 shadow-[0_16px_40px_rgba(89,87,87,0.10)]">
        <div>
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            分配方式 <span className="text-[#d83b36]">*</span>
          </label>
          <div className="grid grid-cols-2 gap-2">
            {(
              [
                ['manual', '自选房间'],
                ['auto', '系统分配'],
              ] as const
            ).map(([key, label]) => (
              <button
                key={key}
                type="button"
                aria-pressed={mode === key}
                onClick={() => setMode(key)}
                className={`min-h-11 rounded-xl border text-sm font-semibold ${
                  mode === key
                    ? 'border-brand bg-accent-soft text-accent-ink'
                    : 'border-border-soft bg-surface text-mid'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            楼栋信息 <span className="text-[#d83b36]">*</span>
          </label>
          <button type="button" onClick={() => setDormVisible(true)} className={`${SMS_INPUT_CLASS} text-left`}>
            {dormitory?.dormitoryName ?? <span className="text-weak">请选择楼栋</span>}
          </button>
          {dormsFailed && (
            <p className="mt-1 text-xs text-[#d83b36]">{dorms.data?.message ?? '获取楼栋信息失败！'}</p>
          )}
          <Picker
            visible={dormVisible}
            onClose={() => setDormVisible(false)}
            columns={[dormOptions]}
            onConfirm={(v) => {
              if (typeof v[0] === 'string') pickDormitory(v[0])
            }}
          />
        </div>

        <div className="mt-3">
          <label className="mb-1.5 block text-[13px] font-semibold text-mid">
            房间类型 <span className="text-[#d83b36]">*</span>
          </label>
          <button
            type="button"
            onClick={() => {
              if (!dormitory) {
                Toast.show('请先选择楼栋信息')
                return
              }
              setTypeVisible(true)
            }}
            className={`${SMS_INPUT_CLASS} text-left`}
          >
            {roomType?.typeName ?? <span className="text-weak">请选择房间类型</span>}
          </button>
          <Picker
            visible={typeVisible}
            onClose={() => setTypeVisible(false)}
            columns={[typeOptions]}
            onConfirm={(v) => {
              if (typeof v[0] === 'string') pickRoomType(v[0])
            }}
          />
        </div>

        {mode === 'manual' && (
          <>
            <div className="mt-3">
              <label className="mb-1.5 block text-[13px] font-semibold text-mid">
                房间号 <span className="text-[#d83b36]">*</span>
              </label>
              <button type="button" onClick={goSelectRoom} className={`${SMS_INPUT_CLASS} text-left`}>
                {draft?.roomName ? `${draft.roomName}房` : <span className="text-weak">请选择房间</span>}
              </button>
            </div>
            <div className="mt-3">
              <label className="mb-1.5 block text-[13px] font-semibold text-mid">
                床位 <span className="text-[#d83b36]">*</span>
              </label>
              <button type="button" onClick={goSelectRoom} className={`${SMS_INPUT_CLASS} text-left`}>
                {draft?.bedNumber ? `${draft.bedNumber}号床` : <span className="text-weak">请选择床位</span>}
              </button>
            </div>
          </>
        )}

        {identityFailed ? (
          <p className="mt-4 text-center text-sm text-[#d83b36]">获取用户信息失败！</p>
        ) : (
          <button
            type="button"
            disabled={submitting}
            onClick={() => void handleSubmit()}
            className="mt-4.5 flex h-12 w-full items-center justify-center rounded-[14px] bg-brand text-base font-semibold text-white active:bg-[#d95f00] disabled:opacity-60"
          >
            {submitting ? '提交中…' : '申请'}
          </button>
        )}
      </section>
    </PageShell>
  )
}
