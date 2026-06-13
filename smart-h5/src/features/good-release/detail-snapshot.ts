import type { PersonDetail, ThingDetail } from './api'

/**
 * 详情只读链的数据快照：详情页（work/return-factory）写入，
 * 只读列表/单条页读取（旧版用超长 query JSON 传递）。
 */
export interface DetailSnapshot {
  persons?: PersonDetail[]
  goods?: ThingDetail[]
}

const SNAPSHOT_KEY = 'good-release-detail-items'

export function saveDetailSnapshot(snapshot: DetailSnapshot): void {
  sessionStorage.setItem(SNAPSHOT_KEY, JSON.stringify(snapshot))
}

/** 缺失或损坏（非 JSON / 非对象）返回 null，调用方负责安全回退。 */
export function loadDetailSnapshot(): DetailSnapshot | null {
  const raw = sessionStorage.getItem(SNAPSHOT_KEY)
  if (raw === null) return null
  try {
    const parsed: unknown = JSON.parse(raw)
    if (typeof parsed !== 'object' || parsed === null || Array.isArray(parsed)) return null
    return parsed as DetailSnapshot
  } catch {
    return null
  }
}

/** 越界 / 非法下标返回 null。 */
export function snapshotItemAt<T>(list: T[] | undefined, index: number): T | null {
  if (!list || !Number.isInteger(index) || index < 0 || index >= list.length) return null
  return list[index] ?? null
}
