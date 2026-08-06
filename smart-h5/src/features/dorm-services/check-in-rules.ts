import type { BedItem, ParkTreeNode, StaffIdentity } from './api'

/**
 * Occupied beds and soft-deleted beds (delFlag=1) are not selectable. The
 * legacy rule keeps only beds whose staffBadge is exactly null — an empty
 * string (or a missing field) still counts as occupied-by-record.
 */
export function availableBeds(beds: BedItem[] | undefined): BedItem[] {
  return (beds ?? []).filter((bed) => bed.staffBadge === null && bed.delFlag !== 1)
}

/** /park/tree/condition returns park → building → floors; flatten to the floor list. */
export function floorsFromConditionTree(data: ParkTreeNode[] | undefined): ParkTreeNode[] {
  return data?.[0]?.children?.[0]?.children ?? []
}

/**
 * Maps the raw /staff/define/badge shape onto the autoallot submit keys
 * (birth→birthday, homeAddress→address, validDate/validDateFm→start/end).
 */
export function identityToSubmitFields(user: StaffIdentity): Record<string, unknown> {
  return {
    name: user.name,
    sex: user.sex,
    nation: user.nation,
    certno: user.certno,
    birthday: user.birth,
    address: user.homeAddress,
    signOrg: null,
    validDateStart: user.validDate,
    validDateEnd: user.validDateFm,
  }
}

export interface RoomDraft {
  floorId: string | number
  roomId: string | number
  roomName: string
  bedId: string | number
  bedNumber: number
}

const DRAFT_KEY = 'check-in-room'

/**
 * Session-scoped draft carrying the room selection back from the select-room
 * page (the legacy app used a localStorage `roomInfo` for the same purpose).
 */
export function saveRoomDraft(draft: RoomDraft): void {
  sessionStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
}

export function loadRoomDraft(): RoomDraft | null {
  const raw = sessionStorage.getItem(DRAFT_KEY)
  if (raw === null) return null
  try {
    return JSON.parse(raw) as RoomDraft
  } catch {
    return null
  }
}

export function clearRoomDraft(): void {
  sessionStorage.removeItem(DRAFT_KEY)
}

export interface FormDraft {
  dormitoryId: string | number
  dormitoryName: string
  roomTypeCode: string | number
  roomTypeDesc: string
}

const FORM_KEY = 'check-in-form'

/**
 * Building/room-type selection persisted across the select-room round trip —
 * navigating away unmounts the page, so component state alone is lost.
 */
export function saveFormDraft(draft: FormDraft): void {
  sessionStorage.setItem(FORM_KEY, JSON.stringify(draft))
}

export function loadFormDraft(): FormDraft | null {
  const raw = sessionStorage.getItem(FORM_KEY)
  if (raw === null) return null
  try {
    return JSON.parse(raw) as FormDraft
  } catch {
    return null
  }
}

export function clearFormDraft(): void {
  sessionStorage.removeItem(FORM_KEY)
}
