import { beforeEach, describe, expect, it } from 'vitest'
import {
  availableBeds,
	checkInSelectionToSubmit,
  clearFormDraft,
  clearRoomDraft,
  floorsFromConditionTree,
  loadFormDraft,
  loadRoomDraft,
  saveFormDraft,
  saveRoomDraft,
} from './check-in-rules'

describe('availableBeds', () => {
  it('仅保留 staffBadge 严格为 null 且未停用的床（空串/缺字段都算占用）', () => {
    expect(
      availableBeds([
        { id: 'b1', bedNumber: 1, staffBadge: null, delFlag: 0 },
        { id: 'b2', bedNumber: 2, staffBadge: 'YT1', delFlag: 0 },
        { id: 'b3', bedNumber: 3, staffBadge: null, delFlag: 1 },
        { id: 'b4', bedNumber: 4, staffBadge: '', delFlag: 0 },
        { id: 'b5', bedNumber: 5, delFlag: 0 },
      ]),
    ).toEqual([{ id: 'b1', bedNumber: 1, staffBadge: null, delFlag: 0 }])
  })

  it('空入参容错', () => {
    expect(availableBeds(undefined)).toEqual([])
  })
})

describe('floorsFromConditionTree', () => {
  it('取 data[0].children[0].children 层级', () => {
    expect(
      floorsFromConditionTree([
        { id: 'P', children: [{ id: 'B', children: [{ id: 'F1', label: '1' }, { id: 'F2', label: '2' }] }] },
      ]),
    ).toEqual([
      { id: 'F1', label: '1' },
      { id: 'F2', label: '2' },
    ])
  })

  it('任一层缺失返回空数组', () => {
    expect(floorsFromConditionTree(undefined)).toEqual([])
    expect(floorsFromConditionTree([])).toEqual([])
    expect(floorsFromConditionTree([{ id: 'P' }])).toEqual([])
    expect(floorsFromConditionTree([{ id: 'P', children: [{ id: 'B' }] }])).toEqual([])
  })
})

describe('checkInSelectionToSubmit', () => {
	it('仅提交宿舍选择，不携带员工身份、证件或工号', () => {
		expect(
			checkInSelectionToSubmit({
				parkId: 1,
				dormitoryId: 'D1',
				roomType: 2,
				floorId: 'F1',
				roomId: 'R301',
				bedId: 'B4',
			}),
		).toEqual({
			parkId: 1,
			dormitoryId: 'D1',
			roomType: 2,
			floorId: 'F1',
			roomId: 'R301',
			bedId: 'B4',
		})
	})
})

describe('room draft (sessionStorage)', () => {
  beforeEach(() => sessionStorage.clear())

  it('存取与清除', () => {
    expect(loadRoomDraft()).toBeNull()
    saveRoomDraft({ floorId: 'f', roomId: 'r', roomName: '302', bedId: 'b', bedNumber: 2 })
    expect(loadRoomDraft()?.roomName).toBe('302')
    expect(loadRoomDraft()?.bedNumber).toBe(2)
    clearRoomDraft()
    expect(loadRoomDraft()).toBeNull()
  })

  it('损坏数据返回 null', () => {
    sessionStorage.setItem('check-in-room', 'not-json')
    expect(loadRoomDraft()).toBeNull()
  })
})

describe('form draft (sessionStorage)', () => {
  beforeEach(() => sessionStorage.clear())

  it('存取与清除', () => {
    expect(loadFormDraft()).toBeNull()
    saveFormDraft({ dormitoryId: 'D1', dormitoryName: '新工厂宿舍楼', roomTypeCode: 1, roomTypeDesc: '四人间' })
    expect(loadFormDraft()?.dormitoryName).toBe('新工厂宿舍楼')
    expect(loadFormDraft()?.roomTypeCode).toBe(1)
    clearFormDraft()
    expect(loadFormDraft()).toBeNull()
  })

  it('损坏数据返回 null', () => {
    sessionStorage.setItem('check-in-form', 'not-json')
    expect(loadFormDraft()).toBeNull()
  })
})
