import { beforeEach, describe, expect, it } from 'vitest'
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
})
