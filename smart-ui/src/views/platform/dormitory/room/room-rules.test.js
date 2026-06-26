import { describe, expect, it } from 'vitest'
import {
  buildRoomListQuery,
  createRoomExportConfig,
  createDormFormForPark,
  createEmptyBatchEditForm,
  createEmptyDormForm,
  createEmptyFloorForm,
  createEmptyRoomEditForm,
  createFloorFormForDormitory,
  formatRoomExportRows,
  hasRoomListData,
  isEmptyRoomBatchEditForm,
  roomGenderClass,
  roomSelectionState,
  toCheckedRoomIds,
  toExportRows,
  validateFloorCount,
  validateFloorStartNumber,
  validateRoomCount
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
  it('导出配置保留旧表头和字段顺序，并为每次导出返回新数组', () => {
    const config = createRoomExportConfig()
    const nextConfig = createRoomExportConfig()

    expect(config).toStrictEqual({
      headers: ['房间号', '是否参与分配', '是否参与计算', '宿舍分类', '床位数', '实住人数', '差异人数', '房间属性', '所属园区'],
      fields: ['roomName', 'isDormitoryRoom', 'isCount', 'typeName', 'bedTotal', 'usedBed', 'freeBed', 'roomSex', 'parkName']
    })
    expect(config.headers).not.toBe(nextConfig.headers)
    expect(config.fields).not.toBe(nextConfig.fields)
  })

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

describe('room form state factories', () => {
  it('构造空楼层新增表单时保留旧字段和值', () => {
    expect(createEmptyFloorForm()).toStrictEqual({
      parkId: undefined,
      dormitoryId: undefined,
      startNum: undefined,
      floorNum: undefined
    })
    expect(createEmptyFloorForm()).not.toBe(createEmptyFloorForm())
  })

  it('构造空楼栋表单时保留旧字段和值', () => {
    expect(createEmptyDormForm()).toStrictEqual({
      parkId: undefined,
      dormitoryName: undefined
    })
    expect(createEmptyDormForm()).not.toBe(createEmptyDormForm())
  })

  it('构造空批量编辑表单时保留旧字段和值，并为 roomIds 返回新数组', () => {
    const firstForm = createEmptyBatchEditForm()
    const secondForm = createEmptyBatchEditForm()

    expect(firstForm).toStrictEqual({
      roomIds: [],
      isDormitoryRoom: undefined,
      isCount: undefined,
      roomType: undefined,
      roomSex: undefined,
      sdTemplateId: undefined
    })
    expect(firstForm).not.toBe(secondForm)
    expect(firstForm.roomIds).not.toBe(secondForm.roomIds)
  })

  it('构造空房间编辑表单时保留旧字段和值', () => {
    expect(createEmptyRoomEditForm()).toStrictEqual({
      roomName: undefined,
      isDormitoryRoom: undefined,
      isCount: undefined,
      roomType: undefined,
      bedTotal: undefined,
      roomSex: undefined,
      sdTemplateId: undefined,
      leaveTempName: undefined,
      leaveTempId: undefined
    })
    expect(createEmptyRoomEditForm()).not.toBe(createEmptyRoomEditForm())
  })

  it('新增楼栋和新增楼层表单只按旧逻辑带入当前树范围 id', () => {
    expect(createDormFormForPark(10)).toStrictEqual({
      parkId: 10,
      dormitoryName: undefined
    })
    expect(createFloorFormForDormitory(10, 20)).toStrictEqual({
      parkId: 10,
      dormitoryId: 20,
      startNum: undefined,
      floorNum: undefined
    })
  })
})

function collectValidatorCalls(validator, value) {
  const calls = []
  validator({}, value, error => {
    calls.push(error ? error.message : undefined)
  })
  return calls
}

describe('room form validators', () => {
  it('保留楼层起始编号校验的旧 callback 行为和文案', () => {
    expect(collectValidatorCalls(validateFloorStartNumber, 0)).toStrictEqual([
      '请输入非0整数',
      undefined
    ])
    expect(collectValidatorCalls(validateFloorStartNumber, '0')).toStrictEqual([undefined])
    expect(collectValidatorCalls(validateFloorStartNumber, '-1')).toStrictEqual([undefined])
    expect(collectValidatorCalls(validateFloorStartNumber, '1.5')).toStrictEqual(['请输入整数'])
    expect(collectValidatorCalls(validateFloorStartNumber, 'A')).toStrictEqual(['请输入整数'])
  })

  it('保留楼层数量校验的旧 callback 行为和上限文案', () => {
    expect(collectValidatorCalls(validateFloorCount, 0)).toStrictEqual([
      '请输入大于0的正整数',
      undefined
    ])
    expect(collectValidatorCalls(validateFloorCount, '0')).toStrictEqual([
      '请输入大于0的正整数',
      undefined
    ])
    expect(collectValidatorCalls(validateFloorCount, '15')).toStrictEqual(['楼层数量最大值为14'])
    expect(collectValidatorCalls(validateFloorCount, '-1')).toStrictEqual(['请输入正整数'])
    expect(collectValidatorCalls(validateFloorCount, '1.5')).toStrictEqual(['请输入正整数'])
    expect(collectValidatorCalls(validateFloorCount, '14')).toStrictEqual([undefined])
  })

  it('保留房间数量校验允许 0 且拒绝小数和负数的旧行为', () => {
    expect(collectValidatorCalls(validateRoomCount, 0)).toStrictEqual([undefined])
    expect(collectValidatorCalls(validateRoomCount, '0')).toStrictEqual([undefined])
    expect(collectValidatorCalls(validateRoomCount, '24')).toStrictEqual([undefined])
    expect(collectValidatorCalls(validateRoomCount, '-1')).toStrictEqual(['不能输入小数和负数'])
    expect(collectValidatorCalls(validateRoomCount, '1.5')).toStrictEqual(['不能输入小数和负数'])
  })
})
