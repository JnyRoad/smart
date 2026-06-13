import type { MyRoom } from './api'

export interface RoomChoice {
  value: string
  label: string
}

/** Legacy option shape: value `${dormitoryId}/${roomId}`, label `${name}/${room}号房`. */
export function roomOption(room: MyRoom): RoomChoice {
  return {
    value: `${room.dormitoryId}/${room.roomId}`,
    label: `${room.dormitoryName}/${room.roomName}号房`,
  }
}

/** Splits selected values back into the submit body's parallel id arrays. */
export function splitRoomValues(values: string[]): { dormitoryIds: string[]; roomIds: string[] } {
  const dormitoryIds: string[] = []
  const roomIds: string[] = []
  for (const value of values) {
    const [dormitoryId, roomId] = value.split('/')
    dormitoryIds.push(dormitoryId ?? '')
    roomIds.push(roomId ?? '')
  }
  return { dormitoryIds, roomIds }
}

/** Repeated picks of the same room are ignored (legacy behavior). */
export function addRoomDeduped(list: RoomChoice[], room: RoomChoice): RoomChoice[] {
  return list.some((item) => item.value === room.value) ? list : [...list, room]
}
