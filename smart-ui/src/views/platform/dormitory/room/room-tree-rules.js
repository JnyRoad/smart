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

export function roomInitialTreeSelection(treeData) {
  const selection = {
    parkId: treeData[0].id,
    defaultKey: treeData[0].id
  }

  if (treeData) {
    if (selection.parkId) {
      if (treeData[0].children && treeData[0].children.length > 0) {
        selection.dormitoryId = treeData[0].children[0].id
        selection.defaultKey = selection.dormitoryId
        if (selection.dormitoryId) {
          if (treeData[0].children[0].children[0] && treeData[0].children[0].children[0].length > 0) {
            selection.floorId = treeData[0].children[0].children[0].id
            selection.defaultKey = selection.floorId
          }
        }
      }
    }
  }

  return selection
}
