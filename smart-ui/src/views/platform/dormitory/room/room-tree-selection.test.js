import { describe, expect, it } from 'vitest'
import { roomInitialTreeSelection } from './room-rules'

describe('room initial tree selection', () => {
  it('只有园区节点时只覆盖 parkId 和 defaultKey，不清空旧下级 id', () => {
    expect(roomInitialTreeSelection([
      { id: 10, label: '许昌园区', children: [] }
    ])).toStrictEqual({
      parkId: 10,
      defaultKey: 10
    })
  })

  it('存在楼栋时按旧逻辑默认选中第一个楼栋', () => {
    expect(roomInitialTreeSelection([
      {
        id: 10,
        label: '许昌园区',
        children: [{ id: 20, label: 'A栋', children: [] }]
      }
    ])).toStrictEqual({
      parkId: 10,
      dormitoryId: 20,
      defaultKey: 20
    })
  })

  it('普通楼层对象没有 length 时保留旧逻辑，不默认选中楼层', () => {
    expect(roomInitialTreeSelection([
      {
        id: 10,
        label: '许昌园区',
        children: [
          {
            id: 20,
            label: 'A栋',
            children: [{ id: 30, label: '1楼' }]
          }
        ]
      }
    ])).toStrictEqual({
      parkId: 10,
      dormitoryId: 20,
      defaultKey: 20
    })
  })

  it('楼层节点存在 length 大于 0 时保留旧条件并选中楼层', () => {
    expect(roomInitialTreeSelection([
      {
        id: 10,
        label: '许昌园区',
        children: [
          {
            id: 20,
            label: 'A栋',
            children: [{ id: 30, label: '1楼', length: 1 }]
          }
        ]
      }
    ])).toStrictEqual({
      parkId: 10,
      dormitoryId: 20,
      floorId: 30,
      defaultKey: 30
    })
  })
})
