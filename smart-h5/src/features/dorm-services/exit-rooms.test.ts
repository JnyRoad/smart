import { describe, expect, it } from 'vitest'
import { addRoomDeduped, roomOption, splitRoomValues } from './exit-rooms'

describe('exit rooms', () => {
  it('roomOption 组装 value/label（旧版格式）', () => {
    expect(
      roomOption({ dormitoryId: 'D1', roomId: 'R2', dormitoryName: '新工厂宿舍楼', roomName: '302' }),
    ).toEqual({ value: 'D1/R2', label: '新工厂宿舍楼/302号房' })
  })

  it('splitRoomValues 拆 dormitoryIds/roomIds', () => {
    expect(splitRoomValues(['D1/R2', 'D3/R4'])).toEqual({
      dormitoryIds: ['D1', 'D3'],
      roomIds: ['R2', 'R4'],
    })
  })

  it('addRoomDeduped 去重', () => {
    expect(addRoomDeduped([{ value: 'D1/R2', label: 'x' }], { value: 'D1/R2', label: 'x' })).toHaveLength(1)
    expect(addRoomDeduped([{ value: 'D1/R2', label: 'x' }], { value: 'D1/R3', label: 'y' })).toHaveLength(2)
  })
})
