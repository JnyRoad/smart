import { beforeEach, describe, expect, it } from 'vitest'
import { clearSession, getSessionGeneration, saveSession } from '@/lib/auth/token'
import { useWorkDraft } from './work-draft'

const PERSON = { gh: 'YT2', xm: 9, name: '李四', lcsy: '出差', lcDate: '2026-06-20 09:30' }
const GOOD = {
  wpbm: 'A1',
  wpmc: '电脑',
  wpdw: '台',
  wpsl: '1',
  jsdw: '裕同',
  fxrq: '2026-06-20',
  bz: '',
  ysfs: 1,
  xm: 8,
  name: '王五',
  cph: '',
}

describe('work 草稿 store', () => {
  beforeEach(() => {
    clearSession()
    useWorkDraft.getState().clearAll()
  })

  it('表单字段补丁式更新', () => {
    useWorkDraft.getState().patchApplyMain({ fxqc: 1, fxddxq: '门口' })
    expect(useWorkDraft.getState().applyMain.fxqc).toBe(1)
    expect(useWorkDraft.getState().applyMain.fxddxq).toBe('门口')
  })

  it('人员增改删', () => {
    const s = useWorkDraft.getState()
    s.addPerson(PERSON)
    s.addPerson({ ...PERSON, name: '王五' })
    useWorkDraft.getState().updatePerson(1, { ...PERSON, name: '赵六' })
    expect(useWorkDraft.getState().persons.map((p) => p.name)).toEqual(['李四', '赵六'])
    useWorkDraft.getState().removePerson(0)
    expect(useWorkDraft.getState().persons.map((p) => p.name)).toEqual(['赵六'])
  })

  it('物品增改删', () => {
    const s = useWorkDraft.getState()
    s.addGood(GOOD)
    useWorkDraft.getState().updateGood(0, { ...GOOD, wpmc: '显示器' })
    expect(useWorkDraft.getState().goods[0]?.wpmc).toBe('显示器')
    useWorkDraft.getState().removeGood(0)
    expect(useWorkDraft.getState().goods).toEqual([])
  })

  it('clearAll 清空全部草稿', () => {
	const s = useWorkDraft.getState()
	 s.setReleaseId(17)
    s.patchApplyMain({ fxsx: 0 })
    s.addPerson(PERSON)
    s.addGood(GOOD)
    useWorkDraft.getState().clearAll()
    const after = useWorkDraft.getState()
    expect(after.applyMain).toEqual({})
    expect(after.persons).toEqual([])
	expect(after.goods).toEqual([])
	expect(after.releaseId).toBeUndefined()
  })

  it('账号从 A 切换到 B 后不复用 A 的办公物品放行草稿', () => {
    saveSession({ accessToken: 'employee-A-token' })
    useWorkDraft.getState().patchApplyMain({ fxqc: 1 })
    useWorkDraft.getState().addGood(GOOD)

    saveSession({ accessToken: 'employee-B-token' })

    expect(useWorkDraft.getState().applyMain).toEqual({})
    expect(useWorkDraft.getState().goods).toEqual([])
  })

  it('重新打开页面时不恢复其他会话代际持久化的草稿', async () => {
    saveSession({ accessToken: 'employee-B-token' })
    const currentGeneration = getSessionGeneration()
    // 模拟浏览器持久化层遗留的 A 账号草稿；它不是当前内存 store 写出的内容。
    localStorage.setItem('goods-work-draft', JSON.stringify({
      state: {
        ownerSessionGeneration: currentGeneration - 1,
        releaseId: 18,
        applyMain: { fxqc: 1 },
        persons: [PERSON],
        goods: [GOOD],
      },
      version: 0,
    }))

    await useWorkDraft.persist.rehydrate()

    expect(useWorkDraft.getState().releaseId).toBeUndefined()
    expect(useWorkDraft.getState().applyMain).toEqual({})
    expect(useWorkDraft.getState().persons).toEqual([])
    expect(useWorkDraft.getState().goods).toEqual([])
    expect(useWorkDraft.getState().ownerSessionGeneration).toBe(currentGeneration)
  })
})
