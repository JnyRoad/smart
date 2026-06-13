/** Fixed dictionaries from the legacy const.js — values are backend codes. */

export interface DictOption {
  value: number
  label: string
}

export const FXQC_OPTIONS: DictOption[] = [
  { value: 0, label: '厂内' },
  { value: 1, label: '厂外' },
]

export const SFFC_OPTIONS: DictOption[] = [
  { value: 0, label: '是' },
  { value: 1, label: '否' },
]

/** 出发地点与到达地点共用同一组取值。 */
export const FXDD_OPTIONS: DictOption[] = [
  { value: 0, label: 'A栋' },
  { value: 1, label: 'B栋' },
  { value: 2, label: 'C栋' },
  { value: 3, label: 'D栋' },
  { value: 4, label: 'E栋' },
  { value: 5, label: 'F栋' },
  { value: 6, label: '福侨4号仓库' },
  { value: 7, label: '厂区加工中心' },
  { value: 8, label: '纸托' },
  { value: 9, label: '万顺' },
  { value: 11, label: '市场开发部/生活区' },
  { value: 10, label: '其它' },
]

export const DDDD_OPTIONS = FXDD_OPTIONS

export const SQRJB_OPTIONS: DictOption[] = [
  { value: 0, label: '周边职员级以下' },
  { value: 4, label: '生产楼层长以下' },
  { value: 3, label: '生产楼层长(含)以上' },
  { value: 1, label: '课长级' },
  { value: 2, label: '经理级' },
]

export const FXSX_OPTIONS: DictOption[] = [
  { value: 0, label: '人员放行' },
  { value: 7, label: '人员放行(仅限出差使用)' },
  { value: 1, label: '非保密物品放行' },
  { value: 4, label: '保密物品放行' },
  { value: 6, label: '电脑放行' },
  { value: 5, label: '固定资产放行(不包含电脑)' },
  { value: 3, label: '空车放行' },
  { value: 8, label: '自动化物品放行' },
  { value: 10, label: '废品出售' },
]

export const WPFXLB_OPTIONS: DictOption[] = [
  { value: 0, label: '领/退/转料' },
  { value: 2, label: '异动/转卖' },
  { value: 3, label: '其它' },
]

export const YSFS_OPTIONS: DictOption[] = [
  { value: 0, label: '人工' },
  { value: 1, label: '货车' },
  { value: 2, label: '叉车' },
  { value: 3, label: '三轮' },
]

/** 放行事项 0/7 走「人员放行」子流程，其余走「物品放行」。 */
export function isPersonRelease(fxsx: number | undefined): boolean {
  return fxsx === 0 || fxsx === 7
}

export function ysfsLabel(value: number | undefined): string {
  return YSFS_OPTIONS.find((o) => o.value === value)?.label ?? ''
}
