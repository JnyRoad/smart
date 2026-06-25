import { describe, expect, it } from 'vitest'
import {
  buildRoomListQuery,
  formatRoomExportRows,
  hasRoomListData,
  isEmptyRoomBatchEditForm,
  roomGenderClass,
  roomSelectionState,
  shouldShowRoomTreeNode,
  toCheckedRoomIds,
  toExportRows
} from './room-rules'

describe('roomGenderClass', () => {
  it('按旧 filter 数组顺序映射房间属性 class', () => {
    expect(roomGenderClass(0)).toBe('man')
    expect(roomGenderClass(1)).toBe('woman')
    expect(roomGenderClass(2)).toBe('mix')
    expect(roomGenderClass(3)).toBe('other')
    expect(roomGenderClass('2')).toBe('mix')
    expect(roomGenderClass(4)).toBeUndefined()
  })
})

describe('hasRoomListData', () => {
  it('只有 parkId 有值且 tableData 非空时显示房间列表', () => {
    expect(hasRoomListData(10, [{ id: 1 }])).toBe(true)
    expect(hasRoomListData(null, [{ id: 1 }])).toBe(false)
    expect(hasRoomListData(10, [])).toBe(false)
    expect(hasRoomListData(0, [{ id: 1 }])).toBe(false)
  })
})

describe('shouldShowRoomTreeNode', () => {
  it('空过滤值显示全部节点，非空时沿用 label.indexOf 匹配', () => {
    expect(shouldShowRoomTreeNode('', { label: 'A栋' })).toBe(true)
    expect(shouldShowRoomTreeNode(null, { label: 'A栋' })).toBe(true)
    expect(shouldShowRoomTreeNode('A', { label: 'A栋' })).toBe(true)
    expect(shouldShowRoomTreeNode('B', { label: 'A栋' })).toBe(false)
  })
})

describe('buildRoomListQuery', () => {
  it('按旧 Object.assign 顺序构造列表查询参数，允许传入 params 覆盖树范围字段', () => {
    expect(buildRoomListQuery({
      parkId: 10,
      dormitoryId: 20,
      floorId: 30
    }, {
      roomSex: 1,
      parkId: 99
    })).toStrictEqual({
      asc: 'room_name',
      parkId: 99,
      dormitoryId: 20,
      floorId: 30,
      roomSex: 1
    })

    expect(buildRoomListQuery({
      parkId: 10,
      dormitoryId: 20,
      floorId: 30
    })).toStrictEqual({
      asc: 'room_name',
      parkId: 10,
      dormitoryId: 20,
      floorId: 30
    })
  })
})

describe('room selection rules', () => {
  it('全选时按 tableData 顺序收集房间 id，取消全选时清空', () => {
    const rooms = [{ id: 2 }, { id: 1 }, { id: 1 }]
    expect(toCheckedRoomIds(true, rooms)).toStrictEqual([2, 1, 1])
    expect(toCheckedRoomIds(false, rooms)).toStrictEqual([])
  })

  it('保持旧的全选和半选状态计算，包括空数组时 checkAll 为 true', () => {
    expect(roomSelectionState([1, 2], [{ id: 1 }, { id: 2 }])).toStrictEqual({
      checkAll: true,
      isIndeterminate: false
    })
    expect(roomSelectionState([1], [{ id: 1 }, { id: 2 }])).toStrictEqual({
      checkAll: false,
      isIndeterminate: true
    })
    expect(roomSelectionState([], [])).toStrictEqual({
      checkAll: true,
      isIndeterminate: false
    })
  })
})

describe('room export rules', () => {
  it('按列字段顺序把对象数组映射成二维数组', () => {
    expect(toExportRows(['roomName', 'roomSex'], [
      { roomName: '301', roomSex: '男' },
      { roomName: '302', roomSex: '女' }
    ])).toStrictEqual([
      ['301', '男'],
      ['302', '女']
    ])
  })

  it('格式化导出枚举文案时沿用旧逻辑并原地修改列表项', () => {
    const rows = [
      { id: 1, isDormitoryRoom: 0, roomSex: 0 },
      { id: 2, isDormitoryRoom: 1, roomSex: 1 },
      { id: 3, isDormitoryRoom: 2, roomSex: 2 },
      { id: 4, isDormitoryRoom: null, roomSex: 9 }
    ]

    expect(formatRoomExportRows(rows)).toBe(rows)
    expect(rows).toStrictEqual([
      { id: 1, isDormitoryRoom: '是', roomSex: '男' },
      { id: 2, isDormitoryRoom: '否', roomSex: '女' },
      { id: 3, isDormitoryRoom: 2, roomSex: '夫妻/家属' },
      { id: 4, isDormitoryRoom: null, roomSex: '其他' }
    ])
  })
})

describe('isEmptyRoomBatchEditForm', () => {
  it('只把 null 和 undefined 视为未选择，保持空字符串和 0 的旧行为', () => {
    expect(isEmptyRoomBatchEditForm({
      isDormitoryRoom: null,
      roomType: undefined,
      roomSex: null,
      isCount: undefined
    })).toBe(true)
    expect(isEmptyRoomBatchEditForm({ isDormitoryRoom: 0 })).toBe(false)
    expect(isEmptyRoomBatchEditForm({ roomType: '' })).toBe(false)
    expect(isEmptyRoomBatchEditForm({ roomSex: 0 })).toBe(false)
    expect(isEmptyRoomBatchEditForm({ isCount: 0 })).toBe(false)
  })
})
