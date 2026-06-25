export function buildRecentTaskQuery({ searchForm, selectedStaff }) {
  return {
    parkId: searchForm && searchForm.parkId,
    badge: selectedStaff ? selectedStaff.badge : null
  }
}

function clearStaffResult(staffCandidates, message) {
  return {
    staffCandidates,
    selectedStaff: null,
    staffCards: [],
    staffToSelect: null,
    message
  }
}

function selectStaffResult(staffCandidates, staffToSelect) {
  return {
    staffCandidates,
    selectedStaff: null,
    staffCards: [],
    staffToSelect,
    message: null
  }
}

export function resolveNameSearchResult(records, keyword) {
  if (!records.length) {
    return clearStaffResult([], { message: '未找到匹配员工', type: 'warning' })
  }
  const exactMatches = records.filter(item => item.name === keyword)
  if (exactMatches.length === 1) {
    return selectStaffResult(records, exactMatches[0])
  }
  return clearStaffResult(records, { message: `找到${records.length}名候选员工，请手动选择`, type: 'warning' })
}

export function resolveBadgeSearchResult(records) {
  if (!records.length) {
    return clearStaffResult([], { message: '未找到该工号对应员工', type: 'warning' })
  }
  if (records.length === 1) {
    return selectStaffResult(records, records[0])
  }
  return clearStaffResult(records, { message: `找到${records.length}名候选员工，请手动选择`, type: 'warning' })
}

export async function runNameStaffSearch({
  keyword,
  selectedPark,
  requestStaffByName
}) {
  const records = await requestStaffByName(keyword, selectedPark)
  return resolveNameSearchResult(records, keyword)
}

export async function runBadgeStaffSearch({
  badge,
  selectedPark,
  fallbackToName,
  readFallbackPark,
  requestStaffByBadge,
  requestStaffByName
}) {
  const records = await requestStaffByBadge(badge, selectedPark)
  if (!records.length && fallbackToName) {
    const fallbackPark = readFallbackPark ? readFallbackPark() : selectedPark
    return runNameStaffSearch({
      keyword: badge,
      selectedPark: fallbackPark,
      requestStaffByName
    })
  }
  return resolveBadgeSearchResult(records)
}

export function canApplyStaffCardResult(selectedStaff, requestStaffId) {
  return !!(selectedStaff && selectedStaff.id === requestStaffId)
}

export const emptyStaffCardState = () => ({
  staffCards: [],
  staffCardLoading: false
})
