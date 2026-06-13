import { beforeEach, describe, expect, it } from 'vitest'
import { loadDetailSnapshot, saveDetailSnapshot, snapshotItemAt } from './detail-snapshot'

describe('详情只读链快照', () => {
  beforeEach(() => sessionStorage.clear())

  it('存取往返', () => {
    expect(loadDetailSnapshot()).toBeNull()
    saveDetailSnapshot({ persons: [{ gh: 'YT1', xm: '李四' }] })
    expect(loadDetailSnapshot()?.persons?.[0]?.xm).toBe('李四')
  })

  it('损坏数据（非 JSON / 非对象）返回 null', () => {
    sessionStorage.setItem('good-release-detail-items', 'not-json')
    expect(loadDetailSnapshot()).toBeNull()
    sessionStorage.setItem('good-release-detail-items', '[1,2]')
    expect(loadDetailSnapshot()).toBeNull()
  })

  it('下标取条目：越界 / 负数 / 非整数 / 缺列表都返回 null', () => {
    const list = [{ gh: 'a' }, { gh: 'b' }]
    expect(snapshotItemAt(list, 1)?.gh).toBe('b')
    expect(snapshotItemAt(list, 2)).toBeNull()
    expect(snapshotItemAt(list, -1)).toBeNull()
    expect(snapshotItemAt(list, 0.5)).toBeNull()
    expect(snapshotItemAt(undefined, 0)).toBeNull()
  })
})
