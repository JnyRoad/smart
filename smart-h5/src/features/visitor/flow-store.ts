import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface HostInfo {
	/** 微信授权后签发的短时草稿会话；仅用于换取一次性访客动作 capability。 */
	visitorDraftToken?: string
	visitorDraftId?: string
  receptionistBadge?: string
  receptionistName?: string
  receptionistPhone?: string
}

export interface EnumValue {
  code: string | number
  desc: string
}

export interface VisitorBase {
  visitorName: string
  visitorPhotoId: string
  certNo: string
  company: string
  cause?: EnumValue
  startTime: string
  endTime: string
  permitFactoryType?: string
}

export interface AreaSelection {
  list: string[]
  custom: string
}

export interface FellowPerson {
  fellowName: string
  fellowPhotoId: string
  certNo: string
}

export interface VisitorCar {
  plate: string
  name: string
  certType: EnumValue
  certImg: string
}

const EMPTY_VISITOR: VisitorBase = {
  visitorName: '',
  visitorPhotoId: '',
  certNo: '',
  company: '',
  startTime: '',
  endTime: '',
}

interface VisitorFlowState {
  host: HostInfo
  visitor: VisitorBase
  areasByFactory: Record<string, AreaSelection>
  fellows: FellowPerson[]
  cars: VisitorCar[]
  phone: string
  patchHost: (p: Partial<HostInfo>) => void
  patchVisitor: (p: Partial<VisitorBase>) => void
  setFactoryAreas: (factoryType: string, sel: AreaSelection) => void
  replaceAreas: (areas: Record<string, AreaSelection>) => void
  addFellow: (f: FellowPerson) => void
  updateFellow: (i: number, f: FellowPerson) => void
  removeFellow: (i: number) => void
  addCar: (c: VisitorCar) => void
  updateCar: (i: number, c: VisitorCar) => void
  removeCar: (i: number) => void
  setPhone: (p: string) => void
  reset: () => void
}

/**
 * Cross-page visitor application draft. Persistence is mandatory: the
 * /visitor entry performs a full-page WeChat OAuth redirect, so in-memory
 * state would be lost on the way back (the legacy app used raw localStorage
 * keys for the same reason).
 */
export const useVisitorFlow = create<VisitorFlowState>()(
  persist(
    (set) => ({
      host: {},
      visitor: EMPTY_VISITOR,
      areasByFactory: {},
      fellows: [],
      cars: [],
      phone: '',
      patchHost: (p) =>
        set((s) => {
          // 旧版曾把微信 openId/unionId 持久化在同一 key；首次拿到新草稿时必须
          // 从状态及 localStorage 序列化载荷中移除，不能仅靠 TypeScript 类型隐藏。
          const host = { ...s.host } as HostInfo & {
            openId?: string
            unionId?: string
          }
          delete host.openId
          delete host.unionId
          return { host: { ...host, ...p } }
        }),
      patchVisitor: (p) => set((s) => ({ visitor: { ...s.visitor, ...p } })),
      setFactoryAreas: (factoryType, sel) =>
        set((s) => ({ areasByFactory: { ...s.areasByFactory, [factoryType]: sel } })),
      replaceAreas: (areasByFactory) => set({ areasByFactory }),
      addFellow: (f) => set((s) => ({ fellows: [...s.fellows, f] })),
      updateFellow: (i, f) => set((s) => ({ fellows: s.fellows.map((x, n) => (n === i ? f : x)) })),
      removeFellow: (i) => set((s) => ({ fellows: s.fellows.filter((_, n) => n !== i) })),
      addCar: (c) => set((s) => ({ cars: [...s.cars, c] })),
      updateCar: (i, c) => set((s) => ({ cars: s.cars.map((x, n) => (n === i ? c : x)) })),
      removeCar: (i) => set((s) => ({ cars: s.cars.filter((_, n) => n !== i) })),
      setPhone: (phone) => set({ phone }),
      reset: () =>
        set({ host: {}, visitor: EMPTY_VISITOR, areasByFactory: {}, fellows: [], cars: [], phone: '' }),
    }),
    { name: 'visitor-flow' },
  ),
)
