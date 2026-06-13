'use client'
import { useQuery } from '@tanstack/react-query'
import { Picker, SpinLoading, Toast } from 'antd-mobile'
import { useRouter, useSearchParams } from 'next/navigation'
import { Suspense, useEffect, useRef, useState } from 'react'
import { PageShell } from '@/components/page-shell'
import { useRequireAuth } from '@/features/auth/use-require-auth'
import { getBedDetail, getFloorTree, searchRooms, type BedItem, type RoomCell } from '@/features/dorm-services/api'
import { availableBeds, floorsFromConditionTree, saveRoomDraft } from '@/features/dorm-services/check-in-rules'
import { getTenantConfig } from '@/lib/config/tenant'

function SelectRoomInner() {
  const authorized = useRequireAuth()
  const router = useRouter()
  const params = useSearchParams()
  const config = getTenantConfig()
  const dormitoryId = params.get('dormitoryId') ?? ''
  const roomType = params.get('roomType') ?? ''
  const [floorId, setFloorId] = useState<string | number | undefined>()
  const [bedPicker, setBedPicker] = useState<{ room: RoomCell; beds: BedItem[] } | null>(null)
  const warnedRef = useRef(false)
  const loadingBedsRef = useRef(false)

  const floors = useQuery({
    queryKey: ['check-in-floors', dormitoryId, roomType],
    queryFn: () => getFloorTree({ parkId: config.parkId, dormitoryId, roomType }),
    enabled: authorized && !!dormitoryId && !!roomType,
  })
  const floorList = floors.data?.code === 0 ? floorsFromConditionTree(floors.data.data) : []
  const activeFloorId = floorId ?? floorList[0]?.id

  // Legacy behavior: no floors -> toast and bounce back to /check-in.
  useEffect(() => {
    if (!warnedRef.current && floors.isSuccess && floors.data.code === 0 && floorList.length === 0) {
      warnedRef.current = true
      Toast.show('无房间信息！')
      router.replace('/check-in')
    } else if (!warnedRef.current && (floors.isError || (floors.isSuccess && floors.data.code !== 0))) {
      warnedRef.current = true
      Toast.show('获取房间信息失败！')
      router.replace('/check-in')
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [floors.isSuccess, floors.isError, floorList.length])

  const rooms = useQuery({
    queryKey: ['check-in-rooms', dormitoryId, roomType, activeFloorId],
    queryFn: () =>
      searchRooms({ parkId: config.parkId, dormitoryId, roomType, floorId: activeFloorId as string | number }),
    enabled: authorized && activeFloorId !== undefined,
  })
  const roomsFailed = rooms.isError || (rooms.isSuccess && rooms.data.code !== 0)
  const roomList = rooms.data?.code === 0 ? (rooms.data.data ?? []) : []

  async function pickRoom(room: RoomCell) {
    if ((room.freeBedNum ?? 0) <= 0) {
      Toast.show('该房间无可选用的床位！')
      return
    }
    if (loadingBedsRef.current) return
    loadingBedsRef.current = true
    try {
      const res = await getBedDetail(room.roomId as string | number)
      if (res.code !== 0) {
        Toast.show(res.message ?? res.msg ?? '获取床位信息失败！')
        return
      }
      const beds = availableBeds(res.data)
      if (beds.length === 0) {
        Toast.show('无可选用的床位！')
        return
      }
      setBedPicker({ room, beds })
    } catch {
      Toast.show('获取床位信息失败！')
    } finally {
      loadingBedsRef.current = false
    }
  }

  if (!authorized) return null

  return (
    <PageShell title="选择房间">
      {floors.isPending ? (
        <div className="flex justify-center py-16">
          <SpinLoading color="primary" />
        </div>
      ) : (
        <div className="flex gap-3">
          {/* 左侧楼层 */}
          <div className="flex w-20 flex-none flex-col gap-1.5">
            {floorList.map((floor) => (
              <button
                key={String(floor.id)}
                type="button"
                aria-pressed={floor.id === activeFloorId}
                onClick={() => setFloorId(floor.id)}
                className={`min-h-11 rounded-xl text-sm font-semibold ${
                  floor.id === activeFloorId ? 'bg-brand text-white' : 'bg-white text-mid'
                }`}
              >
                {floor.label}层
              </button>
            ))}
          </div>

          {/* 右侧房间格 */}
          <div className="grid flex-1 auto-rows-min grid-cols-2 gap-2.5">
            {rooms.isPending ? (
              <div className="col-span-2 flex justify-center py-8">
                <SpinLoading color="primary" />
              </div>
            ) : roomsFailed ? (
              <p className="col-span-2 py-8 text-center text-sm text-[#d83b36]">
                {rooms.data?.message ?? '获取房间信息失败！'}
              </p>
            ) : roomList.length === 0 ? (
              <p className="col-span-2 py-8 text-center text-sm text-weak">本层暂无房间</p>
            ) : (
              roomList.map((room) => (
                <button
                  key={String(room.roomId)}
                  type="button"
                  onClick={() => void pickRoom(room)}
                  className="rounded-xl bg-white p-3 text-left shadow-[0_16px_40px_rgba(89,87,87,0.06)] active:bg-surface"
                >
                  <span className="block text-[15px] font-bold">{room.roomName}房</span>
                  <span className="mt-1 block text-xs text-mid">
                    {room.roomSex === 0 ? '男' : room.roomSex === 1 ? '女' : ''} {room.freeBedNum}/
                    {room.bedTotal}
                  </span>
                </button>
              ))
            )}
          </div>
        </div>
      )}

      <Picker
        visible={bedPicker !== null}
        title="选择床位"
        onClose={() => setBedPicker(null)}
        columns={[(bedPicker?.beds ?? []).map((b) => ({ label: `${b.bedNumber}床`, value: String(b.id) }))]}
        onConfirm={(v) => {
          if (!bedPicker || typeof v[0] !== 'string') return
          const bed = bedPicker.beds.find((b) => String(b.id) === v[0])
          if (!bed) return
          saveRoomDraft({
            floorId: activeFloorId as string | number,
            roomId: bedPicker.room.roomId as string | number,
            roomName: bedPicker.room.roomName ?? '',
            bedId: bed.id as string | number,
            bedNumber: bed.bedNumber ?? 0,
          })
          router.back()
        }}
      />
    </PageShell>
  )
}

export default function SelectRoomPage() {
  return (
    <Suspense>
      <SelectRoomInner />
    </Suspense>
  )
}
