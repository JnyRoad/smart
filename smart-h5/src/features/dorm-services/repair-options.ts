export interface RepairOption {
  code: number
  desc: string
}

/** Fallback when the range enum endpoint fails (legacy hardcoded defaults). */
export const FALLBACK_RANGES: RepairOption[] = [
  { code: 1, desc: '宿舍' },
  { code: 2, desc: '办公室' },
  { code: 3, desc: '车间' },
  { code: 4, desc: '园区周边' },
]

/** Fixed repair categories (legacy frontend constant, no backing enum API). */
export const REPAIR_TYPES: RepairOption[] = [
  { code: 1, desc: '灯' },
  { code: 2, desc: '插座' },
  { code: 3, desc: '水龙头' },
  { code: 4, desc: '水管' },
  { code: 5, desc: '门窗' },
  { code: 6, desc: '锁' },
  { code: 7, desc: '空调' },
  { code: 8, desc: '其他' },
  { code: 9, desc: '床' },
  { code: 10, desc: '柜子' },
  { code: 11, desc: '玻璃' },
  { code: 12, desc: '洗手台' },
  { code: 13, desc: '桌椅' },
  { code: 14, desc: '地漏' },
]

/** Range -> building cascade (legacy frontend table). */
const BUILDINGS: Record<number, string[]> = {
  1: ['老工厂1号宿舍', '老工厂2号宿舍', '老工厂3号宿舍', '新工厂宿舍楼'],
  2: ['餐厅三楼', '北门岗', '东门岗', '辅房'],
  3: ['一楼', '二楼', '三楼'],
  4: ['园区周边'],
}

export function buildingsForRange(rangeCode: number): string[] {
  return BUILDINGS[rangeCode] ?? []
}
