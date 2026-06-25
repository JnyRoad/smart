const roomGenderClasses = ['man', 'woman', 'mix', 'other']

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

export function isEmptyRoomBatchEditForm(form) {
  return (
    (form.isDormitoryRoom === null || form.isDormitoryRoom === undefined) &&
    (form.roomType === null || form.roomType === undefined) &&
    (form.roomSex === null || form.roomSex === undefined) &&
    (form.isCount === null || form.isCount === undefined)
  )
}
