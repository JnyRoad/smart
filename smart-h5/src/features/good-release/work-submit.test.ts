import { describe, expect, it } from 'vitest'
import { buildWorkSubmitBody } from './work-submit'
import type { WorkApplyMain, WorkGood, WorkPerson } from './work-draft'

const APPLY_MAIN: WorkApplyMain = {
  fxqc: 0,
  sffc: 0,
  fxdd: 1,
  fxddxq: 'B栋门口',
  dddd: 2,
  ddddxq: 'C栋仓库',
  sqrjb: 1,
  fxsx: 0,
  wpfxlb: 0,
  fjsc: '',
}

const PERSON: WorkPerson = { gh: 'YT2', xm: 9, name: '李四', lcsy: '出差', lcDate: '2026-06-20 09:30' }
const GOOD: WorkGood = {
  wpbm: 'A1',
  wpmc: '电脑',
  wpdw: '台',
  wpsl: '1',
  jsdw: '裕同',
  fxrq: '2026-06-20 00:00',
  bz: '',
  ysfs: 1,
  xm: 8,
  name: '王五',
  cph: '',
}

describe('buildWorkSubmitBody（旧 index.vue:213-272 组装事实）', () => {
  it('人员放行分支：personList 组装（lcDate 拆 lcrq/lcsj），thingList 为空数组', () => {
    const body = buildWorkSubmitBody({
      applyMain: APPLY_MAIN,
      badge: 'YT1',
      parkId: 5000021,
      persons: [PERSON],
      goods: [GOOD],
    })
    expect(body.applyMain).toEqual(APPLY_MAIN)
    expect(body.badge).toBe('YT1')
    expect(body.parkId).toBe(5000021)
    expect(body.status).toBe(1)
    expect(body.personList).toEqual([
      { gh: 'YT2', xm: 9, name: '李四', lcsy: '出差', lcrq: '2026-06-20', lcsj: '09:30' },
    ])
    expect(body.thingList).toEqual([])
  })

  it('物品放行分支：thingList 组装（fxrq 取日期段），personList 为空数组', () => {
    const body = buildWorkSubmitBody({
      applyMain: { ...APPLY_MAIN, fxsx: 1 },
      badge: 'YT1',
      parkId: 5000021,
      persons: [PERSON],
      goods: [GOOD],
    })
    expect(body.personList).toEqual([])
    expect(body.thingList).toEqual([
      {
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
      },
    ])
  })

  it('fxsx=7（出差人员放行）也走人员分支', () => {
    const body = buildWorkSubmitBody({
      applyMain: { ...APPLY_MAIN, fxsx: 7 },
      badge: 'YT1',
      parkId: 5000021,
      persons: [PERSON],
      goods: [],
    })
    expect(body.personList).toHaveLength(1)
    expect(body.thingList).toEqual([])
  })
})
