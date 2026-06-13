import type { LiveRoom } from './api'

/**
 * 旧版选项拼装：value 用 `/` 串起四个 id（床位 id 字段名是 `id`），
 * label 为「宿舍名/楼层名层/房间名号房/床号床」。
 */
export function liveRoomOption(room: LiveRoom): { value: string; label: string } {
  return {
    value: `${room.dormitoryId}/${room.floorId}/${room.roomId}/${room.id}`,
    label: `${room.dormitoryName}/${room.floorName}层/${room.roomName}号房/${room.bedNumber}床`,
  }
}

export function splitLiveRoomValue(value: string): {
  dormitoryId: string
  floorId: string
  roomId: string
  bedId: string
} {
  const [dormitoryId = '', floorId = '', roomId = '', bedId = ''] = value.split('/')
  return { dormitoryId, floorId, roomId, bedId }
}
