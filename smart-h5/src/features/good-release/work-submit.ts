import { isPersonRelease } from './dicts'
import type { WorkApplyMain, WorkGood, WorkPerson } from './work-draft'

/** 「YYYY-MM-DD HH:mm」→ 日期段（无空格时整串当日期，时间段为空串）。 */
function dateSegment(value: string): string {
  return value.split(' ')[0] ?? ''
}

function timeSegment(value: string): string {
  return value.split(' ')[1] ?? ''
}

/**
 * 旧版提交体组装：applyMain 为嵌套对象，personList/thingList 按 fxsx 分支
 * 二选一组装、另一侧恒为空数组；人员 lcDate 拆 lcrq/lcsj，物品 fxrq 取日期段。
 */
export function buildWorkSubmitBody(input: {
  applyMain: WorkApplyMain
	releaseId: string | number
  parkId: number
  persons: WorkPerson[]
  goods: WorkGood[]
}): {
  applyMain: WorkApplyMain
	releaseId: string | number
  parkId: number
  status: 1
  personList: Record<string, unknown>[]
  thingList: Record<string, unknown>[]
} {
  const personBranch = isPersonRelease(input.applyMain.fxsx)
  return {
    applyMain: input.applyMain,
		releaseId: input.releaseId,
    parkId: input.parkId,
    status: 1,
    personList: personBranch
      ? input.persons.map((p) => ({
          gh: p.gh,
          xm: p.xm,
          name: p.name,
          lcsy: p.lcsy,
          lcrq: dateSegment(p.lcDate),
          lcsj: timeSegment(p.lcDate),
        }))
      : [],
    thingList: personBranch
      ? []
      : input.goods.map((g) => ({
          wpbm: g.wpbm,
          wpmc: g.wpmc,
          wpdw: g.wpdw,
          wpsl: g.wpsl,
          jsdw: g.jsdw,
          fxrq: dateSegment(g.fxrq),
          bz: g.bz,
          ysfs: g.ysfs,
          xm: g.xm,
          name: g.name,
          cph: g.cph,
        })),
  }
}
