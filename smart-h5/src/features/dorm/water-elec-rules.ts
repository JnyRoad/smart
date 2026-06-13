export interface CateInfo {
  cateName?: string
  fee?: number
}

/** Legacy display rule: drop 热水 entries, rename 冷水 to 水. */
export function normalizeCateInfos(cateInfos: CateInfo[] | undefined): CateInfo[] {
  return (cateInfos ?? [])
    .filter((c) => c.cateName !== '热水')
    .map((c) => (c.cateName === '冷水' ? { ...c, cateName: '水' } : c))
}
