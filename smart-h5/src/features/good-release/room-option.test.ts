import { describe, expect, it } from 'vitest'
import { liveRoomOption, splitLiveRoomValue } from './room-option'

describe('生活区房间选项（旧 index.vue:151-152 拼装事实）', () => {
  it('value 四段 id（床位 id 字段是 id）、label 四段中文', () => {
    const opt = liveRoomOption({
      dormitoryId: 'D1',
      floorId: 'F2',
      roomId: 'R3',
      id: 'B4',
      dormitoryName: '新工厂宿舍楼',
      floorName: '2',
      roomName: '302',
      bedNumber: 4,
    })
    expect(opt.value).toBe('D1/F2/R3/B4')
    expect(opt.label).toBe('新工厂宿舍楼/2层/302号房/4床')
  })

  it('value 拆回四个提交字段', () => {
    expect(splitLiveRoomValue('D1/F2/R3/B4')).toEqual({
      dormitoryId: 'D1',
      floorId: 'F2',
      roomId: 'R3',
      bedId: 'B4',
    })
  })
})
