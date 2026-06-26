const roomGenderClasses = ['man', 'woman', 'mix', 'other']

export const roomDormitoryOptions = [
  { label: '是', value: 0 },
  { label: '否', value: 1 }
]

export const roomCountOptions = [
  { label: '是', value: 1 },
  { label: '否', value: 0 }
]

export function roomGenderClass(value) {
  return roomGenderClasses[value]
}

export function hasRoomListData(parkId, tableData) {
  if (parkId && tableData && tableData.length > 0) {
    return true
  }
  return false
}

export function shouldShowRoomTreeNode(value, data) {
  if (!value) return true
  return data.label.indexOf(value) !== -1
}

export function roomTreeScopeForNode(data, node) {
  if (node.level == 1) {
    return {
      scope: { parkId: data.id },
      shouldQueryRooms: false
    }
  }

  if (node.level == 2) {
    return {
      scope: {
        parkId: node.parent.data.id,
        dormitoryId: data.id
      },
      shouldQueryRooms: false
    }
  }

  if (node.level == 3) {
    return {
      scope: {
        parkId: node.parent.parent.data.id,
        dormitoryId: node.parent.data.id,
        floorId: data.id
      },
      shouldQueryRooms: true
    }
  }

  return {
    scope: {},
    shouldQueryRooms: false
  }
}

export function buildRoomListQuery(scope, params) {
  return Object.assign(
    {
      asc: 'room_name',
      parkId: scope.parkId,
      dormitoryId: scope.dormitoryId,
      floorId: scope.floorId
    },
    params
  )
}

export function toCheckedRoomIds(checked, tableData) {
  if (!checked) {
    return []
  }
  const roomIds = []
  tableData.forEach(room => {
    roomIds.push(room.id)
  })
  return roomIds
}

export function roomSelectionState(value, tableData) {
  const checkedCount = value.length
  return {
    checkAll: checkedCount === tableData.length,
    isIndeterminate: checkedCount > 0 && checkedCount < tableData.length
  }
}

const roomExportHeaders = ['房间号', '是否参与分配', '是否参与计算', '宿舍分类', '床位数', '实住人数', '差异人数', '房间属性', '所属园区']
const roomExportFields = ['roomName', 'isDormitoryRoom', 'isCount', 'typeName', 'bedTotal', 'usedBed', 'freeBed', 'roomSex', 'parkName']

export function createRoomExportConfig() {
  return {
    headers: roomExportHeaders.slice(),
    fields: roomExportFields.slice()
  }
}

export function toExportRows(filterVal, jsonData) {
  return jsonData.map(row => filterVal.map(field => row[field]))
}

export function formatRoomExportRows(list) {
  list.forEach(function (item) {
    if (item.isDormitoryRoom == 0) {
      item.isDormitoryRoom = '是'
    } else if (item.isDormitoryRoom == 1) {
      item.isDormitoryRoom = '否'
    }

    if (item.roomSex == 0) {
      item.roomSex = '男'
    } else if (item.roomSex == 1) {
      item.roomSex = '女'
    } else if (item.roomSex == 2) {
      item.roomSex = '夫妻/家属'
    } else {
      item.roomSex = '其他'
    }
  })
  return list
}

export function createEmptyFloorForm() {
  return {
    parkId: undefined,
    dormitoryId: undefined,
    startNum: undefined,
    floorNum: undefined
  }
}

export function createFloorFormForDormitory(parkId, dormitoryId) {
  return Object.assign(createEmptyFloorForm(), {
    parkId,
    dormitoryId
  })
}

export function createEmptyDormForm() {
  return {
    parkId: undefined,
    dormitoryName: undefined
  }
}

export function createDormFormForPark(parkId) {
  return Object.assign(createEmptyDormForm(), {
    parkId
  })
}

export function createEmptyBatchEditForm() {
  return {
    roomIds: [],
    isDormitoryRoom: undefined,
    isCount: undefined,
    roomType: undefined,
    roomSex: undefined,
    sdTemplateId: undefined
  }
}

export function createEmptyRoomEditForm() {
  return {
    roomName: undefined,
    isDormitoryRoom: undefined,
    isCount: undefined,
    roomType: undefined,
    bedTotal: undefined,
    roomSex: undefined,
    sdTemplateId: undefined,
    leaveTempName: undefined,
    leaveTempId: undefined
  }
}

export function createFloorAddRules() {
  return {
    startNum: [
      { required: true, message: '请输入起始编号', trigger: 'blur' },
      { validator: validateFloorStartNumber, trigger: 'blur' }
    ],
    floorNum: [
      { required: true, message: '请输入楼层数量', trigger: 'blur' },
      { validator: validateFloorCount, trigger: 'blur' }
    ]
  }
}

export function createFloorEditRules() {
  return {
    roomNum: [
      { required: true, message: '请输入房间数量', trigger: 'blur' },
      { validator: validateRoomCount, trigger: 'blur' }
    ]
  }
}

export function createDormRules() {
  return {
    dormitoryName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }]
  }
}

export function createBatchEditRules() {
  return {
    sdTemplateId: [{ required: true, message: '请选择水电模板', trigger: 'change' }]
  }
}

export function createRoomEditRules() {
  return {
    roomName: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
    isDormitoryRoom: [{ required: true, message: '请选择是否参与分配', trigger: 'change' }],
    isCount: [{ required: true, message: '请选择是否参与计算', trigger: 'change' }],
    roomType: [{ required: true, message: '请选择宿舍分类', trigger: 'change' }],
    bedTotal: [{ required: true, message: '请输入床位数', trigger: 'blur' }],
    roomSex: [{ required: true, message: '请选择房间属性', trigger: 'change' }],
    sdTemplateId: [{ required: true, message: '请选择水电模板', trigger: 'change' }]
  }
}

export function isEmptyRoomBatchEditForm(form) {
  return (
    (form.isDormitoryRoom === null || form.isDormitoryRoom === undefined) &&
    (form.roomType === null || form.roomType === undefined) &&
    (form.roomSex === null || form.roomSex === undefined) &&
    (form.isCount === null || form.isCount === undefined)
  )
}

export function validateFloorStartNumber(rule, value, callback) {
  const regName = /^-?\d+$/
  if (value === 0) {
    callback(new Error('请输入非0整数'))
  }
  if (!regName.test(value)) {
    callback(new Error('请输入整数'))
  } else {
    callback()
  }
}

export function validateFloorCount(rule, value, callback) {
  const regName = /^\d+$/
  if (value == 0) {
    callback(new Error('请输入大于0的正整数'))
  }

  if (!regName.test(value)) {
    callback(new Error('请输入正整数'))
  } else if (value > 14) {
    callback(new Error('楼层数量最大值为14'))
  } else {
    callback()
  }
}

export function validateRoomCount(rule, value, callback) {
  const regName = /^\d+$/

  if (!regName.test(value)) {
    callback(new Error('不能输入小数和负数'))
  } else {
    callback()
  }
}
