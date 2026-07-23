import { create } from 'zustand'
import { persist } from 'zustand/middleware'

/** 主表单字段（提交体 applyMain 的来源，编码取字典 value）。 */
export interface WorkApplyMain {
  fxqc?: number
  sffc?: number
  fxdd?: number
  fxddxq?: string
  dddd?: number
  ddddxq?: string
  sqrjb?: number
  fxsx?: number
  wpfxlb?: number
  fjsc?: string
}

/** 申请链人员（xm 存员工信息接口返回的 id；lcDate 为「日期 时间」整串，提交时拆分）。 */
export interface WorkPerson {
  gh: string
  xm: string | number
  name: string
  lcsy: string
  lcDate: string
}

export interface WorkGood {
  wpbm: string
  wpmc: string
  wpdw: string
  wpsl: string
  jsdw: string
  fxrq: string
  bz: string
  ysfs: number
  xm: string | number
  name: string
  cph: string
}

interface WorkDraftState {
	/** 服务端持久化草稿标识，人员查询与提交均以此验证归属。 */
	releaseId?: string | number
	applyMain: WorkApplyMain
  persons: WorkPerson[]
  goods: WorkGood[]
	patchApplyMain: (p: Partial<WorkApplyMain>) => void
	setReleaseId: (releaseId: string | number) => void
  addPerson: (p: WorkPerson) => void
  updatePerson: (i: number, p: WorkPerson) => void
  removePerson: (i: number) => void
  addGood: (g: WorkGood) => void
  updateGood: (i: number, g: WorkGood) => void
  removeGood: (i: number) => void
  clearAll: () => void
}

/**
 * 办公区放行申请的跨页草稿（主表单 ↔ 人员/物品子流程）。聚合替代旧版
 * applyGoodsWorkInfo / goodsPersonInfo / releaseGoodsInfo 三个 localStorage key；
 * 提交成功 clearAll。
 */
export const useWorkDraft = create<WorkDraftState>()(
  persist(
    (set) => ({
		applyMain: {},
		releaseId: undefined,
      persons: [],
      goods: [],
		patchApplyMain: (p) => set((s) => ({ applyMain: { ...s.applyMain, ...p } })),
		setReleaseId: (releaseId) => set({ releaseId }),
      addPerson: (p) => set((s) => ({ persons: [...s.persons, p] })),
      updatePerson: (i, p) => set((s) => ({ persons: s.persons.map((x, n) => (n === i ? p : x)) })),
      removePerson: (i) => set((s) => ({ persons: s.persons.filter((_, n) => n !== i) })),
      addGood: (g) => set((s) => ({ goods: [...s.goods, g] })),
      updateGood: (i, g) => set((s) => ({ goods: s.goods.map((x, n) => (n === i ? g : x)) })),
      removeGood: (i) => set((s) => ({ goods: s.goods.filter((_, n) => n !== i) })),
		clearAll: () => set({ releaseId: undefined, applyMain: {}, persons: [], goods: [] }),
    }),
    { name: 'goods-work-draft' },
  ),
)
