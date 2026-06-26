import { describe, expect, it } from 'vitest'
import { roomTreeScopeForNode, shouldShowRoomTreeNode } from './room-tree-rules'

describe('shouldShowRoomTreeNode', () => {
  it('空过滤值显示全部节点，非空时沿用 label.indexOf 匹配', () => {
    expect(shouldShowRoomTreeNode('', { label: 'A栋' })).toBe(true)
    expect(shouldShowRoomTreeNode(null, { label: 'A栋' })).toBe(true)
    expect(shouldShowRoomTreeNode('A', { label: 'A栋' })).toBe(true)
    expect(shouldShowRoomTreeNode('B', { label: 'A栋' })).toBe(false)
  })
})

describe('roomTreeScopeForNode', () => {
  it('按旧节点层级规则计算树范围，并只在楼层节点触发房间查询', () => {
    const parkNode = { level: 1 }
    const dormitoryNode = { level: 2, parent: { data: { id: 10 } } }
    const floorNode = {
      level: 3,
      parent: {
        data: { id: 20 },
        parent: { data: { id: 10 } }
      }
    }

    expect(roomTreeScopeForNode({ id: 10 }, parkNode)).toStrictEqual({
      scope: { parkId: 10 },
      shouldQueryRooms: false
    })
    expect(roomTreeScopeForNode({ id: 20 }, dormitoryNode)).toStrictEqual({
      scope: { parkId: 10, dormitoryId: 20 },
      shouldQueryRooms: false
    })
    expect(roomTreeScopeForNode({ id: 30 }, floorNode)).toStrictEqual({
      scope: { parkId: 10, dormitoryId: 20, floorId: 30 },
      shouldQueryRooms: true
    })
    expect(roomTreeScopeForNode({ id: 40 }, { level: 4 })).toStrictEqual({
      scope: {},
      shouldQueryRooms: false
    })
  })
})
